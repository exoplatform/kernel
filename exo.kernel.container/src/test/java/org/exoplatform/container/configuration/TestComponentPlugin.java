/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.container.configuration;

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.jmx.AbstractTestContainer;

/**
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestComponentPlugin extends AbstractTestContainer
{
   public void testComponentPluginOverloading()
   {
      RootContainer rootContainer = createRootContainer("test-component-plugin-configuration.xml");
      A a = (A)rootContainer.getComponentInstanceOfType(A.class);
      assertNotNull(a);
      assertEquals(1, a.countRegisterCP);
      assertEquals(1, a.countRegisterACP);
      assertEquals(2, a.countRegisterCP1);
   }

   public static class A
   {
      public int countRegisterCP;

      public int countRegisterACP;

      public int countRegisterCP1;

      public void register(ComponentPlugin plugin)
      {
         countRegisterCP++;
      }

      public void register(AComponentPlugin plugin)
      {
         countRegisterACP++;
      }

      public void register(ComponentPlugin1 plugin)
      {
         countRegisterCP1++;
      }

   }

   public static abstract class AComponentPlugin implements ComponentPlugin
   {

      public String getDescription()
      {
         return null;
      }

      public String getName()
      {
         return null;
      }

      public void setDescription(String s)
      {
      }

      public void setName(String s)
      {
      }
   }

   public static class ComponentPlugin1 extends AComponentPlugin
   {
   }

   public static class ComponentPlugin2 extends AComponentPlugin
   {
   }

   public static class ComponentPlugin3 extends ComponentPlugin1
   {
   }

   public static class ComponentPlugin4 implements ComponentPlugin
   {

      public String getDescription()
      {
         return null;
      }

      public String getName()
      {
         return null;
      }

      public void setDescription(String s)
      {
      }

      public void setName(String s)
      {
      }
   }
}
