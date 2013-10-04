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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is used to be aware of all the {@link Lock} currently used to prevent
 * deadlocks
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class LockManager
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.LockManager");

   /**
    * The singleton
    */
   private static final LockManager INSTANCE = new LockManager();

   /**
    * Current lockable resources
    */
   private final ConcurrentMap<Thread, Lockable> locks = new ConcurrentHashMap<Thread, Lockable>();

   private LockManager()
   {
   }

   /**
    * The unique instance of the {@link LockManager}
    */
   public static LockManager getInstance()
   {
      return INSTANCE;
   }

   /**
    * Gives a new {@link Lock} instance
    */
   public Lock createLock()
   {
      return new InternalReentrantLock();
   }

   /**
    * {@inheritDoc}
    */
   public <T> RunnableFuture<T> createRunnableFuture(Runnable runnable, T value)
   {
      return new InternalFutureTask<T>(runnable, value);
   }

   /**
    * {@inheritDoc}
    */
   public <T> RunnableFuture<T> createRunnableFuture(Callable<T> callable)
   {
      return new InternalFutureTask<T>(callable);
   }

   /**
    * Indicates whether or not there are some remaining lockable resources
    */
   boolean isEmpty()
   {
      return locks.isEmpty();
   }

   /**
    * Registers a lockable resource for the current thread
    */
   private void register(Lockable l)
   {
      locks.put(Thread.currentThread(), l);
   }

   /**
    * Unregisters a lockable resource for the current thread
    */
   private void unregister(Lockable l)
   {
      locks.remove(Thread.currentThread(), l);
   }

   /**
    * Checks if there is a deadlock, if so an {@link InterruptedException}
    * will be thrown
    */
   private void checkDeadLock(Lockable l) throws InterruptedException
   {
      if (!l.isLocked())
      {
         LOG.trace("The lock is not locked so we cannot have a deadlock");
         return;
      }
      final Thread owner = l.getOwner();
      if (owner == null || owner == Thread.currentThread())
      {
         LOG.trace("The lock is not locked or the lock owner is the current "
            + "thread so we cannot have a deadlock");
         return;
      }
      Thread currentOwner = owner;
      while (true)
      {
         Lockable lock = locks.get(currentOwner);
         if (lock == null)
         {
            LOG.trace("The owner has no lockable resource to acquire so we cannot have a deadlock");
            return;
         }
         // We first check the locks
         Thread lockToAcquireOwner = lock.getOwner();
         if (lockToAcquireOwner == null)
         {
            LOG.trace("The lockable resource has no owner anymore so we cannot have a deadlock");
            return;
         }
         else if (lockToAcquireOwner == Thread.currentThread())
         {
            // A potential deadlock has been detected
            if (owner == l.getOwner() && l.isLocked())
            {
               LOG.debug("A deadlock has been detected, both threads will be interrupted");
               // The owner did not change so we have a deadlock, so
               // we will interrupt both threads
               owner.interrupt();
               throw new InterruptedException();
            }
            else
            {
               LOG.trace("The owner has changed or the resource is no more locked so we cannot have a deadlock");
               return;
            }
         }
         currentOwner = lockToAcquireOwner;
      }
   }

   /**
    * Internal sub-class of {@link ReentrantLock} needed to be able to register
    * and unregister all the locks automatically
    */
   private class InternalReentrantLock extends ReentrantLock implements Lockable
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = 1696442015918441687L;

      /**
       * {@inheritDoc}
       */
      public Thread getOwner()
      {
         return super.getOwner();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void lock()
      {
         register(this);
         super.lock();
         unregister(this);
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void lockInterruptibly() throws InterruptedException
      {
         register(this);
         try
         {
            checkDeadLock(this);
            super.lockInterruptibly();
         }
         finally
         {
            unregister(this);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean tryLock()
      {
         register(this);
         boolean result = super.tryLock();
         unregister(this);
         return result;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public boolean tryLock(long timeout, TimeUnit unit) throws InterruptedException
      {
         register(this);
         try
         {
            checkDeadLock(this);
            return super.tryLock(timeout, unit);
         }
         finally
         {
            unregister(this);
         }
      }
   }

   /**
    * Internal sub-class of {@link FutureTask} needed to be able to register
    * and unregister all the tasks automatically
    */
   private class InternalFutureTask<V> extends FutureTask<V> implements Lockable
   {
      /**
       * The current owner of exclusive mode synchronization.
       */
      private final AtomicReference<Thread> exclusiveOwnerThread = new AtomicReference<Thread>();

      /**
       * {@inheritDoc}
       */
      public InternalFutureTask(Callable<V> callable)
      {
         super(callable);
      }

      /**
       * {@inheritDoc}
       */
      public InternalFutureTask(Runnable runnable, V result)
      {
         super(runnable, result);
      }

      /**
       * Checks if there is a deadlock, if so it will interrupt the thread waiting for the lock
       */
      private void checkDeadLock()
      {
         try
         {
            LockManager.this.checkDeadLock(this);
         }
         catch (InterruptedException e)
         {
            LOG.debug("An InterruptedException has been caught, but a task must not be interrupted");
         }
      }


      /**
       * {@inheritDoc}
       */
      @Override
      public V get() throws InterruptedException, ExecutionException
      {
         register(this);
         checkDeadLock();
         try
         {
            return super.get();
         }
         finally
         {
            unregister(this);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
      {
         register(this);
         checkDeadLock();
         try
         {
            return super.get(timeout, unit);
         }
         finally
         {
            unregister(this);
         }
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public void run()
      {
         exclusiveOwnerThread.compareAndSet(null, Thread.currentThread());
         try
         {
            super.run();
         }
         finally
         {
            exclusiveOwnerThread.compareAndSet(Thread.currentThread(), null);
         }
      }

      /**
       * Gives the Owner of the task
       */
      public Thread getOwner()
      {
         return exclusiveOwnerThread.get();
      }

      /**
       * Indicates whether the task is locked or not, in practice it will be considered as locked if it is not done
       */
      public boolean isLocked()
      {
         return !isDone();
      }
   }

   /**
    * Defines a lockable resource
    */
   private static interface Lockable
   {
      /**
       * Gives the owner in case the resource is locked
       */
      Thread getOwner();

      /**
       * Indicates whether the resource is locked or not
       */
      boolean isLocked();
   }
}
