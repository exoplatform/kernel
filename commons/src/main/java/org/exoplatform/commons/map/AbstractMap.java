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
package org.exoplatform.commons.map;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author Ove Ranheim (oranheim@users.sourceforge.net)
 * @since Nov 6, 2003 5:58:08 PM
 */
public abstract class AbstractMap implements Map
{
   // Generic Attributes

   protected abstract Object getAttribute(String name);

   protected abstract void setAttribute(String name, Object value);

   protected abstract void removeAttribute(String name);

   protected abstract Enumeration getAttributeNames();

   // Bridge methods

   public int size()
   {
      int n = 0;
      Enumeration keys = getAttributeNames();
      while (keys.hasMoreElements())
      {
         String key = (String)keys.nextElement();
         n++;
      }
      return n;
   }

   public boolean isEmpty()
   {
      return !getAttributeNames().hasMoreElements();
   }

   public boolean containsKey(Object key)
   {
      return (getAttribute((String)key) != null);
   }

   public boolean containsValue(Object value)
   {
      boolean match = false;
      Enumeration keys = getAttributeNames();
      while (keys.hasMoreElements())
      {
         String key = (String)keys.nextElement();
         Object val = getAttribute(key);
         if (value.equals(val))
         {
            match = true;
            break;
         }
      }
      return match;
   }

   public Object get(Object key)
   {
      return getAttribute((String)key);
   }

   public Object put(Object key, Object value)
   {
      Object result = null;
      if (containsKey(key))
         result = getAttribute((String)key);
      setAttribute((String)key, value);
      return result;
   }

   public Object remove(Object key)
   {
      Object result = getAttribute((String)key);
      removeAttribute((String)key);
      return result;
   }

   public void putAll(Map t)
   {
      for (Iterator i = t.keySet().iterator(); i.hasNext();)
      {
         String key = (String)i.next();
         Object value = t.get(key);
         put(key, value);
      }
   }

   public void clear()
   {
      throw new UnsupportedOperationException();
   }

   public Set keySet()
   {
      throw new UnsupportedOperationException();
   }

   public Collection values()
   {
      throw new UnsupportedOperationException();
   }

   public Set entrySet()
   {
      throw new UnsupportedOperationException();
   }

   public String toString()
   {
      StringBuffer b = new StringBuffer();
      Enumeration keys = getAttributeNames();
      while (keys.hasMoreElements())
      {
         String key = (String)keys.nextElement();
         b.append(key).append("\n");
      }
      return b.toString();
   }
}
