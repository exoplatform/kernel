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

   private final ConcurrentMap<Class<?>, ComponentAdapter<?>> adapterByType =
      new ConcurrentHashMap<Class<?>, ComponentAdapter<?>>();

   private final ConcurrentMap<Class<?>, Object> instanceByType = new ConcurrentHashMap<Class<?>, Object>();

   private final ConcurrentMap<Object, Object> instanceByKey = new ConcurrentHashMap<Object, Object>();

   private final ConcurrentMap<Class<?>, Object> adaptersByType =
      new ConcurrentHashMap<Class<?>, Object>();

   private final ConcurrentMap<Class<?>, List<?>> instancesByType = new ConcurrentHashMap<Class<?>, List<?>>();

   private final ThreadLocal<Boolean> enabled = new ThreadLocal<Boolean>();

   @SuppressWarnings("unchecked")
   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType)
   {
      ComponentAdapter<T> adapter = (ComponentAdapter<T>)adapterByType.get(componentType);
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

   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      @SuppressWarnings("unchecked")
      List<ComponentAdapter<T>> adapters = (List<ComponentAdapter<T>>)adaptersByType.get(componentType);
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
            Boolean cacheEnabled = enabled.get();
            try
            {
               if (cacheEnabled == null || cacheEnabled.booleanValue())
               {
                  instancesByType.put(componentType, instances);
               }
            }
            finally
            {
               if (cacheEnabled != null)
                  enabled.remove();
            }
         }
      }
      return (List<T>)instances;
   }

   public <T> T getComponentInstance(Object componentKey, Class<T> bindType) throws ContainerException
   {
      Object instance = instanceByKey.get(componentKey);
      if (instance == null)
      {
         instance = super.getComponentInstance(componentKey, bindType);
         if (instance != null)
         {
            Boolean cacheEnabled = enabled.get();
            try
            {
               if (cacheEnabled == null || cacheEnabled.booleanValue())
               {
                  instanceByKey.put(componentKey, instance);
               }
            }
            finally
            {
               if (cacheEnabled != null)
                  enabled.remove();
            }
         }
      }
      return bindType.cast(instance);
   }

   public <T> T getComponentInstanceOfType(Class<T> componentType)
   {
      Object instance = instanceByType.get(componentType);
      if (instance == null)
      {
         instance = super.getComponentInstanceOfType(componentType);
         if (instance != null)
         {
            Boolean cacheEnabled = enabled.get();
            try
            {
               if (cacheEnabled == null || cacheEnabled.booleanValue())
               {
                  instanceByType.put(componentType, instance);
               }
            }
            finally
            {
               if (cacheEnabled != null)
                  enabled.remove();
            }
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

   public ComponentAdapter<?> unregisterComponent(Object componentKey)
   {
      ComponentAdapter<?> adapter = super.unregisterComponent(componentKey);
      invalidate();
      return adapter;
   }

   public <T> ComponentAdapter<T> registerComponentInstance(Object componentKey, T componentInstance)
      throws ContainerException
   {
      ComponentAdapter<T> adapter = super.registerComponentInstance(componentKey, componentInstance);
      invalidate();
      return adapter;
   }

   public <T> ComponentAdapter<T> registerComponentImplementation(Object componentKey, Class<T> componentImplementation)
      throws ContainerException
   {
      ComponentAdapter<T> adapter = super.registerComponentImplementation(componentKey, componentImplementation);
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

   void disable()
   {
      enabled.set(Boolean.FALSE);
   }
}
