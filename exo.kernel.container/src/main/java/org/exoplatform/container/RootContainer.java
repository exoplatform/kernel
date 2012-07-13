/*
 * Copyright (C) 2009 eXo Platform SAS.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.exoplatform.container;

import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.exoplatform.container.configuration.MockConfigurationManagerImpl;
import org.exoplatform.container.definition.PortalContainerConfig;
import org.exoplatform.container.definition.PortalContainerDefinition;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.monitor.jvm.OperatingSystemInfo;
import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.test.mocks.servlet.MockServletContext;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;
import org.gatein.wci.authentication.AuthenticationEvent;
import org.gatein.wci.authentication.AuthenticationListener;
import org.gatein.wci.impl.DefaultServletContainerFactory;
import org.picocontainer.PicoException;

import java.io.File;
import java.lang.ref.WeakReference;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Jul 21, 2004 Time: 12:15:28 AM
 */
@Managed
@NameTemplate(@Property(key = "container", value = "root"))
@RESTEndpoint(path = "rcontainer")
public class RootContainer extends ExoContainer implements WebAppListener, AuthenticationListener
{

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = 812448359436635438L;

   /**
    * The name of the attribute used to mark a session as to be invalidated
    */
   public static final String SESSION_TO_BE_INVALIDATED_ATTRIBUTE_NAME = RootContainer.class.getName()
      + "_TO_BE_INVALIDATED";

   /** The field is volatile to properly implement the double checked locking pattern. */
   private static volatile RootContainer singleton_;

   private OperatingSystemInfo osenv_;

   private PortalContainerConfig config_;

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.RootContainer");
   
   private static final AtomicBoolean booting = new AtomicBoolean();

   private final J2EEServerInfo serverenv_ = new J2EEServerInfo();

   private final Set<String> profiles;
   
   private final Thread hook = new ShutdownThread(this);
   
   private final AtomicBoolean reloading = new AtomicBoolean();
   
   private final AtomicLong lastUpdateTime = new AtomicLong();
   
   private ClassLoader loadingCL;
   
   private Properties loadingSystemProperties;
   
   private volatile Thread reloadingThread;

   /**
    * The list of all the tasks to execute while initializing the corresponding portal containers
    */
   private ConcurrentMap<String, ConcurrentMap<String, Queue<PortalContainerInitTaskContext>>> initTasks =
      new ConcurrentHashMap<String, ConcurrentMap<String, Queue<PortalContainerInitTaskContext>>>();

   /**
    * The list of the web application contexts corresponding to all the portal containers
    */
   private Set<WebAppInitContext> portalContexts = new CopyOnWriteArraySet<WebAppInitContext>();

   /**
    * The list of the all the existing sessions, this will be used in developing mode only
    */
   private final Set<WeakHttpSession> sessions = new CopyOnWriteArraySet<WeakHttpSession>();

   /**
    * The list of the all the portal containers to reload
    */
   private final Set<String> portalContainer2Reload = new CopyOnWriteArraySet<String>();

   public RootContainer()
   {
      Set<String> profiles = new HashSet<String>();

      // Add the profile defined by the server name
      String envProfile = serverenv_.getServerName();
      if (envProfile != null)
      {
         profiles.add(envProfile);
      }

      // Obtain profile list by runtime properties
      profiles.addAll(ExoContainer.getProfiles());

      // Lof the active profiles
      LOG.info("Active profiles " + profiles);

      //
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            Runtime.getRuntime().addShutdownHook(hook);
            return null;
         }
      });
      this.profiles = profiles;
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            registerComponentInstance(J2EEServerInfo.class, serverenv_);
            if (PropertyManager.isDevelopping())
            {
               loadingCL = Thread.currentThread().getContextClassLoader();
               loadingSystemProperties = (Properties)System.getProperties().clone();
            }
            return null;
         }
      });
   }

   public OperatingSystemInfo getOSEnvironment()
   {
      if (osenv_ == null)
      {
         osenv_ = (OperatingSystemInfo)this.getComponentInstanceOfType(OperatingSystemInfo.class);
      }
      return osenv_;
   }

   /**
    * @return the {@link PortalContainerConfig} corresponding to the {@link RootContainer}
    */
   PortalContainerConfig getPortalContainerConfig()
   {
      if (config_ == null)
      {
         config_ = (PortalContainerConfig)this.getComponentInstanceOfType(PortalContainerConfig.class);
      }
      return config_;
   }

   /**
    * Indicates if the current instance is aware of the {@link PortalContainerConfig}
    * @return <code>true</code> if we are using the old way to configure the portal containers,
    * <code>false</code> otherwise
    */
   public boolean isPortalContainerConfigAware()
   {
      return getPortalContainerConfig().hasDefinition();
   }

   public J2EEServerInfo getServerEnvironment()
   {
      return serverenv_;
   }   
   
   /**
    * {@inheritDoc}
    */
   @Override
   public Object getComponentInstance(Object componentKey) throws PicoException
   {
      if (reloading.get())
      {
         // To prevent any early access to the portal container
         synchronized(RootContainer.class) {}
      }
      return super.getComponentInstance(componentKey);
   }

   public PortalContainer getPortalContainer(final String name)
   {
      PortalContainer pcontainer = (PortalContainer)this.getComponentInstance(name);
      if (pcontainer == null)
      {
         J2EEServerInfo senv = getServerEnvironment();
         if ("standalone".equals(senv.getServerName()) || "test".equals(senv.getServerName()))
         {
            try
            {
               MockServletContext scontext = new MockServletContext(name);
               pcontainer = new PortalContainer(this, scontext);
               final PortalContainer currentPortalContainer = pcontainer;
               SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
               {
                  public Void run()
                  {
                     PortalContainer.setInstance(currentPortalContainer);
                     return null;
                  }
               });
               final ConfigurationManagerImpl cService = new MockConfigurationManagerImpl(scontext);
               cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/configuration.xml"));
               cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/test-configuration.xml"));
               cService.processRemoveConfiguration();
               SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
               {
                  public Void run()
                  {
                     currentPortalContainer.registerComponentInstance(ConfigurationManager.class, cService);
                     registerComponentInstance(name, currentPortalContainer);
                     currentPortalContainer.start(true);
                     return null;
                  }
               });
            }
            catch (Exception ex)
            {
               LOG.error(ex.getLocalizedMessage(), ex);
            }
         }
      }
      return pcontainer;
   }

   /**
    * Register a new portal container. It will try to detect if {@link PortalContainerDefinition} has
    *  been defined, if so it will create the portal container later otherwise we assume that we 
    * expect the old behavior, thus the portal container will be initialized synchronously 
    * @param context the context of the portal container
    */
   public void registerPortalContainer(ServletContext context)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);
      
      PortalContainerConfig config = getPortalContainerConfig();
      if (config.hasDefinition())
      {
         // The new behavior has been detected thus, the creation will be done at the end asynchronously
         if (config.isPortalContainerName(context.getServletContextName()))
         {
            // The portal context has been registered has a portal container
            portalContexts.add(new WebAppInitContext(context));
         }
         else
         {
            if (PropertyManager.isDevelopping())
            {
               LOG.info("We assume that the ServletContext '" + context.getServletContextName()
                  + "' is not a portal since no portal container definition with the same name has been"
                  + " registered to the component PortalContainerConfig. The related portal container"
                  + " will be declared as disabled.");
            }
            config.disablePortalContainer(context.getServletContextName());
         }
         // We assume that a ServletContext of a portal container owns configuration files
         PortalContainer.addInitTask(context, new PortalContainer.RegisterTask());
      }
      else
      {
         // Ensure that the portal container has been registered
         config.registerPortalContainerName(context.getServletContextName());
         // The old behavior has been detected thus, the creation will be done synchronously
         createPortalContainer(context);
      }
   }

   /**
    * Creates all the portal containers that have been registered thanks to the method 
    * <code>registerPortalContainer</code>
    */
   public synchronized void createPortalContainers()
   {
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      boolean hasChanged = false;
      try
      {
         for (Iterator<WebAppInitContext> it = portalContexts.iterator();it.hasNext();)
         {
            WebAppInitContext context = it.next();
            // Set the context classloader of the related web application
            Thread.currentThread().setContextClassLoader(context.getWebappClassLoader());
            hasChanged = true;
            createPortalContainer(context.getServletContext());
         }
      }
      finally
      {
         if (hasChanged)
         {
            // Re-set the old classloader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
         }
      }
      if (PropertyManager.isDevelopping())
      {
         DefaultServletContainerFactory.getInstance().getServletContainer().addWebAppListener(this);
         DefaultServletContainerFactory.getInstance().getServletContainer().addAuthenticationListener(this);
      }
      else
      {
         PortalContainerConfig config = getPortalContainerConfig();
         for (String portalContainerName : initTasks.keySet())
         {
            if (config.isPortalContainerName(portalContainerName))
            {
               // Unregister name of portal container that doesn't exist
               LOG.warn("The portal container '" + portalContainerName + "' doesn't exist or"
                  + " it has not yet been registered, please check your PortalContainerDefinitions and "
                  + "the loading order.");
               config.unregisterPortalContainerName(portalContainerName);            
            }
         }
         // remove all the registered web application contexts 
         // corresponding to the portal containers
         portalContexts.clear();
         // remove all the unneeded tasks
         initTasks.clear();
      }
   }
   
   /**
    * {@inheritDoc}
    */
   public void onEvent(WebAppEvent event)
   {
      if (event instanceof WebAppLifeCycleEvent && !stopping.get())
      {
         WebAppLifeCycleEvent waEvent = (WebAppLifeCycleEvent)event;
         if (waEvent.getType() == WebAppLifeCycleEvent.REMOVED)
         {
            String contextName = event.getWebApp().getServletContext().getServletContextName();
            boolean updated = false;
            for (Entry<String, ConcurrentMap<String, Queue<PortalContainerInitTaskContext>>> entry : initTasks.entrySet())
            {
               String portalContainer = entry.getKey();
               ConcurrentMap<String, Queue<PortalContainerInitTaskContext>> queues = entry.getValue();
               for (Queue<PortalContainerInitTaskContext> queue : queues.values())
               {
                  for (Iterator<PortalContainerInitTaskContext> it = queue.iterator(); it.hasNext();)
                  {
                     PortalContainerInitTaskContext context = it.next();
                     if (context.getServletContextName().equals(contextName))
                     {
                        it.remove();
                        portalContainer2Reload.add(portalContainer);
                        updated = true;
                        lastUpdateTime.set(System.currentTimeMillis());
                     }
                  }
               }
            }
            if (updated)
            {
               LOG.info("The webapp '" + contextName + "' has been undeployed, the related init tasks have been removed");
            }
         }
         else if (waEvent.getType() == WebAppLifeCycleEvent.ADDED && lastUpdateTime.get() > 0 && reloadingThread == null)
         {
            // Reloading thread used to reload asynchronously the containers
            reloadingThread = new Thread("Reloading")
            {
               @Override
               public void run()
               {
                  // We delay the reloading to ensure that there is no other webapp that will be reloaded
                  long pause = 5000;
                  do
                  {
                     try
                     {
                        sleep(500);
                     }
                     catch (InterruptedException e)
                     {
                        interrupted();
                     }
                  }
                  while (System.currentTimeMillis() < lastUpdateTime.get() + pause);
                  dynamicReload();
               }
            };
            reloadingThread.setDaemon(true);
            reloadingThread.start();
         }
      }
   }
   
   private void dynamicReload()
   {
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      final Properties currentSystemProperties = System.getProperties();
      boolean hasChanged = false;
      Configuration newConfig = null;
      try
      {
         Thread.currentThread().setContextClassLoader(loadingCL);
         hasChanged = true;
         System.setProperties(loadingSystemProperties);
         ConfigurationManager cm = loadConfigurationManager(this, false);
         if (cm != null)
         {
            newConfig = cm.getConfiguration();
         }
      }
      catch (Exception e)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Could not load the new configuration of the root container", e);
         }
      }
      finally
      {
         if (hasChanged)
         {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
            System.setProperties(currentSystemProperties);
         }         
      }
      if (newConfig == null)
      {
         // We have no way to know if the configuration of the root container has changed so
         // we reload everything
         LOG.info("The new configuration of the root container could not be loaded,"
            + " thus everything will be reloaded");
         reload();
         return;
      }
      Configuration currentConfig = getConfiguration();
      if (currentConfig == null)
      {
         // We have no way to know if the configuration of the root container has changed so
         // we reload everything
         LOG.info("The current configuration of the root container could not be loaded," +
                  " thus everything will be reloaded");
         reload();
         return;         
      }
      if (newConfig.getCurrentSize() != currentConfig.getCurrentSize()
         || newConfig.getCurrentHash() != currentConfig.getCurrentHash())
      {
         // The root container has changed so we reload everything
         LOG.info("The configuration of the root container has changed," +
                  " thus everything will be reloaded");
         reload();
         return;         
      }
      LOG.info("The configuration of the root container did not change," +
               " thus only affected portal containers will be reloaded");
      for (String pc : portalContainer2Reload)
      {
         // At least one dependency has changed so we reload all the affected portal containers
         reload(pc);
      }
   }
   
   /**
    * Adds a session attribute indicating that the sessions have to be invalidated
    */
   private void markSessionsAsToBeInvalidated(String portalContainerName)
   {
      for (WeakHttpSession wSess : sessions)
      {
         HttpSession sess = wSess.get();
         if (sess == null)
         {
            continue;
         }
         else if (portalContainerName == null
            || portalContainerName.equals(sess.getServletContext().getServletContextName()))
         {
            try
            {
               sess.setAttribute(SESSION_TO_BE_INVALIDATED_ATTRIBUTE_NAME, Boolean.TRUE);
            }
            catch (IllegalStateException e)
            {
               if (LOG.isDebugEnabled())
               {
                  LOG.debug("Could not set the flag indicating that the session must be invalidated", e);
               }
            }
         }
      }
      sessions.clear();
   }
   
   /**
    * {@inheritDoc}
    */
   public void onLogin(AuthenticationEvent evt)
   {
      HttpSession sess = evt.getRequest().getSession(false);

      if (sess == null)
         return;
      if (getPortalContainerConfig().isPortalContainerName(sess.getServletContext().getServletContextName()))
      {
         sessions.add(new WeakHttpSession(sess));
      }
   }

   /**
    * {@inheritDoc}
    */
   public void onLogout(AuthenticationEvent evt)
   {
      HttpSession sess = evt.getRequest().getSession(false);

      if (sess == null)
         return;
      if (getPortalContainerConfig().isPortalContainerName(sess.getServletContext().getServletContextName()))
      {
         sessions.remove(new WeakHttpSession(sess));
      }
   }

   public synchronized void createPortalContainer(ServletContext context)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      boolean hasChanged = false;
      final String portalContainerName = context.getServletContextName();
      try
      {
         LOG.info("Trying to create the portal container '" + portalContainerName + "'");
         PortalContainer pcontainer = new PortalContainer(this, context);
         PortalContainer.setInstance(pcontainer);
         executeInitTasks(pcontainer, PortalContainerPreInitTask.TYPE);
         // Set the full classloader of the portal container
         Thread.currentThread().setContextClassLoader(pcontainer.getPortalClassLoader());
         hasChanged = true;
         ConfigurationManagerImpl cService = new ConfigurationManagerImpl(pcontainer.getPortalContext(), profiles);

         // add configs from services
         try
         {
            cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/configuration.xml"));
         }
         catch (Exception ex)
         {
            LOG.error("Cannot add configuration conf/portal/configuration.xml. ServletContext: " + context, ex);
         }

         // Add configuration that depends on the environment
         String uri;
         if (serverenv_.isJBoss())
         {
            uri = "conf/portal/jboss-configuration.xml";
         }
         else
         {
            uri = "conf/portal/generic-configuration.xml";
         }
         Collection envConf = ContainerUtil.getConfigurationURL(uri);
         try
         {
            cService.addConfiguration(envConf);
         }
         catch (Exception ex)
         {
            LOG.error("Cannot add configuration " + uri + ". ServletContext: " + context, ex);
         }

         // add configs from web apps
         Set<WebAppInitContext> contexts = pcontainer.getWebAppInitContexts();
         for (WebAppInitContext webappctx : contexts)
         {
            ServletContext ctx = webappctx.getServletContext();
            try
            {
               cService.addConfiguration(ctx, "war:/conf/configuration.xml");
            }
            catch (Exception ex)
            {
               LOG.error("Cannot add configuration war:/conf/configuration.xml. ServletContext: " + ctx, ex);
            }
         }

         // add config from application server,
         // $AH_HOME/exo-conf/portal/configuration.xml
         String overrideConfig =
            getServerEnvironment().getExoConfigurationDirectory() + "/portal/" + portalContainerName
               + "/configuration.xml";
         try
         {
            File file = new File(overrideConfig);
            if (file.exists())
               cService.addConfiguration(file.toURI().toURL());
         }
         catch (Exception ex)
         {
            LOG.error("Cannot add configuration " + overrideConfig + ". ServletContext: " + context, ex);
         }

         cService.processRemoveConfiguration();
         pcontainer.registerComponentInstance(ConfigurationManager.class, cService);
         registerComponentInstance(portalContainerName, pcontainer);
         pcontainer.start(true);

         // Register the portal as an mbean
         getManagementContext().register(pcontainer);

         //
         executeInitTasks(pcontainer, PortalContainerPostInitTask.TYPE);
         executeInitTasks(pcontainer, PortalContainerPostCreateTask.TYPE);
         LOG.info("The portal container '" + portalContainerName + "' has been created successfully");
      }
      catch (Exception ex)
      {
         LOG.error("Cannot create the portal container '" + portalContainerName + "' . ServletContext: " + context, ex);
      }
      finally
      {
         if (hasChanged)
         {
            // Re-set the old classloader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
         }
         try
         {
            PortalContainer.setInstance(null);
         }
         catch (Exception e)
         {
            LOG.warn("An error occured while cleaning the ThreadLocal", e);
         }
      }
   }

   synchronized public void removePortalContainer(ServletContext servletContext)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      this.unregisterComponent(servletContext.getServletContextName());
   }

   public static Object getComponent(Class key)
   {
      return getInstance().getComponentInstanceOfType(key);
   }

   /**
    * Builds a root container and returns it.
    *
    * @return a root container
    * @throws Error if the root container initialization failed
    */
   private static RootContainer buildRootContainer()
   {
      try
      {
         final RootContainer rootContainer = new RootContainer();
         final ConfigurationManager service = loadConfigurationManager(rootContainer, true);
         SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
         {
            public Void run()
            {
               rootContainer.registerComponentInstance(ConfigurationManager.class, service);
               rootContainer.start(true);
               return null;
            }
         });
         return rootContainer;
      }
      catch (Exception e)
      {
         LOG.error("Could not build root container", e);
         // The logger is not necessary configured so we have to use the standard
         // output stream
         LOG.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   /**
    * @param rootContainer
    * @return
    * @throws Exception
    */
   private static ConfigurationManager loadConfigurationManager(RootContainer rootContainer, boolean logEnabled) throws Exception
   {
      final ConfigurationManagerImpl service = new ConfigurationManagerImpl(rootContainer.profiles, logEnabled);
      service.addConfiguration(ContainerUtil.getConfigurationURL("conf/configuration.xml"));
      if (PrivilegedSystemHelper.getProperty("maven.exoplatform.dir") != null)
      {
         service.addConfiguration(ContainerUtil.getConfigurationURL("conf/test-configuration.xml"));
      }
      String confDir = rootContainer.getServerEnvironment().getExoConfigurationDirectory();
      String overrideConf = confDir + "/configuration.xml";
      File file = new File(overrideConf);
      if (PrivilegedFileHelper.exists(file))
      {
         service.addConfiguration("file:" + overrideConf);
      }
      service.processRemoveConfiguration();
      if (PropertyManager.isDevelopping())
      {
         Configuration conf = service.getConfiguration();
         if (conf != null)
         {
            conf.keepCurrentState();
         }
      }
      return service;
   }

   /**
    * Get the unique instance of the root container per VM. The implementation relies on the double
    * checked locking pattern to guarantee that only one instance will be initialized. See
    *
    * @return the root container singleton
    */
   public static RootContainer getInstance()
   {
      RootContainer result = singleton_;
      if (result == null)
      {
         synchronized (RootContainer.class)
         {
            result = singleton_;
            if (result == null)
            {
               if (booting.get())
               {
                  throw new IllegalStateException("Already booting by the same thread");
               }
               else
               {
                  booting.set(true);
                  try
                  {
                     LOG.info("Building root container");
                     long time = -System.currentTimeMillis();
                     result = buildRootContainer();
                     if (result != null)
                     {
                        time += System.currentTimeMillis();
                        LOG.info("Root container is built (build time " + time + "ms)");
                        singleton_ = result;
                        SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
                        {
                           public Void run()
                           {
                              ExoContainerContext.setTopContainer(singleton_);
                              return null;
                           }
                        }); 
                        LOG.info("Root container booted");
                     }
                     else
                     {
                        LOG.error("Failed to boot root container");
                     }
                  }
                  finally
                  {
                     booting.set(false);
                  }
               }
            }
         }
      }
      return result;
   }

   static public void setInstance(RootContainer rcontainer)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      singleton_ = rcontainer;
   }

   @Managed
   @ManagedDescription("The configuration of the container in XML format.")
   public String getConfigurationXML()
   {
      Configuration config = getConfiguration();
      if (config == null)
      {
         LOG.warn("The configuration of the RootContainer could not be found");
         return null;
      }
      return config.toXML();
   }
   
   @Managed
   @ManagedDescription("Make the RootContainer reloads itself and all the portal containers.")
   public void reload()
   {
      if (!PropertyManager.isDevelopping())
      {
         LOG.debug("The containers can be reloaded only in developping mode, "
            + "please set the system property 'exo.product.developing' to 'true'");
         return;
      }
      else if (stopping.get())
      {
         LOG.debug("The containers cannot be reloaded as we are currently stopping the root container.");
         return;         
      }
      try
      {
         long time = System.currentTimeMillis();
         LOG.info("Trying to reload all the containers");
         LOG.info("Trying to stop all the containers");
         stop();
         markSessionsAsToBeInvalidated(null);
         synchronized (RootContainer.class)
         {
            // Make early accesses to the root container to wait
            singleton_ = null;
            LOG.info("All the containers have been stopped successfully");
            // We unregister the root container
            SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
            {
               public Void run()
               {
                  Runtime.getRuntime().removeShutdownHook(hook);
                  return null;
               }
            });
            DefaultServletContainerFactory.getInstance().getServletContainer().removeWebAppListener(this);
            DefaultServletContainerFactory.getInstance().getServletContainer().removeAuthenticationlistener(this);
            LOG.info("Trying to restart the root container");
            RootContainer rootContainer = null;
            final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            boolean hasChanged = false;
            try
            {
               Thread.currentThread().setContextClassLoader(loadingCL);
               hasChanged = true;
               System.setProperties(loadingSystemProperties);
               rootContainer = RootContainer.getInstance();
               rootContainer.reloading.set(true);
               rootContainer.initTasks = initTasks;
               rootContainer.portalContexts = portalContexts;
               PortalContainer.reloadConfig();
               LOG.info("Trying to restart all the portal containers");
               rootContainer.createPortalContainers();
            }
            finally
            {
               if (rootContainer != null)
               {
                  rootContainer.reloading.set(false);                  
               }
               if (hasChanged)
               {
                  Thread.currentThread().setContextClassLoader(currentClassLoader);
               }
            }            
         }
         LOG.info("All the containers have been reloaded successfully in " + (System.currentTimeMillis() - time) + " ms");
      }
      catch (Exception e)
      {
         LOG.error("Could not reload the containers", e);
      }
   }

   @Managed
   @ManagedDescription("Make the RootContainer reloads only a given portal container.")
   public void reload(String portalContainerName)
   {
      if (!PropertyManager.isDevelopping())
      {
         LOG.debug("The portal container '"
            + portalContainerName
            + "' can be reloaded only in developping mode, please set the system property 'exo.product.developing' to 'true'");
         return;
      }
      else if (stopping.get())
      {
         LOG.debug("The portal container '" + portalContainerName
            + "' cannot be reloaded as we are currently stopping the root container.");
         return;         
      }
      try
      {
         long time = System.currentTimeMillis();
         LOG.info("Trying to reload the portal container '" + portalContainerName + "'");
         PortalContainer pc = getPortalContainer(portalContainerName);
         if (pc == null)
         {
            throw new IllegalArgumentException("The portal container '" + portalContainerName
               + "' doesn't exists or has not yet been created");
         }
         LOG.info("Trying to stop the portal container '" + portalContainerName + "'");
         pc.stop();
         markSessionsAsToBeInvalidated(portalContainerName);
         synchronized (RootContainer.class)
         {
            LOG.info("The portal container '" + portalContainerName + "' has been stopped successfully");
            final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            boolean hasChanged = false;
            try
            {
               reloading.set(true);
               // Make early accesses to the root container to wait
               unregisterComponent(portalContainerName);
               LOG.info("Trying to restart the portal container '" + portalContainerName + "'");
               for (Iterator<WebAppInitContext> it = portalContexts.iterator();it.hasNext();)
               {
                  WebAppInitContext context = it.next();
                  if (context.getServletContextName().equals(portalContainerName))
                  {
                     // Set the context classloader of the related web application
                     Thread.currentThread().setContextClassLoader(context.getWebappClassLoader());
                     hasChanged = true;
                     createPortalContainer(context.getServletContext());
                     break;
                  }
               }
            }
            finally
            {
               if (hasChanged)
               {
                  // Re-set the old classloader
                  Thread.currentThread().setContextClassLoader(currentClassLoader);
               }
               reloading.set(false);
            }               
         }
         LOG.info("The portal container '" + portalContainerName + "' has been reloaded successfully in "
            + (System.currentTimeMillis() - time) + " ms");
      }
      catch (Exception e)
      {
         LOG.error("Could not reload the portal container '" + portalContainerName + "'", e);
      }      
   }
   
   /**
    * Calls the other method <code>addInitTask</code> with <code>ServletContext.getServletContextName()</code>
    * as portal container name
    * 
    * @param context the servlet context from which the task comes from
    * @param task the task to add
    */
   public void addInitTask(ServletContext context, PortalContainerInitTask task)
   {
      addInitTask(context, task, context.getServletContextName());
   }

   /**
    * First check if the related portal container has already been initialized. If so
    * it will call the method onAlreadyExists on the given task otherwise the task will
    * be added to the task list to execute during the related portal container initialization
    * 
    * @param context the servlet context from which the task comes from
    * @param task the task to add
    * @param portalContainer the name of the portal container on which the task must be executed
    */
   public void addInitTask(ServletContext context, PortalContainerInitTask task, String portalContainer)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);
      
      final PortalContainer container = getPortalContainer(portalContainer);
      if (!task.alreadyExists(container) || lastUpdateTime.get() > 0)
      {
         if (LOG.isDebugEnabled())
            LOG.debug("The portal container '" + portalContainer
               + "' has not yet been initialized, thus the task can be added");
         ConcurrentMap<String, Queue<PortalContainerInitTaskContext>> queues = initTasks.get(portalContainer);
         if (queues == null)
         {
            queues = new ConcurrentHashMap<String, Queue<PortalContainerInitTaskContext>>();
            final ConcurrentMap<String, Queue<PortalContainerInitTaskContext>> q =
               initTasks.putIfAbsent(portalContainer, queues);
            if (q != null)
            {
               queues = q;
            }
         }
         final String type = task.getType();
         Queue<PortalContainerInitTaskContext> queue = queues.get(type);
         if (queue == null)
         {
            final List<String> dependencies = getPortalContainerConfig().getDependencies(portalContainer);
            if (dependencies == null || dependencies.isEmpty())
            {
               // No order is required
               queue = new ConcurrentLinkedQueue<PortalContainerInitTaskContext>();
            }
            else
            {
               queue =
                  new PriorityBlockingQueue<PortalContainerInitTaskContext>(10,
                     new PortalContainerInitTaskContextComparator(dependencies));
            }
            final Queue<PortalContainerInitTaskContext> q = queues.putIfAbsent(type, queue);
            if (q != null)
            {
               queue = q;
            }
         }
         else if (reloading.get())
         {
            // The queue already exists and we are in reloading phase, we will then check
            // if a task of the same type exist for the same servlet context if so we replace it
            // with a new one
            String contextName = context.getServletContextName();
            Class<?> c = task.getClass();
            for (Iterator<PortalContainerInitTaskContext> it = queue.iterator(); it.hasNext();)
            {
               PortalContainerInitTaskContext ctx = it.next();
               if (ctx.getServletContextName().equals(contextName) && ctx.getTask().getClass().equals(c))
               {
                  it.remove();
                  break;
               }
            }
         }
         queue.add(new PortalContainerInitTaskContext(context, task));
      }
      else
      {
         if (LOG.isDebugEnabled())
            LOG.debug("The portal container '" + portalContainer
               + "' has already been initialized, thus we call onAlreadyExists");
         PortalContainer oldPortalContainer = PortalContainer.getInstanceIfPresent();
         try
         {
            PortalContainer.setInstance(container);
            task.onAlreadyExists(context, container);
         }
         finally
         {
            PortalContainer.setInstance(oldPortalContainer);
         }
      }
   }

   /**
    * Executes all the tasks of the given type related to the given portal container
    * @param portalContainer the portal container on which we want to execute the tasks
    * @param type the type of the task to execute
    */
   private void executeInitTasks(PortalContainer portalContainer, String type)
   {
      final String portalContainerName = portalContainer.getName();
      final ConcurrentMap<String, Queue<PortalContainerInitTaskContext>> queues = initTasks.get(portalContainerName);
      if (queues == null)
      {
         return;
      }
      final Queue<PortalContainerInitTaskContext> queue = queues.get(type);
      if (queue == null)
      {
         return;
      }
      if (LOG.isDebugEnabled())
         LOG.debug("Start launching the " + type + " tasks of the portal container '" + portalContainer + "'");
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      PortalContainerInitTaskContext context;
      boolean hasChanged = false;
      Collection<PortalContainerInitTaskContext> bckCollection = null;
      if (PropertyManager.isDevelopping())
      {
         bckCollection = new ArrayList<PortalContainerInitTaskContext>(queue);
      }
      try
      {
         while ((context = queue.poll()) != null)
         {
            // Set the context classloader of the related web application
            Thread.currentThread().setContextClassLoader(context.getWebappClassLoader());
            hasChanged = true;
            context.getTask().execute(context.getServletContext(), portalContainer);
         }
      }
      finally
      {
         if (hasChanged)
         {
            // Re-set the old classloader 
            Thread.currentThread().setContextClassLoader(currentClassLoader);
         }
      }
      if (PropertyManager.isDevelopping())
      {
         // reload the queue, it is required because PriorityBlockingQueue only guarantee the order 
         // when we use the method poll if we try to use the iterator the priority is not respected
         queue.addAll(bckCollection);
      }
      else
      {
         // Clear the queue of task
         queue.clear();
         // Remove this specific type of PortalContainerInitTaskContext from the existing queues 
         queues.remove(type);
         if (queues.isEmpty())
         {
            // If there is no queue anymore remove init tasks holder for this portal container
            initTasks.remove(portalContainerName);
         }         
      }
      if (LOG.isDebugEnabled())
         LOG.debug("End launching the " + type + " tasks of the portal container '" + portalContainer + "'");
   }

   static class ShutdownThread extends Thread
   {
      RootContainer container_;

      ShutdownThread(RootContainer container)
      {
         container_ = container;
      }

      @Override
      public void run()
      {
         SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
         {
            public Void run()
            {
               container_.stop();
               return null;
            }
         });
      }
   }

   @Override
   public void stop()
   {
      super.stop();
      ExoContainerContext.setTopContainer(null);
   }

   /**
    * This interface is used to define a task that needs to be launched at a given state during the 
    * initialization of a portal container
    */
   public static interface PortalContainerInitTask
   {

      /**
       * This method allows the implementation to define what the state "already exists"
       * means for a portal container
       * 
       * @param portalContainer the value of the current portal container
       * @return <code>true</code> if the portal container exists according to the task
       * requirements, <code>false</code> otherwise
       */
      public boolean alreadyExists(PortalContainer portalContainer);

      /**
       * This method is called if the related portal container has already been registered
       * 
       * @param context the servlet context of the web application
       * @param portalContainer the value of the current portal container
       */
      public void onAlreadyExists(ServletContext context, PortalContainer portalContainer);

      /**
       * Executes the task
       * 
       * @param context the servlet context of the web application
       * @param portalContainer The portal container on which we would like to execute the task
       */
      public void execute(ServletContext context, PortalContainer portalContainer);

      /**
       * @return the type of the task
       */
      public String getType();
   }

   /**
    * This class is used to define a task that needs to be launched after the initialization of a
    * portal container
    */
   public static abstract class PortalContainerPostInitTask implements PortalContainerInitTask
   {

      /**
       * The name of the type of task
       */
      public static final String TYPE = "post-init";

      /**
       * {@inheritDoc}
       */
      public final boolean alreadyExists(PortalContainer portalContainer)
      {
         return portalContainer != null && portalContainer.isStarted();
      }

      /**
       * {@inheritDoc}
       */
      public final void onAlreadyExists(ServletContext context, PortalContainer portalContainer)
      {
         execute(context, portalContainer);
      }

      /**
       * {@inheritDoc}
       */
      public final String getType()
      {
         return TYPE;
      }
   }

   /**
    * This class is used to define a task that needs to be launched before the initialization of a
    * portal container
    */
   public static abstract class PortalContainerPreInitTask implements PortalContainerInitTask
   {

      /**
       * The name of the type of task
       */
      public static final String TYPE = "pre-init";

      /**
       * {@inheritDoc}
       */
      public final boolean alreadyExists(PortalContainer portalContainer)
      {
         return portalContainer != null;
      }

      /**
       * {@inheritDoc}
       */
      public final void onAlreadyExists(ServletContext context, PortalContainer portalContainer)
      {
         throw new IllegalStateException("No pre init tasks can be added to the portal container '"
            + portalContainer.getName() + "', because it has already been " + "initialized. Check the webapp '"
            + context.getServletContextName() + "'");
      }

      /**
       * {@inheritDoc}
       */
      public final String getType()
      {
         return TYPE;
      }
   }

   /**
    * This class is used to define a task that needs to be launched after creating a portal container
    * Those type of tasks must be launched after all the "post-init" tasks. 
    */
   public static abstract class PortalContainerPostCreateTask implements PortalContainerInitTask
   {

      /**
       * The name of the type of task
       */
      public static final String TYPE = "post-create";

      /**
       * {@inheritDoc}
       */
      public final boolean alreadyExists(PortalContainer portalContainer)
      {
         return portalContainer != null && portalContainer.isStarted();
      }

      /**
       * {@inheritDoc}
       */
      public final void onAlreadyExists(ServletContext context, PortalContainer portalContainer)
      {
         execute(context, portalContainer);
      }

      /**
       * {@inheritDoc}
       */
      public final String getType()
      {
         return TYPE;
      }
   }

   /**
    * This class is used to defined the context of the embedded {@link PortalContainerInitTask}
    */
   static class PortalContainerInitTaskContext extends WebAppInitContext
   {

      /**
       * The task to execute
       */
      private final PortalContainerInitTask task;

      PortalContainerInitTaskContext(ServletContext context, PortalContainerInitTask task)
      {
         super(context);
         this.task = task;
      }

      public PortalContainerInitTask getTask()
      {
         return task;
      }
   }

   /**
    * This class is used to compare the {@link PortalContainerInitTaskContext}
    */
   static class PortalContainerInitTaskContextComparator implements Comparator<PortalContainerInitTaskContext>
   {

      private final List<String> dependencies;

      PortalContainerInitTaskContextComparator(List<String> dependencies)
      {
         this.dependencies = dependencies;
      }

      /**
       * This will sort all the {@link PortalContainerInitTaskContext} such that we will first have
       * all the web applications defined in the list of dependencies of the 
       * related portal container (see {@link PortalContainerConfig} for more details
       *  about the dependencies) ordered in the same order as the dependencies, then
       *  we will have all the web applications undefined ordered by context name
       */
      public int compare(PortalContainerInitTaskContext ctx1, PortalContainerInitTaskContext ctx2)
      {
         int idx1 = dependencies.indexOf(ctx1.getServletContextName());
         int idx2 = dependencies.indexOf(ctx2.getServletContextName());
         if (idx1 == -1 && idx2 != -1)
         {
            return 1;
         }
         else if (idx1 != -1 && idx2 == -1)
         {
            return -1;
         }
         else if (idx1 == -1 && idx2 == -1)
         {
            return ctx1.getServletContextName().compareTo(ctx2.getServletContextName());
         }
         else
         {
            return idx1 - idx2;
         }
      }
   }
   
   private static class WeakHttpSession extends WeakReference<HttpSession>
   {
      public WeakHttpSession(HttpSession session)
      {
         super(session);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         WeakHttpSession other = (WeakHttpSession)obj;
         HttpSession session = get();
         HttpSession otherSession = other.get();
         if (session == null)
         {
            return otherSession == null;
         }
         else if (otherSession == null)
         {
            return false;
         }
         return session.getId().equals(otherSession.getId());
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public int hashCode()
      {
         HttpSession session = get();
         return session == null ? 0 : session.getId().hashCode();
      }
   }
}
