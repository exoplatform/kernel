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

import junit.framework.TestCase;

import org.exoplatform.container.spi.After;
import org.exoplatform.container.spi.Before;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.Container;

import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestKernelExtensionMT extends TestCase
{

   public void testInterceptors()
   {
      Callable<Void> task = new Callable<Void>()
      {
         public Void call() throws Exception
         {
            ExoContainer parent = new ExoContainer();
            testInterceptorsInternal(parent);
            // Make sure that it is consistent
            testInterceptorsInternal(parent);
            return null;
         }

      };
      execute(task, (Mode[])null);
      execute(task, Mode.MULTI_THREADED);
      execute(task, Mode.AUTO_SOLVE_DEP_ISSUES);
      execute(task, Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES);
   }

   private void testInterceptorsInternal(ExoContainer parent)
   {
      MockInterceptor1 i1 = null;
      MockInterceptor2 i2 = null;
      MockInterceptor3 i3 = null;
      MockInterceptor4 i4 = null;
      MockInterceptor5 i5 = null;
      MockInterceptor6 i6 = null;
      MockInterceptor7 i7 = null;
      MockInterceptor8 i8 = null;
      MockInterceptor9 i9 = null;
      ExoContainer holder = new ExoContainer(parent);
      Container container = holder;
      while ((container = container.getSuccessor()) != null)
      {
         if (container instanceof MockInterceptor1)
         {
            i1 = (MockInterceptor1)container;
         }
         else if (container instanceof MockInterceptor2)
         {
            i2 = (MockInterceptor2)container;
         }
         else if (container instanceof MockInterceptor3)
         {
            i3 = (MockInterceptor3)container;
         }
         else if (container instanceof MockInterceptor4)
         {
            i4 = (MockInterceptor4)container;
         }
         else if (container instanceof MockInterceptor5)
         {
            i5 = (MockInterceptor5)container;
         }
         else if (container instanceof MockInterceptor6)
         {
            i6 = (MockInterceptor6)container;
         }
         else if (container instanceof MockInterceptor7)
         {
            i7 = (MockInterceptor7)container;
         }
         else if (container instanceof MockInterceptor8)
         {
            i8 = (MockInterceptor8)container;
         }
         else if (container instanceof MockInterceptor9)
         {
            i9 = (MockInterceptor9)container;
         }
      }
      assertNotNull(i1);
      assertNotNull(i1.getSuccessor());
      assertSame(parent, i1.getParent());
      assertSame(holder, i1.getHolder());
      assertEquals("MockInterceptor1", i1.getId());
      assertNotNull(i2);
      assertNotNull(i2.getSuccessor());
      assertSame(parent, i2.getParent());
      assertSame(holder, i2.getHolder());
      assertEquals("MockInterceptor2", i2.getId());
      assertNotNull(i3);
      assertNotNull(i3.getSuccessor());
      assertSame(parent, i3.getParent());
      assertSame(holder, i3.getHolder());
      assertEquals("MockInterceptor3", i3.getId());
      assertNotNull(i4);
      assertNotNull(i4.getSuccessor());
      assertSame(parent, i4.getParent());
      assertSame(holder, i4.getHolder());
      assertEquals("MockInterceptor4", i4.getId());
      assertNotNull(i5);
      assertNotNull(i5.getSuccessor());
      assertSame(parent, i5.getParent());
      assertSame(holder, i5.getHolder());
      assertEquals("MockInterceptor5", i5.getId());
      assertNotNull(i6);
      assertNotNull(i6.getSuccessor());
      assertSame(parent, i6.getParent());
      assertSame(holder, i6.getHolder());
      assertEquals("MockInterceptor6", i6.getId());
      assertNotNull(i7);
      assertNotNull(i7.getSuccessor());
      assertSame(parent, i7.getParent());
      assertSame(holder, i7.getHolder());
      assertEquals("MockInterceptor7", i7.getId());
      assertNotNull(i8);
      assertNull(i8.getSuccessor());
      assertSame(parent, i8.getParent());
      assertSame(holder, i8.getHolder());
      assertEquals("MockInterceptor8", i8.getId());
      assertNotNull(i9);
      assertNotNull(i9.getSuccessor());
      assertSame(i5, i9.getSuccessor());
      assertSame(parent, i9.getParent());
      assertSame(holder, i9.getHolder());
      assertEquals("MockInterceptor9", i9.getId());
      assertSame(i9, holder.getSuccessor());
      Test0 t0I = new Test0();
      Test1 t1I = new Test1();
      Test2 t2I = new Test2();
      Test3 t3I = new Test3();
      holder.registerComponentInstance(Test0.class, t0I);
      holder.registerComponentInstance(Test1.class, t1I);
      holder.registerComponentInstance(Test2.class, t2I);
      holder.registerComponentInstance(Test3.class, t3I);
      holder.registerComponentImplementation(Test5.class);
      assertSame(t0I, holder.getComponentInstanceOfType(Test0.class));
      Test1 t1FC = holder.getComponentInstanceOfType(Test1.class);
      assertNotSame(t1I, t1FC);
      // A new instance is created at each call so it must not be the same as before
      assertNotSame(t1FC, holder.getComponentInstanceOfType(Test1.class));
      Test2 t2FC = holder.getComponentInstanceOfType(Test2.class);
      assertNotSame(t2I, t2FC);
      // It comes from the cache so it must be the same than before
      assertSame(t2FC, holder.getComponentInstanceOfType(Test2.class));
      Test3 t3FC = holder.getComponentInstanceOfType(Test3.class);
      assertSame(MockInterceptor1.TEST, t3FC);
      Test5 t4FC = holder.getComponentInstanceOfType(Test5.class);
      assertSame(t0I, t4FC.t0);
      assertNotSame(t1I, t4FC.t1);
      assertNotSame(t2I, t4FC.t2);
      assertSame(t3FC, t4FC.t3);
   }

   @SuppressWarnings("serial")
   @Before(value = "Cache")
   public static class MockInterceptor1 extends AbstractInterceptor
   {
      public static final Test3 TEST = new Test3();

      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }

      @Override
      public ComponentAdapter getComponentAdapterOfType(Class<?> componentType)
      {
         if (componentType.equals(Test4.class))
         {
            return new InstanceComponentAdapter(Test4.class, new Test4());
         }
         return super.getComponentAdapterOfType(componentType);
      }

      @Override
      public <T> T getComponentInstanceOfType(Class<T> componentType)
      {
         if (componentType.equals(Test1.class))
         {
            return componentType.cast(new Test1());
         }
         else if (componentType.equals(Test3.class))
         {
            return componentType.cast(TEST);
         }
         else if (componentType.equals(Test4.class))
         {
            return componentType.cast(new Test4());
         }
         return super.getComponentInstanceOfType(componentType);
      }
   }

   @SuppressWarnings("serial")
   @After(value = "Cache")
   public static class MockInterceptor2 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }

      @Override
      public <T> T getComponentInstanceOfType(Class<T> componentType)
      {
         if (componentType.equals(Test2.class))
         {
            return componentType.cast(new Test2());
         }
         return super.getComponentInstanceOfType(componentType);
      }
   }

   @SuppressWarnings("serial")
   public static class MockInterceptor3 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }
   }

   @SuppressWarnings("serial")
   @After(value = "Fake")
   public static class MockInterceptor4 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }
   }

   @SuppressWarnings("serial")
   @Before(value = "Fake")
   public static class MockInterceptor5 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }
   }

   @SuppressWarnings("serial")
   @After(value = "")
   public static class MockInterceptor6 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }
   }

   @SuppressWarnings("serial")
   @Before(value = "")
   public static class MockInterceptor7 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }
   }

   @SuppressWarnings("serial")
   @After(value = "ConcurrentContainer")
   public static class MockInterceptor8 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }
   }

   @SuppressWarnings("serial")
   @Before(value = "MockInterceptor5")
   public static class MockInterceptor9 extends AbstractInterceptor
   {
      ExoContainer getParent()
      {
         return parent;
      }

      ExoContainer getHolder()
      {
         return holder;
      }
   }

   private static class Test0
   {
   };

   private static class Test1
   {
   };

   private static class Test2
   {
   };

   private static class Test3
   {
   };

   private static class Test4
   {
   };

   public static class Test5
   {
      public Test0 t0;
      public Test1 t1;
      public Test2 t2;
      public Test3 t3;
      public Test4 t4;

      public Test5(Test0 t0, Test1 t1, Test2 t2, Test3 t3, Test4 t4)
      {
         this.t0 = t0;
         this.t1 = t1;
         this.t2 = t2;
         this.t3 = t3;
         this.t4 = t4;
      }
   };

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
