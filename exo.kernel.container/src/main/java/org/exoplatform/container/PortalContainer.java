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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.RootContainer.PortalContainerInitTask;
import org.exoplatform.container.RootContainer.PortalContainerPreInitTask;
import org.exoplatform.container.definition.PortalContainerConfig;
import org.exoplatform.container.jmx.MX4JComponentAdapterFactory;
import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.NamingContext;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;

import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.ServletContext;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Date: Jul 31, 2003 Time: 12:15:28 AM
 */
@Managed
@NamingContext(@Property(key = "portal", value = "{Name}"))
@NameTemplate({@Property(key = "container", value = "portal"), @Property(key = "name", value = "{Name}")})
@RESTEndpoint(path = "pcontainer")
public class PortalContainer extends ExoContainer implements SessionManagerContainer
{

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = -9110532469581690803L;

   /**
    * The default name of the portal container
    */
   public static String DEFAULT_PORTAL_CONTAINER_NAME;

   /**
    * The default name of a the {@link ServletContext} of the rest web application
    */
   public static String DEFAULT_REST_CONTEXT_NAME;

   /**
    * The default name of a the realm
    */
   public static String DEFAULT_REALM_NAME;

   /**
    * The configuration of the portal containers
    */
   private static PortalContainerConfig CONFIG;
   static
   {
      reloadConfig();
   }

   private volatile boolean started_;

   private PortalContainerInfo pinfo_;

   private SessionManager smanager_;

   /**
    * The name of the portal container
    */
   private final String name;

   /**
    * The comparator used to sort the web applications by priorities 
    */
   private final Comparator<WebAppInitContext> webAppComparator;

   /**
    * The full {@link ServletContext} of the portal container after merging all the 
    * {@link ServletContext} that have been registered
    */
   private final ServletContext portalMergedContext;

   /**
    * The full {@link ClassLoader} of the portal container after merging all the {@link ClassLoader}
    * of the {@link ServletContext} that have been registered
    */
   private final ClassLoader portalMergedClassLoader;

   /**
    * The {@link Set} of all the web application context that share configuration
    */
   private volatile Set<WebAppInitContext> webAppContexts;

   /**
    * To allow overriding we need to have a custom {@link ClassLoader} by web applications by portal
    * containers
    */
   private volatile Map<String, ClassLoader> webAppClassLoaders;

   /**
    * The {@link ServletContext} of the current portal container
    */
   final ServletContext portalContext;

   public PortalContainer(RootContainer parent, final ServletContext portalContext)
   {
      super(new MX4JComponentAdapterFactory(), parent);
      this.name = portalContext.getServletContextName();
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            context.setName(name);
            return null;
         }
      });
      pinfo_ = new PortalContainerInfo(portalContext);
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            registerComponentInstance(ServletContext.class, portalContext);
            registerComponentInstance(PortalContainerInfo.class, pinfo_);
            return null;
         }
      });
      final PortalContainerConfig config = parent.getPortalContainerConfig();
      final List<String> dependencies = config == null ? null : config.getDependencies(name);
      if (dependencies == null || dependencies.isEmpty())
      {
         // No order is required
         this.webAppComparator = null;
      }
      else
      {
         this.webAppComparator = new WebAppInitContextComparator(dependencies);
      }
      this.webAppContexts = Collections.singleton(new WebAppInitContext(portalContext));
      this.portalContext = portalContext;
      this.portalMergedContext = new PortalContainerContext(this);
      this.portalMergedClassLoader = SecurityHelper.doPrivilegedAction(new PrivilegedAction<ClassLoader>()
      {
         public ClassLoader run()
         {
            return new PortalContainerClassLoader(PortalContainer.this);
         }
      });
      this.webAppClassLoaders = Collections.unmodifiableMap(Collections.singletonMap(name, portalMergedClassLoader));
   }

   /**
    * @return a {@link Set} ordered by priority of all the {@link WebAppInitContext} that represents 
    * the full portal context
    */
   Set<WebAppInitContext> getWebAppInitContexts()
   {
      return webAppContexts;
   }

   /**
    * This gives the merged {@link ClassLoader} between the {@link PortalContainerClassLoader} and the
    * {@link ClassLoader} of the web application.
    * 
    * @param context the {@link ServletContext} of the web application
    * @return the merged {@link ClassLoader} between the {@link PortalContainerClassLoader} and
    * the {@link ClassLoader} of the web application that allows us to override resources contained
    * into the {@link ClassLoader} of the web application 
    */
   public ClassLoader getWebAppClassLoader(ServletContext context)
   {
      final String contextName = context.getServletContextName();
      ClassLoader cl = webAppClassLoaders.get(contextName);
      if (cl == null)
      {
         synchronized (this)
         {
            cl = webAppClassLoaders.get(contextName);
            if (cl == null)
            {
               cl =
                  UnifiedClassLoader.createUnifiedClassLoaderInPrivilegedMode(new ClassLoader[]{
                     Thread.currentThread().getContextClassLoader(), portalMergedClassLoader});
               Map<String, ClassLoader> cls = new HashMap<String, ClassLoader>(webAppClassLoaders);
               cls.put(contextName, cl);
               this.webAppClassLoaders = Collections.unmodifiableMap(cls);
            }
         }
      }
      return cl;
   }

   /**
    * @return the full {@link ClassLoader} of the portal container after merging all the 
    * {@link ClassLoader} of all {@link ServletContext} that have been registered
    */
   public ClassLoader getPortalClassLoader()
   {
      return portalMergedClassLoader;
   }

   /**
    * @return the full {@link ServletContext} of the portal container after merging all the 
    * {@link ServletContext} that have been registered
    */
   public ServletContext getPortalContext()
   {
      return portalMergedContext;
   }

   /**
    * Register a new servlet context that contains configuration files and potentially resource files
    * We assume that this method is called within the initialization context of the related web application
    * @param context the {@link ServletContext} of the web application to register
    */
   public synchronized void registerContext(ServletContext context)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      final WebAppInitContext webappCtx = new WebAppInitContext(context);
      if (!webAppContexts.contains(webappCtx))
      {
         final Set<WebAppInitContext> contexts;
         if (webAppComparator == null)
         {
            contexts = new HashSet<WebAppInitContext>(webAppContexts);
         }
         else
         {
            contexts = new TreeSet<WebAppInitContext>(webAppComparator);
            contexts.addAll(webAppContexts);
         }
         contexts.add(webappCtx);
         this.webAppContexts = Collections.unmodifiableSet(contexts);
      }
   }

   /**
    * Unregister a servlet context that contains configuration files and potentially resource files
    * @param context the {@link ServletContext} of the web application to unregister
    */
   public synchronized void unregisterContext(ServletContext context)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      final WebAppInitContext webappCtx = new WebAppInitContext(context);
      if (webAppContexts.contains(webappCtx))
      {
         final Set<WebAppInitContext> contexts;
         if (webAppComparator == null)
         {
            contexts = new HashSet<WebAppInitContext>(webAppContexts);
         }
         else
         {
            contexts = new TreeSet<WebAppInitContext>(webAppComparator);
            contexts.addAll(webAppContexts);
         }
         contexts.remove(webappCtx);
         this.webAppContexts = Collections.unmodifiableSet(contexts);
      }
   }

   @Managed
   @ManagedDescription("The portal container name")
   public String getName()
   {
      return name;
   }

   @Managed
   @ManagedDescription("The configuration of the container in XML format.")
   public String getConfigurationXML()
   {
      Configuration conf = getConfiguration();
      if (conf == null)
      {
         LOG.warn("The configuration of the PortalContainer could not be found");
         return null;
      }
      Configuration result = Configuration.merge(((ExoContainer)parent).getConfiguration(), conf);
      if (result == null)
      {
         LOG.warn("The configurations could not be merged");
         return null;         
      }
      return result.toXML();
   }
   
   public SessionContainer createSessionContainer(String id, String owner)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      SessionContainer scontainer = getSessionManager().getSessionContainer(id);
      if (scontainer != null)
         getSessionManager().removeSessionContainer(id);
      scontainer = new SessionContainer(id, owner);
      scontainer.setPortalName(pinfo_.getContainerName());
      getSessionManager().addSessionContainer(scontainer);
      SessionContainer.setInstance(scontainer);
      return scontainer;
   }

   public void removeSessionContainer(String sessionID)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      getSessionManager().removeSessionContainer(sessionID);
   }

   public List<SessionContainer> getLiveSessions()
   {
      return getSessionManager().getLiveSessions();
   }

   public SessionManager getSessionManager()
   {
      if (smanager_ == null)
         smanager_ = (SessionManager)this.getComponentInstanceOfType(SessionManager.class);
      return smanager_;
   }

   public PortalContainerInfo getPortalContainerInfo()
   {
      return pinfo_;
   }

   /**
    * @return the current instance of {@link PortalContainer} that has been stored into the related
    * {@link ThreadLocal}. If no value has been set the default portal container will be returned
    */
   public static PortalContainer getInstance()
   {
      PortalContainer container = getInstanceIfPresent();
      if (container == null)
      {
         container = RootContainer.getInstance().getPortalContainer(DEFAULT_PORTAL_CONTAINER_NAME);
         final PortalContainer currentPortalContainer = container;
         SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
         {
            public Void run()
            {
               PortalContainer.setInstance(currentPortalContainer);
               return null;
            }
         });
      }
      return container;
   }

   /**
    * @return the current instance of {@link ExoContainer} that has been stored into the
    * {@link ThreadLocal} of {@link ExoContainerContext}. If no {@link PortalContainer} has been set, 
    * it will return <code>null</code>
    */
   public static PortalContainer getInstanceIfPresent()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
      if (container instanceof PortalContainer)
      {
         return (PortalContainer)container;
      }
      return null;
   }

   /**
    * @see the method isPortalContainerName of {@link PortalContainerConfig}
    */
   public static boolean isPortalContainerName(String name)
   {
      if (CONFIG == null)
      {
         return DEFAULT_PORTAL_CONTAINER_NAME.equals(name);
      }
      else
      {
         return CONFIG.isPortalContainerName(name);
      }
   }

   /**
    * @see the method isPortalContainerNameDisabled of {@link PortalContainerConfig}
    */
   public static boolean isPortalContainerNameDisabled(String name)
   {
      if (CONFIG == null)
      {
         return false;
      }
      else
      {
         return CONFIG.isPortalContainerNameDisabled(name);
      }
   }

   /**
    * Add an init-task to all the portal container instances related to the given ServletContext
    * 
    * @param context the context from which we extract the context name
    * @param task the task to execute 
    */
   public static void addInitTask(ServletContext context, PortalContainerInitTask task)
   {
      addInitTask(context, task, null);
   }

   /**
    * Add an init-task to all the portal container instances related to the given ServletContext if the
    * given portal container name is <code>null</code> other it will execute the task only of this
    * portal container if the {@link ServletContext} is on of its dependencies
    * 
    * @param context the context from which we extract the context name
    * @param task the task to execute
    * @param portalContainerName the name of the portal container for which we want to execute the task
    */
   public static void addInitTask(ServletContext context, PortalContainerInitTask task, String portalContainerName)
   {
      if (context == null || CONFIG == null)
      {
         return;
      }
      String contextName = context.getServletContextName();
      List<String> portalContainerNames = CONFIG.getPortalContainerNames(contextName);
      RootContainer root = RootContainer.getInstance();
      // We assume that we have at list one portal container otherwise there is a bug in PortalContainerConfig
      for (String name : portalContainerNames)
      {
         if (portalContainerName == null || portalContainerName.equals(name))
         {
            root.addInitTask(context, task, name);
         }
      }
   }

   /**
    * Gives the first portal container instance related to the given ServletContext
    * 
    * @param context the context from which we extract the context name
    */
   public static PortalContainer getInstance(ServletContext context)
   {
      if (context == null || CONFIG == null)
      {
         return null;
      }
      String portalContainerName = CONFIG.getPortalContainerName(context.getServletContextName());
      if (portalContainerName == null)
      {
         if (PropertyManager.isDevelopping())
         {
            LOG.warn("The Servlet Context '" + context.getServletContextName() + "' has not been registered"
               + " has a dependency of any PortalContainerDefinitions.");            
         }
         return null;
      }
      RootContainer root = RootContainer.getInstance();
      return root.getPortalContainer(portalContainerName);
   }

   /**
    * We first try to get the ExoContainer that has been stored into the ThreadLocal
    * if the value is of type PortalContainer, we return it otherwise we get the
    * portal container corresponding the given servlet context
    * 
    * @param context the context from which we extract the portal container name
    */
   public static PortalContainer getCurrentInstance(ServletContext context)
   {
      final PortalContainer container = getInstanceIfPresent();
      if (container == null)
      {
         return PortalContainer.getInstance(context);
      }
      return container;
   }

   /**
    * Returns the name of the current portal container that has been stored into the ThreadLocal. If no
    * value can be found the value of PortalContainer.DEFAULT_PORTAL_CONTAINER_NAME will be used
    */
   public static String getCurrentPortalContainerName()
   {
      final PortalContainer container = getInstanceIfPresent();
      if (container == null)
      {
         return DEFAULT_PORTAL_CONTAINER_NAME;
      }
      else
      {
         return container.getName();
      }
   }

   /**
    * Returns the name of the current rest context corresponding to the portal container
    * that has been stored into the ThreadLocal. If no value can be found the value of 
    * PortalContainer.DEFAULT_REST_CONTEXT_NAME will be used
    */
   public static String getCurrentRestContextName()
   {
      final String containerName = getCurrentPortalContainerName();
      return getRestContextName(containerName);
   }

   /**
    * Returns the name of the rest context corresponding to the given portal container name
    * @param portalContainerName the name of the portal container for which we want the
    * name of the rest {@link ServletContext}
    */
   public static String getRestContextName(String portalContainerName)
   {
      if (CONFIG == null)
      {
         return DEFAULT_REST_CONTEXT_NAME;
      }
      return CONFIG.getRestContextName(portalContainerName);
   }

   /**
    * Returns the name of the rest context corresponding to the current portal container 
    */
   public String getRestContextName()
   {
      return getRestContextName(getName());
   }

   /**
    * Returns the name of the current realm corresponding to the portal container
    * that has been stored into the ThreadLocal. If no value can be found the value of 
    * PortalContainer.DEFAULT_REALM_NAME will be used
    */
   public static String getCurrentRealmName()
   {
      final String containerName = getCurrentPortalContainerName();
      return getRealmName(containerName);
   }

   /**
    * Returns the name of the realm corresponding to the given portal container name
    * @param portalContainerName the name of the portal container for which we want the
    * name of the realm
    */
   public static String getRealmName(String portalContainerName)
   {
      if (CONFIG == null)
      {
         return DEFAULT_REALM_NAME;
      }
      return CONFIG.getRealmName(portalContainerName);
   }

   /**
    * Returns the name of the realm corresponding to the current portal container 
    */
   public String getRealmName()
   {
      return getRealmName(getName());
   }

   /**
    * Returns the current value of the setting corresponding to the portal container
    * that has been stored into the ThreadLocal. If no value can be found, <code>null</code> will be
    * returned
    * @param settingName the name of the setting wanted
    */
   public static Object getCurrentSetting(String settingName)
   {
      final String containerName = getCurrentPortalContainerName();
      return getSetting(containerName, settingName);
   }

   /**
    * Returns the value of the setting corresponding to the given portal container name
    * and the given setting name
    * @param portalContainerName the name of the portal container for which we want the
    * name of the value of the setting
    * @param settingName the name of the setting wanted
    */
   public static Object getSetting(String portalContainerName, String settingName)
   {
      if (CONFIG == null)
      {
         return null;
      }
      return CONFIG.getSetting(portalContainerName, settingName);
   }

   /**
    * Returns the value of the setting corresponding to the current portal container 
    * @param settingName the name of the setting wanted
    */
   public Object getSetting(String settingName)
   {
      return getSetting(getName(), settingName);
   }

   /**
    * Indicates if the given servlet context is a dependency of the given portal container
    * @param container the portal container
    * @param context the {@link ServletContext}
    * @return <code>true</code> if the dependencies matches, <code>false</code> otherwise;
    */
   public static boolean isScopeValid(PortalContainer container, ServletContext context)
   {
      if (CONFIG == null)
      {
         return true;
      }
      return CONFIG.isScopeValid(container.getName(), context.getServletContextName());
   }

   @Managed
   public boolean isStarted()
   {
      return started_;
   }

   @Override
   public void start()
   {
      super.start();
      started_ = true;
   }

   @Override
   public void stop()
   {
      super.stop();
      started_ = false;
   }

   public static void setInstance(PortalContainer instance)
   {
      ExoContainerContext.setCurrentContainer(instance);
   }

   public static Object getComponent(Class key)
   {
      PortalContainer pcontainer = getInstanceIfPresent();
      return pcontainer.getComponentInstanceOfType(key);
   }

   /**
    * This class is used to compare the {@link WebAppInitContext}
    */
   static class WebAppInitContextComparator implements Comparator<WebAppInitContext>
   {

      private final List<String> dependencies;

      WebAppInitContextComparator(List<String> dependencies)
      {
         this.dependencies = dependencies;
      }

      /**
       * This will sort all the {@link WebAppInitContext} such that we will first have
       * all the web applications defined in the list of dependencies of the 
       * related portal container (see {@link PortalContainerConfig} for more details
       *  about the dependencies) ordered in the same order as the dependencies, then
       *  we will have all the web applications undefined ordered by context name
       */
      public int compare(WebAppInitContext ctx1, WebAppInitContext ctx2)
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

   /**
    * Reloads the portal container configuration
    */
   static void reloadConfig()
   {
      ExoContainer top = ExoContainerContext.getTopContainer();
      CONFIG = top instanceof RootContainer ? ((RootContainer)top).getPortalContainerConfig() : null;
      if (CONFIG == null)
      {
         DEFAULT_PORTAL_CONTAINER_NAME = PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME;
         DEFAULT_REST_CONTEXT_NAME = PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME;
         DEFAULT_REALM_NAME = PortalContainerConfig.DEFAULT_REALM_NAME;
      }
      else
      {
         DEFAULT_PORTAL_CONTAINER_NAME = CONFIG.getDefaultPortalContainer();
         DEFAULT_REST_CONTEXT_NAME = CONFIG.getDefaultRestContext();
         DEFAULT_REALM_NAME = CONFIG.getDefaultRealmName();
      }
   }
   
   /**
    * This class is used to register a portal container
    */
   public static class RegisterTask extends PortalContainerPreInitTask
   {
      public void execute(ServletContext context, PortalContainer portalContainer)
      {
         portalContainer.registerContext(context);
      }
   }
   
   /**
    * This class is used to unregister a portal container
    */
   public static class UnregisterTask extends PortalContainerPreInitTask
   {
      public void execute(ServletContext context, PortalContainer portalContainer)
      {
         portalContainer.unregisterContext(context);
      }
   }
}
