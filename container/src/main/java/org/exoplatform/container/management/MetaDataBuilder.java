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

import org.exoplatform.commons.reflect.AnnotationIntrospector;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <p>A class that build the management view meta data of a specified class.</p>
 * <p>The following rules do apply to the class from which meta data are constructed:
 * <ul>
 * <li>The class must be annotated by {@link org.exoplatform.management.annotations.Managed}</li>
 * <li>The class may be annoated by {@link org.exoplatform.management.annotations.ManagedDescription}</li>
 * <li>Any property described by its getter and/or setter getter annotated by {@link org.exoplatform.management.annotations.Managed} is exposed as an attribute/li>
 * <li>Any property providing an annotated getter is readable</li>
 * <li>Any property providing an annotated setter is writable</li>
 * <li>Any getter/setter annotated by {@link org.exoplatform.management.annotations.ManagedName} redefines the attribute name</li>
 * <li>Any getter/setter annotated by {@link org.exoplatform.management.annotations.ManagedDescription} defines the attribute description</li>
 * <li>When corresponding getter/setter redefines the attribute name, the value must be the same otherwhise
 * an exception is thrown at built time</li>
 * <li>Any method annotated by {@link org.exoplatform.management.annotations.Managed} is exposed as a management operation</li>
 * <li>Any method annotated by {@link org.exoplatform.management.annotations.ManagedDescription} defines the operation description</li>
 * <li>Any non setter/getter method annotated by {@link org.exoplatform.management.annotations.ManagedName} causes a built time exception</li>
 * <li>Any method argument annotated by {@link org.exoplatform.management.annotations.ManagedName} defines the management name of the corresponding operation parameter</li>
 * <li>Any method argument annotated by {@link org.exoplatform.management.annotations.ManagedDescription} defines the management description of the corresponding operation parameter</li>
 * </ul>
 * </p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class MetaDataBuilder
{

   private static enum OperationType {
      SET, GET, OP
   }

   private Class clazz;

   private boolean buildable;

   /**
    * Create a new builder.
    *
    * @param clazz the clazz
    * @throws IllegalArgumentException if the class is null or is not annotated by {@link org.exoplatform.management.annotations.Managed}
    */
   public MetaDataBuilder(Class clazz) throws IllegalArgumentException
   {
      if (clazz == null)
      {
         throw new NullPointerException();
      }

      //
      Managed mb = AnnotationIntrospector.resolveClassAnnotations(clazz, Managed.class);

      //
      this.clazz = clazz;
      this.buildable = mb != null;
   }

   public boolean isBuildable()
   {
      return buildable;
   }

   /**
    * Build the info.
    *
    * @return returns the info
    * @throws IllegalStateException raised by any build time issue
    */
   public ManagedTypeMetaData build() throws IllegalStateException
   {
      if (!buildable)
      {
         throw new IllegalStateException("Class " + clazz.getName() + " does not contain management annotation");
      }

      //
      ManagedDescription typeDescriptionAnn =
         AnnotationIntrospector.resolveClassAnnotations(clazz, ManagedDescription.class);
      String typeDescription = typeDescriptionAnn != null ? typeDescriptionAnn.value() : null;

      //
      Map<Method, Managed> managedMethods = AnnotationIntrospector.resolveMethodAnnotations(clazz, Managed.class);
      Map<Method, ManagedName> methodNames = AnnotationIntrospector.resolveMethodAnnotations(clazz, ManagedName.class);
      Map<Method, ManagedDescription> methodDescriptions =
         AnnotationIntrospector.resolveMethodAnnotations(clazz, ManagedDescription.class);

      //
      Map<Method, ManagedMethodMetaData> bilto = new HashMap<Method, ManagedMethodMetaData>();
      for (Map.Entry<Method, Managed> entry : managedMethods.entrySet())
      {

         Method method = entry.getKey();

         //
         ManagedDescription methodDescriptionAnn = methodDescriptions.get(method);
         String methodDescription = methodDescriptionAnn != null ? methodDescriptionAnn.value() : null;

         // Build the default mbean info
         ManagedMethodMetaData managedMethod = new ManagedMethodMetaData(method);
         managedMethod.setDescription(methodDescription);

         // Overload with annotations meta data
         Annotation[][] parameterAnnotations = method.getParameterAnnotations();
         for (int i = 0; i < parameterAnnotations.length; i++)
         {
            ManagedMethodParameterMetaData mmpMD = new ManagedMethodParameterMetaData(i);
            for (Annotation parameterAnnotation : parameterAnnotations[i])
            {
               if (parameterAnnotation instanceof ManagedName)
               {
                  mmpMD.setName(((ManagedName)parameterAnnotation).value());
               }
               else if (parameterAnnotation instanceof ManagedDescription)
               {
                  mmpMD.setDescription(((ManagedDescription)parameterAnnotation).value());
               }
            }
            managedMethod.addParameter(mmpMD);
         }

         //
         bilto.put(method, managedMethod);
      }

      //
      ManagedTypeMetaData managedType = new ManagedTypeMetaData(clazz);
      managedType.setDescription(typeDescription);

      //
      Map<String, ManagedMethodMetaData> setters = new HashMap<String, ManagedMethodMetaData>();
      Map<String, ManagedMethodMetaData> getters = new HashMap<String, ManagedMethodMetaData>();
      for (Map.Entry<Method, ManagedMethodMetaData> entry : bilto.entrySet())
      {

         Method method = entry.getKey();

         //
         String methodName = method.getName();
         Class[] parameterTypes = method.getParameterTypes();

         //
         OperationType type = OperationType.OP;
         Integer index = null;
         if (method.getReturnType() == void.class)
         {
            if (parameterTypes.length == 1 && methodName.startsWith("set") && methodName.length() > 4)
            {
               type = OperationType.SET;
               index = 3;
            }
         }
         else
         {
            if (parameterTypes.length == 0)
            {
               type = OperationType.GET;
               if (methodName.startsWith("get") && methodName.length() > 3)
               {
                  index = 3;
               }
               else if (methodName.startsWith("is") && methodName.length() > 2)
               {
                  index = 2;
               }
            }
         }

         // Put in the correct map if it is an attribute
         if (index != null)
         {
            String attributeName = methodName.substring(index);

            //
            Map<String, ManagedMethodMetaData> map = type == OperationType.SET ? setters : getters;
            ManagedMethodMetaData previous = map.put(attributeName, entry.getValue());
            if (previous != null)
            {
               throw new IllegalArgumentException("Duplicate attribute " + type + " " + previous + " and " + method);
            }
         }
         else
         {
            ManagedName managedName = methodNames.get(method);
            if (managedName != null)
            {
               throw new IllegalArgumentException("Managed operation " + method.getName()
                  + " cannot be annoated with @" + ManagedName.class.getName() + " with value " + managedName.value());
            }

            //
            managedType.addMethod(entry.getValue());
         }
      }

      // Process attributes
      Set<String> attributeNames = new HashSet<String>();
      attributeNames.addAll(getters.keySet());
      attributeNames.addAll(setters.keySet());
      for (String attributeName : attributeNames)
      {
         ManagedMethodMetaData managedGetter = getters.get(attributeName);
         ManagedMethodMetaData managedSetter = setters.get(attributeName);

         String propertyDescription = null;
         ManagedName getterName = null;
         ManagedName setterName = null;
         String getterDescription = null;
         String setterDescription = null;
         Method getter = null;
         Method setter = null;
         ManagedParameterMetaData mpm = null;
         if (managedGetter != null)
         {
            getter = managedGetter.getMethod();
            getterName = methodNames.get(getter);
            getterDescription = managedGetter.getDescription();
            propertyDescription = getterDescription;
         }
         if (managedSetter != null)
         {
            setter = managedSetter.getMethod();
            setterName = methodNames.get(setter);
            setterDescription = managedSetter.getDescription();
            if (propertyDescription == null)
            {
               propertyDescription = setterDescription;
            }
            mpm = managedSetter.getParameters().iterator().next();
         }

         // Consistency check
         if (getterName != null)
         {
            if (setterName != null)
            {
               if (!getterName.value().equals(setterName.value()))
               {
                  throw new IllegalArgumentException("Getter name=" + getterName.value()
                     + " does not match the setter name=" + setterName.value());
               }
            }
            attributeName = getterName.value();
         }
         else if (setterName != null)
         {
            attributeName = setterName.value();
         }

         //
         ManagedPropertyMetaData managedProperty =
            new ManagedPropertyMetaData(attributeName, getter, getterDescription, setter, setterDescription, mpm);

         managedProperty.setDescription(propertyDescription);

         //
         ManagedPropertyMetaData previousManagedProperty = managedType.getProperty(managedProperty.getName());
         if (previousManagedProperty != null)
         {
            throw new IllegalArgumentException("The same property was declared twice old=" + previousManagedProperty
               + " new=" + managedProperty);
         }

         //
         managedType.addProperty(managedProperty);
      }

      //
      return managedType;
   }
}