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
package org.exoplatform.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created by The eXo Platform SARL . Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Date: Jul 25, 2003 Time: 12:26:58 AM
 */
public class MockServletContext implements ServletContext
{

   private String name_;

   private Map<String, String> initParams_;

   private Map<String, Object> attributes_;

   private String contextPath_;

   private StringBuffer logBuffer = new StringBuffer();

   public MockServletContext()
   {
      this("portlet_app_1");
   }

   public MockServletContext(String name)
   {
      this(name, "/" + name);
   }

   public MockServletContext(String name, String path)
   {
      this.name_ = name;
      this.contextPath_ = path;
      this.initParams_ = new HashMap<String, String>();
      this.attributes_ = new HashMap<String, Object>();
      this.attributes_.put("javax.servlet.context.tempdir", path);
   }

   public void setName(String name)
   {
      name_ = name;
   }

   public String getLogBuffer()
   {
      try
      {
         return logBuffer.toString();
      }
      finally
      {
         logBuffer = new StringBuffer();
      }
   }

   public ServletContext getContext(String s)
   {
      return null;
   }

   public int getMajorVersion()
   {
      return 2;
   }

   public int getMinorVersion()
   {
      return 5;
   }

   public String getMimeType(String s)
   {
      return "text/html";
   }

   public Set getResourcePaths(String s)
   {
      Set<String> set = new HashSet<String>();
      set.add("/test1");
      set.add("/WEB-INF");
      set.add("/test2");
      return set;
   }

   public URL getResource(String s) throws MalformedURLException
   {
      String path = "file:" + contextPath_ + s;
      URL url = new URL(path);
      return url;
   }

   public InputStream getResourceAsStream(String s)
   {
      try
      {
         return getResource(s).openStream();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return null;
   }

   public RequestDispatcher getRequestDispatcher(String s)
   {
      return null;
   }

   public RequestDispatcher getNamedDispatcher(String s)
   {
      return null;
   }

   public Servlet getServlet(String s) throws ServletException
   {
      return null;
   }

   public Enumeration getServlets()
   {
      return null;
   }

   public Enumeration getServletNames()
   {
      return null;
   }

   public void log(String s)
   {
      logBuffer.append(s);
   }

   public void log(Exception e, String s)
   {
      logBuffer.append(s + e.getMessage());
   }

   public void log(String s, Throwable throwable)
   {
      logBuffer.append(s + throwable.getMessage());
   }

   public void setContextPath(String s)
   {
      contextPath_ = s;
   }

   public String getRealPath(String s)
   {
      return contextPath_ + s;
   }

   public String getServerInfo()
   {
      return null;
   }

   public void setInitParameter(String name, String value)
   {
      initParams_.put(name, value);
   }

   public String getInitParameter(String name)
   {
      return (String)initParams_.get(name);
   }

   public Enumeration getInitParameterNames()
   {
      Vector<String> keys = new Vector<String>(initParams_.keySet());
      return keys.elements();
   }

   public Object getAttribute(String name)
   {
      return attributes_.get(name);
   }

   public Enumeration getAttributeNames()
   {
      Vector<String> keys = new Vector<String>(attributes_.keySet());
      return keys.elements();
   }

   public void setAttribute(String name, Object value)
   {
      attributes_.put(name, value);
   }

   public void removeAttribute(String name)
   {
      attributes_.remove(name);
   }

   public String getServletContextName()
   {
      return name_;
   }

   public String getContextPath()
   {
      return contextPath_;
   }
}
