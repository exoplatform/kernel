/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.services.ispn;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.util.TemplateConfigurationHelper;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class Utils
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger
      .getLogger("exo.kernel.component.ext.cache.impl.infinispan.v5.Utils");
   
   private Utils() {}
   

   /**
    * Load the JGroups configuration file thanks to the {@link ConfigurationManager}
    * @param config the global configuration from which the JGroups config will be extracted
    * @return <code>true</code> if the JGoups config could be loaded successfully, 
    * <code>false</code> if there were no JGroups config to load
    * @throws IllegalStateException if the JGroups config could not be loaded
    */
   public static boolean loadJGroupsConfig(ConfigurationManager cfm, GlobalConfiguration config) throws ExoCacheInitException
   {
      Properties properties = config.getTransportProperties();
      if (properties == null || !properties.containsKey(JGroupsTransport.CONFIGURATION_FILE))
      {
         return false;
      }
      String filename = properties.getProperty(JGroupsTransport.CONFIGURATION_FILE);
      InputStream inputStream = TemplateConfigurationHelper.getInputStream(cfm, filename);

      // inputStream still remains null, so file was not opened
      if (inputStream == null)
      {
         throw new IllegalStateException("The jgroups configuration cannot be loaded from '" + filename
            + "'");
      }      
      try
      {
         // Set the jgroups configuration as XML
         properties.setProperty(JGroupsTransport.CONFIGURATION_XML, readStream(inputStream));
      }
      catch (IOException e)
      {
         throw new IllegalStateException("The jgroups configuration cannot be read from '" + filename
            + "'");
      }
      // Remove the property corresponding to the configuration file
      properties.remove(JGroupsTransport.CONFIGURATION_FILE);
      return true;
   }


   /**
    * Reads bytes from input stream and builds a string from them
    * 
    * @param inputStream
    * @return
    * @throws IOException
    */
   private static String readStream(InputStream inputStream) throws IOException
   {
      StringBuilder out = new StringBuilder(4096);
      byte[] b = new byte[4096];
      try
      {
         for (int length; (length = inputStream.read(b)) != -1;)
         {
            out.append(new String(b, 0, length));
         }
      }
      finally
      {
         try
         {
            inputStream.close();
         }
         catch (Exception e)
         {
            LOG.debug("Cannot close stream", e);
         }
      }
      return out.toString();
   }
}
