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

import javax.enterprise.context.spi.AlterableContext;

/**
 * This interface defines all the additional methods needed to easily implement 
 * an {@link AlterableContext}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface AdvancedContext<K> extends AlterableContext
{
   /**
    * Registers a new key to the context
    * @param key the key to register
    */
   void register(K key);

   /**
    * Unregisters a given key from the context
    * @param key the key to unregister
    */
   void unregister(K key);

   /**
    * Activates the current context using the given key within the context of the thread
    * @param key the key to use to activate the context
    */
   void activate(K key);

   /**
    * Deactivates the current context using the given key from the context of the thread
    * @param key the key to use to deactivate the context
    */
   void deactivate(K key);
}
