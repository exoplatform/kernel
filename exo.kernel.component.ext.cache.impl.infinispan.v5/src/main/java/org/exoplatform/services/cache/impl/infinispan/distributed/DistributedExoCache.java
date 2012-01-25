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
package org.exoplatform.services.cache.impl.infinispan.distributed;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.services.cache.CacheInfo;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.ispn.AbstractMapper;
import org.exoplatform.services.ispn.DistributedCacheManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.MapReduceTask;
import org.infinispan.distexec.mapreduce.Reducer;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntriesEvictedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.util.concurrent.locks.LockManager;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DistributedExoCache<K extends Serializable, V> implements ExoCache<K, V>
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger
      .getLogger("exo.kernel.component.ext.cache.impl.infinispan.v5.DistributedExoCache");

   public static final String CACHE_NAME = "eXoCache";

   private final AtomicInteger hits = new AtomicInteger(0);

   private final AtomicInteger misses = new AtomicInteger(0);

   private String label;

   private String name;

   private final String fullName;

   private boolean distributed;

   private boolean replicated;

   private boolean logEnabled;

   @SuppressWarnings("rawtypes")
   private static final ConcurrentMap<Cache, ConcurrentMap<String, List<ListenerContext>>> ALL_LISTENERS =
      new ConcurrentHashMap<Cache, ConcurrentMap<String, List<ListenerContext>>>();

   protected final AdvancedCache<CacheKey<K>, V> cache;

   @SuppressWarnings("unchecked")
   public DistributedExoCache(ExoContainerContext ctx, ExoCacheConfig config, Cache<K, V> cache)
   {
      this.fullName = ctx.getName() + "-" + config.getName();
      this.cache = (AdvancedCache<CacheKey<K>, V>)cache.getAdvancedCache();
      setDistributed(config.isDistributed());
      setLabel(config.getLabel());
      setName(config.getName());
      setLogEnabled(config.isLogEnabled());
      setReplicated(config.isRepicated());
   }

   AdvancedCache<CacheKey<K>, V> getCache()
   {
      return cache;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   public void addCacheListener(CacheListener<? super K, ? super V> listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException();
      }
      List<ListenerContext> lListeners = getListeners(fullName);
      if (lListeners == null)
      {
         lListeners = new CopyOnWriteArrayList<ListenerContext>();
         boolean alreadyAdded = false;
         ConcurrentMap<String, List<ListenerContext>> listeners = getOrCreateListeners();
         if (listeners.isEmpty())
         {
            synchronized (listeners)
            {
               if (listeners.isEmpty())
               {
                  // Ensure that the listener is added only once
                  cache.addListener(new CacheEventListener());
                  listeners.put(fullName, lListeners);
                  alreadyAdded = true;
               }
            }
         }
         if (!alreadyAdded)
         {
            List<ListenerContext> oldValue = listeners.putIfAbsent(fullName, lListeners);
            if (oldValue != null)
            {
               lListeners = oldValue;
            }
         }
      }
      lListeners.add(new ListenerContext<K, V>(listener, this));
   }

   @SuppressWarnings("rawtypes")
   private ConcurrentMap<String, List<ListenerContext>> getOrCreateListeners()
   {
      ConcurrentMap<String, List<ListenerContext>> listeners = ALL_LISTENERS.get(cache);
      if (listeners == null)
      {
         listeners = new ConcurrentHashMap<String, List<ListenerContext>>();
         ConcurrentMap<String, List<ListenerContext>> oldValue = ALL_LISTENERS.putIfAbsent(cache, listeners);
         if (oldValue != null)
         {
            listeners = oldValue;
         }
      }
      return listeners;
   }

   @SuppressWarnings("rawtypes")
   private List<ListenerContext> getListeners(String fullName)
   {
      ConcurrentMap<String, List<ListenerContext>> listeners = ALL_LISTENERS.get(cache);
      return listeners == null ? null : listeners.get(fullName);
   }

   /**
    * {@inheritDoc}
    */
   public void clearCache()
   {
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {

         @Override
         public Void run()
         {
            MapReduceTask<CacheKey<K>, V, String, CacheKey<K>> task =
               new MapReduceTask<CacheKey<K>, V, String, CacheKey<K>>(cache);
            task.mappedWith(new ClearCacheMapper<K, V>(fullName)).reducedWith(new ClearCacheReducer<String, V, K>());
            task.execute();
            return null;
         }

      });
      onClearCache();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public V get(Serializable name)
   {
      if (name == null)
      {
         return null;
      }
      @SuppressWarnings("rawtypes")
      final CacheKey key = new CacheKey<Serializable>(fullName, name);
      final V result = SecurityHelper.doPrivilegedAction(new PrivilegedAction<V>()
      {

         @Override
         public V run()
         {
            return cache.get(key);
         }

      });
      if (result == null)
      {
         misses.incrementAndGet();
      }
      else
      {
         hits.incrementAndGet();
      }
      onGet(key, result);
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public int getCacheHit()
   {
      return hits.get();
   }

   /**
    * {@inheritDoc}
    */
   public int getCacheMiss()
   {
      return misses.get();
   }

   /**
    * {@inheritDoc}
    */
   public int getCacheSize()
   {
      Map<String, Integer> map = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Map<String, Integer>>()
      {

         @Override
         public Map<String, Integer> run()
         {
            MapReduceTask<CacheKey<K>, V, String, Integer> task =
               new MapReduceTask<CacheKey<K>, V, String, Integer>(cache);
            task.mappedWith(new GetSizeMapper<K, V>(fullName)).reducedWith(new GetSizeReducer<String>());
            return task.execute();
         }

      });
      int sum = 0;
      for (Integer i : map.values())
      {
         sum += i;
      }
      return sum;
   }

   /**
    * {@inheritDoc}
    */
   public List<V> getCachedObjects()
   {
      Map<String, List<V>> map = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Map<String, List<V>>>()
      {

         @Override
         public Map<String, List<V>> run()
         {
            MapReduceTask<CacheKey<K>, V, String, List<V>> task =
               new MapReduceTask<CacheKey<K>, V, String, List<V>>(cache);
            task.mappedWith(new GetCachedObjectsMapper<K, V>(fullName)).reducedWith(
               new GetCachedObjectsReducer<String, V>());
            return task.execute();
         }

      });
      List<V> result = new ArrayList<V>();
      for (List<V> vals : map.values())
      {
         result.addAll(vals);
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public String getLabel()
   {
      return label;
   }

   /**
    * {@inheritDoc}
    */
   public String getName()
   {
      return name;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isDistributed()
   {
      return distributed;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isLogEnabled()
   {
      return logEnabled;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isReplicated()
   {
      return replicated;
   }

   /**
    * {@inheritDoc}
    */
   public void put(final K key, final V value) throws IllegalArgumentException
   {
      if (key == null)
      {
         throw new IllegalArgumentException("No null cache key accepted");
      }
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {

         @Override
         public Void run()
         {
            putOnly(key, value);
            return null;
         }

      });
      onPut(key, value);
   }

   /**
    * Only puts the data into the cache nothing more
    */
   protected void putOnly(K key, V value)
   {
      cache.withFlags(Flag.SKIP_REMOTE_LOOKUP).put(new CacheKey<K>(fullName, key), value);
   }

   /**
    * {@inheritDoc}
    */
   public void putMap(final Map<? extends K, ? extends V> objs) throws IllegalArgumentException
   {
      if (objs == null)
      {
         throw new IllegalArgumentException("No null map accepted");
      }
      for (Serializable name : objs.keySet())
      {
         if (name == null)
         {
            throw new IllegalArgumentException("No null cache key accepted");
         }
      }
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {

         @Override
         public Void run()
         {
            cache.startBatch();
            try
            {
               // Start transaction
               for (Map.Entry<? extends K, ? extends V> entry : objs.entrySet())
               {
                  putOnly(entry.getKey(), entry.getValue());
               }
               cache.endBatch(true);
               // End transaction
               for (Map.Entry<? extends K, ? extends V> entry : objs.entrySet())
               {
                  onPut(entry.getKey(), entry.getValue());
               }
            }
            catch (Exception e)
            {
               cache.endBatch(false);
               LOG.warn("An error occurs while executing the putMap method", e);
            }
            return null;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public V remove(Serializable name) throws IllegalArgumentException
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null cache key accepted");
      }
      @SuppressWarnings("rawtypes")
      CacheKey key = new CacheKey<Serializable>(fullName, name);
      V result = cache.remove(key);
      onRemove(key, result);
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public List<V> removeCachedObjects()
   {
      final List<V> list = getCachedObjects();
      clearCache();
      return list;
   }

   /**
    * {@inheritDoc}
    */
   public void select(CachedObjectSelector<? super K, ? super V> selector) throws Exception
   {
      if (selector == null)
      {
         throw new IllegalArgumentException("No null selector");
      }
      Map<K, V> map = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Map<K, V>>()
      {

         @Override
         public Map<K, V> run()
         {
            MapReduceTask<CacheKey<K>, V, K, V> task = new MapReduceTask<CacheKey<K>, V, K, V>(cache);
            task.mappedWith(new GetEntriesMapper<K, V>(fullName)).reducedWith(new GetEntriesReducer<K, V>());
            return task.execute();
         }

      });

      for (K key : map.keySet())
      {
         if (key == null)
         {
            continue;
         }
         final V value = map.get(key);
         ObjectCacheInfo<V> info = new ObjectCacheInfo<V>()
         {
            public V get()
            {
               return value;
            }

            public long getExpireTime()
            {
               // Cannot know: The expire time is managed by Infinispan itself
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
    * {@inheritDoc}
    */
   public void setDistributed(boolean distributed)
   {
      this.distributed = distributed;
   }

   /**
    * {@inheritDoc}
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * {@inheritDoc}
    */
   public void setLogEnabled(boolean logEnabled)
   {
      this.logEnabled = logEnabled;
   }

   /**
    * {@inheritDoc}
    */
   public void setName(String name)
   {
      this.name = name;
   }

   /**
    * {@inheritDoc}
    */
   public void setReplicated(boolean replicated)
   {
      this.replicated = replicated;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   void onExpire(CacheKey<K> key, V obj)
   {
      List<ListenerContext> listeners = getListeners(key.getFullName());
      if (listeners == null || listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext context : listeners)
      {
         try
         {
            context.onExpire(key.getKey(), obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   void onRemove(CacheKey<K> key, V obj)
   {
      List<ListenerContext> listeners = getListeners(key.getFullName());
      if (listeners == null || listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext context : listeners)
      {
         try
         {
            context.onRemove(key, obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   void onPut(CacheKey<K> key, V obj)
   {
      onPut(key.getFullName(), key.getKey(), obj);
   }

   void onPut(K key, V obj)
   {
      onPut(fullName, key, obj);
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   void onPut(String fullName, K key, V obj)
   {
      List<ListenerContext> listeners = getListeners(fullName);
      if (listeners == null || listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext context : listeners)
      {
         try
         {
            context.onPut(key, obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   void onGet(CacheKey<K> key, V obj)
   {
      List<ListenerContext> listeners = getListeners(key.getFullName());
      if (listeners == null || listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext context : listeners)
      {
         try
         {
            context.onGet(key, obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @SuppressWarnings("rawtypes")
   void onClearCache()
   {
      List<ListenerContext> listeners = getListeners(fullName);
      if (listeners == null || listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext context : listeners)
      {
         try
         {
            context.onClearCache();
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @Listener
   public class CacheEventListener
   {
      /**
       * Warning Infinispan triggers a <code>CacheEntryEvictedEvent</code> only at explicit eviction
       * that is done lazily which is not exactly what we expect, we still use it to be 
       * able to use it with <code>avoidValueReplication</code> set to <code>true</code>.
       */
      @CacheEntriesEvicted
      public void cacheEntryEvicted(CacheEntriesEvictedEvent<CacheKey<K>, V> evt)
      {
         if (evt.isPre())
         {
            for (Map.Entry<CacheKey<K>, V> entry : evt.getEntries().entrySet())
            {
               onExpire(entry.getKey(), entry.getValue());
            }
         }
      }

      @CacheEntryRemoved
      public void cacheEntryRemoved(CacheEntryRemovedEvent<CacheKey<K>, V> evt)
      {
         if (evt.isPre() && !evt.isOriginLocal())
         {
            final CacheKey<K> key = evt.getKey();
            final V value = evt.getValue();
            onRemove(key, value);
         }
      }

      @CacheEntryModified
      public void cacheEntryModified(CacheEntryModifiedEvent<CacheKey<K>, V> evt)
      {
         if (!evt.isOriginLocal() && !evt.isPre())
         {
            final CacheKey<K> key = evt.getKey();
            final V value = evt.getValue();
            onPut(key, value);
         }
      }
   }

   private static class ListenerContext<K extends Serializable, V> implements CacheListenerContext, CacheInfo
   {

      /** . */
      private final ExoCache<K, V> cache;

      /** . */
      final CacheListener<? super K, ? super V> listener;

      public ListenerContext(CacheListener<? super K, ? super V> listener, ExoCache<K, V> cache)
      {
         this.listener = listener;
         this.cache = cache;
      }

      public CacheInfo getCacheInfo()
      {
         return this;
      }

      public String getName()
      {
         return cache.getName();
      }

      public int getMaxSize()
      {
         return cache.getMaxSize();
      }

      public long getLiveTime()
      {
         return cache.getLiveTime();
      }

      public int getSize()
      {
         return cache.getCacheSize();
      }

      void onExpire(K key, V obj) throws Exception
      {
         listener.onExpire(this, key, obj);
      }

      void onRemove(K key, V obj) throws Exception
      {
         listener.onRemove(this, key, obj);
      }

      void onPut(K key, V obj) throws Exception
      {
         listener.onPut(this, key, obj);
      }

      void onGet(K key, V obj) throws Exception
      {
         listener.onGet(this, key, obj);
      }

      void onClearCache() throws Exception
      {
         listener.onClearCache(this);
      }
   }

   public void setMaxSize(int max)
   {
      throw new UnsupportedOperationException("The configuration of the cache cannot not be modified");
   }

   public void setLiveTime(long period)
   {
      throw new UnsupportedOperationException("The configuration of the cache cannot not be modified");
   }

   @ManagedName("MaxEntries")
   @ManagedDescription("Maximum number of entries in a cache instance. -1 means no limit.")
   public int getMaxSize()
   {
      return cache.getConfiguration().getEvictionMaxEntries();
   }

   @ManagedName("Lifespan")
   @ManagedDescription("Maximum lifespan of a cache entry, after which the entry is expired cluster-wide."
      + " -1 means the entries never expire.")
   public long getLiveTime()
   {
      return cache.getConfiguration().getExpirationLifespan();
   }

   @Managed
   @ManagedName("MaxIdle")
   @ManagedDescription("Maximum idle time a cache entry will be maintained in the cache. "
      + "If the idle time is exceeded, the entry will be expired cluster-wide. -1 means the entries never expire.")
   public long getMaxIdle()
   {
      return cache.getConfiguration().getExpirationMaxIdle();
   }

   @Managed
   @ManagedName("WakeUpInterval")
   @ManagedDescription("Interval between subsequent eviction runs. If you wish to disable the periodic eviction "
      + "process altogether, set wakeupInterval to -1.")
   public long getWakeUpInterval()
   {
      return cache.getConfiguration().getExpirationWakeUpInterval();
   }

   public static class CacheKey<K> implements Externalizable
   {
      private K key;

      private String fullName;

      public CacheKey()
      {
      }

      public CacheKey(String fullName, K key)
      {
         this.fullName = fullName;
         this.key = key;
      }

      /**
       * @return the nested key
       */
      K getKey()
      {
         return key;
      }

      /**
       * @return the fullName
       */
      String getFullName()
      {
         return fullName;
      }

      /**
       * @see java.lang.Object#hashCode()
       */
      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
         result = prime * result + ((key == null) ? 0 : key.hashCode());
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
         @SuppressWarnings("rawtypes")
         CacheKey other = (CacheKey)obj;
         if (fullName == null)
         {
            if (other.fullName != null)
               return false;
         }
         else if (!fullName.equals(other.fullName))
            return false;
         if (key == null)
         {
            if (other.key != null)
               return false;
         }
         else if (!key.equals(other.key))
            return false;
         return true;
      }

      /**
       * @see java.lang.Object#toString()
       */
      @Override
      public String toString()
      {
         return "CacheKey [fullName=" + fullName + ", key=" + key + "]";
      }

      /**
       * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
       */
      public void writeExternal(ObjectOutput out) throws IOException
      {
         byte[] buf = fullName.getBytes("UTF-8");
         out.writeInt(buf.length);
         out.write(buf);
         out.writeObject(key);
      }

      /**
       * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
       */
      @SuppressWarnings("unchecked")
      public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
      {
         byte[] buf = new byte[in.readInt()];
         in.readFully(buf);
         fullName = new String(buf, "UTF-8");
         key = (K)in.readObject();
      }
   }

   private abstract static class AbstractExoCacheMapper<K, V, KOut, VOut> extends
      AbstractMapper<CacheKey<K>, V, KOut, VOut> implements Externalizable
   {
      /**
       * The full name of the cache instance
       */
      private String fullName;

      public AbstractExoCacheMapper()
      {
      }

      public AbstractExoCacheMapper(String fullName)
      {
         this.fullName = fullName;
      }

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = 7962676854308932222L;

      /**
       * @see org.exoplatform.services.ispn.AbstractMapper#isValid(java.lang.Object)
       */
      @Override
      protected boolean isValid(CacheKey<K> key)
      {
         return fullName.equals(key.getFullName());
      }

      /**
       * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
       */
      public void writeExternal(ObjectOutput out) throws IOException
      {
         byte[] buf = fullName.getBytes("UTF-8");
         out.writeInt(buf.length);
         out.write(buf);
      }

      /**
       * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
       */
      public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
      {
         byte[] buf = new byte[in.readInt()];
         in.readFully(buf);
         fullName = new String(buf, "UTF-8");
      }
   }

   public static class GetSizeMapper<K, V> extends AbstractExoCacheMapper<K, V, String, Integer>
   {

      public GetSizeMapper()
      {
      }

      public GetSizeMapper(String fullName)
      {
         super(fullName);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void _map(CacheKey<K> key, V value, Collector<String, Integer> collector)
      {
         collector.emit("total", Integer.valueOf(1));
      }

   }

   public static class GetSizeReducer<K> implements Reducer<K, Integer>
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = -5264142863835473112L;

      /**
       * @see org.infinispan.distexec.mapreduce.Reducer#reduce(java.lang.Object, java.util.Iterator)
       */
      @Override
      public Integer reduce(K reducedKey, Iterator<Integer> iter)
      {
         int sum = 0;
         while (iter.hasNext())
         {
            Integer i = iter.next();
            sum += i;
         }
         return sum;
      }
   }

   public static class GetCachedObjectsMapper<K, V> extends AbstractExoCacheMapper<K, V, String, List<V>>
   {

      public GetCachedObjectsMapper()
      {
      }

      public GetCachedObjectsMapper(String fullName)
      {
         super(fullName);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void _map(CacheKey<K> key, V value, Collector<String, List<V>> collector)
      {
         collector.emit("values", Collections.singletonList(value));
      }

   }

   public static class GetCachedObjectsReducer<K, V> implements Reducer<K, List<V>>
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = 8069024420056440405L;

      /**
       * @see org.infinispan.distexec.mapreduce.Reducer#reduce(java.lang.Object, java.util.Iterator)
       */
      @Override
      public List<V> reduce(K reducedKey, Iterator<List<V>> iter)
      {
         List<V> values = new ArrayList<V>();
         while (iter.hasNext())
         {
            List<V> vals = iter.next();
            values.addAll(vals);
         }
         return values;
      }
   }

   public static class ClearCacheMapper<K, V> extends AbstractExoCacheMapper<K, V, String, CacheKey<K>>
   {

      public ClearCacheMapper()
      {
      }

      public ClearCacheMapper(String fullName)
      {
         super(fullName);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void _map(CacheKey<K> key, V value, Collector<String, CacheKey<K>> collector)
      {
         collector.emit("keys", key);
      }

   }

   public static class ClearCacheReducer<K, V, KIn> implements Reducer<K, CacheKey<KIn>>
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = -8111087186325793256L;

      /**
       * @see org.infinispan.distexec.mapreduce.Reducer#reduce(java.lang.Object, java.util.Iterator)
       */
      @Override
      public CacheKey<KIn> reduce(K reducedKey, Iterator<CacheKey<KIn>> iter)
      {
         CacheKey<KIn> firstKey;
         if (iter == null || !iter.hasNext() || (firstKey = iter.next()) == null)
         {
            return null;
         }
         ExoContainer container = ExoContainerContext.getTopContainer();
         if (container == null)
         {
            LOG.error("The top container could not be found");
            return null;
         }
         DistributedCacheManager dcm =
            (DistributedCacheManager)container.getComponentInstanceOfType(DistributedCacheManager.class);
         if (dcm == null)
         {
            LOG.error("The DistributedCacheManager could not be found at top container level, please configure it.");
            return null;
         }
         Cache<CacheKey<K>, V> cache = dcm.getCache(CACHE_NAME);
         final LockManager lm = cache.getAdvancedCache().getLockManager();
         // Sort the keys to prevent deadlocks
         Set<CacheKey<KIn>> keys = new TreeSet<CacheKey<KIn>>(new Comparator<CacheKey<KIn>>()
         {
            public int compare(CacheKey<KIn> o1, CacheKey<KIn> o2)
            {
               int result = lm.getLockId(o1) - lm.getLockId(o2);
               return result == 0 ? System.identityHashCode(o1) - System.identityHashCode(o2) : result;
            }
         });
         keys.add(firstKey);
         while (iter.hasNext())
         {
            keys.add(iter.next());
         }
         for (CacheKey<KIn> key : keys)
         {
            cache.getAdvancedCache().withFlags(Flag.SKIP_REMOTE_LOOKUP, Flag.FAIL_SILENTLY).remove(key);
         }
         return null;
      }
   }

   public static class GetEntriesMapper<K, V> extends AbstractExoCacheMapper<K, V, K, V>
   {
      public GetEntriesMapper()
      {
      }

      public GetEntriesMapper(String fullName)
      {
         super(fullName);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      protected void _map(CacheKey<K> key, V value, Collector<K, V> collector)
      {
         collector.emit(key.getKey(), value);
      }
   }

   public static class GetEntriesReducer<K, V> implements Reducer<K, V>
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = 5153826700048219537L;

      /**
       * @see org.infinispan.distexec.mapreduce.Reducer#reduce(java.lang.Object, java.util.Iterator)
       */
      @Override
      public V reduce(K reducedKey, Iterator<V> iter)
      {
         return iter == null || !iter.hasNext() ? null : iter.next();
      }
   }
}