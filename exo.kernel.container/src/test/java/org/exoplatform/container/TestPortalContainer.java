/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.container;

import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.support.ContainerBuilder;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import java.net.URL;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 18 fﾎvr. 2010  
 */
public class TestPortalContainer extends AbstractTestContainer
{
   public void testInitValues()
   {
      createRootContainer("portal-container-config-with-settings.xml");
      assertEquals("myPortal", PortalContainer.DEFAULT_PORTAL_CONTAINER_NAME);
      assertEquals("myRest", PortalContainer.DEFAULT_REST_CONTEXT_NAME);
      assertEquals("my-exo-domain", PortalContainer.DEFAULT_REALM_NAME);
      // With portal container with no portal container
      PortalContainer portal = PortalContainer.getInstance();
      assertEquals("myPortal", portal.getName());
      assertEquals("myRest", portal.getRestContextName());
      assertEquals("my-exo-domain", portal.getRealmName());
      
      assertTrue(PortalContainer.isPortalContainerName("myPortal"));
      assertTrue(PortalContainer.isPortalContainerName("portal"));
      assertFalse(PortalContainer.isPortalContainerName("foo"));
      
      URL rootURL = getClass().getResource("portal-container-config-with-settings.xml");
      URL portalURL = getClass().getResource("portal-container-test-settings-configuration.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();
      // With portal container with no portal container
      portal = PortalContainer.getInstance();
      assertEquals("portal", portal.getName());
      assertEquals("myRest-pcdef", portal.getRestContextName());
      assertEquals("my-exo-domain-pcdef", portal.getRealmName());
      
      assertNotNull(portal.getContext());
      assertEquals("portal", portal.getContext().getPortalContainerName());
      assertEquals("myRest-pcdef", portal.getContext().getRestContextName());
      assertEquals("my-exo-domain-pcdef", portal.getContext().getRealmName());
      
      assertEquals("portal", PortalContainer.getCurrentPortalContainerName());
      assertEquals("myRest-pcdef", PortalContainer.getCurrentRestContextName());
      assertEquals("my-exo-domain-pcdef", PortalContainer.getCurrentRealmName());      
      
      assertEquals("myRest-pcdef", PortalContainer.getRestContextName("portal"));
      assertEquals("my-exo-domain-pcdef", PortalContainer.getRealmName("portal"));   
      assertEquals("myRest", PortalContainer.getRestContextName("foo"));
      assertEquals("my-exo-domain", PortalContainer.getRealmName("foo"));   
      
      assertTrue(PortalContainer.isPortalContainerName("myPortal"));
      assertTrue(PortalContainer.isPortalContainerName("portal"));
      assertFalse(PortalContainer.isPortalContainerName("foo"));
    
      // With no portal container
      PortalContainer.setInstance(null);

      assertEquals("myPortal", PortalContainer.getCurrentPortalContainerName());
      assertEquals("myRest", PortalContainer.getCurrentRestContextName());
      assertEquals("my-exo-domain", PortalContainer.getCurrentRealmName());      
      
      assertTrue(PortalContainer.isPortalContainerName("myPortal"));
      assertTrue(PortalContainer.isPortalContainerName("portal"));
      assertFalse(PortalContainer.isPortalContainerName("foo"));
   }

   public void testSettings()
   {
      URL rootURL = getClass().getResource("portal-container-config-with-settings.xml");
      URL portalURL = getClass().getResource("portal-container-test-settings-configuration.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();
      // With portal container
      PortalContainer portal = PortalContainer.getInstance();
      MyComponent component = (MyComponent)portal.getComponentInstanceOfType(MyComponent.class);      
      assertNotNull(component);
      assertEquals("portal", component.getValue("portal"));
      assertEquals("myRest-pcdef", component.getValue("rest"));
      assertEquals("my-exo-domain-pcdef", component.getValue("realm"));
      assertEquals("value", component.getValue("foo"));
      assertEquals("before value after", component.getValue("before foo after"));
      
      assertEquals("value", portal.getSetting("foo"));
      assertNull(portal.getSetting("foo2"));
      assertEquals("value", portal.getSetting("string"));
      assertEquals(new Integer(10), portal.getSetting("int"));
      assertEquals(new Long(10), portal.getSetting("long"));
      assertEquals(new Double(10), portal.getSetting("double"));
      assertEquals(new Boolean(true), portal.getSetting("boolean"));
      
      assertNotNull(portal.getContext());
      assertEquals("value", portal.getContext().getSetting("foo"));
      assertNull(portal.getContext().getSetting("foo2"));
      assertEquals("value", portal.getContext().getSetting("string"));
      assertEquals(new Integer(10), portal.getContext().getSetting("int"));
      assertEquals(new Long(10), portal.getContext().getSetting("long"));
      assertEquals(new Double(10), portal.getContext().getSetting("double"));
      assertEquals(new Boolean(true), portal.getContext().getSetting("boolean"));
      
      assertEquals("value", PortalContainer.getCurrentSetting("foo"));
      assertNull(PortalContainer.getCurrentSetting("foo2"));
      assertEquals("value", PortalContainer.getCurrentSetting("string"));
      assertEquals(new Integer(10), PortalContainer.getCurrentSetting("int"));
      assertEquals(new Long(10), PortalContainer.getCurrentSetting("long"));
      assertEquals(new Double(10), PortalContainer.getCurrentSetting("double"));
      assertEquals(new Boolean(true), PortalContainer.getCurrentSetting("boolean"));
      
      assertEquals("value", PortalContainer.getSetting("portal", "foo"));
      assertNull(PortalContainer.getSetting("portal", "foo2"));
      assertEquals("value", PortalContainer.getSetting("portal", "string"));
      assertEquals(new Integer(10), PortalContainer.getSetting("portal", "int"));
      assertEquals(new Long(10), PortalContainer.getSetting("portal", "long"));
      assertEquals(new Double(10), PortalContainer.getSetting("portal", "double"));
      assertEquals(new Boolean(true), PortalContainer.getSetting("portal", "boolean"));
      
      assertNull(PortalContainer.getSetting("foo", "foo"));
      assertNull(PortalContainer.getSetting("foo", "foo2"));
      assertNull(PortalContainer.getSetting("foo", "string"));
      assertNull(PortalContainer.getSetting("foo", "int"));
      assertNull(PortalContainer.getSetting("foo", "long"));
      assertNull(PortalContainer.getSetting("foo", "double"));
      assertNull(PortalContainer.getSetting("foo", "boolean"));  
      
      // With no portal container
      PortalContainer.setInstance(null);

      assertNull(PortalContainer.getCurrentSetting("foo"));
      assertNull(PortalContainer.getCurrentSetting("foo2"));
      assertNull(PortalContainer.getCurrentSetting("string"));
      assertNull(PortalContainer.getCurrentSetting("int"));
      assertNull(PortalContainer.getCurrentSetting("long"));
      assertNull(PortalContainer.getCurrentSetting("double"));
      assertNull(PortalContainer.getCurrentSetting("boolean"));     
   }
   
   public static class MyComponent
   {
      private final InitParams params;
      public MyComponent(InitParams params)
      {
         this.params = params;
      }
      
      public String getValue(String name)
      {
         final ValueParam vp = params.getValueParam(name);
         return vp == null ? null : vp.getValue();
      }
   }
}
