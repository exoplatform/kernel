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

/*
 * @author: Tuan Nguyen
 * @version: $Id: UniqueObjectException.java,v 1.1 2004/10/21 15:23:40 tuan08 Exp $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class UniqueObjectException extends ExoMessageException
{

   public UniqueObjectException(String messageKey)
   {
      super(messageKey);
   }

   public UniqueObjectException(String messageKey, Object[] args)
   {
      super(messageKey, args);
   }

   public String getExceptionDescription()
   {
      return "Usually, this exception is raised when the system detect 2 or more "
         + "objects with the same id the database or a tree of components";
   }

   public String getErrorCode()
   {
      return "UNIQUE_CONSTRAINT: ";
   }
}
