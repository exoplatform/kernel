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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.services.scheduler.JobInfo;
import org.exoplatform.services.scheduler.JobSchedulerService;
import org.exoplatform.services.scheduler.PeriodInfo;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.TriggerListener;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com Oct
 * 5, 2005
 */
public class TestSchedulerService extends SchedulerServiceTestBase
{

   public void setUp() throws Exception
   {
      PortalContainer manager = PortalContainer.getInstance();
      service_ = (JobSchedulerService)manager.getComponentInstanceOfType(JobSchedulerService.class);
   }

   public void tearDown() throws Exception
   {
   }

   public void testQueueTask() throws Exception
   {
      service_.addGlobalTriggerListener(new GlobalTriggerListener());
      AtomicInteger counter = new AtomicInteger(3);
      synchronized (counter)
      {
         service_.queueTask(new ATask(counter));
         service_.queueTask(new ATask(counter));
         service_.queueTask(new ATask(counter));
         counter.wait(30000);
      }
      assertEquals("expect a task is run 3 times", 3, 3 - counter.get());
      boolean b = service_.removeGlobaTriggerListener("GlobalTriggerListener");
      assertTrue("expect Global Trigger Listener is removed", b);
   }

   public void testListener() throws Exception
   {
      // ---------getAllGlobalJobListener
      List<JobListener> jobListenerCol = service_.getAllGlobalJobListener();
      assertEquals("expect 1 GlobalJobListener is found", 1, jobListenerCol.size());
      /* -----2 joblistener------------- */
      JobListener jcontext = service_.getGlobalJobListener("JobContextConfigListener");
      assertTrue("exepect found 'JobContextConfigListenner'", jcontext != null
         && jcontext.getName().equals("JobContextConfigListener"));
      hasObjectInCollection(jcontext, jobListenerCol, new JobListenerComparator());
      // --------------remove JobContextConfigListenner of SchedulerSerice-------
      boolean b = service_.removeGlobalJobListener(jcontext.getName());
      jcontext = service_.getGlobalJobListener("JobContextConfigListener");
      assertTrue("expect JobContextConfigListenner is removed", b && jcontext == null);
      jobListenerCol = service_.getAllGlobalJobListener();
      assertEquals("expect 1 job listenner is found", 0, jobListenerCol.size());

      // -----Test global trigger listener
      List<TriggerListener> triggerListenerCol = service_.getAllGlobalTriggerListener();
      assertEquals("expect no global trigger listener is found", 0, triggerListenerCol.size());
      /* ----------add TriggerListenner--- */
      service_.addGlobalTriggerListener(new GlobalTriggerListener());
      // ------getAllTriggerListener---
      TriggerListener globalTriggerListener = service_.getGlobalTriggerListener("GlobalTriggerListener");
      assertTrue("expect 'GlobalTriggerListener' is found", globalTriggerListener != null
         && globalTriggerListener.getName().equals("GlobalTriggerListener"));
      triggerListenerCol = service_.getAllGlobalTriggerListener();
      assertEquals("expect 1 trigger listenner is found", 1, triggerListenerCol.size());
      hasObjectInCollection(globalTriggerListener, triggerListenerCol, new TriggerListenerComparator());
      // ----------------remove GlobalTriggerListener
      b = service_.removeGlobaTriggerListener(globalTriggerListener.getName());
      assertTrue("expect GlobalTriggerListener is removed", b);
      triggerListenerCol = service_.getAllGlobalTriggerListener();
      assertEquals("expect no trigger listenner is found", 0, triggerListenerCol.size());

      // --------------Test non global Job Listener
      jobListenerCol = service_.getAllJobListener();
      assertEquals("expect no non global job listener is found", 0, jobListenerCol.size());
      // ----add 2 Non Global Job Listenner----
      service_.addGlobalJobListener(new GlobalJobListener());
      service_.addJobListener(new FirstJobListener());
      service_.addJobListener(new SecondJobListener());
      JobListener globalListener = service_.getGlobalJobListener("GlobalJobListener");
      JobListener joblistener1st = service_.getJobListener("FirstJobListener");
      JobListener joblistener2nd = service_.getJobListener("SecondJobListener");
      assertTrue("expect 'GlobalJobListener' is found", globalListener != null
               && globalListener.getName().equals("GlobalJobListener"));
      assertTrue("expect 'FirstJobListener' is found", joblistener1st != null
         && joblistener1st.getName().equals("FirstJobListener"));
      assertTrue("expect 'SecondJobListenner' is found", joblistener2nd != null
         && joblistener2nd.getName().equals("SecondJobListener"));
      jobListenerCol = service_.getAllJobListener();
      assertEquals("expect 1 global job listener is found", 1, service_.getAllGlobalJobListener().size());
      assertEquals("expect 2 non global job listener is found", 2, jobListenerCol.size());
      hasObjectInCollection(joblistener1st, jobListenerCol, new JobListenerComparator());
      hasObjectInCollection(joblistener2nd, jobListenerCol, new JobListenerComparator());
      
      GlobalJobListener.countCalled_ = 0;
      FirstJobListener.countCalled_ = 0;
      SecondJobListener.countCalled_ = 0;
      assertEquals(0, GlobalJobListener.countCalled_);
      assertEquals(0, FirstJobListener.countCalled_);
      assertEquals(0, SecondJobListener.countCalled_);
      
      service_.addJob(new JobInfo("GlobalJobListener", null/* default group */, AJob.class), new Date());
      service_.addJob(new JobInfo("FirstJobListener", null/* default group */, AJob.class), new Date());
      service_.addJob(new JobInfo("SecondJobListener", null/* default group */, AJob.class), new Date());
      Thread.sleep(100);
      
      //Scheduler can be faster and execute more than 3 tasks
      assertTrue(GlobalJobListener.countCalled_ >= 3);
      assertEquals(1, FirstJobListener.countCalled_);
      assertEquals(1, SecondJobListener.countCalled_);
      // ---remove FirstJobListenner---
      b = service_.removeJobListener(joblistener1st.getName());
      assertTrue("expect FirstJobListenner is removed", b);
      joblistener1st = service_.getJobListener("FirstJobListenner");
      assertTrue("expect FirstJobListenner is not found", joblistener1st == null);
      jobListenerCol = service_.getAllJobListener();
      assertEquals("now,expect 1 non global job is found", 1, jobListenerCol.size());
      // ---remove SecondJobListenner---
      b = service_.removeJobListener(joblistener2nd.getName());
      joblistener2nd = service_.getJobListener("SecondJobListener");
      assertTrue("expect SecondJobListenner is removed", b && joblistener2nd == null);
      jobListenerCol = service_.getAllJobListener();
      assertEquals("now,expect no non global job is found", 0, jobListenerCol.size());

      b = service_.removeGlobalJobListener(globalListener.getName());
      globalListener = service_.getGlobalJobListener("GlobalJobListener");
      assertTrue("expect GlobalJobListener is removed", b && globalListener == null);
      assertEquals("now we expect no global job listener", 0,  service_.getAllGlobalJobListener().size());
      
      Thread.sleep(2500);

      // -----Test non global Trigger Listenner ----
      triggerListenerCol = service_.getAllTriggerListener();
      assertEquals("expect no non global trigger listener is found", 0, triggerListenerCol.size());
      // ----------add 2 non global trigger listener---
      service_.addGlobalTriggerListener(new GlobalTriggerListener());
      service_.addTriggerListener(new FirstTriggerListener());
      service_.addTriggerListener(new SecondTriggerListener());
      TriggerListener triggerListener1st = service_.getTriggerListener("FirstTriggerListener");
      TriggerListener triggerListener2nd = service_.getTriggerListener("SecondTriggerListener");
      assertTrue("expect 'FirstTriggerListener' is found", triggerListener1st != null
         && triggerListener1st.getName().equals("FirstTriggerListener"));
      assertTrue("expect 'SecondTriggerListener' is found", triggerListener2nd != null
         && triggerListener2nd.getName().equals("SecondTriggerListener"));
      triggerListenerCol = service_.getAllTriggerListener();
      globalTriggerListener = service_.getGlobalTriggerListener("GlobalTriggerListener");
      assertTrue("expect 'GlobalTriggerListener' is found", globalTriggerListener != null
         && globalTriggerListener.getName().equals("GlobalTriggerListener"));
      assertEquals("expect 1 trigger listenner is found", 1, service_.getAllGlobalTriggerListener().size());     
      assertEquals("expect 2 non global trigger listener is found", 2, triggerListenerCol.size());
      hasObjectInCollection(triggerListener1st, triggerListenerCol, new TriggerListenerComparator());
      hasObjectInCollection(triggerListener2nd, triggerListenerCol, new TriggerListenerComparator());
      
      GlobalTriggerListener.countTriggerComplete_ = 0;
      FirstTriggerListener.countTriggerComplete_ = 0;
      SecondTriggerListener.countTriggerComplete_ = 0;
      assertEquals(0, GlobalTriggerListener.countTriggerComplete_);
      assertEquals(0, FirstTriggerListener.countTriggerComplete_);
      assertEquals(0, SecondTriggerListener.countTriggerComplete_);
      
      service_.addJob(new JobInfo("GlobalTriggerListener", null/* default group */, AJob.class), new Date());
      service_.addJob(new JobInfo("FirstTriggerListener", null/* default group */, AJob.class), new Date());
      service_.addJob(new JobInfo("SecondTriggerListener", null/* default group */, AJob.class), new Date());
      Thread.sleep(100);
      
      //Scheduler can be faster and execute more than 3 tasks
      assertTrue(GlobalTriggerListener.countTriggerComplete_ >= 3);
      assertEquals(1, FirstTriggerListener.countTriggerComplete_);
      assertEquals(1, SecondTriggerListener.countTriggerComplete_);
      
      // ----remove non global trigger listener----
      b = service_.removeTriggerListener(triggerListener1st.getName());
      triggerListener1st = service_.getTriggerListener("FirstTriggerListener");
      assertTrue("expect 'FirstTriggerListener' is removed", b && triggerListener1st == null);
      triggerListenerCol = service_.getAllTriggerListener();
      assertEquals("now, expect 1 non global trigger is found", 1, triggerListenerCol.size());
      b = service_.removeTriggerListener(triggerListener2nd.getName());
      // -----remove Second Trigger Listener----
      triggerListener2nd = service_.getTriggerListener("SecondTriggerListener");
      assertTrue("expect 'SecondTriggerListener' is removed", b && triggerListener2nd == null);
      triggerListenerCol = service_.getAllTriggerListener();
      assertEquals("now, expect no non global trigger is found", 0, triggerListenerCol.size());
      b = service_.removeGlobaTriggerListener(globalTriggerListener.getName());
      assertTrue("expect GlobalTriggerListener is removed", b);
      triggerListenerCol = service_.getAllGlobalTriggerListener();
      assertEquals("expect no trigger listenner is found", 0, triggerListenerCol.size());
   }
   
   public void testJobWithNonStartedServices() throws Exception
   {
      MyComponent component = (MyComponent)PortalContainer.getInstance().getComponentInstanceOfType(MyComponent.class);
      assertEquals(Boolean.TRUE, component.getResult());
   }
   
   public void testMultiplePortalContainers() throws Exception
   {
      ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
      MyComponent component;
      PortalContainer container = null;
      MyComponent component2;
      PortalContainer container2 = null;
      String oldProfileList = System.getProperty(PropertyManager.RUNTIME_PROFILES);
      try
      {
         try
         {
            PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, "MultiplePortalContainers");
            component = (MyComponent)(container = RootContainer.getInstance().getPortalContainer("portal-container")).getComponentInstanceOfType(MyComponent.class);
         }
         finally
         {
            ExoContainerContext.setCurrentContainer(oldContainer);
         }
         try
         {
            PropertyManager.setProperty(PropertyManager.RUNTIME_PROFILES, "MultiplePortalContainers,portal-container2");
            component2 = (MyComponent)(container2 = RootContainer.getInstance().getPortalContainer("portal-container2")).getComponentInstanceOfType(MyComponent.class);
            Thread.sleep(2000);
         }
         finally
         {
            ExoContainerContext.setCurrentContainer(oldContainer);
         }
      }
      finally
      {
         if (oldProfileList == null)
         {
            System.clearProperty(PropertyManager.RUNTIME_PROFILES);
         }
         else
         {
            System.setProperty(PropertyManager.RUNTIME_PROFILES, oldProfileList);
         }
         PropertyManager.refresh();
      }
      assertEquals("myJob1", component.name);
      assertEquals(container, component.container);
      assertTrue(component.endRequest > 0);
      assertFalse(component.endRequestFailed);
      assertEquals("myJob2", component2.name);
      assertEquals(container2, component2.container);
      assertTrue(component2.endRequest > 0);
      assertFalse(component2.endRequestFailed);
   }
   
   public void testSeviceWithGlobalListener() throws Exception
   {
      assertTrue("JobScheduler is not deployed correctly", service_ != null);
      Thread.sleep(1000);
      Date startTime = new Date(System.currentTimeMillis() + 1000);
      Date endTime = null;
      JobInfo jinfo;

      // Test addJob(JobInfo, Date startTime) and GlobalTriggerListener()
      resetTestEnvironment();
      jinfo = new JobInfo("TestAddJob", null/* default group */, AJob.class);
      AJob.expectExecuteTime_ = startTime;
      service_.addGlobalTriggerListener(new GlobalTriggerListener());
      service_.addJob(jinfo, startTime);
      Thread.sleep(1100);
      assertEquals("task has been run once", 1, AJob.counter_);
      // assertEquals("GlobalTriggerListener detect one task", 2,
      // GlobalTriggerListener.countTriggerFired_) ;
      assertTrue("The task is triggered  at the correct time  within 500ms margin", AJob.errorCounter_ == 0);

      /*
       * Test addJobPeriod(JobInfo jinfo,JobPeriodInfor pinfor) Job will fire
       * after 3 second, repeat forever, 2 second pause between repeat fire
       */
      resetTestEnvironment();
      jinfo = new JobInfo("TestJobPeriod", null/* default group */, AJob.class);
      startTime = new Date(System.currentTimeMillis() + 1000);
      PeriodInfo jpinfo = new PeriodInfo(startTime, null /* endTime */, 2/* repeatCount */, 1000/* period */);
      service_.addPeriodJob(jinfo, jpinfo);
      Thread.sleep(2100);
      assertEquals("task has been run exactly two times", 2, AJob.repeatCounter_);

      /*
       * Test addJobPeriod(JobInfo jinfo,JobPeriodInfor pinfor) Job will fire
       * after 1 second, stop after 2 second, 1 second pause between repeat fire
       */
      resetTestEnvironment();
      long currentTime = System.currentTimeMillis();
      jinfo = new JobInfo("TestJobPeriod", null/* default group */, AJob.class);
      startTime = new Date(currentTime + 1000);
      endTime = new Date(currentTime + 2100);
      jpinfo = new PeriodInfo(startTime, endTime, 0/* repeatCount */, 1000/* period */);
      service_.addPeriodJob(jinfo, jpinfo);
      Thread.sleep(2500);
      assertEquals("task has been run exactly two times", 2, AJob.repeatCounter_);

      /*
       * test test addPeriodJob(JobInfo jinfo,JobperiodInfo pinfo) Job will run
       * imediately, and exactly two times, 1 second pause between repeat run
       */
      resetTestEnvironment();
      jinfo = new JobInfo("ImediatelyPeriodJob", null/* default group */, AJob.class);
      jpinfo = new PeriodInfo(null/* startTime */, null/* endTime */, 2/* repeatCount */, 500/* period */);
      service_.addPeriodJob(jinfo, jpinfo);
      Thread.sleep(1100);
      assertEquals("task has been run exactly two times", 2, AJob.repeatCounter_);
      service_.removeJob(jinfo);
      /*
       * test addCronJob(JobInfor jinfo, String exp); Job will fire at 10: am
       * every day "0 15 10 ? * *"
       */
      resetTestEnvironment();
      String exp = "0 25 11 ? * *";
      jinfo = new JobInfo("TestCronJob", "cronGroup"/* default group */, AJob.class);
      service_.addCronJob(jinfo, exp);
      assertEquals("task has been run exactly one times at " + exp, 0, AJob.repeatCounter_);
      service_.removeJob(jinfo);

      /*
       * test test addPeriodJob(JobInfo jinfo,JobperiodInfo pinfo) Job will run
       * imediately, and forever, 1 second pause between repeat run
       */
      resetTestEnvironment();
      jinfo = new JobInfo("ForeverPeriodJob", null/* default group */, AJob.class);
      jpinfo = new PeriodInfo(null/* startTime */, null/* endTime */, 0/* repeatCount */, 500/* period */);
      service_.addPeriodJob(jinfo, jpinfo);
      Thread.sleep(1100);
      assertEquals("task has been run forever: ", 3, AJob.repeatCounter_);
      boolean b = service_.removeJob(jinfo);
      assertTrue("expect Job is removed", b);
      Thread.sleep(1000);
      assertEquals("task has been run forever: ", 3, AJob.repeatCounter_);
      b = service_.removeGlobaTriggerListener("GlobalTriggerListener");
      assertTrue("expect Global Trigger Listener is removed", b);
   }
   
   public void testgetAvailableJobs() throws Exception
   {
      List<JobDetail> availableJobs = service_.getAllJobs();
      int size = availableJobs.size();
      // some information about job execution
      Date firedTime = new Date(System.currentTimeMillis() + 1000000);
      service_.addJob(new JobInfo("queuejob", null/* default group */, AJob.class), firedTime);
      availableJobs = service_.getAllJobs();
      assertEquals("Expect one job inthe queue", size + 1, availableJobs.size());
   }
   
   public static class MyContainerLifecyclePlugin extends BaseContainerLifecyclePlugin
   {

      @Override
      public void startContainer(ExoContainer container) throws Exception
      {
         MyComponent component = (MyComponent)container.getComponentInstanceOfType(MyComponent.class);
         component.doneSignal.await(2, TimeUnit.SECONDS);
         component.started = true;
      }
   }
   
   public static class MyComponent implements ComponentRequestLifecycle
   {
      public ExoContainer container;
      public String name;
      public boolean started;
      public final CountDownLatch doneSignal = new CountDownLatch(1);
      public Boolean result;
      public int endRequest;
      public boolean endRequestFailed;
      private ExoContainer startRequestContainer;

      public void doSomething()
      {
         if (started)
         {
            result = Boolean.TRUE;
         }
         else
         {
            result = Boolean.FALSE;
         }
         doneSignal.countDown();
      }
      
      public Boolean getResult() throws InterruptedException
      {
         doneSignal.await(2, TimeUnit.SECONDS);
         return result;
      }

      @Override
      public void startRequest(ExoContainer container)
      {
         this.startRequestContainer = container;
      }

      @Override
      public void endRequest(ExoContainer container)
      {
         if (container.equals(this.container) && this.container.equals(startRequestContainer))
            endRequest++;
         else if (this.container != null)
            endRequestFailed = true;
      }
   }
   
   public static class MyJobWithNonStartedServices implements Job
   {

      public void execute(JobExecutionContext context) throws JobExecutionException
      {
         MyComponent component = (MyComponent)PortalContainer.getInstance().getComponentInstanceOfType(MyComponent.class);
         component.doSomething();
      }
   }
   public static class MyJobMultiplePortalContainers implements Job
   {

      public void execute(JobExecutionContext context) throws JobExecutionException
      {
         MyComponent component = (MyComponent)PortalContainer.getInstance().getComponentInstanceOfType(MyComponent.class);
         component.name = context.getJobDetail().getKey().getName();
         component.container = PortalContainer.getInstance();
      }
   }
}
