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
package org.exoplatform.container.jmx;

import org.exoplatform.commons.reflect.AnnotationIntrospector;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.NamingContext;
import org.exoplatform.management.jmx.annotations.Property;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertiesInfo
{

   /** . */
   private Map<String, PropertyInfo> properties;

   public PropertiesInfo(Map<String, PropertyInfo> properties)
   {
      this.properties = properties;
   }

   public static PropertiesInfo resolve(Class clazz, Class<? extends Annotation> annotationClass)
   {
      Annotation tpl2 = AnnotationIntrospector.resolveClassAnnotations(clazz, annotationClass);
      Property[] blah = null;
      if (tpl2 instanceof NamingContext)
      {
         blah = ((NamingContext)tpl2).value();
      }
      else if (tpl2 instanceof NameTemplate)
      {
         blah = ((NameTemplate)tpl2).value();
      }
      if (blah != null)
      {
         Map<String, PropertyInfo> properties = new HashMap<String, PropertyInfo>();
         for (Property property : blah)
         {
            PropertyInfo propertyInfo = new PropertyInfo(clazz, property);
            properties.put(propertyInfo.getKey(), propertyInfo);
         }
         return new PropertiesInfo(properties);
      }
      else
      {
         return null;
      }
   }

   public Collection<PropertyInfo> getProperties()
   {
      return properties.values();
   }

   public Map<String, String> resolve(Object instance)
   {
      Map<String, String> props = new HashMap<String, String>();
      for (PropertyInfo propertyInfo : properties.values())
      {
         String key = propertyInfo.getKey();
         String value = propertyInfo.resolveValue(instance);
         props.put(key, value);
      }
      return props;
   }
}
