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

import junit.framework.Assert;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.picocontainer.Startable;

import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ComponentWithRequestLifeCycle implements ComponentRequestLifecycle, Startable
{

   /** . */
   private final LinkedList<LifeCycle> lifeCycles = new LinkedList<LifeCycle>();

   public void startRequest(ExoContainer container)
   {
      lifeCycles.addLast(new LifeCycle(true, container));
   }

   public void endRequest(ExoContainer container)
   {
      lifeCycles.addLast(new LifeCycle(false, container));
   }

   public void start()
   {
   }

   public void stop()
   {
   }

   public void assertLifeCycle(boolean start, ExoContainer container)
   {
      Assert.assertTrue(!lifeCycles.isEmpty());
      LifeCycle lf = lifeCycles.removeFirst();
      Assert.assertEquals(start, lf.getStart());
      Assert.assertSame(container, lf.getContainer());
   }

   public void assertEmpty()
   {
      Assert.assertTrue(lifeCycles.isEmpty());
   }
}