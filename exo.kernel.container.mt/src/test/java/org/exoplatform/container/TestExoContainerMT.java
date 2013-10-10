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

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.jmx.MX4JComponentAdapterMT;
import org.picocontainer.Startable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
               container.getComponentInstanceOfType(TestExoContainer.A1.class);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testBadCyclicRef3() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            RootContainer container = createRootContainer("test-exo-container-mt.xml", "testBadCyclicRef3");
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testBadCyclicRef4() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            try
            {
               createRootContainer("test-exo-container-mt.xml", "testBadCyclicRef4");
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public static class A1
   {
      public B1 b;

      public A1(ExoContainerContext ctx)
      {
         this.b = ctx.getContainer().getComponentInstanceOfType(B1.class);
      }
   }

   public static class B1
   {
      public A1 a;

      public B1(ExoContainerContext ctx)
      {
         this.a = ctx.getContainer().getComponentInstanceOfType(A1.class);
      }
   }

   public static class A2 implements Startable
   {
      public B2 b;

      public A2(ExoContainerContext ctx)
      {
         this.b = ctx.getContainer().getComponentInstanceOfType(B2.class);
      }

      public void start()
      {
      }

      public void stop()
      {
      }
   }

   public static class B2
   {
      public A2 a;

      public B2(ExoContainerContext ctx)
      {
         this.a = ctx.getContainer().getComponentInstanceOfType(A2.class);
      }
   }

   public void testBadCyclicRef5() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            RootContainer container = createRootContainer("test-exo-container-mt.xml", "testBadCyclicRef5");
            try
            {
               container.getComponentInstanceOfType(A3.class);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testBadCyclicRef6() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            try
            {
               createRootContainer("test-exo-container-mt.xml", "testBadCyclicRef6");
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   @Singleton
   public static class A3
   {
      public B3 b;

      @Inject
      public A3(Provider<B3> p)
      {
         this.b = p.get();
      }
   }

   @Singleton
   public static class B3
   {
      public A3 a;

      @Inject
      public B3(Provider<A3> p)
      {
         this.a = p.get();
      }
   }

   @Singleton
   public static class A4 implements Startable
   {
      public B4 b;

      @Inject
      public A4(Provider<B4> p)
      {
         this.b = p.get();
      }

      public void start()
      {
      }

      public void stop()
      {
      }
   }

   @Singleton
   public static class B4
   {
      public A4 a;

      @Inject
      public B4(Provider<A4> p)
      {
         this.a = p.get();
      }
   }

   public void testContainerNameSuffix()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testContainerNameSuffix();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testStartOrder()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStartOrder(true);
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testStartOrder2()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStartOrder2();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testStartOrder3()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testStartOrder3();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
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
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testSortedConstructors()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testSortedConstructors();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testJSR330() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testJSR330();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testScopeWithNoContextManager() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testScopeWithNoContextManager();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testScope() throws Exception
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testScope();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testDefinitionByType()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testDefinitionByType();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testDefinitionByTypeWithProvider()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testDefinitionByTypeWithProvider();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testDefinitionByName()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testDefinitionByName();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testDefinitionByNameWithProvider()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testDefinitionByNameWithProvider();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testDefinitionByQualifier()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testDefinitionByQualifier();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testDefinitionByQualifierWithProvider()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            TestExoContainerMT.super.testDefinitionByQualifierWithProvider();
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public void testAutoSolveDepIssues()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            RootContainer container = createRootContainer("test-exo-container-mt.xml", "testAutoSolveDepIssues");
            MX4JComponentAdapterMT<ASDI_1> adapter1 =
               (MX4JComponentAdapterMT<ASDI_1>)container.getComponentAdapterOfType(ASDI_1.class);
            MX4JComponentAdapterMT<ASDI_2> adapter2 =
               (MX4JComponentAdapterMT<ASDI_2>)container.getComponentAdapterOfType(ASDI_2.class);
            MX4JComponentAdapterMT<ASDI_2_2> adapter3 =
               (MX4JComponentAdapterMT<ASDI_2_2>)container.getComponentAdapterOfType(ASDI_2_2.class);
            if (Mode.hasMode(Mode.AUTO_SOLVE_DEP_ISSUES))
            {
               assertEquals(2, adapter1.getCreateDependencies().size());
               assertEquals(3, adapter1.getInitDependencies().size());
               assertEquals(4, adapter2.getCreateDependencies().size());
               assertEquals(4, adapter2.getInitDependencies().size());
            }
            else
            {
               assertEquals(1, adapter1.getCreateDependencies().size());
               assertEquals(1, adapter1.getInitDependencies().size());
               assertEquals(1, adapter2.getCreateDependencies().size());
               assertEquals(1, adapter2.getInitDependencies().size());
            }
            assertEquals(3, adapter3.getCreateDependencies().size());
            for (Dependency dep : adapter3.getCreateDependencies())
            {
               assertTrue(dep.isLazy());
            }
            assertEquals(3, adapter3.getInitDependencies().size());
            for (Dependency dep : adapter3.getInitDependencies())
            {
               assertTrue(dep.isLazy());
            }
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE);
   }

   public static class ASDI_1 implements Startable
   {
      private ExoContainer container;

      public ASDI_1(ExoContainerContext ctx)
      {
         container = ctx.getContainer();
         container.getComponentInstanceOfType(ASDI_2.class);
      }

      public void addPlugin(ASDI_1Plugin plugin)
      {
         container.getComponentInstanceOfType(ASDI_2.class);
      }

      public void start()
      {
      }

      public void stop()
      {
      }
   }

   public static class ASDI_1Plugin extends BaseComponentPlugin
   {
      public ASDI_1Plugin(ExoContainerContext ctx)
      {
         ctx.getContainer().getComponentInstanceOfType(ASDI_2_2.class);
      }
   }

   @Singleton
   public static class ASDI_2
   {
      @Inject
      public ASDI_2(ExoContainerContext ctx)
      {
         ctx.getContainer().getComponentInstanceOfType(ASDI_3.class);
         ctx.getContainer().getComponentInstance("ASDI_4", ASDI_4.class);
         ctx.getContainer().getComponentInstance(ASDI_5Qualifier.class, ASDI_5.class);
      }

      @Inject
      public void init(ExoContainerContext ctx)
      {
         ctx.getContainer().getComponentInstanceOfType(ASDI_3.class);
         ctx.getContainer().getComponentInstance("ASDI_4", ASDI_4.class);
         ctx.getContainer().getComponentInstance(ASDI_5Qualifier.class, ASDI_5.class);
      }
   }

   @Singleton
   public static class ASDI_2_2
   {
      @Inject
      public ASDI_2_2(Provider<ASDI_3> p1, @Named("ASDI_4") Provider<ASDI_4> p2, @ASDI_5Qualifier Provider<ASDI_5> p3)
      {
         p1.get();
         p2.get();
         p3.get();
      }

      @Inject
      public void init(Provider<ASDI_3> p1, @Named("ASDI_4") Provider<ASDI_4> p2, @ASDI_5Qualifier Provider<ASDI_5> p3)
      {
         p1.get();
         p2.get();
         p3.get();
      }
   }

   public static class ASDI_3
   {

   }

   public static class ASDI_4
   {

   }

   public static class ASDI_5
   {

   }

   @Retention(RetentionPolicy.RUNTIME)
   @Qualifier
   public static @interface ASDI_5Qualifier {
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