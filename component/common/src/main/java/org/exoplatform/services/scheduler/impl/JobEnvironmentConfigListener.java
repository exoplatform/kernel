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
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.component.ComponentRequestLifecycle;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;

import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Hoa Pham hoapham@exoplatform.com Oct
 * 5, 2005
 */
public class JobEnvironmentConfigListener implements JobListener, ComponentPlugin
{
   private String containerName_;

   public JobEnvironmentConfigListener(PortalContainerInfo pinfo)
   {
      containerName_ = pinfo.getContainerName();
   }

   public void jobToBeExecuted(JobExecutionContext context)
   {
      RootContainer rootContainer = RootContainer.getInstance();
      PortalContainer pcontainer = (PortalContainer)rootContainer.getComponentInstance(containerName_);
      PortalContainer.setInstance(pcontainer);
      List<ComponentRequestLifecycle> components =
         pcontainer.getComponentInstancesOfType(ComponentRequestLifecycle.class);
      for (ComponentRequestLifecycle component : components)
      {
         component.startRequest(pcontainer);
      }
   }

   public void jobExecutionVetoed(JobExecutionContext context)
   {

   }

   public void jobWasExecuted(JobExecutionContext context, JobExecutionException exception)
   {
      RootContainer rootContainer = RootContainer.getInstance();
      PortalContainer pcontainer = (PortalContainer)rootContainer.getComponentInstance(containerName_);
      List<ComponentRequestLifecycle> components =
         pcontainer.getComponentInstancesOfType(ComponentRequestLifecycle.class);
      for (ComponentRequestLifecycle component : components)
      {
         component.endRequest(pcontainer);
      }
      PortalContainer.setInstance(null);
   }

   public String getName()
   {
      return "JobContextConfigListener";
   }

   public void setName(String s)
   {
   }

   public String getDescription()
   {
      return null;
   }

   public void setDescription(String s)
   {
   }

}
