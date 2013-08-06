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
package org.exoplatform.container.context;

import java.util.Set;

import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;

/**
 * This class defined all the main methods needed to be able to store and/or access to 
 * a {@link CreationalContext} and its content
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface CreationContextStorage
{
   /**
    * Gives the id of the storage. Two {@link CreationContextStorage} corresponding to the same context must
    * have the same id.
    */
   String getId();

   /**
    * Stores an instance for the given {@link Contextual} id
    * @param id the id of the contextual for which we want to set a value
    * @param creationContext the {@link CreationContext} used for the instance creation
    * @param instance the value to set
    * @return the instance that has been effectively set.
    */
   <T> T setInstance(String id, CreationContext<T> creationContext);

   /**
    * Gives the {@link CreationContext} corresponding to the given {@link Contextual} id
    * @param id the id of the contextual for which we want to retrieve the {@link CreationContext}
    * @return the corresponding {@link CreationalContext} if it exists, <code>null</code> otherwise
    */
   <T> CreationContext<T> getCreationContext(String id);

   /**
    * Removes from the storage the instance corresponding to the given {@link Contextual} id
    * @param id the id of the contextual for which we want to remove the instance
    */
   void removeInstance(String id);

   /**
    * Gives all the {@link Contextual} ids that have been stored
    * @return a {@link Set} containing all the ids that have been used to store a {@link CreationContext}
    */
   Set<String> getAllIds();
}
