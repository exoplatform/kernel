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
package org.exoplatform.container.component;

import org.exoplatform.container.ExoContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class RequestLifeCycleStack
   extends LinkedList<RequestLifeCycle>
{

   /** . */
   private final Set<ComponentRequestLifecycle> allComponents = new HashSet<ComponentRequestLifecycle>();

   RequestLifeCycleStack()
   {
   }

   void begin(ComponentRequestLifecycle lifeCycle)
   {
      if (allComponents.contains(lifeCycle))
      {
         //If the current ComponentRequestLifecycle  is registered and is not started
         //Try to restart it a gain
         if(! lifeCycle.isStarted(null))
         {
            lifeCycle.startRequest(null);
         }
         addLast(new RequestLifeCycle(null, Collections.<ComponentRequestLifecycle> emptyList()));
      }
      else
      {
         RequestLifeCycle requestLF = new RequestLifeCycle(null, Collections.singletonList(lifeCycle));
         allComponents.add(lifeCycle);
         addLast(requestLF);
         requestLF.doBegin();
      }
   }

   void begin(ExoContainer container, boolean local)
   {
      // Need to make a copy as modifying the list is cached by the container
      List<ComponentRequestLifecycle> components = getAllComponentsRequestLifecycle(container, local);

      //check if list of RequestLifecycle already registered is still started
      for(ComponentRequestLifecycle c : allComponents)
      {
         if(!c.isStarted(container))
         {
            c.startRequest(container);
         }
      }

      // Remove components that have already started their life cycle
      components.removeAll(allComponents);

      // Contribute to the all component set
      allComponents.addAll(components);

      //
      RequestLifeCycle lifeCycle = new RequestLifeCycle(container, components);

      //
      addLast(lifeCycle);

      //
      lifeCycle.doBegin();
   }

   Map<Object, Throwable> end()
   {
      RequestLifeCycle lifeCycle = removeLast();

      //
      IdentityHashMap<Object, Throwable> result = lifeCycle.doEnd();

      //
      allComponents.removeAll(result.keySet());

      //
      return result;
   }

    boolean isStarted(ExoContainer container, boolean local)
    {
      List<ComponentRequestLifecycle> components =  getAllComponentsRequestLifecycle(container, local);

      for(ComponentRequestLifecycle c : components)
      {
         if(! isStarted(c))
         {
            return false;
         }
      }

      return true;
   }

   boolean isStarted(ComponentRequestLifecycle lifeCycle)
   {
      return allComponents.contains(lifeCycle) && lifeCycle.isStarted(null);
   }

   private List<ComponentRequestLifecycle> getAllComponentsRequestLifecycle(ExoContainer container, boolean local)
   {
      List<ComponentRequestLifecycle> components =
              new ArrayList<ComponentRequestLifecycle>((List<ComponentRequestLifecycle>) container
                      .getComponentInstancesOfType(ComponentRequestLifecycle.class));

      if (!local)
      {
         for (ExoContainer current = container.getParent(); current != null; current = current.getParent())
         {
            components.addAll((List<ComponentRequestLifecycle>) current
                    .getComponentInstancesOfType(ComponentRequestLifecycle.class));
         }

      }
      return components;
   }
}
