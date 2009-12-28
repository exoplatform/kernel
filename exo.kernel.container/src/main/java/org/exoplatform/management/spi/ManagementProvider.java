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

/**
 * This interface is implemented by a management provider such a JMX.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 * @param <S> the scope type
 */
public interface ManagementProvider<S>
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
