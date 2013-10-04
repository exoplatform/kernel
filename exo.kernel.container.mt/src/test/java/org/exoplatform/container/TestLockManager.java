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
package org.exoplatform.container;

import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestLockManager extends TestCase
{
   private LockManager manager;

   /**
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      manager = LockManager.getInstance();
   }

   /**
    * @see junit.framework.TestCase#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      manager = null;
   }

   public void testNoDeadLock() throws Exception
   {
      int threadCount = 10;
      final CountDownLatch startSignal = new CountDownLatch(1);
      final CountDownLatch endSignal = new CountDownLatch(threadCount);
      final AtomicReference<Exception> ex = new AtomicReference<Exception>();
      Runnable r = new Runnable()
      {
         public void run()
         {
            try
            {
               startSignal.await();
               Lock l = manager.createLock();
               l.lock();
               l.unlock();
               l.lockInterruptibly();
               l.unlock();
               if (l.tryLock())
                  l.unlock();
               else
                  throw new Exception("Could not lock the node using tryLock");
               if (l.tryLock(10, TimeUnit.MILLISECONDS))
                  l.unlock();
               else
                  throw new Exception("Could not lock the node using tryLock with timeout");
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               endSignal.countDown();
            }
         }
      };
      for (int i = 0; i < threadCount; i++)
         new Thread(r).start();
      startSignal.countDown();
      endSignal.await();
      if (ex.get() != null)
         throw ex.get();
      assertTrue(manager.isEmpty());
   }

   public void testDeadlockWith2Threads() throws Exception
   {
      final CyclicBarrier startSignal = new CyclicBarrier(2);
      final CountDownLatch endSignal = new CountDownLatch(2);
      final AtomicReference<Exception> ex = new AtomicReference<Exception>();
      final Lock l1 = manager.createLock();
      final Lock l2 = manager.createLock();
      Thread t1 = new Thread()
      {
         public void run()
         {
            try
            {
               l1.lock();
               startSignal.await();
               l2.lockInterruptibly();
               throw new Exception("Should not occur");
            }
            catch (InterruptedException e)
            {
               // expected
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               l1.unlock();
               endSignal.countDown();
            }
         }
      };
      t1.start();
      Thread t2 = new Thread()
      {
         public void run()
         {
            try
            {
               l2.lock();
               startSignal.await();
               l1.lockInterruptibly();
               throw new Exception("Should not occur");
            }
            catch (InterruptedException e)
            {
               // expected
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               l2.unlock();
               endSignal.countDown();
            }
         }
      };
      t2.start();
      endSignal.await();
      if (ex.get() != null)
         throw ex.get();
      assertTrue(manager.isEmpty());
   }


   public void testDeadlockWith3Threads() throws Exception
   {
      final CyclicBarrier startSignal = new CyclicBarrier(3);
      final CountDownLatch endSignal = new CountDownLatch(3);
      final AtomicReference<Exception> ex = new AtomicReference<Exception>();
      final List<Exception> exceptions = Collections.synchronizedList(new ArrayList<Exception>());
      final Lock l1 = manager.createLock();
      final Lock l2 = manager.createLock();
      final Lock l3 = manager.createLock();
      Thread t1 = new Thread()
      {
         public void run()
         {
            try
            {
               l1.lock();
               startSignal.await();
               l2.lockInterruptibly();
            }
            catch (InterruptedException e)
            {
               // expected
               exceptions.add(e);
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               l1.unlock();
               endSignal.countDown();
            }
         }
      };
      t1.start();
      Thread t2 = new Thread()
      {
         public void run()
         {
            try
            {
               l2.lock();
               startSignal.await();
               l3.lockInterruptibly();
            }
            catch (InterruptedException e)
            {
               // expected
               exceptions.add(e);
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               l2.unlock();
               endSignal.countDown();
            }
         }
      };
      t2.start();
      Thread t3 = new Thread()
      {
         public void run()
         {
            try
            {
               l3.lock();
               startSignal.await();
               l1.lockInterruptibly();
            }
            catch (InterruptedException e)
            {
               // expected
               exceptions.add(e);
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               l3.unlock();
               endSignal.countDown();
            }
         }
      };
      t3.start();
      endSignal.await();
      if (ex.get() != null)
         throw ex.get();
      assertEquals(2, exceptions.size());
      assertTrue(manager.isEmpty());
   }

   public void testDeadlockWithLockNTaskGetFirst() throws Exception
   {
      final CountDownLatch endSignal = new CountDownLatch(2);
      final AtomicReference<Exception> ex = new AtomicReference<Exception>();
      final Lock l = manager.createLock();
      final RunnableFuture<Void> task = manager.createRunnableFuture(new Callable<Void>()
      {
         public Void call() throws Exception
         {
            try
            {
               l.lockInterruptibly();
            }
            catch (InterruptedException e)
            {
               // expected
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            return null;
         }
         
      });
      Thread t1 = new Thread()
      {
         public void run()
         {
            try
            {
               l.lock();
               task.get();
            }
            catch (InterruptedException e)
            {
               // expected
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               l.unlock();
               endSignal.countDown();
            }
         }
      };
      t1.start();
      Thread t2 = new Thread()
      {
         public void run()
         {
            try
            {
               Thread.sleep(100);
               task.run();
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               endSignal.countDown();
            }
         }
      };
      t2.start();
      endSignal.await();
      if (ex.get() != null)
         throw ex.get();
      assertTrue(manager.isEmpty());
   }

   public void testDeadlockWithLockNTaskRunFirst() throws Exception
   {
      final CountDownLatch endSignal = new CountDownLatch(2);
      final AtomicReference<Exception> ex = new AtomicReference<Exception>();
      final Lock l = manager.createLock();
      final RunnableFuture<Void> task = manager.createRunnableFuture(new Callable<Void>()
      {
         public Void call() throws Exception
         {
            try
            {
               l.lockInterruptibly();
            }
            catch (InterruptedException e)
            {
               // expected
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            return null;
         }
         
      });
      Thread t1 = new Thread()
      {
         public void run()
         {
            try
            {
               l.lock();
               Thread.sleep(100);
               task.get();
            }
            catch (InterruptedException e)
            {
               // expected
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               l.unlock();
               endSignal.countDown();
            }
         }
      };
      t1.start();
      Thread t2 = new Thread()
      {
         public void run()
         {
            try
            {
               task.run();
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               endSignal.countDown();
            }
         }
      };
      t2.start();
      endSignal.await();
      if (ex.get() != null)
         throw ex.get();
      assertTrue(manager.isEmpty());
   }
}
