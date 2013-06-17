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

import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;

import java.io.Serializable;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractComponentAdapter implements ComponentAdapter, Serializable
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -8879158955898247836L;

   private Object componentKey;

   private Class<?> componentImplementation;

   /**
    * Constructs a new ComponentAdapter for the given key and implementation. 
    * @param componentKey the search key for this implementation
    * @param componentImplementation the concrete implementation
    * @throws ContainerException if the key is a type and the implementation cannot be assigned to.
    */
   protected AbstractComponentAdapter(Object componentKey, Class<?> componentImplementation)
      throws ContainerException
   {
      if (componentImplementation == null)
      {
         throw new NullPointerException("componentImplementation");
      }
      this.componentKey = componentKey;
      this.componentImplementation = componentImplementation;
      checkTypeCompatibility();
   }

   /**
    * {@inheritDoc}
    * @see org.picocontainer.ComponentAdapter#getComponentKey()
    */
   public Object getComponentKey()
   {
      if (componentKey == null)
      {
         throw new NullPointerException("componentKey");
      }
      return componentKey;
   }

   /**
    * {@inheritDoc}
    * @see org.picocontainer.ComponentAdapter#getComponentImplementation()
    */
   public Class<?> getComponentImplementation()
   {
      return componentImplementation;
   }

   protected void checkTypeCompatibility() throws ContainerException
   {
      if (componentKey instanceof Class)
      {
         Class<?> componentType = (Class<?>)componentKey;
         if (!componentType.isAssignableFrom(componentImplementation))
         {
            throw new ContainerException("The type:" + componentType.getName() + "  was not assignable from the class "
               + componentImplementation.getName());
         }
      }
   }

   /**
    * @return Returns the ComponentAdapter's class name and the component's key.
    * @see java.lang.Object#toString()
    */
   public String toString()
   {
      return getClass().getName() + "[" + getComponentKey() + "]";
   }
}
