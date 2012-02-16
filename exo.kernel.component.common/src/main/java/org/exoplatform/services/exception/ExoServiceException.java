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
package org.exoplatform.services.exception;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author: Tuan Nguyen
 * @version: $Id: ExoServiceException.java 5332 2006-04-29 18:32:44Z geaz $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class ExoServiceException extends Exception
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.ExoServiceException");
   
   protected Object[] params_;

   protected String key_ = "SystemException";

   protected String keyDesc_ = "SystemExceptionDesc";

   public ExoServiceException()
   {
   }

   public ExoServiceException(Throwable ex)
   {
      super(ex.getMessage());
      LOG.error(ex.getLocalizedMessage(), ex);
   }

   public ExoServiceException(String s)
   {
      super(s);
   }

   public ExoServiceException(String key, Object[] params)
   {
      key_ = key;
      keyDesc_ = key + "Desc";
      params_ = params;
   }
}
