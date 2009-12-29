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
package org.exoplatform.container.jmx;

import org.exoplatform.container.CachingContainer;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.management.spi.jmx.JMXManagementProvider;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.MBeanServer;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManageableContainer extends CachingContainer
{

   private static MBeanServer findMBeanServer()
   {
      J2EEServerInfo serverenv_ = new J2EEServerInfo();
      MBeanServer server = serverenv_.getMBeanServer();
      if (server == null)
      {
         server = ManagementFactory.getPlatformMBeanServer();
      }
      return server;
   }

   /** . */
   private static final ThreadLocal<ManageableComponentAdapterFactory> hack =
      new ThreadLocal<ManageableComponentAdapterFactory>();

   /** . */
   protected ManagementContextImpl managementContext;

   /** . */
   protected MBeanServer server;

   public ManageableContainer()
   {
      this((PicoContainer)null);
   }

   public ManageableContainer(PicoContainer parent)
   {
      this(new MX4JComponentAdapterFactory(), parent);
   }

   public ManageableContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
   {
      super(getComponentAdapterFactory(componentAdapterFactory), parent);

      // Yeah this is not pretty but a necessary evil to make it work
      ManageableComponentAdapterFactory factory = hack.get();
      factory.container = this;
      hack.set(null);

      // Reference the same mbean server that the parent has
      if (parent instanceof ManageableContainer)
      {
         ManageableContainer manageableParent = (ManageableContainer)parent;

         //
         ManagementContextImpl parentManagementContext = manageableParent.managementContext;
         if (parentManagementContext != null)
         {
            managementContext = new ManagementContextImpl(parentManagementContext);
         }

         // Get server from parent
         server = manageableParent.server;
      } else {
         KernelManagementContext kernelCtx = new KernelManagementContext();

         //
         server = findMBeanServer();
         managementContext = kernelCtx.root;

         //
         kernelCtx.addProvider(new JMXManagementProvider(server));
      }

      //
      this.managementContext.container  = this;
   }

   public ManageableContainer(ComponentAdapterFactory componentAdapterFactory)
   {
      this(componentAdapterFactory, null);
   }

   @Managed
   @ManagedName("RegisteredComponentNames")
   @ManagedDescription("Return the list of the registered component names")
   public Set<String> getRegisteredComponentNames() throws PicoException
   {
      Set<String> names = new HashSet<String>();
      Collection<ComponentAdapter> adapters = getComponentAdapters();
      for (ComponentAdapter adapter : adapters)
      {
         Object key = adapter.getComponentKey();
         String name = String.valueOf(key);
         names.add(name);
      }
      return names;
   }

   private static ManageableComponentAdapterFactory getComponentAdapterFactory(
      ComponentAdapterFactory componentAdapterFactory)
   {
      ManageableComponentAdapterFactory factory = new ManageableComponentAdapterFactory(componentAdapterFactory);
      hack.set(factory);
      return factory;
   }

   public ManagementContext getManagementContext()
   {
      return managementContext;
   }

   public final MBeanServer getMBeanServer()
   {
      return server;
   }

   @Override
   public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
      throws DuplicateComponentKeyRegistrationException
   {
      return super.registerComponent(componentAdapter);
   }

   public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
      throws PicoRegistrationException
   {
      ComponentAdapter adapter = super.registerComponentInstance(componentKey, componentInstance);
      if (managementContext != null)
      {
         managementContext.register(componentInstance);

         // Register if it is a management provider
         if (componentInstance instanceof ManagementProvider)
         {
            ManagementProvider provider = (ManagementProvider)componentInstance;
            managementContext.kernelContext.addProvider(provider);
         }
      }
      return adapter;
   }
}
