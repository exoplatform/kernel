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
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import org.picocontainer.defaults.InstanceComponentAdapter;
import java.util.ArrayList;
import java.util.List;

public class TenantContainer extends CachingContainer {

  protected TenantContainerContext tenantContainerContext;

  public TenantContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
  {
    super(componentAdapterFactory, parent);
  }

  public TenantContainer(PicoContainer parent)
  {
    super(parent);
  }

  public TenantContainer(ComponentAdapterFactory componentAdapterFactory)
  {
    super(componentAdapterFactory);
  }

  public TenantContainer()
  {
  }

  @Override
  public ComponentAdapter getComponentAdapterOfType(Class componentType)
  {
    if (tenantContainerContext != null)
    {
      ComponentAdapter tenancyAdapter = tenantContainerContext.getComponentAdapterOfType(componentType);
      if (tenancyAdapter != null)
        return tenancyAdapter;
    }
    return super.getComponentAdapterOfType(componentType);
  }

  @Override
  public Object getComponentInstance(Object componentKey) throws PicoException
  {
    if (tenantContainerContext != null)
    {
      Object tenancyAdapter = tenantContainerContext.getComponentInstance(componentKey);
      if (tenancyAdapter != null)
        return tenancyAdapter;
    }
    return super.getComponentInstance(componentKey);
  }


  @Override
  public List getComponentAdaptersOfType(Class componentType)
  {
    List result = new ArrayList();
    if (tenantContainerContext != null)
    {
      List adapters = tenantContainerContext.getComponentAdaptersOfType(componentType);
      if (adapters != null)
        result.addAll(adapters);
    }
    List adapters = super.getComponentAdaptersOfType(componentType);
    result.addAll(adapters);
    return result;
  }

  @Override
  public List getComponentInstancesOfType(Class componentType) throws PicoException
  {
    List result = new ArrayList();
    if (tenantContainerContext != null)
    {
      List instances = tenantContainerContext.getComponentInstancesOfType(componentType);
      if (instances != null)
        result.addAll(instances);
    }
    List instances = super.getComponentInstancesOfType(componentType);
    result.addAll(instances);
    return result;
  }

  @Override
  public Object getComponentInstanceOfType(Class componentType)
  {
    if (tenantContainerContext != null && !componentType.equals(ExoContainerContext.class))
    {
      Object tenancyAdapter = tenantContainerContext.getComponentInstanceOfType(componentType);
      if (tenancyAdapter != null)
        return tenancyAdapter;
    }
    return super.getComponentInstanceOfType(componentType);
  }

  @Override
  public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
    throws DuplicateComponentKeyRegistrationException
  {
    if (tenantContainerContext != null && tenantContainerContext.isNeedRegister(componentAdapter.getComponentKey()))
    {
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponent(componentAdapter);
  }

  @Override
  public ComponentAdapter registerComponentInstance(Object component) throws PicoRegistrationException
  {
    if (tenantContainerContext != null && tenantContainerContext.isNeedRegister(component.getClass()))
    {
      ComponentAdapter componentAdapter = new InstanceComponentAdapter(component.getClass(), component);
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentInstance(component);
  }

  @Override
  public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
    throws PicoRegistrationException
  {
    if (tenantContainerContext != null && tenantContainerContext.isNeedRegister(componentKey))
    {
      ComponentAdapter componentAdapter = new InstanceComponentAdapter(componentKey, componentInstance);
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentInstance(componentKey, componentInstance);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Class componentImplementation)
    throws PicoRegistrationException
  {
    if (tenantContainerContext != null && tenantContainerContext.isNeedRegister(componentImplementation))
    {
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentImplementation, componentImplementation, null);
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentImplementation);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation)
    throws PicoRegistrationException
  {
    if (tenantContainerContext != null && tenantContainerContext.isNeedRegister(componentKey))
    {
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, null);
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentKey, componentImplementation);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
                                                          Parameter[] parameters) throws PicoRegistrationException
  {
    if (tenantContainerContext != null && tenantContainerContext.isNeedRegister(componentKey))
    {
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, parameters);
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentKey, componentImplementation, parameters);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
                                                          List parameters) throws PicoRegistrationException
  {
    if (tenantContainerContext != null && tenantContainerContext.isNeedRegister(componentKey))
    {
      Parameter[] parametersAsArray = (Parameter[])parameters.toArray(new Parameter[parameters.size()]);
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, parametersAsArray);
      tenantContainerContext.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentKey, componentImplementation, parameters);
  }
}
