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

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SAS<br>
 * A bare cache.
 *
 * @author <a href="mailto:tuan08@users.sourceforge.net">Tuan Nguyen</a>
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of cached values
 * @LevelAPI Platform
 */
@Managed
@NameTemplate({@Property(key = "service", value = "cache"), @Property(key = "name", value = "{Name}")})
@ManagedDescription("Exo Cache")
public interface ExoCache<K extends Serializable, V>
{

   /**
    * Returns the cache name
    *
    * @return the cache name
    */
   @Managed
   @ManagedName("Name")
   @ManagedDescription("The cache name")
   public String getName();

   /**
    * Sets the cache name.
    *
    * @param name the cache name
    */
   public void setName(String name);

   /**
    * Returns the cache label
    *
    * @return the cache label
    */
   public String getLabel();

   /**
    * Sets the cache label
    *
    * @param s the cache label
    */
   public void setLabel(String s);

   /**
    * Performs a lookup operation.
    *
    * @param key the cache key
    * @return the cached value which may be evaluated to null
    */
   public V get(Serializable key);

   /**
    * Removes an entry from the cache.
    *
    * @param key the cache key
    * @return the previously cached value or null if no entry existed or that entry value was evaluated to null
    * @throws NullPointerException if the provided key is null
    */
   public V remove(Serializable key) throws NullPointerException;

   /**
    * Removes an entry from the cache local mode (avoid replication).
    *
    * @param key the cache key
    * @throws NullPointerException if the provided key is null
    */
   public default void removeLocal(Serializable key) throws NullPointerException {
      remove(key);
   }

   /**
    * Performs a put in the cache.
    *
    * @param key the cache key
    * @param value the cached value
    * @throws NullPointerException if the key is null
    */
   public void put(K key, V value) throws NullPointerException;

   /**
    * Performs a put in the cache local mode (avoid replication).
    *
    * @param key the cache key
    * @param value the cached value
    * @throws NullPointerException if the key is null
    */
   public default void putLocal(K key, V value) throws NullPointerException {
      put(key, value);
   }

   /**
    * Performs a put of all the entries provided by the map argument.
    *
    * @param objs the objects to put
    * @throws NullPointerException if the provided argument is null
    * @throws IllegalArgumentException if the provided map contains a null key
    */
   public void putMap(Map<? extends K, ? extends V> objs) throws NullPointerException, IllegalArgumentException;

   /**
    * Performs a put of all the entries provided by the map argument on asynchronous mode.
    *
    * @param objs the objects to put
    * @throws NullPointerException if the provided argument is null
    * @throws IllegalArgumentException if the provided map contains a null key
    * @throws UnsupportedOperationException if async put operation is not supported
    */
   public default void putAsyncMap(Map<? extends K, ? extends V> objs) throws NullPointerException, IllegalArgumentException {
      throw new UnsupportedOperationException();
   }

   /**
    * Clears the cache.
    */
   @Managed
   @ManagedDescription("Evict all entries of the cache")
   public void clearCache();

   /**
    * Selects a subset of the cache.
    *
    * @param selector the selector
    * @throws Exception any exception
    */
   public void select(CachedObjectSelector<? super K, ? super V> selector) throws Exception;

   /**
    * Returns the number of entries in the cache.
    *
    * @return the size of the cache
    */
   @Managed
   @ManagedName("Size")
   @ManagedDescription("The cache size")
   public int getCacheSize();

   /**
    * Returns the maximum capacity of the cache.
    *
    * @return the maximum capacity
    */
   @Managed
   @ManagedName("MaxNodes")
   @ManagedDescription("Maximum number of entries in a cache instance. -1 means no limit.")
   public int getMaxSize();

   /**
    * Sets the maximum capacity of the cache.
    *
    * @param max the maximum capacity
    */
   @Managed
   public void setMaxSize(int max);

   /**
    * Returns the maximum life time of an entry in the cache. The life time is a value in seconds, a negative
    * value means that the life time is infinite.
    *
    * @return the live time
    */
   @Managed
   @ManagedName("TimeToLive")
   @ManagedDescription("The maximum life time of an entry in seconds")
   public long getLiveTime();

   /**
    * Sets the maximum life time of an entry in the cache.
    *
    * @param period the live time
    */
   @Managed
   public void setLiveTime(long period);

   /**
    * Returns the number of time the cache was queried and a valid entry was returned.
    *
    * @return the cache hits
    */
   @Managed
   @ManagedName("HitCount")
   @ManagedDescription("The count of cache hits")
   public int getCacheHit();

   /**
    * Returns the number of time the cache was queried and no entry was returned.
    *
    * @return the cache misses
    */
   @Managed
   @ManagedName("MissCount")
   @ManagedDescription("The count of cache misses")
   public int getCacheMiss();

   /**
    * Returns a list of cached object that are considered as valid when the method is called. Any non valid
    * object will not be returned.
    *
    * @return the list of cached objects
    * @throws Exception any exception
    */
   public List<? extends V> getCachedObjects() throws Exception;

   /**
    * Clears the cache and returns the list of cached object that are considered as valid when the method is called.
    * Any non valid
    * object will not be returned.
    *
    * @return the list of cached objects
    */
   public List<? extends V> removeCachedObjects();

   /**
    * Add a listener.
    *
    * @param listener the listener to add
    * @throws NullPointerException if the listener is null
    */
   public void addCacheListener(CacheListener<? super K, ? super V> listener) throws NullPointerException;

   /**
    * On get entry event
    * @param key entry key
    * @param obj value
    */
   public default void onGet(K key, V obj){}

   /**
    * On expire entry event
    * @param key entry key
    * @param obj value
    */
   public  default void onExpire(K key, V obj){}

   /**
    * On remove entry event
    * @param key entry key
    * @param obj value
    */
   public default  void onRemove(K key, V obj){}

   /**
    * On put entry event
    * @param key entry key
    * @param obj value
    */
   public default void onPut(K key, V obj){}

   /**
    * On put entry event
    * @param key entry key
    * @param obj value
    */
   public default void onPutLocal(K key, V obj){}

   /**
    * on clear cache event
    */
   public default void onClearCache(){}

   public boolean isLogEnabled();

   public void setLogEnabled(boolean b);
}
