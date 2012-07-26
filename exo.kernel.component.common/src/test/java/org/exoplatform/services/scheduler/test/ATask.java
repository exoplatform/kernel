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
package org.exoplatform.services.scheduler.test;

import org.exoplatform.services.scheduler.Task;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham
 * hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com Oct 7, 2005
 */
public class ATask extends Task
{
   private final AtomicInteger counter;
   
   public ATask(AtomicInteger counter)
   {
      this.counter = counter;
   }

   public void execute() throws Exception
   {
      synchronized (counter)
      {
         if (counter.decrementAndGet() == 0)
         {
            counter.notify();
         }
      }
   }
}
