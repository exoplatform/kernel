/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.container;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.support.ContainerBuilder;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Disposable;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.Startable;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 3 mai 2010  
 */
public class TestExoContainer extends AbstractTestContainer
{

   public class CachedComponent
   {
      public CachedComponent()
      {
      }

      public int hash()
      {
         return this.hashCode();
      }
   }

   public void testHasProfile()
   {
      String oldValue = PropertyManager.getProperty(PropertyManager.RUNTIME_PROFILES);
      try
      {
         System.clearProperty(PropertyManager.RUNTIME_PROFILES);
         PropertyManager.refresh();
         assertFalse(ExoContainer.hasProfile(null));
         assertFalse(ExoContainer.hasProfile("foo0"));
         PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, "foo1");
         assertFalse(ExoContainer.hasProfile(null));
         assertFalse(ExoContainer.hasProfile("foo0"));
         assertTrue(ExoContainer.hasProfile("foo1"));
         System.clearProperty(PropertyManager.RUNTIME_PROFILES);
         PropertyManager.refresh();
         assertFalse(ExoContainer.hasProfile("foo0"));
         PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, "foo1, foo2, foo3");
         assertFalse(ExoContainer.hasProfile("foo0"));
         assertTrue(ExoContainer.hasProfile("foo1"));
         assertTrue(ExoContainer.hasProfile("foo2"));
         assertTrue(ExoContainer.hasProfile("foo3"));
         PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, "  \tfoo   ");
         assertFalse(ExoContainer.hasProfile("foo0"));
         assertTrue(ExoContainer.hasProfile("foo"));
         PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, ",foo   ");
         assertFalse(ExoContainer.hasProfile("foo0"));
         assertTrue(ExoContainer.hasProfile("foo"));
         PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, "foo, bar, \t baz \t");
         assertFalse(ExoContainer.hasProfile("foo0"));
         assertTrue(ExoContainer.hasProfile("baz"));
         PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, "foo1, bar, \t baz1 \t");
         assertFalse(ExoContainer.hasProfile("foo0"));
         assertFalse(ExoContainer.hasProfile("baz"));
         assertTrue(ExoContainer.hasProfile("bar"));
      }
      finally
      {
         if (oldValue == null)
         {
            System.clearProperty(PropertyManager.RUNTIME_PROFILES);
            PropertyManager.refresh();
         }
         else
         {
            PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, oldValue);
         }
      }
   }

   public void testRemoveComponent() throws Exception
   {
      RootContainer container = RootContainer.getInstance();
      container.registerComponentInstance(new CachedComponent());

      assertNotNull(container.getComponentInstanceOfType(CachedComponent.class));
      container.unregisterComponent(CachedComponent.class);
      assertNull(container.getComponentInstanceOfType(CachedComponent.class));

      container.registerComponent(new DummyAdapter());
      try
      {
         container.unregisterComponentByInstance(new Integer(0));
      }
      catch (Exception e)
      {
         fail("Component unregistration failed");
      }
      container.unregisterComponent("testKey");
   }

   public void testContainerLifecyclePlugin()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
      assertNotNull(counter);
      assertEquals(3, counter.init.size());
      assertEquals(3, counter.start.size());
      container.stop();
      assertEquals(3, counter.stop.size());
      container.dispose();
      assertEquals(3, counter.destroy.size());
      // Check order
      assertTrue(counter.init.get(0) instanceof MyContainerLifecyclePlugin2);
      MyContainerLifecyclePlugin2 plugin = (MyContainerLifecyclePlugin2)counter.init.get(0);
      assertNotNull(plugin.getName());
      assertNotNull(plugin.getDescription());
      assertNotNull(plugin.param);
      assertTrue(counter.init.get(1) instanceof MyContainerLifecyclePlugin3);
      assertTrue(counter.init.get(2) instanceof MyContainerLifecyclePlugin1);
   }

   public void testStackOverFlow()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      MyClass value = (MyClass)container.getComponentInstanceOfType(MyClass.class);
      assertNotNull(value);
      MyClassPlugin plugin = value.plugin_;
      assertNotNull(plugin);
      assertNotNull(plugin.cmanager_);
      assertEquals(value, plugin.myClass_);
   }

   public void testStackOverFlow2()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      SOE1 soe1 = (SOE1)container.getComponentInstanceOfType(SOE1.class);
      assertNotNull(soe1);
      SOEPlugin soe1Plugin = soe1.plugin;
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      assertEquals(soe1, soe1Plugin.soe2.soe1);
   }

   public void testStackOverFlow3()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      SOE2 soe2 = (SOE2)container.getComponentInstanceOfType(SOE2.class);
      assertNotNull(soe2);
      assertNotNull(soe2.soe1);
      SOEPlugin soe1Plugin = soe2.soe1.plugin;
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      assertEquals(soe2.soe1, soe1Plugin.soe2.soe1);
      assertEquals(container.getComponentInstanceOfType(SOE1.class), soe2.soe1);
   }

   public void testCyclicRef()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml", "testCyclicRef");
      A a = (A)container.getComponentInstanceOfType(A.class);
      assertNotNull(a);
      B b = (B)container.getComponentInstanceOfType(B.class);
      assertNotNull(b);
      assertEquals(a, b.a);
   }

   public void testStartOrder()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml", "testStartOrder");
      C0 c0 = (C0)container.getComponentInstanceOfType(C0.class);
      assertNotNull(c0);
      C1 c1 = (C1)container.getComponentInstanceOfType(C1.class);
      assertNotNull(c1);
      C2 c2 = (C2)container.getComponentInstanceOfType(C2.class);
      assertNotNull(c2);
      assertTrue(c1.started);
   }

   public void testCache()
   {
      URL rootURL = getClass().getResource("test-exo-container.xml");
      URL portalURL = getClass().getResource("empty-config.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();

      final RootContainer container = RootContainer.getInstance();
      final ComponentAdapter ca = new ComponentAdapter()
      {

         public void accept(PicoVisitor paramPicoVisitor)
         {
         }

         public Class getComponentImplementation()
         {
            return MyClass.class;
         }

         public Object getComponentInstance(PicoContainer paramPicoContainer) throws PicoInitializationException,
            PicoIntrospectionException
         {
            return new MyClass();
         }

         public Object getComponentKey()
         {
            return "MyKey";
         }

         public void verify(PicoContainer paramPicoContainer) throws PicoIntrospectionException
         {
         }

      };
      container.registerComponent(ca);
      final PortalContainer pcontainer = PortalContainer.getInstance();
      assertEquals(ca, container.getComponentAdapter("MyKey"));
      assertEquals(ca, pcontainer.getComponentAdapter("MyKey"));
      container.unregisterComponent("MyKey");
      assertNull(container.getComponentAdapter("MyKey"));
      assertNull(pcontainer.getComponentAdapter("MyKey"));
   }

   public void testMultiThreading() throws Throwable
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      final AtomicReference<MyMTClass> currentMyClass = new AtomicReference<MyMTClass>();
      testMultiThreading(new Task()
      {

         public void execute()
         {
            MyMTClass value = (MyMTClass)container.getComponentInstance(MyMTClass.class);
            synchronized (currentMyClass)
            {
               if (currentMyClass.get() == null)
               {
                  currentMyClass.set(value);
               }
            }
            assertEquals(currentMyClass.get(), container.getComponentInstance(MyMTClass.class));
         }
      });
      testMultiThreading(new Task()
      {

         public void execute()
         {
            MyMTClass value = (MyMTClass)container.getComponentInstanceOfType(MyMTClass.class);
            synchronized (currentMyClass)
            {
               if (currentMyClass.get() == null)
               {
                  currentMyClass.set(value);
               }
            }
            assertEquals(currentMyClass.get(), container.getComponentInstanceOfType(MyMTClass.class));
         }
      });
      final AtomicReference<ComponentAdapter> ar = new AtomicReference<ComponentAdapter>();
      testMultiThreading(new Task()
      {
         public void execute()
         {
            try
            {
               ComponentAdapter ca = new ComponentAdapter()
               {

                  public void accept(PicoVisitor paramPicoVisitor)
                  {
                  }

                  public Class getComponentImplementation()
                  {
                     return MyClass.class;
                  }

                  public Object getComponentInstance(PicoContainer paramPicoContainer)
                     throws PicoInitializationException, PicoIntrospectionException
                  {
                     return new MyClass();
                  }

                  public Object getComponentKey()
                  {
                     return "a";
                  }

                  public void verify(PicoContainer paramPicoContainer) throws PicoIntrospectionException
                  {
                  }

               };
               ar.compareAndSet(null, container.registerComponent(ca));
            }
            catch (DuplicateComponentKeyRegistrationException e)
            {
               // ignore expected behavior
            }
         }
      });
      testMultiThreading(new Task()
      {

         public void execute()
         {
            assertEquals(ar.get(), container.getComponentAdapter("a"));
         }
      });
      testMultiThreading(new Task()
      {

         public void execute()
         {
            container.unregisterComponent("a");
         }
      });

      testMultiThreading(new Task()
      {

         public void execute()
         {
            final Object key = new Object();
            ComponentAdapter ca = new ComponentAdapter()
            {

               public void accept(PicoVisitor paramPicoVisitor)
               {
               }

               public Class getComponentImplementation()
               {
                  return MyClass.class;
               }

               public Object getComponentInstance(PicoContainer paramPicoContainer) throws PicoInitializationException,
                  PicoIntrospectionException
               {
                  return new MyClass();
               }

               public Object getComponentKey()
               {
                  return key;
               }

               public void verify(PicoContainer paramPicoContainer) throws PicoIntrospectionException
               {
               }

            };
            assertNotNull(container.registerComponent(ca));
            assertNotNull(container.getComponentAdapter(key));
            assertFalse(container.getComponentAdapters().isEmpty());
            assertFalse(container.getComponentAdaptersOfType(MyClass.class).isEmpty());
            assertNotNull(container.getComponentInstance(key));
            assertNotNull(container.getComponentInstanceOfType(MyClass.class));
            assertFalse(container.getComponentInstances().isEmpty());
            assertFalse(container.getComponentInstancesOfType(MyClass.class).isEmpty());
            assertNotNull(container.unregisterComponent(key));
         }
      });
   }

   private void testMultiThreading(final Task task) throws Throwable
   {
      int threads = 50;
      final CountDownLatch startSignal = new CountDownLatch(1);
      final CountDownLatch doneSignal = new CountDownLatch(threads);
      final List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>());
      for (int i = 0; i < threads; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignal.await();
                  task.execute();
               }
               catch (Throwable e)
               {
                  errors.add(e);
               }
               finally
               {
                  doneSignal.countDown();
               }
            }
         };
         thread.start();
      }
      startSignal.countDown();
      doneSignal.await();
      if (!errors.isEmpty())
      {
         for (Throwable e : errors)
         {
            e.printStackTrace();
         }
         throw errors.get(0);
      }
   }

   public interface Task
   {
      public void execute();
   }

   public static class MyMTClass
   {
      public MyMTClass() throws InterruptedException
      {
         // Make the thread wait to ensure that the thread safety issue is properly solved
         Thread.sleep(10);
      }
   }

   public static class MyClass
   {
      public MyClassPlugin plugin_;

      public void add(MyClassPlugin plugin)
      {
         this.plugin_ = plugin;
      }
   }

   public static class MyClassPlugin extends BaseComponentPlugin
   {
      public ConfigurationManager cmanager_;

      public MyClass myClass_;

      public MyClassPlugin(ConfigurationManager cmanager, MyClass myClass)
      {
         this.cmanager_ = cmanager;
         this.myClass_ = myClass;
      }
   }

   public static class MyCounter
   {
      public final List<BaseContainerLifecyclePlugin> init = new ArrayList<BaseContainerLifecyclePlugin>();

      public final List<BaseContainerLifecyclePlugin> start = new ArrayList<BaseContainerLifecyclePlugin>();

      public final List<BaseContainerLifecyclePlugin> stop = new ArrayList<BaseContainerLifecyclePlugin>();

      public final List<BaseContainerLifecyclePlugin> destroy = new ArrayList<BaseContainerLifecyclePlugin>();
   }

   public static class MyContainerLifecyclePlugin1 extends BaseContainerLifecyclePlugin
   {

      public MyContainerLifecyclePlugin1()
      {
      }

      @Override
      public void destroyContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.destroy.add(this);
         }
      }

      @Override
      public void initContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.init.add(this);
         }
      }

      @Override
      public void startContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.start.add(this);
         }
      }

      @Override
      public void stopContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.stop.add(this);
         }
      }

   }

   public static class MyContainerLifecyclePlugin2 extends BaseContainerLifecyclePlugin
   {
      public final String param;

      public MyContainerLifecyclePlugin2(InitParams params)
      {
         this.param = params != null ? params.getValueParam("param").getValue() : null;
      }

      @Override
      public void destroyContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.destroy.add(this);
         }
      }

      @Override
      public void initContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.init.add(this);
         }
      }

      @Override
      public void startContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.start.add(this);
         }
      }

      @Override
      public void stopContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.stop.add(this);
         }
      }
   }

   public static class MyContainerLifecyclePlugin3 extends BaseContainerLifecyclePlugin
   {

      public MyContainerLifecyclePlugin3()
      {
      }

      @Override
      public void destroyContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.destroy.add(this);
         }
      }

      @Override
      public void initContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.init.add(this);
         }
      }

      @Override
      public void startContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.start.add(this);
         }
      }

      @Override
      public void stopContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null)
         {
            counter.stop.add(this);
         }
      }

   }

   public static class A
   {
      public B b;

      public A(B b)
      {
         this.b = b;
      }
   }

   public static class BPlugin extends BaseComponentPlugin
   {
      public A a;

      public BPlugin(A a)
      {
         this.a = a;
      }
   }

   public static class B
   {
      public A a;

      public BPlugin plugin_;

      public void add(BPlugin plugin)
      {
         this.plugin_ = plugin;
         this.a = plugin.a;
      }
   }

   private class DummyAdapter implements ComponentAdapter
   {

      public void verify(PicoContainer arg0) throws PicoIntrospectionException
      {
      }

      public Object getComponentKey()
      {
         return "testKey";
      }

      public Object getComponentInstance(PicoContainer arg0) throws PicoInitializationException,
         PicoIntrospectionException
      {
         // Used to check a situation when RunTimeException occurs while retrieving an instance.
         // This reproduces usecase from JCR-1565
         throw new RuntimeException();
      }

      public Class getComponentImplementation()
      {
         return null;
      }

      public void accept(PicoVisitor arg0)
      {
      }
   }

   public static class C0 implements Startable
   {
      C1 c1;

      public C0(C1 c1)
      {
         this.c1 = c1;
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

   public static class C1 implements Startable
   {
      public boolean started;

      P p;

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         try
         {
            p.init();
            this.started = true;
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }

      public void add(P p)
      {
         this.p = p;
      }
   }

   public static class P extends BaseComponentPlugin
   {
      public C0 c0;

      public C1 c1;

      public C2 c2;

      public P(C0 c0, C1 c1, C2 c2)
      {
         this.c0 = c0;
         this.c1 = c1;
         this.c2 = c2;
      }

      public void init()
      {
         if (!c2.started)
         {
            throw new IllegalStateException("C2 should be started");
         }
      }
   }

   public static class C2 implements Startable
   {
      public boolean started;

      /**
       * @see org.picocontainer.Startable#start()
       */
      public void start()
      {
         started = true;
      }

      /**
       * @see org.picocontainer.Startable#stop()
       */
      public void stop()
      {
      }
   }

   public void testLifeCycle() throws Throwable
   {
      ConcurrentPicoContainer container = new ConcurrentPicoContainer();
      assertTrue(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.registerComponentImplementation(LC1.class);
      container.registerComponentImplementation(LC2.class);
      container.registerComponentImplementation(LC3.class);
      container.registerComponentImplementation(LC4.class);
      container.registerComponentImplementation(LC5.class);
      try
      {
         container.start();
         fail("Should fail due to the start method of C1");
      }
      catch (Exception e)
      {
         // igonre me
      }
      LC1 c1 = (LC1)container.getComponentInstanceOfType(LC1.class);
      LC2 c2 = (LC2)container.getComponentInstanceOfType(LC2.class);
      LC3 c3 = (LC3)container.getComponentInstanceOfType(LC3.class);
      LC4 c4 = (LC4)container.getComponentInstanceOfType(LC4.class);
      LC5 c5 = (LC5)container.getComponentInstanceOfType(LC5.class);
      assertFalse(c2.started && c3.started && c4.started);
      assertTrue(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.stop();
      assertTrue(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.dispose();
      assertTrue(c1.disposed && c2.disposed && c5.disposed);
      assertFalse(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertFalse(container.canBeDisposed());
      container = new ConcurrentPicoContainer();
      assertTrue(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.registerComponentImplementation(LC2.class);
      container.registerComponentImplementation(LC3.class);
      container.registerComponentImplementation(LC4.class);
      container.registerComponentImplementation(LC5.class);
      container.start();
      c2 = (LC2)container.getComponentInstanceOfType(LC2.class);
      c3 = (LC3)container.getComponentInstanceOfType(LC3.class);
      c4 = (LC4)container.getComponentInstanceOfType(LC4.class);
      assertTrue(c2.started && c3.started && c4.started);
      assertFalse(container.canBeStarted());
      assertTrue(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.stop();
      assertTrue(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.dispose();
      assertTrue(c1.disposed && c2.disposed && c5.disposed);
      assertFalse(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertFalse(container.canBeDisposed());
   }

   public static class LC1 implements Startable, Disposable
   {

      public boolean started;

      public boolean stopped;

      public boolean disposed;

      public void start()
      {
         throw new RuntimeException();
      }

      public void stop()
      {
         stopped = true;
      }

      public void dispose()
      {
         disposed = true;
      }
   }

   public static class LC2 implements Startable, Disposable
   {

      public boolean started;

      public boolean stopped;

      public boolean disposed;

      public void start()
      {
         started = true;
      }

      public void stop()
      {
         throw new RuntimeException();
      }

      public void dispose()
      {
         disposed = true;
      }
   }

   public static class LC3 implements Startable, Disposable
   {

      public boolean started;

      public boolean stopped;

      public boolean disposed;

      public void start()
      {
         started = true;
      }

      public void stop()
      {
         stopped = true;
      }

      public void dispose()
      {
         throw new RuntimeException();
      }
   }

   public static class LC4 implements Startable
   {

      public boolean started;

      public boolean stopped;

      public boolean disposed;

      public void start()
      {
         started = true;
      }

      public void stop()
      {
         stopped = true;
      }
   }

   public static class LC5 implements Disposable
   {

      public boolean started;

      public boolean stopped;

      public boolean disposed;

      public void dispose()
      {
         disposed = true;
      }
   }

   public static class SOE1
   {
      public SOEPlugin plugin;

      public void addPlugin(SOEPlugin plugin)
      {
         this.plugin = plugin;
      }
   }

   public static class SOEPlugin extends BaseComponentPlugin
   {
      public SOE2 soe2;

      public SOEPlugin(SOE2 soe2)
      {
         this.soe2 = soe2;
      }
   }

   public static class SOE2
   {
      public SOE1 soe1;

      public SOE2()
      {
         this.soe1 = (SOE1)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SOE1.class);
      }
   }
}
