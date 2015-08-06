/*
 * Copyright (C) 2015 eXo Platform SAS.
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


import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:foo@bar.org">Foo Bar</a>
 * @version $Id: Body Header.java 34027 2009-07-15 23:26:43Z aheritier $
 */
public class TenantsContainer extends AbstractInterceptor
{
   private static final long serialVersionUID = 1945046643718969920L;

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType, boolean autoRegistration)
   {
      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentType))
      {
         return holder.tenantsContainerContext.getComponentAdapterOfType(componentType);
      }
      return delegate.getComponentAdapterOfType(componentType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getComponentInstance(Object componentKey, Class<T> bindType, boolean autoRegistration)
   {
      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentKey))
      {
         return holder.tenantsContainerContext.getComponentInstance(componentKey);
      }
      return delegate.getComponentInstance(componentKey, bindType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   public <T> ComponentAdapter<T> getComponentAdapter(Object componentKey, Class<T> bindType, boolean autoRegistration)
   {
      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentKey))
      {
         return holder.tenantsContainerContext.getComponentAdapterOfType(componentKey.getClass());
      }
      return delegate.getComponentAdapter(componentKey, bindType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      List<ComponentAdapter<T>> result = null;

      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentType))
      {
         List<ComponentAdapter<T>> adapters = holder.tenantsContainerContext.getComponentAdaptersOfType(componentType);
         if (adapters != null && !adapters.isEmpty())
         {
            result = adapters;
         }
      }
      if (result == null)
      {
         return delegate.getComponentAdaptersOfType(componentType);
      }
      else
      {
         List<ComponentAdapter<T>> adapters = delegate.getComponentAdaptersOfType(componentType);
         if (adapters == null || adapters.isEmpty())
         {
            return result;
         }
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
   @Override
   public <T> List<T> getComponentInstancesOfType(Class<T> componentType) throws ContainerException
   {
      List<T> result = null;

      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentType))
      {
         List<T> instances = holder.tenantsContainerContext.getComponentInstancesOfType(componentType);
         if (instances != null && !instances.isEmpty())
         {
            result = instances;
         }
      }
      if (result == null)
      {
         return delegate.getComponentInstancesOfType(componentType);
      }
      else
      {
         List instances = delegate.getComponentInstancesOfType(componentType);
         if (instances == null || instances.isEmpty())
         {
            return result;
         }
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
   @Override
   public <T> T getComponentInstanceOfType(Class<T> componentType, boolean autoRegistration)
   {
      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentType))
      {
         return holder.tenantsContainerContext.getComponentInstanceOfType(componentType);
      }
      return delegate.getComponentInstanceOfType(componentType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   public <T> ComponentAdapter<T> registerComponentImplementation(Object componentKey, Class<T> componentImplementation)
      throws ContainerException
   {

      ComponentAdapter<T> componentAdapter = delegate.registerComponentImplementation(componentKey, componentImplementation);

      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentAdapter))
      {
         ComponentAdapter<T> contextAdapter = holder.tenantsContainerContext.registerComponent(componentAdapter);
         // check if the same adapter returned, if not - register the new in the super also
         if (contextAdapter == componentAdapter)
         {
            return componentAdapter;
         }
         else
         {
            return (ComponentAdapter<T>)delegate.registerComponentImplementation(contextAdapter.getComponentKey(), contextAdapter.getComponentImplementation());
         }
      }
      else
      {
         return componentAdapter;
      }
   }

   /**
    * {@inheritDoc}
    */
   public <T> ComponentAdapter<T> registerComponentInstance(Object componentKey, T componentInstance)
      throws ContainerException
   {
      ComponentAdapter<T> componentAdapter = delegate.registerComponentInstance(componentKey, componentInstance);

      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentAdapter))
      {
         ComponentAdapter<T> contextAdapter = holder.tenantsContainerContext.registerComponent(componentAdapter);
         // check if the same adapter returned, if not - register the new in the super also
         if (contextAdapter == componentAdapter)
         {
            return componentAdapter;
         }
         else
         {
            return delegate.registerComponentInstance(contextAdapter.getComponentKey(), contextAdapter.getComponentInstance());
         }
      }
      else
      {
         return componentAdapter;
      }
   }

   /**
    * {@inheritDoc}
    */
   public ComponentAdapter<?> unregisterComponent(Object componentKey)
   {
      if (holder.tenantsContainerContext != null && holder.tenantsContainerContext.accept(componentKey))
      {
         ComponentAdapter adapter = holder.tenantsContainerContext.unregisterComponent(componentKey);
         if (adapter != null)
         {
            return adapter;
         }
      }
      return delegate.unregisterComponent(componentKey);
   }
}
