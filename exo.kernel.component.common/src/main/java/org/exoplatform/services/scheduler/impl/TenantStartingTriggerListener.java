/*
 * Copyright (C) 2003-2014 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.scheduler.impl;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.listeners.TriggerListenerSupport;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Sep 12, 2014  
 */
/**
 * Trigger listener controls starting tenant's job Always cancel execute job
 * until tenant container is created completely
 */
public class TenantStartingTriggerListener extends TriggerListenerSupport implements
    ComponentPlugin {

  private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.JobSchedulerServiceImpl");
  
  public static final String  TENANTTRIGGER_NAME                 = "TenantStartingTriggerListener";

  private String           tenantName;

  private String           description;

  public TenantStartingTriggerListener(String tenantName) {
    this.tenantName = tenantName;
  }

  @Override
  public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
    String group = context.getJobDetail().getKey().getGroup();
    if (group.contains(tenantName)){
      LOG.debug("Veto job execution of {} ", context.getJobDetail().getKey().getGroup());
      return true;
    }
    return false;
  }

  @Override
  public String getName() {
    return createName(tenantName);
  }

  @Override
  public void setName(String s) {
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public void setDescription(String s) {
    this.description = s;
  }

  public static String createName(String tenantName) {
    return tenantName + ":" + TENANTTRIGGER_NAME;
  }
}
