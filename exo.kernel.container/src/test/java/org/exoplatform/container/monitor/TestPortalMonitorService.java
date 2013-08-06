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
package org.exoplatform.container.monitor;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.monitor.jvm.JVMRuntimeInfo;

/**
 * Thu, May 15, 2003 @
 * 
 * @author: Tuan Nguyen
 * @version: $Id: TestPortalMonitorService.java 5799 2006-05-28 17:55:42Z geaz $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestPortalMonitorService extends TestCase
{

   PortalContainer pcontainer_;

   public TestPortalMonitorService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      pcontainer_ = PortalContainer.getInstance();
   }

   public void tearDown() throws Exception
   {
   }

   public void testOSEnvironment()
   {
      RootContainer.getInstance().getOSEnvironment();
   }

   public void testRuntimInfo()
   {
      JVMRuntimeInfo info =
         (JVMRuntimeInfo)RootContainer.getInstance().getComponentInstanceOfType(JVMRuntimeInfo.class);
   }
}
