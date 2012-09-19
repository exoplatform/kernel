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

import junit.framework.TestCase;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.services.scheduler.AddJobListenerComponentPlugin;
import org.exoplatform.services.scheduler.AddTriggerListenerComponentPlugin;
import org.exoplatform.services.scheduler.BaseJob;
import org.exoplatform.services.scheduler.JobContext;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com Oct
 * 5, 2005
 */
public class SchedulerServiceTestBase extends TestCase
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

   static public class GlobalJobListener extends BaseComponentPlugin implements JobListener
   {
      static int countCalled_ = 0;
      
      public String getName()
      {
         return "GlobalJobListener";
      }

      public void jobToBeExecuted(JobExecutionContext context)
      {
      }

      public void jobExecutionVetoed(JobExecutionContext context)
      {
      }

      public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException)
      {
         countCalled_++;
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
      }

      public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1)
      {
         return false;
      }

      public void triggerMisfired(Trigger arg0)
      {
      }

      public void triggerComplete(Trigger trigger, JobExecutionContext context,
         CompletedExecutionInstruction triggerInstructionCode)
      {
         countTriggerComplete_++;
      }
   }

   static public class FirstTriggerListener extends AddTriggerListenerComponentPlugin
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
      }

      public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1)
      {
         return false;
      }

      public void triggerMisfired(Trigger arg0)
      {
      }

      public void triggerComplete(Trigger trigger, JobExecutionContext context,
         CompletedExecutionInstruction triggerInstructionCode)
      {
         countTriggerComplete_++;
      }
   }

   static public class SecondTriggerListener extends AddTriggerListenerComponentPlugin
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
      }

      public boolean vetoJobExecution(Trigger arg0, JobExecutionContext arg1)
      {
         return false;
      }

      public void triggerMisfired(Trigger arg0)
      {
      }

      public void triggerComplete(Trigger trigger, JobExecutionContext context,
         CompletedExecutionInstruction triggerInstructionCode)
      {
         countTriggerComplete_++;
      }
   }

   static public class FirstJobListener extends AddJobListenerComponentPlugin
   {
      static int countCalled_ = 0;

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
         countCalled_++;
      }
   }

   static public class SecondJobListener extends AddJobListenerComponentPlugin
   {

      static int countCalled_ = 0;
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
         countCalled_++;
      }

   }

   static public class JobListenerComparator implements Comparator<JobListener>
   {

      public int compare(JobListener j1, JobListener j2)
      {
         if (j1.getName().equals(j2.getName()))
            return 0;
         return -1;
      }
   }

   static public class TriggerListenerComparator implements Comparator<TriggerListener>
   {

      public int compare(TriggerListener t1, TriggerListener t2)
      {
         if (t1.getName().equals(t2.getName()))
            return 0;
         return -1;
      }
   }

   protected static void hasObjectInCollection(Object obj, Collection c, Comparator comparator) throws Exception
   {
      Iterator iter = c.iterator();
      while (iter.hasNext())
      {
         Object o = iter.next();
         if (comparator.compare(obj, o) == 0)
            return;
      }
      throw new Exception("Object " + obj + " hasn't in collection " + c);
   }
}
