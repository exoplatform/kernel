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
package org.exoplatform.container.util;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class Utils
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.Utils");//NOSONAR   
   
   /**
    * Prevents instantiation
    */
   private Utils(){}
   
   /**
    * Loads the given stream into memory, returns its content as String and
    * finally close the stream.
    * @throws IOException if the stream could not be loaded
    */
   public static String readStream(InputStream inputStream) throws IOException
   {
      try
      {
         StringBuilder out = new StringBuilder(512);
         byte[] b = new byte[4096];
         for (int n; (n = inputStream.read(b)) != -1;)
         {
            out.append(new String(b, 0, n));
         }
         return out.toString();
      }
      finally
      {
         try
         {
            inputStream.close();
         }
         catch (IOException e)
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("Cannot close stream: " + e.getMessage());
            }
         }
      }
   }
}
