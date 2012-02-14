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
package org.exoplatform.container;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * <p>The property configurator configures a set of system properties via the {@link PropertyManager}
 * static methods. It is possible to configure properties from the init params or from an external
 * file.</p>
 *
 * <p>The constructor will inspect the {@link org.exoplatform.container.xml.InitParams} params argument
 * to find a param named <code>properties</code> with an expected type of {@link PropertiesParam}. The
 * properties contained in that argument will be sourced into the property manager. When such properties
 * are loaded from an XML configuration file, the values are evaluated and property substitution occurs.</p>
 *
 * <p>When the property {@link PropertyManager#PROPERTIES_URL} is not null and points to a valid property
 * file it will loaded and sourced. Property values will be evaluated and property substitution will
 * occur. When the file name ends with the <code>.properties</code> properties are loaded using the
 * {@link Properties#load(java.io.InputStream)} method. When the file name ends with the <code>.xml</code>
 * properties are loaded using the {@link Properties#loadFromXML(java.io.InputStream)} method. Suffix
 * checks are done ignoring the case.</p>
 *
 * <p>When properties are loaded from an URL, the order of the properties declarations in the file matters.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertyConfigurator implements Startable
{

   /** The logger. */
   private static final Log LOG = ExoLogger.getExoLogger(PropertyConfigurator.class);

   public PropertyConfigurator(ConfigurationManager confManager)
   {
      this(null, confManager);
   }

   public PropertyConfigurator(InitParams params, ConfigurationManager confManager)
   {
      String path = null;
      if (params != null)
      {
         PropertiesParam propertiesParam = params.getPropertiesParam("properties");
         if (propertiesParam != null)
         {
            LOG.debug("Going to initialize properties from init param");
            for (Iterator<Property> i = propertiesParam.getPropertyIterator();i.hasNext();)
            {
               Property property = i.next();
               String name = property.getName();
               String value = property.getValue();
               LOG.debug("Adding property from init param " + name + " = " + value);
               PropertyManager.setProperty(name, value);
            }
         }         
         ValueParam pathParam = params.getValueParam("properties.url");
         if (pathParam != null)
         {
            LOG.debug("Using file path " + path + " found from configuration");
            path = pathParam.getValue();
         }
      }

      //
      String systemPath = PropertyManager.getProperty(PropertyManager.PROPERTIES_URL);
      if (systemPath != null)
      {
         LOG.debug("Using file path " + path + " found from system properties");
         path = systemPath;
      }

      //
      if (path != null)
      {
         LOG.debug("Found property file path " + path);
         try
         {
            URL url = confManager.getURL(path);
            Map<String, String> props = ContainerUtil.loadProperties(url);
            if (props != null)
            {
               for (Map.Entry<String, String> entry : props.entrySet())
               {
                  String propertyName = entry.getKey();
                  String propertyValue = entry.getValue();
                  PropertyManager.setProperty(propertyName, propertyValue);
               }
            }
         }
         catch (Exception e)
         {
            LOG.error("Cannot load property file " + path, e);
         }
      }
   }

   public void start()
   {
   }

   public void stop()
   {
   }
}
