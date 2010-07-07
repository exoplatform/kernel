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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
      assertTrue(config.isPortalContainerName("portal"));
      assertFalse(config.isPortalContainerName("myPortal"));
      assertFalse(config.isPortalContainerName("myPortal-dpcdef"));
      assertFalse(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-default-values.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal", config.getDefaultPortalContainer());
      assertEquals("myRest", config.getDefaultRestContext());
      assertEquals("my-exo-domain", config.getDefaultRealmName());
      assertFalse(config.isPortalContainerName("portal"));
      assertTrue(config.isPortalContainerName("myPortal"));
      assertFalse(config.isPortalContainerName("myPortal-dpcdef"));
      assertFalse(config.hasDefinition());

      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal-dpcdef", config.getDefaultPortalContainer());
      assertEquals("myRest-dpcdef", config.getDefaultRestContext());
      assertEquals("my-exo-domain-dpcdef", config.getDefaultRealmName());
      assertFalse(config.isPortalContainerName("portal"));
      assertFalse(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-dpcdef"));      
      assertFalse(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal", config.getDefaultPortalContainer());
      assertEquals("myRest", config.getDefaultRestContext());
      assertEquals("my-exo-domain", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-defs.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal", config.getDefaultPortalContainer());
      assertEquals("myRest", config.getDefaultRestContext());
      assertEquals("my-exo-domain", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());

      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-portal-def-with-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal-dpcdef", config.getDefaultPortalContainer());
      assertEquals("myRest-dpcdef", config.getDefaultRestContext());
      assertEquals("my-exo-domain-dpcdef", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-no-default-values-but-with-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal-pcdef", config.getDefaultPortalContainer());
      assertEquals("myRest-pcdef", config.getDefaultRestContext());
      assertEquals("my-exo-domain-pcdef", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-no-default-values-but-with-portal-defs.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("portal", config.getDefaultPortalContainer());
      assertEquals("myRest", config.getDefaultRestContext());
      assertEquals("my-exo-domain", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());

      rootContainer = createRootContainer("portal-container-config-with-no-default-values-but-with-portal-defs2.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);

      assertEquals("myPortal-pcdef", config.getDefaultPortalContainer());
      assertEquals("myRest-pcdef", config.getDefaultRestContext());
      assertEquals("my-exo-domain-pcdef", config.getDefaultRealmName());
      assertTrue(config.hasDefinition());
   }

   public void testChanges()
   {
      Set<String> s;
      try
      {
         createRootContainer("portal-container-config-with-no-default-values-but-with-portal-defs.xml");
         s = TestPortalContainerDefinitionChange.NAMES.get("change1");
         assertNotNull(s);
         assertEquals(2, s.size());
         assertTrue(s.contains("portal"));
         assertTrue(s.contains("myPortal-pcdef"));

         s = TestPortalContainerDefinitionChange.NAMES.get("change2");
         assertNotNull(s);
         assertEquals(1, s.size());
         assertTrue(s.contains("portal"));

         s = TestPortalContainerDefinitionChange.NAMES.get("change3");
         assertNotNull(s);
         assertEquals(1, s.size());
         assertTrue(s.contains("myPortal-pcdef"));

         s = TestPortalContainerDefinitionChange.NAMES.get("change4");
         assertNotNull(s);
         assertEquals(2, s.size());
         assertTrue(s.contains("portal"));
         assertTrue(s.contains("myPortal-pcdef"));

         s = TestPortalContainerDefinitionChange.NAMES.get("change5");
         assertNotNull(s);
         assertEquals(1, s.size());
         assertTrue(s.contains("portal"));

         s = TestPortalContainerDefinitionChange.NAMES.get("change6");
         assertNull(s);
      }
      finally
      {
         TestPortalContainerDefinitionChange.NAMES.clear();
      }

      try
      {
         createRootContainer("portal-container-config-with-no-default-values-but-with-portal-defs.xml", "change6");
         s = TestPortalContainerDefinitionChange.NAMES.get("change6");
         assertNotNull(s);
         assertEquals(1, s.size());
         assertTrue(s.contains("portal"));
      }
      finally
      {
         TestPortalContainerDefinitionChange.NAMES.clear();
      }
   }

   public void testDependencies()
   {

      // Empty
      RootContainer rootContainer = createRootContainer("portal-container-config-with-no-default-values.xml");
      PortalContainerConfig config =
         (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getDependencies(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME));
      assertNull(config.getDependencies("foo"));
      assertNull(config.getDependencies("myPortal"));
      assertNull(config.getDependencies("myPortal-pcdef"));
      List<String> names = config.getPortalContainerNames("foo");
      assertTrue(names != null && !names.isEmpty());
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, names.get(0));
      names = config.getPortalContainerNames("myPortal");
      assertTrue(names != null && !names.isEmpty());
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, names.get(0));
      names = config.getPortalContainerNames("myPortal-pcdef");
      assertTrue(names != null && !names.isEmpty());
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, names.get(0));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getPortalContainerName("foo"));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getPortalContainerName("myPortal"));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getPortalContainerName("myPortal-pcdef"));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getRestContextName("foo"));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getRestContextName("myPortal"));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getRestContextName("myPortal-pcdef"));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getRealmName("foo"));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getRealmName("myPortal"));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertFalse(config.isPortalContainerName("myPortal"));
      assertFalse(config.isPortalContainerName("myPortal-pcdef"));
      assertTrue(config.isPortalContainerName(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME));
      // Needed for backward compatibility
      assertTrue(config.isScopeValid(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, "foo"));
      assertFalse(config.isScopeValid("foo", "foo"));
      assertFalse(config.isScopeValid("myPortal", "foo"));
      assertFalse(config.isScopeValid("myPortal-pcdef", "foo"));
      assertFalse(config.hasDefinition());

      // Empty with AddDependencies, AddDependenciesBefore and AddDependenciesAfter
      String[] profiles =
         {"AddDependencies", "AddDependenciesBefore-No-Target", "AddDependenciesBefore-With-Fake-Target",
            "AddDependenciesAfter-No-Target", "AddDependenciesAfter-With-Fake-Target"};

      List<String> deps;
      for (String profile : profiles)
      {
         rootContainer = createRootContainer("portal-container-config-with-no-default-values2.xml", profile);
         config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
         deps = config.getDependencies(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME);
         assertTrue(deps != null && deps.size() == 1 && deps.contains("foo"));
         deps = config.getDependencies("foo");
         assertTrue(deps != null && deps.size() == 1 && deps.contains("foo"));
         deps = config.getDependencies("myPortal");
         assertTrue(deps != null && deps.size() == 1 && deps.contains("foo"));
         deps = config.getDependencies("myPortal-pcdef");
         assertTrue(deps != null && deps.size() == 1 && deps.contains("foo"));
         names = config.getPortalContainerNames("foo");
         assertTrue(names != null && !names.isEmpty());
         assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, names.get(0));
         names = config.getPortalContainerNames("myPortal");
         assertTrue(names != null && names.isEmpty());
         names = config.getPortalContainerNames("myPortal-pcdef");
         assertTrue(names != null && names.isEmpty());
         assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getPortalContainerName("foo"));
         assertNull(config.getPortalContainerName("myPortal"));
         assertNull(config.getPortalContainerName("myPortal-pcdef"));
         assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getRestContextName("foo"));
         assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getRestContextName("myPortal"));
         assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getRestContextName("myPortal-pcdef"));
         assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getRealmName("foo"));
         assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getRealmName("myPortal"));
         assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getRealmName("myPortal-pcdef"));
         assertFalse(config.isPortalContainerName("foo"));
         assertFalse(config.isPortalContainerName("myPortal"));
         assertFalse(config.isPortalContainerName("myPortal-pcdef"));
         assertTrue(config.isPortalContainerName(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME));
         // Needed for backward compatibility
         assertFalse(config.isScopeValid("foo", "foo"));
         assertFalse(config.isScopeValid("myPortal", "foo"));
         assertFalse(config.isScopeValid("myPortal-pcdef", "foo"));
         assertTrue(config.isScopeValid(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, "foo"));
         assertFalse(config.hasDefinition());
      }
      // Without dependencies
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getDependencies("foo"));
      assertNull(config.getDependencies("myPortal"));
      assertNull(config.getDependencies("myPortal-pcdef"));
      names = config.getPortalContainerNames("foo");
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
      assertFalse(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-pcdef"));
      // Needed for backward compatibility
      assertFalse(config.isScopeValid("foo", "foo"));
      assertTrue(config.isScopeValid("myPortal", "foo"));
      assertFalse(config.isScopeValid("myPortal-pcdef", "foo"));
      assertTrue(config.isScopeValid("myPortal", "myPortal"));
      assertTrue(config.isScopeValid("myPortal-pcdef", "myPortal-pcdef"));
      assertTrue(config.hasDefinition());

      // Without dependencies and with no portal container name
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-empty-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getDependencies("foo"));
      assertNull(config.getDependencies("myPortal"));
      assertNull(config.getDependencies("myPortal-pcdef"));
      names = config.getPortalContainerNames("foo");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal", names.get(0));
      names = config.getPortalContainerNames("myPortal");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal", names.get(0));
      names = config.getPortalContainerNames("myPortal-pcdef");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal", names.get(0));
      assertEquals("myPortal", config.getPortalContainerName("foo"));
      assertEquals("myPortal", config.getPortalContainerName("myPortal"));
      assertEquals("myPortal", config.getPortalContainerName("myPortal-pcdef"));
      assertEquals("myRest", config.getRestContextName("foo"));
      assertEquals("myRest", config.getRestContextName("myPortal"));
      assertEquals("myRest", config.getRestContextName("myPortal-pcdef"));
      assertEquals("my-exo-domain", config.getRealmName("foo"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertTrue(config.isPortalContainerName("myPortal"));
      assertFalse(config.isPortalContainerName("myPortal-pcdef"));

      // Without dependencies and with no rest context name an realm name
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-empty-portal-def2.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getDependencies("foo"));
      assertNull(config.getDependencies("myPortal"));
      assertNull(config.getDependencies("myPortal-pcdef"));
      names = config.getPortalContainerNames("foo");
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
      assertEquals("myRest", config.getRestContextName("myPortal-pcdef"));
      assertEquals("my-exo-domain", config.getRealmName("foo"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertFalse(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-pcdef"));
      // Needed for backward compatibility
      assertFalse(config.isScopeValid("foo", "foo"));
      assertTrue(config.isScopeValid("myPortal", "foo"));
      assertFalse(config.isScopeValid("myPortal-pcdef", "foo"));
      assertTrue(config.isScopeValid("myPortal", "myPortal"));
      assertTrue(config.isScopeValid("myPortal-pcdef", "myPortal-pcdef"));
      assertTrue(config.hasDefinition());

      // Without dependencies and with default portal container definition
      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-portal-def-with-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      deps = config.getDependencies("foo");
      assertTrue(deps != null && deps.size() == 1 && deps.contains("fooX"));
      deps = config.getDependencies("myPortal");
      assertTrue(deps != null && deps.size() == 1 && deps.contains("fooX"));
      deps = config.getDependencies("myPortal-pcdef");
      assertTrue(deps != null && deps.size() == 1 && deps.contains("fooX"));
      names = config.getPortalContainerNames("fooX");
      assertTrue(names != null && !names.isEmpty());
      assertEquals(2, names.size());
      assertTrue(names.contains("myPortal-dpcdef"));
      assertTrue(names.contains("myPortal-pcdef"));
      names = config.getPortalContainerNames("foo");
      assertTrue(names != null && names.isEmpty());
      names = config.getPortalContainerNames("myPortal");
      assertTrue(names != null && names.isEmpty());
      names = config.getPortalContainerNames("myPortal-pcdef");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal-pcdef", names.get(0));
      assertNull(config.getPortalContainerName("foo"));
      assertNull(config.getPortalContainerName("myPortal"));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("myPortal-pcdef"));
      assertEquals("myRest-dpcdef", config.getRestContextName("foo"));
      assertEquals("myRest-dpcdef", config.getRestContextName("myPortal"));
      assertEquals("myRest-pcdef", config.getRestContextName("myPortal-pcdef"));
      assertEquals("my-exo-domain-dpcdef", config.getRealmName("foo"));
      assertEquals("my-exo-domain-dpcdef", config.getRealmName("myPortal"));
      assertEquals("my-exo-domain-pcdef", config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertFalse(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-pcdef"));
      assertFalse(config.isScopeValid("foo", "fooX"));
      assertFalse(config.isScopeValid("myPortal", "fooX"));
      assertTrue(config.isScopeValid("myPortal-pcdef", "fooX"));
      assertTrue(config.hasDefinition());

      profiles =
         new String[]{"AddDependencies", "AddDependenciesBefore-No-Target", "AddDependenciesBefore-With-Fake-Target",
            "AddDependenciesBefore-With-Target", "AddDependenciesAfter-No-Target",
            "AddDependenciesAfter-With-Fake-Target", "AddDependenciesAfter-With-Target"};

      for (String profile : profiles)
      {
         rootContainer =
            createRootContainer(
               "portal-container-config-with-default-values-and-with-portal-def-with-default-portal-def.xml",
               "with-profiles", profile);
         config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
         deps = config.getDependencies("foo");
         assertTrue(deps != null && deps.size() == 2 && deps.contains("fooX") && deps.contains("foo"));
         int index = deps.indexOf("foo");
         if (profile.equals("AddDependenciesBefore-No-Target")
            || profile.equals("AddDependenciesBefore-With-Fake-Target")
            || profile.equals("AddDependenciesBefore-With-Target"))
         {
            assertEquals(0, index);
         }
         else
         {
            assertEquals(1, index);
         }
         deps = config.getDependencies("myPortal");
         assertTrue(deps != null && deps.size() == 2 && deps.contains("fooX") && deps.contains("foo"));
         deps = config.getDependencies("myPortal-pcdef");
         assertTrue(deps != null && deps.size() == 2 && deps.contains("fooX") && deps.contains("foo"));
         names = config.getPortalContainerNames("fooX");
         assertTrue(names != null && !names.isEmpty());
         assertEquals(2, names.size());
         assertTrue(names.contains("myPortal-dpcdef"));
         assertTrue(names.contains("myPortal-pcdef"));
         names = config.getPortalContainerNames("foo");
         assertTrue(names != null && !names.isEmpty());
         assertEquals(1, names.size());
         assertEquals("myPortal-dpcdef", names.get(0));
         names = config.getPortalContainerNames("myPortal");
         assertTrue(names != null && names.isEmpty());
         names = config.getPortalContainerNames("myPortal-pcdef");
         assertTrue(names != null && !names.isEmpty());
         assertEquals("myPortal-pcdef", names.get(0));
         assertEquals("myPortal-dpcdef", config.getPortalContainerName("foo"));
         assertNull(config.getPortalContainerName("myPortal"));
         assertEquals("myPortal-pcdef", config.getPortalContainerName("myPortal-pcdef"));
         assertEquals("myRest-dpcdef", config.getRestContextName("foo"));
         assertEquals("myRest-dpcdef", config.getRestContextName("myPortal"));
         assertEquals("myRest-pcdef", config.getRestContextName("myPortal-pcdef"));
         assertEquals("my-exo-domain-dpcdef", config.getRealmName("foo"));
         assertEquals("my-exo-domain-dpcdef", config.getRealmName("myPortal"));
         assertEquals("my-exo-domain-pcdef", config.getRealmName("myPortal-pcdef"));
         assertFalse(config.isPortalContainerName("foo"));
         assertFalse(config.isPortalContainerName("myPortal"));
         assertTrue(config.isPortalContainerName("myPortal-pcdef"));
         assertFalse(config.isScopeValid("foo", "fooX"));
         assertFalse(config.isScopeValid("myPortal", "fooX"));
         assertTrue(config.isScopeValid("myPortal-pcdef", "fooX"));
         assertTrue(config.hasDefinition());
      }

      // With dependencies
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-def2.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getDependencies("foo"));
      assertNull(config.getDependencies("myPortal"));
      deps = config.getDependencies("myPortal-pcdef");
      assertTrue(deps != null && deps.size() == 3);
      names = config.getPortalContainerNames("foo");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal-pcdef", names.get(0));
      names = config.getPortalContainerNames("myPortal");
      assertTrue(names != null && names.isEmpty());
      names = config.getPortalContainerNames("myPortal-pcdef");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal-pcdef", names.get(0));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("foo"));
      assertNull(config.getPortalContainerName("myPortal"));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("myPortal-pcdef"));
      assertEquals("myRest", config.getRestContextName("foo"));
      assertEquals("myRest", config.getRestContextName("myPortal"));
      assertEquals("myRest-pcdef", config.getRestContextName("myPortal-pcdef"));
      assertEquals("my-exo-domain", config.getRealmName("foo"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal"));
      assertEquals("my-exo-domain-pcdef", config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertFalse(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-pcdef"));
      assertFalse(config.isScopeValid("foo", "foo"));
      assertFalse(config.isScopeValid("myPortal", "foo"));
      assertTrue(config.isScopeValid("myPortal-pcdef", "foo"));
      assertFalse(config.isScopeValid("myPortal-pcdef", "myPortal"));
      assertFalse(config.isScopeValid("myPortal-pcdef", "fooY"));
      assertTrue(config.hasDefinition());

      for (String profile : profiles)
      {
         rootContainer =
            createRootContainer("portal-container-config-with-default-values-and-with-portal-def2.xml",
               "with-profiles", profile);
         config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
         deps = config.getDependencies("foo");
         assertTrue(deps != null && deps.size() == 1 && deps.contains("fooX"));
         deps = config.getDependencies("myPortal");
         assertTrue(deps != null && deps.size() == 1 && deps.contains("fooX"));
         deps = config.getDependencies("myPortal-pcdef");
         assertTrue(deps != null && deps.size() == 4);
         int index = deps.indexOf("fooX");
         if (profile.equals("AddDependenciesBefore-No-Target") || profile.equals("AddDependenciesBefore-With-Fake-Target"))
         {
            assertEquals(0, index);
         }
         else if (profile.equals("AddDependenciesBefore-With-Target"))
         {
            assertEquals(1, index);
         }
         else if (profile.equals("AddDependenciesAfter-With-Target"))
         {
            assertEquals(2, index);
         }
         else
         {
            assertEquals(3, index);            
         }
         names = config.getPortalContainerNames("foo");
         assertTrue(names != null && !names.isEmpty());
         assertEquals("myPortal-pcdef", names.get(0));
         names = config.getPortalContainerNames("myPortal");
         assertTrue(names != null && names.isEmpty());
         names = config.getPortalContainerNames("myPortal-pcdef");
         assertTrue(names != null && !names.isEmpty());
         assertEquals("myPortal-pcdef", names.get(0));
         assertEquals("myPortal-pcdef", config.getPortalContainerName("foo"));
         assertNull(config.getPortalContainerName("myPortal"));
         assertEquals("myPortal-pcdef", config.getPortalContainerName("myPortal-pcdef"));
         assertEquals("myRest", config.getRestContextName("foo"));
         assertEquals("myRest", config.getRestContextName("myPortal"));
         assertEquals("myRest-pcdef", config.getRestContextName("myPortal-pcdef"));
         assertEquals("my-exo-domain", config.getRealmName("foo"));
         assertEquals("my-exo-domain", config.getRealmName("myPortal"));
         assertEquals("my-exo-domain-pcdef", config.getRealmName("myPortal-pcdef"));
         assertFalse(config.isPortalContainerName("foo"));
         assertFalse(config.isPortalContainerName("myPortal"));
         assertTrue(config.isPortalContainerName("myPortal-pcdef"));
         assertFalse(config.isScopeValid("foo", "foo"));
         assertFalse(config.isScopeValid("myPortal", "foo"));
         assertTrue(config.isScopeValid("myPortal-pcdef", "foo"));
         assertTrue(config.hasDefinition());
      }

      // With dependencies and with default portal container definition
      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-portal-def-with-default-portal-def2.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      deps = config.getDependencies("foo");
      assertTrue(deps != null && deps.size() == 1 && deps.contains("fooX"));
      deps = config.getDependencies("myPortal");
      assertTrue(deps != null && deps.size() == 1 && deps.contains("fooX"));
      deps = config.getDependencies("myPortal-pcdef");
      assertTrue(deps != null && deps.size() == 3);
      names = config.getPortalContainerNames("fooX");
      assertTrue(names != null && !names.isEmpty());
      assertEquals(1, names.size());
      assertEquals("myPortal", names.get(0));
      names = config.getPortalContainerNames("foo");
      assertTrue(names != null && !names.isEmpty());
      assertEquals(1, names.size());
      assertEquals("myPortal-pcdef", names.get(0));
      names = config.getPortalContainerNames("myPortal");
      assertTrue(names != null && names.isEmpty());
      names = config.getPortalContainerNames("myPortal-pcdef");
      assertTrue(names != null && !names.isEmpty());
      assertEquals("myPortal-pcdef", names.get(0));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("foo"));
      assertNull(config.getPortalContainerName("myPortal"));
      assertEquals("myPortal-pcdef", config.getPortalContainerName("myPortal-pcdef"));
      assertEquals("myRest", config.getRestContextName("foo"));
      assertEquals("myRest", config.getRestContextName("myPortal"));
      assertEquals("myRest-pcdef", config.getRestContextName("myPortal-pcdef"));
      assertEquals("my-exo-domain", config.getRealmName("foo"));
      assertEquals("my-exo-domain", config.getRealmName("myPortal"));
      assertEquals("my-exo-domain-pcdef", config.getRealmName("myPortal-pcdef"));
      assertFalse(config.isPortalContainerName("foo"));
      assertFalse(config.isPortalContainerName("myPortal"));
      assertTrue(config.isPortalContainerName("myPortal-pcdef"));
      assertFalse(config.isScopeValid("foo", "foo"));
      assertFalse(config.isScopeValid("myPortal", "foo"));
      assertTrue(config.isScopeValid("myPortal-pcdef", "foo"));
      assertTrue(config.hasDefinition());
   }

   public void testSettings()
   {
      // Without settings, without portal definition and without default values
      RootContainer rootContainer = createRootContainer("portal-container-config-with-no-default-values.xml");
      PortalContainerConfig config =
         (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getSetting("foo",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getSetting("myPortal",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getSetting("foo",
         PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getSetting("myPortal",
         PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getSetting("myPortal-pcdef",
         PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getSetting("foo",
         PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getSetting("myPortal",
         PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getSetting("myPortal-pcdef",
         PortalContainerConfig.REALM_SETTING_NAME));

      rootContainer = createRootContainer("portal-container-config-with-no-default-values.xml", "with-profiles");
      config =
         (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getSetting("foo",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getSetting("myPortal",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_PORTAL_CONTAINER_NAME, config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getSetting("foo",
         PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getSetting("myPortal",
         PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REST_CONTEXT_NAME, config.getSetting("myPortal-pcdef",
         PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getSetting("foo",
         PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getSetting("myPortal",
         PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals(PortalContainerConfig.DEFAULT_REALM_NAME, config.getSetting("myPortal-pcdef",
         PortalContainerConfig.REALM_SETTING_NAME));      
      assertEquals("value1", config.getSetting("foo", "string"));
      assertEquals("value1", config.getSetting("foo", "stringX"));
      
      // Without settings and without portal definition
      rootContainer = createRootContainer("portal-container-config-with-default-values.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal-pcdef", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // Without settings, without portal definition and with empty default portal container definition
      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-empty-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal-pcdef", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // Without settings, without portal definition and with default portal container definition
      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertEquals("myPortal-dpcdef", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-dpcdef", config
         .getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-dpcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest-dpcdef", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-dpcdef", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-dpcdef", config
         .getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain-dpcdef", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-dpcdef", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-dpcdef", config
         .getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("value0", config.getSetting("foo", "string"));
      assertEquals(new Integer(100), config.getSetting("foo", "int"));
      assertEquals(new Long(100), config.getSetting("foo", "long"));
      assertEquals(new Double(100), config.getSetting("foo", "double"));
      assertEquals(new Boolean(false), config.getSetting("foo", "boolean"));
      assertEquals("value0", config.getSetting("myPortal", "string"));
      assertEquals(new Integer(100), config.getSetting("myPortal", "int"));
      assertEquals(new Long(100), config.getSetting("myPortal", "long"));
      assertEquals(new Double(100), config.getSetting("myPortal", "double"));
      assertEquals(new Boolean(false), config.getSetting("myPortal", "boolean"));
      assertEquals("value0", config.getSetting("myPortal-pcdef", "string"));
      assertEquals(new Integer(100), config.getSetting("myPortal-pcdef", "int"));
      assertEquals(new Long(100), config.getSetting("myPortal-pcdef", "long"));
      assertEquals(new Double(100), config.getSetting("myPortal-pcdef", "double"));
      assertEquals(new Boolean(false), config.getSetting("myPortal-pcdef", "boolean"));

      // Without settings and with portal definition
      rootContainer = createRootContainer("portal-container-config-with-default-values-and-with-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // Without settings, with portal definition and with default portal definition
      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-portal-def-with-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertNull(config.getSetting("myPortal-pcdef", "foo"));
      assertEquals("myPortal-dpcdef", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-dpcdef", config
         .getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest-dpcdef", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-dpcdef", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain-dpcdef", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-dpcdef", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
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
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // With internal settings and default portal definition
      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-settings-with-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertNull(config.getSetting("foo", "foo"));
      assertNull(config.getSetting("myPortal", "foo"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "foo"));
      assertEquals("value", config.getSetting("foo", "foo2"));
      assertEquals("value", config.getSetting("myPortal", "foo2"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "foo2"));
      assertNull(config.getSetting("foo", "foo3"));
      assertNull(config.getSetting("myPortal", "foo3"));
      assertNull(config.getSetting("myPortal-pcdef", "foo3"));
      assertEquals("myPortal-dpcdef", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-dpcdef", config
         .getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest-dpcdef", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-dpcdef", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain-dpcdef", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-dpcdef", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("value0", config.getSetting("foo", "string"));
      assertEquals(new Integer(100), config.getSetting("foo", "int"));
      assertEquals(new Long(100), config.getSetting("foo", "long"));
      assertEquals(new Double(100), config.getSetting("foo", "double"));
      assertEquals(new Boolean(false), config.getSetting("foo", "boolean"));
      assertEquals("value0", config.getSetting("myPortal", "string"));
      assertEquals(new Integer(100), config.getSetting("myPortal", "int"));
      assertEquals(new Long(100), config.getSetting("myPortal", "long"));
      assertEquals(new Double(100), config.getSetting("myPortal", "double"));
      assertEquals(new Boolean(false), config.getSetting("myPortal", "boolean"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "string"));
      assertEquals(new Integer(10), config.getSetting("myPortal-pcdef", "int"));
      assertEquals(new Long(10), config.getSetting("myPortal-pcdef", "long"));
      assertEquals(new Double(10), config.getSetting("myPortal-pcdef", "double"));
      assertEquals(new Boolean(true), config.getSetting("myPortal-pcdef", "boolean"));

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
      assertEquals("myPortal-pcdef-myRest-pcdef-my-exo-domain-pcdef-value-new value", config.getSetting(
         "myPortal-pcdef", "complex-value"));
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      // Simple usecase from gatein
      rootContainer = createRootContainer("sample-gtn-configuration.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertEquals("../gatein/data", config.getSetting("portal", "gatein.data.dir"));
      assertEquals("../gatein/data/db", config.getSetting("portal", "gatein.db.data.dir"));
      assertEquals("jdbc:hsqldb:file:../gatein/data/db/data/jdbcjcr_portal", config.getSetting("portal",
         "gatein.jcr.datasource.url"));

      // With external settings, with several portal container definitions and with 
      // default portal container definition
      rootContainer =
         createRootContainer("portal-container-config-with-default-values-and-with-external-settings-with-default-portal-def.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertEquals("value0", config.getSetting("foo", "foo"));
      assertEquals("value0", config.getSetting("myPortal", "foo"));
      assertEquals("value0", config.getSetting("myPortal0", "foo"));
      assertEquals("value0", config.getSetting("myPortal-pcdef", "foo"));
      assertEquals("value0", config.getSetting("myPortal2", "foo"));
      assertNull(config.getSetting("foo", "foo2"));
      assertNull(config.getSetting("myPortal", "foo2"));
      assertNull(config.getSetting("myPortal0", "foo2"));
      assertEquals("value", config.getSetting("myPortal-pcdef", "foo2"));
      assertNull(config.getSetting("myPortal2", "foo2"));
      assertEquals("new value0", config.getSetting("foo", "string"));
      assertEquals("200", config.getSetting("foo", "int"));
      assertEquals("200", config.getSetting("foo", "long"));
      assertEquals("200", config.getSetting("foo", "double"));
      assertEquals("true", config.getSetting("foo", "boolean"));
      assertEquals("myPortal-myRest-my-exo-domain-value0-new value0", config.getSetting("foo", "complex-value2"));
      assertEquals("new value0", config.getSetting("myPortal", "string"));
      assertEquals("200", config.getSetting("myPortal", "int"));
      assertEquals("200", config.getSetting("myPortal", "long"));
      assertEquals("200", config.getSetting("myPortal", "double"));
      assertEquals("true", config.getSetting("myPortal", "boolean"));
      assertEquals("myPortal-myRest-my-exo-domain-value0-new value0", config.getSetting("myPortal", "complex-value2"));
      assertEquals("new value0", config.getSetting("myPortal0", "string"));
      assertEquals("200", config.getSetting("myPortal0", "int"));
      assertEquals("200", config.getSetting("myPortal0", "long"));
      assertEquals("200", config.getSetting("myPortal0", "double"));
      assertEquals("true", config.getSetting("myPortal0", "boolean"));
      assertEquals("myPortal0-myRest0-my-exo-domain0-value0-new value0", config.getSetting("myPortal0",
         "complex-value2"));
      assertEquals("new value", config.getSetting("myPortal-pcdef", "string"));
      assertEquals("20", config.getSetting("myPortal-pcdef", "int"));
      assertEquals("20", config.getSetting("myPortal-pcdef", "long"));
      assertEquals("20", config.getSetting("myPortal-pcdef", "double"));
      assertEquals("false", config.getSetting("myPortal-pcdef", "boolean"));
      assertEquals("myPortal-pcdef-myRest-pcdef-my-exo-domain-pcdef-value-new value", config.getSetting(
         "myPortal-pcdef", "complex-value"));
      assertEquals("myPortal-pcdef-myRest-pcdef-my-exo-domain-pcdef-value0-new value", config.getSetting(
         "myPortal-pcdef", "complex-value2"));
      assertEquals("new value0", config.getSetting("myPortal2", "string"));
      assertEquals("200", config.getSetting("myPortal2", "int"));
      assertEquals("200", config.getSetting("myPortal2", "long"));
      assertEquals("200", config.getSetting("myPortal2", "double"));
      assertEquals("true", config.getSetting("myPortal2", "boolean"));
      assertEquals("myPortal2-myRest2-my-exo-domain2-value0-new value0", config.getSetting("myPortal2",
         "complex-value2"));
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      String path =
         TestPortalContainerConfig.class.getResource(
            "portal-container-config-with-default-values-and-with-external-settings2.xml").getPath();
      path = path.substring(0, path.lastIndexOf('/'));
      String oldPath = System.getProperty(J2EEServerInfo.EXO_CONF_PARAM);

      try
      {
         System.setProperty(J2EEServerInfo.EXO_CONF_PARAM, path);
         // With external settings in exo-conf directory
         rootContainer =
            createRootContainer("portal-container-config-with-default-values-and-with-external-settings2.xml");
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
         assertEquals("myPortal-pcdef-myRest-pcdef-my-exo-domain-pcdef-value 2-new value 2", config.getSetting(
            "myPortal-pcdef", "complex-value"));
         assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
         assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
         assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.REALM_SETTING_NAME));

         // With external settings in exo-conf directory, with several portal container definitions 
         // and with default portal container definition
         rootContainer =
            createRootContainer("portal-container-config-with-default-values-and-with-external-settings-with-default-portal-def2.xml");
         config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
         assertEquals("value01", config.getSetting("foo", "foo"));
         assertEquals("value01", config.getSetting("myPortal", "foo"));
         assertEquals("value01", config.getSetting("myPortal0", "foo"));
         assertEquals("value01", config.getSetting("myPortal-pcdef", "foo"));
         assertEquals("value01", config.getSetting("myPortal2", "foo"));
         assertNull(config.getSetting("foo", "foo2"));
         assertNull(config.getSetting("myPortal", "foo2"));
         assertNull(config.getSetting("myPortal0", "foo2"));
         assertEquals("value 2", config.getSetting("myPortal-pcdef", "foo2"));
         assertNull(config.getSetting("myPortal2", "foo2"));
         assertEquals("new value01", config.getSetting("foo", "string"));
         assertEquals("2001", config.getSetting("foo", "int"));
         assertEquals("2001", config.getSetting("foo", "long"));
         assertEquals("2001", config.getSetting("foo", "double"));
         assertEquals("false", config.getSetting("foo", "boolean"));
         assertEquals("myPortal-myRest-my-exo-domain-value01-new value01", config.getSetting("foo", "complex-value2"));
         assertEquals("new value01", config.getSetting("myPortal", "string"));
         assertEquals("2001", config.getSetting("myPortal", "int"));
         assertEquals("2001", config.getSetting("myPortal", "long"));
         assertEquals("2001", config.getSetting("myPortal", "double"));
         assertEquals("false", config.getSetting("myPortal", "boolean"));
         assertEquals("myPortal-myRest-my-exo-domain-value01-new value01", config.getSetting("myPortal",
            "complex-value2"));
         assertEquals("new value01", config.getSetting("myPortal0", "string"));
         assertEquals("2001", config.getSetting("myPortal0", "int"));
         assertEquals("2001", config.getSetting("myPortal0", "long"));
         assertEquals("2001", config.getSetting("myPortal0", "double"));
         assertEquals("false", config.getSetting("myPortal0", "boolean"));
         assertEquals("myPortal0-myRest0-my-exo-domain0-value01-new value01", config.getSetting("myPortal0",
            "complex-value2"));
         assertEquals("new value 2", config.getSetting("myPortal-pcdef", "string"));
         assertEquals("22", config.getSetting("myPortal-pcdef", "int"));
         assertEquals("22", config.getSetting("myPortal-pcdef", "long"));
         assertEquals("22", config.getSetting("myPortal-pcdef", "double"));
         assertEquals("true", config.getSetting("myPortal-pcdef", "boolean"));
         assertEquals("myPortal-pcdef-myRest-pcdef-my-exo-domain-pcdef-value 2-new value 2", config.getSetting(
            "myPortal-pcdef", "complex-value"));
         assertEquals("myPortal-pcdef-myRest-pcdef-my-exo-domain-pcdef-value01-new value 2", config.getSetting(
            "myPortal-pcdef", "complex-value2"));
         assertEquals("new value01", config.getSetting("myPortal2", "string"));
         assertEquals("2001", config.getSetting("myPortal2", "int"));
         assertEquals("2001", config.getSetting("myPortal2", "long"));
         assertEquals("2001", config.getSetting("myPortal2", "double"));
         assertEquals("false", config.getSetting("myPortal2", "boolean"));
         assertEquals("myPortal2-myRest2-my-exo-domain2-value01-new value01", config.getSetting("myPortal2",
            "complex-value2"));
         assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
         assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
         assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.REALM_SETTING_NAME));

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
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));

      try
      {
         System.setProperty("TestPortalContainerConfig-string", "system value");
         System.setProperty("TestPortalContainerConfig-int", "50");
         // With both settings internal and external and default portal container definition
         rootContainer =
            createRootContainer("portal-container-config-with-default-values-and-with-both-settings-with-default-portal-def.xml");
         config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
         assertEquals("value0", config.getSetting("foo", "foo"));
         assertEquals("value0", config.getSetting("myPortal", "foo"));
         assertEquals("value0", config.getSetting("myPortal-pcdef", "foo"));
         assertEquals("value", config.getSetting("foo", "foo2"));
         assertEquals("value", config.getSetting("myPortal", "foo2"));
         assertEquals("value", config.getSetting("myPortal-pcdef", "foo2"));
         assertNull(config.getSetting("foo", "foo3"));
         assertNull(config.getSetting("myPortal", "foo3"));
         assertEquals("value", config.getSetting("myPortal-pcdef", "foo3"));
         assertEquals("-${foo3}-", config.getSetting("foo", "complex-value3"));
         assertEquals("-${foo3}-", config.getSetting("myPortal", "complex-value3"));
         assertEquals("-value-", config.getSetting("myPortal-pcdef", "complex-value3"));
         assertNull(config.getSetting("foo", "complex-value4"));
         assertNull(config.getSetting("myPortal", "complex-value4"));
         assertEquals("-value-", config.getSetting("myPortal-pcdef", "complex-value4"));
         assertEquals("-value-", config.getSetting("foo", "complex-value5"));
         assertEquals("-value-", config.getSetting("myPortal", "complex-value5"));
         assertEquals("-value-", config.getSetting("myPortal-pcdef", "complex-value5"));
         assertNull(config.getSetting("foo", "complex-value6"));
         assertNull(config.getSetting("myPortal", "complex-value6"));
         assertEquals("-value-", config.getSetting("myPortal-pcdef", "complex-value6"));
         assertNull(config.getSetting("foo", "complex-value7"));
         assertNull(config.getSetting("myPortal", "complex-value7"));
         assertEquals("-value-", config.getSetting("myPortal-pcdef", "complex-value7"));
         assertEquals("-${foo6}-", config.getSetting("foo", "complex-value8"));
         assertEquals("-${foo6}-", config.getSetting("myPortal", "complex-value8"));
         assertEquals("-value-", config.getSetting("myPortal-pcdef", "complex-value8"));
         assertEquals("-property_value_1-", config.getSetting("foo", "complex-value9"));
         assertEquals("-property_value_1-", config.getSetting("myPortal", "complex-value9"));
         assertEquals("-property_value_1-", config.getSetting("myPortal-pcdef", "complex-value9"));
         assertEquals("-system value-", config.getSetting("foo", "cpv1"));
         assertEquals("-system value-", config.getSetting("myPortal", "cpv1"));
         assertEquals("-system value-", config.getSetting("myPortal-pcdef", "cpv1"));
         assertEquals(new Integer(50), config.getSetting("foo", "cpv2"));
         assertEquals(new Integer(50), config.getSetting("myPortal", "cpv2"));
         assertEquals(new Integer(50), config.getSetting("myPortal-pcdef", "cpv2"));
         assertEquals("-property_value_1-", config.getSetting("foo", "cpv3"));
         assertEquals("-property_value_1-", config.getSetting("myPortal", "cpv3"));
         assertEquals("-property_value_1-", config.getSetting("myPortal-pcdef", "cpv3"));
         assertEquals("new value0", config.getSetting("foo", "string"));
         assertEquals(new Integer(200), config.getSetting("foo", "int"));
         assertEquals(new Integer(60), config.getSetting("foo", "int2"));
         assertEquals(new Long(200), config.getSetting("foo", "long"));
         assertEquals(new Double(200), config.getSetting("foo", "double"));
         assertEquals(new Boolean(true), config.getSetting("foo", "boolean"));
         assertEquals("new value0", config.getSetting("myPortal", "string"));
         assertEquals(new Integer(200), config.getSetting("myPortal", "int"));
         assertEquals(new Integer(60), config.getSetting("myPortal", "int2"));
         assertEquals(new Long(200), config.getSetting("myPortal", "long"));
         assertEquals(new Double(200), config.getSetting("myPortal", "double"));
         assertEquals(new Boolean(true), config.getSetting("myPortal", "boolean"));
         assertEquals("new value", config.getSetting("myPortal-pcdef", "string"));
         assertEquals(new Integer(20), config.getSetting("myPortal-pcdef", "int"));
         assertEquals(new Integer(60), config.getSetting("myPortal-pcdef", "int2"));
         assertEquals(new Long(20), config.getSetting("myPortal-pcdef", "long"));
         assertEquals(new Double(20), config.getSetting("myPortal-pcdef", "double"));
         assertEquals(new Boolean(false), config.getSetting("myPortal-pcdef", "boolean"));
         assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
         assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("myRest-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
         assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
         assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
         assertEquals("my-exo-domain-pcdef", config.getSetting("myPortal-pcdef",
            PortalContainerConfig.REALM_SETTING_NAME));
      }
      finally
      {
         System.getProperties().remove("TestPortalContainerConfig-string");
         System.getProperties().remove("TestPortalContainerConfig-int");
      }

      // With both settings internal and external, and with invalid values 
      rootContainer = createRootContainer("portal-container-config-with-invalid-values.xml");
      config = (PortalContainerConfig)rootContainer.getComponentInstanceOfType(PortalContainerConfig.class);
      assertEquals("value", config.getSetting("myPortal-pcdef", "internal-empty-value"));
      assertEquals("", config.getSetting("myPortal-pcdef", "external-empty-value"));
      assertEquals("", config.getSetting("myPortal-pcdef", "fake-value-4-string"));
      assertEquals(new Integer(10), config.getSetting("myPortal-pcdef", "fake-value-4-int"));
      assertEquals(new Integer(10), config.getSetting("myPortal-pcdef", "invalid-value-4-int"));
      assertEquals("myPortal", config.getSetting("foo", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal", config.getSetting("myPortal", PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myPortal-pcdef", config.getSetting("myPortal-pcdef",
         PortalContainerConfig.PORTAL_CONTAINER_SETTING_NAME));
      assertEquals("myRest", config.getSetting("foo", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("myRest", config.getSetting("myPortal-pcdef", PortalContainerConfig.REST_CONTEXT_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("foo", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal", PortalContainerConfig.REALM_SETTING_NAME));
      assertEquals("my-exo-domain", config.getSetting("myPortal-pcdef", PortalContainerConfig.REALM_SETTING_NAME));
   }

   public static class TestPortalContainerDefinitionChange implements PortalContainerDefinitionChange
   {

      public String name;

      public static Map<String, Set<String>> NAMES = new HashMap<String, Set<String>>();

      public void apply(PortalContainerDefinition pcd)
      {
         Set<String> names = NAMES.get(name);
         if (names == null)
         {
            names = new HashSet<String>();
            NAMES.put(name, names);
         }
         names.add(pcd.getName());
      }

   }
}
