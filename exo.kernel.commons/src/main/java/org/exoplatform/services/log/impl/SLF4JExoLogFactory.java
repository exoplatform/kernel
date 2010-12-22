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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LocationAwareLogger;

import java.security.PrivilegedAction;

/**
 * A factory for {@link org.exoplatform.services.log.impl.LocationAwareSLF4JExoLog} and
 * {@link org.exoplatform.services.log.impl.SLF4JExoLog} based on the type of the logger
 * returned by {@link org.slf4j.LoggerFactory} which can be {@link Logger} or {@link org.slf4j.spi.LocationAwareLogger}.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Id: SLF4JExoLogFactory.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class SLF4JExoLogFactory extends AbstractExoLogFactory
{

   /**
    * {@inheritDoc}
    */
   @Override
   protected Log getLogger(final String name)
   {
      Logger slf4jlogger = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Logger>()
      {
         public Logger run()
         {
            return LoggerFactory.getLogger(name);
         }
      });

      if (slf4jlogger instanceof LocationAwareLogger)
      {
         return new LocationAwareSLF4JExoLog((LocationAwareLogger)slf4jlogger);
      }
      else
      {
         return new SLF4JExoLog(slf4jlogger);
      }
   }
}
