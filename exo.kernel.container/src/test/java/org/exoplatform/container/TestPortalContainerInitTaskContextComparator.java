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

import junit.framework.TestCase;

import org.exoplatform.container.RootContainer.PortalContainerInitTaskContext;
import org.exoplatform.container.RootContainer.PortalContainerInitTaskContextComparator;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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
 * 24 sept. 2009  
 */
public class TestPortalContainerInitTaskContextComparator extends TestCase
{
 
   public void testCompare()
   {
      PortalContainerInitTaskContextComparator comparator = new PortalContainerInitTaskContextComparator(Arrays.asList("d", "c"));
      PortalContainerInitTaskContext[] contexts =
         {createPortalContainerInitTaskContext("b"), createPortalContainerInitTaskContext("d"), createPortalContainerInitTaskContext("a"),
         createPortalContainerInitTaskContext("c")};
      Arrays.sort(contexts, comparator);
      assertEquals("d", contexts[0].getServletContextName());
      assertEquals("c", contexts[1].getServletContextName());
      assertEquals("a", contexts[2].getServletContextName());
      assertEquals("b", contexts[3].getServletContextName());
   }
   

   private PortalContainerInitTaskContext createPortalContainerInitTaskContext(String name)
   {
      return new PortalContainerInitTaskContext(new MockServletContext(name), null);
   }
      
   private static class MockServletContext implements ServletContext
   {

      private final String name;

      private MockServletContext(String name)
      {
         this.name = name;
      }

      public Object getAttribute(String name)
      {

         return null;
      }

      public Enumeration<String> getAttributeNames()
      {

         return null;
      }

      public ServletContext getContext(String uripath)
      {

         return null;
      }

      public String getInitParameter(String name)
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

      public String getMimeType(String file)
      {

         return null;
      }

      public int getMinorVersion()
      {

         return 0;
      }

      public RequestDispatcher getNamedDispatcher(String name)
      {

         return null;
      }

      public String getRealPath(String path)
      {

         return null;
      }

      public RequestDispatcher getRequestDispatcher(String path)
      {

         return null;
      }

      public URL getResource(String path) throws MalformedURLException
      {

         return null;
      }

      public InputStream getResourceAsStream(String path)
      {

         return null;
      }

      public Set<String> getResourcePaths(String path)
      {

         return null;
      }

      public String getServerInfo()
      {

         return null;
      }

      public Servlet getServlet(String name) throws ServletException
      {

         return null;
      }

      public String getServletContextName()
      {
         return name;
      }

      public Enumeration<String> getServletNames()
      {

         return null;
      }

      public Enumeration<Servlet> getServlets()
      {

         return null;
      }

      public void log(String msg)
      {

      }

      public void log(Exception exception, String msg)
      {

      }

      public void log(String message, Throwable throwable)
      {

      }

      public void removeAttribute(String name)
      {

      }

      public void setAttribute(String name, Object object)
      {

      }

      public String getContextPath()
      {
         return null;
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
