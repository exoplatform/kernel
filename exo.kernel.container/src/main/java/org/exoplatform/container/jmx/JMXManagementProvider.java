/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.container.jmx;

import org.exoplatform.management.spi.ManagedTypeMetaData;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.management.spi.ManagementProviderContext;
import org.exoplatform.management.jmx.annotations.NameTemplate;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfo;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JMXManagementProvider implements ManagementProvider
{

   /** . */
   private final MBeanServer server;

   public JMXManagementProvider()
   {
      this(MBeanServerFactory.createMBeanServer());
   }

   public JMXManagementProvider(MBeanServer server)
   {
      this.server = server;
   }

   public Object manage(ManagementProviderContext context, Object managedResource, ManagedTypeMetaData metaData)
   {
      ExoModelMBean mbean = null;
      try
      {
         ExoMBeanInfoBuilder infoBuilder = new ExoMBeanInfoBuilder(metaData);
         ModelMBeanInfo info = infoBuilder.build();
         mbean = new ExoModelMBean(context, managedResource, info);
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }

      //
      if (mbean != null)
      {
         ObjectName on = null;
         PropertiesInfo oni = PropertiesInfo.resolve(managedResource.getClass(), NameTemplate.class);
         if (oni != null)
         {
            try
            {
               Map<String, String> foo = oni.resolve(managedResource);
               on = JMX.createObjectName("exo", foo);
            }
            catch (MalformedObjectNameException e)
            {
               e.printStackTrace();
            }
         }

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

               //
               Map<String, String> scopingProperties = context.getScopingProperties();

               // Merge with scoping properties
               props.putAll(scopingProperties);

               //
               on = JMX.createObjectName(on.getDomain(), props);

               //
               attemptToRegister(on, mbean);

               //
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

   private void attemptToRegister(ObjectName name, Object mbean)
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

   public void unmanage(Object key)
   {
      ObjectName name = (ObjectName)key;
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
}
