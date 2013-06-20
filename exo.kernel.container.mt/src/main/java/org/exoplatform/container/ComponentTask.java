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

import java.util.concurrent.Callable;

/**
 * This class represents a task to be launched to change the state of a component
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class ComponentTask<T> implements Callable<T>
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
    * The context of the task
    */
   private final ComponentTaskContext ctx;

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
   public ComponentTask(ConcurrentContainerMT container, ComponentTaskContext ctx, DependencyStackListener caller,
      ComponentTaskType type)
   {
      this(null, container, ctx, caller, type);
   }

   /**
    * The main constructor of a task
    */
   public ComponentTask(String name, ConcurrentContainerMT container, ComponentTaskContext ctx,
      DependencyStackListener caller, ComponentTaskType type)
   {
      this.name = name;
      this.container = container;
      this.ctx = ctx;
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
    * @return the context
    */
   public ComponentTaskContext getContext()
   {
      return ctx;
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

   public final T call() throws Exception
   {
      return container.execute(this);
   }

   /**
    * This is what is actually executed
    */
   protected abstract T execute() throws Exception;
}
