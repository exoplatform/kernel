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
package org.exoplatform.management.spi;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Meta data that describes a managed type.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagedTypeMetaData extends ManagedMetaData
{

   /** . */
   private final Class type;

   /** . */
   private final Map<String, ManagedPropertyMetaData> properties;

   /** . */
   private final Map<MethodKey, ManagedMethodMetaData> methods;

   public ManagedTypeMetaData(Class type) throws IllegalArgumentException
   {
      if (type == null)
      {
         throw new IllegalArgumentException("The type cannot be null");
      }

      //
      this.type = type;
      this.properties = new HashMap<String, ManagedPropertyMetaData>();
      this.methods = new HashMap<MethodKey, ManagedMethodMetaData>();
   }

   public Class getType()
   {
      return type;
   }

   public ManagedPropertyMetaData getProperty(String propertyName)
   {
      return properties.get(propertyName);
   }

   public void addProperty(ManagedPropertyMetaData property)
   {
      properties.put(property.getName(), property);
   }

   public void addMethod(ManagedMethodMetaData method)
   {
      methods.put(new MethodKey(method.getMethod()), method);
   }

   public Collection<ManagedMethodMetaData> getMethods()
   {
      return methods.values();
   }

   public Collection<ManagedPropertyMetaData> getProperties()
   {
      return properties.values();
   }

   private static class MethodKey
   {

      private final String name;

      private final List<Class<?>> types;

      private MethodKey(Method method)
      {
         this.name = method.getName();
         this.types = Arrays.asList(method.getParameterTypes());
      }

      @Override
      public int hashCode()
      {
         int hashCode = name.hashCode();
         for (Class<?> type : types)
         {
            hashCode = hashCode * 41 + type.hashCode();
         }
         return hashCode;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (obj == this)
         {
            return true;
         }
         if (obj instanceof MethodKey)
         {
            MethodKey that = (MethodKey)obj;
            return this.name.equals(that.name) && this.types.equals(that.types);
         }
         return false;
      }
   }
}
