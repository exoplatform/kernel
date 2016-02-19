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
package org.exoplatform.commons.exception;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/*
 * @author: Tuan Nguyen
 * @version: $Id: ExoMessageException.java,v 1.2 2004/11/03 01:24:55 tuan08 Exp $
 * @since: 0.0
 */
public class ExoMessageException extends ExoException
{

   private String messageKey_;

   private Object[] args_;

   public ExoMessageException(String messageKey)
   {
      messageKey_ = messageKey;
   }

   public ExoMessageException(String messageKey, Object[] args)
   {
      messageKey_ = messageKey;
      args_ = args;
   }

   public String getMessageKey()
   {
      return messageKey_;
   }

   public Object[] getArguments()
   {
      return args_;
   }

   public String getMessage(ResourceBundle res)
   {
      if (args_ == null)
      {
         return res.getString(messageKey_);
      }
      return MessageFormat.format(res.getString(messageKey_), args_);
   }

   public String getExceptionDescription()
   {
      return "Usually, this is not a critical exception. The exception is raised "
         + "when unexpected condition such wrong input, object not found...."
         + "The application should not crashed and it should continue working";
   }

   public String getErrorCode()
   {
      return "EXO ERROR: ";
   }
}
