/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.container.multitenancy.bridge;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.multitenancy.CurrentTenantNotSetException;
import org.exoplatform.container.multitenancy.Tenant;

import java.security.PrivilegedAction;

/**
 * Tests of {@link TenantsServiceImpl}.
 *
 */
public class TestTenantsServiceImpl extends TestCase
{

   public static class SupportedPlugin extends BaseComponentPlugin implements CurrentTenantLookup
   {
      public Tenant getCurrentTenant() throws CurrentTenantNotSetException
      {
         throw new CurrentTenantNotSetException("not supported");
      }

      public boolean hasCurrentTenant()
      {
         return false;
      }
   }

   public static class NotSupportedPlugin extends BaseComponentPlugin
   {

   }

   protected ExoContainer parent;

   protected TenantsServiceImpl tenants;

   /**
    * {@inheritDoc}
    */
   protected void setUp() throws Exception
   {
      super.setUp();

      // create new root each test to have portal container clear 
      final RootContainer root = new RootContainer();
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            RootContainer singleton = RootContainer.getInstance();
            root.registerComponentInstance(ConfigurationManager.class,
               singleton.getComponentInstance(ConfigurationManager.class));
            root.start(true);
            return null;
         }
      });

      parent = root.getPortalContainer(PortalContainer.DEFAULT_PORTAL_CONTAINER_NAME);

      tenants = (TenantsServiceImpl)parent.getComponentInstanceOfType(TenantsServiceImpl.class);
   }

   public void testHasNoPluginsByDefault()
   {
      assertTrue(tenants.lookups.size() == 0);
      assertTrue(tenants.observers.size() == 0);
   }

   public void testAddSupportedPlugin()
   {
      ComponentPlugin plugin = new SupportedPlugin();
      tenants.addPlugin(plugin);
      assertTrue(tenants.lookups.contains(plugin));
   }

   public void testAddNotSupportedPlugin()
   {
      ComponentPlugin plugin = new NotSupportedPlugin();
      tenants.addPlugin(plugin);
      assertFalse(tenants.lookups.contains(plugin));
   }

   /**
    * Ensure that no Current Tenant by default.
    */
   public void testGetCurrentTanant() {
      try
      {
         tenants.getCurrentTenant();
         fail("Current Tenant should not be set by default (without eXo Cloud environment)");
      }
      catch (CurrentTenantNotSetException e)
      {
         // ok
      }
   }

   /**
    * Ensure that no Current Tenant by default.
    */
   public void testHasCurrentTanant() {
      assertFalse(tenants.hasCurrentTenant());
   }

}
