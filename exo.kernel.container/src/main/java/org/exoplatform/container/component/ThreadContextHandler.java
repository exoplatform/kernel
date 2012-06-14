/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.container.component;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.picocontainer.PicoContainer;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to propagate Thread Local values from one thread to another
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ThreadContextHandler
{
   private final ExoContainer container;

   private List<ThreadContext> contexts;

   public ThreadContextHandler(ExoContainer container)
   {
      this.container = container;
   }

   /**
    * Stores into memory the current values of all the Thread Local variables
    * of all the registered {@link ThreadContextHolder} within the {@link ExoContainer}
    */
   @SuppressWarnings("unchecked")
   public void store()
   {
      final List<ThreadContextHolder> components =
         new ArrayList<ThreadContextHolder>(
            (List<ThreadContextHolder>)container.getComponentInstancesOfType(ThreadContextHolder.class));

      for (PicoContainer current = container.getParent(); current != null; current = current.getParent())
      {
         components.addAll((List<ThreadContextHolder>)current.getComponentInstancesOfType(ThreadContextHolder.class));
      }
      contexts = new ArrayList<ThreadContext>(components.size());
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            for (int i = 0, length = components.size(); i < length; i++)
            {
               ThreadContextHolder holder = components.get(i);
               ThreadContext tc = holder.getThreadContext();
               contexts.add(tc);
               tc.store();
            }
            return null;
         }
      });
   }

   /**
    * Pushes values stored into memory into all the Thread Local variables
    * of all the registered {@link ThreadContextHolder} within the {@link ExoContainer}
    */
   public void push()
   {
      if (contexts == null)
      {
         throw new IllegalStateException("No values have been set, the store method has not been called or failed");
      }
      for (int i = 0, length = contexts.size(); i < length; i++)
      {
         ThreadContext tc = contexts.get(i);
         tc.push();
      }
   }

   /**
    * Restores all the Thread Local variables of all the registered {@link ThreadContextHolder}
    *  within the {@link ExoContainer}
    */
   public void restore()
   {
      if (contexts == null)
      {
         throw new IllegalStateException("No values have been set, the store method has not been called or failed");
      }
      for (int i = 0, length = contexts.size(); i < length; i++)
      {
         ThreadContext tc = contexts.get(i);
         tc.restore();
      }
      contexts.clear();
   }
}
