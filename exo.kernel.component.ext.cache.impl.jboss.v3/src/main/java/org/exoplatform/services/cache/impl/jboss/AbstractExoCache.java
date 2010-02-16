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
package org.exoplatform.services.cache.impl.jboss;

import org.exoplatform.services.cache.CacheInfo;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheSPI;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.NodeSPI;
import org.jboss.cache.notifications.annotation.NodeEvicted;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.event.NodeEvictedEvent;
import org.jboss.cache.notifications.event.NodeModifiedEvent;
import org.jboss.cache.notifications.event.NodeRemovedEvent;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An {@link org.exoplatform.services.cache.ExoCache} implementation based on {@link org.jboss.cache.Node}.
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 20 juil. 2009  
 */
public abstract class AbstractExoCache<K extends Serializable, V> implements ExoCache<K, V>
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger(AbstractExoCache.class);

   private final AtomicInteger hits = new AtomicInteger(0);

   private final AtomicInteger misses = new AtomicInteger(0);

   private String label;

   private String name;

   private boolean distributed;

   private boolean replicated;

   private boolean logEnabled;

   private final CopyOnWriteArrayList<ListenerContext<K, V>> listeners;

   protected final CacheSPI<K, V> cache;

   public AbstractExoCache(ExoCacheConfig config, Cache<K, V> cache)
   {
      this.cache = (CacheSPI<K, V>)cache;
      this.listeners = new CopyOnWriteArrayList<ListenerContext<K, V>>();
      setDistributed(config.isDistributed());
      setLabel(config.getLabel());
      setName(config.getName());
      setLogEnabled(config.isLogEnabled());
      setReplicated(config.isRepicated());
      cache.getConfiguration().setInvocationBatchingEnabled(true);
      cache.addCacheListener(new CacheEventListener());
   }

   /**
    * {@inheritDoc}
    */
   public void addCacheListener(CacheListener<? super K, ? super V> listener)
   {
      if (listener == null)
      {
         throw new NullPointerException();
      }
      listeners.add(new ListenerContext<K, V>(listener, this));
   }

   /**
    * {@inheritDoc}
    */
   public void clearCache()
   {
      final Node<K, V> rootNode = cache.getRoot();
      for (Node<K, V> node : rootNode.getChildren())
      {
         if (node == null)
         {
            continue;
         }
         remove(getKey(node));
      }
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
      final V result = cache.get(getFqn(name), (K)name);
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
      return cache.getNumberOfNodes();
   }

   /**
    * {@inheritDoc}
    */
   public List<V> getCachedObjects()
   {
      final LinkedList<V> list = new LinkedList<V>();
      for (Node<K, V> node : cache.getRoot().getChildren())
      {
         if (node == null)
         {
            continue;
         }
         final V value = node.get(getKey(node));
         if (value != null)
         {
            list.add(value);
         }
      }
      return list;
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
   public void put(K key, V value) throws NullPointerException
   {
      if (key == null)
      {
         throw new NullPointerException("No null cache key accepted");
      }      
      putOnly(key, value);
      onPut(key, value);
   }

   /**
    * Only puts the data into the cache nothing more
    */
   private V putOnly(K key, V value)
   {
      return cache.put(getFqn(key), key, value);
   }

   /**
    * {@inheritDoc}
    */
   public void putMap(Map<? extends K, ? extends V> objs) throws NullPointerException, IllegalArgumentException
   {
      if (objs == null)
      {
         throw new NullPointerException("No null map accepted");
      }
      for (Serializable name : objs.keySet())
      {
         if (name == null)
         {
            throw new IllegalArgumentException("No null cache key accepted");
         }
      }      
      cache.startBatch();
      int total = 0;
      try
      {
         // Start transaction
         for (Map.Entry<? extends K, ? extends V> entry : objs.entrySet())
         {
            V value = putOnly(entry.getKey(), entry.getValue());
            if (value == null)
            {
               total++;
            }
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
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public V remove(Serializable name) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null cache key accepted");
      }      
      final Fqn<Serializable> fqn = getFqn(name);
      // We use the methods peek and getDirect to avoid going through the interceptor chain
      // in order to avoid to visit nodes that were about to be evicted      
      final NodeSPI<K, V> node = cache.peek(fqn, false);
      V result = null;
      if (node != null)
      {
         result = node.getDirect((K)name);
         if (cache.removeNode(fqn))
         {
            onRemove((K)name, result);
         }
      }
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
      for (Node<K, V> node : cache.getRoot().getChildren())
      {
         if (node == null)
         {
            continue;
         }
         final K key = getKey(node);
         // We use the method getDirect to avoid going through the interceptor chain
         // in order to avoid to visit nodes that were about to be evicted               
         final V value = ((NodeSPI<K, V>)node).getDirect(key);
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

   /**
    * Returns the key related to the given node
    */
   private K getKey(Node<K, V> node)
   {
      return getKey(node.getFqn());
   }

   /**
    * Returns the key related to the given Fqn
    */
   @SuppressWarnings("unchecked")
   private K getKey(Fqn fqn)
   {
      return (K)fqn.get(0);
   }

   /**
    * Returns the Fqn related to the given name
    */
   private Fqn<Serializable> getFqn(Serializable name)
   {
      return Fqn.fromElements(name);
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

   @org.jboss.cache.notifications.annotation.CacheListener
   public class CacheEventListener
   {

      @NodeEvicted
      public void nodeEvicted(NodeEvictedEvent ne)
      {
         if (ne.isPre())
         {
            final NodeSPI<K, V> node = cache.peek(ne.getFqn(), true);
            final K key = getKey(ne.getFqn());
            onExpire(key, node == null ? null : node.getDirect(key));
         }
      }

      @SuppressWarnings("unchecked")
      @NodeRemoved
      public void nodeRemoved(NodeRemovedEvent ne)
      {
         if (ne.isPre() && !ne.isOriginLocal())
         {
            final K key = getKey(ne.getFqn());
            final Map<K, V> data = ne.getData();
            onRemove(key, data == null ? null : data.get(key));
         }
      }

      @SuppressWarnings("unchecked")
      @NodeModified
      public void nodeModified(NodeModifiedEvent ne)
      {
         if (!ne.isOriginLocal() && !ne.isPre())
         {
            final K key = getKey(ne.getFqn());
            final Map<K, V> data = ne.getData();
            onPut(key, data == null ? null : data.get(key));
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
