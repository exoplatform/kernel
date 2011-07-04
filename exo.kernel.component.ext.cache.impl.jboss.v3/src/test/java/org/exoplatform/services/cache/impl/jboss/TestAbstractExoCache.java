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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.cache.impl.InvalidationExoCache;
import org.exoplatform.services.cache.impl.jboss.lru.LRUExoCacheCreator;
import org.exoplatform.test.BasicTestCase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 21 juil. 2009  
 */
public class TestAbstractExoCache extends BasicTestCase
{

   CacheService service;

   AbstractExoCache<Serializable, Object> cache;

   ExoCacheFactory factory;

   public TestAbstractExoCache(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      this.service = (CacheService)PortalContainer.getInstance().getComponentInstanceOfType(CacheService.class);
      this.cache = (AbstractExoCache<Serializable, Object>)service.getCacheInstance("myCache");
      this.factory = (ExoCacheFactory)PortalContainer.getInstance().getComponentInstanceOfType(ExoCacheFactory.class);
   }

   protected void tearDown() throws Exception
   {
      cache.clearCache();
   }

   public void testPut() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      assertEquals(3, cache.getCacheSize());
      cache.put(new MyKey("a"), "c");
      assertEquals(3, cache.getCacheSize());
      cache.put(new MyKey("d"), "c");
      assertEquals(4, cache.getCacheSize());
   }

   public void testClearCache() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      assertTrue(cache.getCacheSize() > 0);
      cache.clearCache();
      assertTrue(cache.getCacheSize() == 0);
   }

   public void testGet() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      assertEquals("a", cache.get(new MyKey("a")));
      cache.put(new MyKey("a"), "c");
      assertEquals("c", cache.get(new MyKey("a")));
      cache.remove(new MyKey("a"));
      assertEquals(null, cache.get(new MyKey("a")));
      assertEquals(null, cache.get(new MyKey("x")));
   }

   public void testRemove() throws Exception
   {
      cache.put(new MyKey("a"), 1);
      cache.put(new MyKey("b"), 2);
      cache.put(new MyKey("c"), 3);
      assertEquals(3, cache.getCacheSize());
      assertEquals(1, cache.remove(new MyKey("a")));
      assertEquals(2, cache.getCacheSize());
      assertEquals(2, cache.remove(new MyKey("b")));
      assertEquals(1, cache.getCacheSize());
      assertEquals(null, cache.remove(new MyKey("x")));
      assertEquals(1, cache.getCacheSize());
   }

   public void testPutMap() throws Exception
   {
      Map<Serializable, Object> values = new HashMap<Serializable, Object>();
      values.put(new MyKey("a"), "a");
      values.put(new MyKey("b"), "b");
      assertEquals(0, cache.getCacheSize());
      cache.putMap(values);
      assertEquals(2, cache.getCacheSize());
      values = new HashMap<Serializable, Object>()
      {
         private static final long serialVersionUID = 1L;

         public Set<Entry<Serializable, Object>> entrySet()
         {
            Set<Entry<Serializable, Object>> set = new LinkedHashSet<Entry<Serializable, Object>>(super.entrySet());
            set.add(new Entry<Serializable, Object>()
            {

               public Object setValue(Object paramV)
               {
                  return null;
               }

               public Object getValue()
               {
                  throw new RuntimeException("An exception");
               }

               public Serializable getKey()
               {
                  return "c";
               }
            });
            return set;
         }
      };
      values.put(new MyKey("e"), "e");
      values.put(new MyKey("d"), "d");
      cache.putMap(values);
      assertEquals(2, cache.getCacheSize());
   }

   public void testGetCachedObjects() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      cache.put(new MyKey("d"), null);
      assertEquals(4, cache.getCacheSize());
      List<Object> values = cache.getCachedObjects();
      assertEquals(3, values.size());
      assertTrue(values.contains("a"));
      assertTrue(values.contains("b"));
      assertTrue(values.contains("c"));
   }

   public void testRemoveCachedObjects() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      cache.put(new MyKey("d"), null);
      assertEquals(4, cache.getCacheSize());
      List<Object> values = cache.removeCachedObjects();
      assertEquals(3, values.size());
      assertTrue(values.contains("a"));
      assertTrue(values.contains("b"));
      assertTrue(values.contains("c"));
      assertEquals(0, cache.getCacheSize());
   }

   public void testSelect() throws Exception
   {
      cache.put(new MyKey("a"), 1);
      cache.put(new MyKey("b"), 2);
      cache.put(new MyKey("c"), 3);
      final AtomicInteger count = new AtomicInteger();
      CachedObjectSelector<Serializable, Object> selector = new CachedObjectSelector<Serializable, Object>()
      {

         public void onSelect(ExoCache<? extends Serializable, ? extends Object> cache, Serializable key,
            ObjectCacheInfo<? extends Object> ocinfo) throws Exception
         {
            assertTrue(key.equals(new MyKey("a")) || key.equals(new MyKey("b")) || key.equals(new MyKey("c")));
            assertTrue(ocinfo.get().equals(1) || ocinfo.get().equals(2) || ocinfo.get().equals(3));
            count.incrementAndGet();
         }

         public boolean select(Serializable key, ObjectCacheInfo<? extends Object> ocinfo)
         {
            return true;
         }
      };
      cache.select(selector);
      assertEquals(3, count.intValue());
   }

   public void testGetHitsNMisses() throws Exception
   {
      int hits = cache.getCacheHit();
      int misses = cache.getCacheMiss();
      cache.put(new MyKey("a"), "a");
      cache.get(new MyKey("a"));
      cache.remove(new MyKey("a"));
      cache.get(new MyKey("a"));
      cache.get(new MyKey("z"));
      assertEquals(1, cache.getCacheHit() - hits);
      assertEquals(2, cache.getCacheMiss() - misses);
   }

   private ExoCacheFactory getExoCacheFactoryInstance() throws ExoCacheInitException
   {
      PortalContainer pc = PortalContainer.getInstance();
      ExoCacheFactoryImpl factory =
         new ExoCacheFactoryImpl("jar:/conf/portal/cache-configuration-template.xml", (ConfigurationManager)pc
            .getComponentInstanceOfType(ConfigurationManager.class), Boolean.valueOf(
            System.getProperty("allow.shareable.cache")).booleanValue());
      InitParams params = new InitParams();
      ObjectParameter param = new ObjectParameter();
      param.setName("LRU");
      param.setObject(new LRUExoCacheCreator());
      params.addParam(param);
      ExoCacheCreatorPlugin plugin = new ExoCacheCreatorPlugin(params);
      factory.addCreator(plugin);
      return factory;
   }

   @SuppressWarnings("unchecked")
   public void testDistributedCache() throws Exception
   {
      System.out
         .println("WARNING: For Linux distributions the following JVM parameter must be set to true, java.net.preferIPv4Stack = "
            + System.getProperty("java.net.preferIPv4Stack"));
      ExoCacheConfig config = new ExoCacheConfig();
      config.setName("MyCacheDistributed");
      config.setMaxSize(5);
      config.setLiveTime(1);
      config.setImplementation("LRU");
      config.setDistributed(true);
      ExoCacheConfig config2 = new ExoCacheConfig();
      config2.setName("MyCacheDistributed2");
      config2.setMaxSize(5);
      config2.setLiveTime(1);
      config2.setImplementation("LRU");
      config2.setDistributed(true);
      AbstractExoCache<Serializable, String> cache1 =
         (AbstractExoCache<Serializable, String>)getExoCacheFactoryInstance().createCache(config);
      MyCacheListener<String> listener1 = new MyCacheListener<String>();
      cache1.addCacheListener(listener1);
      AbstractExoCache<Serializable, String> cache2 =
         (AbstractExoCache<Serializable, String>)getExoCacheFactoryInstance().createCache(config);
      MyCacheListener<String> listener2 = new MyCacheListener<String>();
      cache2.addCacheListener(listener2);
      AbstractExoCache<Serializable, String> cache3 =
         (AbstractExoCache<Serializable, String>)getExoCacheFactoryInstance().createCache(config2);
      MyCacheListener<String> listener3 = new MyCacheListener<String>();
      cache3.addCacheListener(listener3);
      try
      {
         cache1.put(new MyKey("a"), "b");
         assertEquals(1, cache1.getCacheSize());
         assertEquals("b", cache2.get(new MyKey("a")));
         assertEquals(1, cache2.getCacheSize());
         assertEquals(0, cache3.getCacheSize());
         assertEquals(1, listener1.put);
         assertEquals(1, listener2.put);
         assertEquals(0, listener3.put);
         assertEquals(0, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(0, listener3.get);
         cache2.put(new MyKey("b"), "c");
         assertEquals(2, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());
         assertEquals("c", cache1.get(new MyKey("b")));
         assertEquals(0, cache3.getCacheSize());
         assertEquals(2, listener1.put);
         assertEquals(2, listener2.put);
         assertEquals(0, listener3.put);
         assertEquals(1, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(0, listener3.get);
         cache3.put(new MyKey("c"), "d");
         assertEquals(2, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());
         assertEquals(1, cache3.getCacheSize());
         assertEquals("d", cache3.get(new MyKey("c")));
         assertEquals(2, listener1.put);
         assertEquals(2, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(1, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(1, listener3.get);
         cache2.put(new MyKey("a"), "a");
         assertEquals(2, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());
         assertEquals("a", cache1.get(new MyKey("a")));
         assertEquals(3, listener1.put);
         assertEquals(3, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(2, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(1, listener3.get);
         cache2.remove(new MyKey("a"));
         assertEquals(1, cache1.getCacheSize());
         assertEquals(1, cache2.getCacheSize());
         assertEquals(3, listener1.put);
         assertEquals(3, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(2, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         cache1.put(new MyKey("c"), "c");
         cache1.clearCache();
         assertEquals(0, cache1.getCacheSize());
         assertEquals(null, cache1.get(new MyKey("b")));
         assertEquals("c", cache2.get(new MyKey("b")));
         assertEquals("c", cache2.get(new MyKey("c")));
         assertEquals(2, cache2.getCacheSize());
         assertEquals(4, listener1.put);
         assertEquals(4, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(3, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         Map<Serializable, String> values = new HashMap<Serializable, String>();
         values.put(new MyKey("a"), "a");
         values.put(new MyKey("b"), "b");
         cache1.putMap(values);
         assertEquals(2, cache1.getCacheSize());
         Thread.sleep(40);
         assertEquals("a", cache2.get(new MyKey("a")));
         assertEquals("b", cache2.get(new MyKey("b")));
         assertEquals(3, cache2.getCacheSize());
         assertEquals(6, listener1.put);
         assertEquals(6, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         values = new HashMap<Serializable, String>()
         {
            private static final long serialVersionUID = 1L;

            public Set<Entry<Serializable, String>> entrySet()
            {
               Set<Entry<Serializable, String>> set = new LinkedHashSet<Entry<Serializable, String>>(super.entrySet());
               set.add(new Entry<Serializable, String>()
               {

                  public String setValue(String paramV)
                  {
                     return null;
                  }

                  public String getValue()
                  {
                     throw new RuntimeException("An exception");
                  }

                  public Serializable getKey()
                  {
                     return "c";
                  }
               });
               return set;
            }
         };
         values.put(new MyKey("e"), "e");
         values.put(new MyKey("d"), "d");
         cache1.putMap(values);
         assertEquals(2, cache1.getCacheSize());
         assertEquals(3, cache2.getCacheSize());
         assertEquals(1, cache3.getCacheSize());
         assertEquals(6, listener1.put);
         assertEquals(6, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         assertEquals(0, listener1.expire);
         assertEquals(0, listener2.expire);
         assertEquals(0, listener3.expire);
         Thread.sleep(1600);
         assertEquals(0, cache1.getCacheSize());
         assertEquals(0, cache2.getCacheSize());
         assertEquals(0, cache3.getCacheSize());
         assertEquals(6, listener1.put);
         assertEquals(6, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         assertEquals(2, listener1.expire);
         assertEquals(3, listener2.expire);
         assertEquals(1, listener3.expire);
      }
      finally
      {
         cache1.cache.stop();
         cache2.cache.stop();
         cache3.cache.stop();
      }
   }

   @SuppressWarnings("unchecked")
   public void testDistributedCacheWithNSValues() throws Exception
   {
      System.out
         .println("WARNING: For Linux distributions the following JVM parameter must be set to true, java.net.preferIPv4Stack = "
            + System.getProperty("java.net.preferIPv4Stack"));
      ExoCacheConfig config = new ExoCacheConfig();
      config.setName("MyCacheDistributedWithNSValues");
      config.setMaxSize(5);
      config.setLiveTime(1);
      config.setImplementation("LRU");
      config.setDistributed(true);
      config.setAvoidValueReplication(true);
      ExoCacheConfig config2 = new ExoCacheConfig();
      config2.setName("MyCacheDistributedWithNSValues2");
      config2.setMaxSize(5);
      config2.setLiveTime(1);
      config2.setImplementation("LRU");
      config2.setDistributed(true);
      config2.setAvoidValueReplication(true);
      AbstractExoCache<Serializable, MyNonSerializableValue> acache1 =
         (AbstractExoCache<Serializable, MyNonSerializableValue>)getExoCacheFactoryInstance().createCache(config);
      MyCacheListener<MyNonSerializableValue> listener1 = new MyCacheListener<MyNonSerializableValue>();
      ExoCache<Serializable, MyNonSerializableValue> cache1 = new InvalidationExoCache<Serializable, MyNonSerializableValue>(acache1);
      cache1.addCacheListener(listener1);
      AbstractExoCache<Serializable, MyNonSerializableValue> acache2 =
         (AbstractExoCache<Serializable, MyNonSerializableValue>)getExoCacheFactoryInstance().createCache(config);
      MyCacheListener<MyNonSerializableValue> listener2 = new MyCacheListener<MyNonSerializableValue>();
      ExoCache<Serializable, MyNonSerializableValue> cache2 = new InvalidationExoCache<Serializable, MyNonSerializableValue>(acache2);
      cache2.addCacheListener(listener2);
      AbstractExoCache<Serializable, MyNonSerializableValue> acache3 =
         (AbstractExoCache<Serializable, MyNonSerializableValue>)getExoCacheFactoryInstance().createCache(config2);
      MyCacheListener<MyNonSerializableValue> listener3 = new MyCacheListener<MyNonSerializableValue>();
      ExoCache<Serializable, MyNonSerializableValue> cache3 = new InvalidationExoCache<Serializable, MyNonSerializableValue>(acache3);
      cache3.addCacheListener(listener3);
      try
      {
         cache1.put(new MyKey("a"), new MyNonSerializableValue("b"));
         assertEquals(1, cache1.getCacheSize());
         assertNull(cache2.get(new MyKey("a")));
         assertEquals(0, cache2.getCacheSize());
         assertEquals(0, cache3.getCacheSize());
         assertEquals(1, listener1.put);
         assertEquals(1, listener2.put);
         assertEquals(0, listener3.put);
         assertEquals(0, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(0, listener3.get);
         cache2.put(new MyKey("b"), new MyNonSerializableValue("c"));
         assertEquals(1, cache1.getCacheSize());
         assertEquals(1, cache2.getCacheSize());
         assertNull(cache1.get(new MyKey("b")));
         assertEquals(0, cache3.getCacheSize());
         assertEquals(2, listener1.put);
         assertEquals(2, listener2.put);
         assertEquals(0, listener3.put);
         assertEquals(1, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(0, listener3.get);
         cache3.put(new MyKey("c"), new MyNonSerializableValue("d"));
         assertEquals(1, cache1.getCacheSize());
         assertEquals(1, cache2.getCacheSize());
         assertEquals(1, cache3.getCacheSize());
         assertEquals(new MyNonSerializableValue("d"), cache3.get(new MyKey("c")));
         assertEquals(2, listener1.put);
         assertEquals(2, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(1, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(1, listener3.get);
         cache2.put(new MyKey("a"), new MyNonSerializableValue("a"));
         assertEquals(0, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());
         assertNull(cache1.get(new MyKey("a")));
         assertEquals(3, listener1.put);
         assertEquals(3, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(2, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(1, listener3.get);
         cache2.remove(new MyKey("a"));
         assertEquals(0, cache1.getCacheSize());
         assertEquals(1, cache2.getCacheSize());
         assertEquals(3, listener1.put);
         assertEquals(3, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(2, listener1.get);
         assertEquals(1, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         cache1.put(new MyKey("c"), new MyNonSerializableValue("c"));
         cache1.clearCache();
         assertEquals(0, cache1.getCacheSize());
         assertNull(cache1.get(new MyKey("b")));
         assertEquals(new MyNonSerializableValue("c"), cache2.get(new MyKey("b")));
         assertNull(cache2.get(new MyKey("c")));
         assertEquals(1, cache2.getCacheSize());
         assertEquals(4, listener1.put);
         assertEquals(4, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(3, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         Map<Serializable, MyNonSerializableValue> values = new HashMap<Serializable, MyNonSerializableValue>();
         values.put(new MyKey("a"), new MyNonSerializableValue("a"));
         values.put(new MyKey("b"), new MyNonSerializableValue("b"));
         cache1.putMap(values);
         assertEquals(2, cache1.getCacheSize());
         Thread.sleep(40);
         assertNull(cache2.get(new MyKey("a")));
         assertNull(cache2.get(new MyKey("b")));
         assertEquals(0, cache2.getCacheSize());
         assertEquals(6, listener1.put);
         assertEquals(6, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         values = new HashMap<Serializable, MyNonSerializableValue>()
         {
            private static final long serialVersionUID = 1L;

            public Set<Entry<Serializable, MyNonSerializableValue>> entrySet()
            {
               Set<Entry<Serializable, MyNonSerializableValue>> set = new LinkedHashSet<Entry<Serializable, MyNonSerializableValue>>(super.entrySet());
               set.add(new Entry<Serializable, MyNonSerializableValue>()
               {

                  public MyNonSerializableValue setValue(MyNonSerializableValue paramV)
                  {
                     return null;
                  }

                  public MyNonSerializableValue getValue()
                  {
                     throw new RuntimeException("An exception");
                  }

                  public Serializable getKey()
                  {
                     return "c";
                  }
               });
               return set;
            }
         };
         values.put(new MyKey("e"), new MyNonSerializableValue("e"));
         values.put(new MyKey("d"), new MyNonSerializableValue("d"));
         try
         {
            cache1.putMap(values);
         }
         catch (Exception e)
         {
            // ignore me
         }
         assertEquals(2, cache1.getCacheSize());
         assertEquals(0, cache2.getCacheSize());
         assertEquals(1, cache3.getCacheSize());
         assertEquals(6, listener1.put);
         assertEquals(6, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         assertEquals(0, listener1.expire);
         assertEquals(0, listener2.expire);
         assertEquals(0, listener3.expire);
         Thread.sleep(1600);
         assertEquals(0, cache1.getCacheSize());
         assertEquals(0, cache2.getCacheSize());
         assertEquals(0, cache3.getCacheSize());
         assertEquals(6, listener1.put);
         assertEquals(6, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         assertEquals(2, listener1.expire);
         assertEquals(3, listener2.expire);
         assertEquals(1, listener3.expire);
         cache1.put(new MyKey("a"), new MyNonSerializableValue("b"));
         assertNotNull(cache1.get(new MyKey("a")));
         assertNull(cache2.get(new MyKey("a")));
         cache2.put(new MyKey("a"), new MyNonSerializableValue("c"));
         assertNotNull(cache2.get(new MyKey("a")));
         assertNull(cache1.get(new MyKey("a")));
         cache1.put(new MyKey("a"), new MyNonSerializableValue("c"));
         assertEquals(new MyNonSerializableValue("c"), cache2.get(new MyKey("a")));
         assertEquals(new MyNonSerializableValue("c"), cache1.get(new MyKey("a")));
         assertEquals(9, listener1.put);
         assertEquals(9, listener2.put);
         assertEquals(1, listener3.put);
         assertEquals(6, listener1.get);
         assertEquals(8, listener2.get);
         assertEquals(1, listener3.get);
         assertEquals(1, listener1.remove);
         assertEquals(1, listener2.remove);
         assertEquals(0, listener3.remove);
         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);
         assertEquals(0, listener3.clearCache);
         assertEquals(2, listener1.expire);
         assertEquals(3, listener2.expire);
         assertEquals(1, listener3.expire);
         
      }
      finally
      {
         acache1.cache.stop();
         acache2.cache.stop();
         acache3.cache.stop();
      }
   }

   public void testMultiThreading() throws Exception
   {
      long time = System.currentTimeMillis();
      final ExoCache<Serializable, Object> cache = service.getCacheInstance("test-multi-threading");
      final int totalElement = 100;
      final int totalTimes = 20;
      int reader = 20;
      int writer = 10;
      int remover = 5;
      int cleaner = 1;
      final CountDownLatch startSignalWriter = new CountDownLatch(1);
      final CountDownLatch startSignalOthers = new CountDownLatch(1);
      final CountDownLatch doneSignal = new CountDownLatch(reader + writer + remover);
      final List<Exception> errors = Collections.synchronizedList(new ArrayList<Exception>());
      for (int i = 0; i < writer; i++)
      {
         final int index = i;
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignalWriter.await();
                  for (int j = 0; j < totalTimes; j++)
                  {
                     for (int i = 0; i < totalElement; i++)
                     {
                        cache.put(new MyKey("key" + i), "value" + i);
                     }
                     if (index == 0 && j == 0)
                     {
                        // The cache is full, we can launch the others
                        startSignalOthers.countDown();
                     }
                     sleep(50);
                  }
               }
               catch (Exception e)
               {
                  errors.add(e);
               }
               finally
               {
                  doneSignal.countDown();
               }
            }
         };
         thread.start();
      }
      startSignalWriter.countDown();
      for (int i = 0; i < reader; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignalOthers.await();
                  for (int j = 0; j < totalTimes; j++)
                  {
                     for (int i = 0; i < totalElement; i++)
                     {
                        cache.get(new MyKey("key" + i));
                     }
                     sleep(50);
                  }
               }
               catch (Exception e)
               {
                  errors.add(e);
               }
               finally
               {
                  doneSignal.countDown();
               }
            }
         };
         thread.start();
      }
      for (int i = 0; i < remover; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignalOthers.await();
                  for (int j = 0; j < totalTimes; j++)
                  {
                     for (int i = 0; i < totalElement; i++)
                     {
                        cache.remove(new MyKey("key" + i));
                     }
                     sleep(50);
                  }
               }
               catch (Exception e)
               {
                  errors.add(e);
               }
               finally
               {
                  doneSignal.countDown();
               }
            }
         };
         thread.start();
      }
      doneSignal.await();
      for (int i = 0; i < totalElement; i++)
      {
         cache.put(new MyKey("key" + i), "value" + i);
      }
      assertEquals(totalElement, cache.getCacheSize());
      final CountDownLatch startSignal = new CountDownLatch(1);
      final CountDownLatch doneSignal2 = new CountDownLatch(writer + cleaner);
      for (int i = 0; i < writer; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignal.await();
                  for (int j = 0; j < totalTimes; j++)
                  {
                     for (int i = 0; i < totalElement; i++)
                     {
                        cache.put(new MyKey("key" + i), "value" + i);
                     }
                     sleep(50);
                  }
               }
               catch (Exception e)
               {
                  errors.add(e);
               }
               finally
               {
                  doneSignal2.countDown();
               }
            }
         };
         thread.start();
      }
      for (int i = 0; i < cleaner; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignal.await();
                  for (int j = 0; j < totalTimes; j++)
                  {
                     sleep(150);
                     cache.clearCache();
                  }
               }
               catch (Exception e)
               {
                  errors.add(e);
               }
               finally
               {
                  doneSignal2.countDown();
               }
            }
         };
         thread.start();
      }
      cache.clearCache();
      assertEquals(0, cache.getCacheSize());
      if (!errors.isEmpty())
      {
         for (Exception e : errors)
         {
            e.printStackTrace();
         }
         throw errors.get(0);
      }
      System.out.println("Total Time = " + (System.currentTimeMillis() - time));
   }

   public static class MyCacheListener<T> implements CacheListener<Serializable, T>
   {

      public int clearCache;

      public int expire;

      public int get;

      public int put;

      public int remove;

      public void onClearCache(CacheListenerContext context) throws Exception
      {
         clearCache++;
      }

      public void onExpire(CacheListenerContext context, Serializable key, T obj) throws Exception
      {
         expire++;
      }

      public void onGet(CacheListenerContext context, Serializable key, T obj) throws Exception
      {
         get++;
      }

      public void onPut(CacheListenerContext context, Serializable key, T obj) throws Exception
      {
         put++;
      }

      public void onRemove(CacheListenerContext context, Serializable key, T obj) throws Exception
      {
         remove++;
      }
   }

   public static class MyKey implements Serializable
   {
      private static final long serialVersionUID = 1L;

      public String value;

      public MyKey(String value)
      {
         this.value = value;
      }

      @Override
      public boolean equals(Object paramObject)
      {
         return paramObject instanceof MyKey && ((MyKey)paramObject).value.endsWith(value);
      }

      @Override
      public int hashCode()
      {
         return value.hashCode();
      }

      @Override
      public String toString()
      {
         return value;
      }
   }
   
   public static class MyNonSerializableValue
   {
      public String value;

      public MyNonSerializableValue(String value)
      {
         this.value = value;
      }

      @Override
      public boolean equals(Object paramObject)
      {
         return paramObject instanceof MyNonSerializableValue && ((MyNonSerializableValue)paramObject).value.endsWith(value);
      }

      @Override
      public int hashCode()
      {
         return value.hashCode();
      }

      @Override
      public String toString()
      {
         return value;
      }
   }
}
