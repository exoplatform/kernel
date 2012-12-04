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
 * multi-tenant capable services. This service used by {@link TenantsContainerContext}
 * implementation.
 * 
 */
public interface TenantsService {

  /**
   * Return Current Tenant name or throws an exception if no current tenant was set.
   * 
   * @return {@link String} with name of Current Tenant.
   * @throws RepositoryException if Current Tenant not found or not set.
   */
  String getCurrentTanantName() throws CurrentTenantNotSetException;

  /**
   * Wrap given component instance into multi-tenant context. Doing this an user gets a guaranty
   * that his component always will relate to the Current Tenant context (component will be taken
   * from the tenant container).<br>
   * Implementation of this method will get the component key(s) from given instance and late will
   * use them in {@link Multitenant#get()} method.
   * 
   * @param T component instance
   * @return {@link Multitenant} instance
   */
  <T> Multitenant<T> asMultitenant(T componnet);

  /**
   * Create a component wrapper using given class as a component key for use in multi-tenant
   * context. Doing this an user gets a guaranty that his component always will relate to the
   * Current Tenant context (component will be taken from the tenant container). <br>
   * Implementation of this method will use given class as a key in the container and late will use
   * it in {@link Multitenant#get()} method.
   * 
   * @param Class componentType
   * @return {@link Multitenant} instance
   */
  <T> Multitenant<T> asMultitenant(Class<T> componentType);

}
