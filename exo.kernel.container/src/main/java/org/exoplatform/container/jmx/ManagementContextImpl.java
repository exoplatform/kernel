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

import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.management.jmx.annotations.NameTemplate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagementContextImpl implements ManagementContext
{

   /** . */
   private Map<String, String> scopingProperties;

   /** The registrations done by this mbean. */
   private final Map<Object, ObjectName> registrations;

   /** . */
   private final ManagementContextImpl parent;

   /** . */
   final MBeanServer server;

   public ManagementContextImpl(MBeanServer server, Map<String, String> scopingProperties)
   {
      this.registrations = new HashMap<Object, ObjectName>();
      this.parent = null;
      this.scopingProperties = scopingProperties;
      this.server = server;
   }

   public ManagementContextImpl(ManagementContextImpl parent, Map<String, String> scopingProperties)
   {
      this.registrations = new HashMap<Object, ObjectName>();
      this.parent = parent;
      this.scopingProperties = scopingProperties;
      this.server = parent.server;
   }

   public void register(Object o)
   {
      ObjectName name = manageMBean(o);
      if (name != null)
      {
         registrations.put(o, name);
      }
   }

   public void unregister(Object o)
   {
      ObjectName name = registrations.remove(o);
      if (name != null)
      {
         unmanageMBean(name);
      }
   }

   private ExoModelMBean createExoMBean(Object bean)
   {
      Object view = null;

      // Apply managed by annotation
      ManagedBy managedBy = bean.getClass().getAnnotation(ManagedBy.class);
      if (managedBy != null)
      {
         try
         {
            Class managedByClass = managedBy.value();
            Constructor<?> blah = managedByClass.getConstructor(bean.getClass());
            view = blah.newInstance(bean);
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
         view = bean;
      }

      //
      if (view != null)
      {
         ExoMBeanInfoBuilder infoBuilder = new ExoMBeanInfoBuilder(view.getClass());
         if (infoBuilder.isBuildable())
         {
            try
            {
               ModelMBeanInfo info = infoBuilder.build();
               return new ExoModelMBean(this, view, info);
            }
            catch (Exception e)
            {
               e.printStackTrace();
            }
         }
      }

      //
      return null;
   }

   public ObjectName manageMBean(Object bean)
   {

      //
      ExoModelMBean mbean = createExoMBean(bean);

      //
      if (mbean != null)
      {
         Object mr = mbean.getManagedResource();
         ObjectName on = null;
         PropertiesInfo oni = PropertiesInfo.resolve(mr.getClass(), NameTemplate.class);
         if (oni != null)
         {
            try
            {
               Map<String, String> foo = oni.resolve(mr);
               on = JMX.createObjectName("exo", foo);
            }
            catch (MalformedObjectNameException e)
            {
               e.printStackTrace();
            }
         }

         //
         if (on != null)
         {
            // Merge with the container hierarchy context
            try
            {
               Map<String, String> props = new Hashtable<String, String>();

               // Julien : I know it's does not look great but it's necessary
               // for compiling under Java 5 and Java 6 properly. The methods
               // ObjectName#getKeyPropertyList() returns an Hashtable with Java 5
               // and a Hashtable<String, String> with Java 6.
               for (Object o : on.getKeyPropertyList().entrySet())
               {
                  Map.Entry entry = (Map.Entry)o;
                  String key = (String)entry.getKey();
                  String value = (String)entry.getValue();
                  props.put(key, value);
               }
               for (ManagementContextImpl current = this; current != null; current = current.parent)
               {
                  props.putAll(current.scopingProperties);
               }
               on = JMX.createObjectName(on.getDomain(), props);
               attemptToRegister(on, mbean);
               return on;
            }
            catch (MalformedObjectNameException e)
            {
               e.printStackTrace();
            }
         }
      }

      //
      return null;
   }

   public void unmanageMBean(ObjectName name)
   {
      try
      {
         server.unregisterMBean(name);
      }
      catch (InstanceNotFoundException e)
      {
         e.printStackTrace();
      }
      catch (MBeanRegistrationException e)
      {
         e.printStackTrace();
      }
   }

   synchronized void attemptToRegister(ObjectName name, Object mbean)
   {
      synchronized (server)
      {
         try
         {
            server.registerMBean(mbean, name);
         }
         catch (InstanceAlreadyExistsException e)
         {
            try
            {

               server.unregisterMBean(name);
               server.registerMBean(mbean, name);

            }
            catch (Exception e1)
            {
               throw new RuntimeException("Failed to register MBean '" + name + " due to " + e.getMessage(), e);
            }
         }
         catch (Exception e)
         {
            throw new RuntimeException("Failed to register MBean '" + name + " due to " + e.getMessage(), e);
         }
      }
   }
}
