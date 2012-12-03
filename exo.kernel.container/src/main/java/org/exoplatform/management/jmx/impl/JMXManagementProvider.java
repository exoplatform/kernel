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
package org.exoplatform.management.jmx.impl;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.spi.ManagedResource;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JMXManagementProvider implements ManagementProvider
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.JMXManagementProvider");

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

   public Object manage(ManagedResource context)
   {
      if (context == null)
      {
         throw new IllegalArgumentException("The context cannot be null");
      }
      ExoModelMBean mbean = null;
      try
      {
         ExoMBeanInfoBuilder infoBuilder = new ExoMBeanInfoBuilder(context.getMetaData());
         ModelMBeanInfo info = infoBuilder.build();
         mbean = new ExoModelMBean(context, context.getResource(), info);
      }
      catch (IllegalArgumentException e)
      {
         LOG.warn("Could not create the ExoModelMBean for the class " + context.getResource() == null ? null : context
            .getResource().getClass(), e);
      }
      catch (RuntimeOperationsException e)
      {
         LOG.warn("Could not create the ExoModelMBean for the class " + context.getResource() == null ? null : context
            .getResource().getClass(), e);
      }
      catch (InstanceNotFoundException e)
      {
         LOG.warn("Could not create the ExoModelMBean for the class " + context.getResource() == null ? null : context
            .getResource().getClass(), e);
      }
      catch (MBeanException e)
      {
         LOG.warn("Could not create the ExoModelMBean for the class " + context.getResource() == null ? null : context
            .getResource().getClass(), e);
      }
      catch (InvalidTargetObjectTypeException e)
      {
         LOG.warn("Could not create the ExoModelMBean for the class " + context.getResource() == null ? null : context
            .getResource().getClass(), e);
      }

      //
      if (mbean != null)
      {
         ObjectName on = null;
         PropertiesInfo oni = PropertiesInfo.resolve(context.getResource().getClass(), NameTemplate.class);
         if (oni != null)
         {
            try
            {
               Map<String, String> foo = oni.resolve(context.getResource());
               on = JMX.createObjectName("exo", foo);
            }
            catch (MalformedObjectNameException e)
            {
               LOG.warn("Could not create the ObjectName for the class " + context.getResource().getClass(), e);
            }
         }

         if (on != null)
         {
            // Merge with the container hierarchy context
            try
            {
               Map<String, String> props = new LinkedHashMap<String, String>();

               // Merge scoping properties
               List<MBeanScopingData> list = context.getScopingData(MBeanScopingData.class);
               // Read in revert order because wee received list of parents in upward order
               for (int i = list.size(); i > 0; i--)
               {
                  MBeanScopingData scopingData = list.get(i - 1);
                  props.putAll(scopingData);
               }

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
               on = JMX.createObjectName(on.getDomain(), props);

               //
               attemptToRegister(on, mbean);

               //
               return on;
            }
            catch (MalformedObjectNameException e)
            {
               LOG.warn("Could not register the MBean for the class " + context.getResource().getClass(), e);
            }
         }
      }

      //
      return null;  
   }

   private void attemptToRegister(final ObjectName name, final Object mbean)
   {
      synchronized (server)
      {
         if (server.isRegistered(name))
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("The MBean '" + name + " has already been registered, it will be unregistered and then re-registered");
            }
            try
            {
               SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Void>()
               {
                  public Void run() throws Exception
                  {
                     server.unregisterMBean(name);
                     return null;
                  }
               });
            }
            catch (PrivilegedActionException e)
            {
               throw new RuntimeException("Failed to unregister MBean '" + name + " due to " + e.getMessage(), e);
            }
         }
         try
         {
            SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Void>()
            {
               public Void run() throws Exception
               {
                  server.registerMBean(mbean, name);
                  return null;
               }
            });
         }
         catch (PrivilegedActionException e)
         {
            throw new RuntimeException("Failed to register MBean '" + name + " due to " + e.getMessage(), e);
         }
      }
   }

   public void unmanage(Object key)
   {
      final ObjectName name = (ObjectName)key;
      try
      {
         try
         {
            SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Void>()
            {
               public Void run() throws Exception
               {
                  if (server.isRegistered(name))
                  {
                     server.unregisterMBean(name);                     
                  }
                  return null;
               }
            });
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof InstanceNotFoundException)
            {
               throw (InstanceNotFoundException)cause;
            }
            else if (cause instanceof MBeanRegistrationException)
            {
               throw (MBeanRegistrationException)cause;
            }
            else if (cause instanceof RuntimeException)
            {
               throw (RuntimeException)cause;
            }
            else
            {
               throw new RuntimeException(cause);
            }
         }
      }
      catch (InstanceNotFoundException e)
      {
         LOG.warn("Could not unregister the MBean " + name, e);
      }
      catch (MBeanRegistrationException e)
      {
         LOG.warn("Could not unregister the MBean " + name, e);
      }
   }
}
