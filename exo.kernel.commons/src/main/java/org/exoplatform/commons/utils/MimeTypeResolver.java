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
package org.exoplatform.commons.utils;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;
import java.util.Properties;

public class MimeTypeResolver
{

   private Properties mimeTypes = new Properties();

   private String defaultMimeType = "application/octet-stream";

   public MimeTypeResolver()
   {
      try
      {
         SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<Void>()
         {
            public Void run() throws Exception
            {
               mimeTypes.load(getClass().getResourceAsStream("mimetypes.properties"));
               return null;
            }
         });
      }
      catch (IOException e)
      {
         throw new InternalError("Unable to load mimetypes: " + e.toString());
      }
   }

   public String getDefaultMimeType()
   {
      return defaultMimeType;
   }

   public void setDefaultMimeType(String defaultMimeType)
   {
      this.defaultMimeType = defaultMimeType;
   }

   public String getMimeType(String filename)
   {
      String ext = filename.substring(filename.lastIndexOf(".") + 1);
      if (ext.equals(""))
      {
         ext = filename;
      }
      return mimeTypes.getProperty(ext.toLowerCase(), defaultMimeType);
   }

   public String getExtension(String mimeType)
   {
      if (mimeType.equals("") || mimeType.equals(defaultMimeType))
         return "";
      Iterator iterator = mimeTypes.keySet().iterator();
      // if true than this flag define multiple mimetypes for different extensions
      // exists
      String ext = "";
      while (iterator.hasNext())
      {
         String key = (String)iterator.next();
         String value = (String)mimeTypes.get(key);
         if (value.equals(mimeType) && mimeType.endsWith(key))
            return key;
         if (value.equals(mimeType) && ext.equals(""))
            ext = new String(key);
         else if (value.equals(mimeType) && (!ext.equals("")))
            return ext;
      }
      return ext;
   }
}
