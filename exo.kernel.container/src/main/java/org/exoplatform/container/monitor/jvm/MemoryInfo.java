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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Nov 8, 2004
 * @version $Id: MemoryInfo.java 12726 2007-02-12 05:01:32Z tuan08 $
 */
public class MemoryInfo
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.container.monitor.jvm.MemoryInfo");
   
   private MemoryMXBean mxbean_;

   public MemoryInfo()
   {
      mxbean_ = ManagementFactory.getMemoryMXBean();
   }

   public MemoryUsage getHeapMemoryUsage()
   {
      return mxbean_.getHeapMemoryUsage();
   }

   public MemoryUsage getNonHeapMemoryUsage()
   {
      return mxbean_.getNonHeapMemoryUsage();
   }

   public int getObjectPendingFinalizationCount()
   {
      return mxbean_.getObjectPendingFinalizationCount();
   }

   public boolean isVerbose()
   {
      return mxbean_.isVerbose();
   }

   public void printMemoryInfo()
   {
      LOG.info("  Memory Heap Usage: " + mxbean_.getHeapMemoryUsage());
      LOG.info("  Memory Non Heap Usage" + mxbean_.getNonHeapMemoryUsage());
   }
}
