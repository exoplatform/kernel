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

import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.spi.ManagedMethodMetaData;
import org.exoplatform.management.spi.ManagedMethodParameterMetaData;
import org.exoplatform.management.spi.ManagedPropertyMetaData;
import org.exoplatform.management.spi.ManagedTypeMetaData;
import org.exoplatform.container.management.MetaDataBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.management.Descriptor;
import javax.management.IntrospectionException;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.modelmbean.ModelMBeanAttributeInfo;
import javax.management.modelmbean.ModelMBeanConstructorInfo;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.ModelMBeanInfoSupport;
import javax.management.modelmbean.ModelMBeanNotificationInfo;
import javax.management.modelmbean.ModelMBeanOperationInfo;

/**
 * <p>A class that build mbean meta data</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ExoMBeanInfoBuilder
{

   private static enum Role {
      SET("setter"), IS("getter"), GET("getter"), OP("operation");

      private final String name;

      private Role(String role)
      {
         this.name = role;
      }
   }

   private ManagedTypeMetaData typeMD;

   /**
    * Create a new builder.
    *
    * @param clazz the clazz
    * @throws IllegalArgumentException if the class is null or does not contain meta data
    */
   public ExoMBeanInfoBuilder(Class clazz) throws IllegalArgumentException
   {
      this.typeMD = new MetaDataBuilder(clazz).build();
   }

   public ExoMBeanInfoBuilder(ManagedTypeMetaData typeMD) throws IllegalArgumentException
   {
      this.typeMD = typeMD;
   }

   private ModelMBeanOperationInfo buildOperationInfo(Method method, String description, Role role,
      Collection<ManagedMethodParameterMetaData> parametersMD, ImpactType impactType)
   {
      ModelMBeanOperationInfo operationInfo = new ModelMBeanOperationInfo(description, method);

      //
      if (description == null)
      {
         description = "Management operation";
      }

      //
      MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
      for (ManagedMethodParameterMetaData parameterMD : parametersMD)
      {
         int i = parameterMD.getIndex();
         MBeanParameterInfo parameterInfo = parameterInfos[i];
         String parameterName = parameterInfo.getName();
         String parameterDescription = operationInfo.getSignature()[i].getDescription();
         if (parameterMD.getName() != null)
         {
            parameterName = parameterMD.getName();
         }
         else if (parameterMD.getDescription() != null)
         {
            parameterDescription = parameterMD.getDescription();
         }
         parameterInfos[i] = new MBeanParameterInfo(parameterName, parameterInfo.getType(), parameterDescription);
      }

      //
      int jmxImpact;
      switch (impactType)
      {
         case READ:
            jmxImpact = MBeanOperationInfo.INFO;
            break;
         case IDEMPOTENT_WRITE:
         case WRITE:
            jmxImpact = MBeanOperationInfo.ACTION;
            break;
         default:
            throw new AssertionError();
      }

      //
      Descriptor operationDescriptor = operationInfo.getDescriptor();
      operationDescriptor.setField("role", role.name);

      //
      return new ModelMBeanOperationInfo(operationInfo.getName(), description, parameterInfos, operationInfo
         .getReturnType(), jmxImpact, operationDescriptor);
   }

   /**
    * Build the info.
    *
    * @return returns the info
    * @throws IllegalStateException raised by any build time issue
    */
   public ModelMBeanInfo build() throws IllegalStateException
   {
      String mbeanDescription = "Exo model mbean";
      if (typeMD.getDescription() != null)
      {
         mbeanDescription = typeMD.getDescription();
      }

      //
      ArrayList<ModelMBeanOperationInfo> operations = new ArrayList<ModelMBeanOperationInfo>();
      for (ManagedMethodMetaData methodMD : typeMD.getMethods())
      {
         ModelMBeanOperationInfo operationInfo =
            buildOperationInfo(methodMD.getMethod(), methodMD.getDescription(), Role.OP, methodMD.getParameters(), methodMD.getImpact());
         operations.add(operationInfo);
      }

      //
      Map<String, ModelMBeanAttributeInfo> attributeInfos = new HashMap<String, ModelMBeanAttributeInfo>();
      for (ManagedPropertyMetaData propertyMD : typeMD.getProperties())
      {

         Method getter = propertyMD.getGetter();
         if (getter != null)
         {
            Role role;
            String getterName = getter.getName();
            if (getterName.startsWith("get") && getterName.length() > 3)
            {
               role = Role.GET;
            }
            else if (getterName.startsWith("is") && getterName.length() > 2)
            {
               role = Role.IS;
            }
            else
            {
               throw new AssertionError();
            }
            Collection<ManagedMethodParameterMetaData> blah = Collections.emptyList();
            ModelMBeanOperationInfo operationInfo =
               buildOperationInfo(getter, propertyMD.getGetterDescription(), role, blah, ImpactType.READ);
            operations.add(operationInfo);
         }

         //
         Method setter = propertyMD.getSetter();
         if (setter != null)
         {
            ManagedMethodParameterMetaData s = new ManagedMethodParameterMetaData(0);
            s.setDescription(propertyMD.getSetterParameter().getDescription());
            s.setName(propertyMD.getSetterParameter().getName());
            Collection<ManagedMethodParameterMetaData> blah = Collections.singletonList(s);
            ModelMBeanOperationInfo operationInfo =
               buildOperationInfo(setter, propertyMD.getSetterDescription(), Role.SET, blah, ImpactType.IDEMPOTENT_WRITE);
            operations.add(operationInfo);
         }

         //
         try
         {
            String attributeDescription =
               propertyMD.getDescription() != null ? propertyMD.getDescription() : ("Managed attribute " + propertyMD
                  .getName());

            //
            ModelMBeanAttributeInfo attributeInfo =
               new ModelMBeanAttributeInfo(propertyMD.getName(), attributeDescription, getter, setter);

            //
            Descriptor attributeDescriptor = attributeInfo.getDescriptor();
            if (getter != null)
            {
               attributeDescriptor.setField("getMethod", getter.getName());
            }
            if (setter != null)
            {
               attributeDescriptor.setField("setMethod", setter.getName());
            }
            attributeDescriptor.setField("currencyTimeLimit", "-1");
            attributeDescriptor.setField("persistPolicy", "Never");
            attributeInfo.setDescriptor(attributeDescriptor);

            //
            ModelMBeanAttributeInfo previous = attributeInfos.put(propertyMD.getName(), attributeInfo);
            if (previous != null)
            {
               throw new IllegalArgumentException();
            }
         }
         catch (IntrospectionException e)
         {
            throw new AssertionError(e);
         }
      }

      //
      return new ModelMBeanInfoSupport(typeMD.getType().getName(), mbeanDescription, attributeInfos.values().toArray(
         new ModelMBeanAttributeInfo[attributeInfos.size()]), new ModelMBeanConstructorInfo[0], operations
         .toArray(new ModelMBeanOperationInfo[operations.size()]), new ModelMBeanNotificationInfo[0]);
   }
}
