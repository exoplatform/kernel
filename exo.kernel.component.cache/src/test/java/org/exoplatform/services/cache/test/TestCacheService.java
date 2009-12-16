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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.FIFOExoCache;
import org.exoplatform.services.cache.SimpleExoCache;
import org.exoplatform.test.BasicTestCase;

import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
public class TestCacheService extends BasicTestCase
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

   public void testCacheService() throws Exception
   {
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
      assertEquals("expect number of cache instanse is ", 7, caches.size());
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
      ObjectName name = new ObjectName("exo:service=cache,name=\"cacheLiveTime2s\",portal=\"portal\"");
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
   }

   /*
     public void testLoggingListener() throws Exception {
       ExoCache<String, Object> cache = service_.getCacheInstance("TestLogCache");
       
       int count = 100;
       while (count-- > 0) {
         cache.put("key"+count, "anything");
       }
       Log log = ExoLogger.getLogger("kernel.cache.log");
       
       assertTrue("logger is not MockTestLogger, you needto pass -Dorg.exoplatform.services.log.Log=org.exoplatform.services.cache.test.MockTestLogger", (log instanceof MockTestLogger));
       
       MockTestLogger logger = (MockTestLogger) log;
       assertEquals("Number of warnings ", 6,logger.getWarnCount());
     }
   */

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
}
