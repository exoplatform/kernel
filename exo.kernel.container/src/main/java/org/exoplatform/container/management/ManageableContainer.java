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
package org.exoplatform.container.management;

import org.exoplatform.container.CachingContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterFactory;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.management.jmx.impl.JMX;
import org.exoplatform.management.jmx.impl.JMXManagementProvider;
import org.exoplatform.management.jmx.impl.MBeanScopingData;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.defaults.ComponentAdapterFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManageableContainer extends CachingContainer
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.container.management.ManageableContainer");

   private static MBeanServer findMBeanServer()
   {
      J2EEServerInfo serverenv_ = new J2EEServerInfo();
      return serverenv_.getMBeanServer();
   }

   /** . */
   private static final ThreadLocal<ManageableComponentAdapterFactory> hack =
      new ThreadLocal<ManageableComponentAdapterFactory>();

   /** . */
   final ManagementContextImpl managementContext;

   /** . */
   private MBeanServer server;

   private volatile boolean objectNameSet;

   private ObjectName objectName;

   /** . */
   private final Set<ManagementProvider> providers;

   /** . */
   private final ManageableContainer parent;

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

      // The synchronized wrapper, here will not impact runtime performances
      // so it's fine
      this.providers = Collections.synchronizedSet(new HashSet<ManagementProvider>());

      //
      ManagementContextImpl parentCtx = null;
      if (parent instanceof ManageableContainer)
      {
         ManageableContainer manageableParent = (ManageableContainer)parent;
         parentCtx = manageableParent.managementContext;
      }

      //
      this.parent = parent instanceof ManageableContainer ? (ManageableContainer)parent : null;

      //
      if (parentCtx != null)
      {
         server = parentCtx.container.server;
         managementContext = new ManagementContextImpl(parentCtx, this);
      }
      else
      {
         server = findMBeanServer();
         managementContext = new ManagementContextImpl(this);
         addProvider(new JMXManagementProvider(server));
      }
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

   /**
    * Gives the ObjectName of the container build from the scoping data
    * @return
    */
   public ObjectName getScopingObjectName()
   {
      if (!objectNameSet)
      {
         synchronized (this)
         {
            if (!objectNameSet)
            {
               Map<String, String> props = new LinkedHashMap<String, String>();

               // Merge scoping properties
               List<MBeanScopingData> list = managementContext.getScopingData(MBeanScopingData.class);
               if (list != null && !list.isEmpty())
               {
                  // Read in revert order because wee received list of parents in upward order
                  for (int i = list.size(); i > 0; i--)
                  {
                     MBeanScopingData scopingData = list.get(i - 1);
                     props.putAll(scopingData);
                  }
                  try
                  {
                     this.objectName = JMX.createObjectName("exo", props);
                  }
                  catch (Exception e)
                  {
                     LOG.error("Could not create the object name", e);
                  }
               }
               this.objectNameSet = true;
            }
         }
      }
      return objectName;
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
            addProvider(provider);
         }
      }
      return adapter;
   }

   @Override
   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      ComponentAdapter adapter = getComponentAdapter(componentKey);

      if (managementContext != null && adapter != null)
      {
         managementContext.unregister(adapter.getComponentInstance(this));
      }
      return super.unregisterComponent(componentKey);
   }

   @Override
   public void stop()
   {
      if (managementContext != null)
      {
         // un-manage all registered MBeans
         managementContext.unregisterAll();
      }
      super.stop();
   };

   /**
    * Returns the list of the providers which are relevant for this container.
    *
    * @return the providers
    */
   Collection<ManagementProvider> getProviders()
   {
      HashSet<ManagementProvider> allProviders = new HashSet<ManagementProvider>();
      computeAllProviders(allProviders);
      return allProviders;
   }

   private void computeAllProviders(Set<ManagementProvider> allProviders)
   {
      if (parent != null)
      {
         parent.computeAllProviders(allProviders);
      }

      //
      allProviders.addAll(providers);
   }

   boolean addProvider(ManagementProvider provider)
   {
      // Prevent double registration just in case...
      if (providers.contains(provider))
      {
         return false;
      }

      //
      providers.add(provider);

      // Perform registration of already registered managed components
      managementContext.install(provider);

      //
      return true;
   }
}
