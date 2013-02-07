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

import org.exoplatform.container.TenantsContainer;
import org.picocontainer.ComponentAdapter;

import java.util.List;

/**
 * Context for {@link TenantsContainer}. Prescribes general contract between container and
 * multitenancy capable components registration/unregistration. Container should use
 * {@link #accept(ComponentAdapter)} method during the registration to answer should a some
 * component be registered in {@link TenantsContainer} or not. The same way method 
 * {@link #accept(Object)} should be used to choose from where to get a component in getters.<br>
 * Context implementation should apply own "container" for per-tenant managed components and use it
 * for {@link TenantsContainer} methods implementation.
 */
public interface TenantsContainerContext {

  /**
   * Return list of component adapters regarding the given Class key and Current Tenant.
   * 
   * @param componentType {@link Class} type of a components 
   * @return list of component adapters, an empty list if nothing found.
   */
  List<?> getComponentAdaptersOfType(Class<?> componentType);

  /**
   * Return list of component instances regarding the given Class key and Current Tenant.
   * 
   * @param componentType {@link Class} type of instances 
   * @return list of components, an empty list if nothing found.
   */
  List<?> getComponentInstancesOfType(Class<?> componentType);

  /**
   * Return component adapter regarding the given Class key and Current Tenant.
   * 
   * @param key {@link Class} type of a component
   * @return {@link ComponentAdapter} of a component or <code>null</code> if nothing found in Current Tenant.
   */
  ComponentAdapter getComponentAdapterOfType(Class<?> key);

  /**
   * Return component instance regarding the given key (String in most cases) and Current Tenant.
   * 
   * @param key {@link Object} key of a component
   * @return a component instance or <code>null</code> if nothing found in Current Tenant.
   */
  Object getComponentInstance(Object componentKey);

  /**
   * Return component instance regarding the given Class key and Current Tenant.
   * 
   * @param componentType {@link Class} type of a component
   * @return a component instance or <code>null</code> if nothing found in Current Tenant.
   */
  Object getComponentInstanceOfType(Class<?> componentType);

  /**
   * Answers if given component should be regarded as per-tenant service and can be registered (and
   * unregistered) in the context. This method created for use in conjunction with
   * {@link #registerComponent(ComponentAdapter)} and {@link #unregisterComponent(Object)} methods.
   * 
   * @param adapter {@link ComponentAdapter}
   * @return boolean, <code>true</code> if given component should be regarded as per-tenant service,
   *         <code>false</code> otherwise.
   */
  boolean accept(ComponentAdapter adapter);

  /**
   * Answers if given component key should be regarded as a key of per-tenant service and can be
   * used to get a component from the context. This method created for use in conjunction with
   * getters of components.
   * 
   * @see #accept(ComponentAdapter)
   * @param key {@link Object}, it can be a {@link Class} otherwise it will be treated as
   *          {@link String}.
   * @return boolean, <code>true</code> if given key should be regarded as a key of
   *         per-tenant component, <code>false</code> otherwise.
   */
  boolean accept(Object key);

  /**
   * Register component adapter in the context. If this context component itself not yet started by
   * the container it will store given adapter for later new tenants and return <code>false</code>,
   * otherwise will register it into current tenant container and return <code>true</code>. <br>
   * If <code>false</code> returned it means that given component should be registered in the
   * container itself also (for use by Default Tenant).<br>
   * Note that {@link #registerComponent(ComponentAdapter)} method doesn't check if the given
   * component is regarding as per-tenant service. To get this answer use
   * {@link #accept(ComponentAdapter)} method. <br>
   * If an error will occur during the current container calculation the component will not be
   * registered and an exception of type {@link TenantComponentRegistrationException} will be
   * thrown.
   * 
   * @param component {@link ComponentAdapter}
   * @return {@link ComponentAdapter}, Not <code>null</code> indicates that component was successfully registered,
   *         <code>false</code> tells that component also should be registered in the container.
   * @throws TenantComponentRegistrationException if tenant services not ready or Current Tenant
   *           cannot be defined (not set properly in most cases).
   */
  ComponentAdapter registerComponent(ComponentAdapter component) throws TenantComponentRegistrationException;

  /**
   * Unregister component by key from the context. If this context component itself not yet started
   * by the container it will remove internally stored component adapter from later use for new
   * tenants and return <code>null</code>, otherwise it will unregister the component from the
   * context and return its adapter. <br>
   * If <code>null</code> returned it means that this component should be unregistered from the
   * container also. See also registration in {@link #registerComponent(ComponentAdapter)} for
   * <code>false</code> result.<br>
   * Note that {@link #unregisterComponent(ComponentAdapter)} method doesn't check if the given
   * component is regarding as per-tenant service. To get this answer use
   * {@link #accept(ComponentAdapter)} method. <br>
   * If an error will occur during the current container calculation the component will not be
   * unregistered and an exception of type {@link TenantComponentRegistrationException} will be
   * thrown.
   * 
   * @param componentKey {@link Object}
   * @return {@link ComponentAdapter} what was registered in the context or <code>null</code> if
   *         component should be unregistered from the
   *         container.
   * @throws TenantComponentRegistrationException if tenant services not ready or Current Tenant
   *           cannot be defined (not set properly in most cases).
   * @see #registerComponent(ComponentAdapter) for registration details.
   */
  ComponentAdapter unregisterComponent(Object componentKey) throws TenantComponentRegistrationException;

}
