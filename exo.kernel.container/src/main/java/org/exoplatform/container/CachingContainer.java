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

import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.ContainerVisitor;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CachingContainer extends AbstractInterceptor
{

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = 316388590860241305L;

   private final ConcurrentMap<Class<?>, ComponentAdapter> adapterByType =
      new ConcurrentHashMap<Class<?>, ComponentAdapter>();

   private final ConcurrentMap<Class<?>, Object> instanceByType = new ConcurrentHashMap<Class<?>, Object>();

   private final ConcurrentMap<Object, Object> instanceByKey = new ConcurrentHashMap<Object, Object>();

   private final ConcurrentMap<Class<?>, List<ComponentAdapter>> adaptersByType =
      new ConcurrentHashMap<Class<?>, List<ComponentAdapter>>();

   private final ConcurrentMap<Class<?>, List<?>> instancesByType = new ConcurrentHashMap<Class<?>, List<?>>();

   public ComponentAdapter getComponentAdapterOfType(Class<?> componentType)
   {
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

   public List<ComponentAdapter> getComponentAdaptersOfType(Class<?> componentType)
   {
      List<ComponentAdapter> adapters = adaptersByType.get(componentType);
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

   @SuppressWarnings("unchecked")
   public <T> List<T> getComponentInstancesOfType(Class<T> componentType) throws ContainerException
   {
      List<?> instances = instancesByType.get(componentType);
      if (instances == null)
      {
         instances = super.getComponentInstancesOfType(componentType);
         if (instances != null)
         {
            instancesByType.put(componentType, instances);
         }
      }
      return (List<T>)instances;
   }

   public Object getComponentInstance(Object componentKey) throws ContainerException
   {
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

   public <T> T getComponentInstanceOfType(Class<T> componentType)
   {
      Object instance = instanceByType.get(componentType);
      if (instance == null)
      {
         instance = super.getComponentInstanceOfType(componentType);
         if (instance != null)
         {
            instanceByType.put(componentType, instance);
         }
      }
      return componentType.cast(instance);
   }

   private static final ContainerVisitor invalidator = new ContainerVisitor()
   {
      public void visitContainer(Container container)
      {
         do
         {
            if (container instanceof CachingContainer)
            {
               CachingContainer caching = (CachingContainer)container;
               caching.adapterByType.clear();
               caching.adaptersByType.clear();
               caching.instanceByKey.clear();
               caching.instanceByType.clear();
               caching.instancesByType.clear();
               break;
            }
         }
         while ((container = container.getSuccessor()) != null);
      }
   };

   private void invalidate()
   {
      accept(invalidator);
   }

   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      ComponentAdapter adapter = super.unregisterComponent(componentKey);
      invalidate();
      return adapter;
   }

   public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
      throws ContainerException
   {
      ComponentAdapter adapter = super.registerComponentInstance(componentKey, componentInstance);
      invalidate();
      return adapter;
   }

   public ComponentAdapter registerComponentImplementation(Object componentKey, Class<?> componentImplementation)
      throws ContainerException
   {
      ComponentAdapter adapter = super.registerComponentImplementation(componentKey, componentImplementation);
      invalidate();
      return adapter;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return "Cache";
   }
}
