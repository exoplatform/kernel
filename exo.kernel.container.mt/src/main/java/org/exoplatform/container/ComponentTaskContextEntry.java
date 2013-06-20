/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ComponentTaskContextEntry
{

   /**
    * The key of the dependency
    */
   private final Object componentKey;
   
   /**
    * Indicates the type of the task for which the dependency is needed.
    */
   private final ComponentTaskType type;

   /**
    * Default constructor
    */
   public ComponentTaskContextEntry(Object componentKey, ComponentTaskType type)
   {
      this.componentKey = componentKey;
      this.type = type;
   }

   /**
    * @return the key of the dependency
    */
   public Object getComponentKey()
   {
      return componentKey;
   }

   /**
    * @return the type of the task for which this dependency is needed.
    */
   public ComponentTaskType getTaskType()
   {
      return type;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((componentKey == null) ? 0 : componentKey.hashCode());
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ComponentTaskContextEntry other = (ComponentTaskContextEntry)obj;
      if (componentKey == null)
      {
         if (other.componentKey != null)
            return false;
      }
      else if (!componentKey.equals(other.componentKey))
         return false;
      if (type != other.type)
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      return "ComponentTaskContextEntry [componentKey=" + componentKey + ", type=" + type + "]";
   }
}
