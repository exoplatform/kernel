/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.container.configuration;

import junit.framework.TestCase;

import org.exoplatform.container.xml.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 22 fŽvr. 2010  
 */
public class TestConfigurationManagerImpl extends TestCase
{
   public void testGetURL() throws Exception
   {
      // Empty CM
      ConfigurationManager cm = new ConfigurationManagerImpl();
      URL url = cm.getURL(null);
      assertNull(url);
      url = cm.getURL("jar:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm.getURL("jar:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm.getURL("classpath:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm.getURL("classpath:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      try
      {
         url = cm.getURL("war:/org/exoplatform/container/configuration/empty-config.xml");
         fail("An error should be thrown");
      }
      catch (Exception e)
      {
         // ok;
      }
      try
      {
         url = cm.getURL("war:/org/exoplatform/container/configuration/empty-config-fake.xml");
         fail("An error should be thrown");
      }
      catch (Exception e)
      {
         // ok;
      }
      String sURL = getClass().getResource("empty-config.xml").toString();
      assertNotNull(sURL);
      assertTrue("the expected path should starts with file:", sURL.startsWith("file:"));
      sURL = sURL.substring(0, sURL.lastIndexOf('/'));
      sURL = sURL.substring(0, sURL.lastIndexOf('/'));
      url = cm.getURL(sURL + "/configuration/empty-config.xml");
      checkURL(url);
      url = cm.getURL(sURL + "/configuration/empty-config-fake.xml");
      checkURL(url, true);
      url = cm.getURL(sURL + "\\configuration\\empty-config.xml");
      checkURL(url);
      url = cm.getURL(sURL + "\\configuration\\empty-config-fake.xml");
      checkURL(url, true);
      String incompleteURL = "file:/" + getClass().getResource("empty-config.xml").getPath();
      incompleteURL = incompleteURL.substring(0, incompleteURL.lastIndexOf('/'));
      url = cm.getURL(incompleteURL + "/empty-config.xml");
      checkURL(url);
      url = cm.getURL(incompleteURL + "/empty-config-fake.xml");
      checkURL(url, true);
      incompleteURL = "file:" + getClass().getResource("empty-config.xml").getPath();
      incompleteURL = incompleteURL.substring(0, incompleteURL.lastIndexOf('/'));
      url = cm.getURL(incompleteURL + "/empty-config.xml");
      checkURL(url);
      url = cm.getURL(incompleteURL + "/empty-config-fake.xml");
      checkURL(url, true);     
      url = cm.getURL("org/exoplatform/container/configuration/empty-config.xml");
      assertNull(url);
      url = cm.getURL("org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      
      // CM with ClassLoader
      ConfigurationManager cm1 = new ConfigurationManagerImpl(Thread.currentThread().getContextClassLoader(), null);
      url = cm1.getURL(null);
      assertNull(url);      
      url = cm1.getURL("jar:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm1.getURL("jar:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm1.getURL("classpath:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm1.getURL("classpath:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm1.getURL("war:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm1.getURL("war:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm1.getURL(sURL + "/configuration/empty-config.xml");
      checkURL(url);
      url = cm1.getURL(sURL + "/configuration/empty-config-fake.xml");
      checkURL(url, true);
      url = cm1.getURL("org/exoplatform/container/configuration/empty-config.xml");
      assertNull(url);
      url = cm1.getURL("org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      
      // CM with ServletContext
      ConfigurationManager cm2 = new ConfigurationManagerImpl(new MockServletContext(), null);
      url = cm2.getURL(null);
      assertNull(url);      
      url = cm2.getURL("jar:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm2.getURL("jar:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm2.getURL("classpath:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm2.getURL("classpath:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm2.getURL("war:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm2.getURL("war:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm2.getURL(sURL + "/configuration/empty-config.xml");
      checkURL(url);
      url = cm2.getURL(sURL + "/configuration/empty-config-fake.xml");
      checkURL(url, true);
      url = cm2.getURL("org/exoplatform/container/configuration/empty-config.xml");
      assertNull(url);
      url = cm2.getURL("org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);      
      
      // CM with Context path
      ConfigurationManager cm3 = new ConfigurationManagerImpl();
      String path = getClass().getResource("empty-config.xml").getPath();
      assertNotNull(path);
      path = path.substring(0, path.lastIndexOf('/'));      
      cm3.addConfiguration((new File(path)).toURI().toURL());
      url = cm3.getURL(null);
      assertNull(url);            
      url = cm3.getURL("jar:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm3.getURL("jar:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      url = cm3.getURL("classpath:/org/exoplatform/container/configuration/empty-config.xml");
      checkURL(url);
      url = cm3.getURL("classpath:/org/exoplatform/container/configuration/empty-config-fake.xml");
      assertNull(url);
      try
      {
         url = cm3.getURL("war:/org/exoplatform/container/configuration/empty-config.xml");
         fail("An error should be thrown");
      }
      catch (Exception e)
      {
         // ok;
      }
      try
      {
         url = cm3.getURL("war:/org/exoplatform/container/configuration/empty-config-fake.xml");
         fail("An error should be thrown");
      }
      catch (Exception e)
      {
         // ok;
      }
      url = cm3.getURL(sURL + "/configuration/empty-config.xml");
      checkURL(url);
      url = cm3.getURL(sURL + "/configuration/empty-config-fake.xml");
      checkURL(url, true);
      url = cm3.getURL("configuration/empty-config.xml");
      checkURL(url);
      url = cm3.getURL("configuration/empty-config-fake.xml");
      checkURL(url, true);      
      url = cm3.getURL("configuration\\empty-config.xml");
      checkURL(url);
      url = cm3.getURL("configuration\\empty-config-fake.xml");
      checkURL(url, true);      
   }

   public void testImport() throws Exception
   {
      // no import
      ConfigurationManager cm = new ConfigurationManagerImpl();
      cm.addConfiguration("classpath:/org/exoplatform/container/configuration/config-manager-configuration-a.xml");
      Configuration conf = cm.getConfiguration();
      assertNotNull(conf.getComponent("A"));
      assertTrue(conf.getComponent("A").getDocumentURL().getFile().endsWith("config-manager-configuration-a.xml"));
      assertNull(conf.getComponent("B"));
      assertNull(conf.getComponent("C"));
      
      // b import a
      cm = new ConfigurationManagerImpl();
      cm.addConfiguration("classpath:/org/exoplatform/container/configuration/config-manager-configuration-b.xml");
      conf = cm.getConfiguration();
      assertNotNull(conf.getComponent("A"));
      assertTrue(conf.getComponent("A").getDocumentURL().getFile().endsWith("config-manager-configuration-a.xml"));
      assertNotNull(conf.getComponent("B"));
      assertTrue(conf.getComponent("B").getDocumentURL().getFile().endsWith("config-manager-configuration-b.xml"));
      assertNull(conf.getComponent("C"));
      
      // c import b and b import a
      cm = new ConfigurationManagerImpl();
      cm.addConfiguration("classpath:/org/exoplatform/container/configuration/config-manager-configuration-c.xml");
      conf = cm.getConfiguration();
      assertNotNull(conf.getComponent("A"));
      assertTrue(conf.getComponent("A").getDocumentURL().getFile().endsWith("config-manager-configuration-a.xml"));
      assertNotNull(conf.getComponent("B"));
      assertTrue(conf.getComponent("B").getDocumentURL().getFile().endsWith("config-manager-configuration-b.xml"));
      assertNotNull(conf.getComponent("C"));
      assertTrue(conf.getComponent("C").getDocumentURL().getFile().endsWith("config-manager-configuration-c.xml"));
   }
   
   private void checkURL(URL url) throws Exception
   {
      checkURL(url, false);
   }
   
   private void checkURL(URL url, boolean empty) throws Exception
   {
      assertNotNull(url);
      InputStream is = null;
      try
      {
         is = url.openStream();
         if (empty)
         {
            assertNull(is);
         }
         else
         {
            assertNotNull(is);
            assertTrue(is.available() > 0);            
         }
      }
      catch (IOException e)
      {
         if (empty)
         {
            // OK
         }       
         else
         {
            throw e;
         }
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (Exception e)
            {
               // ignore me
            }
         }
      }
   }
   
   private static class MockServletContext implements ServletContext
   {

      public Object getAttribute(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public Enumeration getAttributeNames()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public ServletContext getContext(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public String getContextPath()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public String getInitParameter(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public Enumeration getInitParameterNames()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public int getMajorVersion()
      {
         // TODO Auto-generated method stub
         return 0;
      }

      public String getMimeType(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public int getMinorVersion()
      {
         // TODO Auto-generated method stub
         return 0;
      }

      public RequestDispatcher getNamedDispatcher(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public String getRealPath(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public RequestDispatcher getRequestDispatcher(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public URL getResource(String arg0) throws MalformedURLException
      {
         // We remove "/WEB-INF/
         String path = arg0.substring(ConfigurationManagerImpl.WAR_CONF_LOCATION.length() + 1);
         return Thread.currentThread().getContextClassLoader().getResource(path);
      }

      public InputStream getResourceAsStream(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public Set getResourcePaths(String arg0)
      {
         // TODO Auto-generated method stub
         return null;
      }

      public String getServerInfo()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public Servlet getServlet(String arg0) throws ServletException
      {
         // TODO Auto-generated method stub
         return null;
      }

      public String getServletContextName()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public Enumeration getServletNames()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public Enumeration getServlets()
      {
         // TODO Auto-generated method stub
         return null;
      }

      public void log(String arg0)
      {
         // TODO Auto-generated method stub
         
      }

      public void log(Exception arg0, String arg1)
      {
         // TODO Auto-generated method stub
         
      }

      public void log(String arg0, Throwable arg1)
      {
         // TODO Auto-generated method stub
         
      }

      public void removeAttribute(String arg0)
      {
         // TODO Auto-generated method stub
         
      }

      public void setAttribute(String arg0, Object arg1)
      {
         // TODO Auto-generated method stub
         
      }
      
   }
}
