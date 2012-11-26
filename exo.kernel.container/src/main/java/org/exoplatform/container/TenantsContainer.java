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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.tenant.TenantsContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import org.picocontainer.defaults.InstanceComponentAdapter;

/**
 * 
 *
 */
public class TenantsContainer extends CachingContainer {

  private static final long        serialVersionUID = 1945046643718969920L;

  protected TenantsContainerContext tenantsContainerContext;

  public TenantsContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent) 
  {
    super(componentAdapterFactory, parent);
  }

  public TenantsContainer(PicoContainer parent) 
  {
    super(parent);
  }

  public TenantsContainer(ComponentAdapterFactory componentAdapterFactory) 
  {
    super(componentAdapterFactory);
  }

  public TenantsContainer() 
  {
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  public ComponentAdapter getComponentAdapterOfType(Class componentType)
  {
    if (tenantsContainerContext != null && !componentType.equals(ExoContainerContext.class))
    {
      ComponentAdapter adapter = tenantsContainerContext.getComponentAdapterOfType(componentType);
      if (adapter != null) {
        return adapter;
      }
    }
    return super.getComponentAdapterOfType(componentType);
  }

  @Override
  public Object getComponentInstance(Object componentKey) throws PicoException 
  {
    if (tenantsContainerContext != null) 
    {
      Object comp = tenantsContainerContext.getComponentInstance(componentKey);
      if (comp != null) 
      {
        return comp;
      }
    }
    return super.getComponentInstance(componentKey);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List getComponentAdaptersOfType(Class componentType) {
    List result = new ArrayList();
    result.addAll(super.getComponentAdaptersOfType(componentType));

    if (tenantsContainerContext != null) 
    {
      List adapters = tenantsContainerContext.getComponentAdaptersOfType(componentType);
      if (adapters != null) 
      {
        result.addAll(adapters);
      }
    }
    return result;
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List getComponentInstancesOfType(Class componentType) throws PicoException 
  {
    // XXX: order of components in the list broken as we taking it from two sources
    List result = new ArrayList();
    result.addAll(super.getComponentInstancesOfType(componentType));

    if (tenantsContainerContext != null) 
    {
      List instances = tenantsContainerContext.getComponentInstancesOfType(componentType);
      if (instances != null) 
      {
        result.addAll(instances);
      }
    }
    return result;
  }

  @SuppressWarnings({ "rawtypes" })
  @Override
  public Object getComponentInstanceOfType(Class componentType) 
  {
    if (tenantsContainerContext != null && !componentType.equals(ExoContainerContext.class)) 
    {
      Object comp = tenantsContainerContext.getComponentInstanceOfType(componentType);
      if (comp != null) 
      { // TODO no need to check on null here, context already uses parent container
        return comp;
      }
    }
    return super.getComponentInstanceOfType(componentType);
  }
  
  @Override
  public ComponentAdapter registerComponent(ComponentAdapter componentAdapter) throws DuplicateComponentKeyRegistrationException
  {
//    Object componentKey = componentAdapter.getComponentKey();
//    if (componentKey instanceof Class && TenantsContainerContext.class.isAssignableFrom(((Class<?>)componentKey)))
//    {
//      if (componentAdapter instanceof InstanceComponentAdapter)
//        return super.registerComponent(componentAdapter);
//      else
//        return getComponentAdapterOfType(TenantsContainerContext.class);
//    }
    
    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentAdapter))
    {
      tenantsContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponent(componentAdapter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ComponentAdapter unregisterComponent(Object componentKey) {
    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentKey))
    {
      return tenantsContainerContext.unregisterComponent(componentKey);
    }
    
    return super.unregisterComponent(componentKey);
  }
  
}
