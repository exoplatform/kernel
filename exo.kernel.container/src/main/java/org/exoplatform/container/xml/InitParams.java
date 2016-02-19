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
 * @version: $Id: InitParams.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class InitParams extends HashMap<String, Parameter>
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 5377748844130285300L;

   public InitParams()
   {
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

   public ObjectParameter getObjectParam(String name)
   {
      return (ObjectParameter)get(name);
   }

   public <T> List<T> getObjectParamValues(Class<T> type)
   {
      List<T> list = new ArrayList<T>();
      for (Object o : values())
      {
         if (o instanceof ObjectParameter)
         {
            ObjectParameter param = (ObjectParameter)o;
            Object paramValue = param.getObject();
            if (type.isInstance(paramValue))
            {
               T t = type.cast(paramValue);
               list.add(t);
            }
         }
      }
      return list;
   }

   public Parameter getParameter(String name)
   {
      return get(name);
   }

   public void addParameter(Parameter param)
   {
      put(param.getName(), param);
   }

   public Parameter removeParameter(String name)
   {
      return remove(name);
   }

   // --------------xml binding---------------------------------

   public void addParam(Object o)
   {
      Parameter param = (Parameter)o;
      put(param.getName(), param);
   }

   public Iterator<ValueParam> getValueParamIterator()
   {
      return getValueIterator(ValueParam.class);
   }

   public Iterator<ValuesParam> getValuesParamIterator()
   {
      return getValueIterator(ValuesParam.class);
   }

   public Iterator<PropertiesParam> getPropertiesParamIterator()
   {
      return getValueIterator(PropertiesParam.class);
   }

   public Iterator<ObjectParameter> getObjectParamIterator()
   {
      return getValueIterator(ObjectParameter.class);
   }

   private <T> Iterator<T> getValueIterator(Class<T> type)
   {
      List<T> list = new ArrayList<T>();
      for (Object o : values())
      {
         if (type.isInstance(o))
         {
            T t = type.cast(o);
            list.add(t);
         }
      }
      return list.iterator();
   }
}
