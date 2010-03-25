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
package org.exoplatform.services.command.impl;

import org.apache.commons.chain.config.ConfigParser;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.net.URL;

/**
 * Created by The eXo Platform SAS.<br/> The plugin for configuring
 * command/chain catalog using "native" Apache Commons Chain's XML file
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: CommonsXMLConfigurationPlugin.java 9846 2006-10-27 11:03:37Z
 *          geaz $
 */

public class CommonsXMLConfigurationPlugin extends BaseComponentPlugin
{

   // protected Catalog defaultCatalog;
   
   private static Log log = ExoLogger.getLogger("exo.kernel.component.cache.CommonsXMLConfigurationPlugin");

   public CommonsXMLConfigurationPlugin(InitParams params, ConfigurationManager configurationManager) throws Exception
   {
      ValueParam confFile = params.getValueParam("config-file");
      if (confFile != null)
      {
         String path = confFile.getValue();
         ConfigParser parser = new ConfigParser();
         // may work for StandaloneContainer
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         URL res = cl.getResource(path);
         // for PortalContainer
         if (res == null)
            res = configurationManager.getResource(path);
         if (res == null)
            throw new Exception("Resource not found " + path);
         log.info("Catalog configuration found at " + res);
         parser.parse(res);
      }

   }
}
