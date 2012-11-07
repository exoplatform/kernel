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

import org.exoplatform.container.tenant.TenantsContainerController;
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

public class TenancyContainer extends CachingContainer {

  protected TenantsContainerController tenantsContainerController;

  public TenancyContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
  {
    super(componentAdapterFactory, parent);
  }

  public TenancyContainer(PicoContainer parent)
  {
    super(parent);
  }

  public TenancyContainer(ComponentAdapterFactory componentAdapterFactory)
  {
    super(componentAdapterFactory);
  }

  public TenancyContainer()
  {
  }

  @Override
  public ComponentAdapter getComponentAdapterOfType(Class componentType)
  {
    if (tenantsContainerController != null)
    {
      ComponentAdapter tenancyAdapter = tenantsContainerController.getComponentAdapterOfType(componentType);
      if (tenancyAdapter != null)
        return tenancyAdapter;
    }
    return super.getComponentAdapterOfType(componentType);
  }

  @Override
  public Object getComponentInstance(Object componentKey) throws PicoException
  {
    if (tenantsContainerController != null)
    {
      Object tenancyAdapter = tenantsContainerController.getComponentInstance(componentKey);
      if (tenancyAdapter != null)
        return tenancyAdapter;
    }
    return super.getComponentInstance(componentKey);
  }


  @Override
  public List getComponentAdaptersOfType(Class componentType)
  {
    List result = new ArrayList();
    if (tenantsContainerController != null)
    {
      List adapters = tenantsContainerController.getComponentAdaptersOfType(componentType);
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
    if (tenantsContainerController != null)
    {
      List instances = tenantsContainerController.getComponentInstancesOfType(componentType);
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
    if (tenantsContainerController != null && !componentType.equals(ExoContainerContext.class))
    {
      Object tenancyAdapter = tenantsContainerController.getComponentInstanceOfType(componentType);
      if (tenancyAdapter != null)
        return tenancyAdapter;
    }
    return super.getComponentInstanceOfType(componentType);
  }

  @Override
  public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
    throws DuplicateComponentKeyRegistrationException
  {
    if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentAdapter.getComponentKey()))
    {
      tenantsContainerController.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponent(componentAdapter);
  }

  @Override
  public ComponentAdapter registerComponentInstance(Object component) throws PicoRegistrationException
  {
    if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(component.getClass()))
    {
      ComponentAdapter componentAdapter = new InstanceComponentAdapter(component.getClass(), component);
      tenantsContainerController.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentInstance(component);
  }

  @Override
  public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
    throws PicoRegistrationException
  {
    if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
    {
      ComponentAdapter componentAdapter = new InstanceComponentAdapter(componentKey, componentInstance);
      tenantsContainerController.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentInstance(componentKey, componentInstance);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Class componentImplementation)
    throws PicoRegistrationException
  {
    if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentImplementation))
    {
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentImplementation, componentImplementation, null);
      tenantsContainerController.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentImplementation);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation)
    throws PicoRegistrationException
  {
    if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
    {
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, null);
      tenantsContainerController.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentKey, componentImplementation);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
                                                          Parameter[] parameters) throws PicoRegistrationException
  {
    if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
    {
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, parameters);
      tenantsContainerController.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentKey, componentImplementation, parameters);
  }

  @Override
  public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
                                                          List parameters) throws PicoRegistrationException
  {
    if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
    {
      Parameter[] parametersAsArray = (Parameter[])parameters.toArray(new Parameter[parameters.size()]);
      ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, parametersAsArray);
      tenantsContainerController.registerComponent(componentAdapter);
      return componentAdapter;
    }
    return super.registerComponentImplementation(componentKey, componentImplementation, parameters);
  }
}
