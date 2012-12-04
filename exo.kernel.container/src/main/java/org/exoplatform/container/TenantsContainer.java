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

import org.exoplatform.container.multitenancy.TenantsContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

/**
 * TenantsContainer's goal to separate generally used components from ones what should be instantiated, started 
 * and stopped on per-tenant basis.<br> 
 * It overrides component getters, {@link #registerComponent(ComponentAdapter)} and {@link #unregisterComponent(Object)} 
 * methods to be able to get components taking in account Current Tenant context.<br>
 * The Current Tenant context it's an abstraction what will be set by actual cloud implementation (for versions currently 
 * in production it's based on JCR Current Repository, but this should be transparent for Kernel level). 
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

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes" })
  @Override
  public ComponentAdapter getComponentAdapterOfType(Class componentType)
  {
    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType))
    {
      return tenantsContainerContext.getComponentAdapterOfType(componentType);
    }
    return super.getComponentAdapterOfType(componentType);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Object getComponentInstance(Object componentKey) throws PicoException 
  {
    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentKey)) 
    {
      return tenantsContainerContext.getComponentInstance(componentKey);
    }
    return super.getComponentInstance(componentKey);
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List getComponentAdaptersOfType(Class componentType) {
    List result = new ArrayList();
    result.addAll(super.getComponentAdaptersOfType(componentType));

    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType)) 
    {
      List adapters = tenantsContainerContext.getComponentAdaptersOfType(componentType);
      if (adapters != null) 
      {
        result.addAll(adapters);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  @Override
  public List getComponentInstancesOfType(Class componentType) throws PicoException 
  {
    // XXX: order of components in the list broken as we taking it from two sources
    List result = new ArrayList();
    result.addAll(super.getComponentInstancesOfType(componentType));

    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType)) 
    {
      List instances = tenantsContainerContext.getComponentInstancesOfType(componentType);
      if (instances != null) 
      {
        result.addAll(instances);
      }
    }
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @SuppressWarnings({ "rawtypes" })
  @Override
  public Object getComponentInstanceOfType(Class componentType) 
  {
    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType)) 
    {
      return tenantsContainerContext.getComponentInstanceOfType(componentType);
    }
    return super.getComponentInstanceOfType(componentType);
  }
  
  /**
   * {@inheritDoc}
   */
  @Override
  public ComponentAdapter registerComponent(ComponentAdapter componentAdapter) throws DuplicateComponentKeyRegistrationException
  {
    if (tenantsContainerContext != null && tenantsContainerContext.accept(componentAdapter))
    {
      if (tenantsContainerContext.registerComponent(componentAdapter)) 
      {
        return componentAdapter;
      }
    }
    return super.registerComponent(componentAdapter);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ComponentAdapter unregisterComponent(Object componentKey) {
    ComponentAdapter adapter = getComponentAdapter(componentKey);
    if (tenantsContainerContext != null && tenantsContainerContext.accept(adapter))
    {
      adapter = tenantsContainerContext.unregisterComponent(componentKey);
      if (adapter != null) 
      {
        return adapter;
      }
    }
    
    return super.unregisterComponent(componentKey);
  }
  
}
