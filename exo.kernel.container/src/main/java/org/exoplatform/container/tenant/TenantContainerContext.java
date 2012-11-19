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
package org.exoplatform.container.tenant;

import org.exoplatform.container.TenantContainer;
import org.picocontainer.ComponentAdapter;

import java.util.List;

/**
 * Context for {@link TenantContainer}. Prescribes general contract between container and
 * multitenancy capable components registration. Container should user
 * {@link #accept(ComponentAdapter)} method during the registration to answer  should be some
 * component registered in {@link TenantContainer} or not. <br>
 * Context applies own container for per-tenant managed components and used in
 * {@link TenantContainer} for its methods implementation.
 */
public interface TenantContainerContext {

  @SuppressWarnings("rawtypes")
  List getComponentAdaptersOfType(Class componentType);

  @SuppressWarnings("rawtypes")
  List getComponentInstancesOfType(Class componentType);

  @SuppressWarnings("rawtypes")
  ComponentAdapter getComponentAdapterOfType(Class key);

  Object getComponentInstance(Object componentKey);

  Object getComponentInstanceOfType(Class<?> componentType);

  boolean accept(ComponentAdapter adapter);

  void registerComponent(ComponentAdapter component);

}
