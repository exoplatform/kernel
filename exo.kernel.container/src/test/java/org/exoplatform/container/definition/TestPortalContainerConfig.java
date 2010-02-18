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
package org.exoplatform.container.definition;

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 18 févr. 2010  
 */
public class TestPortalContainerConfig extends AbstractTestContainer
{

   public void testInitValues()
   {
      RootContainer rootContainer = createRootContainer("empty-config.xml");
      PortalContainerConfig config =
         (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config);

      rootContainer = createRootContainer("portal-container-config-with-no-default-values.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getDefaultPortalContainer());
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getDefaultRestContext());
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getDefaultRealmName());
      assertFalse(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-default-values.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal", config.getDefaultPortalContainer());
      assertEquals("myRest", config.getDefaultRestContext());
      assertEquals("my-exo-domain", config.getDefaultRealmName());
      assertFalse(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal", config.getDefaultPortalContainer());
      assertEquals("myRest", config.getDefaultRestContext());
      assertEquals("my-exo-domain", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-no-default-values-but-with-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal-pcdef", config.getDefaultPortalContainer());
      assertEquals("myRest-pcdef", config.getDefaultRestContext());
      assertEquals("my-exo-domain-pcdef", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());
   }

   public void testDependencies()
   {
      // Without dependencies
      RootContainer rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-portal-def.xml");
      PortalContainerConfig config =
         (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getDependencies("foo"));
      assertNull(config.getDependencies("myPortal"));
      assertNull(config.getDependencies("myPortal-pcdef"));
      List<String> names = config.getPortalContainerNames("foo");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal", names.get(0));
      names = config.getPortalContainerNames("myPortal");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal", names.get(0));
      names = config.getPortalContainerNames("myPortal-pcdef");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal-pcdef", names.get(0));
      assertEquals("myPortal", config.getPortalContainerName("foo"));
      assertEquals("myPortal", config.getPortalContainerName("myPortal"));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("myPortal-pcdef"));
      assertEquals("myRest", config.getRestContextName("foo"));
      assertEquals("myRest", config.getRestContextName("myPortal"));
      assertEquals("myRest-pcdef", config.getRestContextName("myPortal-pcdef"));
      assertEquals("my-exo-domain", config.getRealmName("foo"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal"));
      assertEquals("my-exo-domain-pcdef", config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertTrue(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-pcdef"));
      // Needed for backward compatibility
      assertTrue(config.isScopeValid("foo", "foo"));
      assertTrue(config.isScopeValid("myPortal", "foo"));
      assertTrue(config.isScopeValid("myPortal-pcdef", "foo"));

      // With dependencies
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-def2.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getDependencies("foo"));
      assertNull(config.getDependencies("myPortal"));
      List<String> deps = config.getDependencies("myPortal-pcdef");
      assertTrue(deps != null && deps.size() == 3);
      names = config.getPortalContainerNames("foo");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal-pcdef", names.get(0));
      names = config.getPortalContainerNames("myPortal");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal", names.get(0));
      names = config.getPortalContainerNames("myPortal-pcdef");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal-pcdef", names.get(0));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("foo"));
      assertEquals("myPortal", config.getPortalContainerName("myPortal"));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("myPortal-pcdef"));
      assertEquals("myRest", config.getRestContextName("foo"));
      assertEquals("myRest", config.getRestContextName("myPortal"));
      assertEquals("myRest-pcdef", config.getRestContextName("myPortal-pcdef"));
      assertEquals("my-exo-domain", config.getRealmName("foo"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal"));
      assertEquals("my-exo-domain-pcdef", config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertTrue(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-pcdef"));
      assertFalse(config.isScopeValid("foo", "foo"));
      assertFalse(config.isScopeValid("myPortal", "foo"));
      assertTrue(config.isScopeValid("myPortal-pcdef", "foo"));
   }

   public void testSettings()
   {
      // Without settings and without portal definition
      RootContainer rootContainer = createRootContainer("portal-container-config-with-default-values.xml");
      PortalContainerConfig config =
         (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertNull(config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("myPortal-pcdef", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertNull(config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // Without settings and with portal definition
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertNull(config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // With internal settings
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-settings.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "foo"));
      assertNull(config.getSetting("foo", "foo2"));
      assertNull(config.getSetting("myPortal", "foo2"));
      assertNull(config.getSetting("myPortal-pcdef", "foo2"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "string"));
      assertEquals(new Integer(10), config.getSetting("myPortal-pcdef", "int"));
      assertEquals(new Long(10), config.getSetting("myPortal-pcdef", "long"));
      assertEquals(new Double(10), config.getSetting("myPortal-pcdef", "double"));
      assertEquals(new Boolean(true), config.getSetting("myPortal-pcdef", "boolean"));
      assertNull(config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // With external settings
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-external-settings.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertNull(config.getSetting("foo", "foo2"));
      assertNull(config.getSetting("myPortal", "foo2"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "foo2"));
      assertEquals("new value", config.getSetting("myPortal-pcdef", "string"));
      assertEquals("20", config.getSetting("myPortal-pcdef", "int"));
      assertEquals("20", config.getSetting("myPortal-pcdef", "long"));
      assertEquals("20", config.getSetting("myPortal-pcdef", "double"));
      assertEquals("false", config.getSetting("myPortal-pcdef", "boolean"));
      assertNull(config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // With external settings in exo-conf directory
      String path = TestPortalContainerConfig.class.getResource("portal-container-config-with-default-values-and-with-external-settings2.xml").getPath();
      path = path.substring(0, path.lastIndexOf('/'));
      String oldPath = System.getProperty(J2EEServerInfo.EXO_CONF_PARAM);
      
      try
      {
         System.setProperty(J2EEServerInfo.EXO_CONF_PARAM, path);
         rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-external-settings2.xml");
         config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
         assertNull(config.getSetting("foo", "foo"));
         assertNull(config.getSetting("myPortal", "foo"));
         assertNull(config.getSetting("myPortal-pcdef", "foo"));
         assertNull(config.getSetting("foo", "foo2"));
         assertNull(config.getSetting("myPortal", "foo2"));
         assertEquals("value 2", config.getSetting("myPortal-pcdef", "foo2"));
         assertEquals("new value 2", config.getSetting("myPortal-pcdef", "string"));
         assertEquals("22", config.getSetting("myPortal-pcdef", "int"));
         assertEquals("22", config.getSetting("myPortal-pcdef", "long"));
         assertEquals("22", config.getSetting("myPortal-pcdef", "double"));
         assertEquals("true", config.getSetting("myPortal-pcdef", "boolean"));
         assertNull(config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertNull(config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertNull(config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertNull(config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertNull(config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
         assertNull(config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
         assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));
      }
      finally
      {
         if (oldPath == null)
         {
            System.getProperties().remove(J2EEServerInfo.EXO_CONF_PARAM);
         }
         else
         {
            System.setProperty(J2EEServerInfo.EXO_CONF_PARAM, oldPath);
         }
      }
      
      // With both settings internal and external
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-both-settings.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "foo"));
      assertNull(config.getSetting("foo", "foo2"));
      assertNull(config.getSetting("myPortal", "foo2"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "foo2"));
      assertEquals("new value", config.getSetting("myPortal-pcdef", "string"));
      assertEquals(new Integer(20), config.getSetting("myPortal-pcdef", "int"));
      assertEquals(new Long(20), config.getSetting("myPortal-pcdef", "long"));
      assertEquals(new Double(20), config.getSetting("myPortal-pcdef", "double"));
      assertEquals(new Boolean(false), config.getSetting("myPortal-pcdef", "boolean"));
      assertNull(config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertNull(config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertNull(config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));      
   }
}
