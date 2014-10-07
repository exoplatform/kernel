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

import org.exoplatform.container.multitenancy.bridge.TenantsContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * TenantsContainer separate generally used components from ones what should be instantiated,
 * started and stopped on per-tenant basis.<br>
 * It overrides component getters, {@link #registerComponent(ComponentAdapter)} and
 * {@link #unregisterComponent(Object)} methods to get components taking in account Current Tenant
 * context.<br>
 * The Current Tenant context it's an abstraction what will be set by actual Multitenancy implementation (for
 * versions currently in production it's based on JCR Current Repository, but this should be transparent for
 * Kernel level and implementation can be changed in future).
 * 
 * 
 */
public class TenantsContainer extends CachingContainer
{

   private static final long serialVersionUID = 1945046643718969920L;

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
   @SuppressWarnings({"rawtypes"})
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
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public List getComponentAdaptersOfType(Class componentType)
   {
      List result = null;

      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType))
      {
         List adapters = tenantsContainerContext.getComponentAdaptersOfType(componentType);
         if (adapters != null && !adapters.isEmpty())
         {
            result = adapters;
         }
      }
      if (result == null)
      {
         return super.getComponentAdaptersOfType(componentType);
      }
      else
      {
         List<ComponentAdapter> adapters = super.getComponentAdaptersOfType(componentType);
         if (adapters == null || adapters.isEmpty())
            return result;
         Map<Object, ComponentAdapter> mapAdapters = new LinkedHashMap<Object, ComponentAdapter>();
         // Put first the adapters found in the main container 
         for (int i = 0, length = adapters.size(); i < length; i++)
         {
            ComponentAdapter adapter = adapters.get(i);
            mapAdapters.put(adapter.getComponentKey(), adapter);
         }
         // Replace all adapters whose key has already been added to the map
         // to avoid duplicates and add undefined adapters
         for (int i = 0, length = result.size(); i < length; i++)
         {
            ComponentAdapter adapter = (ComponentAdapter)result.get(i);
            mapAdapters.put(adapter.getComponentKey(), adapter);
         }
         return new ArrayList(mapAdapters.values());
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   @Override
   public List getComponentInstancesOfType(Class componentType) throws PicoException
   {
      List result = null;

      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentType))
      {
         List instances = tenantsContainerContext.getComponentInstancesOfType(componentType);
         if (instances != null && !instances.isEmpty())
         {
            result = instances;
         }
      }
      if (result == null)
      {
         return super.getComponentInstancesOfType(componentType);
      }
      else
      {
         List instances = super.getComponentInstancesOfType(componentType);
         if (instances == null || instances.isEmpty())
            return result;
         Map<String, Object> mapInstances = new LinkedHashMap<String, Object>();
         // Put first the instances found in the main container 
         for (int i = 0, length = instances.size(); i < length; i++)
         {
            Object instance = instances.get(i);
            mapInstances.put(instance.getClass().getName(), instance);
         }
         // Replace all instances whose class name has already been added to the map
         // to avoid duplicates and add undefined instances
         for (int i = 0, length = result.size(); i < length; i++)
         {
            Object instance = result.get(i);
            mapInstances.put(instance.getClass().getName(), instance);
         }
         return new ArrayList(mapInstances.values());
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes"})
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
   public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
      throws DuplicateComponentKeyRegistrationException
   {
      if (tenantsContainerContext != null && tenantsContainerContext.accept(componentAdapter))
      {
         ComponentAdapter contextAdapter = tenantsContainerContext.registerComponent(componentAdapter);
         // check if the same adapter returned, if not - register the new in the super also 
         if (contextAdapter == componentAdapter)
         {
            return componentAdapter;
         }
         else
         {
            return super.registerComponent(contextAdapter);
         }
      }
      else
      {
         return super.registerComponent(componentAdapter);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ComponentAdapter unregisterComponent(Object componentKey)
   {
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
