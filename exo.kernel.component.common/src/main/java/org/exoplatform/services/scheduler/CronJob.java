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

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.quartz.JobDataMap;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham
 * hoapham@exoplatform.com,phamvuxuanhoa@yahoo.com Oct 10, 2005
 */
public class CronJob extends BaseComponentPlugin
{
   private String expression_;

   private JobInfo jinfo_;

   public CronJob(InitParams params) throws Exception
   {
      ExoProperties props = params.getPropertiesParam("cronjob.info").getProperties();

      String jobName = props.getProperty("jobName");
      String jobGroup = props.getProperty("groupName");
      String jobClass = props.getProperty("job");
      Class<?> clazz = ClassLoading.forName(jobClass, this);
      jinfo_ = new JobInfo(jobName, jobGroup, clazz);

      expression_ = props.getProperty("expression");
   }

   public JobInfo getJobInfo()
   {
      return jinfo_;
   }

   public String getExpression()
   {
      return expression_;
   }
   
   public JobDataMap getJobDataMap()
   {
      return null;
   }

}
