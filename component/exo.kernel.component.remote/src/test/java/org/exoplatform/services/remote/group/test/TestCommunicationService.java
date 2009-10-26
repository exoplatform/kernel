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
package org.exoplatform.services.remote.group.test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.remote.group.CommunicationService;
import org.exoplatform.services.remote.group.CommunicationServiceMonitor;
import org.exoplatform.services.remote.group.MemberInfo;
import org.exoplatform.services.remote.group.PingResult;
import org.exoplatform.services.remote.group.impl.CommunicationServiceImpl;
import org.exoplatform.services.remote.group.impl.GetCommunicationMonitorHandler;
import org.exoplatform.services.remote.group.impl.PingMessageHandler;
import org.exoplatform.test.BasicTestCase;

import java.util.List;

/**
 * Created y the eXo platform team User: Benjamin Mestrallet Date: 16 juin 2004
 */
public class TestCommunicationService extends BasicTestCase
{

   public TestCommunicationService(String name)
   {
      super(name);
   }

   public void testDummy()
   {
      // for surefire
   }

   /**
    * 
    * Renamed to _testInitCommunicationService.
    * 
    * CommunicationService is deprecated.
    * 
    * @throws Exception
    *           if error
    */
   public void _testInitCommunicationService() throws Exception
   {
      PortalContainer pcontainer = PortalContainer.getInstance();
      CommunicationServiceImpl service1 =
         (CommunicationServiceImpl)pcontainer.getComponentInstanceOfType(CommunicationService.class);

      ConfigurationManager manager =
         (ConfigurationManager)pcontainer.getComponentInstanceOfType(ConfigurationManager.class);
      Component component = manager.getComponent(CommunicationService.class);
      InitParams params = null;
      if (component != null)
         params = component.getInitParams();

      CommunicationServiceImpl service2 =
         (CommunicationServiceImpl)pcontainer.createComponent(CommunicationServiceImpl.class, params);
      service2.addPlugin(new PingMessageHandler());
      service2.addPlugin(new GetCommunicationMonitorHandler());

      CommunicationServiceImpl service3 =
         (CommunicationServiceImpl)pcontainer.createComponent(CommunicationServiceImpl.class, params);
      service3.addPlugin(new PingMessageHandler());
      service3.addPlugin(new GetCommunicationMonitorHandler());

      Thread.sleep(1000);
      System.out.println(service3.getInfo());

      List results = service3.pingAll("ping");
      for (int i = 0; i < results.size(); i++)
      {
         PingResult result = (PingResult)results.get(i);
         System.out.println("Result from sender: " + result.getMemberInfo().getIpAddress());
         System.out.println("    Reply Message" + result.getReplyMessage());
      }

      List members = service1.getMembersInfo();
      for (int i = 0; i < members.size(); i++)
      {
         MemberInfo member = (MemberInfo)members.get(i);
         CommunicationServiceMonitor monitor = service1.getCommunicationServiceMonitor(member);
         System.out.println("Find monitor of the host: " + monitor.getMemberInfo());
      }
      service3 = null;
      System.out.println(service1.getInfo());
   }
}
