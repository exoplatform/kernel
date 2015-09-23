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

import org.exoplatform.container.ar.Archive;
import org.exoplatform.container.xml.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.Map;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 22 fevr. 2010  
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

      url = cm.getURL("classpath:org/exoplatform/container/configuration/empty-config.xml");
      assertTrue(url.getPath().endsWith("org/exoplatform/container/configuration/empty-config.xml"));
      checkURL(url);

      url = cm.getURL("jar:org/exoplatform/container/configuration/empty-config.xml");
      assertTrue(url.getPath().endsWith("org/exoplatform/container/configuration/empty-config.xml"));
      checkURL(url);

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
      // Check relative path
      cm.addConfiguration(cm.getURL(sURL + "/configuration/empty-config.xml"));
      url = cm.getURL("import-configuration.xml");
      checkURL(url);

      String sArchiveURL = sURL.replace("file:", Archive.PROTOCOL + ":");
      url = cm.getURL(sArchiveURL + "/configuration/empty-config.xml");
      checkURL(url);
      url = cm.getURL(sArchiveURL + "/configuration/empty-config-fake.xml");
      checkURL(url, true);
      url = cm.getURL(sArchiveURL + "\\configuration\\empty-config.xml");
      checkURL(url);
      url = cm.getURL(sArchiveURL + "\\configuration\\empty-config-fake.xml");
      checkURL(url, true);
      // Check relative path
      cm.addConfiguration(cm.getURL(sArchiveURL + "/configuration/empty-config.xml"));
      url = cm.getURL("import-configuration.xml");
      checkURL(url);

      // Clear the context path
      cm = new ConfigurationManagerImpl();

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

   public void testGetFileURL() throws Exception
   {
      // Empty CM
      ConfigurationManager cm = new ConfigurationManagerImpl();
      URL url = cm.getURL(null);
      assertNull(url);
      url = cm.getURL("file:F:\\somepath\\path\\configuration.xml");
      assertEquals("file:/F:/somepath/path/configuration.xml", url.toString());

      //make context configuration starting fith "file:D:..."
      try
      {
         cm.addConfiguration("file:D:\\somepath\\config.xml");
      }
      catch (Exception e)
      {
         // thats is ok, because such config does not exists, 
         // but ConfigurationManagerInmp.contextPath going to be initialized
         // thats all we need to reproduce bug.
      }

      // now lets check relative url
      url = cm.getURL("configuration.xml");
      assertEquals("file:/D:/somepath/configuration.xml", url.toString());
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
         return null;
      }

      public Enumeration<String> getAttributeNames()
      {
         return null;
      }

      public ServletContext getContext(String arg0)
      {
         return null;
      }

      public String getContextPath()
      {
         return null;
      }

      public String getInitParameter(String arg0)
      {
         return null;
      }

      public Enumeration<String> getInitParameterNames()
      {
         return null;
      }

      public int getMajorVersion()
      {
         return 0;
      }

      public String getMimeType(String arg0)
      {
         return null;
      }

      public int getMinorVersion()
      {
         return 0;
      }

      public RequestDispatcher getNamedDispatcher(String arg0)
      {
         return null;
      }

      public String getRealPath(String arg0)
      {
         return null;
      }

      public RequestDispatcher getRequestDispatcher(String arg0)
      {
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
         return null;
      }

      public Set<String> getResourcePaths(String arg0)
      {
         return null;
      }

      public String getServerInfo()
      {
         return null;
      }

      public Servlet getServlet(String arg0) throws ServletException
      {
         return null;
      }

      public String getServletContextName()
      {
         return null;
      }

      public Enumeration<String> getServletNames()
      {
         return null;
      }

      public Enumeration<Servlet> getServlets()
      {
         return null;
      }

      public void log(String arg0)
      {
      }

      public void log(Exception arg0, String arg1)
      {
      }

      public void log(String arg0, Throwable arg1)
      {
      }

      public void removeAttribute(String arg0)
      {
      }

      public void setAttribute(String arg0, Object arg1)
      {
      }

      public int getEffectiveMajorVersion()
      {
         return 0;
      }

      public int getEffectiveMinorVersion()
      {
         return 0;
      }

      public boolean setInitParameter(String name, String value)
      {
         return false;
      }

      public Dynamic addServlet(String servletName, String className)
      {
         return null;
      }

      public Dynamic addServlet(String servletName, Servlet servlet)
      {
         return null;
      }

      public Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass)
      {
         return null;
      }

      public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException
      {
         return null;
      }

      public ServletRegistration getServletRegistration(String servletName)
      {
         return null;
      }

      public Map<String, ? extends ServletRegistration> getServletRegistrations()
      {
         return null;
      }

      public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, String className)
      {
         return null;
      }

      public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Filter filter)
      {
         return null;
      }

      public javax.servlet.FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass)
      {
         return null;
      }

      public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException
      {
         return null;
      }

      public FilterRegistration getFilterRegistration(String filterName)
      {
         return null;
      }

      public Map<String, ? extends FilterRegistration> getFilterRegistrations()
      {
         return null;
      }

      public SessionCookieConfig getSessionCookieConfig()
      {
         return null;
      }

      public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes)
      {
      }

      public Set<SessionTrackingMode> getDefaultSessionTrackingModes()
      {
         return null;
      }

      public Set<SessionTrackingMode> getEffectiveSessionTrackingModes()
      {
         return null;
      }

      public void addListener(String className)
      {
      }

      public <T extends EventListener> void addListener(T t)
      {
      }

      public void addListener(Class<? extends EventListener> listenerClass)
      {
      }

      public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException
      {
         return null;
      }

      public JspConfigDescriptor getJspConfigDescriptor()
      {
         return null;
      }

      public ClassLoader getClassLoader()
      {
         return null;
      }

      public void declareRoles(String... roleNames)
      {
      }
   }
}
