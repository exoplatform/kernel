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

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestExoContainerMT extends TestExoContainer
{

   public void testHasProfile()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testHasProfile();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testRemoveComponent() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testRemoveComponent();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testContainerLifecyclePlugin()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testContainerLifecyclePlugin();
            return null;
         }
      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlowB() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlowB();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow2()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow2();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow3()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow3();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow2B() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow2B();
            return null;
         }

      };
      //      execute(task, (Mode[])null);
      //      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow3B() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow3B();
            return null;
         }

      };
      //      execute(task, (Mode[])null);
      //      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow2C() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow2C();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow3C() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow3C();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStackOverFlow4()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStackOverFlow4();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testCyclicRef()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testCyclicRef();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testBadCyclicRef() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            RootContainer container = createRootContainer("test-exo-container-mt.xml", "testBadCyclicRef");
            try
            {
               container.getComponentInstanceOfType(A1.class);
               fail("A CyclicDependencyException was expected");
            }
            catch (CyclicDependencyException e)
            {
               // expected exception
            }
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testBadCyclicRef2() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            try
            {
               createRootContainer("test-exo-container-mt.xml", "testBadCyclicRef2");
            }
            catch (CyclicDependencyException e)
            {
               // expected exception
            }
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStartOrder()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStartOrder();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testCache()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testCache();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStart()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStart();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testMultiThreading() throws Throwable
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            try
            {
               TestExoContainerMT.super.testMultiThreading();
            }
            catch (Throwable e)
            {
               throw new Exception(e);
            }
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testLifeCycle() throws Throwable
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            try
            {
               TestExoContainerMT.super.testLifeCycle();
            }
            catch (Throwable e)
            {
               throw new Exception(e);
            }
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testStates() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStates();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testContainerOwner() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testContainerOwner();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public void testContainers() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testContainers();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   private static void execute(Callable<Void> task, Mode... modes)
   {
      try
      {
         Mode.setModes(modes);
         task.call();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         Mode.clearModes();
      }
   }
}