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

/**
 * This class represents a task to be launched to change the state of a component
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class ComponentTask<T>
{

   /**
    * The name of the task
    */
   private final String name;

   /**
    * The container that holds the component
    */
   private final ConcurrentContainerMT container;

   /**
    * The component that expects to be notified any time we
    * try to access to a dependency non properly declared
    */
   private final DependencyStackListener caller;

   /**
    * The type of the task
    */
   private final ComponentTaskType type;

   /**
    * The main constructor of a task
    */
   public ComponentTask(ConcurrentContainerMT container, DependencyStackListener caller, ComponentTaskType type)
   {
      this(null, container, caller, type);
   }

   /**
    * The main constructor of a task
    */
   public ComponentTask(String name, ConcurrentContainerMT container, DependencyStackListener caller,
      ComponentTaskType type)
   {
      this.name = name;
      this.container = container;
      this.caller = caller;
      this.type = type;
   }

   /**
    * @return the name
    */
   public String getName()
   {
      return name;
   }

   /**
    * @return the container
    */
   public ConcurrentContainerMT getContainer()
   {
      return container;
   }

   /**
    * @return the caller
    */
   public DependencyStackListener getCaller()
   {
      return caller;
   }

   /**
    * @return the type of the task
    */
   public ComponentTaskType getType()
   {
      return type;
   }

   public final T call(CreationalContextComponentAdapter<?> cCtx) throws Exception
   {
      return container.execute(this, cCtx);
   }

   /**
    * This is what is actually executed
    */
   protected abstract T execute(CreationalContextComponentAdapter<?> cCtx) throws Exception;
}
