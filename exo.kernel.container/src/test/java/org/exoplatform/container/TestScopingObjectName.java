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
 */
package org.exoplatform.container;

import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.support.ContainerBuilder;

import java.net.URL;

/**
 * @author <a href="anatoliy.bazko@exoplatform.com">Anatoliy Bazko</a>
 * @version $Id: TestScopingObjectName.java 34360 2009-07-22 23:58:59Z tolusha $
 */
public class TestScopingObjectName extends AbstractTestContainer
{
   public void testHasScopingObjectName()
   {
      URL rootURL = getClass().getResource("empty-config.xml");
      URL portalURL = getClass().getResource("empty-config.xml");
      assertNotNull(rootURL);
      assertNotNull(portalURL);
      //
      new ContainerBuilder().withRoot(rootURL).withPortal(portalURL).build();
      assertNull(RootContainer.getInstance().getScopingObjectName());
      assertNotNull(PortalContainer.getInstance().getScopingObjectName());
   }

}
