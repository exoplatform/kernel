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
package org.exoplatform.container.monitor.jvm;

import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.management.ManagementFactory;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Jan 11, 2005
 * @version $Id: AddJVMComponentsToRootContainer.java 5799 2006-05-28 17:55:42Z
 *          geaz $
 */
public class AddJVMComponentsToRootContainer extends BaseContainerLifecyclePlugin
{

   private static final Log log = ExoLogger.getLogger("exo.kernel.container.AddJVMComponentsToRootContainer");

   public void initContainer(ExoContainer container)
   {
      attemptToRegisterMXComponent(container, ManagementFactory.getOperatingSystemMXBean());
      attemptToRegisterMXComponent(container, ManagementFactory.getRuntimeMXBean());
      attemptToRegisterMXComponent(container, ManagementFactory.getThreadMXBean());
      attemptToRegisterMXComponent(container, ManagementFactory.getClassLoadingMXBean());
      attemptToRegisterMXComponent(container, ManagementFactory.getCompilationMXBean());

      attemptToRegisterMXComponent(container, new MemoryInfo());
      attemptToRegisterMXComponent(container, JVMRuntimeInfo.MEMORY_MANAGER_MXBEANS, ManagementFactory
         .getMemoryManagerMXBeans());
      attemptToRegisterMXComponent(container, JVMRuntimeInfo.MEMORY_POOL_MXBEANS, ManagementFactory
         .getMemoryPoolMXBeans());
      attemptToRegisterMXComponent(container, JVMRuntimeInfo.GARBAGE_COLLECTOR_MXBEANS, ManagementFactory
         .getGarbageCollectorMXBeans());
   }

   private void attemptToRegisterMXComponent(ExoContainer container, Object mxComponent)
   {
      if (mxComponent != null)
      {
         log.debug("Attempt to register mx component " + mxComponent);
         container.registerComponentInstance(mxComponent);
         log.debug("Mx component " + mxComponent + " registered");
      }
   }

   private void attemptToRegisterMXComponent(ExoContainer container, Object mxKey, Object mxComponent)
   {
      if (mxComponent != null)
      {
         log.debug("Attempt to register mx component " + mxComponent + " with key " + mxKey);
         container.registerComponentInstance(mxKey, mxComponent);
         log.debug("Mx component " + mxComponent + " registered");
      }
   }

}
