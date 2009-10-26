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

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.FIFOExoCache;
import org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache;
import org.exoplatform.services.log.Log;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ScalabilityTestLoad extends TestCase
{

   private static void doTest(String name, List<CacheProvider> providers, int cacheSize, Config config)
   {

      System.out.println("-----------------------------------------");
      System.out.println("Test " + name + " cacheSize=" + cacheSize + " threadSize=" + config.threadSize
         + " objectSize=" + config.objectSize + " getSize=" + config.getSize + " putSize=" + config.putSize
         + " removalSize=" + config.removalSize);
      for (CacheProvider provider : providers)
      {
         Test test = new Test(provider.createCache(cacheSize), config);
         long time = test.perform();
         System.out.println("Cache " + provider.name + ": " + time + "ms");
      }
      System.out.println("");

   }

   private int multiplier = 10;

   List<CacheProvider> providers = Arrays.asList(fifo, concurrentFIFO);

   List<CacheProvider> providers2 = Arrays.asList(fifo, concurrentFIFO, fifoWithListener, concurrentFIFOWithListener);

   public void testReadMostly1()
   {
      doTest("Read mostly 1", providers, 50, new Config(4, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
      doTest("Read mostly 1", providers, 50, new Config(8, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
      doTest("Read mostly 1", providers, 50, new Config(16, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
      doTest("Read mostly 1", providers, 50, new Config(32, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
   }

   public void testReadMostly2()
   {
      doTest("Read mostly 2", providers, 500, new Config(4, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
      doTest("Read mostly 2", providers, 500, new Config(8, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
      doTest("Read mostly 2", providers, 500, new Config(16, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
      doTest("Read mostly 2", providers, 500, new Config(32, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
   }

   public void testWrite1()
   {
      doTest("Write only 1", providers, 50, new Config(4, 100, 0, 1000 * multiplier, 1000 * multiplier));
      doTest("Write only 1", providers, 50, new Config(8, 100, 0, 1000 * multiplier, 1000 * multiplier));
      doTest("Write only 1", providers, 50, new Config(16, 100, 0, 1000 * multiplier, 1000 * multiplier));
      doTest("Write only 1", providers, 50, new Config(32, 100, 0, 1000 * multiplier, 1000 * multiplier));
   }

   public void testWrite2()
   {
      doTest("Write only 2", providers, 500, new Config(4, 100, 0, 1000 * multiplier, 1000 * multiplier));
      doTest("Write only 2", providers, 500, new Config(8, 100, 0, 1000 * multiplier, 1000 * multiplier));
      doTest("Write only 2", providers, 500, new Config(16, 100, 0, 1000 * multiplier, 1000 * multiplier));
      doTest("Write only 2", providers, 500, new Config(32, 100, 0, 1000 * multiplier, 1000 * multiplier));
   }

   public void testContention()
   {
      doTest("Contention", providers2, 500, new Config(4, 100, 100000 * multiplier, 1000 * multiplier,
         1000 * multiplier));
      doTest("Contention", providers2, 500, new Config(8, 100, 100000 * multiplier, 500 * multiplier, 500 * multiplier));
      doTest("Contention", providers2, 500,
         new Config(16, 100, 100000 * multiplier, 250 * multiplier, 250 * multiplier));
      doTest("Contention", providers2, 500,
         new Config(32, 100, 100000 * multiplier, 125 * multiplier, 125 * multiplier));
   }

   private abstract static class CacheProvider
   {

      String name;

      private CacheProvider(String name)
      {
         this.name = name;
      }

      abstract ExoCache<Serializable, Object> createCache(int cacheSize);

   }

   private static Log createLog()
   {
      /*
          SimpleLog log = new SimpleLog("test");
          log.setLevel(SimpleLog.LOG_LEVEL_ALL);
          return log;
      */
      return null;
   }

   private static CacheProvider concurrentFIFO = new CacheProvider("Concurrent FIFO cache")
   {
      public ExoCache<Serializable, Object> createCache(int cacheSize)
      {
         return new ConcurrentFIFOExoCache<Serializable, Object>(cacheSize, createLog());
      }
   };

   private static CacheProvider fifo = new CacheProvider("FIFO cache")
   {
      public ExoCache<Serializable, Object> createCache(int cacheSize)
      {
         return new FIFOExoCache<Serializable, Object>(cacheSize);
      }
   };

   private static CacheProvider concurrentFIFOWithListener = new CacheProvider("Concurrent FIFO cache with listener")
   {
      public ExoCache<Serializable, Object> createCache(int cacheSize)
      {
         ConcurrentFIFOExoCache<Serializable, Object> cache =
            new ConcurrentFIFOExoCache<Serializable, Object>(cacheSize, createLog());
         cache.addCacheListener(new SimpleCacheListener());
         return cache;
      }
   };

   private static CacheProvider fifoWithListener = new CacheProvider("FIFO cache with listener")
   {
      public ExoCache<Serializable, Object> createCache(int cacheSize)
      {
         FIFOExoCache<Serializable, Object> cache = new FIFOExoCache<Serializable, Object>(cacheSize);
         cache.addCacheListener(new SimpleCacheListener());
         return cache;
      }
   };

   private static class Config
   {

      private final int threadSize;

      private final int objectSize;

      private final int putSize;

      private final int getSize;

      private final int removalSize;

      private Config(int threadSize, int objectSize, int getSize, int putSize, int removalSize)
      {
         this.threadSize = threadSize;
         this.objectSize = objectSize;
         this.putSize = putSize;
         this.getSize = getSize;
         this.removalSize = removalSize;
      }
   }

   private static class Test
   {

      private final Config config;

      private final ExecutorService executor;

      private final ExecutorCompletionService<Worker> completionService;

      private final Worker[] workers;

      private final ExoCache<Serializable, Object> cache;

      private Test(ExoCache<Serializable, Object> cache, Config config)
      {
         this.config = config;
         this.executor = Executors.newFixedThreadPool(config.threadSize);
         this.completionService = new ExecutorCompletionService<Worker>(executor);
         this.cache = cache;

         //
         Worker[] workers = new Worker[config.threadSize];
         for (int i = 0; i < config.threadSize; i++)
         {
            workers[i] = new Worker();
         }
         this.workers = workers;
      }

      private void start()
      {
         for (Worker worker : workers)
         {
            completionService.submit(worker);
         }
      }

      private void stop()
      {
         try
         {
            for (int i = 0; i < config.threadSize; i++)
            {
               completionService.take().get();
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
            fail();
         }
      }

      public long perform()
      {
         long time = -System.currentTimeMillis();
         start();
         stop();

         //
         if (cache instanceof ConcurrentFIFOExoCache)
         {
            ((ConcurrentFIFOExoCache)cache).assertConsistent();
         }

         //
         time += System.currentTimeMillis();
         return time;
      }

      private class Worker implements Callable<Worker>
      {

         private final Serializable[] keys;

         private final Object[] objects;

         private final Random random = new Random();

         private Worker()
         {
            keys = new Serializable[config.objectSize];
            objects = new Object[config.objectSize];
            for (int i = 0; i < config.objectSize; i++)
            {
               keys[i] = new Key();
               objects[i] = new Object();
            }
         }

         public Worker call() throws Exception
         {
            int gets = config.getSize;
            int puts = config.putSize;
            int removals = config.removalSize;

            //
            while (gets > 0 || puts > 0 || removals > 0)
            {
               int entry = Math.abs(random.nextInt()) % objects.length;
               int decision = Math.abs(random.nextInt()) % (gets + puts + removals);
               Serializable key = keys[entry];
               if (decision < gets)
               {
                  cache.get(key);
                  gets--;
               }
               else if (decision >= gets && decision < (gets + puts))
               {
                  Object object = objects[entry];
                  cache.put(key, object);
                  puts--;
               }
               else
               {
                  cache.remove(key);
                  removals--;
               }
            }

            //
            return this;
         }
      }

      private static class Key implements Serializable
      {
      }
   }

   private static class SimpleCacheListener implements CacheListener<Serializable, Object>
   {
      public void onGet(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
      }

      public void onExpire(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
         doWait();
      }

      public void onRemove(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
         doWait();
      }

      public void onPut(CacheListenerContext context, Serializable key, Object obj) throws Exception
      {
         doWait();
      }

      public void onClearCache(CacheListenerContext context) throws Exception
      {
         doWait();
      }

      private void doWait() throws Exception
      {
         Thread.sleep(1);
      }
   }

}
