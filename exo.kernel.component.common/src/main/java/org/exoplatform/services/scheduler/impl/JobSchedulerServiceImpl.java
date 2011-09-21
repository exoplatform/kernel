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

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.CronJob;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.services.scheduler.PeriodJob;
import org.exoplatform.services.scheduler.QueueTasks;
import org.exoplatform.services.scheduler.Task;
import org.picocontainer.Startable;
import org.quartz.CronTrigger;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa  Pham
 *          hoapham@exoplatform.com
 * Oct 5, 2005
 * 
 * @version $Id: JobSchedulerServiceImpl.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class JobSchedulerServiceImpl implements JobSchedulerService, Startable
{
   
   private static final Log log = ExoLogger.getLogger("exo.kernel.component.common.JobSchedulerServiceImpl");
   
   static final String STANDALONE_CONTAINER_NAME = "$Standalone";
   private final Scheduler scheduler_;

   private final String containerName_;

   private final QueueTasks qtasks_;

   public JobSchedulerServiceImpl(PortalContainerInfo pinfo, QuartzSheduler quartzSchduler, QueueTasks qtasks)
   {
      scheduler_ = quartzSchduler.getQuartzSheduler();
      containerName_ = pinfo.getContainerName();
      qtasks_ = qtasks;
   }

   /**
    * For run in Standalone container
    * 
    * @param quartzSchduler
    * @param qtasks
    */
   public JobSchedulerServiceImpl(QuartzSheduler quartzSchduler, QueueTasks qtasks)
   {
      scheduler_ = quartzSchduler.getQuartzSheduler();
      containerName_ = STANDALONE_CONTAINER_NAME;
      qtasks_ = qtasks;
   }

   public void queueTask(Task task)
   {
      qtasks_.add(task);
   }

   public void addJob(JobDetail job, Trigger trigger) throws Exception
   {
      scheduler_.scheduleJob(job, trigger);
   }

   public void addJob(JobInfo jinfo, Trigger trigger) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      JobDetail job = new JobDetail(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJob());
      scheduler_.scheduleJob(job, trigger);
   }

   public void addJob(JobInfo jinfo, Date date) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      SimpleTrigger trigger = new SimpleTrigger(jobinfo.getJobName(), jobinfo.getGroupName(), date);
      JobDetail job = new JobDetail(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJob());
      job.setDescription(jinfo.getDescription());
      scheduler_.scheduleJob(job, trigger);
   }

   public void addPeriodJob(JobInfo jinfo, int repeatCount, long period) throws Exception
   {
      int repeat;
      if (repeatCount < 0)
         repeat = SimpleTrigger.REPEAT_INDEFINITELY;
      else if (repeatCount == 0)
         repeat = SimpleTrigger.REPEAT_INDEFINITELY;
      else
         repeat = repeatCount - 1;
      JobInfo jobinfo = getJobInfo(jinfo);
      SimpleTrigger trigger = new SimpleTrigger(jobinfo.getJobName(), jobinfo.getGroupName(), repeat, period);
      JobDetail job = new JobDetail(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJob());
      job.setDescription(jobinfo.getDescription());
      scheduler_.scheduleJob(job, trigger);
   }

   public void addPeriodJob(JobInfo jinfo, PeriodInfo pinfo) throws Exception
   {
      int repeat = pinfo.getRepeatCount();
      Date start = pinfo.getStartTime();
      JobInfo jobinfo = getJobInfo(jinfo);
      if (start == null)
         start = new Date();
      if (repeat <= 0)
         repeat = SimpleTrigger.REPEAT_INDEFINITELY;
      else
         repeat = repeat - 1;
      SimpleTrigger trigger =
         new SimpleTrigger(jobinfo.getJobName(), jobinfo.getGroupName(), start, pinfo.getEndTime(), repeat, pinfo
            .getRepeatInterval());
      JobDetail job = new JobDetail(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJob());
      job.setDescription(jobinfo.getDescription());
      scheduler_.scheduleJob(job, trigger);
   }

   public void addPeriodJob(ComponentPlugin plugin) throws Exception
   {
      PeriodJob pjob = (PeriodJob)plugin;
      addPeriodJob(pjob.getJobInfo(), pjob.getPeriodInfo(), pjob.getJobDataMap());
   }

   public void addCronJob(JobInfo jinfo, String exp) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      CronTrigger trigger =
         new CronTrigger(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJobName(), jobinfo.getGroupName(),
         exp);
      JobDetail job = new JobDetail(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJob());
      job.setDescription(jobinfo.getDescription());
      scheduler_.addJob(job, true);
      scheduler_.scheduleJob(trigger);
   }

   public void addCronJob(ComponentPlugin plugin) throws Exception
   {
      CronJob cjob = (CronJob)plugin;
      addCronJob(cjob.getJobInfo(),cjob.getExpression(), cjob.getJobDataMap());

   }

   public void addCronJob(JobInfo jinfo, String exp, JobDataMap jdatamap) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      CronTrigger trigger =
         new CronTrigger(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJobName(), jobinfo.getGroupName(),
         exp);
      JobDetail job = new JobDetail(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJob());
      job.setJobDataMap(jdatamap);
      job.setDescription(jobinfo.getDescription());
      scheduler_.addJob(job, true);
      scheduler_.scheduleJob(trigger);
   }

   public void addPeriodJob(JobInfo jinfo, PeriodInfo pinfo, JobDataMap jdatamap) throws Exception
   {
      int repeat = pinfo.getRepeatCount();
      Date start = pinfo.getStartTime();
      JobInfo jobinfo = getJobInfo(jinfo);
      if (start == null)
         start = new Date();
      if (repeat <= 0)
         repeat = SimpleTrigger.REPEAT_INDEFINITELY;
      else
         repeat = repeat - 1;
      SimpleTrigger trigger =
         new SimpleTrigger(jobinfo.getJobName(), jobinfo.getGroupName(), start, pinfo.getEndTime(), repeat, pinfo
            .getRepeatInterval());
      JobDetail job = new JobDetail(jobinfo.getJobName(), jobinfo.getGroupName(), jobinfo.getJob());
      job.setJobDataMap(jdatamap);
      job.setDescription(jobinfo.getDescription());
      scheduler_.scheduleJob(job, trigger);
   }

   public boolean removeJob(JobInfo jinfo) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      return scheduler_.deleteJob(jobinfo.getJobName(), jobinfo.getGroupName());
   }

   public List getAllExcutingJobs() throws Exception
   {
      return scheduler_.getCurrentlyExecutingJobs();
   }

   public List getAllJobs() throws Exception
   {
      List jlist = new ArrayList();
      String jgroup[] = scheduler_.getJobGroupNames();
      for (int i = 1; i < jgroup.length; i++)
      {
         String jname[] = scheduler_.getJobNames(jgroup[i]);
         for (int j = 0; j < jname.length; j++)
         {
            jlist.add(scheduler_.getJobDetail(jname[j], jgroup[i]));
         }
      }
      return jlist;
   }

   public void addGlobalJobListener(ComponentPlugin plugin) throws Exception
   {
      scheduler_.addGlobalJobListener((JobListener)plugin);
   }

   public List getAllGlobalJobListener() throws Exception
   {
      return scheduler_.getGlobalJobListeners();
   }

   public JobListener getGlobalJobListener(String name) throws Exception
   {
      JobListener jlistener;
      List listener = scheduler_.getGlobalJobListeners();
      ListIterator iterator = listener.listIterator();
      while (iterator.hasNext())
      {
         jlistener = (JobListener)iterator.next();
         if (jlistener.getName().equals(name))
         {
            return jlistener;
         }
      }
      return null;
   }

   public boolean removeGlobalJobListener(String name) throws Exception
   {
      JobListener jlistener = getGlobalJobListener(name);
      return scheduler_.removeGlobalJobListener(jlistener);
   }

   public void addJobListener(ComponentPlugin plugin) throws Exception
   {
      scheduler_.addJobListener((JobListener)plugin);
   }

   public List getAllJobListener() throws Exception
   {
      Set jlNames = scheduler_.getJobListenerNames();
      List jlisteners = new ArrayList();
      if (jlNames.isEmpty())
         return jlisteners;
      for (Object obj : jlNames.toArray())
      {
         jlisteners.add(getJobListener(obj.toString()));
      }
      return jlisteners;
   }

   public JobListener getJobListener(String name) throws Exception
   {
      return scheduler_.getJobListener(name);
   }

   public boolean removeJobListener(String name) throws Exception
   {
      return scheduler_.removeJobListener(name);
   }

   public void addGlobalTriggerListener(ComponentPlugin plugin) throws Exception
   {
      scheduler_.addGlobalTriggerListener((TriggerListener)plugin);
   }

   public List getAllGlobalTriggerListener() throws Exception
   {
      return scheduler_.getGlobalTriggerListeners();
   }

   public TriggerListener getGlobalTriggerListener(String name) throws Exception
   {
      TriggerListener tlistener;
      List listener = scheduler_.getGlobalTriggerListeners();
      ListIterator iterator = listener.listIterator();
      while (iterator.hasNext())
      {
         tlistener = (TriggerListener)iterator.next();
         if (tlistener.getName().equals(name))
         {
            return tlistener;
         }
      }
      return null;
   }

   public boolean removeGlobaTriggerListener(String name) throws Exception
   {
      TriggerListener tlistener = getGlobalTriggerListener(name);
      return scheduler_.removeGlobalTriggerListener(tlistener);
   }

   public void addTriggerListener(ComponentPlugin plugin) throws Exception
   {
      scheduler_.addTriggerListener((TriggerListener)plugin);
   }

   public List getAllTriggerListener() throws Exception
   {
      List tlisteners = new ArrayList();
      Set tlistenerNames = scheduler_.getTriggerListenerNames();
      if (tlistenerNames.isEmpty())
         return tlisteners;
      for (Object obj : tlistenerNames.toArray())
      {
         tlisteners.add(getTriggerListener(obj.toString()));
      }
      return tlisteners;
   }

   public TriggerListener getTriggerListener(String name) throws Exception
   {
      return scheduler_.getTriggerListener(name);
   }

   public boolean removeTriggerListener(String name) throws Exception
   {
      return scheduler_.removeTriggerListener(name);
   }

   private JobInfo getJobInfo(JobInfo jinfo) throws Exception
   {
      String gname;
      if (jinfo.getGroupName() == null)
         gname = containerName_;
      else
         gname = containerName_ + ":" + jinfo.getGroupName();
      JobInfo jobInfo = new JobInfo(jinfo.getJobName(), gname, jinfo.getJob());
      jobInfo.setDescription(jinfo.getDescription());
      return jobInfo;
   }

   public void pauseJob(String jobName, String groupName) throws Exception
   {
      scheduler_.pauseJob(jobName, groupName);
   }

   public void resumeJob(String jobName, String groupName) throws Exception
   {
      scheduler_.resumeJob(jobName, groupName);
   }

   public void executeJob(String jname, String jgroup, JobDataMap jdatamap) throws Exception
   {
      scheduler_.triggerJobWithVolatileTrigger(jname, jgroup, jdatamap);
   }

   public Trigger[] getTriggersOfJob(String jobName, String groupName) throws Exception
   {
      return scheduler_.getTriggersOfJob(jobName, groupName);
   }

   public int getTriggerState(String triggerName, String triggerGroup) throws Exception
   {
      return scheduler_.getTriggerState(triggerName, triggerGroup);
   }

   public Date rescheduleJob(String triggerName, String groupName, Trigger newTrigger) throws SchedulerException
   {
      return scheduler_.rescheduleJob(triggerName, groupName, newTrigger);
   }

   @Managed
   @ManagedDescription("Suspend all the existing jobs")
   public boolean suspend()
   {
      try
      {
         scheduler_.standby();
         return true;
      }
      catch (SchedulerException e)
      {
         log.error("Could not suspend the scheduler", e);
      }
      return false;
   }

   @Managed
   @ManagedDescription("Resume all the existing jobs")   
   public boolean resume()
   {
      try
      {
         scheduler_.start();
         return true;
      }
      catch (SchedulerException e)
      {
         log.error("Could not resume the scheduler", e);
      }
      return false;
   }
   
   public void start()
   {
      try
      {
         // Ensure that only one JobEnvironmentConfigListener will be registered
         while (removeGlobalJobListener(JobEnvironmentConfigListener.NAME));
         // Add the unique instance of JobEnvironmentConfigListener
         scheduler_.addGlobalJobListener(new JobEnvironmentConfigListener());
      }
      catch (Exception e)
      {
         log.warn("Could not remove the GlobalJobListener " + JobEnvironmentConfigListener.NAME, e);
      }
   }

   public void stop()
   {
      try
      {
         List jobs = getAllExcutingJobs();
         for (Object object : jobs)
         {
            JobExecutionContext ctx = (JobExecutionContext)object;
            Job job = ctx.getJobInstance();
            if (job instanceof InterruptableJob)
            {
               ((InterruptableJob)job).interrupt();
            }
         }
         scheduler_.shutdown(true);
      }
      catch (Exception ex)
      {
         log.warn("Could not interrupt all the current jobs properly", ex);
      }
   }

   public JobDetail getJob(JobInfo jobInfo) throws Exception
   {
      JobInfo innerJobInfo = getJobInfo(jobInfo);
      return scheduler_.getJobDetail(innerJobInfo.getJobName(), innerJobInfo.getGroupName());
   }
}
