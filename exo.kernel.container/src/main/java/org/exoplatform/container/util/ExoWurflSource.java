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
package org.exoplatform.container.util;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;

//import net.sourceforge.wurfl.wurflapi.WurflSource;

public class ExoWurflSource /* implements WurflSource */
{
   
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.util.ExoWurflSource");

   public InputStream getWurflInputStream()
   {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      java.net.URL wurflUrl = cl.getResource("conf/wurfl.xml");
      try
      {
         return wurflUrl.openStream();
      }
      catch (IOException e)
      {
         LOG.error(e.getLocalizedMessage(), e);
         return null;
      }
   }

   public InputStream getWurflPatchInputStream()
   {
      return null;
   }
}
