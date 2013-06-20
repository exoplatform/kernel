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

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

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
    * Indicates whether the dependencies should be checked or not. In case it is set to <code>false</code>
    * and the dependency that we would like to add has already been registered, a {@link CyclicDependencyException}
    * will be thrown otherwise it will be ignored.
    */
   private final boolean skipDependencyChecking;

   /**
    * Indicates whether the multi-threading is allowed or not. In case it is set to <code>true</code> and the
    * kernel is launched in multi-threaded mode, the task won't be delegated to the thread pool, it will be managed
    * by the current thread instead to prevent possible deadlock.
    */
   private final boolean disableMultiThreading;

   /**
    * Context used to keep in memory the components that are currently being created.
    * This context is used to prevent cyclic resolution due to component plugins.
    */
   private final Map<Object, Object> depResolutionCtx;

   private ComponentTaskContext(LinkedHashSet<ComponentTaskContextEntry> dependencies, boolean skipDependencyChecking,
      boolean disableMultiThreading, Map<Object, Object> depResolutionCtx)
   {
      this.dependencies = dependencies;
      this.skipDependencyChecking = skipDependencyChecking;
      this.disableMultiThreading = disableMultiThreading;
      this.depResolutionCtx = depResolutionCtx;
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
      this.skipDependencyChecking = false;
      this.disableMultiThreading = false;
      this.depResolutionCtx = null;
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
      return new ComponentTaskContext(dependencies, skipDependencyChecking, disableMultiThreading, depResolutionCtx);
   }

   /**
    * Creates a new {@link ComponentTaskContext} based on the already registered dependencies
    * with the flag skip dependency checking set to <code>true</code>. If it is already in skip dependency
    * checking mode, an {@link IllegalStateException} will be thrown
    */
   public ComponentTaskContext toSkipDependencyChecking() throws IllegalStateException
   {
      if (skipDependencyChecking)
         throw new IllegalStateException("The context is already in skip dependency checking mode.");
      return new ComponentTaskContext(dependencies, true, disableMultiThreading, depResolutionCtx);
   }

   /**
    * Creates a new {@link ComponentTaskContext} based on the already registered dependencies
    * with the flag disable multi-threading set to <code>true</code>. If it is already in disable 
    * multi-threading mode, it will return itself
    */
   public ComponentTaskContext toDisableMultiThreading()
   {
      if (disableMultiThreading)
         return this;
      return new ComponentTaskContext(dependencies, skipDependencyChecking, true, depResolutionCtx);
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
    * Checks if the given dependency has already been defined, if so a {@link CyclicDependencyException}
    * will be thrown.
    */
   private void checkDependency(ComponentTaskContextEntry entry)
   {
      if (dependencies.contains(entry)
         && (depResolutionCtx == null || !depResolutionCtx.containsKey(entry.getComponentKey())))
      {
         boolean startToCheck = false;
         boolean sameType = true;
         for (ComponentTaskContextEntry e : dependencies)
         {
            if (startToCheck)
            {
               if (e.getTaskType() != entry.getTaskType())
               {
                  sameType = false;
                  break;
               }
            }
            else if (entry.equals(e))
            {
               startToCheck = true;
            }
         }
         if (!skipDependencyChecking || sameType)
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
    * Indicates whether the dependencies should be checked or not. In case it is set to <code>false</code>
    * and the dependency that we would like to add has already been registered, a {@link CyclicDependencyException}
    * will be thrown otherwise it will be ignored.
    */
   public boolean skipDependencyChecking()
   {
      return skipDependencyChecking;
   }

   /**
    * Indicates whether the multi-threading is allowed or not. In case it is set to <code>true</code> and the
    * kernel is launched in multi-threaded mode, the task won't be delegated to the thread pool, it will be managed
    * by the current thread instead to prevent possible deadlock.
    */
   public boolean disableMultiThreading()
   {
      return disableMultiThreading;
   }

   /**
    * Add the component corresponding to the given key, to the dependency resolution
    * context
    * @param key The key of the component to add to the context
    * @param component The instance of the component to add to the context
    */
   public ComponentTaskContext addComponentToContext(Object key, Object component)
   {
      Map<Object, Object> depResolutionCtx = this.depResolutionCtx;
      if (depResolutionCtx == null)
      {
         depResolutionCtx = new HashMap<Object, Object>();
      }
      else
      {
         depResolutionCtx = new HashMap<Object, Object>(depResolutionCtx);
      }
      depResolutionCtx.put(key, component);
      return new ComponentTaskContext(dependencies, skipDependencyChecking, disableMultiThreading, depResolutionCtx);
   }

   /**
    * Tries to get the component related to the given from the context, if it can be found the current state of the component
    * instance is returned, otherwise <code>null</code> is returned
    */
   public Object getComponentFromContext(Object key)
   {
      return depResolutionCtx != null ? depResolutionCtx.get(key) : null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String toString()
   {
      return "ComponentTaskContext [dependencies=" + dependencies + ", skipDependencyChecking="
         + skipDependencyChecking + ", disableMultiThreading=" + disableMultiThreading + ", depResolutionCtx="
         + depResolutionCtx + "]";
   }
}
