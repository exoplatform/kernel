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

   public void testIsStartedLifeCycleOnContainer()
   {
      //Transaction not started
      assertFalse(a.isStarted(child));
      assertFalse(b.isStarted(child));
      assertFalse(RequestLifeCycle.isStarted(child, false));

      //Begin Request == > Transaction started
      RequestLifeCycle.begin(child, false);
      assertTrue(RequestLifeCycle.isStarted(child, false));
      assertTrue(a.isStarted(child));
      assertTrue(b.isStarted(child));

      //Stop Transaction == > Transaction not started
      a.setStarted(false);
      assertFalse(a.isStarted(child));
      assertTrue(b.isStarted(child));

      //Start new Request lifecycle == > Transaction started
      RequestLifeCycle.begin(child, false);
      assertTrue(a.isStarted(child));
      assertTrue(b.isStarted(child));

      RequestLifeCycle.end();
      assertTrue(a.isStarted(child));
      assertTrue(b.isStarted(child));

      RequestLifeCycle.end();
      assertFalse(a.isStarted(child));
      assertFalse(b.isStarted(child));
   }

   public void testIsStartedLifeCycle()
   {
      assertFalse(a.isStarted(child));
      assertFalse(RequestLifeCycle.isStarted(a));
      RequestLifeCycle.begin(a);
      assertTrue(RequestLifeCycle.isStarted(a));
      assertTrue(a.isStarted(child));

      //Stop Transaction == > Transaction not started
      a.setStarted(false);
      assertFalse(a.isStarted(child));

      RequestLifeCycle.begin(a);
      assertTrue(a.isStarted(child));

      RequestLifeCycle.end();
      assertTrue(a.isStarted(child));

      RequestLifeCycle.end();
      assertFalse(a.isStarted(child));
   }
}
