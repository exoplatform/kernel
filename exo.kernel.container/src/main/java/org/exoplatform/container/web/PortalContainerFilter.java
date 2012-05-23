/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.container.web;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This filter will allow to prevent to any accesses to a web application corresponding to a
 * {@link PortalContainer} that has been disabled.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 8 juil. 2010  
 */
public class PortalContainerFilter extends AbstractFilter
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.PortalContainerFilter");

   /**
    * @see javax.servlet.Filter#destroy()
    */
   public void destroy()
   {
   }

   /**
    * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
    */
   public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
      ServletException
   {
      if (PortalContainer.isPortalContainerNameDisabled(servletContextName))
      {
         // The current portal container has been disabled
         onPortalContainerDisabled(request, response, chain);
         return;
      }
      else if (PropertyManager.isDevelopping())
      {
         HttpSession sess = ((HttpServletRequest)request).getSession(false);
         if (sess != null && sess.getAttribute(RootContainer.SESSION_TO_BE_INVALIDATED_ATTRIBUTE_NAME) != null)
         {
            sess.invalidate();
         }
      }
      chain.doFilter(request, response);
   }

   /**
    * Allow the sub classed to execute a task when a user try to access to a 
    * web application corresponding to a {@link PortalContainer} that has been disabled.
    * @param request the {@link ServletRequest}
    * @param response the {@link ServletResponse}
    * @param response the {@link FilterChain}
    */
   protected void onPortalContainerDisabled(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException
   {
      if (PropertyManager.isDevelopping())
      {
         LOG.info("The portal container corresponding to the webapp '" + servletContextName
            + "' is disabled, thus the request is cancelled: target URI was "
            + ((HttpServletRequest)request).getRequestURI());
      }
      ((HttpServletResponse)response).sendError(HttpServletResponse.SC_NOT_FOUND);
   }
}
