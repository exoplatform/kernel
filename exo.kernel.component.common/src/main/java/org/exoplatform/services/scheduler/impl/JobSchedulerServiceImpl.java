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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.multitenancy.CurrentTenantNotSetException;
import org.exoplatform.container.multitenancy.Tenant;
import org.exoplatform.container.multitenancy.TenantsService;
import org.exoplatform.container.multitenancy.TenantsStateListener;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.scheduler.AddJobListenerComponentPlugin;
import org.exoplatform.services.scheduler.AddTriggerListenerComponentPlugin;
import org.exoplatform.services.scheduler.CronJob;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.exoplatform.services.scheduler.PeriodJob;
import org.exoplatform.services.scheduler.QueueTasks;
import org.exoplatform.services.scheduler.Task;
import org.picocontainer.Startable;
import org.quartz.CronScheduleBuilder;
import org.quartz.InterruptableJob;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Matcher;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.TriggerListener;
import org.quartz.impl.matchers.EverythingMatcher;
import org.quartz.impl.matchers.GroupMatcher;
import org.quartz.impl.matchers.KeyMatcher;

/**
 * Created by The eXo Platform SAS
 * Author : Hoa Pham
 * hoapham@exoplatform.com
 * Oct 5, 2005
 * 
 * @version $Id: JobSchedulerServiceImpl.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class JobSchedulerServiceImpl implements JobSchedulerService, Startable
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.JobSchedulerServiceImpl");
   
   static final String STANDALONE_CONTAINER_NAME = "$Standalone";

   private final Scheduler scheduler_;

   private final String containerName_;

   private final QueueTasks qtasks_;

   private final TenantsService tenantsService;
   
   protected final Tenant        jobTenant;

   public JobSchedulerServiceImpl(PortalContainerInfo pinfo, QuartzSheduler quartzSchduler, QueueTasks qtasks,
      TenantsService tService, ExoContainerContext context)
   {
      scheduler_ = quartzSchduler.getQuartzSheduler();
      containerName_ = pinfo.getContainerName();
      qtasks_ = qtasks;
      this.tenantsService = tService;
      Tenant tenant;
      try {
        tenant = tenantsService.getCurrentTanant();
      } catch (CurrentTenantNotSetException e) {
        tenant = null;
      }
      this.jobTenant = tenant;
      if (jobTenant != null){
        addTenantStartingTriggerListener(context);
      }
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
      tenantsService = null;
      this.jobTenant = null;
   }

   private void addTenantStartingTriggerListener(ExoContainerContext context){
     //Add trigger listener for all starting tenant's jobs
     try {
       TenantStartingTriggerListener listener = new TenantStartingTriggerListener(jobTenant.getName());
       addGlobalTriggerListener(listener);
       LOG.debug("Add trigger listener for starting tenant {}",jobTenant.getName());
     } catch (Exception e1) {
       LOG.warn("Can't add trigger listener for {}",jobTenant.getName());
     }
     
     //Remove trigger listener when tenant is online, all components are created
     context.getContainer().addContainerLifecylePlugin(new BaseContainerLifecyclePlugin()
     {       
        @Override
        public void startContainer(ExoContainer container) throws Exception
        {
          LOG.debug("Remove trigger listener for online tenant {}",jobTenant.getName());
          removeGlobaTriggerListener(TenantStartingTriggerListener.createName(jobTenant.getName()));
        }        
     });
   }
   
   protected boolean isTenantJob(JobDetail jobDetail) {
     if (jobTenant != null) {
       String tenantName = jobTenant.getName();

       String[] elements = jobDetail.getKey().getGroup().split(":");
       if (elements.length == 3 && tenantName.equals(elements[1])) {
         return true;
       }
     }
     return false;
   }
   
   public void queueTask(Task task)
   {
      qtasks_.add(task);
   }

   public void addJob(JobDetail job, Trigger trigger) throws Exception
   {
      String gname = getGroupName(job.getKey().getGroup());
      trigger = trigger.getTriggerBuilder().withIdentity(job.getKey().getName(), gname).build();
      scheduleJob(job.getJobBuilder().withIdentity(job.getKey().getName(), gname).build(), trigger);
   }

   public void addJob(JobInfo jinfo, Trigger trigger) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      trigger = trigger.getTriggerBuilder().withIdentity(jobinfo.getJobName(), jobinfo.getGroupName()).build();
      @SuppressWarnings("unchecked")
      JobDetail job =
         JobBuilder.newJob(jobinfo.getJob()).withIdentity(jobinfo.getJobName(), jobinfo.getGroupName()).build();
      scheduleJob(job, trigger);
   }

   public void addJob(JobInfo jinfo, Date date) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      Trigger trigger =
         TriggerBuilder.newTrigger().withIdentity(jobinfo.getJobName(), jobinfo.getGroupName()).startAt(date).build();
      @SuppressWarnings("unchecked")
      JobDetail job =
         JobBuilder.newJob(jobinfo.getJob()).withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withDescription(jinfo.getDescription()).build();
      scheduleJob(job, trigger);
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
      Trigger trigger =
         TriggerBuilder
            .newTrigger()
            .withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withSchedule(
               SimpleScheduleBuilder.simpleSchedule().withRepeatCount(repeat).withIntervalInMilliseconds(period))
            .build();
      @SuppressWarnings("unchecked")
      JobDetail job =
         JobBuilder.newJob(jobinfo.getJob()).withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withDescription(jinfo.getDescription()).build();
      scheduleJob(job, trigger);
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
      Trigger trigger =
         TriggerBuilder
            .newTrigger()
            .withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withSchedule(
               SimpleScheduleBuilder.simpleSchedule().withRepeatCount(repeat)
                  .withIntervalInMilliseconds(pinfo.getRepeatInterval())).startAt(start).endAt(pinfo.getEndTime())
            .build();
      @SuppressWarnings("unchecked")
      JobDetail job =
         JobBuilder.newJob(jobinfo.getJob()).withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withDescription(jinfo.getDescription()).build();
      scheduleJob(job, trigger);
   }

   public void addPeriodJob(ComponentPlugin plugin) throws Exception
   {
      PeriodJob pjob = (PeriodJob)plugin;
      try
      {
         addPeriodJob(pjob.getJobInfo(), pjob.getPeriodInfo(), pjob.getJobDataMap());
      }
      catch (Exception e)
      {
         LOG.warn("Could not add the period job (" + pjob.getJobInfo().getJobName() + ", "
            + pjob.getJobInfo().getGroupName() + ") defined in the plugin " + plugin.getName() + " : " + e.getMessage());
      }
   }

   public void addCronJob(JobInfo jinfo, String exp) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      Trigger trigger =
         TriggerBuilder.newTrigger().withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .forJob(jobinfo.getJobName(), jobinfo.getGroupName()).withSchedule(CronScheduleBuilder.cronSchedule(exp))
            .build();
      @SuppressWarnings("unchecked")
      JobDetail job =
         JobBuilder.newJob(jobinfo.getJob()).withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withDescription(jinfo.getDescription()).build();
      scheduleJob(job, trigger);
   }

   public void addCronJob(ComponentPlugin plugin) throws Exception
   {
      CronJob cjob = (CronJob)plugin;
      try
      {
         addCronJob(cjob.getJobInfo(), cjob.getExpression(), cjob.getJobDataMap());
      }
      catch (Exception e)
      {
         LOG.warn("Could not add the cron job (" + cjob.getJobInfo().getJobName() + ", "
            + cjob.getJobInfo().getGroupName() + ") defined in the plugin " + plugin.getName() + " : " + e.getMessage());
      }
   }

   public void addCronJob(JobInfo jinfo, String exp, JobDataMap jdatamap) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      Trigger trigger =
         TriggerBuilder.newTrigger().withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .forJob(jobinfo.getJobName(), jobinfo.getGroupName()).withSchedule(CronScheduleBuilder.cronSchedule(exp))
            .build();
      @SuppressWarnings("unchecked")
      JobBuilder jb =
         JobBuilder.newJob(jobinfo.getJob()).withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withDescription(jinfo.getDescription());
      JobDetail job = jdatamap == null ? jb.build() : jb.usingJobData(jdatamap).build();
      scheduleJob(job, trigger);
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
      Trigger trigger =
         TriggerBuilder
            .newTrigger()
            .withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withSchedule(
               SimpleScheduleBuilder.simpleSchedule().withRepeatCount(repeat)
                  .withIntervalInMilliseconds(pinfo.getRepeatInterval())).startAt(start).endAt(pinfo.getEndTime())
            .build();
      @SuppressWarnings("unchecked")
      JobBuilder jb =
         JobBuilder.newJob(jobinfo.getJob()).withIdentity(jobinfo.getJobName(), jobinfo.getGroupName())
            .withDescription(jinfo.getDescription());
      JobDetail job = jdatamap == null ? jb.build() : jb.usingJobData(jdatamap).build();
      scheduleJob(job, trigger);
   }
   
   protected void scheduleJob(JobDetail job, Trigger trigger) throws SchedulerException{
     scheduler_.scheduleJob(job, trigger);
   }

   public boolean removeJob(JobInfo jinfo) throws Exception
   {
      JobInfo jobinfo = getJobInfo(jinfo);
      return scheduler_.deleteJob(JobKey.jobKey(jobinfo.getJobName(), jobinfo.getGroupName()));
   }

   public List<JobExecutionContext> getAllExcutingJobs() throws Exception
   {
      return scheduler_.getCurrentlyExecutingJobs();
   }

   public List<JobDetail> getAllJobs() throws Exception
   {
      List<JobDetail> jlist = new ArrayList<JobDetail>();
      List<String> jgroups = scheduler_.getJobGroupNames();
      for (int i = 0, length = jgroups.size(); i < length; i++)
      {
         Set<JobKey> jkeys = scheduler_.getJobKeys(GroupMatcher.jobGroupEquals(jgroups.get(i)));
         for (JobKey jkey : jkeys)
         {
            jlist.add(scheduler_.getJobDetail(jkey));
         }
      }
      return jlist;
   }

   public void addGlobalJobListener(ComponentPlugin plugin) throws Exception
   {
      JobListener jl = (JobListener)plugin;
      try
      {
         scheduler_.getListenerManager().addJobListener(jl);
      }
      catch (Exception e)
      {
         LOG.warn("Could not add the global job listener (" + jl.getName() + ") defined in the plugin "
            + plugin.getName() + " : " + e.getMessage());
      }
   }

   public List<JobListener> getAllGlobalJobListener() throws Exception
   {
      List<JobListener> listeners = scheduler_.getListenerManager().getJobListeners();
      List<JobListener> result = new ArrayList<JobListener>();
      for (JobListener l : listeners)
      {
         List<Matcher<JobKey>> matchers = scheduler_.getListenerManager().getJobListenerMatchers(l.getName());
         if (matchers.contains(EverythingMatcher.allJobs()))
         {
            result.add(l);
         }
      }
      return result;
   }

   public JobListener getGlobalJobListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().getJobListener(name);
   }

   public boolean removeGlobalJobListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().removeJobListener(name);
   }

   public void addJobListener(AddJobListenerComponentPlugin plugin) throws Exception
   {
      JobListener jl = (JobListener)plugin;
      try
      {
         List<Matcher<JobKey>> matchers = null;
         if (plugin.getKeys() != null)
         {
            matchers = new ArrayList<Matcher<JobKey>>();
            for (org.exoplatform.services.scheduler.JobKey key : plugin.getKeys())
            {
               matchers.add(KeyMatcher.keyEquals(JobKey.jobKey(key.getName(), getGroupName(key.getGroup()))));
            }
         }
         scheduler_.getListenerManager().addJobListener(jl, matchers);
      }
      catch (Exception e)
      {
         LOG.warn("Could not add the job listener (" + jl.getName() + ") defined in the plugin " + plugin.getName()
            + " : " + e.getMessage());
      }
   }

   public List<JobListener> getAllJobListener() throws Exception
   {
      List<JobListener> listeners = scheduler_.getListenerManager().getJobListeners();
      List<JobListener> result = new ArrayList<JobListener>();
      for (JobListener l : listeners)
      {
         List<Matcher<JobKey>> matchers = scheduler_.getListenerManager().getJobListenerMatchers(l.getName());
         if (!matchers.contains(EverythingMatcher.allJobs()))
         {
            result.add(l);
         }
      }
      return result;
   }

   public JobListener getJobListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().getJobListener(name);
   }

   public boolean removeJobListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().removeJobListener(name);
   }

   public void addGlobalTriggerListener(ComponentPlugin plugin) throws Exception
   {
      TriggerListener tl = (TriggerListener)plugin;
      try
      {
         scheduler_.getListenerManager().addTriggerListener(tl);
      }
      catch (Exception e)
      {
         LOG.warn("Could not add the global trigger listener (" + tl.getName() + ") defined in the plugin "
            + plugin.getName() + " : " + e.getMessage());
      }
   }

   public List<TriggerListener> getAllGlobalTriggerListener() throws Exception
   {
      List<TriggerListener> listeners = scheduler_.getListenerManager().getTriggerListeners();
      List<TriggerListener> result = new ArrayList<TriggerListener>();
      for (TriggerListener l : listeners)
      {
         List<Matcher<TriggerKey>> matchers = scheduler_.getListenerManager().getTriggerListenerMatchers(l.getName());
         if (matchers.contains(EverythingMatcher.allJobs()))
         {
            result.add(l);
         }
      }
      return result;
   }

   public TriggerListener getGlobalTriggerListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().getTriggerListener(name);
   }

   public boolean removeGlobaTriggerListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().removeTriggerListener(name);
   }

   public void addTriggerListener(AddTriggerListenerComponentPlugin plugin) throws Exception
   {
      TriggerListener tl = (TriggerListener)plugin;
      try
      {
         List<Matcher<TriggerKey>> matchers = null;
         if (plugin.getKeys() != null)
         {
            matchers = new ArrayList<Matcher<TriggerKey>>();
            for (org.exoplatform.services.scheduler.JobKey key : plugin.getKeys())
            {
               matchers.add(KeyMatcher.keyEquals(TriggerKey.triggerKey(key.getName(), getGroupName(key.getGroup()))));
            }
         }
         scheduler_.getListenerManager().addTriggerListener(tl, matchers);
      }
      catch (Exception e)
      {
         LOG.warn("Could not add the trigger listener (" + tl.getName() + ") defined in the plugin " + plugin.getName()
            + " : " + e.getMessage());
      }
   }

   public List<TriggerListener> getAllTriggerListener() throws Exception
   {
      List<TriggerListener> listeners = scheduler_.getListenerManager().getTriggerListeners();
      List<TriggerListener> result = new ArrayList<TriggerListener>();
      for (TriggerListener l : listeners)
      {
         List<Matcher<TriggerKey>> matchers = scheduler_.getListenerManager().getTriggerListenerMatchers(l.getName());
         if (!matchers.contains(EverythingMatcher.allJobs()))
         {
            result.add(l);
         }
      }
      return result;
   }

   public TriggerListener getTriggerListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().getTriggerListener(name);
   }

   public boolean removeTriggerListener(String name) throws Exception
   {
      return scheduler_.getListenerManager().removeTriggerListener(name);
   }

   private JobInfo getJobInfo(JobInfo jinfo) throws Exception
   {
      String gname = getGroupName(jinfo.getGroupName());
      JobInfo jobInfo = new JobInfo(jinfo.getJobName(), gname, jinfo.getJob());
      jobInfo.setDescription(jinfo.getDescription());
      return jobInfo;
   }

   public void pauseJob(String jobName, String groupName) throws Exception
   {
      scheduler_.pauseJob(JobKey.jobKey(jobName, getGroupName(groupName)));
   }

   public void resumeJob(String jobName, String groupName) throws Exception
   {
      scheduler_.resumeJob(JobKey.jobKey(jobName, getGroupName(groupName)));
   }

   public void executeJob(String jname, String jgroup, JobDataMap jdatamap) throws Exception
   {
      Trigger trigger = TriggerBuilder.newTrigger().withIdentity(jname, getGroupName(jgroup)).startNow().build();
      JobBuilder jb = JobBuilder.newJob().withIdentity(jname, getGroupName(jgroup));
      JobDetail job = jdatamap == null ? jb.build() : jb.usingJobData(jdatamap).build();
      scheduler_.scheduleJob(job, trigger);
   }

   public Trigger[] getTriggersOfJob(String jobName, String groupName) throws Exception
   {
      List<? extends Trigger> triggers = scheduler_.getTriggersOfJob(JobKey.jobKey(jobName, getGroupName(groupName)));
      Trigger[] aTriggers = new Trigger[triggers.size()];
      return (Trigger[])triggers.toArray(aTriggers);
   }

   public TriggerState getTriggerState(String jobName, String groupName) throws Exception
   {
      return scheduler_.getTriggerState(TriggerKey.triggerKey(jobName, getGroupName(groupName)));
   }

   public Date rescheduleJob(String jobName, String groupName, Trigger newTrigger) throws SchedulerException
   {
      return scheduler_.rescheduleJob(TriggerKey.triggerKey(jobName, getGroupName(groupName)), newTrigger
         .getTriggerBuilder().withIdentity(jobName, getGroupName(groupName)).build());
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
         LOG.error("Could not suspend the scheduler", e);
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
         LOG.error("Could not resume the scheduler", e);
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
         scheduler_.getListenerManager().addJobListener(new JobEnvironmentConfigListener());
      }
      catch (Exception e)
      {
         LOG.warn("Could not remove the GlobalJobListener " + JobEnvironmentConfigListener.NAME, e);
      }
   }

   public void stop()
   {
      try
      {
         List<JobExecutionContext> jobs = getAllExcutingJobs();
         for (JobExecutionContext ctx : jobs)
         {
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
         LOG.warn("Could not interrupt all the current jobs properly", ex);
      }
   }

   public JobDetail getJob(JobInfo jobInfo) throws Exception
   {
      JobInfo innerJobInfo = getJobInfo(jobInfo);
      return scheduler_.getJobDetail(JobKey.jobKey(innerJobInfo.getJobName(), innerJobInfo.getGroupName()));
   }

   private String getGroupName(String initialGroupName)
   {
      StringBuilder gname = new StringBuilder();
      gname.append(containerName_);

      if (tenantsService != null)
      {
         try
         {
            String tenantName = tenantsService.getCurrentTanant().getName();
            gname.append(":");
            gname.append(tenantName);
         }
         catch (CurrentTenantNotSetException e)
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug("Cannot append current tenant name: " + e.getMessage());
            }
         }
      }

      if (initialGroupName != null && !(initialGroupName = initialGroupName.trim()).isEmpty())
      {
         gname.append(":");
         gname.append(initialGroupName);
      }
      return gname.toString();
   }

}
