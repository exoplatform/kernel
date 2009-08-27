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
package org.exoplatform.services.threadpool.impl;

/**
 * $Id: Queue.java 5332 2006-04-29 18:32:44Z geaz $
 *
 * The contents of this file are subject to the ClickBlocks Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.clickblocks.org
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied, including, but not limited to, the implied warranties of
 * merchantability, fitness for a particular purpose and
 * non-infringement. See the License for the specific language
 * governing rights and limitations under the License.
 * 
 * ClickBlocks, the ClickBlocks logo and combinations thereof are
 * trademarks of ClickBlocks, LLC in the United States and other
 * countries.
 * 
 * The Initial Developer of the Original Code is ClickBlocks, LLC.
 * Portions created by ClickBlocks, LLC are Copyright (C) 2000.  
 * All Rights Reserved.
 *
 * Contributor(s): Mark Grand
 */

import java.util.ArrayList;

public class Queue
{
   private ArrayList data = new ArrayList();

   private int maxQueueSize = Integer.MAX_VALUE;

   /**
    * Put an object on the end of the queue. If the size of the queue is equal to
    * or greater than the current value of maxQueueSize then this method will
    * wait until the size of the queue shrinks to less than maxQueueSize.
    * 
    * @param obj the object to put at end of queue
    */
   synchronized public void put(Object obj) throws InterruptedException
   {
      if (Thread.currentThread().isInterrupted())
         throw new InterruptedException();
      if (obj == null)
         throw new IllegalArgumentException("null");
      while (data.size() >= maxQueueSize)
      {
         try
         {
            wait();
         }
         catch (InterruptedException e)
         {
            return;
         } // try
      } // while
      data.add(obj);
      notify();
   } // put(Object)

   /**
    * Put an object on the end of the queue. If the size of the queue is equal to
    * or greater than the current value of maxQueueSize then this method will
    * wait until the size of the queue shrinks to less than maxQueueSize.
    * 
    * @param obj the object to put at end of queue
    * @param msecs If this method has to wait for the size of the queue to shrink
    *          to less than maxQueueSize, it will stop waiting after it has
    *          waited this many milliseconds.
    * @return false if this method returns without queuing the given object
    *         becuase it had to wait msec milliseconds; otherwise true.
    */
   synchronized public boolean put(Object obj, long msecs) throws InterruptedException
   {
      if (Thread.currentThread().isInterrupted())
         throw new InterruptedException();
      if (obj == null)
         throw new IllegalArgumentException("null");
      long startTime = System.currentTimeMillis();
      long waitTime = msecs;
      while (data.size() >= maxQueueSize)
      {
         waitTime = msecs - (System.currentTimeMillis() - startTime);
         if (waitTime <= 0)
            return false;
         wait(waitTime);
      } // while
      data.add(obj);
      notify();
      return true;
   } // put(Object, long)

   /**
    * Get an object from the front of the queue. If queue is empty, waits until
    * it is not empty.
    */
   synchronized public Object get() throws InterruptedException
   {
      while (data.size() == 0)
      {
         wait();
      } // while
      Object obj = data.remove(0);
      notify();
      return obj;
   } // get()

   /**
    * Get an object from the front of the queue. If queue is empty, waits until
    * it is not empty.
    * 
    * @param msecs The maximum number of milliseconds that this method should
    *          wait before giving up.
    * @return The object at the front of the queue or null if there are no
    *         objects in the queue and the method has waited at least the given
    *         number of milliseconds for an object to be put in the queue.
    */
   synchronized public Object get(long msecs) throws InterruptedException
   {
      long startTime = System.currentTimeMillis();
      long waitTime = msecs;

      if (data.size() > 0)
         return data.remove(0);
      while (true)
      {
         waitTime = msecs - (System.currentTimeMillis() - startTime);
         if (waitTime <= 0)
            return null;
         wait(waitTime);
         if (data.size() > 0)
         {
            Object obj = data.remove(0);
            notify();
            return obj;
         } // if data.size()
      } // while
   } // get(long)

   /**
    * Set the maximum queue size.
    */
   public void setMaxQueueSize(int newValue)
   {
      maxQueueSize = newValue;
   }
}
