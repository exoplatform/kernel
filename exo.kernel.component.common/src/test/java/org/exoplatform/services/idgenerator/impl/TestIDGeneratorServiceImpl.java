/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.services.idgenerator.impl;

import junit.framework.TestCase;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestIDGeneratorServiceImpl extends TestCase
{

   public void testConcurrentCreation() throws Exception
   {
      final IDGeneratorServiceImpl generator = new IDGeneratorServiceImpl();
      final int totalIterations = 15000;
      int totalThreads = 50;
      final AtomicInteger count = new AtomicInteger();
      final AtomicReference<Exception> ex = new AtomicReference<Exception>();
      final ConcurrentMap<String, String> ids = new ConcurrentHashMap<String, String>(totalIterations * totalThreads);
      final CountDownLatch startSignal = new CountDownLatch(1);
      final CountDownLatch doneSignal = new CountDownLatch(totalThreads);

      Runnable task = new Runnable()
      {
         public void run()
         {
            try
            {
               startSignal.await();
               for (int i = 0; i < totalIterations; i++)
               {
                  String id = generator.generateStringID(Long.toString(System.currentTimeMillis()));
                  if (ids.putIfAbsent(id, id) == null)
                  {
                     count.incrementAndGet();
                  }
                  else
                  {
                     throw new IllegalStateException("The id '" + id + "' already exists");
                  }
               }
            }
            catch (Exception e)
            {
               ex.set(e);
            }
            finally
            {
               doneSignal.countDown();
            }
         }
      };
      
      for (int i=0; i<totalThreads; i++)
      {
         new Thread(task).start();
      }
      long time = System.currentTimeMillis();
      startSignal.countDown();
      doneSignal.await();
      if (ex.get() != null)
         throw ex.get();
      System.out.printf("Generated %d ids in %d ms \n", count.get(), (System.currentTimeMillis() - time));
   }
}
