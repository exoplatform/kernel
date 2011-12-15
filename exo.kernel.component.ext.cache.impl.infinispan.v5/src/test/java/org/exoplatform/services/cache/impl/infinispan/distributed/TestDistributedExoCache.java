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
package org.exoplatform.services.cache.impl.infinispan.distributed;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ObjectCacheInfo;
import org.exoplatform.services.cache.impl.infinispan.ExoCacheFactoryImpl;
import org.exoplatform.services.ispn.DistributedCacheManager;
import org.exoplatform.test.BasicTestCase;
import org.infinispan.distribution.DistributionManager;

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
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestDistributedExoCache extends BasicTestCase
{

   CacheService service;

   DistributedExoCache<Serializable, Object> cache;

   DistributedExoCache<Serializable, Object> cache2;

   public TestDistributedExoCache(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      this.service = (CacheService)PortalContainer.getInstance().getComponentInstanceOfType(CacheService.class);
      this.cache = (DistributedExoCache<Serializable, Object>)service.getCacheInstance("cache-distributed");
      this.cache2 = (DistributedExoCache<Serializable, Object>)service.getCacheInstance("cache-distributed2");
      cache2.put(new MyKey("a"), "a");
   }

   protected void tearDown() throws Exception
   {
      cache.clearCache();
      cache2.clearCache();
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
      assertEquals(3, cache.getCacheSize());
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
      assertEquals(3, cache.getCacheSize());
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

   public void testMultiThreading() throws Exception
   {
      long time = System.currentTimeMillis();
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

   public static class MyCacheListener implements CacheListener<Serializable, Object>
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

      public void onExpire(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
         expire++;
      }

      public void onGet(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
         get++;
      }

      public void onPut(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
         put++;
      }

      public void onRemove(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
         remove++;
      }
   }

   public static class MyKey implements Serializable
   {
      private static final long serialVersionUID = 1L;

      public String value;
      public MyKey(){}
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
   
   public void testDistributedCache() throws Exception
   {
      PortalContainer pc = PortalContainer.getInstance();
      System.out
         .println("WARNING: For Linux distributions the following JVM parameter must be set to true, java.net.preferIPv4Stack = "
            + System.getProperty("java.net.preferIPv4Stack"));
      ExoCacheConfig config = new ExoCacheConfig();
      config.setName("MyCacheDistributed");
      config.setMaxSize(5);
      config.setLiveTime(1);
      config.setImplementation("LRU");
      config.setDistributed(true);
      Map<String, String> params = new HashMap<String, String>();
      params.put("infinispan-num-owners", "1");
      ConfigurationManager cm = (ConfigurationManager)pc.getComponentInstanceOfType(ConfigurationManager.class);
      DistributedCacheManager dcm2 =
         new DistributedCacheManager("jar:/conf/portal/distributed-cache-configuration.xml", params, cm);

      @SuppressWarnings("unchecked")
      DistributedExoCache<Serializable, Object> cache1 =
         (DistributedExoCache<Serializable, Object>)((ExoCacheFactory)pc
            .getComponentInstanceOfType(ExoCacheFactory.class)).createCache(config);
      DistributionManager dm = cache1.getCache().getDistributionManager();
      MyCacheListener listener1 = new MyCacheListener();
      cache1.addCacheListener(listener1);
      DistributedExoCache<Serializable, Object> cache2 =
         (DistributedExoCache<Serializable, Object>)new ExoCacheFactoryImpl(
            (ExoContainerContext)pc.getComponentInstanceOfType(ExoContainerContext.class),
            "jar:/conf/portal/cache-configuration-template.xml", cm, dcm2).createCache(config);
      MyCacheListener listener2 = new MyCacheListener();
      cache2.addCacheListener(listener2);
      try
      {
         MyKey key;
         cache1.put(key = new MyKey("a"), "b");
         assertEquals(1, cache1.getCacheSize());
         assertEquals("b", cache2.get(new MyKey("a")));
         assertEquals(1, cache2.getCacheSize());
         
//         int put1 = 1;
//         int put2 = dm.getLocality(key).isLocal() ? 0 : 1;
//
//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(0, listener1.get);
         assertEquals(1, listener2.get);
         
         MyKey key2;
         cache2.put(key2 = new MyKey("b"), "c");
         assertEquals(2, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());
         assertEquals("c", cache1.get(new MyKey("b")));
         
//         put1 += dm.getLocality(key2).isLocal() ? 1 : 0;
//         put2++;
//
//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(1, listener1.get);
         assertEquals(1, listener2.get);

         assertEquals(2, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());

//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(1, listener1.get);
         assertEquals(1, listener2.get);

         cache2.put(key = new MyKey("a"), "a");
         assertEquals(2, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());
         assertEquals("a", cache1.get(new MyKey("a")));
         
//         put1 += dm.getLocality(key).isLocal() ? 1 : 0;
//         put2++;
//
//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(2, listener1.get);
         assertEquals(1, listener2.get);

         cache2.remove(key = new MyKey("a"));
         assertEquals(1, cache1.getCacheSize());
         assertEquals(1, cache2.getCacheSize());
         
//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(2, listener1.get);
         assertEquals(1, listener2.get);
         
//         int remove1 = dm.getLocality(key).isLocal() ? 1 : 0;
//         int remove2 = 1;
//         
//         assertEquals(remove1, listener1.remove);
//         assertEquals(remove2, listener2.remove);
         
         cache1.put(key = new MyKey("c"), "c");
         cache1.clearCache();
         assertEquals(0, cache1.getCacheSize());
         assertNull(cache1.get(new MyKey("b")));
         assertNull(cache2.get(new MyKey("b")));
         assertNull(cache2.get(new MyKey("c")));
         assertEquals(0, cache2.getCacheSize());
         
//         put1++;
//         put2 += dm.getLocality(key).isLocal() ? 0 : 1;

//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(3, listener1.get);
         assertEquals(3, listener2.get);

//         assertEquals(remove1, listener1.remove);
//         assertEquals(remove2, listener2.remove);

         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);

         Map<Serializable, Object> values = new HashMap<Serializable, Object>();
         values.put(key = new MyKey("a"), "a");
         values.put(key2 = new MyKey("b"), "b");
         cache1.putMap(values);
         assertEquals(2, cache1.getCacheSize());
         Thread.sleep(40);
         assertEquals("a", cache2.get(new MyKey("a")));
         assertEquals("b", cache2.get(new MyKey("b")));
         assertEquals(2, cache2.getCacheSize());

//         put1 += 2;
//         put2 += (dm.getLocality(key).isLocal() ? 0 : 1) + (dm.getLocality(key2).isLocal() ? 0 : 1);
//         
//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);

//         assertEquals(remove1, listener1.remove);
//         assertEquals(remove2, listener2.remove);

         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);

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
         cache1.putMap(values);
         assertEquals(2, cache1.getCacheSize());
         assertEquals(2, cache2.getCacheSize());

//         assertEquals(put1, listener1.put);
//         assertEquals(put2, listener2.put);

         assertEquals(3, listener1.get);
         assertEquals(5, listener2.get);

//         assertEquals(remove1, listener1.remove);
//         assertEquals(remove2, listener2.remove);

         assertEquals(1, listener1.clearCache);
         assertEquals(0, listener2.clearCache);

         assertEquals(0, listener1.expire);
         assertEquals(0, listener2.expire);

      }
      finally
      {
         dcm2.stop();
      }
   }
}
