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
import org.exoplatform.container.management.ManagedTypeMetaData;
import org.exoplatform.container.management.MetaDataBuilder;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.ManagedBy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagementContextImpl implements ManagementContext
{

   /** . */
   Map<String, String> scopingProperties;

   /** The registrations done by this mbean. */
   private final Map<Object, Object> registrations;

   /** . */
   private final ManagementContextImpl parent;

   /** . */
   final JMXManagementProvider provider;

   /** An optional container setup when the management context is attached to a container. */
   ManageableContainer container;

   public ManagementContextImpl()
   {
      this(new JMXManagementProvider());
   }

   public ManagementContextImpl(JMXManagementProvider provider)
   {
      if (provider == null)
      {
         throw new NullPointerException();
      }
      this.registrations = new HashMap<Object, Object>();
      this.parent = null;

      // This is the root container that never have scoping properties
      // Also without that we would have an NPE when the portal container are registered
      // as the scoping properties would not exist since the root container would not be yet
      
      this.scopingProperties = Collections.emptyMap();
      this.provider = provider;
   }

   public ManagementContextImpl(ManagementContextImpl parent)
   {
      if (parent == null)
      {
         throw new NullPointerException();
      }
      this.registrations = new HashMap<Object, Object>();
      this.parent = parent;
      this.scopingProperties = null;
      this.provider = parent.provider;
   }

   public ManagementContext getParent()
   {
      return parent;
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
            Object name = provider.manage(this, view, metaData);
            if (name != null)
            {
               registrations.put(o, name);
            }
         }
      }
   }

   public void unregister(Object o)
   {
      Object name = registrations.remove(o);
      if (name != null)
      {
         provider.unmanage(name);
      }
   }

   public Map<String, String> getScopingProperties() {
      Map<String, String> props = new HashMap<String, String>();
      for (ManagementContextImpl current = this; current != null; current = current.parent)
      {
         if (current.scopingProperties != null)
         {
            props.putAll(current.scopingProperties);
         }
      }
      return props;
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

   @Override
   public String toString()
   {
      return "ManagementContextImpl[container=" + container + "]";
   }
}
