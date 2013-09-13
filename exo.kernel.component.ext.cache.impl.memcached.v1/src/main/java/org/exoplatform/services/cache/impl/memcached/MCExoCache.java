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
package org.exoplatform.services.cache.impl.memcached;

import net.spy.memcached.CASValue;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.internal.OperationFuture;

import org.apache.ws.commons.util.Base64;
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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * An {@link org.exoplatform.services.cache.ExoCache} implementation based on spymemcached.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public class MCExoCache<K extends Serializable, V> implements ExoCache<K, V>
{

   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger//NOSONAR
      .getLogger("exo.kernel.component.ext.cache.impl.memcached.v1.AbstractExoCache");//NOSONAR

   private final AtomicInteger hits = new AtomicInteger(0);

   private final AtomicInteger misses = new AtomicInteger(0);

   private final AtomicInteger count = new AtomicInteger(0);

   private final AtomicReference<String> lastNamespace = new AtomicReference<String>();

   private String label;

   private String name;

   private final String fullName;

   private boolean distributed;

   private boolean replicated;

   private boolean logEnabled;

   private int expirationTimeout;

   protected final MemcachedClient cache;

   @SuppressWarnings("rawtypes")
   private static final ConcurrentMap<String, List<ListenerContext>> ALL_LISTENERS =
      new ConcurrentHashMap<String, List<ListenerContext>>();

   public MCExoCache(ExoContainerContext ctx, ExoCacheConfig config, MemcachedClient cache, long expirationTimeout)
   {
      this.fullName = ctx.getName() + "-" + config.getName();
      this.cache = cache;
      this.expirationTimeout = (int)(expirationTimeout / 1000L);
      setDistributed(config.isDistributed());
      setLabel(config.getLabel());
      setName(config.getName());
      setLogEnabled(config.isLogEnabled());
      setReplicated(config.isRepicated());
   }

   /**
    * @return the fullName
    */
   String getFullName()
   {
      return fullName;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   public void addCacheListener(CacheListener<? super K, ? super V> listener)
   {
      if (listener == null)
      {
         throw new IllegalArgumentException("The listener cannot be null");
      }
      List<ListenerContext> lListeners = getOrCreateListeners();
      lListeners.add(new ListenerContext<K, V>(listener, this));
   }

   @SuppressWarnings("rawtypes")
   private List<ListenerContext> getOrCreateListeners()
   {
      List<ListenerContext> lListeners = getListeners();
      if (lListeners == null)
      {
         lListeners = new CopyOnWriteArrayList<ListenerContext>();
         List<ListenerContext> oldValue = ALL_LISTENERS.putIfAbsent(fullName, lListeners);
         if (oldValue != null)
         {
            lListeners = oldValue;
         }
      }
      return lListeners;
   }

   @SuppressWarnings("rawtypes")
   private List<ListenerContext> getListeners()
   {
      return ALL_LISTENERS.get(fullName);
   }

   /**
    * Tries at worse 3 times to get the namespace
    * @return
    */
   private String getNamespace()
   {
      return getNamespace(3);
   }

   /**
    * Gives the namespace to use as prefix for our key in order to allow invalidation of a cache
    * @param triesLeft the total amount of tries left in case of a failure
    * @return the namespace
    */
   private String getNamespace(int triesLeft)
   {
      String oldNamespace = lastNamespace.get();
      CASValue<Object> casValue = cache.getAndTouch(fullName, expirationTimeout);
      String value;
      if (casValue == null || casValue.getValue() == null)
      {
         value = UUID.randomUUID().toString();
         OperationFuture<Boolean> resp = cache.add(fullName, expirationTimeout, value);
         Boolean result = null;
         try
         {
            result = resp.get();
         }
         catch (InterruptedException e)
         {
            LOG.error("Could not get the namespace", e);
         }
         catch (ExecutionException e)
         {
            LOG.error("Could not get the namespace", e);
         }
         if (result == null || !result.booleanValue())
         {
            if (result == null && triesLeft == 0)
            {
               throw new RuntimeException("The namespace could not be found");
            }
            LOG.debug("Could not get the namespace, so we need to retry");
            return getNamespace(triesLeft - 1);
         }
      }
      else
      {
         value = (String)casValue.getValue();
      }
      if (lastNamespace.compareAndSet(oldNamespace, value) && oldNamespace != null && !oldNamespace.equals(value))
      {
         // The namespace has changed so we reset the counter as it could be due to
         // a remote clear cache
         count.set(0);
      }
      return value;
   }

   /**
    * Gives the name of the key with the prefix
    * @param name the name of the key without the prefix
    * @return the full name of the key
    */
   private String getKeyFullName(Serializable name)
   {
      return getKeyFullName(getNamespace(), name);
   }

   /**
    * Gives the name of the key with the prefix
    * 
    * @param namespace the namespace to use
    * @param name the name of the key without the prefix
    * @return the full name of the key
    */
   private String getKeyFullName(String namespace, Serializable name)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(namespace);
      sb.append(':');
      sb.append(toString(name));
      return sb.toString();
   }

   /**
    * Used to serialize the key
    * @param key the key to serialize
    * @return the value of the key serialized in Base64
    */
   private static String toString(Serializable key)
   {
      if (key instanceof String)
         return (String)key;
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = null;
      try
      {
         oos = new ObjectOutputStream(baos);
         oos.writeObject(key);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not serialize the key " + key, e);
      }
      finally
      {
         if (oos != null)
         {
            try
            {
               oos.close();
            }
            catch (IOException e)
            {
               LOG.trace("Could not close the object output stream", e);
            }
         }
      }
      return Base64.encode(baos.toByteArray());
   }

   /**
    * {@inheritDoc}
    */
   public void clearCache()
   {
      // As it is not possible to clear a particular cache, we simply change the namespace as
      // described in the doc https://code.google.com/p/memcached/wiki/NewProgrammingTricks#Namespacing
      String namespace = UUID.randomUUID().toString();
      String oldNamespace = lastNamespace.get();
      OperationFuture<Boolean> resp = cache.set(fullName, expirationTimeout, namespace);
      Boolean result;
      try
      {
         result = resp.get();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not clear the cache ", e);
      }
      if (result != null && result.booleanValue())
      {
         lastNamespace.compareAndSet(oldNamespace, namespace);
         count.set(0);
         onClearCache();
      }
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
      CASValue<Object> casValue = cache.getAndTouch(getKeyFullName(name), expirationTimeout);
      V result = casValue == null ? null : (V)casValue.getValue();
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
   @Managed
   @ManagedName("Size")
   @ManagedDescription("The local cache size as it is not possible to get the global cache size")
   public int getCacheSize()
   {
      return count.get();
   }

   /**
    * {@inheritDoc}
    */
   public List<V> getCachedObjects()
   {
      throw new UnsupportedOperationException("Cannot get the cached objects");
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
      else if (value == null)
      {
         // ignore null values
         return;
      }
      putOnly(getNamespace(), key, value);
      onPut(key, value);
   }

   /**
    * Only puts the data into the cache nothing more
    */
   protected void putOnly(String namespace, K key, V value)
   {
      OperationFuture<Boolean> resp = cache.add(getKeyFullName(namespace, key), expirationTimeout, value);
      Boolean result;
      try
      {
         result = resp.get();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not add the new value for the key " + key, e);
      }
      if (result == null || !result.booleanValue())
      {
         // The value already exists in the cache so we simply replace it
         resp = cache.replace(getKeyFullName(namespace, key), expirationTimeout, value);
         try
         {
            result = resp.get();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Could not replace the old value of the key " + key, e);
         }
         if (result == null || !result.booleanValue())
         {
            // we try again
            putOnly(namespace, key, value);
         }
      }
      else if (namespace.equals(lastNamespace.get()))
      {
         // A new value has been added and the namespace has not been modified during the process
         count.incrementAndGet();
      }
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
      try
      {
         String namespace = getNamespace();
         for (Map.Entry<? extends K, ? extends V> entry : objs.entrySet())
         {
            putOnly(namespace, entry.getKey(), entry.getValue());
            onPut(entry.getKey(), entry.getValue());
         }
      }
      catch (Exception e)//NOSONAR
      {
         LOG.warn("An error occurs while executing the putMap method", e);
      }
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
      String namespace = getNamespace();
      V value = (V)cache.get(getKeyFullName(namespace, name));
      OperationFuture<Boolean> resp = cache.delete(getKeyFullName(namespace, name));
      Boolean result;
      try
      {
         result = resp.get();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not remove the value for the key " + name, e);
      }
      if (result != null && result.booleanValue())
      {
         if (namespace.equals(lastNamespace.get()))
         {
            // The value has been removed successfully and the namespace has not been modified during the process
            count.decrementAndGet();
         }
         onRemove((K)name, value);
      }
      return value;
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
      throw new UnsupportedOperationException("Cannot select a sub part of the cache dynamically");
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
   void onExpire(K key, V obj)
   {
      List<ListenerContext> listeners = getListeners();
      if (listeners == null || listeners.isEmpty())
      {
         return;
      }
      for (ListenerContext context : listeners)
      {
         try
         {
            context.onExpire(key, obj);
         }
         catch (Exception e)//NOSONAR
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   void onRemove(K key, V obj)
   {
      List<ListenerContext> listeners = getListeners();
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
         catch (Exception e)//NOSONAR
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   void onPut(K key, V obj)
   {
      List<ListenerContext> listeners = getListeners();
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
         catch (Exception e)//NOSONAR
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   void onGet(K key, V obj)
   {
      List<ListenerContext> listeners = getListeners();
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
         catch (Exception e)//NOSONAR
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
         }
      }
   }

   @SuppressWarnings("rawtypes")
   void onClearCache()
   {
      List<ListenerContext> listeners = getListeners();
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
         catch (Exception e)//NOSONAR
         {
            if (LOG.isWarnEnabled())
               LOG.warn("Cannot execute the CacheListener properly", e);
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

   @Managed
   @ManagedName("ExpirationTimeout")
   @ManagedDescription("This is the timeout after which the cache entry must be evicted.")
   public long getExpirationTimeout()
   {
      return expirationTimeout;
   }

   @Managed
   public void setExpirationTimeout(long expirationTimeout)
   {
      this.expirationTimeout = (int)(expirationTimeout / 1000L);
   }

   public void setMaxSize(int max)
   {
      throw new UnsupportedOperationException("The max size cannot be modified");
   }

   public void setLiveTime(long period)
   {
      this.expirationTimeout = (int)period;
   }

   public int getMaxSize()
   {
      return -1;
   }

   public long getLiveTime()
   {
      return expirationTimeout;
   }
}
