package org.exoplatform.services.cache.concurrent;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This is unit test for the class (@link org.exoplatform.services.cache.concurrent.SynchronizedQueue)
 */
public class TestSynchronizedQueue {

  /**
   * This test if SynchronizedQueue.trim thread-safe by using
   * multiple thread concurrently add and trim items in the queue
   */
  @Test
  public void testTrim() throws Exception {
    SynchronizedQueue queue = new SynchronizedQueue(null);

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
            if (queue.size() <= 1000) {
              queue.add(new Item());
              //trimming the queue, make sure it size is 1000
              queue.trim(1000);
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

    Assert.assertEquals(1000, queue.size());
  }
}
