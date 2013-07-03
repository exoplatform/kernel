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
package org.exoplatform.container.spi;

/**
 * Super class of any exception that could be thrown by a {@link Container}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ContainerException extends RuntimeException
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -6382969544216575848L;

   /**
    * Construct a new exception with no cause and no detail message. Note modern JVMs may still track the exception
    * that caused this one.
    */
   public ContainerException()
   {
   }

   /**
    * Construct a new exception with no cause and the specified detail message.  Note modern JVMs may still track the
    * exception that caused this one.
    *
    * @param message the message detailing the exception.
    */
   public ContainerException(String message)
   {
      super(message);
   }

   /**
    * Construct a new exception with the specified cause and no detail message.
    * 
    * @param cause the exception that caused this one.
    */
   public ContainerException(Throwable cause)
   {
      super(cause);
   }

   /**
    * Construct a new exception with the specified cause and the specified detail message.
    *
    * @param message the message detailing the exception.
    * @param cause   the exception that caused this one.
    */
   public ContainerException(String message, Throwable cause)
   {
      super(message, cause);
   }
}
