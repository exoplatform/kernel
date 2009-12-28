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

import org.exoplatform.management.spi.ManagedTypeMetaData;

/**
 * This interface is implemented by a management provider such a JMX.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ManagementProvider
{

   /**
    * Instruct the management provider to manage the provided resource with the specified meta data.
    *
    * @param context the context
    * @param managedResource the managed resource
    * @param metaData the meta data describing the management interface
    * @return the key under which the resource is registered
    */
   Object manage(ManagementProviderContext context, Object managedResource, ManagedTypeMetaData metaData);

   /**
    * Instruct the management provider to remove the specifed resource from management.
    *
    * @param key the key under which the resource is registered
    */
   void unmanage(Object key);

}
