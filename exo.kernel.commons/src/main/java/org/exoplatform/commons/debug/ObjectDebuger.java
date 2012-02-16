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
package org.exoplatform.commons.debug;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Jul 29, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ObjectDebuger.java,v 1.4 2004/09/21 00:04:43 tuan08 Exp $
 */
public class ObjectDebuger
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.commons.ObjectDebuger");

   static public void printObject(Object o) throws Exception
   {
      // System. out.println(asString(o)) ;
   }

   static public String asString(Object o)
   {
      StringBuffer b = new StringBuffer();
      try
      {
         Map map = new HashMap(100);
         if (o instanceof Collection)
            printCollection(map, (Collection)o, b, "");
         else if (o instanceof Map)
            printCollection(map, ((Map)o).values(), b, "");
         else
            printObject(map, o, b, "");
      }
      catch (Exception ex)
      {
         LOG.error(ex.getLocalizedMessage(), ex);
         b.append("\n").append(ex.getMessage());
      }
      return b.toString();
   }

   static private void printObject(Map printedObjects, Object o, StringBuffer b, String indent) throws Exception
   {
      if (o == null)
         return;
      if (printedObjects.containsKey(o))
         return;
      printedObjects.put(o, o);
      Class clazz = o.getClass();
      Field[] fields = clazz.getDeclaredFields();
      b.append(indent).append("object[" + getClassName(clazz)).append("]: ").append(o).append("\n");
      indent = indent + "  ";
      for (int i = 0; i < fields.length; i++)
      {
         if (fields[i].getDeclaringClass().getName().startsWith("java"))
            continue;
         Class type = fields[i].getType();
         fields[i].setAccessible(true);
         if (type.equals(String.class))
         {
            String s = (String)fields[i].get(o);
            if (s == null)
               s = "";
            if (s.length() > 50)
               s = s.substring(0, 50) + "...\n";
            b.append(indent).append(fields[i].getName()).append(": ").append(s).append("\n");
         }
         else if (type.equals(Boolean.class) || type.equals(Boolean.TYPE) || type.equals(Integer.class)
            || type.equals(Integer.TYPE) || type.equals(Long.class) || type.equals(Long.TYPE)
            || type.equals(Float.class) || type.equals(Float.TYPE) || type.equals(Double.class)
            || type.equals(Double.TYPE))
         {
            Object value = fields[i].get(o);
            b.append(indent).append(fields[i].getName()).append(": ").append(value).append("\n");
         }
         else
         {
            Object value = fields[i].get(o);
            if (value instanceof Collection)
            {
               b.append(indent).append(fields[i].getName()).append("[Collection]\n");
               printCollection(printedObjects, (Collection)value, b, indent + "  ");
            }
            else if (value instanceof Map)
            {
               b.append(indent).append(fields[i].getName()).append("[Map]\n");
               printMap((Map)value, b, indent + "  ");
            }
            else
            {
               printObject(printedObjects, value, b, indent);
            }
         }
      }
   }

   static private void printMap(Map map, StringBuffer b, String indent) throws Exception
   {
      Iterator i = map.entrySet().iterator();
      while (i.hasNext())
      {
         Map.Entry entry = (Map.Entry)i.next();
         b.append(indent).append(entry.getKey()).append(":").append(entry.getValue()).append("\n");
      }
   }

   static private void printCollection(Map printedObjects, Collection c, StringBuffer b, String indent)
      throws Exception
   {
      Iterator i = c.iterator();
      while (i.hasNext())
      {
         Object o = i.next();
         printObject(printedObjects, o, b, indent);
      }
   }

   static private String getClassName(Class clazz)
   {
      String name = clazz.getName();
      int idx = name.lastIndexOf(".");
      if (idx > 0)
         name = name.substring(idx + 1, name.length());
      return name;
   }
}
