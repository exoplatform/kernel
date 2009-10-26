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
package org.exoplatform.services.scheduler;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham
 * hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com Oct 6, 2005
 */
public class JobInfo
{
   private String jobName_;

   private String groupName_;

   private String description_;

   private Class job_;

   public JobInfo(Class job)
   {
      this(null, null, job);
   }

   public JobInfo(String jobName, String groupName, Class job)
   {
      job_ = job;
      if (jobName == null)
         jobName_ = Integer.toString(this.hashCode());
      else
         jobName_ = jobName;
      groupName_ = groupName;
   }

   public String getJobName()
   {
      return jobName_;
   }

   public void setJobName(String name)
   {
      jobName_ = name;
   }

   public String getGroupName()
   {
      return groupName_;
   }

   public void setGroupName(String name)
   {
      groupName_ = name;
   }

   public String getDescription()
   {
      return description_;
   }

   public void setDescription(String s)
   {
      description_ = s;
   }

   public Class getJob()
   {
      return job_;
   }

}
