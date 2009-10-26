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

import org.exoplatform.services.remote.group.Message;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Feb 21, 2005
 * @version $Id: MessageImpl.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class MessageImpl implements Message
{
   public String target_;

   public Object message_;

   public String getTargetHandler()
   {
      return target_;
   }

   public void setTargetHandler(String targetHandler)
   {
      target_ = targetHandler;
   }

   public Object getMessage()
   {
      return message_;
   }

   public void setMessage(Object obj)
   {
      message_ = obj;
   }
}
