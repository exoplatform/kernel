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

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class CyclicDependencyException extends RuntimeException
{

   /**
    * The serial version id
    */
   private static final long serialVersionUID = 9138676186744680652L;

   /**
    * The dependency that causes the exception
    */
   private final ComponentTaskContextEntry entry;
   
   /**
    * Indicates whether the cycle of dependencies that causes this issue was of same type 
    */
   private final boolean sameType;

   public CyclicDependencyException(ComponentTaskContextEntry entry, boolean sameType)
   {
      super("The component corresponding to the key '" + entry.getComponentKey() + "' is already registered as a "
         + entry.getTaskType() + " dependency");
      this.entry = entry;
      this.sameType = sameType;
   }

   /**
    * @return the key of the dependency that causes the issue
    */
   public Object getComponentKey()
   {
      return entry.getComponentKey();
   }

   /**
    * @return the type of the task for which this dependency is needed.
    */
   public ComponentTaskType getTaskType()
   {
      return entry.getTaskType();
   }

   /**
    * @return the sameType
    */
   public boolean isSameType()
   {
      return sameType;
   }
}
