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

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.support.ContainerBuilder;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;
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
   
   public void testCyclicRef()
   {
      final RootContainer container = createRootContainer("test-exo-container.xml", "testCyclicRef");
      A a = (A)container.getComponentInstanceOfType(A.class);
      assertNotNull(a);
      B b = (B)container.getComponentInstanceOfType(B.class);
      assertNotNull(b);
      assertEquals(a, b.a);
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

         public Object getComponentInstance(PicoContainer paramPicoContainer)
            throws PicoInitializationException, PicoIntrospectionException
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

               public Object getComponentInstance(PicoContainer paramPicoContainer)
                  throws PicoInitializationException, PicoIntrospectionException
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
         if (counter != null) counter.destroy.add(this);
      }

      @Override
      public void initContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.init.add(this);
      }

      @Override
      public void startContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.start.add(this);
      }

      @Override
      public void stopContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.stop.add(this);
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
         if (counter != null) counter.destroy.add(this);
      }

      @Override
      public void initContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.init.add(this);
      }

      @Override
      public void startContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.start.add(this);
      }

      @Override
      public void stopContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.stop.add(this);
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
         if (counter != null) counter.destroy.add(this);
      }

      @Override
      public void initContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.init.add(this);
      }

      @Override
      public void startContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.start.add(this);
      }

      @Override
      public void stopContainer(ExoContainer container) throws Exception
      {
         MyCounter counter = (MyCounter)container.getComponentInstanceOfType(MyCounter.class);
         if (counter != null) counter.stop.add(this);
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
}