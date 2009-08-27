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
package org.exoplatform.container.client.http;

import org.exoplatform.container.client.ClientInfo;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Jan 12, 2005
 * @version $Id: HttpClientInfo.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class HttpClientInfo implements ClientInfo
{
   final static public String STANDARD_BROWSER_TYPE = "standard-browser";

   final static public String MOBILE_BROWSER_TYPE = "mobile-browser";

   final static public String PDA_BROWSER_TYPE = "pda-browser";

   private HttpClientType clientType_;

   private String ipAddress_;

   private String remoteUser_;

   public HttpClientInfo(HttpServletRequest request)
   {
      clientType_ = ClientTypeMap.getInstance().findClient(request.getHeader("User-Agent"));
      remoteUser_ = request.getRemoteUser();
      ipAddress_ = request.getRemoteAddr();
   }

   public String getClientType()
   {
      return clientType_.getType();
   }

   public String getRemoteUser()
   {
      return remoteUser_;
   }

   public String getIpAddress()
   {
      return ipAddress_;
   }

   public String getClientName()
   {
      return clientType_.getName();
   }

   public String getPreferredMimeType()
   {
      return clientType_.getPreferredMimeType();
   }

   public String getUserAgentPattern()
   {
      return clientType_.getUserAgentPattern();
   }
}
