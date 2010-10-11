/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.services.rpc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * This command will allow you to call one specific method with the arguments given by the execute method
 * on a component.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public class SingleMethodCallCommand implements RemoteCommand
{

   /**
    * The component on which we want to execute the method
    */
   private final Object component;

   /**
    * The method that we want to call
    */
   private final Method method;

   /**
    * The id of the command
    */
   private final String id;

   /**
    * 
    * @param component the component on which we want to execute the method
    * @param methodName the name of the method
    * @param parameterTypes the parameter array
    * @throws NoSuchMethodException  if a matching method is not found.
     * @exception  SecurityException
     *             If a security manager, <i>s</i>, is present and any of the
     *             following conditions is met:
     *
     *             <ul>
     *
     *             <li> invocation of 
     *             <tt>{@link SecurityManager#checkMemberAccess
     *             s.checkMemberAccess(this, Member.DECLARED)}</tt> denies
     *             access to the declared method
     *
     *             <li> the caller's class loader is not the same as or an
     *             ancestor of the class loader for the current class and
     *             invocation of <tt>{@link SecurityManager#checkPackageAccess
     *             s.checkPackageAccess()}</tt> denies access to the package
     *             of this class
     *
     *             </ul>
    * @throws ClassNotFoundException If the last parameter type is an array and we 
    * cannot find the type of the array
    */
   public SingleMethodCallCommand(Object component, String methodName, Class<?>... parameterTypes)
      throws SecurityException, NoSuchMethodException, ClassNotFoundException
   {
      if (component == null)
      {
         throw new IllegalArgumentException("The component cannot be null");
      }
      if (methodName == null || (methodName = methodName.trim()).length() == 0)
      {
         throw new IllegalArgumentException("The methodName cannot be empty");
      }
      this.component = component;
      this.method = component.getClass().getDeclaredMethod(methodName, parameterTypes);
      if (Modifier.isPrivate(method.getModifiers()))
      {
         throw new IllegalArgumentException("The method '" + methodName + "' cannot be private");
      }
      this.id = getId(component, method);
   }

   /**
    * {@inheritDoc}
    */
   public Serializable execute(Serializable[] args) throws Throwable
   {
      try
      {
         return (Serializable)method.invoke(component, (Object[])args);
      }
      catch (Exception e)
      {
         throw new Exception("Could not execute the method " + id + " with the arguments " + Arrays.toString(args), e);
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return id;
   }

   /**
    * Gives a unique Id from the component and the method
    */
   private static String getId(Object component, Method method)
   {
      StringBuilder sb = new StringBuilder();
      sb.append(component.getClass().getName());
      sb.append('.');
      sb.append(method.getName());
      sb.append('(');
      boolean first = true;
      for (Class<?> c : method.getParameterTypes())
      {
         if (first)
         {
            first = false;
         }
         else
         {
            sb.append(',');
         }
         sb.append(c.getSimpleName());
      }
      sb.append(')');
      return sb.toString();
   }
}
