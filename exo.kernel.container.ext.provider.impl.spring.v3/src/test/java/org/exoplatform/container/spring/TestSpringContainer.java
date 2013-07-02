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
package org.exoplatform.container.spring;

import org.exoplatform.container.ContainerBuilder;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.spi.ComponentAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestSpringContainer extends AbstractTestContainer
{

   public void testIntegrationClass()
   {
      URL rootURL = getClass().getResource("test-exo-container.xml");
      URL portalURL = getClass().getResource("test-exo-container2.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("class").build();
      RootContainer root = RootContainer.getInstance();
      testIntegration(root);
   }

   public void testIntegrationFile()
   {
      URL rootURL = getClass().getResource("test-exo-container.xml");
      URL portalURL = getClass().getResource("test-exo-container2.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).profiledBy("file").build();
      RootContainer root = RootContainer.getInstance();
      testIntegration(root);
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
      assertSame(c, container.getComponentInstanceOfType(C.class));
      assertSame(c, adapterC.getComponentInstance());
      assertSame(a, c.a);
      assertSame(b, c.b);
      assertSame(a, ((C)adapterC.getComponentInstance()).a);
      assertSame(b, ((C)adapterC.getComponentInstance()).b);
      assertSame(a, container.getComponentInstanceOfType(C.class).a);
      assertSame(b, container.getComponentInstanceOfType(C.class).b);
      D d = container.getComponentInstanceOfType(D.class);
      assertNotNull(d);
      assertSame(d, container.getComponentInstanceOfType(D.class));
      assertSame(d, adapterD.getComponentInstance());
      assertSame(a, d.a);
      assertSame(b, d.b);
      E e = (E)container.getComponentInstance("MyClassE");
      assertNotNull(a);
      assertSame(e, container.getComponentInstance("MyClassE"));
      assertSame(e, adapterE.getComponentInstance());
      F f = container.getComponentInstanceOfType(F.class);
      assertNotNull(f);
      assertSame(f, container.getComponentInstanceOfType(F.class));
      assertSame(f, adapterF.getComponentInstance());
      assertSame(e, f.e);
      G g = container.getComponentInstanceOfType(G.class);
      assertNotNull(g);
      assertSame(g, container.getComponentInstanceOfType(G.class));
      assertSame(g, adapterG.getComponentInstance());
      List<ComponentAdapter<Marker>> adapters = container.getComponentAdaptersOfType(Marker.class);
      assertNotNull(adapters);
      assertEquals(2, adapters.size());
      boolean foundE = false, foundF = false;
      for (ComponentAdapter<Marker> adapter : adapters)
      {
         if (adapter.getComponentImplementation().equals(E.class))
         {
            foundE = true;
            assertSame(e, adapter.getComponentInstance());
         }
         else if (adapter.getComponentImplementation().equals(F.class))
         {
            foundF = true;
            assertSame(f, adapter.getComponentInstance());
         }
      }
      assertTrue(foundE);
      assertTrue(foundF);
      List<Marker> markers = container.getComponentInstancesOfType(Marker.class);
      assertNotNull(markers);
      assertEquals(2, markers.size());
      assertTrue(markers.contains(e));
      assertTrue(markers.contains(f));
      ComponentAdapter<H> adapterH = container.getComponentAdapterOfType(H.class);
      assertNull(adapterH);
      PortalContainer portal = PortalContainer.getInstance();
      adapterH = portal.getComponentAdapterOfType(H.class);
      assertNotNull(adapterH);
      H h = container.getComponentInstanceOfType(H.class);
      assertNull(h);
      h = portal.getComponentInstanceOfType(H.class);
      assertNotNull(h);
      assertSame(h, portal.getComponentInstanceOfType(H.class));
      assertSame(h, adapterH.getComponentInstance());
      List<ComponentAdapter<H>> adaptersH = container.getComponentAdaptersOfType(H.class);
      assertTrue(adaptersH == null || adaptersH.isEmpty());
      adaptersH = portal.getComponentAdaptersOfType(H.class);
      assertNotNull(adaptersH);
      assertEquals(1, adaptersH.size());
      assertSame(h, adaptersH.get(0).getComponentInstance());
      List<H> allH = container.getComponentInstancesOfType(H.class);
      assertTrue(allH == null || allH.isEmpty());
      allH = portal.getComponentInstancesOfType(H.class);
      assertNotNull(allH);
      assertEquals(1, allH.size());
      assertSame(h, allH.get(0));
   }

   public static class A
   {
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
      B b;
   }

   public static class D
   {
      A a;

      B b;

      public D(A a, B b)
      {
         this.a = a;
         this.b = b;
      }
   }

   public static class E implements Marker
   {
   }

   @Singleton
   public static class F implements Marker
   {
      @Inject
      @Named("MyClassE")
      E e;
   }

   @Singleton
   @Named("MyClassG")
   public static class G
   {
   }

   @Singleton
   public static class H
   {
   }

   public static interface Marker
   {
   }

   @Configuration
   public static class Config
   {
      @Autowired
      A a;

      @Autowired
      B b;

      @Autowired
      @Named("MyClassE")
      E e;

      @Bean
      public B b()
      {
         return new B();
      }

      @Bean
      public C c()
      {
         C c = new C();
         c.a = a;
         c.b = b;
         return c;
      }

      @Bean
      public F f()
      {
         F f = new F();
         f.e = e;
         return f;
      }

      @Bean
      public G g()
      {
         return new G();
      }
   }

   @Configuration
   public static class Config2
   {
      @Bean
      public H h()
      {
         return new H();
      }
   }
}