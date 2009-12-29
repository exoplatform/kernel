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

package org.exoplatform.container.management;

import org.exoplatform.management.spi.ManagedTypeMetaData;
import org.exoplatform.management.spi.ManagementProviderContext;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagedResource
{

   final ResourceKey key;

   /** . */
   final Object resource;

   /** . */
   final ManagementProviderContext context;

   /** . */
   final ManagedTypeMetaData metaData;

   /** . */
   final ScopedData data;

   public ManagedResource(Object resource, ManagementProviderContext context, ManagedTypeMetaData metaData)
   {
      this.key = new ResourceKey();
      this.resource = resource;
      this.context = context;
      this.metaData = metaData;
      this.data = new ScopedData();
   }

   public void register()
   {
      context.setScopingData(ScopedData.class, data);
   }
}
