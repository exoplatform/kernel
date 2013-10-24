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
package org.exoplatform.container.management;

import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestManagementProviderThreadSafety
{
   private static final int TOTAL_THREADS = 50;

   @Rule
   public ContiPerfRule rule = new ContiPerfRule();

   private CyclicBarrier startSignal = new CyclicBarrier(TOTAL_THREADS);
   final AtomicInteger sequence = new AtomicInteger();
   private RootContainer container;

   @Before
   public void setUp()
   {
      this.container = AbstractTestContainer.createRootContainer(getClass(), "configuration1.xml");
   }

   @Test
   @PerfTest(invocations = TOTAL_THREADS, threads = TOTAL_THREADS)
   public void testMultiThreading() throws Throwable
   {
      startSignal.await();
      container.registerComponentInstance("ManagementProviderImpl" + sequence.incrementAndGet(), new ManagementProviderImpl());
   }
}
