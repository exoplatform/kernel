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
package org.exoplatform.container.xml;

import org.exoplatform.commons.utils.ExoProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: PropertiesParam.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class PropertiesParam extends Parameter
{
   private ExoProperties properties = new ExoProperties();

   public ExoProperties getProperties()
   {
      return properties;
   }

   public String getProperty(String name)
   {
      return properties.getProperty(name);
   }

   public void setProperty(String name, String value)
   {
      properties.setProperty(name, value);
   }

   public void addProperty(Object value)
   {
      Property property = (Property)value;
      properties.put(property.getName(), property.getValue());
   }

   public Iterator<Property> getPropertyIterator()
   {
      List<Property> list = new ArrayList<Property>();
      Iterator<Map.Entry<String, String>> i = properties.entrySet().iterator();
      while (i.hasNext())
      {
         Map.Entry<String, String> entry = (Map.Entry<String, String>)i.next();
         list.add(new Property((String)entry.getKey(), (String)entry.getValue()));
      }
      return list.iterator();
   }

   public String toString()
   {
      return properties.toString();
   }
}
