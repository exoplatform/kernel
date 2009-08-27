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

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.test.BasicTestCase;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

import java.util.Comparator;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com Oct
 * 5, 2005
 */
public class SchedulerServiceTestBase extends BasicTestCase
{
   protected JobSchedulerService service_;

   static protected void resetTestEnvironment()
   {
      AJob.reset();
      GlobalTriggerListener.countTriggerComplete_ = 0;
   }

   static public class RunningJob extends BaseJob
   {
      static long SLEEP_TIME = 1000;

      public void execute(JobContext context) throws Exception
      {
         Thread.sleep(SLEEP_TIME);
      }
   }

   static public class GlobalTriggerListener extends BaseComponentPlugin implements TriggerListener
   {

      static int countTriggerFired_ = 0;

      static int countTriggerComplete_ = 0;

      public String getName()
      {
         return "GlobalTriggerListener";
      }

      public void triggerFired(Trigger arg0, JobExecutionContext arg1)
      {
         countTriggerFired_++;
         System.out.println("trigger fired.......................");
      }

      public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1)
      {
         return false;
      }

      public void triggerMisfired(Trigger arg0)
      {
         System.out.println("calll  TestJob.......................");
      }

      public void triggerComplete(Trigger arg0, JobExecutionContext arg1, int arg2)
      {
         countTriggerComplete_++;
         System.out.println("trigger complete.......................");
      }
   }

   static public class FirstTriggerListener extends BaseComponentPlugin implements TriggerListener
   {

      static int countTriggerFired_ = 0;

      static int countTriggerComplete_ = 0;

      public String getName()
      {
         return "FirstTriggerListener";
      }

      public void triggerFired(Trigger arg0, JobExecutionContext arg1)
      {
         countTriggerFired_++;
         System.out.println("trigger fired.......................");
      }

      public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1)
      {
         return false;
      }

      public void triggerMisfired(Trigger arg0)
      {
         System.out.println("calll  TestJob.......................");
      }

      public void triggerComplete(Trigger arg0, JobExecutionContext arg1, int arg2)
      {
         countTriggerComplete_++;
         System.out.println("trigger complete.......................");
      }
   }

   static public class SecondTriggerListener extends BaseComponentPlugin implements TriggerListener
   {

      static int countTriggerFired_ = 0;

      static int countTriggerComplete_ = 0;

      public String getName()
      {
         return "SecondTriggerListener";
      }

      public void triggerFired(Trigger arg0, JobExecutionContext arg1)
      {
         countTriggerFired_++;
         System.out.println("trigger fired.......................");
      }

      public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1)
      {
         return false;
      }

      public void triggerMisfired(Trigger arg0)
      {
         System.out.println("calll  TestJob.......................");
      }

      public void triggerComplete(Trigger arg0, JobExecutionContext arg1, int arg2)
      {
         countTriggerComplete_++;
         System.out.println("trigger complete.......................");
      }
   }

   static public class FirstJobListener extends BaseComponentPlugin implements JobListener
   {

      public String getName()
      {
         return "FirstJobListener";
      }

      public void jobToBeExecuted(JobExecutionContext arg0)
      {
      }

      public void jobExecutionVetoed(JobExecutionContext arg0)
      {
      }

      public void jobWasExecuted(JobExecutionContext arg0, JobExecutionException arg1)
      {
      }

   }

   static public class SecondJobListener extends BaseComponentPlugin implements JobListener
   {

      public String getName()
      {
         return "SecondJobListener";
      }

      public void jobToBeExecuted(JobExecutionContext arg0)
      {
      }

      public void jobExecutionVetoed(JobExecutionContext arg0)
      {
      }

      public void jobWasExecuted(JobExecutionContext arg0, JobExecutionException arg1)
      {
      }

   }

   static public class JobListenerComparator implements Comparator
   {

      public int compare(Object o1, Object o2)
      {
         JobListener j1 = (JobListener)o1;
         JobListener j2 = (JobListener)o2;
         if (j1.getName().equals(j2.getName()))
            return 0;
         return -1;
      }
   }

   static public class TriggerListenerComparator implements Comparator
   {

      public int compare(Object o1, Object o2)
      {
         TriggerListener t1 = (TriggerListener)o1;
         TriggerListener t2 = (TriggerListener)o2;
         if (t1.getName().equals(t2.getName()))
            return 0;
         return -1;
      }
   }

}
