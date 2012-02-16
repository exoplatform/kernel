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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.management.spi.ManagedResource;
import org.exoplatform.management.spi.ManagedTypeMetaData;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagementContextImpl implements ManagementContext, ManagedResource
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ManagementContextImpl");

   /** . */
   private final Map<Class<?>, Object> scopingDataList;

   /** The registrations done by this mbean. */
   private final Map<Object, ManagementContextImpl> registrations;

   /** . */
   final Map<ManagementProvider, Object> managedSet;

   /** . */
   private final ManagementContextImpl parent;

   /** . */
   private final Object resource;

   /** . */
   private final ManagedTypeMetaData typeMD;

   /** An optional container setup when the management context is attached to a container. */
   final ManageableContainer container;

   public ManagementContextImpl(ManageableContainer container)
   {
      if (container == null)
      {
         throw new IllegalArgumentException("The container cannot be null");
      }
      
      //
      Object resource = null;
      ManagedTypeMetaData typeMD = null;
      MetaDataBuilder builder = new MetaDataBuilder(container.getClass());
      if (builder.isBuildable())
      {
         resource = container;
         typeMD = builder.build();
      }

      //
      this.managedSet = new HashMap<ManagementProvider, Object>();
      this.registrations = new HashMap<Object, ManagementContextImpl>();
      this.parent = null;
      this.scopingDataList = new HashMap<Class<?>, Object>();
      this.resource = resource;
      this.typeMD = typeMD;
      this.container = container;
   }

   public ManagementContextImpl(ManagementContextImpl parent, ManageableContainer container)
   {
      if (parent == null)
      {
         throw new IllegalArgumentException("The parent cannot be null");
      }
      if (container == null)
      {
         throw new IllegalArgumentException("The container cannot be null");
      }

      //
      Object resource = null;
      ManagedTypeMetaData typeMD = null;
      MetaDataBuilder builder = new MetaDataBuilder(container.getClass());
      if (builder.isBuildable())
      {
         resource = container;
         typeMD = builder.build();
      }

      //
      this.managedSet = new HashMap<ManagementProvider, Object>();
      this.registrations = new HashMap<Object, ManagementContextImpl>();
      this.parent = parent;
      this.scopingDataList = new HashMap<Class<?>, Object>();
      this.resource = resource;
      this.typeMD = typeMD;
      this.container = container;
   }

   public ManagementContextImpl(ManagementContextImpl parent, Object resource, ManagedTypeMetaData typeMD)
   {
      if (parent == null)
      {
         throw new IllegalArgumentException("The parent cannot be null");
      }
      if ((resource != null && typeMD == null) && (resource == null && typeMD != null))
      {
         throw new IllegalArgumentException("Can't have resource null and meta data not null or the converse");
      }

      //
      this.managedSet = new HashMap<ManagementProvider, Object>();
      this.registrations = new HashMap<Object, ManagementContextImpl>();
      this.parent = parent;
      this.scopingDataList = new HashMap<Class<?>, Object>();
      this.resource = resource;
      this.typeMD = typeMD;
      this.container = null;
   }

   public ManagementContext getParent()
   {
      return parent;
   }

   public <S> void setScopingData(Class<S> scopeType, S scopingData)
   {
      this.scopingDataList.put(scopeType, scopingData);
   }

   public void register(Object o)
   {
      Object resource = null;

      // Apply managed by annotation
      ManagedBy managedBy = o.getClass().getAnnotation(ManagedBy.class);
      if (managedBy != null)
      {
         try
         {
            Class managedByClass = managedBy.value();
            Constructor<?> blah = managedByClass.getConstructor(o.getClass());
            resource = blah.newInstance(o);
         }
         catch (NoSuchMethodException e)
         {
            LOG.error(e.getLocalizedMessage(), e);
         }
         catch (InstantiationException e)
         {
            LOG.error(e.getLocalizedMessage(), e);
         }
         catch (IllegalAccessException e)
         {
            LOG.error(e.getLocalizedMessage(), e);
         }
         catch (InvocationTargetException e)
         {
            LOG.error(e.getLocalizedMessage(), e);
         }
      }
      else
      {
         resource = o;
      }

      //
      if (resource != null) {

         MetaDataBuilder builder = new MetaDataBuilder(resource.getClass());
         if (builder.isBuildable()) {
            ManagedTypeMetaData metaData = builder.build();

            //
            ManagementContextImpl managementContext;
            if (resource instanceof ManageableContainer)
            {
               managementContext = ((ManageableContainer)resource).managementContext;
            }
            else
            {
               managementContext = new ManagementContextImpl(this, resource, metaData);
            }

            //
            registrations.put(resource, managementContext);

            //
            ManageableContainer container = findContainer();

            // Install for all the providers related
            for (ManagementProvider provider : container.getProviders())
            {
               Object name = provider.manage(managementContext);
               if (name != null)
               {
                  managementContext.managedSet.put(provider, name);
               }
            }

            // Allow for more resource management
            if (resource instanceof ManagementAware)
            {
               ((ManagementAware)resource).setContext(managementContext);
            }
         }
      }
   }

    /**
     * 
    * {@inheritDoc}
    */
   public void unregister(Object o)
   {
      ManagementContextImpl context = registrations.remove(o);
      if (context != null)
      {
         for (Map.Entry<ManagementProvider, Object> entry : context.managedSet.entrySet()) {
            entry.getKey().unmanage(entry.getValue());
         }
      }
   }
   
   /**
    * Unmanages (unregisters) all early registered MBeans in ManagementProviders
    */
   public void unregisterAll()
   {
      Iterator<Entry<Object, ManagementContextImpl>> iterator = registrations.entrySet().iterator();
      while (iterator.hasNext())
      {
         Entry<Object, ManagementContextImpl> contextEntry = iterator.next();
         iterator.remove();
         if (contextEntry.getValue() != null)
         {
            for (Map.Entry<ManagementProvider, Object> provider : contextEntry.getValue().managedSet.entrySet())
            {
               provider.getKey().unmanage(provider.getValue());
            }
         }
      }
   }

   public <S> List<S> getScopingData(Class<S> scopeType)
   {
      ArrayList<S> list = new ArrayList<S>();
      for (ManagementContextImpl current = this; current != null; current = current.parent)
      {
         Object scopedData = current.scopingDataList.get(scopeType);
         if (scopedData != null)
         {
            // It must be that type since we put it
            list.add((S)scopedData);
         }
      }
      return list;
   }

   public ExoContainer findContainer()
   {
      for (ManagementContextImpl current = this;true;current = current.parent)
      {
         if (current.container instanceof ExoContainer)
         {
            return (ExoContainer)current.container;
         }
         else if (current.parent == null)
         {
            return null;
         }
      }
   }

   public void beforeInvoke(Object managedResource)
   {
      ExoContainer container = findContainer();
      if (container != null)
      {
         RequestLifeCycle.begin(container);
      }
   }

   public void afterInvoke(Object managedResource)
   {
      RequestLifeCycle.end();
   }

   @Override
   public String toString()
   {
      return "ManagementContextImpl[container=" + container + "]";
   }

   public Object getResource()
   {
      return resource;
   }

   public ManagedTypeMetaData getMetaData()
   {
      return typeMD;
   }

   void install(ManagementProvider provider) {

      // Install the current resource if necessary
      if (resource != null&& typeMD != null)
      {
         Object name = provider.manage(this);
         if (name != null)
         {
            managedSet.put(provider, name);
         }
      }

      // Install thie children except the container ones
      for (ManagementContextImpl registration : registrations.values())
      {
         registration.install(provider);
      }
   }
}
