/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
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
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.container.jmx;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.jmx.support.SimpleManagementAware;
import org.exoplatform.container.support.ContainerBuilder;

import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPortalContainerManagedIntegration extends AbstractTestContainer
{

   public void testManagementContext()
   {
      URL rootURL = TestPortalContainerManagedIntegration.class.getResource("root-configuration.xml");
      URL portalURL = TestPortalContainerManagedIntegration.class.getResource("portal-configuration.xml");

      //
      RootContainer root = new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();
      ManagementContextImpl rootManagementContext = (ManagementContextImpl)root.getManagementContext();

      //
      PortalContainer portal = PortalContainer.getInstance();
      ManagementContextImpl portalManagementContext = (ManagementContextImpl)portal.getManagementContext();
      assertSame(root.getManagementContext(), portalManagementContext.getParent());
      assertSame(portal, portalManagementContext.findContainer());

      //
      SimpleManagementAware rootManagementAware = (SimpleManagementAware)root.getComponentInstance("RootManagementAware");
      ManagementContextImpl rootManagementAwareContext = (ManagementContextImpl)((ExoModelMBean)rootManagementAware.context).getManagementContext();
      assertSame(rootManagementContext, rootManagementAwareContext.getParent());

      //
      SimpleManagementAware portalManagementAware = (SimpleManagementAware)portal.getComponentInstance("PortalManagementAware");
      ManagementContextImpl portalManagementAwareContext = (ManagementContextImpl)((ExoModelMBean)portalManagementAware.context).getManagementContext();
      assertSame(portalManagementContext, portalManagementAwareContext.getParent());
   }

}
