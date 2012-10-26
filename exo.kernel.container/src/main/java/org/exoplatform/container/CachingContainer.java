/*
 * Copyright (C) 2009 eXo Platform SAS.
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

import org.exoplatform.container.mc.MCIntegrationContainer;
import org.exoplatform.container.tenant.TenantsContainerController;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import org.picocontainer.defaults.InstanceComponentAdapter;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@SuppressWarnings("unchecked")
public class CachingContainer extends MCIntegrationContainer
{

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = 316388590860241305L;

   private final ConcurrentMap<Class, ComponentAdapter> adapterByType =
      new ConcurrentHashMap<Class, ComponentAdapter>();

   private final ConcurrentMap<Class, Object> instanceByType = new ConcurrentHashMap<Class, Object>();

   private final ConcurrentMap<Object, Object> instanceByKey = new ConcurrentHashMap<Object, Object>();

   private final ConcurrentMap<Class, List> adaptersByType = new ConcurrentHashMap<Class, List>();

   private final ConcurrentMap<Class, List> instancesByType = new ConcurrentHashMap<Class, List>();

   protected TenantsContainerController tenantsContainerController;
   
   public CachingContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
   {
      super(componentAdapterFactory, parent);
   }

   public CachingContainer(PicoContainer parent)
   {
      super(parent);
   }

   public CachingContainer(ComponentAdapterFactory componentAdapterFactory)
   {
      super(componentAdapterFactory);
   }

   public CachingContainer()
   {
   }

   public ComponentAdapter getComponentAdapterOfType(Class componentType)
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentType))
      {
         ComponentAdapter tenancyAdapter = tenantsContainerController.getComponentOfType(componentType);
         if (tenancyAdapter != null)
            return tenancyAdapter;
      }
      ComponentAdapter adapter = adapterByType.get(componentType);
      if (adapter == null)
      {
         adapter = super.getComponentAdapterOfType(componentType);
         if (adapter != null)
         {
            adapterByType.put(componentType, adapter);
         }
      }
      return adapter;
   }

   public List getComponentAdaptersOfType(Class componentType)
   {
      List adapters = adaptersByType.get(componentType);
      if (adapters == null)
      {
         adapters = super.getComponentAdaptersOfType(componentType);
         if (adapters != null)
         {
            adaptersByType.put(componentType, adapters);
         }
      }
      return adapters;
   }

   public List getComponentInstancesOfType(Class componentType) throws PicoException
   {
      List instances = instancesByType.get(componentType);
      if (instances == null)
      {
         instances = super.getComponentInstancesOfType(componentType);
         if (instances != null)
         {
            instancesByType.put(componentType, instances);
         }
      }
      return instances;
   }

   public Object getComponentInstance(Object componentKey) throws PicoException
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
      {
         ComponentAdapter tenancyAdapter = tenantsContainerController.getComponentOfType(componentKey);
         if (tenancyAdapter != null)
            return tenancyAdapter;
      }
      Object instance = instanceByKey.get(componentKey);
      if (instance == null)
      {
         instance = super.getComponentInstance(componentKey);
         if (instance != null)
         {
            instanceByKey.put(componentKey, instance);
         }
      }
      return instance;
   }

   public Object getComponentInstanceOfType(Class componentType)
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentType))
      {
         ComponentAdapter tenancyAdapter = tenantsContainerController.getComponentOfType(componentType);
         if (tenancyAdapter != null)
            return tenancyAdapter;
      }
      Object instance = instanceByType.get(componentType);
      if (instance == null)
      {
         instance = super.getComponentInstanceOfType(componentType);
         if (instance != null)
         {
            instanceByType.put(componentType, instance);
         }
      }
      return instance;
   }

   private static final PicoVisitor invalidator = new ContainerVisitor()
   {
      public void visitContainer(PicoContainer pico)
      {
         if (pico instanceof CachingContainer)
         {
            CachingContainer caching = (CachingContainer)pico;
            caching.adapterByType.clear();
            caching.adaptersByType.clear();
            caching.instanceByKey.clear();
            caching.instanceByType.clear();
            caching.instancesByType.clear();
         }
      }
   };

   private void invalidate()
   {
      accept(invalidator);
   }

   //

   public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
      throws DuplicateComponentKeyRegistrationException
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentAdapter.getComponentKey()))
      {
        tenantsContainerController.registerComponent(componentAdapter);
        return componentAdapter;
      }
      ComponentAdapter adapter = super.registerComponent(componentAdapter);
      invalidate();
      return adapter;
   }

   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      ComponentAdapter adapter = super.unregisterComponent(componentKey);
      invalidate();
      return adapter;
   }

   public ComponentAdapter registerComponentInstance(Object component) throws PicoRegistrationException
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(component.getClass()))
      {
        ComponentAdapter componentAdapter = new InstanceComponentAdapter(component.getClass(), component);
        tenantsContainerController.registerComponent(componentAdapter);
        return componentAdapter;
      }
      ComponentAdapter adapter = super.registerComponentInstance(component);
      invalidate();
      return adapter;
   }

   public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
      throws PicoRegistrationException
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
      {
        ComponentAdapter componentAdapter = new InstanceComponentAdapter(componentKey, componentInstance);
        tenantsContainerController.registerComponent(componentAdapter);
        return componentAdapter;
      }
      ComponentAdapter adapter = super.registerComponentInstance(componentKey, componentInstance);
      invalidate();
      return adapter;
   }

   public ComponentAdapter registerComponentImplementation(Class componentImplementation)
      throws PicoRegistrationException
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentImplementation))
      {
        ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentImplementation, componentImplementation, null);
        tenantsContainerController.registerComponent(componentAdapter);
        return componentAdapter;
      }
      ComponentAdapter adapter = super.registerComponentImplementation(componentImplementation);
      invalidate();
      return adapter;
   }

   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation)
      throws PicoRegistrationException
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
      {
        ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, null);
        tenantsContainerController.registerComponent(componentAdapter);
        return componentAdapter;
      }
      ComponentAdapter adapter = super.registerComponentImplementation(componentKey, componentImplementation);
      invalidate();
      return adapter;
   }

   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
      Parameter[] parameters) throws PicoRegistrationException
   {
      if (tenantsContainerController != null && tenantsContainerController.isNeedRegister(componentKey))
      {
        ComponentAdapter componentAdapter = componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, parameters);
        tenantsContainerController.registerComponent(componentAdapter);
        return componentAdapter;
      }
      ComponentAdapter adapter = super.registerComponentImplementation(componentKey, componentImplementation, parameters);
      invalidate();
      return adapter;
   }

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
      ComponentAdapter adapter = super.registerComponentImplementation(componentKey, componentImplementation, parameters);
      invalidate();
      return adapter;
   }

}
