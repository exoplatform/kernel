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
package org.exoplatform.services.scheduler.impl;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;
import org.exoplatform.services.scheduler.QueueTasks;
import org.exoplatform.services.scheduler.Task;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham
 * hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com Oct 7, 2005
 * 
 * @version $Id: QueueTaskJob.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class QueueTaskJob extends BaseJob
{
   public void execute(JobContext context) throws Exception
   {
      PortalContainer manager = PortalContainer.getInstance();
      QueueTasks qtasks = (QueueTasks)manager.getComponentInstanceOfType(QueueTasks.class);
      Task task = qtasks.poll();
      while (task != null)
      {
         try
         {
            task.execute();
         }
         catch (Exception ex)
         {
            ex.printStackTrace();
         }
         task = qtasks.poll();
      }
   }
}
