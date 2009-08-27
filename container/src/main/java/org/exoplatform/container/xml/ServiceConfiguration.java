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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ServiceConfiguration.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ServiceConfiguration extends HashMap
{
   private String key;

   private String type;

   public ServiceConfiguration()
   {
   }

   public String getServiceKey()
   {
      return key;
   }

   public void setServiceKey(String s)
   {
      key = s;
   }

   public String getServiceType()
   {
      return type;
   }

   public void setServiceType(String s)
   {
      type = s;
   }

   public ValuesParam getValuesParam(String name)
   {
      return (ValuesParam)get(name);
   }

   public ValueParam getValueParam(String name)
   {
      return (ValueParam)get(name);
   }

   public PropertiesParam getPropertiesParam(String name)
   {
      return (PropertiesParam)get(name);
   }

   public ObjectParam getObjectParam(String name)
   {
      return (ObjectParam)get(name);
   }

   public List getObjectParamValues(Class type)
   {
      List list = new ArrayList();
      Iterator i = values().iterator();
      while (i.hasNext())
      {
         Object o = i.next();
         if (o instanceof ObjectParam)
         {
            ObjectParam param = (ObjectParam)o;
            Object paramValue = param.getObject();
            if (type.isInstance(paramValue))
               list.add(paramValue);
         }
      }
      return list;
   }

   public Parameter getParameter(String name)
   {
      return (Parameter)get(name);
   }

   public void addParameter(Parameter param)
   {
      put(param.getName(), param);
   }

   public Parameter removeParameter(String name)
   {
      return (Parameter)remove(name);
   }

   // --------------xml binding---------------------------------
   public void addParam(Object o)
   {
      Parameter param = (Parameter)o;
      put(param.getName(), param);
   }

   public Iterator getValueParamIterator()
   {
      List list = new ArrayList();
      Iterator i = values().iterator();
      while (i.hasNext())
      {
         Object o = i.next();
         if (o instanceof ValueParam)
            list.add(o);
      }
      return list.iterator();
   }

   public Iterator getValuesParamIterator()
   {
      List list = new ArrayList();
      Iterator i = values().iterator();
      while (i.hasNext())
      {
         Object o = i.next();
         if (o instanceof ValuesParam)
            list.add(o);
      }
      return list.iterator();
   }

   public Iterator getPropertiesParamIterator()
   {
      List list = new ArrayList();
      Iterator i = values().iterator();
      while (i.hasNext())
      {
         Object o = i.next();
         if (o instanceof PropertiesParam)
            list.add(o);
      }
      return list.iterator();
   }

   public Iterator getObjectParamIterator()
   {
      List list = new ArrayList();
      Iterator i = values().iterator();
      while (i.hasNext())
      {
         Object o = i.next();
         if (o instanceof ObjectParam)
            list.add(o);
      }
      return list.iterator();
   }
}
