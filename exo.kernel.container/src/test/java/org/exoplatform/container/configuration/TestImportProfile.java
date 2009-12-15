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

import org.exoplatform.container.xml.Configuration;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestImportProfile extends AbstractProfileTest
{

   public void testNoProfile() throws Exception
   {
      Configuration config = getConfiguration("import-configuration.xml");
      assertEquals(1, config.getImports().size());
   }

   public void testFooProfile() throws Exception
   {
      Configuration config = getConfiguration("import-configuration.xml", "foo");
      assertEquals(2, config.getImports().size());
   }

   public void testFooBarProfiles() throws Exception
   {
      Configuration config = getConfiguration("import-configuration.xml", "foo", "bar");
      assertEquals(3, config.getImports().size());
   }
}