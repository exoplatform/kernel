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
   List<Map<String, String>> getScopingProperties();

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

   /**
    * Callback to obtain a management provider context  for the specified managed resource scoped with
    * the provided properties.
    *
    * @param managedResource the managed resource
    * @param scopingProperties the scoping properties
    * @return the context
    */
   ManagementProviderContext createContext(Object managedResource, Map<String, String> scopingProperties);

}
