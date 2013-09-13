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

import java.util.concurrent.locks.Lock;


/**
 * This is the root class of all the unshared contexts, it relies on a thread local
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class UnSharedContext<K> extends AbstractContext<K>
{

   /**
    * {@inheritDoc}
    */
   public void unregister(K key)
   {
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void deactivate(K key)
   {
      destroy();
      super.deactivate(key);
   }

   /**
    * {@inheritDoc}
    */
   protected final boolean isSharable()
   {
      return false;
   }

   /**
    * {@inheritDoc}
    */
   protected final Lock getLock(String id)
   {
      return null;
   }
}
