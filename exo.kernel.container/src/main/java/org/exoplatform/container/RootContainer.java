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

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.exoplatform.container.configuration.MockConfigurationManagerImpl;
import org.exoplatform.container.definition.PortalContainerConfig;
import org.exoplatform.container.definition.PortalContainerDefinition;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.monitor.jvm.OperatingSystemInfo;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NamingContext;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.rest.annotations.RESTEndpoint;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.test.mocks.servlet.MockServletContext;

import java.io.File;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

import javax.servlet.ServletContext;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Jul 21, 2004 Time: 12:15:28 AM
 */
@Managed
@NamingContext(@Property(key = "container", value = "root"))
@RESTEndpoint(path = "rcontainer")
public class RootContainer extends ExoContainer
{

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = 812448359436635438L;

   /** The field is volatile to properly implement the double checked locking pattern. */
   private static volatile RootContainer singleton_;

   private OperatingSystemInfo osenv_;

   private PortalContainerConfig config_;

   private static final Log log = ExoLogger.getLogger("exo.kernel.container.RootContainer");

   private static volatile boolean booting = false;

   private final J2EEServerInfo serverenv_ = new J2EEServerInfo();

   private final Set<String> profiles;

   /**
    * The list of all the tasks to execute while initializing the corresponding portal containers
    */
   private final ConcurrentMap<String, ConcurrentMap<String, Queue<PortalContainerInitTaskContext>>> initTasks =
      new ConcurrentHashMap<String, ConcurrentMap<String, Queue<PortalContainerInitTaskContext>>>();

   /**
    * The list of the web application contexts corresponding to all the portal containers
    */
   private final Queue<WebAppInitContext> portalContexts = new ConcurrentLinkedQueue<WebAppInitContext>();

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
      log.info("Active profiles " + profiles);

      //
      Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
      this.profiles = profiles;
      this.registerComponentInstance(J2EEServerInfo.class, serverenv_);
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

   public PortalContainer getPortalContainer(String name)
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
               PortalContainer.setInstance(pcontainer);
               ConfigurationManagerImpl cService = new MockConfigurationManagerImpl(scontext);
               cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/configuration.xml"));
               cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/test-configuration.xml"));
               cService.processRemoveConfiguration();
               pcontainer.registerComponentInstance(ConfigurationManager.class, cService);
               registerComponentInstance(name, pcontainer);
               pcontainer.start(true);
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
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
            log.info("We assume that the ServletContext '" + context.getServletContextName()
               + "' is not a portal since no portal container with the same name has been"
               + " registered to the component PortalContainerConfig.");
         }
         // We assume that a ServletContext of a portal container owns configuration files
         final PortalContainerPreInitTask task = new PortalContainerPreInitTask()
         {

            public void execute(ServletContext context, PortalContainer portalContainer)
            {
               portalContainer.registerContext(context);
            }
         };
         PortalContainer.addInitTask(context, task);
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
      WebAppInitContext context;
      boolean hasChanged = false;
      try
      {
         while ((context = portalContexts.poll()) != null)
         {
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
      PortalContainerConfig config = getPortalContainerConfig();
      for (String portalContainerName : initTasks.keySet())
      {
         if (config.isPortalContainerName(portalContainerName))
         {
            // Unregister name of portal container that doesn't exist
            log.warn("The portal container '" + portalContainerName + "' doesn't not exist or"
               + " it has not yet been registered, please check your PortalContainerDefinitions and "
               + "the loading order.");
            config.unregisterPortalContainerName(portalContainerName);            
         }
      }
      // remove all the unneeded tasks
      initTasks.clear();
   }

   public synchronized void createPortalContainer(ServletContext context)
   {
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      boolean hasChanged = false;
      final String portalContainerName = context.getServletContextName();
      try
      {
         log.info("Trying to create the portal container '" + portalContainerName + "'");
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
            log.error("Cannot add configuration conf/portal/configuration.xml. ServletContext: " + context, ex);
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
            log.error("Cannot add configuration " + uri + ". ServletContext: " + context, ex);
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
               log.error("Cannot add configuration war:/conf/configuration.xml. ServletContext: " + ctx, ex);
            }
         }

         // add config from application server,
         // $AH_HOME/exo-conf/portal/configuration.xml
         String overrideConfig =
            singleton_.getServerEnvironment().getExoConfigurationDirectory() + "/portal/" + portalContainerName
               + "/configuration.xml";
         try
         {
            File file = new File(overrideConfig);
            if (file.exists())
               cService.addConfiguration(file.toURI().toURL());
         }
         catch (Exception ex)
         {
            log.error("Cannot add configuration " + overrideConfig + ". ServletContext: " + context, ex);
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
         log.info("The portal container '" + portalContainerName + "' has been created successfully");
      }
      catch (Exception ex)
      {
         log.error("Cannot create the portal container '" + portalContainerName + "' . ServletContext: " + context, ex);
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
            log.warn("An error occured while cleaning the ThreadLocal", e);
         }
      }
   }

   synchronized public void removePortalContainer(ServletContext servletContext)
   {
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
         RootContainer rootContainer = new RootContainer();
         ConfigurationManagerImpl service = new ConfigurationManagerImpl(rootContainer.profiles);
         service.addConfiguration(ContainerUtil.getConfigurationURL("conf/configuration.xml"));
         if (System.getProperty("maven.exoplatform.dir") != null)
         {
            service.addConfiguration(ContainerUtil.getConfigurationURL("conf/test-configuration.xml"));
         }
         String confDir = rootContainer.getServerEnvironment().getExoConfigurationDirectory();
         String overrideConf = confDir + "/configuration.xml";
         File file = new File(overrideConf);
         if (file.exists())
         {
            service.addConfiguration("file:" + overrideConf);
         }
         service.processRemoveConfiguration();
         rootContainer.registerComponentInstance(ConfigurationManager.class, service);
         rootContainer.start(true);
         return rootContainer;
      }
      catch (Exception e)
      {
         log.error("Could not build root container", e);
         return null;
      }
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
               if (booting)
               {
                  throw new IllegalStateException("Already booting by the same thread");
               }
               else
               {
                  booting = true;
                  try
                  {
                     log.info("Building root container");
                     long time = -System.currentTimeMillis();
                     result = buildRootContainer();
                     if (result != null)
                     {
                        time += System.currentTimeMillis();
                        log.info("Root container is built (build time " + time + "ms)");
                        ExoContainerContext.setTopContainer(result);
                        singleton_ = result;
                        log.info("Root container booted");
                     }
                     else
                     {
                        log.error("Failed to boot root container");
                     }
                  }
                  finally
                  {
                     booting = false;
                  }
               }
            }
         }
      }
      return result;
   }

   static public void setInstance(RootContainer rcontainer)
   {
      singleton_ = rcontainer;
   }

   @Managed
   @ManagedDescription("The configuration of the container in XML format.")
   public String getConfigurationXML()
   {
      Configuration config = getConfiguration();
      if (config == null)
      {
         log.warn("The configuration of the RootContainer could not be found");
         return null;
      }
      return config.toXML();
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
      final PortalContainer container = getPortalContainer(portalContainer);
      if (!task.alreadyExists(container))
      {
         if (log.isDebugEnabled())
            log.debug("The portal container '" + portalContainer
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
         queue.add(new PortalContainerInitTaskContext(context, task));
      }
      else
      {
         if (log.isDebugEnabled())
            log.debug("The portal container '" + portalContainer
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
      if (log.isDebugEnabled())
         log.debug("Start launching the " + type + " tasks of the portal container '" + portalContainer + "'");
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      PortalContainerInitTaskContext context;
      boolean hasChanged = false;
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
      queues.remove(type);
      if (queues.isEmpty())
      {
         initTasks.remove(portalContainerName);
      }
      if (log.isDebugEnabled())
         log.debug("End launching the " + type + " tasks of the portal container '" + portalContainer + "'");
   }

   static class ShutdownThread extends Thread
   {
      RootContainer container_;

      ShutdownThread(RootContainer container)
      {
         container_ = container;
      }

      public void run()
      {
         container_.stop();
      }
   }

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
}
