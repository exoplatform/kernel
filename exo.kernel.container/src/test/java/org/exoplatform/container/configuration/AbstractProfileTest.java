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

import junit.framework.TestCase;
import org.exoplatform.commons.utils.Tools;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.support.ContainerBuilder;
import org.exoplatform.container.xml.Configuration;

import java.io.File;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public abstract class AbstractProfileTest extends TestCase
{
   protected final Configuration getConfiguration(String configName, String... profiles) throws Exception
   {
      String basedir = System.getProperty("basedir");
      File f = new File(basedir + "/src/test/resources/org/exoplatform/container/configuration/" + configName);
      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller(Tools.<String>set(profiles));
      URL url = f.toURI().toURL();
      return unmarshaller.unmarshall(url);
   }

   protected final RootContainer getKernel(String configName, String... profiles) throws Exception
   {
      String basedir = System.getProperty("basedir");
      File f = new File(basedir + "/src/test/resources/org/exoplatform/container/configuration/" + configName);
      return ContainerBuilder.bootstrap(f.toURI().toURL(), profiles);
   }
}
