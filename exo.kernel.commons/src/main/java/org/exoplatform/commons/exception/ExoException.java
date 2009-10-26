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

import java.util.ResourceBundle;

/**
 * @author: Tuan Nguyen
 * @version: $Id: ExoException.java,v 1.5 2004/11/03 01:24:55 tuan08 Exp $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
abstract public class ExoException extends Exception
{
   static final public int FATAL = 0;

   static final public int ERROR = 1;

   static final public int WARN = 2;

   static final public int INFO = 3;

   private int severity_ = INFO;

   public int getSeverity()
   {
      return severity_;
   }

   public void setSeverity(int severity)
   {
      severity_ = severity;
   }

   abstract public String getMessage(ResourceBundle res);

   abstract public String getExceptionDescription();

   abstract public String getErrorCode();
}
