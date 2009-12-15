/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertiesLoader
{

   private static class LinkedProperties extends Properties
   {

      /** A list that contains each element at most once. */
      private LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();

      @Override
      public Object put(Object key, Object value)
      {
         if (list.containsKey(key))
         {
            list.remove(key);
         }
         list.put((String)key, (String)value);
         return super.put(key, value);
      }

      @Override
      public Object remove(Object key)
      {
         list.remove(key);
         return super.remove(key);
      }

   }

   public static LinkedHashMap<String, String> load(InputStream in) throws IOException
   {
      LinkedProperties props = new LinkedProperties();
      props.load(in);
      return props.list;
   }

   public static LinkedHashMap<String, String> loadFromXML(InputStream in) throws IOException
   {
      LinkedProperties props = new LinkedProperties();
      props.loadFromXML(in);
      return props.list;
   }
}
