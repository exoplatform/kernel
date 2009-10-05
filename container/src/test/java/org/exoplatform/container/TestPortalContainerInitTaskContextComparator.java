/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.container;

import junit.framework.TestCase;

import org.exoplatform.container.RootContainer.PortalContainerInitTaskContext;
import org.exoplatform.container.RootContainer.PortalContainerInitTaskContextComparator;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
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
 * 24 sept. 2009  
 */
@SuppressWarnings("unchecked")
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

      public Enumeration getAttributeNames()
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

      public Enumeration getInitParameterNames()
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

      public Set getResourcePaths(String path)
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

      public Enumeration getServletNames()
      {

         return null;
      }

      public Enumeration getServlets()
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

   }   
}
