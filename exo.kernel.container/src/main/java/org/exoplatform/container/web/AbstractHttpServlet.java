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

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 29 sept. 2009  
 */
public abstract class AbstractHttpServlet extends HttpServlet
{

   /**
    * Serial Version ID.
    */
   private static final long serialVersionUID = -3302886470677004895L;

   /**
    * The filter configuration
    */
   protected ServletConfig config;

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
   public final void init(ServletConfig config) throws ServletException
   {
      super.init(config);
      this.config = config;
      this.servletContextName = config.getServletContext().getServletContextName();
      afterInit(config);
   }

   /**
    * Allows sub-classes to initialize 
    * @param config the current servlet configuration
    */
   protected void afterInit(ServletConfig config) throws ServletException
   {
   }

   /**
    * @see javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
   public final void service(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
   {
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
      // Keep the old ClassLoader
      final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer();
         if (!container.equals(oldContainer))
         {
            if (container instanceof PortalContainer)
            {
               PortalContainer.setInstance((PortalContainer)container);
            }
            ExoContainerContext.setCurrentContainer(container);
            hasBeenSet = true;
         }
         if (requirePortalEnvironment() && container instanceof PortalContainer)
         {
            if (PortalContainer.getInstanceIfPresent() == null)
            {
               // The portal container has not been set
               PortalContainer.setInstance((PortalContainer)container);
               hasBeenSet = true;
            }
            // Set the full classloader of the portal container
            Thread.currentThread().setContextClassLoader(((PortalContainer)container).getPortalClassLoader());
         }
         onService(container, req, res);
      }
      finally
      {
         if (hasBeenSet)
         {
            if (container instanceof PortalContainer)
            {
               // Remove the current Portal Container and the current ExoContainer
               PortalContainer.setInstance(null);
            }
            // Re-set the old container
            ExoContainerContext.setCurrentContainer(oldContainer);
         }
         if (requirePortalEnvironment())
         {
            // Re-set the old classloader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
         }
      }
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
    * Allow the sub classes to execute a task when the method <code>service</code> is called 
    * @param container the eXo container
    * @param req the {@link HttpServletRequest}
    * @param res the {@link HttpServletResponse}
    */
   protected void onService(ExoContainer container, HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException
   {
      // Dispatches to the right HTTP method
      super.service(req, res);
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
    * @return the current {@link ServletContext}
    */
   @Override
   public ServletContext getServletContext()
   {
      if (requirePortalEnvironment())
      {
         ExoContainer container = getContainer();
         if (container instanceof PortalContainer)
         {
            return ((PortalContainer)container).getPortalContext();
         }
      }
      return super.getServletContext();
   }
}
