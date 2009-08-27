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
package org.exoplatform.management;

/**
 * A context for managed objects that wants to do more.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface ManagementContext
{

   /**
    * Register an object as a managed object.
    *
    * @param o the object to be managed
    * @throws IllegalArgumentException if the object is not manageable
    * @throws NullPointerException if the object is null
    */
   void register(Object o) throws IllegalArgumentException, NullPointerException;

   /**
    * Unregisters an object from its managed life cycle.
    *
    * @param o the object to be unmanaged
    * @throws IllegalArgumentException if the object is not manageable
    * @throws NullPointerException if the object is null
    */
   void unregister(Object o) throws IllegalArgumentException, NullPointerException;

}
