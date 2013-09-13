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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.servlet.ServletContext;
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
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ContextManagerListener");

   /**
    * {@inheritDoc}
    */
   public void requestDestroyed(ServletRequestEvent event)
   {
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer(event);
         if (container == null)
            return;
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
      final ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
      ExoContainer container = null;
      boolean hasBeenSet = false;
      try
      {
         container = getContainer(event);
         if (container == null)
            return;
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
      if (LOG.isTraceEnabled())
      {
         LOG.trace("onRequestInitialized called on container = "
            + (container == null ? null : container.getContext().getName()));
      }
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestInitialized: no context manager found");
         }
         return;
      }
      AdvancedContext<HttpSession> ctxSess = manager.<HttpSession> getContext(SessionScoped.class);
      if (ctxSess != null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestInitialized: A context has been found for the scope session");
         }
         if (event.getServletRequest() instanceof HttpServletRequest)
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("onRequestInitialized: The request is an HttpServletRequest");
            }
            HttpServletRequest request = (HttpServletRequest)event.getServletRequest();
            HttpSession session = request.getSession(false);
            if (session != null)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("onRequestInitialized: A session has been found");
               }
               ctxSess.activate(session);
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("onRequestInitialized: Session activated");
               }
            }
         }
      }
      AdvancedContext<ServletRequest> ctxReq = manager.<ServletRequest> getContext(RequestScoped.class);
      if (ctxReq != null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestInitialized: A context has been found for the scope request");
         }
         ctxReq.activate(event.getServletRequest());
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestInitialized: Request activated");
         }
      }
   }

   /**
    * Called when a request is destroyed
    */
   protected void onRequestDestroyed(ExoContainer container, ServletRequestEvent event)
   {
      if (LOG.isTraceEnabled())
      {
         LOG.trace("onRequestDestroyed called on container = "
            + (container == null ? null : container.getContext().getName()));
      }
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestDestroyed: no context manager found");
         }
         return;
      }
      AdvancedContext<HttpSession> ctxSess = manager.<HttpSession> getContext(SessionScoped.class);
      if (ctxSess != null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestDestroyed: A context has been found for the scope session");
         }
         if (event.getServletRequest() instanceof HttpServletRequest)
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("onRequestDestroyed: The request is an HttpServletRequest");
            }
            HttpServletRequest request = (HttpServletRequest)event.getServletRequest();
            HttpSession session = request.getSession(false);
            if (session != null)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("onRequestDestroyed: A session has been found");
               }
               ctxSess.deactivate(session);
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("onRequestDestroyed: Session deactivated");
               }
            }
         }
      }
      AdvancedContext<ServletRequest> ctxReq = manager.<ServletRequest> getContext(RequestScoped.class);
      if (ctxReq != null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestDestroyed: A context has been found for the scope request");
         }
         ctxReq.deactivate(event.getServletRequest());
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onRequestDestroyed: Request deactivated");
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void onSessionCreated(ExoContainer container, HttpSessionEvent event)
   {
      if (LOG.isTraceEnabled())
      {
         LOG.trace("onSessionCreated called on container = "
            + (container == null ? null : container.getContext().getName()));
      }
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onSessionCreated: no context manager found");
         }
         return;
      }
      AdvancedContext<HttpSession> ctx = manager.<HttpSession> getContext(SessionScoped.class);
      if (ctx == null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onSessionCreated: no context found for the scope session");
         }
         return;
      }
      ctx.register(event.getSession());
      if (LOG.isTraceEnabled())
      {
         LOG.trace("onSessionCreated: Session registered");
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected void onSessionDestroyed(ExoContainer container, HttpSessionEvent event)
   {
      if (LOG.isTraceEnabled())
      {
         LOG.trace("onSessionDestroyed called on container = "
            + (container == null ? null : container.getContext().getName()));
      }
      ContextManager manager = container.getContextManager();
      if (manager == null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onSessionDestroyed: no context manager found");
         }
         return;
      }
      AdvancedContext<HttpSession> ctx = manager.<HttpSession> getContext(SessionScoped.class);
      if (ctx == null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("onSessionDestroyed: no context found for the scope session");
         }
         return;
      }
      ctx.unregister(event.getSession());
      if (LOG.isTraceEnabled())
      {
         LOG.trace("onSessionCreated: Session unregistered");
      }
   }

   /**
    * @return Gives the {@link ExoContainer} that fits best with the current context
    */
   protected final ExoContainer getContainer(ServletRequestEvent event)
   {
      ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
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

   /**
    * Registers dynamically the listener if a {@link ContextManager} has been defined and
    * a context for SessionScoped and/or RequestScoped has been configured.
    * 
    * @param container the container from which we will get the {@link ContextManager}
    * @param context the context to which we want to add the listener
    */
   public static void registerIfNeeded(ExoContainer container, ServletContext context)
   {
      ContextManager manager = container.getContextManager();
      if (manager != null && (manager.hasContext(SessionScoped.class) || manager.hasContext(RequestScoped.class)))
      {
         // A Context manager has been defined and a context for SessionScoped and/or RequestScoped has
         // been configured. So we can add dynamically the listener.
         context.addListener(ContextManagerListener.class);
      }
   }
}
