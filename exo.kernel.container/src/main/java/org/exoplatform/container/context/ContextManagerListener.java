/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container.context;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.web.AbstractHttpSessionListener;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestEvent;
import javax.servlet.ServletRequestListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;

/**
 * This listener is used to manage the components of scope {@link SessionScoped} and {@link RequestScoped}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ContextManagerListener extends AbstractHttpSessionListener implements ServletRequestListener
{

   /**
    * {@inheritDoc}
    */
   public void requestDestroyed(ServletRequestEvent event)
   {
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer(event);
         if (!container.equals(oldContainer))
         {
            if (container instanceof PortalContainer)
            {
               PortalContainer.setInstance((PortalContainer)container);
            }
            ExoContainerContext.setCurrentContainer(container);
            hasBeenSet = true;
         }
         onRequestDestroyed(container, event);
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
      }
   }

   /**
    * {@inheritDoc}
    */
   public void requestInitialized(ServletRequestEvent event)
   {
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainer();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer(event);
         if (!container.equals(oldContainer))
         {
            if (container instanceof PortalContainer)
            {
               PortalContainer.setInstance((PortalContainer)container);
            }
            ExoContainerContext.setCurrentContainer(container);
            hasBeenSet = true;
         }
         onRequestInitialized(container, event);
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
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected boolean requirePortalEnvironment()
   {
      return false;
   }

   /**
    * Called when a request is created
    */
   protected void onRequestInitialized(ExoContainer container, ServletRequestEvent event)
   {
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         return;
      }
      if (event.getServletRequest() instanceof HttpServletRequest)
      {
         HttpServletRequest request = (HttpServletRequest)event.getServletRequest();
         HttpSession session = request.getSession(false);
         if (session != null)
         {
            AdvancedContext<HttpSession> ctx = manager.<HttpSession> getContext(SessionScoped.class);
            if (ctx != null)
            {
               ctx.activate(session);
            }
         }
      }
      AdvancedContext<ServletRequest> ctx = manager.<ServletRequest> getContext(RequestScoped.class);
      if (ctx != null)
      {
         ctx.activate(event.getServletRequest());
      }
   }

   /**
    * Called when a request is destroyed
    */
   protected void onRequestDestroyed(ExoContainer container, ServletRequestEvent event)
   {
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         return;
      }
      if (event.getServletRequest() instanceof HttpServletRequest)
      {
         HttpServletRequest request = (HttpServletRequest)event.getServletRequest();
         HttpSession session = request.getSession(false);
         if (session != null)
         {
            AdvancedContext<HttpSession> ctx = manager.<HttpSession> getContext(SessionScoped.class);
            if (ctx != null)
            {
               ctx.deactivate(session);
            }
         }
      }
      AdvancedContext<ServletRequest> ctx = manager.<ServletRequest> getContext(RequestScoped.class);
      if (ctx != null)
      {
         ctx.deactivate(event.getServletRequest());
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void onSessionCreated(ExoContainer container, HttpSessionEvent event)
   {
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         return;
      }
      AdvancedContext<HttpSession> ctx = manager.<HttpSession> getContext(SessionScoped.class);
      if (ctx == null)
      {
         return;
      }
      ctx.register(event.getSession());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void onSessionDestroyed(ExoContainer container, HttpSessionEvent event)
   {
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         return;
      }
      AdvancedContext<HttpSession> ctx = manager.<HttpSession> getContext(SessionScoped.class);
      if (ctx == null)
      {
         return;
      }
      ctx.unregister(event.getSession());
   }

   /**
    * @return Gives the {@link ExoContainer} that fits best with the current context
    */
   protected final ExoContainer getContainer(ServletRequestEvent event)
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      if (container instanceof RootContainer)
      {
         // The top container is a RootContainer, thus we assume that we are in a portal mode
         container = PortalContainer.getCurrentInstance(event.getServletContext());
         if (container == null)
         {
            container = ExoContainerContext.getTopContainer();
         }
      }
      // The container is a PortalContainer or a StandaloneContainer
      return container;
   }
}
