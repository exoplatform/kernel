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
package org.exoplatform.container.web;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * This class is the root class of all the Filters that require an ExoContainer 
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 21 aout 2009  
 */
public abstract class AbstractFilter implements Filter
{

   /**
    * The filter configuration
    */
   protected FilterConfig config;

   /**
    * The Servlet context name
    */
   protected String servletContextName;

   /**
    * Indicates if we need a portal environment.
    */
   private volatile Boolean requirePortalEnvironment;

   /**
    * {@inheritDoc}
    */
   public final void init(FilterConfig config) throws ServletException
   {
      this.config = config;
      this.servletContextName = config.getServletContext().getServletContextName();
      afterInit(config);
   }

   /**
    * Allows sub-classes to initialize 
    * @param config the current filter configuration
    */
   protected void afterInit(FilterConfig config) throws ServletException
   {
   }

   /**
    * @return Gives the {@link ExoContainer} that fits best with the current context
    */
   protected final ExoContainer getContainer()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      if (container instanceof RootContainer)
      {
         // The top container is a RootContainer, thus we assume that we are in a portal mode
         container = PortalContainer.getCurrentInstance(config.getServletContext());
         if (container == null)
         {
            container = ExoContainerContext.getTopContainer();
         }
      }
      // The container is a PortalContainer or a StandaloneContainer
      return container;
   }

   /**
    * Indicates if it requires that a full portal environment must be set
    * @return <code>true</code> if it requires the portal environment <code>false</code> otherwise.
    */
   protected boolean requirePortalEnvironment()
   {
      if (requirePortalEnvironment == null)
      {
         synchronized (this)
         {
            if (requirePortalEnvironment == null)
            {
               this.requirePortalEnvironment = PortalContainer.isPortalContainerName(servletContextName);
            }
         }
      }
      return requirePortalEnvironment.booleanValue();
   }

   /**
    * @return the current {@link ServletContext}
    */
   protected ServletContext getServletContext()
   {
      if (requirePortalEnvironment())
      {
         ExoContainer container = getContainer();
         if (container instanceof PortalContainer)
         {
            return ((PortalContainer)container).getPortalContext();
         }
      }
      return config.getServletContext();
   }
}
