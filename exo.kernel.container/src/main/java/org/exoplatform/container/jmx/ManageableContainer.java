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
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManageableContainer extends CachingContainer
{

   /** . */
   private static final ThreadLocal<ManageableComponentAdapterFactory> hack =
      new ThreadLocal<ManageableComponentAdapterFactory>();

   /** . */
   protected ManagementContextImpl managementContext;

   public ManageableContainer(ManagementContextImpl managementContext)
   {
      super(getComponentAdapterFactory(new MX4JComponentAdapterFactory()));
      this.managementContext = managementContext;
      managementContext.container = this;
      init(null);
   }

   public ManageableContainer(PicoContainer parent)
   {
      super(getComponentAdapterFactory(new MX4JComponentAdapterFactory()), parent);
      init(parent);
   }

   public ManageableContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
   {
      super(getComponentAdapterFactory(componentAdapterFactory), parent);
      init(parent);
   }

   public ManageableContainer(ComponentAdapterFactory componentAdapterFactory)
   {
      super(getComponentAdapterFactory(componentAdapterFactory));
      init(null);
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

   private void init(PicoContainer parent)
   {
      // Yeah this is not pretty but a necessary evil to make it work
      ManageableComponentAdapterFactory factory = hack.get();
      factory.container = this;
      hack.set(null);

      // Reference the same mbean server that the parent has
      if (parent instanceof ManageableContainer)
      {
         ManagementContextImpl parentManagementContext = ((ManageableContainer)parent).managementContext;
         if (parentManagementContext != null)
         {
            managementContext = new ManagementContextImpl(parentManagementContext, new HashMap<String, String>());
            managementContext.container  = this;
         }
      }
   }

   public ManagementContext getManagementContext()
   {
      return managementContext;
   }

   public final MBeanServer getMBeanServer()
   {
      return managementContext != null ? managementContext.server : null;
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
      }
      return adapter;
   }

   //

   public void printMBeanServer()
   {
      MBeanServer server = getMBeanServer();
      final Set names = server.queryNames(null, null);
      for (final Iterator i = names.iterator(); i.hasNext();)
      {
         ObjectName name = (ObjectName)i.next();
         try
         {
            MBeanInfo info = server.getMBeanInfo(name);
            MBeanAttributeInfo[] attrs = info.getAttributes();
            if (attrs == null)
               continue;
            for (int j = 0; j < attrs.length; j++)
            {
               if (attrs[j].isReadable())
               {
                  try
                  {
                     Object o = server.getAttribute(name, attrs[j].getName());
                  }
                  catch (Exception x)
                  {
                     x.printStackTrace();
                  }
               }
            }
            MBeanOperationInfo[] methods = info.getOperations();
            for (int j = 0; j < methods.length; j++)
            {
               MBeanParameterInfo[] params = methods[j].getSignature();
               for (int k = 0; k < params.length; k++)
               {
               }
            }
         }
         catch (Exception x)
         {
            // x.printStackTrace(System. err);
         }
      }
   }
}
