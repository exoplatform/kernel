/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container;

import org.exoplatform.container.spi.ContainerException;

import java.util.Deque;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class CachingContainerMT extends CachingContainer
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -448537861455415058L;

   /**
    * Used to detect all the dependencies not properly defined
    */
   protected final transient ThreadLocal<Deque<DependencyStack>> dependencyStacks = Mode
      .hasMode(Mode.AUTO_SOLVE_DEP_ISSUES) ? new ThreadLocal<Deque<DependencyStack>>() : null;

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getComponentInstanceOfType(Class<T> componentType, boolean autoRegistration)
   {
      Deque<DependencyStack> stacks = dependencyStacks != null ? dependencyStacks.get() : null;
      DependencyStack stack = null;
      T instance;
      try
      {
         if (stacks != null)
         {
            stack = stacks.getLast();
            stack.add(new DependencyByType(componentType));
         }
         instance = super.getComponentInstanceOfType(componentType, autoRegistration);
      }
      finally
      {
         if (stack != null && !stack.isEmpty())
         {
            stack.removeLast();
         }
      }
      return instance;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getComponentInstance(Object componentKey, Class<T> bindType, boolean autoRegistration)
      throws ContainerException
   {
      Deque<DependencyStack> stacks = dependencyStacks != null ? dependencyStacks.get() : null;
      DependencyStack stack = null;
      T instance;
      try
      {
         if (stacks != null)
         {
            stack = stacks.getLast();
            if (componentKey instanceof String)
            {
               stack.add(new DependencyByName((String)componentKey, bindType));
            }
            else if (componentKey instanceof Class<?>)
            {
               Class<?> type = (Class<?>)componentKey;
               if (type.isAnnotation())
               {
                  stack.add(new DependencyByQualifier(type, bindType));
               }
               else
               {
                  stack.add(new DependencyByType(type));
               }
            }
            else
            {
               stack = null;
            }
         }
         instance = super.getComponentInstance(componentKey, bindType, autoRegistration);
      }
      finally
      {
         if (stack != null && !stack.isEmpty())
         {
            stack.removeLast();
         }
      }
      return instance;
   }
}
