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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.remote.group.Message;
import org.exoplatform.services.remote.group.MessageHandler;
import org.exoplatform.services.remote.group.MessageHandlerMonitor;
import org.jgroups.blocks.RequestHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Mar 4, 2005
 * @version $Id: RequestHandlerImpl.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class RequestHandlerImpl implements RequestHandler
{
   private Map messageHandlers_ = new HashMap();

   private static final Log LOG = ExoLogger.getLogger(RequestHandlerImpl.class.getName());

   public RequestHandlerImpl()
   {
   }

   public void registerMessageHandler(MessageHandler handler)
   {
      messageHandlers_.put(handler.getIdentifier(), handler);
   }

   public Object handle(org.jgroups.Message jmessage)
   {
      Message message = (Message)jmessage.getObject();
      String handlerId = message.getTargetHandler();
      MessageHandler handler = (MessageHandler)messageHandlers_.get(handlerId);
      MessageHandlerMonitor monitor = handler.getMonitor();
      if (handler != null)
      {
         try
         {
            monitor.addMessageCounter(1);
            return handler.handle(message);
         }
         catch (Exception ex)
         {
            monitor.setLastError(ex);
            LOG.error("Error :", ex);
            return null;
         }
      }
      LOG.info("Cannot finf the message handler for the request handler: " + handlerId);
      return null;
   }

   public List getMessageHandlerMonitors()
   {
      List monitors = new ArrayList();
      Iterator i = messageHandlers_.values().iterator();
      while (i.hasNext())
      {
         MessageHandler handler = (MessageHandler)i.next();
         monitors.add(handler.getMonitor());
      }
      return monitors;
   }
}
