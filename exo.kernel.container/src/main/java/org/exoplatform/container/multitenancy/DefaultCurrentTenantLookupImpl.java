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
 * Default implementation of Current Tenant lookup. It throws {@link CurrentTenantNotSetException} always.
 */
public class DefaultCurrentTenantLookupImpl implements CurrentTenantLookup {

  /**
   * {@inheritDoc}
   */
  @Override
  public Tenant getCurrentTenant() throws CurrentTenantNotSetException {
    // XXX we could return something dummy (or predefined as default) here, but it is not required on Kernel
    // level. For non-cloud mode, where JCR used as main CF storage, an another implementation can do this properly
    // using Current Repository.
    throw new CurrentTenantNotSetException("Current Tenant not set.");
  }

}
