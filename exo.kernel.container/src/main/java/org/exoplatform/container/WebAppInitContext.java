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

import org.exoplatform.container.util.ContainerUtil;

import javax.servlet.ServletContext;

/**
 * This class is used to define the initialization context of a web application
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 11 sept. 2009  
 */
public class WebAppInitContext
{

   /**
    * The servlet context of the web application
    */
   private final ServletContext servletContext;

   /**
    * The class loader of the web application;
    */
   private final ClassLoader webappClassLoader;

   public WebAppInitContext(ServletContext servletContext)
   {
      this.servletContext = servletContext;
      this.webappClassLoader = Thread.currentThread().getContextClassLoader();
   }

   public ServletContext getServletContext()
   {
      return servletContext;
   }

   public String getServletContextName()
   {
      return ContainerUtil.getServletContextName(servletContext);
   }

   public ClassLoader getWebappClassLoader()
   {
      return webappClassLoader;
   }

   @Override
   public boolean equals(Object o)
   {
      if (o instanceof WebAppInitContext)
      {
         return getServletContextName().equals(((WebAppInitContext)o).getServletContextName());
      }
      return false;
   }

   @Override
   public int hashCode()
   {
      return getServletContextName().hashCode();
   }
}
