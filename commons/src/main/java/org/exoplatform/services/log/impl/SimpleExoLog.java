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

import org.apache.commons.logging.impl.SimpleLog;
import org.exoplatform.services.log.Log;

/**
 * Based on apache commons logging {@link org.apache.commons.logging.impl.SimpleLog} class. It could be
 * implemented later to use the system output directly in the future if the dependency over commons
 * logging is removed.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Id: SimpleExoLog.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class SimpleExoLog implements Log
{

   /** . */
   private final SimpleLog delegate;

   public void setLevel(int i)
   {
      delegate.setLevel(i);
   }

   public int getLevel()
   {
      return delegate.getLevel();
   }

   public void debug(Object o)
   {
      delegate.debug(o);
   }

   public void debug(Object o, Throwable throwable)
   {
      delegate.debug(o, throwable);
   }

   public void trace(Object o)
   {
      delegate.trace(o);
   }

   public void trace(Object o, Throwable throwable)
   {
      delegate.trace(o, throwable);
   }

   public void info(Object o)
   {
      delegate.info(o);
   }

   public void info(Object o, Throwable throwable)
   {
      delegate.info(o, throwable);
   }

   public void warn(Object o)
   {
      delegate.warn(o);
   }

   public void warn(Object o, Throwable throwable)
   {
      delegate.warn(o, throwable);
   }

   public void error(Object o)
   {
      delegate.error(o);
   }

   public void error(Object o, Throwable throwable)
   {
      delegate.error(o, throwable);
   }

   public void fatal(Object o)
   {
      delegate.fatal(o);
   }

   public void fatal(Object o, Throwable throwable)
   {
      delegate.fatal(o, throwable);
   }

   public boolean isDebugEnabled()
   {
      return delegate.isDebugEnabled();
   }

   public boolean isErrorEnabled()
   {
      return delegate.isErrorEnabled();
   }

   public boolean isFatalEnabled()
   {
      return delegate.isFatalEnabled();
   }

   public boolean isInfoEnabled()
   {
      return delegate.isInfoEnabled();
   }

   public boolean isTraceEnabled()
   {
      return delegate.isTraceEnabled();
   }

   public boolean isWarnEnabled()
   {
      return delegate.isWarnEnabled();
   }

   public SimpleExoLog(String name)
   {
      this.delegate = new SimpleLog(name);
   }
}
