/*
 * Copyright (C) 2013 eXo Platform SAS.
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

import org.exoplatform.container.ConcurrentContainer.CreationalContextComponentAdapter;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import javax.enterprise.context.spi.CreationalContext;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ComponentTaskContext
{
   /**
    * A {@link LinkedHashSet} representing the dependency stack
    */
   private final LinkedHashSet<ComponentTaskContextEntry> dependencies;

   /**
    * The last dependency that has been added to the context
    */
   private final ComponentTaskContextEntry lastDependency;

   /**
    * The last task type known
    */
   private final ComponentTaskType lastTaskType;

   /**
    * Context used to keep in memory the components that are currently being created.
    * This context is used to prevent cyclic resolution due to component plugins.
    */
   private Map<Object, CreationalContextComponentAdapter<?>> depResolutionCtx;

   private ComponentTaskContext(LinkedHashSet<ComponentTaskContextEntry> dependencies,
      Map<Object, CreationalContextComponentAdapter<?>> depResolutionCtx, ComponentTaskContextEntry lastDependency,
      ComponentTaskType lastTaskType)
   {
      this.dependencies = dependencies;
      this.depResolutionCtx = depResolutionCtx;
      this.lastDependency = lastDependency;
      this.lastTaskType = lastTaskType;
   }

   /**
    * Default constructor
    */
   public ComponentTaskContext(Object componentKey, ComponentTaskType type)
   {
      LinkedHashSet<ComponentTaskContextEntry> dependencies = new LinkedHashSet<ComponentTaskContextEntry>();
      ComponentTaskContextEntry entry = new ComponentTaskContextEntry(componentKey, type);
      dependencies.add(entry);
      this.dependencies = dependencies;
      this.lastDependency = entry;
      this.lastTaskType = type;
   }

   /**
    * Defines explicitly the last task type known
    */
   public ComponentTaskContext setLastTaskType(ComponentTaskType lastTaskType)
   {
      return new ComponentTaskContext(dependencies, depResolutionCtx == null ? null
         : new HashMap<Object, ConcurrentContainer.CreationalContextComponentAdapter<?>>(depResolutionCtx),
         lastDependency, lastTaskType);
   }

   /**
    * This method will call {@link #addToContext(Object, ComponentTaskType)} with the <code>lastTaskType</code> as type
    */
   public ComponentTaskContext addToContext(Object componentKey) throws CyclicDependencyException
   {
      return addToContext(componentKey, lastTaskType);
   }

   /**
    * Creates a new {@link ComponentTaskContext} based on the given dependency and the 
    * already registered ones. If the dependency has already been registered
    * a {@link CyclicDependencyException} will be thrown.
    */
   public ComponentTaskContext addToContext(Object componentKey, ComponentTaskType type)
      throws CyclicDependencyException
   {
      ComponentTaskContextEntry entry = new ComponentTaskContextEntry(componentKey, type);
      checkDependency(entry);
      LinkedHashSet<ComponentTaskContextEntry> dependencies =
         new LinkedHashSet<ComponentTaskContextEntry>(this.dependencies);
      dependencies.add(entry);
      return new ComponentTaskContext(dependencies, depResolutionCtx == null ? null
         : new HashMap<Object, ConcurrentContainer.CreationalContextComponentAdapter<?>>(depResolutionCtx), entry, type);
   }

   /**
    * Checks if the given dependency has already been defined, if so a {@link CyclicDependencyException}
    * will be thrown.
    */
   public void checkDependency(Object componentKey, ComponentTaskType type) throws CyclicDependencyException
   {
      ComponentTaskContextEntry entry = new ComponentTaskContextEntry(componentKey, type);
      checkDependency(entry);
   }

   /**
    * Indicates whether the provided componentKey is the last dependency that has been added to the context.
    * @return <code>true</code> if the dependency is the last, <code>false</code> otherwise.
    */
   public boolean isLast(Object componentKey)
   {
      ComponentTaskContextEntry entry = new ComponentTaskContextEntry(componentKey, lastTaskType);
      return lastDependency.equals(entry);
   }

   /**
    * Checks if the given dependency has already been defined, if so a {@link CyclicDependencyException}
    * will be thrown.
    */
   private void checkDependency(ComponentTaskContextEntry entry)
   {
      if (entry.getTaskType() == ComponentTaskType.CREATE
         && dependencies.contains(entry)
         && (depResolutionCtx == null || !depResolutionCtx.containsKey(entry.getComponentKey()) || depResolutionCtx
            .get(entry.getComponentKey()).get() == null))
      {
         boolean startToCheck = false;
         Boolean sameType = null;
         for (ComponentTaskContextEntry e : dependencies)
         {
            if (startToCheck)
            {
               if (e.getTaskType() != entry.getTaskType())
               {
                  sameType = Boolean.FALSE;
                  break;
               }
               sameType = Boolean.TRUE;
            }
            else if (entry.equals(e))
            {
               startToCheck = true;
            }
         }
         if (sameType != null && sameType.booleanValue())
         {
            throw new CyclicDependencyException(entry, sameType);
         }
      }
   }

   /**
    * @return indicates whether the current context is the root context or not.
    */
   public boolean isRoot()
   {
      return dependencies.size() == 1;
   }

   /**
    * Adds the {@link CreationalContext} of the component corresponding to the given key, to the dependency resolution
    * context
    * @param key The key of the component to add to the context
    * @param ctx The {@link CreationalContext} of the component to add to the context
    * @return {@link CreationalContextComponentAdapter} instance that has been put into the map
    */
   @SuppressWarnings("unchecked")
   public <T> CreationalContextComponentAdapter<T> addComponentToContext(Object key,
      CreationalContextComponentAdapter<T> ctx)
   {
      if (depResolutionCtx == null)
      {
         depResolutionCtx = new HashMap<Object, ConcurrentContainer.CreationalContextComponentAdapter<?>>();
         depResolutionCtx.put(key, ctx);
         return ctx;
      }
      CreationalContextComponentAdapter<?> prevValue = depResolutionCtx.get(key);
      if (prevValue == null)
      {
         depResolutionCtx.put(key, ctx);
         return ctx;
      }
      return (CreationalContextComponentAdapter<T>)prevValue;
   }

   /**
    * Removes the {@link CreationalContext} of the component corresponding to the given key, from the dependency resolution
    * context
    * @param key The key of the component to remove from the context
    */
   public CreationalContextComponentAdapter<?> removeComponentFromContext(Object key)
   {
      if (depResolutionCtx == null)
         return null;
      return depResolutionCtx.remove(key);
   }

   /**
    * Tries to get the component related to the given from the context, if it can be found the current state of the component
    * instance is returned, otherwise <code>null</code> is returned
    */
   public <T> T getComponentInstanceFromContext(Object key, Class<T> bindType)
   {
      if (depResolutionCtx == null)
         return null;
      CreationalContextComponentAdapter<?> ctx = depResolutionCtx.get(key);
      return ctx == null ? null : bindType.cast(ctx.get());
   }

   /**
    * Resets the dependencies but keeps the current dependency resolution context.
    * @param key the key of the new first dependency
    * @param type the type of the corresponding task
    * @return a {@link ComponentTaskContext} instance with the dependencies reseted
    */
   public ComponentTaskContext resetDependencies(Object key, ComponentTaskType type)
   {
      LinkedHashSet<ComponentTaskContextEntry> dependencies = new LinkedHashSet<ComponentTaskContextEntry>();
      ComponentTaskContextEntry entry = new ComponentTaskContextEntry(key, type);
      dependencies.add(entry);
      return new ComponentTaskContext(dependencies, depResolutionCtx == null ? null
         : new HashMap<Object, ConcurrentContainer.CreationalContextComponentAdapter<?>>(depResolutionCtx), entry, type);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      return "ComponentTaskContext [dependencies=" + dependencies + ", depResolutionCtx=" + depResolutionCtx + "]";
   }
}
