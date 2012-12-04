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

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Oct 6, 2005
 */
public class PeriodJob extends BaseComponentPlugin
{
   private PeriodInfo pjinfo_;

   private JobInfo jinfo_;

   public PeriodJob(InitParams params) throws Exception
   {
      ExoProperties props = params.getPropertiesParam("job.info").getProperties();

      String jobName = props.getProperty("jobName");
      String jobGroup = props.getProperty("groupName");
      String jobClass = props.getProperty("job");
      Class<?> clazz = ClassLoading.forName(jobClass, this);
      jinfo_ = new JobInfo(jobName, jobGroup, clazz);

      Date startTime = getDate(props.getProperty("startTime"));
      Date endTime = getDate(props.getProperty("endTime"));
      int repeatCount = Integer.parseInt(props.getProperty("repeatCount"));
      long repeatInterval = Integer.parseInt(props.getProperty("period"));
      pjinfo_ = new PeriodInfo(startTime, endTime, repeatCount, repeatInterval);
   }

   private Date getDate(String stime) throws Exception
   {
      Date date = null;
      if (stime == null || stime.equals(""))
      {
         return date;
      }
      else if (stime.startsWith("+"))
      {
         long val = Long.parseLong(stime.substring(1));
         date = new Date(System.currentTimeMillis() + val);
      }
      else
      {
         SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
         date = ft.parse(stime);
      }
      return date;
   }

   public JobInfo getJobInfo()
   {
      return jinfo_;
   }

   public PeriodInfo getPeriodInfo()
   {
      return pjinfo_;
   }

   public JobDataMap getJobDataMap()
   {
      return null;
   }
}
