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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.support.ExoContainerFinder;
import org.exoplatform.container.jmx.support.ManagedWithObjectNameTemplate;

import java.util.Set;

import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRegistration extends AbstractTestContainer
{

   public void testFoo() throws Exception
   {
      RootContainer root = createRootContainer("registration-configuration.xml");
      assertNotNull(root.getMBeanServer());

      Object instance = root.getComponentInstance("Foo");
      assertNotNull(instance);

      MBeanServer server = root.getMBeanServer();

      Set<ObjectInstance> set = server.queryMBeans(ObjectName.getInstance("exo:object=Foo"), null);
      assertEquals(1, set.size());

      ObjectInstance oi = set.iterator().next();
      ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
      
      ExoContainer currentContainer = new ExoContainer();
      ExoContainerContext.setCurrentContainer(currentContainer);
      try
      {
         ExoContainerFinder proxyObject =
            MBeanServerInvocationHandler.newProxyInstance(server, oi.getObjectName(),
               ExoContainerFinder.class, false);
         assertTrue("We expect to get the current exo container", oldContainer == proxyObject.getCurrentExoContainer());
         assertTrue("We expect to get the previous exo container", ExoContainerContext.getCurrentContainerIfPresent() == currentContainer);
         ExoContainerContext.setCurrentContainer(oldContainer);
         assertTrue("We expect to get the current exo container", oldContainer == proxyObject.getCurrentExoContainer());
         assertTrue("We expect to get the previous exo container", ExoContainerContext.getCurrentContainerIfPresent() == oldContainer);
         ExoContainerContext.setCurrentContainer(null);
         assertTrue("We expect to get the current exo container", oldContainer == proxyObject.getCurrentExoContainer());
         assertTrue("We expect to get the previous exo container", ExoContainerContext.getCurrentContainerIfPresent() == oldContainer);
      }
      finally
      {
         ExoContainerContext.setCurrentContainer(oldContainer);
      }
      
      // Manual

      root.registerComponentInstance("Bar", new ManagedWithObjectNameTemplate("Bar"));

      Object instance2 = root.getComponentInstance("Bar");
      assertNotNull(instance2);

      Set set2 = server.queryMBeans(ObjectName.getInstance("exo:object=Bar"), null);
      assertEquals(1, set2.size());

   }

}
