/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.container.multitenancy.bridge;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.multitenancy.CurrentTenantNotSetException;
import org.exoplatform.container.multitenancy.Tenant;

/**
 * Default implementation of Current Tenant lookup. It has not Current Tenant set and throws
 * {@link CurrentTenantNotSetException} always. This implementation can be used with tests.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 */
final class CurrentTenantNotSetLookupPlugin extends BaseComponentPlugin implements CurrentTenantLookup
{

   /**
    * {@inheritDoc}
    */
   public Tenant getCurrentTenant() throws CurrentTenantNotSetException
   {
      // XXX we could return something predefined here (like 'default'), but it is not required
      // on Kernel level and will confuse developers in general case.  
      throw new CurrentTenantNotSetException("Current Tenant not set.");
   }

   public boolean hasCurrentTenant()
   {
      return false;
   }

}
