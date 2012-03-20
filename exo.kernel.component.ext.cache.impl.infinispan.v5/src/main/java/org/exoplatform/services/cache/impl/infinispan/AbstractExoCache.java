/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.services.cache.impl.infinispan;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.cache.CacheInfo;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.context.Flag;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.cachelistener.annotation.CacheEntriesEvicted;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryModified;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntriesEvictedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryModifiedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;

import java.io.Serializable;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link org.exoplatform.services.cache.ExoCache} implementation based on {@link Cache}.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractExoCache<K extends Serializable, V> implements ExoCache<K, V>
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.ext.cache.impl.infinispan.v5.AbstractExoCache");

   private final AtomicInteger hits = new AtomicInteger(0);

   private final AtomicInteger misses = new AtomicInteger(0);

   private String label;

   private String name;

   private boolean distributed;

   private boolean replicated;

   private boolean logEnabled;

   private final CopyOnWriteArrayList<ListenerContext<K, V>> listeners;

   protected final AdvancedCache<K, V> cache;

   public AbstractExoCache(ExoCacheConfig config, Cache<K, V> cache)
   {
      this.cache = cache.getAdvancedCache();
      this.listeners = new CopyOnWriteArrayList<ListenerContext<K, V>>();
      setDistributed(config.isDistributed());
      setLabel(config.getLabel());
      setName(config.getName());
      setLogEnabled(config.isLogEnabled());
      setReplicated(config.isRepicated());
      cache.addListener(new CacheEventListener());
   }

   /**
    * {@inheritDoc}
    */
   public void addCacheListener(CacheListener<? super K, ? super V> listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("The listener cannot be null");
      }
      listeners.add(new ListenerContext<K, V>(listener, this));
   }

   /**
    * {@inheritDoc}
    */
   public void clearCache()
   {
      cache.withFlags(Flag.CACHE_MODE_LOCAL).clear();
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
      final V result = cache.get(name);
      if (result == null)
      {
         misses.incrementAndGet();
      }
      else
      {
         hits.incrementAndGet();
      }
      onGet((K)name, result);
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
      return cache.size();
   }

   /**
    * {@inheritDoc}
    */
   public List<V> getCachedObjects()
   {
      Collection<V> values = cache.values();
      if (values == null || values.isEmpty())
      {
         return Collections.emptyList();
      }
      else
      {
         return new ArrayList<V>(values);
      }
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
      cache.withFlags(Flag.SKIP_REMOTE_LOOKUP).put(key, value);
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
   public V remove(final Serializable name) throws NullPointerException
   {
      if (name == null)
      {
         throw new IllegalArgumentException("No null cache key accepted");
      }      
      V result = SecurityHelper.doPrivilegedAction(new PrivilegedAction<V>()
      {

         @Override
         public V run()
         {
            return cache.remove(name);
         }
      });
      onRemove((K)name, result);
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
      for (K key : cache.keySet())
      {
         if (key == null)
         {
            continue;
         }
         final V value = cache.withFlags(Flag.SKIP_LOCKING).get(key);
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

   void onExpire(K key, V obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext<K, V> context : listeners)
      {
         try
         {
            context.onExpire(key, obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   void onRemove(K key, V obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext<K, V> context : listeners)
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

   void onPut(K key, V obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext<K, V> context : listeners)
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

   void onGet(K key, V obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext<K, V> context : listeners)
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

   void onClearCache()
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext<K, V> context : listeners)
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

   @Listener
   public class CacheEventListener
   {
      /**
       * Warning Infinispan triggers a <code>CacheEntryEvictedEvent</code> only at explicit eviction
       * that is done lazily which is not exactly what we expect, we still use it to be 
       * able to use it with <code>avoidValueReplication</code> set to <code>true</code>.
       */
      @CacheEntriesEvicted
      public void cacheEntryEvicted(CacheEntriesEvictedEvent<K, V> evt)
      {
         if (evt.isPre())
         {
            for (Map.Entry<K, V> entry : evt.getEntries().entrySet())
            {
               onExpire(entry.getKey(), entry.getValue());               
            }
         }
      }

      @CacheEntryRemoved
      public void cacheEntryRemoved(CacheEntryRemovedEvent<K, V> evt)
      {
         if (evt.isPre() && !evt.isOriginLocal())
         {
            final K key = evt.getKey();
            final V value = evt.getValue();
            onRemove(key, value);
         }
      }

      @CacheEntryModified
      public void cacheEntryModified(CacheEntryModifiedEvent<K, V> evt)
      {
         if (!evt.isOriginLocal() && !evt.isPre())
         {
            final K key = evt.getKey();
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
}