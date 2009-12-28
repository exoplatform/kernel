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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.container.management.KernelManagementContext;
import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.spi.ManagedTypeMetaData;
import org.exoplatform.container.management.MetaDataBuilder;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.management.spi.ManagementProviderContext;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.ManagedBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagementContextImpl implements ManagementContext, ManagementProviderContext
{

   /** . */
   private final Map<Class<?>, Object> scopingProperties;

   /** The registrations done by this mbean. */
   private final Map<Object, ManagementContextImpl> registrations;

   /** . */
   final Map<ManagementProvider, Object> bilto;

   /** . */
   private final ManagementContextImpl parent;

   /** . */
   final KernelManagementContext kernelContext;

   /** An optional container setup when the management context is attached to a container. */
   ManageableContainer container;

   public ManagementContextImpl(KernelManagementContext kernelContext)
   {
      if (kernelContext == null)
      {
         throw new NullPointerException();
      }
      this.bilto = new HashMap<ManagementProvider, Object>();
      this.registrations = new HashMap<Object, ManagementContextImpl>();
      this.parent = null;

      // This is the root container that never have scoping properties
      // Also without that we would have an NPE when the portal container are registered
      // as the scoping properties would not exist since the root container would not be yet
      
      this.scopingProperties = new HashMap<Class<?>, Object>();
      this.kernelContext = kernelContext;
   }

   public ManagementContextImpl(ManagementContextImpl parent)
   {
      if (parent == null)
      {
         throw new NullPointerException();
      }
      this.bilto = new HashMap<ManagementProvider, Object>();
      this.registrations = new HashMap<Object, ManagementContextImpl>();
      this.parent = parent;
      this.scopingProperties = new HashMap<Class<?>, Object>();
      this.kernelContext = parent.kernelContext;
   }

   public ManagementContext getParent()
   {
      return parent;
   }

   public <S> void setScopingData(Class<S> scopeType, S scopingProperties)
   {
      this.scopingProperties.put(scopeType, scopingProperties);
   }

   public void register(Object o)
   {
      Object view = null;

      // Apply managed by annotation
      ManagedBy managedBy = o.getClass().getAnnotation(ManagedBy.class);
      if (managedBy != null)
      {
         try
         {
            Class managedByClass = managedBy.value();
            Constructor<?> blah = managedByClass.getConstructor(o.getClass());
            view = blah.newInstance(o);
         }
         catch (NoSuchMethodException e)
         {
            e.printStackTrace();
         }
         catch (InstantiationException e)
         {
            e.printStackTrace();
         }
         catch (IllegalAccessException e)
         {
            e.printStackTrace();
         }
         catch (InvocationTargetException e)
         {
            e.printStackTrace();
         }
      }
      else
      {
         view = o;
      }

      //
      if (view != null) {

         MetaDataBuilder builder = new MetaDataBuilder(view.getClass());
         if (builder.isBuildable()) {
            ManagedTypeMetaData metaData = builder.build();

            //
            ManagementContextImpl viewContext;
            if (view instanceof ManageableContainer)
            {
               viewContext = ((ManageableContainer)view).managementContext;
            }
            else
            {
               viewContext = new ManagementContextImpl(this);
            }

            //
            registrations.put(view, viewContext);

            //
            for (ManagementProvider provider : kernelContext.getProviders())
            {
               Object name = provider.manage(this, view, metaData);
               if (name != null)
               {
                  viewContext.bilto.put(provider, name);
               }
            }

            // Allow for more resource management
            if (view instanceof ManagementAware)
            {
               ((ManagementAware)view).setContext(viewContext);
            }
         }
      }
   }

   public void unregister(Object o)
   {
      ManagementContextImpl context = registrations.remove(o);
      if (context != null)
      {
         for (Map.Entry<ManagementProvider, Object> entry : context.bilto.entrySet()) {
            entry.getKey().unmanage(entry.getValue());
         }
      }
   }

   public <S> List<S> getScopingProperties(Class<S> scopeType)
   {
      ArrayList<S> list = new ArrayList<S>();
      for (ManagementContextImpl current = this; current != null; current = current.parent)
      {
         Object scopedData = current.scopingProperties.get(scopeType);
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
}
