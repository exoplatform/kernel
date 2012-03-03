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
package org.exoplatform.xml.object;

import org.exoplatform.commons.utils.Tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 11, 2005
 * @version $Id: XMLMap.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class XMLMap
{
   private ArrayList listmap = new ArrayList();

   private String type_;

   public XMLMap()
   {

   }

   public XMLMap(Map map) throws Exception
   {
      Iterator i = map.entrySet().iterator();
      while (i.hasNext())
      {
         Map.Entry entry = (Map.Entry)i.next();
         Object key = entry.getKey();
         Object value = entry.getValue();
         // S ystem.out.println("key: " + key + ", value: " + value) ;
         if (key == null || value == null)
         {
            throw new RuntimeException("key: " + key + ", value: " + value + " cannot be null");
         }
         listmap.add(new XMLEntry(key, value));
      }
      type_ = map.getClass().getName();
   }

   public String getType()
   {
      return type_;
   }

   public void setType(String s)
   {
      type_ = s;
   }

   public Iterator getEntryIterator()
   {
      return listmap.iterator();
   }

   public Map getMap() throws Exception
   {
      Class<?> clazz = Tools.forName(type_, this);
      Map map = (Map)clazz.newInstance();
      for (int i = 0; i < listmap.size(); i++)
      {
         XMLEntry entry = (XMLEntry)listmap.get(i);
         XMLBaseObject key = entry.getKey();
         XMLBaseObject value = entry.getValue();
         map.put(key.getObjectValue(), value.getObjectValue());
      }
      return map;
   }
}
