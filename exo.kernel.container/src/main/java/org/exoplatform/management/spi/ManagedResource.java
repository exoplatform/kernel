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
package org.exoplatform.management.spi;

import org.exoplatform.management.ManagementContext;

import java.util.List;

/**
 * The contract between a management provider and the kernel.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ManagedResource extends ManagementContext
{

   /**
    * The resource implementation.
    *
    * @return the resource
    */
   Object getResource();

   /**
    * Returns the resource meta data.
    *
    * @return the meta data
    */
   ManagedTypeMetaData getMetaData();

   /**
    * Returns the scoping data of the context. The list contains the scoping properties
    * registered by the provider if a call has been made to the {@link #setScopingData(Class, Object)}
    * method plus the scoping properties of the parent context.
    *
    * @param scopeType the scope type
    * @param <S> the generic type of the scope type
    * @return the scoping properties
    */
   <S> List<S> getScopingData(Class<S> scopeType);

   /**
    * Callback made by the provie to the resource to signal that scopgin data is used for the managed resource.
    *
    * @param scopeType the scope type
    * @param <S> the generic type of the scope type
    * @param scopingData the scoping data
    */
   <S> void setScopingData(Class<S> scopeType, S scopingData);

   /**
    * Before a managed resource is invoked by the management layer.
    *
    * @param managedResource the managed resource
    */
   void beforeInvoke(Object managedResource);

   /**
    * After a managed resource is invoked by the management layer.
    *
    * @param managedResource the managed resource
    */
   void afterInvoke(Object managedResource);

}
