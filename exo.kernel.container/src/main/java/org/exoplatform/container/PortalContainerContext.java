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

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * This class is used to merge all the {@link ServletContext} related to a given portal container.
 * It will used the {@link WebAppInitContext} that have been defined in the related portal container.
 * It will always consider that the {@link WebAppInitContext}
 * with the highest priority has always right, in other words for example in the method
 * getInitParameter, it will try to get the init parameter in the {@link WebAppInitContext} 
 * of the highest priority, if it cans not find it, it will try the {@link WebAppInitContext}
 *  with the second highest priority and so on. The priority of the {@link WebAppInitContext} is
 *  the order given by the method PortalContainer.getWebAppInitContexts(), 
 * the last {@link WebAppInitContext} is the one with the highest priority.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 14 sept. 2009  
 */
class PortalContainerContext implements ServletContext
{

   /**
    * The related portal container
    */
   private final PortalContainer container;

   PortalContainerContext(PortalContainer container)
   {
      this.container = container;
   }

   private WebAppInitContext[] getWebAppInitContexts()
   {
      final Set<WebAppInitContext> contexts = container.getWebAppInitContexts();
      final WebAppInitContext[] aContexts = new WebAppInitContext[contexts.size()];
      return (WebAppInitContext[])contexts.toArray(aContexts);
   }

   private ServletContext getPortalContext()
   {
      return container.portalContext;
   }

   /**
    * {@inheritDoc}
    */
   public Object getAttribute(String name)
   {
      return getPortalContext().getAttribute(name);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Enumeration getAttributeNames()
   {
      return getPortalContext().getAttributeNames();
   }

   /**
    * {@inheritDoc}
    */
   public ServletContext getContext(String uripath)
   {
      return getPortalContext().getContext(uripath);
   }

   /**
    * {@inheritDoc}
    */
   public String getInitParameter(String name)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         String param = context.getInitParameter(name);
         if (param != null)
         {
            return param;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Enumeration<String> getInitParameterNames()
   {
      final Set<WebAppInitContext> contexts = container.getWebAppInitContexts();
      Set<String> names = null;
      for (WebAppInitContext context : contexts)
      {
         Enumeration<String> eNames = context.getServletContext().getAttributeNames();
         if (eNames != null)
         {
            if (names == null)
            {
               names = new HashSet<String>();
            }
            names.addAll(Collections.list(eNames));
         }
      }
      if (names == null)
      {
         return null;
      }
      return Collections.enumeration(names);
   }

   /**
    * {@inheritDoc}
    */
   public int getMajorVersion()
   {
      return getPortalContext().getMajorVersion();
   }

   /**
    * {@inheritDoc}
    */
   public String getMimeType(String file)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         String mimeType = context.getMimeType(file);
         if (mimeType != null)
         {
            return mimeType;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public int getMinorVersion()
   {
      return getPortalContext().getMinorVersion();
   }

   /**
    * {@inheritDoc}
    */
   public RequestDispatcher getNamedDispatcher(String name)
   {
      return getPortalContext().getNamedDispatcher(name);
   }

   /**
    * {@inheritDoc}
    */
   public String getRealPath(String path)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final InputStream is = context.getResourceAsStream(path);
         if (is != null)
         {
            // The resource exists within this servlet context
            return context.getRealPath(path);
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public RequestDispatcher getRequestDispatcher(String path)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final InputStream is = context.getResourceAsStream(path);
         if (is != null)
         {
            // The resource exists within this servlet context
            return context.getRequestDispatcher(path);
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public URL getResource(String path) throws MalformedURLException
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final URL url = context.getResource(path);
         if (url != null)
         {
            return url;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public InputStream getResourceAsStream(String path)
   {
      final WebAppInitContext[] contexts = getWebAppInitContexts();
      for (int i = contexts.length - 1; i >= 0; i--)
      {
         final ServletContext context = contexts[i].getServletContext();
         final InputStream is = context.getResourceAsStream(path);
         if (is != null)
         {
            return is;
         }
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Set<String> getResourcePaths(String path)
   {
      final Set<WebAppInitContext> contexts = container.getWebAppInitContexts();
      Set<String> paths = null;
      for (WebAppInitContext context : contexts)
      {
         Set<String> sPaths = context.getServletContext().getResourcePaths(path);
         if (sPaths != null)
         {
            if (paths == null)
            {
               paths = new LinkedHashSet<String>();
            }
            paths.addAll(sPaths);
         }
      }
      return paths;
   }

   /**
    * {@inheritDoc}
    */
   public String getServerInfo()
   {
      return getPortalContext().getServerInfo();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("deprecation")
   public Servlet getServlet(String name) throws ServletException
   {
      return getPortalContext().getServlet(name);
   }

   /**
    * {@inheritDoc}
    */
   public String getServletContextName()
   {
      return getPortalContext().getServletContextName();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"deprecation", "unchecked"})
   public Enumeration getServletNames()
   {
      return getPortalContext().getServletNames();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"deprecation", "unchecked"})
   public Enumeration getServlets()
   {
      return getPortalContext().getServlets();
   }

   /**
    * {@inheritDoc}
    */
   public void log(String message)
   {
      getPortalContext().log(message);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("deprecation")
   public void log(Exception exception, String message)
   {
      getPortalContext().log(exception, message);
   }

   /**
    * {@inheritDoc}
    */
   public void log(String message, Throwable throwable)
   {
      getPortalContext().log(message, throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void removeAttribute(String name)
   {
      getPortalContext().removeAttribute(name);
   }

   /**
    * {@inheritDoc}
    */
   public void setAttribute(String name, Object object)
   {
      getPortalContext().setAttribute(name, object);
   }

   /**
    * {@inheritDoc}
    */
   public String getContextPath()
   {
      return getPortalContext().getContextPath();
   }
}
