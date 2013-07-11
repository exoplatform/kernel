/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container.spi;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.ManagementContext;
import org.picocontainer.Disposable;
import org.picocontainer.Startable;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * This interface defines all the methods that we need to implement in order to allow eXo Kernel
 * to delegate the whole life cycle of the components. An implementation needs to be able to:
 * <ul>
 * <li>Register a new component</li>
 * <li>Manage the creation of the components</li>
 * <li>Manage the initialization of the components</li>
 * <li>Manage the startup of the components</li>
 * <li>Manage the shutdown of the components</li>
 * <li>Manage the destruction of the components</li>
 * <li>Provide the components matching with some specific criteria</li>
 * </ul>
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface Container extends Startable, Disposable, Serializable
{

   /**
    * Retrieve a component instance registered with a specific key. If a component cannot be found in this container,
    * the parent container (if one exists) will be searched.
    * 
    * @param componentKey the key that the component was registered with.
    * @param bindType the expected type of the instance if one can be found.
    * @return an instantiated component, or <code>null</code> if no component has been registered for the specified
    *         key.
    */
   <T> T getComponentInstance(Object componentKey, Class<T> bindType);

   /**
    * Find a component instance matching the specified type.
    * 
    * @param componentType the type of the component.
    * @return the adapter matching the class.
    */
   <T> T getComponentInstanceOfType(Class<T> componentType);

   /**
    * Retrieve the successor of this container in the chain of {@link Interceptor}.
    * 
    * @return a {@link Interceptor} instance, or <code>null</code> if this container does not have a successor.
    */
   Interceptor getSuccessor();

   /**
    * Find a component adapter associated with the specified key. If a component adapter cannot be found in this
    * container, the parent container (if one exists) will be searched.
    * 
    * @param componentKey the key that the component was registered with.
    * @param bindType the expected raw type of the adapter if one can be found.
    * @return the component adapter associated with this key, or <code>null</code> if no component has been registered
    *         for the specified key.
    */
   <T> ComponentAdapter<T> getComponentAdapter(Object componentKey, Class<T> bindType);

   /**
    * Find a component adapter associated with the specified type. If a component adapter cannot be found in this
    * container, the parent container (if one exists) will be searched.
    * 
    * @param componentType the type of the component.
    * @return the component adapter associated with this class, or <code>null</code> if no component has been
    *         registered for the specified key.
    */
   <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType);

   /**
    * Retrieve all the component adapters inside this container. The component adapters from the parent container are
    * not returned.
    *
    * @return a collection containing all the {@link ComponentAdapter}s inside this container. The collection will
    *         not be modifiable.
    * @see #getComponentAdaptersOfType(Class) a variant of this method which returns the component adapters inside this
    *      container that are associated with the specified type.
    */
   Collection<ComponentAdapter<?>> getComponentAdapters();

   /**
    * Retrieve all component adapters inside this container that are associated with the specified type. The component
    * adapters from the parent container are not returned.
    * 
    * @param componentType the type of the components.
    * @return a collection containing all the {@link ComponentAdapter}s inside this container that are associated with
    *         the specified type. Changes to this collection will not be reflected in the container itself.
    */
   <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType);

   /**
    * Returns a List of components of a certain componentType. The list is ordered by instantiation order,
    * starting with the components instantiated first at the beginning.
    * @param componentType the searched type.
    * @return a List of components.
    */
   <T> List<T> getComponentInstancesOfType(Class<T> componentType) throws ContainerException;

   /**
    * Accepts a visitor that should visit the child containers, component adapters and component instances.
    * @param visitor the visitor
    */
   void accept(ContainerVisitor visitor);

   /**
    * Register a component.
    *
    * @param componentKey            a key that identifies the component. Must be unique within the container. The type
    *                                of the key object has no semantic significance unless explicitly specified in the
    *                                documentation of the implementing container.
    * @param componentImplementation the component's implementation class. This must be a concrete class (ie, a
    *                                class that can be instantiated).
    * @return the ComponentAdapter that has been associated with this component. In the majority of cases, this return
    *         value can be safely ignored, as one of the <code>getXXX()</code> methods of the
    *         {@link Container} interface can be used to retrieve a reference to the component later on.
    * @throws ContainerExceptio if registration of the component fails.
    */
   <T> ComponentAdapter<T> registerComponentImplementation(Object componentKey, Class<T> componentImplementation)
      throws ContainerException;

   /**
    * Register an arbitrary object as a component in the container. This is handy when other components in the same
    * container have dependencies on this kind of object, but where letting the container manage and instantiate it is
    * impossible.
    * <p/>
    * Beware that too much use of this method is an <a href="http://docs.codehaus.org/display/PICO/Instance+Registration">antipattern</a>.
    *
    * @param componentKey      a key that identifies the component. Must be unique within the container. The type of the
    *                          key object has no semantic significance unless explicitly specified in the implementing
    *                          container.
    * @param componentInstance an arbitrary object.
    * @return the ComponentAdapter that has been associated with this component. In the majority of cases, this return
    *         value can be safely ignored, as one of the <code>getXXX()</code> methods of the
    *         {@link Container} interface can be used to retrieve a reference to the component later on.
    * @throws ContainerException if registration fails.
    */
   <T> ComponentAdapter<T> registerComponentInstance(Object componentKey, T componentInstance)
      throws ContainerException;

   /**
    * Unregister a component by key.
    * 
    * @param componentKey key of the component to unregister.
    * @return the ComponentAdapter that was associated with this component.
    */
   ComponentAdapter<?> unregisterComponent(Object componentKey);

   /**
    * Gives the corresponding {@link ManagementContext}
    */
   ManagementContext getManagementContext();

   /**
    * Provides the {@link MBeanServer} this method is needed for backward compatibility
    */
   MBeanServer getMBeanServer();

   /**
    * Gives the ObjectName of the container build from the scoping data
    */
   ObjectName getScopingObjectName();

   /**
    * Creates a component corresponding to the given {@link Class} with the
    * given {@link InitParams}
    * @param clazz the Class of the object to create
    * @param params the parameters to use to create the component
    * @return an instance of the component
    * @throws Exception if any issue occurs while creating the component.
    */
   <T> T createComponent(Class<T> clazz, InitParams params) throws Exception;

   /**
    * Initializes the container
    */
   void initialize();
}
