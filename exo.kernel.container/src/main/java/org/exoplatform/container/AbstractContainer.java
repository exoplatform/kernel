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
package org.exoplatform.container;

import org.exoplatform.container.multitenancy.bridge.TenantsContainerContext;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.ContainerVisitor;
import org.exoplatform.container.spi.Interceptor;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.ManagementContext;

import java.util.Collection;
import java.util.List;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * This class is the root class of all the implementations of a {@link Container}.
 * It implements by default all the methods by delegating the call the the successor.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractContainer implements Container
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -426600082255394067L;

   protected Interceptor delegate;

   protected TenantsContainerContext tenantsContainerContext;

   protected AbstractContainer()
   {
   }

   protected AbstractContainer(Interceptor successor)
   {
      this.delegate = successor;
   }

   /**
    * {@inheritDoc}
    */
   public void start()
   {
      delegate.start();
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
      delegate.stop();
   }

   /**
    * {@inheritDoc}
    */
   public void dispose()
   {
      delegate.dispose();
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getComponentInstance(Object componentKey, Class<T> bindType, boolean autoRegistration)
   {
      return delegate.getComponentInstance(componentKey, bindType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   public <T> T getComponentInstanceOfType(Class<T> componentType, boolean autoRegistration)
   {
      return delegate.getComponentInstanceOfType(componentType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   public Interceptor getSuccessor()
   {
      return delegate;
   }

   /**
    * {@inheritDoc}
    */
   public <T> ComponentAdapter<T> getComponentAdapter(Object componentKey, Class<T> bindType, boolean autoRegistration)
   {
      return delegate.getComponentAdapter(componentKey, bindType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType, boolean autoRegistration)
   {
      return delegate.getComponentAdapterOfType(componentType, autoRegistration);
   }

   /**
    * {@inheritDoc}
    */
   public Collection<ComponentAdapter<?>> getComponentAdapters()
   {
      return delegate.getComponentAdapters();
   }

   /**
    * {@inheritDoc}
    */
   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      return delegate.getComponentAdaptersOfType(componentType);
   }

   /**
    * {@inheritDoc}
    */
   public <T> List<T> getComponentInstancesOfType(Class<T> componentType) throws ContainerException
   {
      return delegate.getComponentInstancesOfType(componentType);
   }

   /**
    * {@inheritDoc}
    */
   public void accept(ContainerVisitor visitor)
   {
      delegate.accept(visitor);
   }

   /**
    * {@inheritDoc}
    */
   public <T> ComponentAdapter<T> registerComponentImplementation(Object componentKey, Class<T> componentImplementation)
      throws ContainerException
   {
      return delegate.registerComponentImplementation(componentKey, componentImplementation);
   }

   /**
    * {@inheritDoc}
    */
   public <T> ComponentAdapter<T> registerComponentInstance(Object componentKey, T componentInstance)
      throws ContainerException
   {
      return delegate.registerComponentInstance(componentKey, componentInstance);
   }

   /**
    * {@inheritDoc}
    */
   public ComponentAdapter<?> unregisterComponent(Object componentKey)
   {
      return delegate.unregisterComponent(componentKey);
   }

   /**
    * {@inheritDoc}
    */
   public ManagementContext getManagementContext()
   {
      return delegate.getManagementContext();
   }

   /**
    * {@inheritDoc}
    */
   public MBeanServer getMBeanServer()
   {
      return delegate.getMBeanServer();
   }

   /**
    * {@inheritDoc}
    */
   public ObjectName getScopingObjectName()
   {
      return delegate.getScopingObjectName();
   }

   /**
    * {@inheritDoc}
    */
   public <T> T createComponent(Class<T> clazz, InitParams params) throws Exception
   {
      return delegate.createComponent(clazz, params);
   }

   /**
    * {@inheritDoc}
    */
   public void initialize()
   {
      delegate.initialize();
   }
}