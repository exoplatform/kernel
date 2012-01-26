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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.PrivilegedAction;

import javax.management.MBeanServer;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Nov 8, 2004
 * @version $Id: J2EEServerInfo.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class J2EEServerInfo
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.J2EEServerInfo");
   
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
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {

            String jonasHome = System.getProperty("jonas.base");
            String jbossHome = System.getProperty("jboss.home.dir");
            String jettyHome = System.getProperty("jetty.home");
            String websphereHome = System.getProperty("was.install.root");
            String weblogicHome = System.getProperty("wls.home");
            String glassfishHome = System.getProperty("com.sun.aas.instanceRoot");
            String catalinaHome = System.getProperty("catalina.home");
            String testHome = System.getProperty("maven.exoplatform.dir");

            // The name of the configuration directory
            final String confDirName = System.getProperty(EXO_CONF_DIR_NAME_PARAM, "exo-conf");
            if (jonasHome != null)
            {
               serverName_ = "jonas";
               serverHome_ = jonasHome;
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
                     exoConfDir_ = new File(new File(new URI(jbossConfigUrl)), confDirName).getAbsolutePath();
                  }
                  catch (SecurityException e)
                  {
                     if (LOG.isTraceEnabled())
                     {
                        LOG.trace("An exception occurred: " + e.getMessage());
                     }
                  }
                  catch (URISyntaxException e)
                  {
                     if (LOG.isTraceEnabled())
                     {
                        LOG.trace("An exception occurred: " + e.getMessage());
                     }
                  }
                  catch (IllegalArgumentException e)
                  {
                     if (LOG.isTraceEnabled())
                     {
                        LOG.trace("An exception occurred: " + e.getMessage());
                     }
                  }
               }
               else
               {
                  // New variable that exists only since JBoss AS 7
                  String jbossConfigDir = System.getProperty("jboss.server.config.dir");
                  if (jbossConfigDir != null)
                  {
                     try
                     {
                        exoConfDir_ = new File(jbossConfigDir, confDirName).getAbsolutePath();
                     }
                     catch (SecurityException e)
                     {
                        if (LOG.isTraceEnabled())
                        {
                           LOG.trace("An exception occurred: " + e.getMessage());
                        }
                     }
                  }
               }
               try
               {
                  Class<?> clazz = Thread.currentThread().getContextClassLoader()
                           .loadClass("org.jboss.mx.util.MBeanServerLocator");
                  Method m = clazz.getMethod("locateJBoss");
                  mbeanServer = (MBeanServer)m.invoke(null);
               }
               catch (ClassNotFoundException ignore)
               {
                  // We assume that JBoss AS 7 or higher is currently used
                  // since this class has been removed starting from this version
                  // of JBoss AS
                  LOG.debug(ignore.getLocalizedMessage(), ignore);
               }
               catch (Exception ignore)
               {
                  LOG.error(ignore.getLocalizedMessage(), ignore);
               }
            }
            else if (jettyHome != null)
            {
               serverName_ = "jetty";
               serverHome_ = jettyHome;
            }
            else if (websphereHome != null)
            {
               serverName_ = "websphere";
               serverHome_ = websphereHome;
            }
            else if (weblogicHome != null)
            {
               serverName_ = "weblogic";
               serverHome_ = weblogicHome;
            }
            else if (glassfishHome != null)
            {
               serverName_ = "glassfish";
               serverHome_ = glassfishHome;
            }
            else if (catalinaHome != null)
            {
               // Catalina has to be processed at the end as other servers may embed it
               serverName_ = "tomcat";
               serverHome_ = catalinaHome;
            }
            else if (testHome != null)
            {
               serverName_ = "test";
               serverHome_ = testHome;
            }
            else
            {
               // throw new UnsupportedOperationException("unknown server platform") ;
               serverName_ = "standalone";
               serverHome_ = System.getProperty("user.dir");
            }
            if (exoConfDir_ == null)
            {
               exoConfDir_ = serverHome_ + "/" + confDirName;
            }
            if (mbeanServer == null)
            {
               mbeanServer = ManagementFactory.getPlatformMBeanServer();
            }

            String exoConfHome = System.getProperty(EXO_CONF_PARAM);
            if (exoConfHome != null && exoConfHome.length() > 0)
            {
               LOG.info("Override exo-conf directory '" + exoConfDir_ + "' with location '" + exoConfHome
                  + "'");
               exoConfDir_ = exoConfHome;
            }

            serverHome_ = serverHome_.replace('\\', '/');
            exoConfDir_ = exoConfDir_.replace('\\', '/');
            return null;
         }
      });
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
