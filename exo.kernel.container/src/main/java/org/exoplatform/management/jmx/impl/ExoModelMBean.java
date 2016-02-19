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

import org.exoplatform.management.spi.ManagedResource;
import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.jmx.annotations.NamingContext;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

/**
 * A convenient subclass of {@link RequiredModelMBean} that routes the invocation of the interface
 * {@link MBeanRegistration} to the managed resource when it implements the method. 
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoModelMBean extends RequiredModelMBean
{

   /** . */
   private Object mr;

   /** . */
   private final ManagedResource context;

   public ExoModelMBean(ManagedResource context, Object mr, ModelMBeanInfo mbi) throws MBeanException,
      RuntimeOperationsException, InstanceNotFoundException, InvalidTargetObjectTypeException
   {
      super(mbi);

      //
      this.context = context;
      this.mr = mr;

      //
      setManagedResource(mr, "ObjectReference");
   }

   @Override
   public Object invoke(String opName, Object[] opArgs, String[] sig) throws MBeanException, ReflectionException
   {
      context.beforeInvoke(mr);
      try
      {
         return super.invoke(opName, opArgs, sig);
      }
      finally
      {
         context.afterInvoke(mr);
      }
   }

   @Override
   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      name = super.preRegister(server, name);

      //
      if (mr instanceof MBeanRegistration)
      {
         ((MBeanRegistration)mr).preRegister(server, name);
      }

      //
      return name;
   }

   @Override
   public void postRegister(Boolean registrationDone)
   {
      super.postRegister(registrationDone);

      //
      PropertiesInfo info = PropertiesInfo.resolve(mr.getClass(), NamingContext.class);

      //
      MBeanScopingData scopingData = info != null ? info.resolve(mr) : new MBeanScopingData();

      //
      context.setScopingData(MBeanScopingData.class, scopingData);

      //
      if (mr instanceof MBeanRegistration)
      {
         ((MBeanRegistration)mr).postRegister(registrationDone);
      }
   }

   @Override
   public void preDeregister() throws Exception
   {
      if (mr instanceof MBeanRegistration)
      {
         ((MBeanRegistration)mr).preDeregister();
      }

      //
      if (mr instanceof ManagementAware)
      {
         ((ManagementAware)mr).setContext(null);
      }

      //
      super.preDeregister();
   }

   @Override
   public void postDeregister()
   {
      if (mr instanceof MBeanRegistration)
      {
         ((MBeanRegistration)mr).postDeregister();
      }

      //
      super.postDeregister();
   }
}
