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

import org.apache.commons.beanutils.PropertyUtils;
import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @version: $Id: ObjectParam.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ObjectParam extends Parameter
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.xml.ObjectParam");
   
   private String type;

   private String package_;

   private Object object_;

   private List<Property> properties_ = new ArrayList<Property>();

   public String getType()
   {
      return type;
   }

   public void setType(String s)
   {
      type = s;
      int idx = type.lastIndexOf(".");
      if (idx > 0)
      {
         package_ = type.substring(0, idx);
      }
   }

   public Object getObject()
   {
      if (object_ == null)
      {
         populateBean();
      }
      return object_;
   }

   public void addProperty(String name, String value)
   {
      properties_.add(new Property(name, value));
   }

   private void populateBean()
   {
      Property prop = null;
      try
      {
         Class<?> clazz = ClassLoading.forName(type, this);
         object_ = clazz.newInstance();
         for (int i = 0; i < properties_.size(); i++)
         {
            prop = properties_.get(i);
            if (prop.name.endsWith("]"))
            {
               // arrary or list
               populateBeanInArray(object_, prop.name, prop.value);
            }
            else
            {
               Object valueBean = getValue(prop.value);
               PropertyUtils.setProperty(object_, prop.name, valueBean);
            }
         }
      }
      catch (Exception ex)
      {
         LOG.error(ex.getLocalizedMessage(), ex);
      }
   }

   private void populateBeanInArray(Object bean, String name, String value) throws Exception
   {
      int idx = name.lastIndexOf("[");
      String arrayBeanName = name.substring(0, idx);
      int index = Integer.parseInt(name.substring(idx + 1, name.length() - 1));
      Object arrayBean = PropertyUtils.getProperty(bean, arrayBeanName);
      if (arrayBean instanceof List)
      {
         @SuppressWarnings("unchecked")
         List<Object> list = (List<Object>)arrayBean;
         Object valueBean = getValue(value);
         if (list.size() == index)
         {
            list.add(valueBean);
         }
         else
         {
            list.set(index, valueBean);
         }
      }
      else if (arrayBean instanceof Collection)
      {
         @SuppressWarnings("unchecked")
         Collection<Object> c = (Collection<Object>)arrayBean;
         Object valueBean = getValue(value);
         c.add(valueBean);
      }
      else
      {
         Object[] array = (Object[])arrayBean;
         array[index] = getValue(value);
      }
   }

   private Object getValue(String value) throws Exception
   {
      if (value.startsWith("#new"))
      {
         String[] temp = value.split(" ");
         String className = temp[1];

         if (className.indexOf(".") < 0)
         {
            StringBuilder fullName = new StringBuilder();
            fullName.append(package_);
            fullName.append(".");
            fullName.append(className);

            Class<?> clazz = ClassLoading.forName(fullName.toString(), this);
            return clazz.newInstance();
         }
      }
      else if (value.startsWith("#int"))
      {
         String[] temp = value.split(" ");
         value = temp[1].trim();
         return new Integer(value);
      }
      else if (value.startsWith("#long"))
      {
         String[] temp = value.split(" ");
         value = temp[1].trim();
         return new Long(value);
      }
      else if (value.startsWith("#boolean"))
      {
         String[] temp = value.split(" ");
         value = temp[1].trim();
         return new Boolean("true".equals(value));
      }
      return value;
   }

   public void addProperty(Object value)
   {
      properties_.add((Property)value);
   }

   public Iterator<Property> getPropertyIterator()
   {
      return properties_.iterator();
   }
}
