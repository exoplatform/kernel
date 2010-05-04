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
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

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
               ar.set(container.registerComponent(ca));
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
}