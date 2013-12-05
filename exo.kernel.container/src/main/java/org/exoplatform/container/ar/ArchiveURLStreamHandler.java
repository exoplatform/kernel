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
package org.exoplatform.container.ar;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A specific {@link URLStreamHandler} that knows how to parse and
 * open a connection to an {@link URL} using the archive protocol 
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
class ArchiveURLStreamHandler extends URLStreamHandler
{

   ArchiveURLStreamHandler()
   {
   }

   /**
    * @see java.net.URLStreamHandler#openConnection(java.net.URL)
    */
   @Override
   protected URLConnection openConnection(URL u) throws IOException
   {
      if (!u.getProtocol().equals(Archive.PROTOCOL))
         throw new MalformedURLException("Only the protocol " + Archive.PROTOCOL + " is supported");
      if (u.getHost() != null)
         throw new MalformedURLException("Only local access is supported, so no host is expected");
      String file = u.getFile();
      if (!file.startsWith("/"))
         throw new MalformedURLException("The path of an archive URL must start with '/'");
      if (file.contains("//"))
         throw new MalformedURLException("The path of an archive URL cannot contain '//'");
      int fromIndex = file.indexOf("!/");
      if (fromIndex != -1)
      {
         if (file.endsWith("!/"))
            throw new MalformedURLException("The path of an archive URL cannot end with the separator '!/'");
         if (((fromIndex = file.indexOf("!/", fromIndex + 2)) != -1)
            && ((fromIndex = file.indexOf("!/", fromIndex + 2)) != -1))
         {
            throw new MalformedURLException("Only 2 level of achive is supported which means that you "
               + "cannot use more than twice the separator '!/'");
         }
      }
      if (file.endsWith("/"))
         throw new MalformedURLException("The path of an archive URL cannot end with '/' as only files are supported");
      return new ArchiveURLConnection(u);
   }

   /**
    * @see java.net.URLStreamHandler#parseURL(java.net.URL, java.lang.String, int, int)
    */
   @Override
   protected void parseURL(URL u, String spec, int start, int limit)
   {
      setURL(u, Archive.PROTOCOL, null, -1, null, null, spec.substring(start, limit), null, null);
   }
}
