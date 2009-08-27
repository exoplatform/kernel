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
package org.exoplatform.services.log;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="work.visor.ck@gmail.com">Dmytro Katayev</a> Jun 26, 2009
 * @version $Id: Log.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public interface Log
{

   /**
    * <p>
    * Is debug logging currently enabled?
    * </p>
    * <p>
    * Call this method to prevent having to perform expensive operations (for
    * example, <code>String</code> concatenation) when the log level is more than
    * debug.
    * </p>
    * 
    * @return true if debug is enabled in the underlying logger.
    */
   boolean isDebugEnabled();

   /**
    * <p>
    * Is error logging currently enabled?
    * </p>
    * <p>
    * Call this method to prevent having to perform expensive operations (for
    * example, <code>String</code> concatenation) when the log level is more than
    * error.
    * </p>
    * 
    * @return true if error is enabled in the underlying logger.
    */
   boolean isErrorEnabled();

   /**
    * <p>
    * Is fatal logging currently enabled?
    * </p>
    * <p>
    * Call this method to prevent having to perform expensive operations (for
    * example, <code>String</code> concatenation) when the log level is more than
    * fatal.
    * </p>
    * 
    * @return true if fatal is enabled in the underlying logger.
    */
   boolean isFatalEnabled();

   /**
    * <p>
    * Is info logging currently enabled?
    * </p>
    * <p>
    * Call this method to prevent having to perform expensive operations (for
    * example, <code>String</code> concatenation) when the log level is more than
    * info.
    * </p>
    * 
    * @return true if info is enabled in the underlying logger.
    */
   boolean isInfoEnabled();

   /**
    * <p>
    * Is trace logging currently enabled?
    * </p>
    * <p>
    * Call this method to prevent having to perform expensive operations (for
    * example, <code>String</code> concatenation) when the log level is more than
    * trace.
    * </p>
    * 
    * @return true if trace is enabled in the underlying logger.
    */
   boolean isTraceEnabled();

   /**
    * <p>
    * Is warn logging currently enabled?
    * </p>
    * <p>
    * Call this method to prevent having to perform expensive operations (for
    * example, <code>String</code> concatenation) when the log level is more than
    * warn.
    * </p>
    * 
    * @return true if warn is enabled in the underlying logger.
    */
   boolean isWarnEnabled();

   // -------------------------------------------------------- Logging Methods

   /**
    * <p>
    * Log a message with trace log level.
    * </p>
    * 
    * @param message log this message
    */
   void trace(Object message);

   /**
    * <p>
    * Log an error with trace log level.
    * </p>
    * 
    * @param message log this message
    * @param t log this cause
    */
   void trace(Object message, Throwable t);

   /**
    * <p>
    * Log a message with debug log level.
    * </p>
    * 
    * @param message log this message
    */
   void debug(Object message);

   /**
    * <p>
    * Log an error with debug log level.
    * </p>
    * 
    * @param message log this message
    * @param t log this cause
    */
   void debug(Object message, Throwable t);

   /**
    * <p>
    * Log a message with info log level.
    * </p>
    * 
    * @param message log this message
    */
   void info(Object message);

   /**
    * <p>
    * Log an error with info log level.
    * </p>
    * 
    * @param message log this message
    * @param t log this cause
    */
   void info(Object message, Throwable t);

   /**
    * <p>
    * Log a message with warn log level.
    * </p>
    * 
    * @param message log this message
    */
   void warn(Object message);

   /**
    * <p>
    * Log an error with warn log level.
    * </p>
    * 
    * @param message log this message
    * @param t log this cause
    */
   void warn(Object message, Throwable t);

   /**
    * <p>
    * Log a message with error log level.
    * </p>
    * 
    * @param message log this message
    */
   void error(Object message);

   /**
    * <p>
    * Log an error with error log level.
    * </p>
    * 
    * @param message log this message
    * @param t log this cause
    */
   void error(Object message, Throwable t);

   /**
    * <p>
    * Log a message with fatal log level.
    * </p>
    * 
    * @param message log this message
    */
   void fatal(Object message);

   /**
    * <p>
    * Log an error with fatal log level.
    * </p>
    * 
    * @param message log this message
    * @param t log this cause
    */
   void fatal(Object message, Throwable t);

}
