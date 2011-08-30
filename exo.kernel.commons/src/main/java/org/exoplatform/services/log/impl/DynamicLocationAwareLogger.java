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
package org.exoplatform.services.log.impl;

import org.slf4j.Marker;
import org.slf4j.spi.LocationAwareLogger;

import java.lang.reflect.Method;

/**
 * This is an utility class allowing to use in runtime either 1.5.x or
 * 1.6.x slf4j libraries. As their log method signature differs, 
 * we use reflection library to invoke it.
 * 
 * @author <a href="mailto:dkuleshov@exoplatform.com">Dmitry Kuleshov</a>
 */
public class DynamicLocationAwareLogger
{
   /**
    * To keep log method instance
    */
   private Method log;

   /**
    * Message to show if log method invocation has failed for some reasons.
    */
   private final static String LOG_METHOD_INVOKE_ERROR_MSG =
      "LocationAwareLogger had some issues on method 'log' invocation. Using location unaware methods.\n";

   /**
    * To trace if new parameter is supported
    * 1.5.x slf4j lib does not support
    * 1.6.x slf4j lib supports
    */
   private boolean parameterSupported = false;

   /**
    * Logger
    */
   private LocationAwareLogger logger;

   /**
    * Simple constructor with one parameter is used to pull out log {@link Method}
    * an determine which version of slf4j library is currently used.
    * 
    * @param logger location aware logger to be wrapped
    */
   public DynamicLocationAwareLogger(LocationAwareLogger logger)
   {
      this.logger = logger;

      // here we're going to retrieve 'log' method instance from logger's class
      // using java reflection library 
      // also we're determining number of parameters of 'log' method to know
      // what slf4j library version we're dealing with
      for (Method m : logger.getClass().getDeclaredMethods())
      {
         if ("log".equals(m.getName()))
         {
            log = m;
            if (log.getParameterTypes().length == 6)
            {
               parameterSupported = true;
            }
            break;
         }
      }

      // if no method named 'log' is determined for currently used logger class
      // we throw an exception to warn that something is definitely going wrong
      if (log == null)
      {
         throw new UnsupportedOperationException("Currently used logger does not have log method.");
      }
   }

   /**
    * Printing method with support for location information. 
    * Encapsulates slf4j lib log {@link Method} invocation and passing it the correct parameters.
    */
   public void log(Marker marker, String fqcn, int level, String message, Throwable t)
   {
      try
      {
         if (!parameterSupported)
         {
            log.invoke(logger, marker, fqcn, level, message, t);
         }
         else
         {
            log.invoke(logger, marker, fqcn, level, message, null, t);
         }
      }
      catch (Exception e)
      {
         switch( level)
         {
            case LocationAwareLogger.TRACE_INT :
               logger.trace(LOG_METHOD_INVOKE_ERROR_MSG + message, t);
               break;
            case LocationAwareLogger.DEBUG_INT :
               logger.debug(LOG_METHOD_INVOKE_ERROR_MSG + message, t);
               break;
            case LocationAwareLogger.INFO_INT :
               logger.info(LOG_METHOD_INVOKE_ERROR_MSG + message, t);
               break;
            case LocationAwareLogger.WARN_INT :
               logger.warn(LOG_METHOD_INVOKE_ERROR_MSG + message, t);
               break;
            case LocationAwareLogger.ERROR_INT :
               logger.error(LOG_METHOD_INVOKE_ERROR_MSG + message, t);
               break;
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDebugEnabled()
   {
      return logger.isDebugEnabled();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isErrorEnabled()
   {
      return logger.isErrorEnabled();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isFatalEnabled()
   {
      return logger.isErrorEnabled();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isInfoEnabled()
   {
      return logger.isInfoEnabled();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isTraceEnabled()
   {
      return logger.isTraceEnabled();
   }

   /**
    * {@inheritDoc}
    */
   public boolean isWarnEnabled()
   {
      return logger.isWarnEnabled();
   }
}