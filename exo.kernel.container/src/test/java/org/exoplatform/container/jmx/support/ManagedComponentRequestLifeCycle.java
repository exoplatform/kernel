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
package org.exoplatform.container.jmx.support;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.picocontainer.Startable;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@Managed
@NameTemplate(@Property(key = "object", value = "ManagedComponentRequestLifeCycle"))
public class ManagedComponentRequestLifeCycle implements ComponentRequestLifecycle, Startable
{

   public int fooCount = 0;

   public int startCount = 0;

   public int endCount = 0;

   public ExoContainer startContainer;

   public ExoContainer endContainer;

   @Managed
   public void foo()
   {
      if (startCount == 1 && endCount == 0)
      {
         fooCount++;
      }
   }


   public void startRequest(ExoContainer container)
   {
      if (fooCount == 0 && endCount == 0)
      {
         startCount++;
         startContainer = container;
      }
   }

   public void endRequest(ExoContainer container)
   {
      if (startCount == 1 && fooCount == 1)
      {
         endCount++;
         endContainer = container;
      }
   }

   public void start()
   {
   }

   public void stop()
   {
   }
}
