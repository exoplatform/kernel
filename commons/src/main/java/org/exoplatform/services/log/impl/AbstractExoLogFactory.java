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

import org.exoplatform.services.log.ExoLogFactory;
import org.exoplatform.services.log.Log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * An abstract logger factory that maintains a cache of name to logger instance.
 * The cache is based on the {@link ConcurrentHashMap} for better scalability.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Id: AbstractExoLogFactory.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public abstract class AbstractExoLogFactory implements ExoLogFactory
{

   /** . */
   private final ConcurrentMap<String, Log> loggers = new ConcurrentHashMap<String, Log>();

   /**
    * Obtain a specified logger.
    *
    * @param name the logger name
    * @return the logger
    */
   protected abstract Log getLogger(String name);

   /**
    * {@inheritDoc}
    */
   public final Log getExoLogger(String name)
   {
      if (name == null)
      {
         throw new NullPointerException();
      }
      Log exoLogger = loggers.get(name);
      if (exoLogger == null)
      {
         exoLogger = getLogger(name);
         Log phantom = loggers.putIfAbsent(name, exoLogger);
         if (phantom != null)
         {
            exoLogger = phantom;
         }
      }
      return exoLogger;
   }

   /**
    * {@inheritDoc}
    */
   public final Log getExoLogger(Class clazz)
   {
      if (clazz == null)
      {
         throw new NullPointerException();
      }
      return getExoLogger(clazz.getName());
   }
}