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
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.container;

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
      return servletContext.getServletContextName();
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
