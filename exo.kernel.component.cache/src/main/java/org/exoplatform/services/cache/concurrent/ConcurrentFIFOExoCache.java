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
package org.exoplatform.services.cache.concurrent;

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.log.Log;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link org.exoplatform.services.cache.ExoCache} implementation based on {@link java.util.concurrent.ConcurrentHashMap}
 * that minimize locking. Cache entries are maintained in a fifo list that is used for the fifo eviction policy.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConcurrentFIFOExoCache<K extends Serializable, V> implements ExoCache<K, V>
{

   private static int DEFAULT_MAX_SIZE = 50;

   private final Log log;

   private volatile long liveTimeMillis;

   volatile int maxSize;

   private CopyOnWriteArrayList<ListenerContext<K, V>> listeners;

   private CacheState<K, V> state;

   AtomicInteger hits = new AtomicInteger();

   AtomicInteger misses = new AtomicInteger();

   private String label;

   private String name;

   private boolean logEnabled = false;

   public ConcurrentFIFOExoCache()
   {
      this(DEFAULT_MAX_SIZE);
   }

   public ConcurrentFIFOExoCache(Log log)
   {
      this(DEFAULT_MAX_SIZE, log);
   }

   public ConcurrentFIFOExoCache(int maxSize)
   {
      this(null, maxSize);
   }

   public ConcurrentFIFOExoCache(int maxSize, Log log)
   {
      this(null, maxSize, log);
   }

   public ConcurrentFIFOExoCache(String name, int maxSize)
   {
      this(name, maxSize, null);
   }

   public ConcurrentFIFOExoCache(String name, int maxSize, Log log)
   {
      this.maxSize = maxSize;
      this.name = name;
      this.state = new CacheState<K, V>(this, log);
      this.liveTimeMillis = -1;
      this.log = log;
      this.listeners = new CopyOnWriteArrayList<ListenerContext<K, V>>();
   }

   public void assertConsistent()
   {
      state.assertConsistency();
   }

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String getLabel()
   {
      if (label == null)
      {
         if (name.length() > 30)
         {
            String shortLabel = name.substring(name.lastIndexOf(".") + 1);
            setLabel(shortLabel);
            return shortLabel;
         }
         return name;
      }
      return label;
   }

   public void setLabel(String name)
   {
      label = name;
   }

   public long getLiveTime()
   {
      long tmp = getLiveTimeMillis();
      return tmp == -1 ? -1 : tmp / 1000;
   }

   public void setLiveTime(long period)
   {
      setLiveTimeMillis(period * 1000);
   }

   public long getLiveTimeMillis()
   {
      return liveTimeMillis;
   }

   public void setLiveTimeMillis(long liveTimeMillis)
   {
      if (liveTimeMillis < 0)
      {
         liveTimeMillis = -1;
      }
      this.liveTimeMillis = liveTimeMillis;
   }

   public int getMaxSize()
   {
      return maxSize;
   }

   public void setMaxSize(int max)
   {
      this.maxSize = max;
   }

   public V get(Serializable name)
   {
      if (name == null)
      {
         return null;
      }
      return state.get(name);
   }

   public void put(K name, V obj)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null cache key accepted");
      }
      if (liveTimeMillis != 0)
      {
         long expirationTime = liveTimeMillis > 0 ? System.currentTimeMillis() + liveTimeMillis : Long.MAX_VALUE;
         state.put(expirationTime, name, obj);
      }
   }

   public void putMap(Map<? extends K, ? extends V> objs)
   {
      if (objs == null)
      {
         throw new IllegalArgumentException("No null map accepted");
      }
      long expirationTime = liveTimeMillis > 0 ? System.currentTimeMillis() + liveTimeMillis : Long.MAX_VALUE;
      for (Serializable name : objs.keySet())
      {
         if (name == null)
         {
            throw new IllegalArgumentException("No null cache key accepted");
         }
      }
      for (Map.Entry<? extends K, ? extends V> entry : objs.entrySet())
      {
         state.put(expirationTime, entry.getKey(), entry.getValue());
      }
   }

   public V remove(Serializable name)
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null cache key accepted");
      }
      return state.remove(name);
   }

   public List<? extends V> getCachedObjects()
   {
      LinkedList<V> list = new LinkedList<V>();
      for (ObjectRef<K, V> objectRef : state.map.values())
      {
         V object = objectRef.getObject();
         if (objectRef.isValid())
         {
            list.add(object);
         }
      }
      return list;
   }

   public List<? extends V> removeCachedObjects()
   {
      List<? extends V> list = getCachedObjects();
      clearCache();
      return list;
   }

   public void clearCache()
   {
      state = new CacheState<K, V>(this, log);
   }

   public void select(CachedObjectSelector<? super K, ? super V> selector) throws Exception
   {
      if (selector == null)
      {
         throw new IllegalArgumentException("No null selector");
      }
      for (Map.Entry<K, ObjectRef<K, V>> entry : state.map.entrySet())
      {
         K key = entry.getKey();
         ObjectRef<K, V> info = entry.getValue();
         ObjectCacheInfo<V> bilto = null;
         if (selector.select(key, bilto))
         {
            selector.onSelect(this, key, info);
         }
      }
   }

   public int getCacheSize()
   {
      return state.queue.size();
   }

   public int getCacheHit()
   {
      return hits.get();
   }

   public int getCacheMiss()
   {
      return misses.get();
   }

   public synchronized void addCacheListener(CacheListener<? super K, ? super V> listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("The listener cannot be null");
      }
      listeners.add(new ListenerContext<K, V>(listener, this));
   }

   public boolean isLogEnabled()
   {
      return logEnabled;
   }

   public void setLogEnabled(boolean logEnabled)
   {
      this.logEnabled = logEnabled;
   }

   //

   void onExpire(K key, V obj)
   {
      if (!listeners.isEmpty())
         for (ListenerContext<K, V> context : listeners)
            context.onExpire(key, obj);
   }

   void onRemove(K key, V obj)
   {
      if (!listeners.isEmpty())
         for (ListenerContext<K, V> context : listeners)
            context.onRemove(key, obj);
   }

   void onPut(K key, V obj)
   {
      if (!listeners.isEmpty())
         for (ListenerContext<K, V> context : listeners)
            context.onPut(key, obj);
   }

   void onGet(K key, V obj)
   {
      if (!listeners.isEmpty())
         for (ListenerContext<K, V> context : listeners)
            context.onGet(key, obj);
   }

   void onClearCache()
   {
      if (!listeners.isEmpty())
         for (ListenerContext<K, V> context : listeners)
            context.onClearCache();
   }
}