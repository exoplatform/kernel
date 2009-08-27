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
import org.exoplatform.container.jmx.ManagementContextImpl;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.monitor.jvm.OperatingSystemInfo;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.jmx.annotations.NamingContext;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.test.mocks.servlet.MockServletContext;
import org.picocontainer.ComponentAdapter;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.servlet.ServletContext;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Jul 21, 2004 Time: 12:15:28 AM
 */
@Managed
@NamingContext(@Property(key = "container", value = "root"))
public class RootContainer extends ExoContainer
{

   private static MBeanServer findMBeanServer()
   {
      J2EEServerInfo serverenv_ = new J2EEServerInfo();
      MBeanServer server = serverenv_.getMBeanServer();
      if (server == null)
      {
         server = ManagementFactory.getPlatformMBeanServer();
      }
      return server;
   }

   /** The field is volatile to properly implement the double checked locking pattern. */
   private static volatile RootContainer singleton_;

   private OperatingSystemInfo osenv_;

   private static final Log log = ExoLogger.getLogger(RootContainer.class);

   private static volatile boolean booting = false;

   private final J2EEServerInfo serverenv_ = new J2EEServerInfo();

   public RootContainer()
   {
      super(new ManagementContextImpl(findMBeanServer(), new HashMap<String, String>()));
      Runtime.getRuntime().addShutdownHook(new ShutdownThread(this));
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
               ConfigurationManagerImpl cService = new MockConfigurationManagerImpl(scontext);
               cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/configuration.xml"));
               cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/test-configuration.xml"));
               cService.processRemoveConfiguration();
               pcontainer.registerComponentInstance(ConfigurationManager.class, cService);
               pcontainer.initContainer();
               registerComponentInstance(name, pcontainer);
               PortalContainer.setInstance(pcontainer);
               ExoContainerContext.setCurrentContainer(pcontainer);
               pcontainer.start();
            }
            catch (Exception ex)
            {
               ex.printStackTrace();
            }
         }
      }
      return pcontainer;
   }

   synchronized public PortalContainer createPortalContainer(ServletContext context)
   {
      try
      {
         PortalContainer pcontainer = new PortalContainer(this, context);
         PortalContainer.setInstance(pcontainer);
         ExoContainerContext.setCurrentContainer(pcontainer);
         ConfigurationManagerImpl cService = new ConfigurationManagerImpl(context);

         // add configs from services
         try
         {
            cService.addConfiguration(ContainerUtil.getConfigurationURL("conf/portal/configuration.xml"));
         }
         catch (Exception ex)
         {
            System.err.println("ERROR: cannot add configuration conf/portal/configuration.xml. ServletContext: "
               + context);
            ex.printStackTrace();
         }

         // Add configuration that depends on the environment
         String uri;
         if (Environnment.isJBoss())
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
            System.err.println("ERROR: cannot add configuration " + uri + ". ServletContext: " + context);
            ex.printStackTrace();
         }

         // add configs from web apps
         try
         {
            cService.addConfiguration("war:/conf/configuration.xml");
         }
         catch (Exception ex)
         {
            System.err.println("ERROR: cannot add configuration war:/conf/configuration.xml. ServletContext: "
               + context);
            ex.printStackTrace();
         }

         // add config from application server,
         // $AH_HOME/exo-conf/portal/configuration.xml
         String overrideConfig =
            singleton_.getServerEnvironment().getExoConfigurationDirectory() + "/portal/"
               + pcontainer.getPortalContainerInfo().getContainerName() + "/configuration.xml";
         try
         {
            File file = new File(overrideConfig);
            if (file.exists())
               cService.addConfiguration(file.toURI().toURL());
         }
         catch (Exception ex)
         {
            System.err.println("ERROR: cannot add configuration " + overrideConfig + ". ServletContext: " + context);
            ex.printStackTrace();
         }

         cService.processRemoveConfiguration();
         ComponentAdapter adapter = pcontainer.registerComponentInstance(ConfigurationManager.class, cService);
         pcontainer.initContainer();
         registerComponentInstance(context.getServletContextName(), pcontainer);
         PortalContainer.setInstance(pcontainer);
         ExoContainerContext.setCurrentContainer(pcontainer);
         pcontainer.start();

         // Register the portal as an mbean
         managementContext.register(pcontainer);

         //
         return pcontainer;
      }
      catch (Exception ex)
      {
         System.err.println("ERROR: cannot create portal container. ServletContext: " + context);
         ex.printStackTrace();
      }
      return null;
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
         ConfigurationManagerImpl service = new ConfigurationManagerImpl();
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
         rootContainer.initContainer();
         rootContainer.start();
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
                  log.error("Booting root container with id " + RootContainer.class.hashCode() + "");
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
            }
         }
      }
      return result;
   }

   static public void setInstance(RootContainer rcontainer)
   {
      singleton_ = rcontainer;
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
}
