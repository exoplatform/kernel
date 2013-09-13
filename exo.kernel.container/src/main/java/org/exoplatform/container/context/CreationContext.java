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
package org.exoplatform.container.context;

import java.io.Serializable;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * This class defined all the context needed while creation the object instance
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class CreationContext<T> implements Serializable
{
   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 1399725412498300170L;

   private transient Contextual<T> contextual;
   private transient CreationalContext<T> creationalContext;
   private T instance;

   public CreationContext()
   {
   }

   public CreationContext(Contextual<T> contextual, CreationalContext<T> creationalContext, T instance)
   {
      this.contextual = contextual;
      this.creationalContext = creationalContext;
      this.instance = instance;
   }

   /**
    * @return the creationalContext
    */
   public CreationalContext<T> getCreationalContext()
   {
      return creationalContext;
   }

   /**
    * @return the instance
    */
   public T getInstance()
   {
      return instance;
   }

   /**
    * @return the contextual
    */
   public Contextual<T> getContextual()
   {
      return contextual;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((instance == null) ? 0 : instance.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
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
      CreationContext<?> other = (CreationContext<?>)obj;
      if (instance == null)
      {
         if (other.instance != null)
            return false;
      }
      else if (!instance.equals(other.instance))
         return false;
      return true;
   }
}
