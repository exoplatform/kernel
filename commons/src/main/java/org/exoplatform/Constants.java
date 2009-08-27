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
package org.exoplatform;

import java.util.List;
import java.util.Map;

/**
 * @author : Mestrallet Benjamin
 */
public class Constants
{
   final static public List EMPTY_LIST = new java.util.ArrayList();

   final static public Map EMPTY_MAP = new java.util.HashMap();

   final static public String AMPERSAND = "&amp;";

   final static public String PARAMETER_ENCODER = "portal:";

   final static public String PORTLET_META_DATA_ENCODER = "/";

   final static public String PORTLET_HANDLE_ENCODER = "/";

   final static public String PROPERTY_ENCODER = "property:";

   public static final String PORTAL_CONTEXT = PARAMETER_ENCODER + "ctx";

   final static public String EXO_PORTAL_INFO = "javax.portlet.exo-portal-info";

   public static final String PORTAL_PROCESS_ACTION = "action";

   public static final String PORTAL_SERVE_RESOURCE = "resource";

   public static final String PORTAL_RENDER = "render";

   public static final String COMPONENT_PARAMETER = PARAMETER_ENCODER + "componentId";

   public static final String TYPE_PARAMETER = PARAMETER_ENCODER + "type";

   public static final String WINDOW_STATE_PARAMETER = PARAMETER_ENCODER + "windowState";

   public static final String PORTLET_MODE_PARAMETER = PARAMETER_ENCODER + "portletMode";

   public static final String RESOURCE_ID_PARAMETER = PARAMETER_ENCODER + "resourceID";

   public static final String CACHELEVEL_PARAMETER = PARAMETER_ENCODER + "cacheLevel";

   public static final String SECURE_PARAMETER = PARAMETER_ENCODER + "isSecure";

   public static final String LANGUAGE_PARAMETER = PARAMETER_ENCODER + "lang";

   public static final String PORTLET_CONTAINER = "org.exoplatform.portletcontainer";

   public static final String PORTLET_META_DATA = PORTLET_CONTAINER + ".portlet";

   public static final String SERVLET_META_DATA = PORTLET_CONTAINER + ".servlet";

   public static final String PORTLET_ENCODER = "_portlet_";

   public static final String VALIDATOR_ENCODER = "_validator_";

   public static final String FILTER_ENCODER = "_filter_";

   public static final String MESSAGE_LISTENER_ENCODER = "_message_listener_";

   public static final String APPLICATION_RESOURCE = "javax.portlet.application-resource";

   final static public String ANON_USER = "anon";

   // portlets
   public static final String FORWARD_PAGE = "org.exoplatform.portal.portlet.ForwardPage";

   // roles
   public static final String USER_ROLE = "user";

   public static final String ADMIN_ROLE = "admin";

   public static final String GUEST_ROLE = "guest";

   public static final String DEFAUL_PORTAL_OWNER = "exo";

   // security
   public static final String PUBLIC = "public";

   public static final String PRIVATE = "private";
}
