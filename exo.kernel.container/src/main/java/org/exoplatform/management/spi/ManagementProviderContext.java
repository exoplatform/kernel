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
import java.util.Map;

/**
 * The contract between a management provider and the kernel.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ManagementProviderContext extends ManagementContext
{

   /**
    * Returns the scoping properties of the context;
    *
    * @return the scoping properties
    */
   <S> List<S> getScopingProperties(Class<S> scopeType);

   /**
    * Callback to obtain a management provider context for the specified managed resource scoped with
    * the provided properties.
    *
    * @param scopingProperties the scoping properties
    */
   <S> void setScopingData(Class<S> scopeType, S scopingProperties);

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
