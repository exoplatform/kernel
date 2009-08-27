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

import org.exoplatform.management.jmx.annotations.NameTemplate;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * A builder for object name templates.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ObjectNameBuilder<T>
{

   /** . */
   private String domain;

   /** . */
   private Class<? extends T> clazz;

   /**
    * Create a new builder.
    *
    * @param clazz the class
    * @throws IllegalArgumentException if the object is null
    */
   public ObjectNameBuilder(String domain, Class<? extends T> clazz) throws IllegalArgumentException
   {
      if (clazz == null)
      {
         throw new IllegalArgumentException("Clazz cannot be null");
      }
      this.domain = domain;
      this.clazz = clazz;
   }

   /**
    * Build the object name or return null if the class is not annotated by
    * {@link NameTemplate}.
    *
    * @param object the object
    * @return the built name
    * @throws IllegalStateException raised by a build time issue 
    */
   public ObjectName build(T object) throws IllegalStateException
   {
      PropertiesInfo info = PropertiesInfo.resolve(clazz, NameTemplate.class);

      //
      if (info != null)
      {

         try
         {
            Map<String, String> props = info.resolve(object);
            return JMX.createObjectName(domain, props);
         }
         catch (MalformedObjectNameException e)
         {
            throw new IllegalArgumentException("ObjectName template is malformed", e);
         }
      }

      //
      return null;
   }
}
