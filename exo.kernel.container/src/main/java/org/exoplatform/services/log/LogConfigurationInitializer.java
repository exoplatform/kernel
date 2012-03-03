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
package org.exoplatform.services.log;

import org.exoplatform.commons.utils.Tools;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.picocontainer.Startable;

import java.util.Map;
import java.util.Properties;

/**
 * Created by The eXo Platform SAS. <br/> The component for commons based
 * logging configuration initialization. There are 3 optional initialization
 * parameters: logger - a logger class implemented
 * org.exoplatform.services.log.Log configurator - a log system configurator
 * implementation of LogConfigurator parameters - list of parameters for the
 * configurator
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: LogConfigurationInitializer.java 5332 2006-04-29 18:32:44Z geaz
 *          $
 */

public class LogConfigurationInitializer implements Startable
{

   private Map properties = null;

   private String logger = null;

   private String configurer = null;

   /**
    * Constructor for in-container using
    * 
    * @param params - initialization parameters, optionally included logger,
    *          configurator and properties
    * @throws Exception
    */
   public LogConfigurationInitializer(InitParams params) throws Exception
   {

      ValueParam loggerValue = params.getValueParam("logger");
      if (loggerValue != null)
         logger = loggerValue.getValue();

      ValueParam confValue = params.getValueParam("configurator");
      if (confValue != null)
         configurer = confValue.getValue();

      PropertiesParam p = params.getPropertiesParam("properties");
      if (p != null)
         properties = p.getProperties();

      initLogger();
   }

   /**
    * Simple constructor, not for use in container
    * 
    * @param logger
    * @param configurator
    * @param properties
    */
   public LogConfigurationInitializer(String logger, String configurator, Properties properties) throws Exception
   {

      this.logger = logger;
      this.configurer = configurator;
      this.properties = properties;
      initLogger();

   }

   /**
    * @return logger class name
    */
   public String getLoggerClass()
   {
      return logger;
   }

   /**
    * @return configurator class name
    */
   public String getConfiguratorClass()
   {
      return configurer;
   }

   /**
    * @return current Log properties (name-value pairs)
    */
   public Map getProperties()
   {
      return properties;
   }

   /**
    * Updates or adds property
    * 
    * @param name
    * @param value
    * @throws Exception
    */
   public void setProperty(String name, String value) throws Exception
   {
      properties.put(name, value);
      initLogger();
   }

   /**
    * Removes property
    * 
    * @param name
    * @throws Exception
    */
   public void removeProperty(String name) throws Exception
   {
      properties.remove(name);
      initLogger();
   }

   /*
    * (non-Javadoc)
    * @see org.picocontainer.Startable#start()
    */
   public void start()
   {
   }

   /*
    * (non-Javadoc)
    * @see org.picocontainer.Startable#stop()
    */
   public void stop()
   {
   }

   /**
    * initializes log configuration
    * 
    * @throws Exception
    */
   private void initLogger() throws Exception
   {

      Properties props;
      if (configurer != null && properties != null)
      {
         LogConfigurator conf = (LogConfigurator)Tools.forName(configurer, this).newInstance();
         props = new Properties();
         props.putAll(properties);
         conf.configure(props);
      }

      // todo if logger exists create and replace the factory
   }

}
