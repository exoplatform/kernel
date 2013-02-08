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
package org.exoplatform.container.multitenancy;


/**
 * Provides convenient methods to get an information about Current Tenant and provides support for
 * multi-tenant capable services. <br>
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 */
public interface TenantsService {

  /**
   * Return Current Tenant or throws an exception if no current tenant was set in current thread.
   * 
   * @return {@link Tenant} Current Tenant descriptor.
   * @throws CurrentTenantNotSetException if Current Tenant not found or not set.
   */
  Tenant getCurrentTanant() throws CurrentTenantNotSetException;

  /**
   * Add listener for Tenant events in Multitenancy sub-system.
   * Added listener later can be removed by {@link #removeListener(TenantsStateListener)} method.
   * 
   * @param listener {@link TenantsStateListener}
   */
  void addListener(TenantsStateListener listener);

  /**
   * Remove Tenant events listener from Multitenancy sub-system. <br>
   * Take in account that it's possible to remove only explicitly added listeners. Listeners
   * available as components in eXo Container don't affected by this method.
   * 
   * @param listener {@link TenantsStateListener}
   */
  void removeListener(TenantsStateListener listener);

}
