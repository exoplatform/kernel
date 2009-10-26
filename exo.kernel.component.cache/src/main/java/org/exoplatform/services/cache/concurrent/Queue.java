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
package org.exoplatform.services.cache.concurrent;

import java.util.ArrayList;

/**
 * The queue needed by the concurrent FIFO cache.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public interface Queue<I extends Item>
{

   /**
    * Returns the queue size
    * @return the size
    */
   public int size();

   /**
    * Attempt to remove an item from the queue.
    *
    * @param item the item to remove
    * @return true if the item was removed by this thread
    */
   public boolean remove(I item);

   /**
    * Add the item to the head of the list.
    *
    * @param item the item to add
    */
   public void add(I item);

   /**
    * Attempt to trim the queue. Trim will occur if no other thread is already performing a trim
    * and the queue size is greater than the provided size.
    *
    * @param size the wanted size
    * @return the list of evicted items
    */
   public ArrayList<I> trim(int size);
}
