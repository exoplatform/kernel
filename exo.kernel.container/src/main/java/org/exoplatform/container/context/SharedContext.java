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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This is the root class of all the shared contexts, it relies on a {@link ConcurrentMap}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class SharedContext<K> extends AbstractContext<K>
{

   /**
    * A map in which we store all current locks
    */
   private final ConcurrentMap<String, Lock> locks = new ConcurrentHashMap<String, Lock>(64, 0.75f, 64);

   /**
    * {@inheritDoc}
    */
   protected final boolean isSharable()
   {
      return true;
   }

   /**
    * {@inheritDoc}
    */
   protected Lock getLock(String id)
   {
      CreationContextStorage storage = getStorage();
      String idStorage = storage.getId();
      if (idStorage == null)
      {
         throw new IllegalArgumentException("The id of a storage cannot be null");
      }
      String fullID = (new StringBuilder(id).append('#').append(idStorage)).toString();
      Lock lock = locks.get(fullID);
      if (lock != null)
      {
         return lock;
      }
      lock = new SharedContextLock(fullID);
      Lock o = locks.putIfAbsent(fullID, lock);
      return o == null ? lock : o;
   }

   private class SharedContextLock extends ReentrantLock
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = 4963739847878774832L;

      /**
       * The full id
       */
      private final String id;

      public SharedContextLock(String id)
      {
         this.id = id;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void unlock()
      {
         super.unlock();
         locks.remove(id, this);
      }
   }
}
