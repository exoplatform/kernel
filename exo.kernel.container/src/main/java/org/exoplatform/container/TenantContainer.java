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
package org.exoplatform.container;

import org.exoplatform.container.tenant.TenantContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import org.picocontainer.defaults.InstanceComponentAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 *
 */
public class TenantContainer extends CachingContainer {

  private static final long        serialVersionUID = 1945046643718969920L;

  protected TenantContainerContext tenantContainerContext;

  public TenantContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent) {
    super(componentAdapterFactory, parent);
  }

  public TenantContainer(PicoContainer parent) {
    super(parent);
  }

  public TenantContainer(ComponentAdapterFactory componentAdapterFactory) {
    super(componentAdapterFactory);
  }

  public TenantContainer() {
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  public ComponentAdapter getComponentAdapterOfType(Class componentType) {
    if (tenantContainerContext != null) {
      ComponentAdapter adapter = tenantContainerContext.getComponentAdapterOfType(componentType);
      if (adapter != null) {
        return adapter;
      }
    }
    return super.getComponentAdapterOfType(componentType);
  }

  @Override
  public Object getComponentInstance(Object componentKey) throws PicoException {
    if (tenantContainerContext != null) {
      Object adapter = tenantContainerContext.getComponentInstance(componentKey);
      if (adapter != null) {
        return adapter;
      }
    }
    return super.getComponentInstance(componentKey);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List getComponentAdaptersOfType(Class componentType) {
    List result = new ArrayList();
    result.addAll(super.getComponentAdaptersOfType(componentType));

    if (tenantContainerContext != null) {
      List adapters = tenantContainerContext.getComponentAdaptersOfType(componentType);
      if (adapters != null) {
        result.addAll(adapters);
      }
    }
    return result;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List getComponentInstancesOfType(Class componentType) throws PicoException {
    // XXX: order of components in the list broken as we taking it from two sources
    List result = new ArrayList();
    result.addAll(super.getComponentInstancesOfType(componentType));

    if (tenantContainerContext != null) {
      List instances = tenantContainerContext.getComponentInstancesOfType(componentType);
      if (instances != null) {
        result.addAll(instances);
      }
    }
    return result;
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  public Object getComponentInstanceOfType(Class componentType) {
    if (tenantContainerContext != null && !componentType.equals(ExoContainerContext.class)) {
      Object adapter = tenantContainerContext.getComponentInstanceOfType(componentType);
      if (adapter != null) {
        return adapter;
      }
    }
    return super.getComponentInstanceOfType(componentType);
  }

  @Override
  public ComponentAdapter registerComponent(ComponentAdapter componentAdapter) throws DuplicateComponentKeyRegistrationException {
    if (tenantContainerContext != null && tenantContainerContext.accept(componentAdapter)) {
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponent(componentAdapter);
  }
}
