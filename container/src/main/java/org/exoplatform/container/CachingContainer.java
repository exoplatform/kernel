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

import org.picocontainer.ComponentAdapter;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.defaults.AbstractPicoVisitor;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class CachingContainer extends DefaultPicoContainer
{

   private final ConcurrentMap<Class, ComponentAdapter> adapterByType =
      new ConcurrentHashMap<Class, ComponentAdapter>();

   private final ConcurrentMap<Class, Object> instanceByType = new ConcurrentHashMap<Class, Object>();

   private final ConcurrentMap<Object, Object> instanceByKey = new ConcurrentHashMap<Object, Object>();

   private final ConcurrentMap<Class, List> adaptersByType = new ConcurrentHashMap<Class, List>();

   private final ConcurrentMap<Class, List> instancesByType = new ConcurrentHashMap<Class, List>();

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

   private static final PicoVisitor invalidator = new AbstractPicoVisitor()
   {
      public void visitContainer(PicoContainer pico)
      {
         if (pico instanceof CachingContainer)
         {
            CachingContainer caching = (CachingContainer)pico;
            caching.adapterByType.clear();
            caching.adaptersByType.clear();
            caching.instanceByKey.clear();
            caching.adaptersByType.clear();
            caching.instancesByType.clear();
         }
      }

      public void visitComponentAdapter(ComponentAdapter componentAdapter)
      {
      }

      public void visitParameter(Parameter parameter)
      {
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
      invalidate();
      return super.registerComponent(componentAdapter);
   }

   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      invalidate();
      return super.unregisterComponent(componentKey);
   }

   public ComponentAdapter registerComponentInstance(Object component) throws PicoRegistrationException
   {
      invalidate();
      return super.registerComponentInstance(component);
   }

   public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
      throws PicoRegistrationException
   {
      invalidate();
      return super.registerComponentInstance(componentKey, componentInstance);
   }

   public ComponentAdapter registerComponentImplementation(Class componentImplementation)
      throws PicoRegistrationException
   {
      invalidate();
      return super.registerComponentImplementation(componentImplementation);
   }

   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation)
      throws PicoRegistrationException
   {
      invalidate();
      return super.registerComponentImplementation(componentKey, componentImplementation);
   }

   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
      Parameter[] parameters) throws PicoRegistrationException
   {
      invalidate();
      return super.registerComponentImplementation(componentKey, componentImplementation, parameters);
   }

   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
      List parameters) throws PicoRegistrationException
   {
      invalidate();
      return super.registerComponentImplementation(componentKey, componentImplementation, parameters);
   }

   public boolean addChildContainer(PicoContainer child)
   {
      invalidate();
      return super.addChildContainer(child);
   }

   public boolean removeChildContainer(PicoContainer child)
   {
      invalidate();
      return super.removeChildContainer(child);
   }
}
