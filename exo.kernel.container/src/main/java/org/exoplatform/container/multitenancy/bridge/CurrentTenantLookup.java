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

import org.exoplatform.container.multitenancy.CurrentTenantNotSetException;
import org.exoplatform.container.multitenancy.Tenant;

/**
 * A lookup mechanism to find a Current Tenant. This mechanism provides isolation between the
 * Container and an actual implementation of Multitenancy. <br>
 * Implementations of this interface can provide different algorithms for an actual lookup.<br>
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 */
public interface CurrentTenantLookup
{

   /**
    * Return Current Tenant descriptor or throw {@link CurrentTenantNotSetException} if 
    * Current Tenant not set in current thread.<br>
    * Current Tenant can be not set in two cases:
    * <ul>
    * <li>Thread runs not in a context of multitenant request (e.g. web request). It can be a server startup or some custom thread.</li>
    * <li>Multitenant request wasn't properly initialized on eXo Cloud level. This might have a place in case of internal errors of the cloud.</li>
    * </ul>
    * In both cases an application should not rely on multitenant environment in this thread.   
    * 
    * @throws CurrentTenantNotSetException if current tenant not set.
    * @return {@link Tenant}
    */
   Tenant getCurrentTenant() throws CurrentTenantNotSetException;

   /**
    * Answers if Current Tenant is set in current thread. See {@link #getCurrentTenant()} for details.  
    * 
    * @see #getCurrentTenant()
    * @return boolean, {@code true} if current tenant is set in current thread, {@code false} otherwise.
    */
   boolean hasCurrentTenant();

}
