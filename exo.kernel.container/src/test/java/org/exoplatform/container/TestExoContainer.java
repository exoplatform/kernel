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
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.Disposable;
import org.picocontainer.Startable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
import javax.inject.Singleton;

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
      ConcurrentContainer container = new ConcurrentContainer(RootContainer.getInstance(), null);
      container.registerComponentInstance(CachedComponent.class, new CachedComponent());

      assertNotNull(container.getComponentInstanceOfType(CachedComponent.class));
      container.unregisterComponent(CachedComponent.class);
      assertNull(container.getComponentInstanceOfType(CachedComponent.class));

      container.registerComponent(new DummyAdapter());
      try
      {
         container.getComponentInstanceOfType(DummyClass.class);
         fail("A RuntimeException is expected");
      }
      catch (RuntimeException e)
      {
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
      assertSame(value, plugin.myClass_);
      MyClassPlugin2 plugin2 = value.plugin2_;
      assertNotNull(plugin2);
      assertNotNull(plugin2.cmanager_);
      assertSame(value, plugin2.myClass_);
   }

   public void testStackOverFlowB() throws Exception
   {
      final URL rootURL = getClass().getResource("empty-config.xml");
      final URL portalURL = getClass().getResource("test-exo-container.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("testStackOverFlowB").build();
      final ExoContainer container = PortalContainer.getInstance();
      MyClassB value = (MyClassB)container.getComponentInstanceOfType(MyClassB.class);
      assertNotNull(value);
      assertTrue(value.started);
      MyClassPluginB plugin = value.plugin_;
      assertNotNull(plugin);
      assertNotNull(plugin.cmanager_);
      assertSame(value, plugin.myClass_);
      MyClassPlugin2B plugin2 = value.plugin2_;
      assertNotNull(plugin2);
      assertNotNull(plugin2.cmanager_);
      assertSame(value, plugin2.myClass_);
   }

   public void testStackOverFlow2()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      SOE1 soe1 = (SOE1)container.getComponentInstanceOfType(SOE1.class);
      assertNotNull(soe1);
      assertEquals(1, soe1.plugins.size());
      SOEPlugin soe1Plugin = soe1.plugins.get(0);
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      assertSame(soe1, soe1Plugin.soe2.soe1);
      assertSame(container.getComponentInstanceOfType(SOE2.class), soe1Plugin.soe2);
   }

   public void testStackOverFlow3()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      SOE2 soe2 = (SOE2)container.getComponentInstanceOfType(SOE2.class);
      assertNotNull(soe2);
      assertNotNull(soe2.soe1);
      assertEquals(1, soe2.soe1.plugins.size());
      SOEPlugin soe1Plugin = soe2.soe1.plugins.get(0);
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      assertSame(soe2.soe1, soe1Plugin.soe2.soe1);
      assertSame(container.getComponentInstanceOfType(SOE1.class), soe2.soe1);
   }

   public void testStackOverFlow2B() throws Exception
   {
      final URL rootURL = getClass().getResource("test-exo-container.xml");
      final URL portalURL = getClass().getResource("test-exo-container-portal-mode.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      COUNTER = new AtomicInteger();
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("Case-B").build();
      final ExoContainer container = PortalContainer.getInstance();
      SOE1B soe1 = (SOE1B)container.getComponentInstanceOfType(SOE1B.class);
      assertNotNull(soe1);
      //      assertEquals(1, soe1.startOrder);
      assertTrue(soe1.startOrder >= 1);
      assertEquals(1, soe1.plugins.size());
      SOEPluginB soe1Plugin = soe1.plugins.get(0);
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      //      assertEquals(2, soe1Plugin.soe2.startOrder);
      assertTrue(soe1Plugin.soe2.startOrder >= 1);
      assertSame(soe1, soe1Plugin.soe2.soe1);
      assertSame(container.getComponentInstanceOfType(SOE2B.class), soe1Plugin.soe2);
   }

   public void testStackOverFlow3B() throws Exception
   {
      final URL rootURL = getClass().getResource("test-exo-container.xml");
      final URL portalURL = getClass().getResource("test-exo-container-portal-mode.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      COUNTER = new AtomicInteger();
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("Case-B").build();
      final ExoContainer container = PortalContainer.getInstance();
      SOE2B soe2 = (SOE2B)container.getComponentInstanceOfType(SOE2B.class);
      assertNotNull(soe2);
      //      assertEquals(2, soe2.startOrder);
      assertTrue(soe2.startOrder >= 1);
      assertNotNull(soe2.soe1);
      assertEquals(1, soe2.soe1.plugins.size());
      SOEPluginB soe1Plugin = soe2.soe1.plugins.get(0);
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      assertSame(soe2.soe1, soe1Plugin.soe2.soe1);
      //      assertEquals(1, soe2.soe1.startOrder);
      assertTrue(soe2.soe1.startOrder >= 1);
      assertSame(container.getComponentInstanceOfType(SOE1B.class), soe2.soe1);
   }

   public void testStackOverFlow2C() throws Exception
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      SOE1C soe1 = (SOE1C)container.getComponentInstanceOfType(SOE1C.class);
      assertNotNull(soe1);
      assertEquals(2, soe1.plugins.size());
      SOEPluginC soe1Plugin = (SOEPluginC)soe1.plugins.get(1);
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      assertSame(soe1, soe1Plugin.soe2.soe1);
      assertSame(container.getComponentInstanceOfType(SOE2C.class), soe1Plugin.soe2);
   }

   public void testStackOverFlow3C() throws Exception
   {
      final RootContainer container = createRootContainer("test-exo-container.xml");
      SOE2C soe2 = (SOE2C)container.getComponentInstanceOfType(SOE2C.class);
      assertNotNull(soe2);
      assertNotNull(soe2.soe1);
      assertEquals(2, soe2.soe1.plugins.size());
      SOEPluginC soe1Plugin = (SOEPluginC)soe2.soe1.plugins.get(1);
      assertNotNull(soe1Plugin);
      assertNotNull(soe1Plugin.soe2);
      assertSame(soe2.soe1, soe1Plugin.soe2.soe1);
      assertSame(container.getComponentInstanceOfType(SOE1C.class), soe2.soe1);
   }

   public void testStackOverFlow4()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml", "testStackOverflowError");
      MyService ms = (MyService)container.getComponentInstanceOfType(MyService.class);
      assertNotNull(ms);
      assertTrue(ms instanceof MyServiceImpl);
      MyServiceImpl msi = (MyServiceImpl)ms;
      assertNotNull(msi.componentPlugin);
      assertTrue(msi.componentPlugin instanceof MyPlugin);
      MyPlugin mp = (MyPlugin)msi.componentPlugin;
      assertSame(mp.svc, ms);
   }

   public void testCyclicRef()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml", "testCyclicRef");
      A a = (A)container.getComponentInstanceOfType(A.class);
      assertNotNull(a);
      B b = (B)container.getComponentInstanceOfType(B.class);
      assertNotNull(b);
      assertSame(a, b.a);
   }

   public void testStartOrder()
   {
      COUNTER = new AtomicInteger();
      final RootContainer container = createRootContainer("test-exo-container.xml", "testStartOrder");
      C0 c0 = (C0)container.getComponentInstanceOfType(C0.class);
      assertNotNull(c0);
      //      assertEquals(3, c0.startOrder);
      assertTrue(c0.startOrder > 0);
      C1 c1 = (C1)container.getComponentInstanceOfType(C1.class);
      assertNotNull(c1);
      //      assertEquals(2, c1.startOrder);
      assertTrue(c1.startOrder > 0);
      C2 c2 = (C2)container.getComponentInstanceOfType(C2.class);
      assertNotNull(c2);
      //      assertEquals(1, c2.startOrder);
      assertTrue(c2.startOrder > 0);
   }

   public void testCache()
   {
      URL rootURL = getClass().getResource("test-exo-container.xml");
      URL portalURL = getClass().getResource("empty-config.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();

      RootContainer container = RootContainer.getInstance();
      Object value = new MyClass();
      ComponentAdapter<?> ca = container.registerComponentInstance("MyKey", value);
      PortalContainer pcontainer = PortalContainer.getInstance();
      assertSame(ca, container.getComponentAdapter("MyKey"));
      assertSame(ca, pcontainer.getComponentAdapter("MyKey"));
      assertSame(ca, container.getComponentAdapter("MyKey", MyClass.class));
      assertSame(ca, pcontainer.getComponentAdapter("MyKey", MyClass.class));
      try
      {
         container.getComponentAdapter("MyKey", String.class);
         fail("A ClassCastException was expected");
      }
      catch (ClassCastException e)
      {
         // ok
      }
      try
      {
         pcontainer.getComponentAdapter("MyKey", String.class);
         fail("A ClassCastException was expected");
      }
      catch (ClassCastException e)
      {
         // ok
      }
      assertSame(value, container.getComponentInstance("MyKey"));
      assertSame(value, pcontainer.getComponentInstance("MyKey"));
      assertSame(value, container.getComponentInstance("MyKey", MyClass.class));
      assertSame(value, pcontainer.getComponentInstance("MyKey", MyClass.class));
      try
      {
         container.getComponentInstance("MyKey", String.class);
         fail("A ClassCastException was expected");
      }
      catch (ClassCastException e)
      {
         // ok
      }
      try
      {
         pcontainer.getComponentInstance("MyKey", String.class);
         fail("A ClassCastException was expected");
      }
      catch (ClassCastException e)
      {
         // ok
      }
      container.unregisterComponent("MyKey");
      assertNull(container.getComponentAdapter("MyKey"));
      assertNull(pcontainer.getComponentAdapter("MyKey"));
      assertNull(container.getComponentAdapter("MyKey", MyClass.class));
      assertNull(pcontainer.getComponentAdapter("MyKey", MyClass.class));
      assertNull(container.getComponentAdapter("MyKey", String.class));
      assertNull(pcontainer.getComponentAdapter("MyKey", String.class));
      assertNull(container.getComponentInstance("MyKey"));
      assertNull(pcontainer.getComponentInstance("MyKey"));
      assertNull(container.getComponentInstance("MyKey", MyClass.class));
      assertNull(pcontainer.getComponentInstance("MyKey", MyClass.class));
      assertNull(container.getComponentInstance("MyKey", String.class));
      assertNull(pcontainer.getComponentInstance("MyKey", String.class));
   }

   public void testStart()
   {
      URL rootURL = getClass().getResource("test-exo-container.xml");
      URL portalURL = getClass().getResource("empty-config.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("testStart").build();

      RootContainer container = RootContainer.getInstance();
      PortalContainer pcontainer = PortalContainer.getInstance();
      container.stop();
      container.dispose();
      assertNotNull(container.getComponentInstanceOfType(TS1.class));
      assertNotNull(container.getComponentInstanceOfType(TS2.class));
      assertNotNull(container.getComponentInstanceOfType(TS3.class));
      assertNotNull(container.getComponentInstanceOfType(TS4.class));
      assertNotNull(pcontainer.getComponentInstanceOfType(TS1.class));
      assertNotNull(pcontainer.getComponentInstanceOfType(TS2.class));
      assertNotNull(pcontainer.getComponentInstanceOfType(TS3.class));
      assertNotNull(pcontainer.getComponentInstanceOfType(TS4.class));
      TS1 ts1rc = container.getComponentInstanceOfType(TS1.class);
      TS1 ts1pc = pcontainer.getComponentInstanceOfType(TS1.class);
      assertSame(ts1rc, ts1pc);
      assertEquals(1, ts1pc.started);
      assertEquals(1, ts1pc.stopped);
      TS2 ts2rc = container.getComponentInstanceOfType(TS2.class);
      TS2 ts2pc = pcontainer.getComponentInstanceOfType(TS2.class);
      assertSame(ts2rc, ts2pc);
      assertEquals(1, ts2rc.disposed);
      TS3 ts3rc = container.getComponentInstanceOfType(TS3.class);
      TS3 ts3pc = pcontainer.getComponentInstanceOfType(TS3.class);
      assertNotSame(ts3rc, ts3pc);
      assertEquals(1, ts3rc.started);
      assertEquals(1, ts3pc.started);
      assertEquals(1, ts3rc.stopped);
      assertEquals(1, ts3pc.stopped);
      TS4 ts4rc = container.getComponentInstanceOfType(TS4.class);
      TS4 ts4pc = pcontainer.getComponentInstanceOfType(TS4.class);
      assertNotSame(ts4rc, ts4pc);
      assertEquals(1, ts4rc.disposed);
      assertEquals(1, ts4pc.disposed);
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
      final AtomicReference<ComponentAdapter<?>> ar = new AtomicReference<ComponentAdapter<?>>();
      testMultiThreading(new Task()
      {
         public void execute()
         {
            try
            {
               ar.compareAndSet(null, container.registerComponentInstance("a", new MyClass()));
            }
            catch (ContainerException e)
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
            assertNotNull(container.registerComponentInstance(key, new MyClass()));
            assertNotNull(container.getComponentAdapter(key));
            assertFalse(container.getComponentAdapters().isEmpty());
            assertFalse(container.getComponentAdaptersOfType(MyClass.class).isEmpty());
            assertNotNull(container.getComponentInstance(key));
            assertNotNull(container.getComponentInstanceOfType(MyClass.class));
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

      public MyClassPlugin2 plugin2_;

      public void add(MyClassPlugin plugin)
      {
         this.plugin_ = plugin;
      }

      public void add(MyClassPlugin2 plugin)
      {
         this.plugin2_ = plugin;
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

   public static class MyClassPlugin2 extends BaseComponentPlugin
   {
      public ConfigurationManager cmanager_;

      public MyClass myClass_;

      public MyClassPlugin2(ConfigurationManager cmanager)
      {
         this.cmanager_ = cmanager;
         this.myClass_ = (MyClass)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MyClass.class);
      }
   }

   public static class MyClassB implements Startable
   {
      public MyClassPluginB plugin_;

      public MyClassPlugin2B plugin2_;

      public boolean started;

      public void add(MyClassPluginB plugin)
      {
         this.plugin_ = plugin;
      }

      public void add(MyClassPlugin2B plugin)
      {
         this.plugin2_ = plugin;
      }

      public void start()
      {
         started = true;
      }

      public void stop()
      {
      }
   }

   public static class MyClassPluginB extends BaseComponentPlugin
   {
      public ConfigurationManager cmanager_;

      public MyClassB myClass_;

      public MyClassPluginB(ConfigurationManager cmanager, MyClassB myClass)
      {
         this.cmanager_ = cmanager;
         this.myClass_ = myClass;
      }
   }

   public static class MyClassPlugin2B extends BaseComponentPlugin
   {
      public ConfigurationManager cmanager_;

      public MyClassB myClass_;

      public MyClassPlugin2B(ConfigurationManager cmanager)
      {
         this.cmanager_ = cmanager;
         this.myClass_ = (MyClassB)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(MyClassB.class);
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

   public static class A1
   {
      public B1 b;

      public A1(B1 b)
      {
         this.b = b;
      }
   }

   public static class B1
   {
      public A1 a;

      public B1(A1 a)
      {
         this.a = a;
      }
   }

   public static class A2 implements Startable
   {
      public B2 b;

      public A2(B2 b)
      {
         this.b = b;
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

      public B2(A2 a)
      {
         this.a = a;
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

   private class DummyAdapter implements ComponentAdapter<DummyClass>
   {

      public Object getComponentKey()
      {
         return "testKey";
      }

      public DummyClass getComponentInstance()
      {
         // Used to check a situation when RunTimeException occurs while retrieving an instance.
         // This reproduces usecase from JCR-1565
         throw new RuntimeException();
      }

      public Class<DummyClass> getComponentImplementation()
      {
         return DummyClass.class;
      }

      public boolean isSingleton()
      {
         return true;
      }
   }

   public static class DummyClass
   {
   }

   public static AtomicInteger COUNTER;

   public static class C0 implements Startable
   {
      public int startOrder;

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
         startOrder = COUNTER.incrementAndGet();
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
      public int startOrder;

      P p;

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
   }

   public static class C2 implements Startable
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

   public void testLifeCycle() throws Throwable
   {
      ExoContainer container = new ExoContainer();
      assertTrue(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.registerComponentImplementation(LC1.class, LC1.class);
      container.registerComponentImplementation(LC2.class, LC2.class);
      container.registerComponentImplementation(LC3.class, LC3.class);
      container.registerComponentImplementation(LC4.class, LC4.class);
      container.registerComponentImplementation(LC5.class, LC5.class);
      try
      {
         container.start();
         fail("Should fail due to the start method of C1");
      }
      catch (Exception e)
      {
         // ignore me
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
      container = new ExoContainer();
      assertTrue(container.canBeStarted());
      assertFalse(container.canBeStopped());
      assertTrue(container.canBeDisposed());
      container.registerComponentImplementation(LC2.class, LC2.class);
      container.registerComponentImplementation(LC3.class, LC3.class);
      container.registerComponentImplementation(LC4.class, LC4.class);
      container.registerComponentImplementation(LC5.class, LC5.class);
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

      public boolean disposed;

      public void dispose()
      {
         disposed = true;
      }
   }

   public static class SOE1
   {
      public List<SOEPlugin> plugins = new ArrayList<SOEPlugin>();

      public void addPlugin(SOEPlugin plugin)
      {
         plugins.add(plugin);
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

   public static class SOE1B implements Startable
   {
      public List<SOEPluginB> plugins = new ArrayList<SOEPluginB>();

      public int startOrder;

      public void addPlugin(SOEPluginB plugin)
      {
         plugins.add(plugin);
      }

      public void start()
      {
         startOrder = COUNTER.incrementAndGet();
      }

      public void stop()
      {
      }
   }

   public static class SOEPluginB extends BaseComponentPlugin
   {
      public SOE2B soe2;

      public SOEPluginB(SOE2B soe2)
      {
         this.soe2 = soe2;
      }
   }

   public static class SOE2B implements Startable
   {
      public SOE1B soe1;

      public int startOrder;

      public SOE2B()
      {
         this.soe1 = (SOE1B)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SOE1B.class);
      }

      public void start()
      {
         startOrder = COUNTER.incrementAndGet();
      }

      public void stop()
      {
      }
   }

   public static class SOE1C
   {
      public List<SOEPluginCR> plugins = new ArrayList<SOEPluginCR>();

      public void addPlugin(SOEPluginCR plugin)
      {
         plugins.add(plugin);
      }
   }

   public static class SOEPluginCR extends BaseComponentPlugin
   {
      public SOEPluginCR()
      {
      }
   }

   public static class SOEPluginC extends SOEPluginCR
   {
      public SOE2C soe2;

      public SOEPluginC()
      {
         this.soe2 = (SOE2C)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(SOE2C.class);
      }
   }

   public static class SOE2C
   {
      public SOE1C soe1;

      public SOE2C(SOE1C soe1)
      {
         this.soe1 = soe1;
      }
   }

   public static class MyPlugin extends BaseComponentPlugin
   {
      MySpecialService svc;

      public MyPlugin(MySpecialService svc)
      {
         this.svc = svc;
      }
   }

   public static interface MyService
   {
      public void addPlugin(ComponentPlugin componentPlugin);
   }

   public static interface MySpecialService extends MyService
   {
   }

   public static class MyServiceImpl implements MySpecialService, Startable
   {
      ComponentPlugin componentPlugin;

      public MyServiceImpl()
      {
      }

      public void addPlugin(ComponentPlugin componentPlugin)
      {
         this.componentPlugin = componentPlugin;
      }

      public void stop()
      {
      }

      public void start()
      {
      }
   }

   public static class TS1 implements Startable
   {

      public int started;

      public int stopped;

      public void start()
      {
         ++started;
      }

      public void stop()
      {
         ++stopped;
      }
   }

   public static class TS2 implements Disposable
   {
      public int disposed;

      public void dispose()
      {
         ++disposed;
      }
   }

   public static class TS3 implements Startable
   {

      public int started;

      public int stopped;

      public void start()
      {
         ++started;
      }

      public void stop()
      {
         ++stopped;
      }
   }

   public static class TS4 implements Disposable
   {
      public int disposed;

      public void dispose()
      {
         ++disposed;
      }
   }

   public void testStates() throws Exception
   {
      final RootContainer container = createRootContainer("test-exo-container.xml", "testStates");
      TSC1 value = (TSC1)container.getComponentInstanceOfType(TSC1.class);
      assertNotNull(value);
      TSC2Plugin plugin = value.plugin;
      assertNotNull(plugin);
      assertNotNull(plugin.tsc2);
      assertNotNull(plugin.tsc2.plugin);
      TSC2 value2 = (TSC2)container.getComponentInstanceOfType(TSC2.class);
      assertNotNull(value2);
      assertSame(value2, plugin.tsc2);
      TSC2Plugin plugin2 = value2.plugin;
      assertNotNull(plugin2);
      assertNotNull(plugin2.tsc2);
      assertSame(value2, plugin2.tsc2);
      assertSame(plugin.tsc2.plugin, plugin2);
   }

   public static class TSC1
   {
      TSC2Plugin plugin;

      public void addPlugin(TSC2Plugin plugin)
      {
         this.plugin = plugin;
      }
   }

   public static class TSC2
   {
      TSC2Plugin plugin;

      public void addPlugin(TSC2Plugin plugin)
      {
         this.plugin = plugin;
      }
   }

   public static class TSC2Plugin extends BaseComponentPlugin
   {
      TSC2 tsc2;

      public TSC2Plugin(TSC2 tsc2)
      {
         this.tsc2 = tsc2;
      }
   }

   private static ExoContainer parent;

   public void testContainerOwner() throws Exception
   {
      try
      {
         parent = new ExoContainer();
         parent.registerComponentImplementation(ContainerOwner.class);
         parent.start();
      }
      finally
      {
         parent = null;
      }
   }

   public static class ContainerOwner implements Startable
   {
      ExoContainer container;

      public void start()
      {
         container = new ExoContainer(parent);
         parent.registerComponentInstance("TestContainerOwner", container);
         container.registerComponentImplementation(ContainerOwnerHolder.class);
         container.start();
      }

      public void stop()
      {
      }
   }

   public static class ContainerOwnerHolder implements Startable
   {
      ContainerOwner co;

      public ContainerOwnerHolder(ContainerOwner co)
      {
         this.co = co;
      }

      public void start()
      {
      }

      public void stop()
      {
      }
   }

   public void testContainers() throws Exception
   {
      final URL rootURL = getClass().getResource("test-exo-container.xml");
      final URL portalURL = getClass().getResource("test-exo-container.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("testContainers").build();

      ExoContainer container = PortalContainer.getInstance();
      CCTC1 c1 = (CCTC1)container.getComponentInstanceOfType(CCTC1.class);
      assertNotNull(c1);
      assertNotNull(c1.container);
      assertNotNull(c1.c2);
      assertNotNull(c1.c2.container);
      assertNotNull(c1.c3);
      assertNotNull(c1.c3.container);
      assertSame(c1.container, c1.c2.container);
      assertSame(c1.container, c1.c3.container);
      assertSame(container, c1.container);
   }

   public static class CCTC1
   {
      public ExoContainer container;

      public CCTC2 c2;

      public CCTC3 c3;

      public CCTC1(CCTC2 c2, CCTC3 c3)
      {
         this.container = ExoContainerContext.getCurrentContainer();
         this.c2 = c2;
         this.c3 = c3;
      }
   }

   public static class CCTC2
   {
      public ExoContainer container;

      public CCTC2()
      {
         this.container = ExoContainerContext.getCurrentContainer();
      }
   }

   public static class CCTC3
   {
      public ExoContainer container;

      public CCTC3()
      {
         this.container = ExoContainerContext.getCurrentContainer();
      }
   }

   public void testJSR330() throws Exception
   {
      RootContainer container = createRootContainer("empty-config.xml");
      container.registerComponentImplementation(JSR330_A.class);
      container.registerComponentImplementation(JSR330_B.class);
      container.registerComponentImplementation(JSR330_C.class);
      container.registerComponentImplementation(JSR330_C2.class);
      container.registerComponentImplementation(JSR330_C3.class);
      container.registerComponentImplementation(JSR330_C4.class);
      container.registerComponentImplementation(JSR330_C5.class);
      container.registerComponentImplementation(JSR330_C6.class);
      container.registerComponentImplementation(JSR330_D.class);
      container.registerComponentImplementation(JSR330_P1.class);
      container.registerComponentImplementation(JSR330_P2.class);
      container.registerComponentImplementation(JSR330_P3.class);
      container.registerComponentImplementation(JSR330_P4.class);
      container.registerComponentImplementation(N1.class, JSR330_N1.class);
      container.registerComponentImplementation("n2", JSR330_N2.class);
      JSR330_A a = container.getComponentInstanceOfType(JSR330_A.class);
      assertNotNull(a);
      JSR330_P1 p1 = container.getComponentInstanceOfType(JSR330_P1.class);
      assertNotNull(p1);
      JSR330_P2 p2 = container.getComponentInstanceOfType(JSR330_P2.class);
      assertNotNull(p2);
      JSR330_B b = container.getComponentInstanceOfType(JSR330_B.class);
      assertNotNull(b);
      assertSame(a, b.a);
      assertSame(a, b.a2);
      assertSame(a, b.a3);
      assertSame(a, b.a4);
      assertSame(a, b.getA5());
      assertNull(b.a6);
      assertNull(JSR330_B.a7);
      assertNotNull(b.p1);
      assertNotNull(b.n);
      assertTrue(b.n instanceof JSR330_N2);
      assertNotNull(b.n2);
      assertTrue(b.n2 instanceof JSR330_N1);
      assertSame(container.getComponentInstanceOfType(JSR330_P1.class), b.p1.get());
      JSR330_C c = container.getComponentInstanceOfType(JSR330_C.class);
      assertNotNull(c);
      assertNotSame(c, container.getComponentInstanceOfType(JSR330_C.class));
      assertSame(a, c.a);
      assertNotNull(c.p2);
      assertNotNull(c.n);
      assertTrue(c.n instanceof JSR330_N2);
      assertNotNull(c.n2);
      assertTrue(c.n2 instanceof JSR330_N1);
      assertNotSame(container.getComponentInstanceOfType(JSR330_P2.class), c.p2.get());
      JSR330_C2 c2 = container.getComponentInstanceOfType(JSR330_C2.class);
      assertNotNull(c2);
      assertSame(a, c2.a);
      JSR330_C3 c3 = container.getComponentInstanceOfType(JSR330_C3.class);
      assertNotNull(c3);
      assertSame(a, c3.a);
      JSR330_C4 c4 = container.getComponentInstanceOfType(JSR330_C4.class);
      assertNotNull(c4);
      assertSame(a, c4.a);
      try
      {
         container.getComponentInstanceOfType(JSR330_C5.class);
         fail("A Runtime Exception was expected");
      }
      catch (RuntimeException e)
      {
         // OK
      }
      try
      {
         container.getComponentInstanceOfType(JSR330_C6.class);
         fail("A Runtime Exception was expected");
      }
      catch (RuntimeException e)
      {
         // OK
      }
      JSR330_D d = container.getComponentInstanceOfType(JSR330_D.class);
      assertNotNull(d);
      assertSame(a, d.a);
      assertSame(a, d.a2);
      assertSame(a, d.a3);
      assertSame(a, d.a4);
      assertSame(a, d.a5);
      assertEquals(1, d.calledInit);
      assertEquals(2, d.calledInit2);
      assertNotNull(d.p3);
      assertNotNull(d.n);
      assertTrue(d.n instanceof JSR330_N2);
      assertNotNull(d.n2);
      assertTrue(d.n2 instanceof JSR330_N1);
      assertSame(container.getComponentInstanceOfType(JSR330_P3.class), d.p3.get());
      assertNotNull(d.p4);
      try
      {
         d.p4.get();
         fail("A Runtime Exception was expected");
      }
      catch (RuntimeException e)
      {
         // ok
      }
      JSR330_N2 n2 = container.getComponentInstanceOfType(JSR330_N2.class);
      assertNotNull(n2);
      assertNotSame(n2, container.getComponentInstanceOfType(JSR330_N2.class));
      JSR330_N2 n2_2 = container.getComponentInstance("n2", JSR330_N2.class);
      assertNotNull(n2_2);
      assertNotSame(n2_2, n2);
      assertNotSame(n2_2, container.getComponentInstance("n2", JSR330_N2.class));
      List<JSR330_N2> allN2 = container.getComponentInstancesOfType(JSR330_N2.class);
      assertNotNull(allN2);
      assertEquals(1, allN2.size());
      JSR330_N2 n2_3 = allN2.get(0);
      assertNotSame(n2_3, n2);
      assertNotSame(n2_3, n2_2);
      allN2 = container.getComponentInstancesOfType(JSR330_N2.class);
      assertNotNull(allN2);
      assertEquals(1, allN2.size());
      assertNotSame(n2_3, allN2.get(0));
      assertTrue(ContainerUtil.isSingleton(JSR330_A.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_N.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_P3.class));
      assertFalse(ContainerUtil.isSingleton(JSR330_N2.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_N2_2.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_N2_3.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_N2_4.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_N2_5.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_N2_6.class));
      assertFalse(ContainerUtil.isSingleton(JSR330_N7.class));
      assertFalse(ContainerUtil.isSingleton(JSR330_N7_2.class));
      assertFalse(ContainerUtil.isSingleton(JSR330_N8.class));
      assertFalse(ContainerUtil.isSingleton(JSR330_N8_2.class));
      assertTrue(ContainerUtil.isSingleton(JSR330_N8_3.class));
   }

   public static class JSR330_A
   {

   }

   public static class JSR330_B extends JSR330_B1
   {
      @Inject
      private JSR330_A a;

      @Inject
      protected JSR330_A a2;

      @Inject
      public JSR330_A a3;

      @Inject
      JSR330_A a4;

      @Inject
      private final JSR330_A a6 = null;

      @Inject
      private static JSR330_A a7;

      @Inject
      private Provider<JSR330_P1> p1;

      @Inject
      private int value;

      @Inject
      @Named("n2")
      private JSR330_N n;

      @Inject
      @N1
      private JSR330_N n2;
   }

   public static class JSR330_B1
   {
      @Inject
      private JSR330_A a5;

      protected JSR330_A getA5()
      {
         return a5;
      }
   }

   public static class JSR330_C
   {
      private final JSR330_A a;

      private final Provider<JSR330_P2> p2;

      private final JSR330_N n;

      private final JSR330_N n2;

      @Inject
      public JSR330_C(JSR330_A a, Provider<JSR330_P2> p2, @Named("n2") JSR330_N n, @N1 JSR330_N n2)
      {
         this.a = a;
         this.p2 = p2;
         this.n = n;
         this.n2 = n2;
      }

      /**
       * Bad constructor
       */
      public JSR330_C(JSR330_A a, JSR330_B b, Provider<JSR330_P2> p2, JSR330_N n, JSR330_N n2)
      {
         this.a = null;
         this.p2 = null;
         this.n = null;
         this.n2 = null;
      }
   }

   public static class JSR330_C2
   {
      private final JSR330_A a;

      @Inject
      protected JSR330_C2(JSR330_A a)
      {
         this.a = a;
      }
   }

   public static class JSR330_C3
   {
      private final JSR330_A a;

      @Inject
      private JSR330_C3(JSR330_A a)
      {
         this.a = a;
      }
   }

   public static class JSR330_C4
   {
      private final JSR330_A a;

      @Inject
      JSR330_C4(JSR330_A a)
      {
         this.a = a;
      }
   }

   public static class JSR330_C5
   {
      @Inject
      JSR330_C5(int value)
      {
      }
   }

   public static class JSR330_C6
   {
      JSR330_C6()
      {
      }
   }

   public static class JSR330_D extends JSR330_D1
   {
      private JSR330_A a;

      private JSR330_A a2;

      private JSR330_A a3;

      private JSR330_A a4;

      private Provider<JSR330_P3> p3;

      @Inject
      private Provider<JSR330_P4> p4;

      private int value;

      private int calledInit;

      private JSR330_N n;

      private JSR330_N n2;

      @Inject
      public void setA(JSR330_A a)
      {
         this.a = a;
      }

      @Inject
      private void setA2(JSR330_A a)
      {
         this.a2 = a;
      }

      @Inject
      protected void setA3(JSR330_A a)
      {
         this.a3 = a;
      }

      @Inject
      void setA4(JSR330_A a)
      {
         this.a4 = a;
      }

      @Inject
      void setP3(Provider<JSR330_P3> p3)
      {
         this.p3 = p3;
      }

      @Inject
      void setValue(int value)
      {
         this.value = value;
      }

      @Inject
      void setN(@Named("n2") JSR330_N n)
      {
         this.n = n;
      }

      @Inject
      void setN2(@N1 JSR330_N n2)
      {
         this.n2 = n2;
      }

      @Inject
      public void init()
      {
         calledInit++;
      }

      @Inject
      public void init2()
      {
         super.init2();
      }

      @Inject
      public void init2(JSR330_A a)
      {
         super.init2();
      }
   }

   public abstract static class JSR330_D1
   {
      protected JSR330_A a5;

      protected int calledInit2;

      @Inject
      private void setA5(JSR330_A a)
      {
         this.a5 = a;
      }

      @Inject
      public abstract void init();

      @Inject
      public void init2()
      {
         calledInit2++;
      }

      @Inject
      public void init2(JSR330_A a)
      {
         init2();
      }
   }

   @Singleton
   public static class JSR330_P1
   {
      @Inject
      JSR330_A a;
   }

   public static class JSR330_P2
   {
      private final JSR330_A a;

      @Inject
      public JSR330_P2(JSR330_A a)
      {
         this.a = a;
      }

      /**
       * Bad constructor
       */
      public JSR330_P2(JSR330_A a, JSR330_B b)
      {
         this.a = null;
      }
   }

   @Singleton
   public static class JSR330_P3
   {
      private JSR330_A a;

      @Inject
      public void setA(JSR330_A a)
      {
         this.a = a;
      }
   }

   public static class JSR330_P4
   {
      @Inject
      public JSR330_P4(int value)
      {
      }
   }

   public static class JSR330_N
   {
   }

   public static class JSR330_N1 extends JSR330_N
   {
   }

   public static class JSR330_N2 extends JSR330_N
   {
      // Create a constructor with the inject annotation to indicate clearly that it is a component
      // JSR 330 aware
      @Inject
      public JSR330_N2() {}
   }

   public static class JSR330_N2_2 extends JSR330_N2
   {
      public JSR330_N2_2() {}
      public JSR330_N2_2(JSR330_N n) {}
   }

   public static class JSR330_N2_3 extends JSR330_N2
   {
      JSR330_N2_3() {}
   }

   public static class JSR330_N2_4 extends JSR330_N2
   {
      private JSR330_N2_4() {}
   }

   public static class JSR330_N2_5 extends JSR330_N2
   {
      protected JSR330_N2_5() {}
   }

   public static class JSR330_N2_6 extends JSR330_N2
   {
   }

   public static class JSR330_N7
   {
      @Inject
      public JSR330_N2 n;
   }

   public static class JSR330_N7_2 extends JSR330_N7
   {
   }

   public static class JSR330_N8
   {
      @Inject
      public void setJSR330_N2(JSR330_N2 n)
      {}
   }

   public static class JSR330_N8_2 extends JSR330_N8
   {
   }

   @Singleton
   public static class JSR330_N8_3 extends JSR330_N8
   {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Qualifier
   public static @interface N1 
   {
   }
}
