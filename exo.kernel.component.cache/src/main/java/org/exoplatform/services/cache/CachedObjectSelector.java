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

import java.io.Serializable;

/**
 * Created by The eXo Platform SAS Author : Thuannd nhudinhthuan@yahoo.com Apr
 * 4, 2006
 * @LevelAPI Platform
 */
public interface CachedObjectSelector<K extends Serializable, V>
{
   /**
    * @param key the name of the cache entry
    * @param ocinfo the cache info instance
    * @return flag asserts whether key is managed by the Cache or not
    */
   public boolean select(K key, ObjectCacheInfo<? extends V> ocinfo);
   /**
    * @param cache the exo cache
    * @param ocinfo the cache info instance
    */
   public void onSelect(ExoCache<? extends K, ? extends V> cache, K key, ObjectCacheInfo<? extends V> ocinfo)
      throws Exception;
}
