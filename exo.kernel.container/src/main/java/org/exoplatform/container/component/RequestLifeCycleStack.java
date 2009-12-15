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
package org.exoplatform.container.component;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.picocontainer.PicoContainer;

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
class RequestLifeCycleStack extends LinkedList<RequestLifeCycle>
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
         addLast(new RequestLifeCycle(null, Collections.<ComponentRequestLifecycle>emptyList()));
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
      List<ComponentRequestLifecycle> components = new ArrayList<ComponentRequestLifecycle>((List<ComponentRequestLifecycle>)container.getComponentInstancesOfType(ComponentRequestLifecycle.class));

      //
      if (!local)
      {
         for (PicoContainer current = container.getParent();current != null;current = current.getParent())
         {
            components.addAll((List<ComponentRequestLifecycle>)current.getComponentInstancesOfType(ComponentRequestLifecycle.class));
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
}
