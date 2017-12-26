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

   private boolean isTransactionStarted;

   public void startRequest(ExoContainer container)
   {
      lifeCycles.addLast(new LifeCycle(true, container));
      isTransactionStarted = true;
   }

   public void endRequest(ExoContainer container)
   {
      lifeCycles.addLast(new LifeCycle(false, container));
      isTransactionStarted = false;
   }

   public boolean isStarted(ExoContainer container)
   {
      return isTransactionStarted;
   }

   public void setStarted(boolean started)
   {
      isTransactionStarted = started;
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