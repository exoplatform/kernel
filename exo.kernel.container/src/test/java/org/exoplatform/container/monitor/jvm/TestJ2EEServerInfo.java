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

import junit.framework.TestCase;

import org.exoplatform.container.configuration.ConfigurationException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by The eXo Platform SAS Author : Alex Reshetnyak
 * alex.reshetnyak@exoplatform.com.ua reshetnyak.alex@exoplatform.com.ua Nov 7,
 * 2007
 */
public class TestJ2EEServerInfo extends TestCase
{

   private static URL configurationURL = null;

   private File confFile;

   private String confPath;

   private String confDir;

   public void setUp()
   {
      try
      {
         confFile = new File("exo-configuration.xml");
         if (confFile.createNewFile())
         {
            confPath = confFile.getAbsolutePath();
            confDir = confPath.replace(System.getProperty("file.separator") + "exo-configuration.xml", "");
         }
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   public void testConfigDirName()
   {
      testConfigDirName(null,"jonas.base");
      testConfigDirName(null,"jboss.home.dir");
      testConfigDirName(null,"jboss.home.dir", "jboss.server.config.url");
      testConfigDirName(null,"jetty.home");
      testConfigDirName(null,"was.install.root");
      testConfigDirName(null,"wls.home");
      testConfigDirName(null,"catalina.home");
      testConfigDirName(null,"maven.exoplatform.dir");
      testConfigDirName(null);
      testConfigDirName("foo","jonas.base");
      testConfigDirName("foo","jboss.home.dir");
      testConfigDirName("foo","jboss.home.dir", "jboss.server.config.url");
      testConfigDirName("foo","jetty.home");
      testConfigDirName("foo","was.install.root");
      testConfigDirName("foo","wls.home");
      testConfigDirName("foo","catalina.home");
      testConfigDirName("foo","maven.exoplatform.dir");
      testConfigDirName("foo");
   }
   
   private void testConfigDirName(String confDirName, String... asVMParams)
   {
      if (confDirName != null)
      {
         System.setProperty(J2EEServerInfo.EXO_CONF_DIR_NAME_PARAM, confDirName);
      }
      if (asVMParams != null)
      {
         for (String asVMParam : asVMParams)
         {
            System.setProperty(asVMParam, confDir);
         }
      } 
      try
      {
         assertTrue((new J2EEServerInfo().getExoConfigurationDirectory()).contains(confDirName == null ? "exo-conf" : confDirName));
      }
      finally
      {
         if (confDirName != null)
         {
            System.getProperties().remove(J2EEServerInfo.EXO_CONF_DIR_NAME_PARAM);
         }         
         if (asVMParams != null)
         {
            for (String asVMParam : asVMParams)
            {
               System.getProperties().remove(asVMParam);
            }
         } 
      }
   }
   
   public void testServerDirs() throws Exception
   {
      try
      {
         testServerDir("catalina.home");
         testServerDir("jonas.base");
         testServerDir("jboss.home.dir");
         testServerDir("jetty.home");
         testServerDir("was.install.root");
         testServerDir("wls.home");
         testServerDir("maven.exoplatform.dir");
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void testServerDir(String systemProperty) throws Exception
   {
      System.setProperty(systemProperty, confDir);

      initConfigurationURL(null);

      System.out.println(configurationURL.getFile());

      if (!System.getProperty("file.separator").equals("/"))
      {
         String sTemp = confPath.replace(System.getProperty("file.separator"), "/");
         assertEquals(configurationURL.getFile(), sTemp);
      }
      else
         assertEquals(configurationURL.getFile(), confPath);

      System.clearProperty(systemProperty);
   }

   public void tearDown()
   {
      if (confFile.delete())
         System.out.println("delete ok!");
   }

   private static void initConfigurationURL(ClassLoader configClassLoader) throws MalformedURLException,
      ConfigurationException
   {
      // (1) set by setConfigurationURL or setConfigurationPath
      // or
      if (configurationURL == null)
      {

         // (2) exo-configuration.xml in AS (standalone) home directory
         configurationURL = new URL("file:" + (new J2EEServerInfo()).getServerHome() + "/exo-configuration.xml");

         // (3) conf/exo-configuration.xml in war/ear(?)
         if (!fileExists(configurationURL) && configClassLoader != null)
         {
            configurationURL = configClassLoader.getResource("conf/exo-configuration.xml");
         }
      }
   }

   private static boolean fileExists(URL url)
   {
      try
      {
         url.openStream().close();
         return true;
      }
      catch (Exception e)
      {
         return false;
      }
   }

}
