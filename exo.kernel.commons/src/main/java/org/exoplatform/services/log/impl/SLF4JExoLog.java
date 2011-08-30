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

import org.exoplatform.services.log.Log;
import org.slf4j.Logger;

/**
 * An implementation of {@link Log} that delegates to an instance of {@link Logger}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Id: SLF4JExoLog.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class SLF4JExoLog implements Log
{

   /** SLF4J logger. */
   private Logger logger;

   /**
    * Create a new instance.
    *
    * @param logger Logger
    * @throws NullPointerException if the logger is null
    */
   public SLF4JExoLog(Logger logger) throws NullPointerException
   {
      if (logger == null)
         throw new NullPointerException();

      this.logger = logger;
   }

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
      logger.trace(String.valueOf(o));
   }

   /**
    * {@inheritDoc}
    */
   public void trace(Object o, Throwable throwable)
   {
      logger.trace(String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void debug(Object o)
   {
      logger.debug(String.valueOf(o));
   }

   /**
    * {@inheritDoc}
    */
   public void debug(Object o, Throwable throwable)
   {
      logger.debug(String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void info(Object o)
   {
      logger.info(String.valueOf(o));
   }

   /**
    * {@inheritDoc}
    */
   public void info(Object o, Throwable throwable)
   {
      logger.info(String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void warn(Object o)
   {
      logger.warn(String.valueOf(o));
   }

   /**
    * {@inheritDoc}
    */
   public void warn(Object o, Throwable throwable)
   {
      logger.warn(String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void error(Object o)
   {
      logger.error(String.valueOf(o));
   }

   /**
    * {@inheritDoc}
    */
   public void error(Object o, Throwable throwable)
   {
      logger.error(String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void fatal(Object o)
   {
      logger.error(String.valueOf(o));
   }

   /**
    * {@inheritDoc}
    */
   public void fatal(Object o, Throwable throwable)
   {
      logger.error(String.valueOf(o), throwable);
   }

   /**
    * {@inheritDoc}
    */
   public void trace(String format, Object... argsArray)
   {
      logger.trace(format, argsArray);
   }

   /**
    * {@inheritDoc}
    */
   public void debug(String format, Object... argsArray)
   {
      logger.debug(format, argsArray);
   }

   /**
    * {@inheritDoc}
    */
   public void info(String format, Object... argsArray)
   {
      logger.info(format, argsArray);
   }

   /**
    * {@inheritDoc}
    */
   public void warn(String format, Object... argsArray)
   {
      logger.warn(format, argsArray);
   }

   /**
    * {@inheritDoc}
    */
   public void error(String format, Object... argsArray)
   {
      logger.error(format, argsArray);
   }

   /**
    * {@inheritDoc}
    */
   public void fatal(String format, Object... argsArray)
   {
      logger.error(format, argsArray);
   }
}
