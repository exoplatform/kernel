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
package org.exoplatform.container.configuration;

import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestInitParamProfile extends AbstractProfileTest
{

   public void testNoProfile() throws Exception
   {
      Configuration config = getConfiguration("init-param-configuration.xml");
      Component component = config.getComponent("Component");
      InitParams initParams = component.getInitParams();
      ValueParam valueParam = initParams.getValueParam("param");
      assertEquals("empty", valueParam.getValue());
   }

   public void testFooProfile() throws Exception
   {
      Configuration config = getConfiguration("init-param-configuration.xml", "foo");
      Component component = config.getComponent("Component");
      InitParams initParams = component.getInitParams();
      ValueParam valueParam = initParams.getValueParam("param");
      assertEquals("foo", valueParam.getValue());
   }

   public void testFooBarProfiles() throws Exception
   {
      Configuration config = getConfiguration("init-param-configuration.xml", "foo", "bar");
      Component component = config.getComponent("Component");
      InitParams initParams = component.getInitParams();
      ValueParam valueParam = initParams.getValueParam("param");
      assertEquals("bar", valueParam.getValue());
   }
}