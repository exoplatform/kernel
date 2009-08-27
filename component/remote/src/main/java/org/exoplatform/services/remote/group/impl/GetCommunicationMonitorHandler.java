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
package org.exoplatform.services.remote.group.impl;

import org.exoplatform.services.remote.group.CommunicationService;
import org.exoplatform.services.remote.group.Message;
import org.exoplatform.services.remote.group.MessageHandler;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Mar 5, 2005
 * @version $Id: GetCommunicationMonitorHandler.java 5799 2006-05-28 17:55:42Z
 *          geaz $
 */
public class GetCommunicationMonitorHandler extends MessageHandler
{
   final static public String IDENTIFIER = "GetCommunicationMonitorHandler";

   private CommunicationService service_;

   public GetCommunicationMonitorHandler()
   {
      super(IDENTIFIER);
   }

   public void init(CommunicationService service)
   {
      service_ = service;
   }

   public Object handle(Message message) throws Exception
   {
      return service_.getCommunicationServiceMonitor(null);
   }
}
