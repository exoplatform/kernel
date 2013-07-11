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
package org.exoplatform.container.weld;

import org.exoplatform.container.ContainerBuilder;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.spi.ComponentAdapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URL;
import java.util.List;

import javax.enterprise.inject.Default;
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
public class TestWeldContainer extends AbstractTestContainer
{

   public void testIntegration()
   {
      URL rootURL = getClass().getResource("test-exo-container.xml");
      URL portalURL = getClass().getResource("test-exo-container2.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();
      RootContainer root = RootContainer.getInstance();
      testIntegration(root);
      ComponentAdapter<H> adapterH = root.getComponentAdapterOfType(H.class);
      assertNull(adapterH);
      PortalContainer portal = PortalContainer.getInstance();
      adapterH = portal.getComponentAdapterOfType(H.class);
      assertNotNull(adapterH);
      H h = root.getComponentInstanceOfType(H.class);
      assertNull(h);
      h = portal.getComponentInstanceOfType(H.class);
      assertNotNull(h);
      assertSame(h, portal.getComponentInstanceOfType(H.class));
      assertSame(h, adapterH.getComponentInstance());
      List<ComponentAdapter<H>> adapters = root.getComponentAdaptersOfType(H.class);
      assertTrue(adapters == null || adapters.isEmpty());
      adapters = portal.getComponentAdaptersOfType(H.class);
      assertNotNull(adapters);
      assertEquals(1, adapters.size());
      assertSame(h, adapters.get(0).getComponentInstance());
      List<H> allH = root.getComponentInstancesOfType(H.class);
      assertTrue(allH == null || allH.isEmpty());
      allH = portal.getComponentInstancesOfType(H.class);
      assertNotNull(allH);
      assertEquals(1, allH.size());
      assertSame(h, allH.get(0));
   }

   @SuppressWarnings("unchecked")
   private void testIntegration(RootContainer container)
   {
      assertNotNull(container);
      ComponentAdapter<A> adapterA = container.getComponentAdapterOfType(A.class);
      assertNotNull(adapterA);
      assertSame(adapterA, container.getComponentAdapterOfType(A.class));
      ComponentAdapter<B> adapterB = container.getComponentAdapterOfType(B.class);
      assertNotNull(adapterB);
      ComponentAdapter<C> adapterC = container.getComponentAdapterOfType(C.class);
      assertNotNull(adapterC);
      ComponentAdapter<D> adapterD = container.getComponentAdapterOfType(D.class);
      assertNotNull(adapterD);
      assertSame(adapterD, container.getComponentAdapterOfType(D.class));
      ComponentAdapter<E> adapterE = container.getComponentAdapterOfType(E.class);
      assertNotNull(adapterE);
      adapterE = (ComponentAdapter<E>)container.getComponentAdapter("MyClassE");
      assertNotNull(adapterE);
      assertSame(adapterE, container.getComponentAdapter("MyClassE"));
      ComponentAdapter<F> adapterF = container.getComponentAdapterOfType(F.class);
      assertNotNull(adapterF);
      ComponentAdapter<G> adapterG = container.getComponentAdapterOfType(G.class);
      assertNotNull(adapterG);
      A a = container.getComponentInstanceOfType(A.class);
      assertNotNull(a);
      assertSame(a, container.getComponentInstanceOfType(A.class));
      assertSame(a, adapterA.getComponentInstance());
      B b = container.getComponentInstanceOfType(B.class);
      assertNotNull(b);
      assertSame(b, container.getComponentInstanceOfType(B.class));
      assertSame(b, adapterB.getComponentInstance());
      C c = container.getComponentInstanceOfType(C.class);
      assertNotNull(c);
      assertNotSame(c, container.getComponentInstanceOfType(C.class));
      assertNotSame(c, adapterC.getComponentInstance());
      assertSame(a, c.a);
      assertSame(b, c.b);
      assertSame(a, ((C)adapterC.getComponentInstance()).a);
      assertSame(b, ((C)adapterC.getComponentInstance()).b);
      assertSame(a, container.getComponentInstanceOfType(C.class).a);
      assertSame(b, container.getComponentInstanceOfType(C.class).b);
      assertNotNull(c.a2);
      assertNotNull(c.a2_2);
      assertNotSame(c.a2, c.a2_2);
      assertNotNull(c.d);
      assertSame(c.d, c.d2);
      D d = container.getComponentInstanceOfType(D.class);
      assertNotNull(d);
      assertSame(d, container.getComponentInstanceOfType(D.class));
      assertSame(d, adapterD.getComponentInstance());
      assertSame(a, d.a);
      assertSame(b, d.b);
      assertTrue(d.g instanceof G1);
      assertTrue(d.g2 instanceof G2);
      assertTrue(d.g3.get() instanceof G3);
      E e = (E)container.getComponentInstance("MyClassE");
      assertNotNull(a);
      assertSame(e, container.getComponentInstance("MyClassE"));
      assertSame(e, adapterE.getComponentInstance());
      F f = container.getComponentInstanceOfType(F.class);
      assertNotNull(f);
      assertSame(f, container.getComponentInstanceOfType(F.class));
      assertSame(f, adapterF.getComponentInstance());
      assertSame(e, f.e);
      assertTrue(f.e instanceof E1);
      assertTrue(f.m instanceof E1);
      assertTrue(f.e2 instanceof E2);
      assertNotNull(f.e3.get());
      assertTrue(f.e3.get() instanceof E);
      assertFalse(f.e3.get() instanceof E1);
      assertFalse(f.e3.get() instanceof E2);
      G g = container.getComponentInstanceOfType(G.class);
      assertNotNull(g);
      assertSame(g, container.getComponentInstanceOfType(G.class));
      assertSame(g, adapterG.getComponentInstance());
      List<ComponentAdapter<Marker>> adapters = container.getComponentAdaptersOfType(Marker.class);
      assertNotNull(adapters);
      assertEquals(4, adapters.size());
      boolean foundE = false, foundF = false, foundE1 = false, foundE2 = false;
      for (ComponentAdapter<Marker> adapter : adapters)
      {
         if (adapter.getComponentImplementation().equals(E1.class))
         {
            foundE1 = true;
            assertSame(e, adapter.getComponentInstance());
         }
         else if (adapter.getComponentImplementation().equals(E2.class))
         {
            foundE2 = true;
            assertSame(f.e2, adapter.getComponentInstance());
         }
         else if (adapter.getComponentImplementation().equals(E.class))
         {
            foundE = true;
            assertSame(f.e3.get(), adapter.getComponentInstance());
         }
         else if (adapter.getComponentImplementation().equals(F.class))
         {
            foundF = true;
            assertSame(f, adapter.getComponentInstance());
         }
      }
      assertTrue(foundE);
      assertTrue(foundE1);
      assertTrue(foundE2);
      assertTrue(foundF);
      List<Marker> markers = container.getComponentInstancesOfType(Marker.class);
      assertNotNull(markers);
      assertEquals(4, markers.size());
      assertTrue(markers.contains(e));
      assertTrue(markers.contains(f.e));
      assertTrue(markers.contains(f.e2));
      assertTrue(markers.contains(f));
   }

   public static class A
   {
   }

   public static class A2
   {
      @Inject
      public A2() {}
   }

   @Singleton
   public static class B
   {
   }

   public static class C
   {
      @Inject
      A a;

      @Inject
      A2 a2;

      @Inject
      A2 a2_2;

      @Inject
      B b;

      @Inject
      D d;

      @Inject
      D d2;
   }

   @Singleton
   public static class D
   {
      A a;

      B b;

      @Inject
      @Named("MyClassG")
      G g;

      @Inject
      @QG2
      G g2;

      @Inject
      Provider<G> g3;

      @Inject
      public D(A a, B b)
      {
         this.a = a;
         this.b = b;
      }
   }

   public static class E implements Marker
   {
   }

   public static class E1 extends E
   {
   }

   public static class E2 extends E
   {
   }

   @Singleton
   public static class F implements Marker
   {
      @Inject
      @Named("MyClassE")
      E e;

      @Inject
      @Named("MyClassE")
      Marker m;

      @Inject
      @QE2
      E e2;

      @Inject
      Provider<E> e3;
   }

   public abstract static class G
   {
   }

   @Singleton
   @Named("MyClassG")
   public static class G1 extends G
   {
   }

   @Singleton
   @QG2
   public static class G2 extends G
   {
   }

   @Singleton
   @Default
   public static class G3 extends G
   {
   }

   @Singleton
   public static class H
   {
   }

   public static interface Marker {}

   @Retention(RetentionPolicy.RUNTIME)
   @Qualifier
   public static @interface QE2 
   {
   }

   @Retention(RetentionPolicy.RUNTIME)
   @Qualifier
   public static @interface QG2 
   {
   }
}