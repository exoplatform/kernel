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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.impl.infinispan.TestExoCacheCreator.TestExoCache;
import org.exoplatform.test.BasicTestCase;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.manager.CacheContainer;

/**
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestExoCacheFactoryImpl extends BasicTestCase
{

   CacheService service_;

   public TestExoCacheFactoryImpl(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      service_ = (CacheService)PortalContainer.getInstance().getComponentInstanceOfType(CacheService.class);
   }

   public void testCacheFactory()
   {
      ExoCache cache = service_.getCacheInstance("myCache");
      assertTrue("expect an instance of AbstractExoCache", cache instanceof AbstractExoCache);
      AbstractExoCache aCache = (AbstractExoCache)cache;
      assertTrue("expect a local cache", aCache.cache.getConfiguration().getCacheMode() == CacheMode.LOCAL);
      aCache.cache.stop();
      cache = service_.getCacheInstance("cacheDistributed");
      assertTrue("expect an instance of AbstractExoCache", cache instanceof AbstractExoCache);
      aCache = (AbstractExoCache)cache;
      assertTrue("expect a distributed cache", aCache.cache.getConfiguration().getCacheMode() == CacheMode.REPL_SYNC);
      aCache.cache.stop();
      cache = service_.getCacheInstance("myCustomCache");
      assertTrue("expect an instance of AbstractExoCache", cache instanceof AbstractExoCache);
      aCache = (AbstractExoCache)cache;
      assertTrue("expect a distributed cache", aCache.cache.getConfiguration().getCacheMode() == CacheMode.REPL_SYNC);
      aCache.cache.stop();
   }

   public void testExoCacheCreator()
   {
      ExoCache cache = service_.getCacheInstance("test-default-impl");
      assertTrue("expect an instance of AbstractExoCache", cache instanceof AbstractExoCache);
      AbstractExoCache aCache = (AbstractExoCache)cache;
      aCache.cache.stop();
      cache = service_.getCacheInstance("test-custom-impl-with-old-config");
      assertTrue("expect an instance of TestExoCache", cache instanceof TestExoCache);
      cache = service_.getCacheInstance("test-custom-impl-with-new-config");
      assertTrue("expect an instance of TestExoCache", cache instanceof TestExoCache);
   }
   
   public void testSameCacheManager()
   {
      ExoCache cache1 = service_.getCacheInstance("myCustomCache");
      assertTrue("expect an instance of AbstractExoCache", cache1 instanceof AbstractExoCache);
      AbstractExoCache aCache1 = (AbstractExoCache)cache1;
      CacheContainer cacheContainer1 = aCache1.cache.getCacheManager();
      
      ExoCache cache2 = service_.getCacheInstance("myCustomCache-Bis");
      assertTrue("expect an instance of AbstractExoCache", cache2 instanceof AbstractExoCache);
      AbstractExoCache aCache2 = (AbstractExoCache)cache2;
      CacheContainer cacheContainer2 = aCache2.cache.getCacheManager();
      assertTrue("The CacheContainer should be the same", cacheContainer1 == cacheContainer2);
      
      ExoCache cache3 = service_.getCacheInstance("myCustomCache-Bis2");
      assertTrue("expect an instance of AbstractExoCache", cache3 instanceof AbstractExoCache);
      AbstractExoCache aCache3 = (AbstractExoCache)cache3;
      CacheContainer cacheContainer3 = aCache3.cache.getCacheManager();
      assertTrue("The CacheContainer should be the same", cacheContainer1 == cacheContainer3);
      
      aCache1.cache.stop();
      aCache2.cache.stop();      
   }
}
