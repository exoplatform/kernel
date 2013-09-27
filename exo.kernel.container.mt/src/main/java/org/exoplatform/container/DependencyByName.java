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
 * This defines a dependency by name
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DependencyByName extends Dependency
{

   public DependencyByName(String key, Class<?> bindType)
   {
      super(key, bindType);
   }

   /**
    * {@inheritDoc}
    */
   protected Object load(ExoContainer holder) throws Exception
   {
      return holder.getComponentInstance(key, bindType);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ComponentAdapter<?> getAdapter(ExoContainer holder)
   {
      return holder.getComponentAdapter(key, bindType);
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "DependencyByName [key=" + key + ", bindType=" + bindType + "]";
   }
}
