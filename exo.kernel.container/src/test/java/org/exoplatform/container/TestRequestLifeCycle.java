/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.container;

import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.jmx.AbstractTestContainer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestRequestLifeCycle extends AbstractTestContainer
{

   /** . */
   private ExoContainer parent;

   /** . */
   private ExoContainer child;

   /** . */
   private ComponentWithRequestLifeCycle a;

   /** . */
   private ComponentWithRequestLifeCycle b;

   @Override
   protected void setUp() throws Exception
   {
      RootContainer parent = createRootContainer("request-lifecycle-configuration.xml");
      ExoContainer child = new ExoContainer(parent);
      ComponentWithRequestLifeCycle a = (ComponentWithRequestLifeCycle)parent.getComponentInstance("A");
      assertNotNull(a);
      child.registerComponentInstance("B", new ComponentWithRequestLifeCycle());
      ComponentWithRequestLifeCycle b = (ComponentWithRequestLifeCycle)child.getComponentInstance("B");

      //
      this.parent = parent;
      this.child = child;
      this.a = a;
      this.b = b;
   }

   public void testObject()
   {
      RequestLifeCycle.begin(a);
      a.assertLifeCycle(true, null);
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, null);
      b.assertEmpty();
   }

   public void testContainerObject()
   {
      RequestLifeCycle.begin(parent, false);
      a.assertLifeCycle(true, parent);
      b.assertEmpty();
      RequestLifeCycle.begin(a);
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, parent);
      b.assertEmpty();
   }

   public void testObjectContainer()
   {
      RequestLifeCycle.begin(a);
      a.assertLifeCycle(true, null);
      b.assertEmpty();
      RequestLifeCycle.begin(parent, false);
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, null);
      b.assertEmpty();
   }

   public void testParent()
   {
      RequestLifeCycle.begin(parent, false);
      a.assertLifeCycle(true, parent);
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, parent);
      b.assertEmpty();
   }

   public void testLocalParent()
   {
      RequestLifeCycle.begin(parent, true);
      a.assertLifeCycle(true, parent);
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, parent);
      b.assertEmpty();
   }

   public void testChild()
   {
      RequestLifeCycle.begin(child, false);
      a.assertLifeCycle(true, child);
      b.assertLifeCycle(true, child);
      RequestLifeCycle.end();
      a.assertLifeCycle(false, child);
      b.assertLifeCycle(false, child);
   }

   public void testLocalChild()
   {
      RequestLifeCycle.begin(child, true);
      a.assertEmpty();
      b.assertLifeCycle(true, child);
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertLifeCycle(false, child);
   }

   public void testParentParent()
   {
      RequestLifeCycle.begin(parent, false);
      a.assertLifeCycle(true, parent);
      b.assertEmpty();
      RequestLifeCycle.begin(parent, false);
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, parent);
      b.assertEmpty();
   }

   public void testChildChild()
   {
      RequestLifeCycle.begin(child, false);
      a.assertLifeCycle(true, child);
      b.assertLifeCycle(true, child);
      RequestLifeCycle.begin(child, false);
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, child);
      b.assertLifeCycle(false, child);
   }

   public void testChildParent()
   {
      RequestLifeCycle.begin(child, false);
      a.assertLifeCycle(true, child);
      b.assertLifeCycle(true, child);
      RequestLifeCycle.begin(parent, false);
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, child);
      b.assertLifeCycle(false, child);
   }

   public void testLocalChildParent()
   {
      RequestLifeCycle.begin(child, true);
      a.assertEmpty();
      b.assertLifeCycle(true, child);
      RequestLifeCycle.begin(parent, true);
      a.assertLifeCycle(true, parent);
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertLifeCycle(false, parent);
      b.assertEmpty();
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertLifeCycle(false, child);
   }

   public void testParentChild()
   {
      RequestLifeCycle.begin(parent, false);
      a.assertLifeCycle(true, parent);
      b.assertEmpty();
      RequestLifeCycle.begin(child, false);
      a.assertEmpty();
      b.assertLifeCycle(true, child);
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertLifeCycle(false, child);
      RequestLifeCycle.end();
      a.assertLifeCycle(false, parent);
      b.assertEmpty();
   }

   public void testLocalParentChild()
   {
      RequestLifeCycle.begin(parent, true);
      a.assertLifeCycle(true, parent);
      b.assertEmpty();
      RequestLifeCycle.begin(child, true);
      a.assertEmpty();
      b.assertLifeCycle(true, child);
      RequestLifeCycle.end();
      a.assertEmpty();
      b.assertLifeCycle(false, child);
      RequestLifeCycle.end();
      a.assertLifeCycle(false, parent);
      b.assertEmpty();
   }
}
