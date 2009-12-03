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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.exoplatform.services.cache.CacheListener;
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
import org.jboss.cache.notifications.annotation.NodeCreated;
import org.jboss.cache.notifications.annotation.NodeEvicted;
import org.jboss.cache.notifications.annotation.NodeModified;
import org.jboss.cache.notifications.annotation.NodeRemoved;
import org.jboss.cache.notifications.event.EventImpl;
import org.jboss.cache.notifications.event.NodeEvent;

/**
 * An {@link org.exoplatform.services.cache.ExoCache} implementation based on {@link org.jboss.cache.Node}.
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 20 juil. 2009  
 */
public abstract class AbstractExoCache implements ExoCache
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger(AbstractExoCache.class);

   protected final AtomicInteger size = new AtomicInteger();

   private volatile int hits;

   private volatile int misses;

   private String label;

   private String name;

   private boolean distributed;

   private boolean replicated;

   private boolean logEnabled;

   private final CopyOnWriteArrayList<CacheListener> listeners;

   protected final CacheSPI<Serializable, Object> cache;

   @SuppressWarnings("unchecked")
   public AbstractExoCache(ExoCacheConfig config, CacheSPI<Serializable, Object> cache)
   {
      this.cache = cache;
      this.listeners = new CopyOnWriteArrayList<CacheListener>();
      setDistributed(config.isDistributed());
      setLabel(config.getLabel());
      setName(config.getName());
      setLogEnabled(config.isLogEnabled());
      setReplicated(config.isRepicated());
      cache.getConfiguration().setInvocationBatchingEnabled(true);
      cache.addCacheListener(new SizeManager());
   }

   /**
    * {@inheritDoc}
    */
   public void addCacheListener(CacheListener listener)
   {
      if (listener == null)
      {
         return;
      }
      listeners.add(listener);
   }

   /**
    * {@inheritDoc}
    */
   public void clearCache() throws Exception
   {
      final Node<Serializable, Object> rootNode = cache.getRoot();
      for (Node<Serializable, Object> node : rootNode.getChildren())
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
   public Object get(Serializable name) throws Exception
   {
      final Object result = cache.get(Fqn.fromElements(name), name);
      if (result == null)
      {
         misses++;
      }
      else
      {
         hits++;
      }
      onGet(name, result);
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public int getCacheHit()
   {
      return hits;
   }

   /**
    * {@inheritDoc}
    */
   public int getCacheMiss()
   {
      return misses;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public int getCacheSize()
   {
      return cache.getNumberOfNodes();
   }

   /**
    * {@inheritDoc}
    */
   public List<Object> getCachedObjects()
   {
      final LinkedList<Object> list = new LinkedList<Object>();
      for (Node<Serializable, Object> node : cache.getRoot().getChildren())
      {
         if (node == null)
         {
            continue;
         }
         final Object value = node.get(getKey(node));
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
   public void put(Serializable name, Object obj) throws Exception
   {
      putOnly(name, obj);
      onPut(name, obj);
   }

   /**
    * Only puts the data into the cache nothing more
    */
   private Object putOnly(Serializable name, Object obj) throws Exception
   {
      return cache.put(Fqn.fromElements(name), name, obj);
   }

   /**
    * {@inheritDoc}
    */
   public void putMap(Map<Serializable, Object> objs) throws Exception
   {
      cache.startBatch();
      int total = 0;
      try
      {
         // Start transaction
         for (Entry<Serializable, Object> entry : objs.entrySet())
         {
            Object value = putOnly(entry.getKey(), entry.getValue());
            if (value == null)
            {
               total++;
            }
         }
         cache.endBatch(true);
         // End transaction
         for (Entry<Serializable, Object> entry : objs.entrySet())
         {
            onPut(entry.getKey(), entry.getValue());
         }
      }
      catch (Exception e)
      {
         cache.endBatch(false);
         throw e;
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object remove(Serializable name) throws Exception
   {
      final Fqn<Serializable> fqn = Fqn.fromElements(name);
      final Node<Serializable, Object> node = cache.getNode(fqn);
      if (node != null)
      {
         final Object result = node.get(name);
         if (cache.removeNode(fqn))
         {
            onRemove(name, result);
         }
         return result;
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public List<Object> removeCachedObjects() throws Exception
   {
      final List<Object> list = getCachedObjects();
      clearCache();
      return list;
   }

   /**
    * {@inheritDoc}
    */
   public void select(CachedObjectSelector selector) throws Exception
   {
      for (Node<Serializable, Object> node : cache.getRoot().getChildren())
      {
         if (node == null)
         {
            continue;
         }
         final Serializable key = getKey(node);
         final Object value = node.get(key);
         ObjectCacheInfo info = new ObjectCacheInfo()
         {
            public Object get()
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
   private Serializable getKey(Node<Serializable, Object> node)
   {
      return getKey(node.getFqn());
   }

   /**
    * Returns the key related to the given Fqn
    */
   @SuppressWarnings("unchecked")
   private Serializable getKey(Fqn fqn)
   {
      return (Serializable)fqn.get(0);
   }

   void onExpire(Serializable key, Object obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener listener : listeners)
      {
         try
         {
            listener.onExpire(this, key, obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   void onRemove(Serializable key, Object obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener listener : listeners)
      {
         try
         {
            listener.onRemove(this, key, obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   void onPut(Serializable key, Object obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener listener : listeners)
         try
         {
            listener.onPut(this, key, obj);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
   }

   void onGet(Serializable key, Object obj)
   {
      if (listeners.isEmpty())
      {
         return;
      }
      for (CacheListener listener : listeners)
         try
         {
            listener.onGet(this, key, obj);
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
      for (CacheListener listener : listeners)
         try
         {
            listener.onClearCache(this);
         }
         catch (Exception e)
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
   }

   @org.jboss.cache.notifications.annotation.CacheListener
   public class SizeManager
   {

      @NodeEvicted
      public void nodeEvicted(NodeEvent ne)
      {
         if (ne.isPre())
         {
            // Cannot give the value since
            // since it disturbs the eviction
            // algorithms
            onExpire(getKey(ne.getFqn()), null);
         }
      }

      @NodeRemoved
      public void nodeRemoved(NodeEvent ne)
      {
         if (ne.isPre())
         {
            if (!ne.isOriginLocal())
            {
               final Node<Serializable, Object> node = cache.getNode(ne.getFqn());
               final Serializable key = getKey(ne.getFqn());
               onRemove(key, node.get(key));
            }
         }
      }

      @NodeCreated
      public void nodeCreated(NodeEvent ne)
      {
         size.incrementAndGet();
      }

      @SuppressWarnings("unchecked")
      @NodeModified
      public void nodeModified(NodeEvent ne)
      {
         if (!ne.isOriginLocal() && !ne.isPre())
         {
            final Serializable key = getKey(ne.getFqn());
            if (ne instanceof EventImpl)
            {
               EventImpl evt = (EventImpl)ne;
               Map<Serializable, Object> data = evt.getData();
               if (data != null)
               {
                  onPut(key, data.get(key));
                  return;
               }
            }
            final Node<Serializable, Object> node = cache.getNode(ne.getFqn());
            onPut(key, node.get(key));
         }
      }
   }
}
