/*
 * Copyright (C) 2009 eXo Platform SAS.
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

package org.exoplatform.container.management;

import junit.framework.TestCase;

import org.exoplatform.container.ContainerBuilder;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestManagementProvider extends TestCase 
{

   public void testProviderRegistration()
   {
      URL url = getClass().getResource("configuration1.xml");
      RootContainer container = new ContainerBuilder().withRoot(url).build();
      ManagementProviderImpl provider = (ManagementProviderImpl)container.getComponentInstanceOfType(ManagementProviderImpl.class);
      assertNotNull(provider);
   }

   public void testManagedRegistrationAfterProviderRegistration()
   {
      URL url = getClass().getResource("configuration1.xml");
      RootContainer container = new ContainerBuilder().withRoot(url).build();
      ManagementProviderImpl provider = (ManagementProviderImpl)container.getComponentInstanceOfType(ManagementProviderImpl.class);
      assertEquals(1, provider.managedResources.size());
      Object foo = container.getComponentInstance("Foo");
      assertNotNull(foo);
      assertEquals(2, provider.managedResources.size());
      ManagedResource fooMR = provider.managedResources.get(1);
      assertSame(foo, fooMR.resource);
      assertEquals(Collections.<ScopedData>emptyList(), fooMR.context.getScopingData(ScopedData.class));
      fooMR.register();
      assertEquals(Collections.singletonList(fooMR.data), fooMR.context.getScopingData(ScopedData.class));
   }

   public void testManagedRegistrationBeforeProviderRegistration()
   {
      URL url = getClass().getResource("configuration2.xml");
      RootContainer container = new ContainerBuilder().withRoot(url).build();
      ManagementProviderImpl provider = (ManagementProviderImpl)container.getComponentInstanceOfType(ManagementProviderImpl.class);
      assertNull(provider);
      Object foo = container.getComponentInstance("Foo");
      assertNotNull(foo);
      provider = new ManagementProviderImpl();
      container.registerComponentInstance(provider);
      assertEquals(2, provider.managedResources.size());
      ManagedResource fooMR = provider.managedResources.get(1);
      assertSame(foo, fooMR.resource);
      assertEquals(Collections.<ScopedData>emptyList(), fooMR.context.getScopingData(ScopedData.class));
      fooMR.register();
      assertEquals(Collections.singletonList(fooMR.data), fooMR.context.getScopingData(ScopedData.class));
   }

   public void testManagementAware()
   {
      URL url = getClass().getResource("configuration1.xml");
      RootContainer container = new ContainerBuilder().withRoot(url).build();
      ManagementProviderImpl provider = (ManagementProviderImpl)container.getComponentInstanceOfType(ManagementProviderImpl.class);
      Foo foo = (Foo)container.getComponentInstance("Foo");
      assertEquals(2, provider.managedResources.size());
      ManagedResource fooMR = provider.managedResources.get(1);
      fooMR.register();
      assertTrue(foo.isAware());

      //
      foo.deploy();
      assertEquals(3, provider.managedResources.size());
      ManagedResource barMR = provider.managedResources.get(2);
      assertSame(foo.bar, barMR.resource);
      barMR.register();
      assertEquals(Arrays.asList(barMR.data, fooMR.data), barMR.context.getScopingData(ScopedData.class));

      //
      foo.undeploy();
      assertEquals(2, provider.managedResources.size());
      assertEquals(fooMR, provider.managedResources.get(1));
   }

   public void testContainerScopedRegistration()
   {
      URL rootURL = getClass().getResource("root-configuration.xml");
      URL portal1URL = getClass().getResource("portal-configuration1.xml");
      URL portal2URL = getClass().getResource("portal-configuration2.xml");
      RootContainer root = new ContainerBuilder().withRoot(rootURL).withPortal("portal1", portal1URL).withPortal("portal2", portal2URL).build();
      Foo fooRoot = (Foo)root.getComponentInstanceOfType(Foo.class);
      ManagementProviderImpl provider = (ManagementProviderImpl)root.getComponentInstanceOfType(ManagementProviderImpl.class);
      PortalContainer portal1 = root.getPortalContainer("portal1");
      Foo fooPortal1 = (Foo)portal1.getComponentInstanceOfType(Foo.class);
      ManagementProviderImpl provider1 = (ManagementProviderImpl)portal1.getComponentInstanceOfType(ManagementProviderImpl.class);
      PortalContainer portal2 = root.getPortalContainer("portal2");
      Foo fooPortal2 = (Foo)portal2.getComponentInstanceOfType(Foo.class);
      ManagementProviderImpl provider2 = (ManagementProviderImpl)portal2.getComponentInstanceOfType(ManagementProviderImpl.class);

      //
      assertEquals(6, provider.managedResources.size());
      assertTrue(provider.resources.contains(root));
      assertTrue(provider.resources.contains(root));
      assertTrue(provider.resources.contains(portal1));
      assertTrue(provider.resources.contains(portal2));
      assertTrue(provider.resources.contains(fooRoot));
      assertTrue(provider.resources.contains(fooPortal1));
      assertTrue(provider.resources.contains(fooPortal2));
      assertEquals(2, provider1.managedResources.size());
      assertEquals(portal1, provider1.managedResources.get(0).resource);
      assertEquals(fooPortal1, provider1.managedResources.get(1).resource);
      assertEquals(2, provider2.managedResources.size());
      assertEquals(portal2, provider2.managedResources.get(0).resource);
      assertEquals(fooPortal2, provider2.managedResources.get(1).resource);
   }

   public void testMultiThreading() throws Throwable
   {
      URL url = getClass().getResource("configuration1.xml");
      final RootContainer container = new ContainerBuilder().withRoot(url).build();
      int threads = 50;
      final CountDownLatch startSignal = new CountDownLatch(1);
      final CountDownLatch doneSignal = new CountDownLatch(threads);
      final List<Throwable> errors = Collections.synchronizedList(new ArrayList<Throwable>());
      final AtomicInteger sequence = new AtomicInteger();
      for (int i = 0; i < threads; i++)
      {
         Thread thread = new Thread()
         {
            public void run()
            {
               try
               {
                  startSignal.await();
                  container.registerComponentInstance("ManagementProviderImpl" + sequence.incrementAndGet(), new ManagementProviderImpl());
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
}
