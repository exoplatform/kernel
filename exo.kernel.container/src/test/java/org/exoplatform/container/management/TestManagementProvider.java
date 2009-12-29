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
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.support.ContainerBuilder;

import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

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
      assertEquals(0, provider.resources.size());
      Object foo = container.getComponentInstance("Foo");
      assertNotNull(foo);
      assertEquals(1, provider.resources.size());
      ManagedResource fooMR = provider.resources.get(0);
      assertSame(foo, fooMR.resource);
      assertEquals(Collections.<ScopedData>emptyList(), fooMR.context.getScopingProperties(ScopedData.class));
      fooMR.register();
      assertEquals(Collections.singletonList(fooMR.data), fooMR.context.getScopingProperties(ScopedData.class));
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
      assertEquals(1, provider.resources.size());
      ManagedResource fooMR = provider.resources.get(0);
      assertSame(foo, fooMR.resource);
      assertEquals(Collections.<ScopedData>emptyList(), fooMR.context.getScopingProperties(ScopedData.class));
      fooMR.register();
      assertEquals(Collections.singletonList(fooMR.data), fooMR.context.getScopingProperties(ScopedData.class));
   }

   public void testManagementAware()
   {
      URL url = getClass().getResource("configuration1.xml");
      RootContainer container = new ContainerBuilder().withRoot(url).build();
      ManagementProviderImpl provider = (ManagementProviderImpl)container.getComponentInstanceOfType(ManagementProviderImpl.class);
      Foo foo = (Foo)container.getComponentInstance("Foo");
      assertEquals(1, provider.resources.size());
      ManagedResource fooMR = provider.resources.get(0);
      fooMR.register();
      assertTrue(foo.isAware());

      //
      foo.deploy();
      assertEquals(2, provider.resources.size());
      ManagedResource barMR = provider.resources.get(1);
      assertSame(foo.bar, barMR.resource);
      barMR.register();
      assertEquals(Arrays.asList(barMR.data, fooMR.data), barMR.context.getScopingProperties(ScopedData.class));

      //
      foo.undeploy();
      assertEquals(Arrays.asList(fooMR), provider.resources);
   }
}
