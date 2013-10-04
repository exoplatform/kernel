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

/**
 * This abstract class defines the main parts of a dependency
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class Dependency
{

   /**
    * The key of the corresponding component
    */
   protected final Object key;

   /**
    * The bind type
    */
   protected final Class<?> bindType;

   /**
    * Indicates whether the dependency is lazy or not
    */
   private final boolean lazy;

   public Dependency(Object key, Class<?> bindType)
   {
      this(key, bindType, false);
   }

   public Dependency(Object key, Class<?> bindType, boolean lazy)
   {
      this.key = key;
      this.bindType = bindType;
      this.lazy = lazy;
   }

   /**
    * @return the key
    */
   public Object getKey()
   {
      return key;
   }

   /**
    * @return the bindType
    */
   public Class<?> getBindType()
   {
      return bindType;
   }

   /**
    * @return the lazy
    */
   public boolean isLazy()
   {
      return lazy;
   }

   /**
    * Loads a given dependency from the provided {@link ExoContainer}
    */
   protected abstract Object load(ExoContainer holder);

   /**
    * Gives the {@link ComponentAdapter} corresponding to this dependency
    */
   protected abstract ComponentAdapter<?> getAdapter(ExoContainer holder);

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((bindType == null) ? 0 : bindType.hashCode());
      result = prime * result + ((key == null) ? 0 : key.hashCode());
      result = prime * result + (lazy ? 1231 : 1237);
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
      Dependency other = (Dependency)obj;
      if (bindType == null)
      {
         if (other.bindType != null)
            return false;
      }
      else if (!bindType.equals(other.bindType))
         return false;
      if (key == null)
      {
         if (other.key != null)
            return false;
      }
      else if (!key.equals(other.key))
         return false;
      if (lazy != other.lazy)
         return false;
      return true;
   }
}
