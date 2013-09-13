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

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ObjectCacheInfo;

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
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestMCExoCache extends TestCase
{

   CacheService service;

   MCExoCache<Serializable, Object> cache;

   ExoCacheFactory factory;

   MyCacheListener<Object> listener;

   public TestMCExoCache(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      this.service = (CacheService)PortalContainer.getInstance().getComponentInstanceOfType(CacheService.class);
      this.cache = (MCExoCache<Serializable, Object>)service.getCacheInstance("myCache");
      this.factory = (ExoCacheFactory)PortalContainer.getInstance().getComponentInstanceOfType(ExoCacheFactory.class);
      listener = new MyCacheListener<Object>();
      cache.addCacheListener(listener);
   }

   protected void tearDown() throws Exception
   {
      cache.clearCache();
   }

   public void testPut() throws Exception
   {
      assertEquals(0, listener.put);
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      cache.put(new MyKey("d"), null);
      assertEquals(3, listener.put);
      assertEquals(3, cache.getCacheSize());
      assertEquals("a", cache.get(new MyKey("a")));
      cache.put(new MyKey("a"), "c");
      assertEquals(4, listener.put);
      assertEquals(3, cache.getCacheSize());
      assertEquals("c", cache.get(new MyKey("a")));
      cache.put(new MyKey("d"), "c");
      assertEquals(5, listener.put);
      assertEquals(4, cache.getCacheSize());
   }

   public void testClearCache() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      assertEquals("a", cache.get(new MyKey("a")));
      assertTrue(cache.getCacheSize() > 0);
      assertEquals(0, listener.clearCache);
      cache.clearCache();
      assertEquals(1, listener.clearCache);
      assertTrue(cache.getCacheSize() == 0);
      assertNull(cache.get(new MyKey("a")));
   }

   public void testGet() throws Exception
   {
      assertEquals(0, cache.getCacheSize());
      cache.put(new MyKey("a"), "a");
      assertEquals(1, cache.getCacheSize());
      assertEquals(0, listener.get);
      assertEquals("a", cache.get(new MyKey("a")));
      assertEquals(1, listener.get);
      cache.put(new MyKey("a"), "c");
      assertEquals(1, cache.getCacheSize());
      assertEquals("c", cache.get(new MyKey("a")));
      assertEquals(2, listener.get);
      cache.remove(new MyKey("a"));
      assertEquals(0, cache.getCacheSize());
      assertNull(cache.get(new MyKey("a")));
      assertNull(cache.get(new MyKey("x")));
      assertEquals(4, listener.get);
   }

   public void testRemove() throws Exception
   {
      cache.put(new MyKey("a"), 1);
      cache.put(new MyKey("b"), 2);
      cache.put(new MyKey("c"), 3);
      assertEquals(3, cache.getCacheSize());
      assertEquals(0, listener.remove);
      assertEquals(1, cache.remove(new MyKey("a")));
      assertEquals(1, listener.remove);
      assertEquals(2, cache.getCacheSize());
      assertEquals(2, cache.remove(new MyKey("b")));
      assertEquals(2, listener.remove);
      assertEquals(1, cache.getCacheSize());
      assertNull(cache.remove(new MyKey("x")));
      assertEquals(2, listener.remove);
      assertEquals(1, cache.getCacheSize());
   }

   public void testPutMap() throws Exception
   {
      Map<Serializable, Object> values = new HashMap<Serializable, Object>();
      values.put(new MyKey("a"), "a");
      values.put(new MyKey("b"), "b");
      assertEquals(0, listener.put);
      assertEquals(0, cache.getCacheSize());
      cache.putMap(values);
      assertEquals(2, listener.put);
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
      assertEquals(4, listener.put);
      assertEquals(4, cache.getCacheSize());
   }

   public void testGetCachedObjects() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      cache.put(new MyKey("d"), null);
      assertEquals(3, cache.getCacheSize());
      try
      {
         List<Object> values = cache.getCachedObjects();
         assertEquals(3, values.size());
         assertTrue(values.contains("a"));
         assertTrue(values.contains("b"));
         assertTrue(values.contains("c"));
      }
      catch (UnsupportedOperationException e)
      {
         // OK
      }
   }

   public void testRemoveCachedObjects() throws Exception
   {
      cache.put(new MyKey("a"), "a");
      cache.put(new MyKey("b"), "b");
      cache.put(new MyKey("c"), "c");
      cache.put(new MyKey("d"), null);
      assertEquals(3, cache.getCacheSize());
      try
      {
         List<Object> values = cache.removeCachedObjects();
         assertEquals(3, values.size());
         assertTrue(values.contains("a"));
         assertTrue(values.contains("b"));
         assertTrue(values.contains("c"));
         assertEquals(0, cache.getCacheSize());
      }
      catch (UnsupportedOperationException e)
      {
         // OK
      }
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
      try
      {
         cache.select(selector);
         assertEquals(3, count.intValue());
      }
      catch (UnsupportedOperationException e)
      {
         // OK
      }
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
      assertTrue(cache.getCacheSize() > 0);
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
         return paramObject instanceof MyKey && ((MyKey)paramObject).value.equals(value);
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
