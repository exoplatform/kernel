/*
 * Copyright (C) 2013 eXo Platform SAS.
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

import junit.framework.Assert;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.test.TestCacheService.MyExoCache;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestCacheServiceThreadSafety
{
   private static final int TOTAL_THREADS = 50;

   @Rule
   public ContiPerfRule rule = new ContiPerfRule();

   private CyclicBarrier startSignal = new CyclicBarrier(TOTAL_THREADS);
   private CacheService service_;

   @Before
   public void setUp()
   {
      service_ = (CacheService)PortalContainer.getInstance().getComponentInstanceOfType(CacheService.class);
      //Needed for that case if testCacheFactory will be executed before testConcurrentCreation and MyExoCache.count will have value bigger than 0
      MyExoCache.count.getAndSet(0);
      // Pre-create it 
      service_.getCacheInstance("FooCache");
   }

   @Test
   @PerfTest(invocations = TOTAL_THREADS, threads = TOTAL_THREADS)
   public void testConcurrentCreation() throws Exception
   {
      startSignal.await();
      if (service_.getCacheInstance("TestConcurrentCreation") == null)
      {
         throw new RuntimeException("The cache 'TestConcurrentCreation' cannot be null");
      }
      Assert.assertEquals(1, MyExoCache.count.get());
   }

   @Test
   @PerfTest(invocations = 100000000, threads = 100)
   public void testPerf() throws Exception
   {
      if (service_.getCacheInstance("FooCache") == null)
      {
         throw new RuntimeException("The cache 'FooCache' cannot be null");
      }
   }
}
