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
package org.exoplatform.container.jmx;

import junit.framework.TestCase;

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;

import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class AbstractTestContainer extends TestCase
{

   public RootContainer createRootContainer(String relativeConfigurationFile)
   {
      try
      {
         RootContainer container = new RootContainer();
         ConfigurationManager manager = new ConfigurationManagerImpl();
         URL url = AbstractTestContainer.class.getResource(relativeConfigurationFile);
         assertNotNull(url);
         manager.addConfiguration(url);
         container.registerComponentInstance(ConfigurationManager.class, manager);
         container.initContainer();
         container.start();
         return container;
      }
      catch (Exception e)
      {
         AssertionError err = new AssertionError("Could not start root container");
         err.initCause(e);
         throw err;
      }
   }

}
