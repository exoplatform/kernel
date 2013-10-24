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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.exoplatform.container.TestExoContainer.MyClass;
import org.exoplatform.container.TestExoContainer.MyMTClass;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestExoContainerThreadSafety
{
   protected static final int TOTAL_THREADS = 50;

   @Rule
   public ContiPerfRule rule = new ContiPerfRule();

   private CyclicBarrier startSignal = new CyclicBarrier(TOTAL_THREADS);
   private AtomicReference<MyMTClass> currentMyClass = new AtomicReference<MyMTClass>();
   private AtomicReference<ComponentAdapter<?>> ar = new AtomicReference<ComponentAdapter<?>>();
   private RootContainer container;

   @Before
   public void setUp()
   {
      this.container = AbstractTestContainer.createRootContainer(getClass(), "test-exo-container.xml");
   }

   @Test
   @PerfTest(invocations = TOTAL_THREADS, threads = TOTAL_THREADS)
   public void getComponentInstance() throws InterruptedException, BrokenBarrierException
   {
      // Needed to make sure that all threads start at the same time
      startSignal.await();
      MyMTClass value = (MyMTClass)container.getComponentInstance(MyMTClass.class);
      currentMyClass.compareAndSet(null, value);
      assertEquals(currentMyClass.get(), container.getComponentInstance(MyMTClass.class));
      
   }

   @Test
   @PerfTest(invocations = TOTAL_THREADS, threads = TOTAL_THREADS)
   public void getComponentInstanceOfType() throws InterruptedException, BrokenBarrierException
   {
      // Needed to make sure that all threads start at the same time
      startSignal.await();
      MyMTClass value = (MyMTClass)container.getComponentInstanceOfType(MyMTClass.class);
      currentMyClass.compareAndSet(null, value);
      assertEquals(currentMyClass.get(), container.getComponentInstanceOfType(MyMTClass.class));
   }

   @Test
   @PerfTest(invocations = TOTAL_THREADS, threads = TOTAL_THREADS)
   public void registerGetNUnregister() throws InterruptedException, BrokenBarrierException
   {
      // Needed to make sure that all threads start at the same time
      startSignal.await();
      try
      {
         ar.compareAndSet(null, container.registerComponentInstance("a", new MyClass()));
      }
      catch (ContainerException e)
      {
         // ignore expected behavior
      }
      // Needed to make sure that all threads start at the same time
      startSignal.await();
      assertEquals(ar.get(), container.getComponentAdapter("a"));
      // Needed to make sure that all threads start at the same time
      startSignal.await();
      container.unregisterComponent("a");
   }

   @Test
   @PerfTest(invocations = TOTAL_THREADS, threads = TOTAL_THREADS)
   public void callMainMethods() throws InterruptedException, BrokenBarrierException
   {
      // Needed to make sure that all threads start at the same time
      startSignal.await();
      final Object key = new Object();
      assertNotNull(container.registerComponentInstance(key, new MyClass()));
      assertNotNull(container.getComponentAdapter(key));
      assertFalse(container.getComponentAdapters().isEmpty());
      assertFalse(container.getComponentAdaptersOfType(MyClass.class).isEmpty());
      assertNotNull(container.getComponentInstance(key));
      assertNotNull(container.getComponentInstanceOfType(MyClass.class));
      assertFalse(container.getComponentInstancesOfType(MyClass.class).isEmpty());
      assertNotNull(container.unregisterComponent(key));
   }
}
