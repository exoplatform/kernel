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

import org.exoplatform.container.spi.ComponentAdapter;

import java.util.Collection;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class WrapperComponentAdapterStateAware extends ComponentAdapterStateAware
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -3709971189532454197L;

   private final ComponentAdapter delegate;

   protected WrapperComponentAdapterStateAware(ConcurrentContainerMT exocontainer, ComponentAdapter adapter)
   {
      super(exocontainer, adapter.getComponentKey(), adapter.getComponentImplementation());
      this.delegate = adapter;
   }

   protected ComponentAdapter getNestedComponentAdapter()
   {
      return delegate;
   }
   
   /**
    * {@inheritDoc}
    */
   @Override
   protected Collection<Class<?>> getCreateDependencies()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected ComponentTask<Object> getCreateTask(ComponentTaskContext ctx)
   {
      return new ComponentTask<Object>(exocontainer, ctx, this, ComponentTaskType.CREATE)
      {
         public Object execute() throws Exception
         {
            return delegate.getComponentInstance();
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Collection<Class<?>> getInitDependencies()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected Collection<ComponentTask<Void>> getInitTasks(Object instance, ComponentTaskContext ctx)
   {
      return null;
   }

}