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
package org.exoplatform.container.monitor.jvm;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URL;

import javax.management.MBeanServer;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Nov 8, 2004
 * @version $Id: J2EEServerInfo.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class J2EEServerInfo
{
   
   /**
    * The name of the JVM parameter that allows us to change the location of the
    * configuration directory
    */
   public static final String EXO_CONF_PARAM = "exo.conf.dir";
   
   /**
    * The name of the JVM parameter that allows us to change the default name
    * of the configuration directory which is "exo-conf"
    */
   public static final String EXO_CONF_DIR_NAME_PARAM = "exo.conf.dir.name";
   
   private String serverName_;

   private String serverHome_;

   private String exoConfDir_;

   protected String sharedLibDirecotry_;

   protected String appDeployDirecotry_;

   protected MBeanServer mbeanServer;

   public J2EEServerInfo()
   {

      String jonasHome = System.getProperty("jonas.base");
      String jbossHome = System.getProperty("jboss.home.dir");
      String jettyHome = System.getProperty("jetty.home");
      String websphereHome = System.getProperty("was.install.root");
      String weblogicHome = System.getProperty("wls.home");
      String catalinaHome = System.getProperty("catalina.home");
      String testHome = System.getProperty("maven.exoplatform.dir");

      // The name of the configuration directory
      final String confDirName = System.getProperty(EXO_CONF_DIR_NAME_PARAM, "exo-conf");
      if (jonasHome != null)
      {
         serverName_ = "jonas";
         serverHome_ = jonasHome;
         exoConfDir_ = serverHome_ + "/" + confDirName;
      }
      else if (jbossHome != null)
      {
         serverName_ = "jboss";
         serverHome_ = jbossHome;

         // try find and use jboss.server.config.url
         // based on http://www.jboss.org/community/docs/DOC-10730
         String jbossConfigUrl = System.getProperty("jboss.server.config.url");
         if (jbossConfigUrl != null)
         {
            try
            {
               exoConfDir_ = new File(new URL(jbossConfigUrl).getFile() + "/" + confDirName).getAbsolutePath();
            }
            catch (Throwable e)
            {
               // don't care about it
               exoConfDir_ = serverHome_ + "/" + confDirName;
            }
         }
         else
            exoConfDir_ = serverHome_ + "/" + confDirName;

         //
         try
         {
            Class clazz =
               Thread.currentThread().getContextClassLoader().loadClass("org.jboss.mx.util.MBeanServerLocator");
            Method m = clazz.getMethod("locateJBoss");
            mbeanServer = (MBeanServer)m.invoke(null);
         }
         catch (Exception ignore)
         {
            ignore.printStackTrace();
         }
      }
      else if (jettyHome != null)
      {
         serverName_ = "jetty";
         serverHome_ = jettyHome;
         exoConfDir_ = serverHome_ + "/" + confDirName;
      }
      else if (websphereHome != null)
      {
         serverName_ = "websphere";
         serverHome_ = websphereHome;
         exoConfDir_ = serverHome_ + "/" + confDirName;
      }
      else if (weblogicHome != null)
      {
         serverName_ = "weblogic";
         serverHome_ = weblogicHome;
         exoConfDir_ = serverHome_ + "/" + confDirName;
         // Catalina has to be processed at the end as other servers may embed it
      }
      else if (catalinaHome != null)
      {
         serverName_ = "tomcat";
         serverHome_ = catalinaHome;
         exoConfDir_ = serverHome_ + "/" + confDirName;
      }
      else if (testHome != null)
      {
         serverName_ = "test";
         serverHome_ = testHome;
         exoConfDir_ = serverHome_ + "/" + confDirName;
      }
      else
      {
         // throw new UnsupportedOperationException("unknown server platform") ;
         serverName_ = "standalone";
         serverHome_ = System.getProperty("user.dir");
         exoConfDir_ = serverHome_ + "/" + confDirName;
      }
      if (mbeanServer == null)
      {
         mbeanServer = ManagementFactory.getPlatformMBeanServer();
      }

      String exoConfHome = System.getProperty(EXO_CONF_PARAM);
      if (exoConfHome != null && exoConfHome.length() > 0)
      {
         System.out.println("[INFO] Override exo-conf directory '" + exoConfDir_ + "' with location '" + exoConfHome
            + "'");
         exoConfDir_ = exoConfHome;
      }

      serverHome_ = serverHome_.replace('\\', '/');
      exoConfDir_ = exoConfDir_.replace('\\', '/');
   }

   /**
    * Returns an mbean server setup by the application server environment or null
    * if none cannot be located.
    *
    * @return an mean server
    */
   public MBeanServer getMBeanServer()
   {
      return mbeanServer;
   }

   public String getServerName()
   {
      return serverName_;
   }

   public String getServerHome()
   {
      return serverHome_;
   }

   public String getExoConfigurationDirectory()
   {
      return exoConfDir_;
   }

   public String getSharedLibDirectory()
   {
      return sharedLibDirecotry_;
   }

   public String getApplicationDeployDirectory()
   {
      return appDeployDirecotry_;
   }

   public boolean isJBoss()
   {
      return "jboss".equals(serverName_);
   }
}
