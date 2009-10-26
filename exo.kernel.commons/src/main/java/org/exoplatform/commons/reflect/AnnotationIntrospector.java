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
package org.exoplatform.commons.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Various utils for performing runtime introspection on annotations.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AnnotationIntrospector
{

   /**
    * Resolve an annotation of the specified class and its inheritance hierarchy. If no such annotation
    * cannot be resolved then null is returned.
    *
    * @param clazz the examined class
    * @param classAnnotation the annotation to lookup
    * @param <A> the annotation class
    * @return the annotation
    * @throws NullPointerException if any argument is null
    */
   public static <A extends Annotation> A resolveClassAnnotations(Class<?> clazz, Class<A> classAnnotation)
      throws NullPointerException
   {
      if (clazz == null)
      {
         throw new NullPointerException("No null class");
      }
      if (classAnnotation == null)
      {
         throw new NullPointerException("No null annotation");
      }

      //
      A tpl = clazz.getAnnotation(classAnnotation);

      //
      if (tpl == null)
      {
         for (Class itf : clazz.getInterfaces())
         {
            tpl = resolveClassAnnotations(itf, classAnnotation);
            if (tpl != null)
            {
               break;
            }
         }
      }

      //
      if (tpl == null)
      {
         Class<?> superClazz = clazz.getSuperclass();
         if (superClazz != null)
         {
            tpl = resolveClassAnnotations(superClazz, classAnnotation);
         }
      }

      //
      return tpl;
   }

   public static <A extends Annotation> Map<Method, A> resolveMethodAnnotations(Class<?> clazz,
      Class<A> methodAnnotation)
   {
      if (clazz == null)
      {
         throw new NullPointerException("No null class");
      }
      if (methodAnnotation == null)
      {
         throw new NullPointerException("No null annotation");
      }

      //
      Map<Method, A> methods = new HashMap<Method, A>();

      //
      for (Method method : clazz.getDeclaredMethods())
      {
         A annotation = method.getAnnotation(methodAnnotation);
         if (annotation != null)
         {
            methods.put(method, annotation);
         }
      }

      // Resolve parent annotations
      Class<?> superClazz = clazz.getSuperclass();
      if (superClazz != null)
      {
         Map<Method, A> parentAnnotations = resolveMethodAnnotations(superClazz, methodAnnotation);
         for (Map.Entry<Method, A> entry : parentAnnotations.entrySet())
         {
            Method parentMethod = entry.getKey();
            try
            {
               Method method = clazz.getMethod(parentMethod.getName(), parentMethod.getParameterTypes());
               if (!methods.containsKey(method))
               {
                  methods.put(method, entry.getValue());
               }
            }
            catch (NoSuchMethodException e)
            {
               throw new AssertionError(e);
            }
         }
      }

      // Resolve interface annotations
      for (Class<?> itf : clazz.getInterfaces())
      {
         Map<Method, A> itfAnnotations = resolveMethodAnnotations(itf, methodAnnotation);
         for (Map.Entry<Method, A> entry : itfAnnotations.entrySet())
         {
            Method itfMethod = entry.getKey();
            try
            {
               Method method = clazz.getMethod(itfMethod.getName(), itfMethod.getParameterTypes());
               if (!methods.containsKey(method))
               {
                  methods.put(method, entry.getValue());
               }
            }
            catch (NoSuchMethodException e)
            {
               throw new AssertionError(e);
            }
         }
      }

      //
      return methods;
   }

}
