/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.services.cache.invalidation;

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This eXo cache type is a decorator allowing ExoCache instances that have
 * big values or non serializable values to be replicated thanks to an invalidation
 * mechanism. To prevent infinite loop described below, we replicate the hash code of
 * the value such that if the hash code is the same, we don't invalidate the value locally
 * <ul>
 * <li>Cluster node #1 puts (key1, value1) into the cache</li>
 * <li>On cluster node #2 key1 is invalidated by the put call in node #1</li>
 * <li>Node #2 re-loads key1 and puts (key1, value1) into the cache</li>
 * <li>On cluster node #1 key1 is invalidated, so we get back to step #1</li>
 * </ul>
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class InvalidationExoCache<K extends Serializable, V> implements ExoCache<K, V>,
   CacheListener<K, InvalidationExoCache.HashCode<V>>
{
   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.cache.InvalidationExoCache");

   /**
    * The eXo cache instance that we would like to replicate using the invalidation
    * mechanism
    */
   private final ExoCache<K, HashCode<V>> delegate;
   
   /**
    * The listeners of the cache
    */
   private final CopyOnWriteArrayList<CacheListener<? super K, ? super V>> listeners;
   
   /**
    * The local cache that contains the real values
    */
   private final ConcurrentMap<K, V> localCache;
   
   /**
    * @param delegate the underneath eXo cache instance, we assume that the eXo cache
    * implementation behind is fully functional.
    */
   public InvalidationExoCache(ExoCache<K, V> delegate)
   {
      this(delegate, delegate.getMaxSize() > 0 && delegate.getMaxSize() < 512 ? delegate.getMaxSize() : 512);
   }
   
   /**
    * @param delegate the underneath eXo cache instance, we assume that the eXo cache
    * implementation behind is fully functional.
    * @concurrencyLevel the estimated number of concurrently
    * updating threads. The implementation performs internal sizing
    * to try to accommodate this many threads.  
    */
   @SuppressWarnings("unchecked")
   public InvalidationExoCache(ExoCache<K, V> delegate, int concurrencyLevel)
   {
      this.delegate = (ExoCache<K, HashCode<V>>)delegate;
      // We listen to the cache in order to get a callbacks in case of internal puts for example
      this.delegate.addCacheListener(this);
      this.listeners = new CopyOnWriteArrayList<CacheListener<? super K, ? super V>>();
      this.localCache = new ConcurrentHashMap<K, V>(concurrencyLevel, 0.75f, concurrencyLevel);      
   }
   
   /**
    * @see org.exoplatform.services.cache.ExoCache#getName()
    */
   public String getName()
   {
      return delegate.getName();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#setName(java.lang.String)
    */
   public void setName(String name)
   {
      delegate.setName(name);
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#getLabel()
    */
   public String getLabel()
   {
      return delegate.getLabel();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#setLabel(java.lang.String)
    */
   public void setLabel(String s)
   {
      delegate.setLabel(s);
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#get(java.io.Serializable)
    */
   public V get(Serializable name)
   {
      HashCode<V> result = delegate.get(name);
      return result == null ? null : localCache.get(name);
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#remove(java.io.Serializable)
    */
   public V remove(Serializable key) throws NullPointerException
   {
      V value = localCache.get(key);
      delegate.remove(key);
      return value;
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#put(java.io.Serializable, java.lang.Object)
    */
   public void put(K key, V value) throws NullPointerException
   {
      delegate.put(key, new HashCode<V>(value));
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#putMap(java.util.Map)
    */
   public void putMap(Map<? extends K, ? extends V> objs) throws IllegalArgumentException
   {
      if (objs == null)
      {
         throw new IllegalArgumentException("No null map accepted");
      }
      Map<K, HashCode<V>> map = new LinkedHashMap<K, HashCode<V>>();
      for (Entry<? extends K, ? extends V> entry : objs.entrySet())
      {
         if (entry.getKey() == null)
         {
            throw new IllegalArgumentException("No null cache key accepted");
         }
         else if (entry.getValue() == null)
         {
            throw new IllegalArgumentException("No null cache value accepted");            
         }
         map.put(entry.getKey(), new HashCode<V>(entry.getValue()));
      }
      delegate.putMap(map);
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#clearCache()
    */
   public void clearCache()
   {
      delegate.clearCache();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#select(org.exoplatform.services.cache.CachedObjectSelector)
    */
   public void select(CachedObjectSelector<? super K, ? super V> selector) throws Exception
   {
      if (selector == null)
      {
         throw new IllegalArgumentException("No null selector");
      }
      for (Entry<K, V> entry : localCache.entrySet())
      {
         final K key = entry.getKey();
         final V value = entry.getValue();
         ObjectCacheInfo<V> info = new ObjectCacheInfo<V>()
         {
            public V get()
            {
               return value;
            }

            public long getExpireTime()
            {
               // Cannot know: The expire time is managed by JBoss Cache itself
               return -1;
            }
         };
         if (selector.select(key, info))
         {
            selector.onSelect(this, key, info);
         }
      }
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#getCacheSize()
    */
   public int getCacheSize()
   {
      return localCache.size();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#getMaxSize()
    */
   public int getMaxSize()
   {
      return delegate.getMaxSize();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#setMaxSize(int)
    */
   public void setMaxSize(int max)
   {
      delegate.setMaxSize(max);
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#getLiveTime()
    */
   public long getLiveTime()
   {
      return delegate.getLiveTime();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#setLiveTime(long)
    */
   public void setLiveTime(long period)
   {
      delegate.setLiveTime(period);
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#getCacheHit()
    */
   public int getCacheHit()
   {
      return delegate.getCacheHit();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#getCacheMiss()
    */
   public int getCacheMiss()
   {
      return delegate.getCacheMiss();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#getCachedObjects()
    */
   public List<? extends V> getCachedObjects()
   {
      return new ArrayList<V>(localCache.values());
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#removeCachedObjects()
    */
   public List<? extends V> removeCachedObjects()
   {
      final List<? extends V> list = getCachedObjects();
      clearCache();
      return list;
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#addCacheListener(org.exoplatform.services.cache.CacheListener)
    */
   public void addCacheListener(CacheListener<? super K, ? super V> listener) throws IllegalArgumentException
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("The listener cannot be null");
      }
      listeners.add(listener);
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#isLogEnabled()
    */
   public boolean isLogEnabled()
   {
      return delegate.isLogEnabled();
   }

   /**
    * @see org.exoplatform.services.cache.ExoCache#setLogEnabled(boolean)
    */
   public void setLogEnabled(boolean b)
   {
      delegate.setLogEnabled(b);
   }

   /**
    * {@inheritDoc}
    */
   public void onExpire(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
   {
      V value = localCache.remove(key);
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener<? super K, ? super V> listener : listeners)
      {
         try
         {
            listener.onExpire(context, key, value);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void onRemove(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
   {
      V value = localCache.remove(key);
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener<? super K, ? super V> listener : listeners)
      {
         try
         {
            listener.onRemove(context, key, value);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void onPut(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
   {
      V value = obj == null ? null : obj.getValue();
      if (value != null)
      {
         // we assume that it is a local put since the value is inside the HashCode object
         localCache.put(key, value);
      }
      else
      {
         // we assume that it is a remote put since the value is not inside the HashCode object
         V currentValue = localCache.get(key);
         if (currentValue != null && obj != null && currentValue.hashCode() == obj.hashCode())
         {
            // We assume that it is the same value so we don't change the value in the cache
            value = currentValue;
         }
         else
         {
            // A new value has been added to the cache so we invalidate the local one
            value = null;
            localCache.remove(key);
         }
      }
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener<? super K, ? super V> listener : listeners)
         try
         {
            listener.onPut(context, key, value);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
   }

   /**
    * {@inheritDoc}
    */
   public void onGet(CacheListenerContext context, K key, HashCode<V> obj) throws Exception
   {
      if (listeners.isEmpty())
      {
         return;
      }
      V value = obj == null ? null : localCache.get(key);
      for (CacheListener<? super K, ? super V> listener : listeners)
         try
         {
            listener.onGet(context, key, value);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
   }

   /**
    * @see org.exoplatform.services.cache.CacheListener#onClearCache(org.exoplatform.services.cache.CacheListenerContext)
    */
   public void onClearCache(CacheListenerContext context) throws Exception
   {
      localCache.clear();
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener<? super K, ? super V> listener : listeners)
      {
         try
         {
            listener.onClearCache(context);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }      
   }
      
   /**
    * We use this class to propagate the hash code of the value efficiently over the network
    */
   public static class HashCode<V> implements Externalizable
   {
      /**
       * The hash code of the value
       */
      private int hashCode;
      
      /**
       * The corresponding value
       */
      private V value;
      
      public HashCode() {}
      
      public HashCode(V value)
      {
         this.hashCode = value.hashCode();
         this.value = value;
      }
      
      /**
       * @return the value
       */
      public V getValue()
      {
         return value;
      }

      /**
       * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
       */
      public void writeExternal(ObjectOutput out) throws IOException
      {
         out.writeInt(hashCode);
      }

      /**
       * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
       */
      public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
      {
         this.hashCode = in.readInt();
      }

      /**
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         return hashCode;
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
         @SuppressWarnings("rawtypes")
         HashCode other = (HashCode)obj;
         if (hashCode != other.hashCode)
            return false;
         if (value != null && other.value != null)
         {
            return value.equals(other.value);
         }
         return true;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return "HashCode [hashCode=" + hashCode + ", value=" + value + "]";
      }
   }   
}
