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
package org.exoplatform.container.jmx;

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.support.ManagedComponentRequestLifeCycle;
import org.exoplatform.container.jmx.support.ManagedDependent;
import org.exoplatform.container.jmx.support.ManagedManagementAware;
import org.exoplatform.container.jmx.support.ManagedWithObjectNameTemplate;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRootContainerManagedIntegration extends AbstractTestContainer
{

   public void _testExplicitObjectName() throws Exception
   {
      RootContainer container = createRootContainer("configuration1.xml");
      Object expectedObject = container.getComponentInstance("ManagedWithExplicitObjectName");
      assertNotNull(expectedObject);
      MBeanServer server = container.getMBeanServer();
      assertNotNull(server);
      Object object =
         server.getAttribute(ObjectName.getInstance("exo:object=ManagedWithExplicitObjectName"), "Reference");
      assertNotNull(object);
      assertEquals(expectedObject, object);
   }

   public void testObjectNameTemplate() throws Exception
   {
      RootContainer container = createRootContainer("configuration2.xml");
      Object expectedFoo = container.getComponentInstance("Foo");
      Object expectedBar = container.getComponentInstance("Bar");
      assertNotNull(expectedFoo);
      assertNotNull(expectedBar);
      MBeanServer server = container.getMBeanServer();
      assertNotNull(server);
      Object foo = server.getAttribute(ObjectName.getInstance("exo:object=\"Foo\""), "Reference");
      assertNotNull(foo);
      Object bar = server.getAttribute(ObjectName.getInstance("exo:object=\"Bar\""), "Reference");
      assertNotNull(bar);
      assertEquals(expectedFoo, foo);
      assertEquals(expectedBar, bar);
   }

   public void _testObjectNameTemplateOverriddenByExplicitObjectName() throws Exception
   {
      RootContainer container = createRootContainer("configuration3.xml");
      Object expectedObject =
         container.getComponentInstance("ManagedWithObjectNameTemplateOverriddenByExplicitObjectName");
      assertNotNull(expectedObject);
      MBeanServer server = container.getMBeanServer();
      assertNotNull(server);
      Object object =
         server.getAttribute(ObjectName
            .getInstance("exo:object=ManagedWithObjectNameTemplateOverriddenByExplicitObjectName"), "Reference");
      assertNotNull(object);
      assertEquals(expectedObject, object);
   }

   public void testManagementAware() throws Exception
   {
      RootContainer container = createRootContainer("configuration4.xml");
      ManagedManagementAware aware = (ManagedManagementAware)container.getComponentInstance("ManagedManagementAware");
      assertNotNull(aware.context);
      MBeanServer server = container.getMBeanServer();
      assertNotNull(server);
      Object foo = server.getAttribute(ObjectName.getInstance("exo:object=\"Foo\""), "Reference");
      assertNotNull(foo);
      assertEquals(aware.foo, foo);
      ManagedDependent expectedBar = new ManagedDependent("Bar");
      aware.context.register(expectedBar);
      assertEquals(1, server.queryMBeans(ObjectName.getInstance("exo:object=\"Bar\""), null).size());
      Object bar = server.getAttribute(ObjectName.getInstance("exo:object=\"Bar\""), "Reference");
      assertEquals(expectedBar, bar);
      aware.context.unregister(expectedBar);
      assertEquals(0, server.queryMBeans(ObjectName.getInstance("exo:object=\"Bar\""), null).size());
   }

   public void testManagementAwareManagingOtherBeans() throws Exception
   {
      RootContainer container = createRootContainer("configuration4.xml");
      ManagedManagementAware aware = (ManagedManagementAware)container.getComponentInstance("ManagedManagementAware");
      aware.context.register(new ManagedWithObjectNameTemplate("juu"));
      container.getMBeanServer().getObjectInstance(new ObjectName("exo:object=\"juu\""));
   }

   public void testRootManagedRequestLifeCycle() throws Exception
   {
      RootContainer container = createRootContainer("configuration5.xml");
      ManagedComponentRequestLifeCycle component = (ManagedComponentRequestLifeCycle)container.getComponentInstanceOfType(ManagedComponentRequestLifeCycle.class);
      assertNotNull(component);
      MBeanServer server = container.getMBeanServer();
      server.invoke(new ObjectName("exo:object=ManagedComponentRequestLifeCycle"), "foo", new Object[0], new String[0]);
      assertEquals(1, component.startCount);
      assertEquals(1, component.fooCount);
      assertEquals(1, component.endCount);
      assertSame(container, component.startContainer);
      assertSame(container, component.endContainer);
   }
}
