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
package org.exoplatform.services.cache;

/**
 * Created by The eXo Platform SAS</br>
 * Wraps a cache entry to provide meta information in addition of the entry value.
 *
 * @author <a href="tuan08@users.sourceforge.net">Tuan Nguyen</a>.
 *
 * @param <V> the value type
 * @LevelAPI Platform
 */
public interface ObjectCacheInfo<V>
{

   /**
    * Returns the expiration time of the entry in milli seconds.
    *
    * @return the expiration time
    */
   public long getExpireTime();

   /**
    * Returns the entry value which may be null.
    *
    * @return the entry value
    */
   public V get();
}
