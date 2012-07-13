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
package org.exoplatform.container.test;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.mocks.MockService;
import org.exoplatform.mocks.PriorityService;

import java.util.List;

/**
 * Created by the Exo Development team.<br/> 
 * Author : Mestrallet Benjamin benjamin.mestrallet@exoplatform.com
 * @version $Id: TestContainer.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class TestContainer extends TestCase
{

   public void setUp() throws Exception
   {
      System.setProperty("maven.exoplatform.dir", TestContainer.class.getResource("/").getFile());
   }

   public void testComponent() throws Exception
   {
      RootContainer rootContainer = RootContainer.getInstance();
      MockService mservice = (MockService)rootContainer.getComponentInstance("MockService");
      assertTrue(mservice != null);
      assertTrue(mservice.getPlugins().size() == 2);
   }

   public void testComponent2() throws Exception
   {
      RootContainer rootContainer = RootContainer.getInstance();
      PortalContainer pcontainer = rootContainer.getPortalContainer("portal");
      assertNotNull(pcontainer);
      MultibleComponent c = (MultibleComponent)pcontainer.getComponentInstanceOfType(MultibleComponent.class);
      assertNotNull(c);
      c = (MultibleComponent)pcontainer.getComponentInstanceOfType(MultibleComponent.class);
      assertNotNull(c);
      c = (MultibleComponent)pcontainer.getComponentInstanceOfType(MultibleComponent.class);
      assertNotNull(c);
   }

   public void testComponent3() throws Exception
   {
      RootContainer rootContainer = RootContainer.getInstance();
      PortalContainer pcontainer = rootContainer.getPortalContainer("portal");
      assertNotNull(pcontainer);
      DefaultComponent c = (DefaultComponent)pcontainer.getComponentInstanceOfType(DefaultComponent.class);
      assertNotNull(c);
      c = (DefaultComponent)pcontainer.getComponentInstanceOfType(DefaultComponent.class);
      assertNotNull(c);
      c = (DefaultComponent)pcontainer.getComponentInstanceOfType(DefaultComponent.class);
      assertNotNull(c);
   }

   public void testPriorityPlugins()
   {
      RootContainer rootContainer = RootContainer.getInstance();
      PortalContainer pcontainer = rootContainer.getPortalContainer("portal");
      assertNotNull(pcontainer);
      PriorityService ps = (PriorityService)pcontainer.getComponentInstanceOfType(PriorityService.class);
      assertNotNull(ps);
      List<ComponentPlugin> l = ps.getPlugins();
      assertNotNull(l);
      assertEquals(3, l.size());
      assertEquals("PluginPriority3", l.get(0).getName());
      assertEquals("PluginPriority1", l.get(1).getName());
      assertEquals("PluginPriority2", l.get(2).getName());
   }
   
   public void testPortalContainer() throws Exception
   {
      RootContainer rootContainer = RootContainer.getInstance();
      PortalContainer pcontainer = rootContainer.getPortalContainer("portal");
      Object parent = pcontainer.getParent();
      assertTrue("Root container should not be null", parent != null);
      pcontainer.createSessionContainer("sessioncontainer1", "anon");
      pcontainer.createSessionContainer("sessioncontainer2", "anon");
      List sessions = pcontainer.getLiveSessions();
      assertEquals("expect 2 session container", 2, sessions.size());
      // performance test

      int INSERTLOOP = 0;
      for (int i = 0; i < INSERTLOOP; i++)
      {
         rootContainer.getPortalContainer("name-" + Integer.toString(i));
      }

      int LOOP = 10000000;
      for (int i = 0; i < LOOP; i++)
      {
         pcontainer = (PortalContainer)rootContainer.getComponentInstance("portal");
         assertTrue("not null", pcontainer != null);
      }
   }   
}
