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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.slf4j.spi.LocationAwareLogger;

/**
 * An implementation of {@link ExoLogger} that delegates to an instance of {@link LocationAwareLogger}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Id: LocationAwareSLF4JExoLog.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class LocationAwareSLF4JExoLog implements Log
{
   /** . */
   private static final String FQCN = LocationAwareSLF4JExoLog.class.getName();

   /** . */
   private final DynamicLocationAwareLogger logger;

   /**
    * Create a new instance.
    *
    * @param logger the logger
    * @throws IllegalArgumentException if the logger is null
    */
   public LocationAwareSLF4JExoLog(LocationAwareLogger logger)
   {
      if (logger == null)
      {
         throw new IllegalArgumentException();
      }
      this.logger = new DynamicLocationAwareLogger(logger);
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

   /**
    * {@inheritDoc}
    */
   public void trace(Object o)
   {
      logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, String.valueOf(o), null);
   }

   /**
    * {@inheritDoc}
    */
   public void trace(Object o, Throwable throwable)
   {
      logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void debug(Object o)
   {
      logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, String.valueOf(o), null);
   }

   /**
    * {@inheritDoc}
    */
   public void debug(Object o, Throwable throwable)
   {
      logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void info(Object o)
   {
      logger.log(null, FQCN, LocationAwareLogger.INFO_INT, String.valueOf(o), null);
   }

   /**
    * {@inheritDoc}
    */
   public void info(Object o, Throwable throwable)
   {
      logger.log(null, FQCN, LocationAwareLogger.INFO_INT, String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void warn(Object o)
   {
      logger.log(null, FQCN, LocationAwareLogger.WARN_INT, String.valueOf(o), null);
   }

   /**
    * {@inheritDoc}
    */
   public void warn(Object o, Throwable throwable)
   {
      logger.log(null, FQCN, LocationAwareLogger.WARN_INT, String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void error(Object o)
   {
      logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, String.valueOf(o), null);
   }

   /**
    * {@inheritDoc}
    */
   public void error(Object o, Throwable throwable)
   {
      logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void fatal(Object o)
   {
      logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, String.valueOf(o), null);
   }

   /**
    * {@inheritDoc}
    */
   public void fatal(Object o, Throwable throwable)
   {
      logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void trace(String format, Object... argsArray)
   {
      logger.log(null, FQCN, LocationAwareLogger.TRACE_INT, LogMessageFormatter.getMessage(format, argsArray),
         LogMessageFormatter.getThrowable(argsArray));
   }

   /**
    * {@inheritDoc}
    */
   public void debug(String format, Object... argsArray)
   {
      logger.log(null, FQCN, LocationAwareLogger.DEBUG_INT, LogMessageFormatter.getMessage(format, argsArray),
         LogMessageFormatter.getThrowable(argsArray));
   }

   /**
    * {@inheritDoc}
    */
   public void info(String format, Object... argsArray)
   {
      logger.log(null, FQCN, LocationAwareLogger.INFO_INT, LogMessageFormatter.getMessage(format, argsArray),
         LogMessageFormatter.getThrowable(argsArray));
   }

   /**
    * {@inheritDoc}
    */
   public void warn(String format, Object... argsArray)
   {
      logger.log(null, FQCN, LocationAwareLogger.WARN_INT, LogMessageFormatter.getMessage(format, argsArray),
         LogMessageFormatter.getThrowable(argsArray));
   }

   /**
    * {@inheritDoc}
    */
   public void error(String format, Object... argsArray)
   {
      logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, LogMessageFormatter.getMessage(format, argsArray),
         LogMessageFormatter.getThrowable(argsArray));
   }

   /**
    * {@inheritDoc}
    */
   public void fatal(String format, Object... argsArray)
   {
      logger.log(null, FQCN, LocationAwareLogger.ERROR_INT, LogMessageFormatter.getMessage(format, argsArray),
         LogMessageFormatter.getThrowable(argsArray));
   }
}
