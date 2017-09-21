package org.exoplatform.services.cache.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * This is unit test for the class (@link org.exoplatform.services.cache.concurrent.ConcurrentFIFOExoCache)
 */
public class TestConcurrentFIFOExoCache {

  private int LIMIT = 1000;

  /**
   * This test if ConcurrentFIFOExoCache#put threadsafe
   */
  @Test
  public void testPut() throws Exception {
    ConcurrentFIFOExoCache<String, String> cache = new ConcurrentFIFOExoCache<>(LIMIT);

    //
    CountDownLatch start = new CountDownLatch(1);
    CountDownLatch finish = new CountDownLatch(1000);
    //
    for (int i = 0; i < 1000; i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            start.await(10, TimeUnit.SECONDS);
          } catch (InterruptedException e) {
            Assert.fail("CountDownLatch fail to wait");
          }
          
          for (int j = 0; j < 10000; j++) {
            if (cache.getCacheSize() <= LIMIT) {
              cache.put("", "");
            }
          }
          finish.countDown();
        }
      });
      t.start();
    }
    //
    start.countDown();
    finish.await(1, TimeUnit.MINUTES);

    Assert.assertEquals(LIMIT, cache.getCacheSize());
  }
}
