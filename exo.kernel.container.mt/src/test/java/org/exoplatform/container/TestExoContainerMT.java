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

import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.Startable;

import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

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

   public void testContainerNameSuffix()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            final URL rootURL = getClass().getResource("test-exo-container-mt.xml");
            final URL portalURL = getClass().getResource("test-exo-container-mt.xml");
            assertNotNull(rootURL);
            assertNotNull(portalURL);
            final ExoContainer rContainer =
               new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("testContainerNameSuffix")
                  .build();
            final ExoContainer pContainer = PortalContainer.getInstance();
            TCNS t1 = rContainer.getComponentInstanceOfType(TCNS.class);
            assertNotNull(t1);
            assertEquals("empty${container.name.suffix}", t1.value);
            assertNotNull(t1.dep);
            assertEquals("empty${container.name.suffix}", t1.dep.value);
            TCNS t2 = pContainer.getComponentInstanceOfType(TCNS.class);
            assertNotNull(t2);
            assertEquals("empty_portal", t2.value);
            assertNotNull(t2.dep);
            assertEquals("empty_portal", t2.dep.value);
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public static class TCNS implements Startable
   {
      public String value;

      public TCNS_DEP dep;

      public TCNS(InitParams params, TCNS_DEP dep)
      {
         this.dep = dep;
         this.value = params.getValueParam("param").getValue();
      }

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
   }

   public static class TCNS_DEP implements Startable
   {
      public String value;

      public TCNS_DEP(InitParams params)
      {
         this.value = params.getValueParam("param").getValue();
      }

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
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

   public void testStartOrder2()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            COUNTER = new AtomicInteger();
            ExoContainer container = createRootContainer("test-exo-container-mt.xml", "testStartOrder2");
            TSO2_A a = container.getComponentInstanceOfType(TSO2_A.class);
            assertNotNull(a);
            TSO2_B b = container.getComponentInstanceOfType(TSO2_B.class);
            assertNotNull(b);
            TSO2_C c = container.getComponentInstanceOfType(TSO2_C.class);
            assertNotNull(c);
            TSO2_D d = container.getComponentInstanceOfType(TSO2_D.class);
            assertNotNull(d);
            assertTrue(a.startOrder > 0);
            assertTrue(c.startOrder > 0);
            assertTrue(d.startOrder > 0);
            assertTrue(c.startOrder < a.startOrder);
            assertTrue(d.startOrder < a.startOrder);
            assertTrue(c.startOrder < d.startOrder);
            TSO2_A2 a2 = container.getComponentInstanceOfType(TSO2_A2.class);
            assertNotNull(a2);
            TSO2_B2 b2 = container.getComponentInstanceOfType(TSO2_B2.class);
            assertNotNull(b2);
            TSO2_C2 c2 = container.getComponentInstanceOfType(TSO2_C2.class);
            assertNotNull(c2);
            assertTrue(a2.startOrder > 0);
            assertTrue(b2.startOrder > 0);
            assertTrue(c2.startOrder > 0);
            assertTrue(c2.startOrder < b2.startOrder);
            assertTrue(b2.startOrder < a2.startOrder);
            assertTrue(c.startOrder < d.startOrder);
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   public static class TSO2_A implements Startable
   {
      public int startOrder;

      public TSO2_A(TSO2_B b)
      {
      }

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         startOrder = COUNTER.incrementAndGet();
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
   }

   public static class TSO2_B
   {
      public TSO2_B(TSO2_C c, TSO2_D d)
      {
      }
   }

   public static class TSO2_C implements Startable
   {
      public int startOrder;

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         startOrder = COUNTER.incrementAndGet();
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
   }

   public static class TSO2_D implements Startable
   {
      public int startOrder;

      public TSO2_D(TSO2_C c)
      {
      }

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         startOrder = COUNTER.incrementAndGet();
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
   }

   public static class TSO2_A2 implements Startable
   {
      public int startOrder;
      private ExoContainerContext ctx;

      public TSO2_A2(TSO2_B2 b, ExoContainerContext ctx)
      {
         this.ctx = ctx;
      }

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         try
         {
            RequestLifeCycle.begin(ctx.getContainer());
            startOrder = COUNTER.incrementAndGet();
         }
         finally
         {
            RequestLifeCycle.end();
         }
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
   }

   public static class TSO2_B2 implements Startable, ComponentRequestLifecycle
   {
      public int startOrder;

      public TSO2_C2 c;

      public TSO2_B2(TSO2_C2 c)
      {
         this.c = c;
      }

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         try
         {
            RequestLifeCycle.begin(this);
            startOrder = COUNTER.incrementAndGet();
         }
         finally
         {
            RequestLifeCycle.end();
         }
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }

      /**
       * @see org.exoplatform.container.component.ComponentRequestLifecycle#startRequest(org.exoplatform.container.ExoContainer)
       */
      public void startRequest(ExoContainer container)
      {
         if (c.startOrder == 0)
            throw new IllegalStateException("TSO2_C2 should be started");
      }

      /**
       * @see org.exoplatform.container.component.ComponentRequestLifecycle#endRequest(org.exoplatform.container.ExoContainer)
       */
      public void endRequest(ExoContainer container)
      {
      }
   }

   public static class TSO2_C2 implements Startable
   {
      public int startOrder;

      public TSO2_C2()
      {
      }

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         startOrder = COUNTER.incrementAndGet();
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
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