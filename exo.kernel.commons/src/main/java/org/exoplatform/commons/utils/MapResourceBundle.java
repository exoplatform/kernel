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

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com
 */
public class MapResourceBundle extends ResourceBundle implements Serializable
{

   private final static Pattern PATTERN = Pattern.compile("#\\{.*\\}");

   private Map<String, String> props;

   private Locale locale;

   public MapResourceBundle(Locale l)
   {
      this.locale = l;
      this.props = new HashMap<String, String>();
   }

   public MapResourceBundle(ResourceBundle rB, Locale l)
   {
      Map<String, String> props = new HashMap<String, String>();
      doMerge(props, rB);

      //
      this.locale = l;
      this.props = props;
   }

   private static void doMerge(Map<String, String> props, ResourceBundle rB)
   {
      Enumeration<String> e = rB.getKeys();
      while (e.hasMoreElements())
      {
         String key = e.nextElement();
         if (props.get(key) == null)
         {
            Object o = rB.getObject(key);
            if (o instanceof String)
            {
               String value = (String)o;
               props.put(key.intern(), value.intern());
            }
         }
      }
   }

   protected Object handleGetObject(String key)
   {
      return props.get(key);
   }

   public Enumeration<String> getKeys()
   {
      final Iterator<String> i = props.keySet().iterator();
      return new Enumeration<String>()
      {
         public boolean hasMoreElements()
         {
            return i.hasNext();
         }
         public String nextElement()
         {
            return i.next();
         }
      };
   }

   public Locale getLocale()
   {
      return this.locale;
   }

   public void add(String key, Object o)
   {
      if (key != null && o instanceof String)
      {
         String value = (String)o;
         props.put(key.intern(), value.intern());
      }
   }

   public void remove(String key)
   {
      if (key != null)
      {
         props.remove(key);
      }
   }

   public void merge(ResourceBundle bundle)
   {
      doMerge(props, bundle);
   }

   public void resolveDependencies()
   {
      Map<String, String> tempMap = new HashMap<String ,String>();
      for (String element : props.keySet())
      {
         String value = lookupKey(element);
         if (value != null)
         {
            tempMap.put(element.intern(), value.intern());
         }
      }
      props = tempMap;
   }

   private String lookupKey(String key)
   {
      String s = props.get(key);
      if (s == null)
      {
         return key;
      }

      //
      Matcher matcher = PATTERN.matcher(s);
      if (matcher.find())
      {
         return recursivedResolving(s);
      }
      return s;
   }

   private String recursivedResolving(String key)
   {
      String resolved = key;
      StringBuilder sB = new StringBuilder();
      while (resolved.indexOf("#{") != -1)
      {
         sB.setLength(0);
         int firstIndex = resolved.indexOf('#');
         int lastIndex = resolved.indexOf('}', firstIndex);
         String realKey = resolved.substring(firstIndex + 2, lastIndex);
         sB.append(resolved.substring(0, firstIndex));
         sB.append(lookupKey(realKey));
         sB.append(resolved.substring(lastIndex + 1));
         resolved = sB.toString();
      }
      return resolved;
   }
}
