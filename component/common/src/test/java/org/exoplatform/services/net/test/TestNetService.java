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
package org.exoplatform.services.net.test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.net.NetService;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by The eXo Platform SAS Author : HoaPham phamvuxuanhoa@yahoo.com Jan
 * 10, 2006
 */
public class TestNetService extends BasicTestCase
{
   private NetService service_;

   public TestNetService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      if (service_ == null)
      {
         PortalContainer manager = PortalContainer.getInstance();
         service_ = (NetService)manager.getComponentInstanceOfType(NetService.class);
      }
   }

   public void tearDown() throws Exception
   {

   }

   public void testNetService() throws Exception
   {
      ping(null, 0);
      ping("www.google.com", 80);
      ping("www.vnexpress.net", 80);
      ping("www.exoplatform.org", 80);
      // ----ping a host on LAN
      ping("localhost", 25);
   }

   private void ping(String host, int port) throws Exception
   {
      System.out.println("Ping host:" + host + " port: " + port + " .....");
      long pingTime = 0;
      pingTime = service_.ping(host, port);
      if (pingTime > 0)
      {
         System.out.println("--------> Host is connected");
         System.out.println("--------> Ping time(ms): " + pingTime);
      }
      else
      {
         System.out.println("--------> Internet is not connected or host doesn't exits");
      }
   }
}
