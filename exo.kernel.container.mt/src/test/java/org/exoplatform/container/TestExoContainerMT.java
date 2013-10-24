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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterMT;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.picocontainer.Startable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;

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
@RunWith(Parameterized.class)
public class TestExoContainerMT extends TestExoContainer
{
   private Mode[] modes;
   public TestExoContainerMT(Mode... modes)
   {
      this.modes = modes;
   }

   @Before
   public void setUp()
   {
      Mode.setModes(modes);
   }

   @After
   public void tearDown()
   {
      Mode.clearModes();
   }

   @Parameters
   public static List<Object[]> data()
   {
      return Arrays.asList(new Object[][]{{null}, {new Mode[]{Mode.MULTI_THREADED}},
         {new Mode[]{Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE}}, {new Mode[]{Mode.AUTO_SOLVE_DEP_ISSUES}},
         {new Mode[]{Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES}},
         {new Mode[]{Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE}}});
   }

   @Test
   public void testBadCyclicRef() throws Exception
   {
      RootContainer container = AbstractTestContainer.createRootContainer(getClass(), "test-exo-container-mt.xml", "testBadCyclicRef");
      try
      {
         container.getComponentInstanceOfType(TestExoContainer.A1.class);
         fail("A CyclicDependencyException was expected");
      }
      catch (CyclicDependencyException e)
      {
         // expected exception
      }
   }

   @Test
   public void testBadCyclicRef2() throws Exception
   {
      try
      {
         AbstractTestContainer.createRootContainer(getClass(), "test-exo-container-mt.xml", "testBadCyclicRef2");
      }
      catch (CyclicDependencyException e)
      {
         // expected exception
      }
   }

   @Test
   public void testBadCyclicRef3() throws Exception
   {
      RootContainer container = AbstractTestContainer.createRootContainer(getClass(), "test-exo-container-mt.xml", "testBadCyclicRef3");
      try
      {
         container.getComponentInstanceOfType(A1.class);
         fail("A CyclicDependencyException was expected");
      }
      catch (CyclicDependencyException e)
      {
         // expected exception
      }
   }

   @Test
   public void testBadCyclicRef4() throws Exception
   {
      try
      {
         AbstractTestContainer.createRootContainer(getClass(), "test-exo-container-mt.xml", "testBadCyclicRef4");
      }
      catch (CyclicDependencyException e)
      {
         // expected exception
      }
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

   @Test
   public void testBadCyclicRef5() throws Exception
   {
      RootContainer container = AbstractTestContainer.createRootContainer(getClass(), "test-exo-container-mt.xml", "testBadCyclicRef5");
      try
      {
         container.getComponentInstanceOfType(A3.class);
         fail("A CyclicDependencyException was expected");
      }
      catch (CyclicDependencyException e)
      {
         // expected exception
      }
   }

   @Test
   public void testBadCyclicRef6() throws Exception
   {
      try
      {
         AbstractTestContainer.createRootContainer(getClass(), "test-exo-container-mt.xml", "testBadCyclicRef6");
      }
      catch (CyclicDependencyException e)
      {
         // expected exception
      }
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

   @Test
   public void testAutoSolveDepIssues()
   {
      RootContainer container = AbstractTestContainer.createRootContainer(getClass(), "test-exo-container-mt.xml", "testAutoSolveDepIssues");
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
}