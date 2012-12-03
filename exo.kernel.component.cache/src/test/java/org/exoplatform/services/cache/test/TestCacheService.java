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
package org.exoplatform.services.cache.test;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.FIFOExoCache;
import org.exoplatform.services.cache.SimpleExoCache;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.services.cache.impl.CacheServiceImpl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/*
 * Thu, May 15, 2003 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestCacheService.java 5799 2006-05-28 17:55:42Z geaz $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestCacheService extends TestCase
{

   CacheService service_;

   public TestCacheService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      service_ = (CacheService)PortalContainer.getInstance().getComponentInstanceOfType(CacheService.class);
   }

   public void testConcurrentCreation() throws Exception
   {
      //Needed for that case if testCacheFactory will be executed before testConcurrentCreation and MyExoCache.count will have value bigger than 0
      MyExoCache.count.getAndSet(0);
      int threads = 20;
      final CountDownLatch startSignal = new CountDownLatch(1);
      final CountDownLatch doneSignal = new CountDownLatch(threads);
      final List<Exception> errors = Collections.synchronizedList(new ArrayList<Exception>());
      for (int i = 0; i < threads; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignal.await();
                  if (service_.getCacheInstance("TestConcurrentCreation") == null)
                  {
                     throw new RuntimeException("The cache 'TestConcurrentCreation' cannot be null");
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
      startSignal.countDown();
      doneSignal.await();
      if (!errors.isEmpty())
      {
         for (Exception e : errors)
         {
            e.printStackTrace();
         }
         throw errors.get(0);
      }
      assertEquals(1, MyExoCache.count.get());
   }

   public void testPerf() throws Exception
   {
      // Pre-create it 
      service_.getCacheInstance("FooCache");
      int threads = 100;
      final CountDownLatch startSignal = new CountDownLatch(1);
      final CountDownLatch doneSignal = new CountDownLatch(threads);
      final List<Exception> errors = Collections.synchronizedList(new ArrayList<Exception>());
      for (int i = 0; i < threads; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignal.await();
                  for (int i = 0; i < 1000000; i++)
                  {
                     if (service_.getCacheInstance("FooCache") == null)
                     {
                        throw new RuntimeException("The cache 'FooCache' cannot be null");
                     }
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
      startSignal.countDown();
      doneSignal.await();

      if (!errors.isEmpty())
      {
         for (Exception e : errors)
         {
            e.printStackTrace();
         }
         throw errors.get(0);
      }
   }

   public void testCacheFactory() throws Exception
   {
      InitParams params = new InitParams();
      ObjectParameter param = new ObjectParameter();
      param.setName("NoImpl");
      ExoCacheConfig config = new ExoCacheConfig();
      config.setName(param.getName());
      param.setObject(config);
      params.addParameter(param);

      param = new ObjectParameter();
      param.setName("KnownImpl");
      config = new ExoCacheConfig();
      config.setName(param.getName());
      config.setImplementation("org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache");
      param.setObject(config);
      params.addParameter(param);

      param = new ObjectParameter();
      param.setName("UnKnownImpl");
      config = new ExoCacheConfig();
      config.setName(param.getName());
      config.setImplementation("fakeImpl");
      param.setObject(config);
      params.addParameter(param);

      param = new ObjectParameter();
      param.setName("UnKnownImplButCorrectFQN");
      config = new ExoCacheConfig();
      config.setName(param.getName());
      config.setImplementation("java.lang.String");
      param.setObject(config);
      params.addParameter(param);

      param = new ObjectParameter();
      param.setName("NoImpl-MyExoCacheConfig");
      config = new MyExoCacheConfig();
      config.setName(param.getName());
      param.setObject(config);
      params.addParameter(param);

      param = new ObjectParameter();
      param.setName("KnownImpl-MyExoCacheConfig");
      config = new MyExoCacheConfig();
      config.setName(param.getName());
      config.setImplementation("org.exoplatform.services.cache.FIFOExoCache");
      param.setObject(config);
      params.addParameter(param);

      param = new ObjectParameter();
      param.setName("UnKnownImpl-MyExoCacheConfig");
      config = new MyExoCacheConfig();
      config.setName(param.getName());
      config.setImplementation("fakeImpl");
      param.setObject(config);
      params.addParameter(param);

      param = new ObjectParameter();
      param.setName("UnKnownImplButCorrectFQN-MyExoCacheConfig");
      config = new MyExoCacheConfig();
      config.setName(param.getName());
      config.setImplementation("java.lang.String");
      param.setObject(config);
      params.addParameter(param);

      CacheService cs = new CacheServiceImpl(params, new MyExoCacheFactory());
      assertTrue("Expected type MyExoCache found " + cs.getCacheInstance("NoImpl").getClass(),
         cs.getCacheInstance("NoImpl") instanceof MyExoCache);
      assertTrue("Expected type ConcurrentFIFOExoCache found " + cs.getCacheInstance("KnownImpl").getClass(),
         cs.getCacheInstance("KnownImpl") instanceof ConcurrentFIFOExoCache);
      assertTrue("Expected type MyExoCache found " + cs.getCacheInstance("UnKnownImpl").getClass(),
         cs.getCacheInstance("UnKnownImpl") instanceof MyExoCache);
      assertTrue("Expected type MyExoCache found " + cs.getCacheInstance("UnKnownImplButCorrectFQN").getClass(),
         cs.getCacheInstance("UnKnownImplButCorrectFQN") instanceof MyExoCache);
      assertTrue("Expected type MyExoCache found " + cs.getCacheInstance("NoImpl-MyExoCacheConfig").getClass(),
         cs.getCacheInstance("NoImpl-MyExoCacheConfig") instanceof MyExoCache);
      assertTrue("Expected type MyExoCache found " + cs.getCacheInstance("KnownImpl-MyExoCacheConfig").getClass(),
         cs.getCacheInstance("KnownImpl-MyExoCacheConfig") instanceof MyExoCache);
      assertTrue("Expected type MyExoCache found " + cs.getCacheInstance("UnKnownImpl-MyExoCacheConfig").getClass(),
         cs.getCacheInstance("UnKnownImpl-MyExoCacheConfig") instanceof MyExoCache);
      assertTrue("Expected type MyExoCache found "
         + cs.getCacheInstance("UnKnownImplButCorrectFQN-MyExoCacheConfig").getClass(),
         cs.getCacheInstance("UnKnownImplButCorrectFQN-MyExoCacheConfig") instanceof MyExoCache);
   }

   public void testCacheService() throws Exception
   {
      assertNotNull(service_.getAllCacheInstances());
      int size = service_.getAllCacheInstances().size();

      // -----nocache info is retrived from test-configuration(nocache 0bject)
      ExoCache<String, Object> nocache = service_.getCacheInstance("nocache");
      assertTrue("expect find nocache configuaration", nocache instanceof SimpleExoCache);
      assertEquals("expect 'maxsize' of nocache is", 5, nocache.getMaxSize());
      assertEquals("expect 'liveTime' of nocache' is", 0, nocache.getLiveTime());
      nocache.put("key1", "object 1");
      assertEquals("expect 'nocache' is not lived(LiveTime=0)", 0, nocache.getCacheSize());
      // -----cacheLiveTime2s's info is retrived from test-configuration
      // (cacheLiveTime2s object)
      ExoCache<String, Object> cacheLiveTime2s = service_.getCacheInstance("cacheLiveTime2s");
      assertTrue("expect find cacheLiveTime2s configuaration", cacheLiveTime2s instanceof SimpleExoCache);
      assertEquals("expect 'maxsize' of this cache is", 5, cacheLiveTime2s.getMaxSize());
      assertEquals("expect 'liveTime' of nocache' is", 2, cacheLiveTime2s.getLiveTime());
      cacheLiveTime2s.put("key", "object2s");
      String obj2s = (String)cacheLiveTime2s.get("key");
      assertTrue("expect found 'object' in cache", obj2s != null && obj2s.equals("object2s"));
      assertEquals("expect found object in this cache", 1, cacheLiveTime2s.getCacheSize());
      Thread.sleep(2500);
      assertTrue("expect no found 'object' in this cache", cacheLiveTime2s.get("key") == null);
      assertEquals("expect cache size is ", 0, cacheLiveTime2s.getCacheSize());
      // -----cacheMaxSize0's info retrived from test-configuration (cacheMaxSize0
      // object)
      ExoCache<String, Object> cacheMaxSize0 = service_.getCacheInstance("cacheMaxSize0");
      assertTrue("expect find cacheMaxSize0 configuaration", cacheMaxSize0 instanceof SimpleExoCache);
      assertEquals("expect 'maxsize' of this cache is", 0, cacheMaxSize0.getMaxSize());
      assertEquals("expect 'liveTime' of nocache' is", 4, cacheMaxSize0.getLiveTime());
      cacheMaxSize0.put("mkey", "maxsize object");
      assertTrue("expect can't put any object to  cache", cacheMaxSize0.get("mkey") == null);
      // -----default cache's info is retrived if no cache's info is found
      ExoCache<String, Object> cache = service_.getCacheInstance("exo");
      assertTrue("expect find defaul cache configuaration", cache instanceof SimpleExoCache);
      assertEquals("expect 'maxsize' of this cache is", 100, cache.getMaxSize());
      assertEquals("expect 'liveTime' of this cache' is", 300, cache.getLiveTime());
      cache.put("test", "this is a test");
      String ret = (String)cache.get("test");
      assertTrue("expect object is cached", ret != null);

      /* ----------FIFOExoCache--------------- */
      ExoCache<String, Object> fifoCache = service_.getCacheInstance("fifocache");
      assertTrue("expect find fifo cache configuration", fifoCache instanceof FIFOExoCache);
      assertEquals("expect 'maxsize' of this cache is", 3, fifoCache.getMaxSize());
      assertEquals("expect 'liveTime' of this cache' is", 4, fifoCache.getLiveTime());
      fifoCache.put("key1", "object 1");
      fifoCache.put("key2", "object 2");
      assertEquals("expect FIFOExoCache size is:", 2, fifoCache.getCacheSize());
      String obj1 = (String)fifoCache.get("key1");
      String obj2 = (String)fifoCache.get("key2");
      assertTrue("expect found 'key1' object", obj1 != null && obj1.equals("object 1"));
      assertTrue("expect found 'key2' object", obj2 != null && obj2.equals("object 2"));
      fifoCache.put("skey", "serializable object");
      assertEquals("expect FIFOExoCache size is:", 3, fifoCache.getCacheSize());
      String sobj = (String)fifoCache.get("skey");
      assertTrue("expect found serializable key and it's value", sobj != null && sobj.equals("serializable object"));
      fifoCache.put("key4", "object 4");
      // because maxsize of cache is 3, 'object 1' associated with 'key1' is
      // remove form FIFOExoCache
      assertEquals("expect cache size is still:", 3, fifoCache.getCacheSize());
      String obj4 = (String)fifoCache.get("key4");
      assertTrue("expect object has 'key4' is put in cache", obj4 != null && obj4.equals("object 4"));
      assertTrue("expect object has key is 'key1' is remove automatically", fifoCache.get("key1") == null);
      // -------remove a object in cache by key
      fifoCache.remove("key2");
      assertEquals("now, expect cache size is", 2, fifoCache.getCacheSize());
      assertEquals("now, expect number of object in cache is:", 2, fifoCache.getCachedObjects().size());
      assertTrue("expect object has 'key2' is removed", fifoCache.get("key2") == null);
      // -------remove a object in cache by serializable name
      fifoCache.remove(new String("skey"));
      assertEquals("now, expect cache size is", 1, fifoCache.getCacheSize());
      assertEquals("now, expect number of object in cache is:", 1, fifoCache.getCachedObjects().size());
      assertTrue("expect serializable object with name 'skey' is remove", fifoCache.get(new String("skey")) == null);
      // --------------clear cache
      fifoCache.clearCache();
      assertEquals("now, expect cache is clear", 0, fifoCache.getCacheSize());
      assertEquals("now, expect number of object in cache is:", 0, fifoCache.getCachedObjects().size());
      /* --------------test cache service with add extenal component plugin------ */
      ExoCache simpleCachePlugin = service_.getCacheInstance("simpleCachePlugin");
      assertTrue("expect found simpleCache from extenal plugin", simpleCachePlugin instanceof SimpleExoCache);
      assertEquals("expect 'maxsize' of this cache is", 8, simpleCachePlugin.getMaxSize());
      assertEquals("expect 'LiveTime' of this cache is", 5, simpleCachePlugin.getLiveTime());
      ExoCache fifoCachePlugin = service_.getCacheInstance("fifoCachePlugin");
      assertTrue("expect found fifoCache from extenal plugin", fifoCachePlugin instanceof FIFOExoCache);
      assertEquals("expect 'maxsize' of this cache is", 6, fifoCachePlugin.getMaxSize());
      assertEquals("expect 'LiveTime' of this cache is", 10, fifoCachePlugin.getLiveTime());
      // ----all cache instances---
      Collection<ExoCache<? extends Serializable, ?>> caches = service_.getAllCacheInstances();
      assertEquals("expect number of cache instanse is ", size + 7, caches.size());
      hasObjectInCollection(nocache, caches, new ExoCacheComparator());
      hasObjectInCollection(cacheLiveTime2s, caches, new ExoCacheComparator());
      hasObjectInCollection(cacheMaxSize0, caches, new ExoCacheComparator());
      hasObjectInCollection(fifoCache, caches, new ExoCacheComparator());
      hasObjectInCollection(cache, caches, new ExoCacheComparator());
      hasObjectInCollection(simpleCachePlugin, caches, new ExoCacheComparator());
      hasObjectInCollection(fifoCachePlugin, caches, new ExoCacheComparator());

      // Managed tests
      MBeanServerLocator locator =
         (MBeanServerLocator)PortalContainer.getInstance().getComponentInstanceOfType(MBeanServerLocator.class);
      MBeanServer server = locator.server;
      assertNotNull(locator.server);
      ObjectName name = new ObjectName("exo:service=cache,name=cacheLiveTime2s,portal=portal");
      MBeanInfo info = server.getMBeanInfo(name);
      assertNotNull(info);
      Map<String, MBeanAttributeInfo> infoMap = new HashMap<String, MBeanAttributeInfo>();
      for (MBeanAttributeInfo attributeInfo : info.getAttributes())
      {
         infoMap.put(attributeInfo.getName(), attributeInfo);
      }
      assertTrue(infoMap.containsKey("Name"));
      assertTrue(infoMap.containsKey("Size"));
      assertTrue(infoMap.containsKey("Capacity"));
      assertTrue(infoMap.containsKey("TimeToLive"));
      assertTrue(infoMap.containsKey("HitCount"));
      assertTrue(infoMap.containsKey("MissCount"));
      assertEquals(6, infoMap.size());
      assertEquals(5, server.getAttribute(name, "Capacity"));
      assertEquals(size + 7, service_.getAllCacheInstances().size());
   }

   private static class ExoCacheComparator implements Comparator
   {

      public int compare(Object o1, Object o2)
      {
         ExoCache c1 = (ExoCache)o1;
         ExoCache c2 = (ExoCache)o2;
         if ((c1.getName().equals(c2.getName()) && (c1.getMaxSize() == c2.getMaxSize()))
            && (c1.getLiveTime() == c2.getLiveTime()))
         {
            return 0;
         }
         return -1;
      }
   }

   protected String getDescription()
   {
      return "Test Cache Service";
   }

   public static class MyExoCache<V> implements ExoCache<Serializable, V>
   {

      private static AtomicInteger count = new AtomicInteger();

      public MyExoCache()
      {
         count.incrementAndGet();
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getName()
       */
      public String getName()
      {
         return "TestConcurrentCreation";
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#setName(java.lang.String)
       */
      public void setName(String name)
      {
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getLabel()
       */
      public String getLabel()
      {
         return "TestConcurrentCreation";
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#setLabel(java.lang.String)
       */
      public void setLabel(String s)
      {
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#get(java.io.Serializable)
       */
      public V get(Serializable key)
      {
         return null;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#remove(java.io.Serializable)
       */
      public V remove(Serializable key) throws NullPointerException
      {
         return null;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#put(java.io.Serializable, java.lang.Object)
       */
      public void put(Serializable key, V value) throws NullPointerException
      {

      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#putMap(java.util.Map)
       */
      public void putMap(Map<? extends Serializable, ? extends V> objs) throws NullPointerException,
         IllegalArgumentException
      {

      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#clearCache()
       */
      public void clearCache()
      {

      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#select(org.exoplatform.services.cache.CachedObjectSelector)
       */
      public void select(CachedObjectSelector<? super Serializable, ? super V> selector) throws Exception
      {

      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getCacheSize()
       */
      public int getCacheSize()
      {
         return 0;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getMaxSize()
       */
      public int getMaxSize()
      {
         return 0;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#setMaxSize(int)
       */
      public void setMaxSize(int max)
      {
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getLiveTime()
       */
      public long getLiveTime()
      {
         return 0;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#setLiveTime(long)
       */
      public void setLiveTime(long period)
      {
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getCacheHit()
       */
      public int getCacheHit()
      {
         return 0;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getCacheMiss()
       */
      public int getCacheMiss()
      {
         return 0;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#getCachedObjects()
       */
      public List<? extends V> getCachedObjects() throws Exception
      {
         return null;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#removeCachedObjects()
       */
      public List<? extends V> removeCachedObjects()
      {
         return null;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#addCacheListener(org.exoplatform.services.cache.CacheListener)
       */
      public void addCacheListener(CacheListener<? super Serializable, ? super V> listener) throws NullPointerException
      {
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#isLogEnabled()
       */
      public boolean isLogEnabled()
      {
         return false;
      }

      /**
       * @see org.exoplatform.services.cache.ExoCache#setLogEnabled(boolean)
       */
      public void setLogEnabled(boolean b)
      {
      }

   }

   public static class MyExoCacheFactory implements ExoCacheFactory
   {

      /**
       * @see org.exoplatform.services.cache.ExoCacheFactory#createCache(org.exoplatform.services.cache.ExoCacheConfig)
       */
      public ExoCache createCache(ExoCacheConfig config) throws ExoCacheInitException
      {
         return new MyExoCache();
      }

   }

   public static class MyExoCacheConfig extends ExoCacheConfig
   {
   }

   private static void hasObjectInCollection(Object obj, Collection c, Comparator comparator) throws Exception
   {
      Iterator iter = c.iterator();
      while (iter.hasNext())
      {
         Object o = iter.next();
         if (comparator.compare(obj, o) == 0)
            return;
      }
      throw new Exception("Object " + obj + " hasn't in collection " + c);
   }
}
