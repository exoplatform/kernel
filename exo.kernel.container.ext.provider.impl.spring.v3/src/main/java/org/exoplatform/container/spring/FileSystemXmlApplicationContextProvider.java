/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container.spring;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.net.URL;

/**
 * This is the implementation of the {@link ApplicationContextProvider} based on the
 * {@link FileSystemXmlApplicationContext} allowing to configure Spring thanks to XML files.
 * It can be configured using a values-param, each value will be the path of the XML files
 * to be registered. Please note that the expected paths will be retrieved thanks to the
 * {@link ConfigurationManager} which means that all the prefixes supported by the kernel
 * are supported by this component such as <i>jar:</i> and <i>classpath:</i>
 * 
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class FileSystemXmlApplicationContextProvider implements ApplicationContextProvider
{

   /**
    * The name of the values parameter that will contain the path to the configuration
    * files
    */
   private static final String CONFIG_PATHS_PARAM_NAME = "config.paths";

   /**
    * The values param containing the configuration
    */
   private final ValuesParam params;

   /**
    * The configuration manager
    */
   private final ConfigurationManager cm;

   /**
    * The default constructor
    * @param p the initial parameters
    * @param cm the configuration manager
    */
   public FileSystemXmlApplicationContextProvider(InitParams p, ConfigurationManager cm)
   {
      if (p == null || p.getValuesParam(CONFIG_PATHS_PARAM_NAME) == null)
      {
         throw new IllegalArgumentException("The values parameter " + CONFIG_PATHS_PARAM_NAME
            + " is mandatory, please set at least one value.");
      }
      this.params = p.getValuesParam(CONFIG_PATHS_PARAM_NAME);
      this.cm = cm;
   }

   /**
    * {@inheritDoc}
    */
   public ApplicationContext getApplicationContext(ApplicationContext parent)
   {
      try
      {
         String[] paths = new String[params.getValues().size()];
         int i = 0;
         for (Object value : params.getValues())
         {
            URL url = cm.getResource((String)value);
            paths[i++] = url.toURI().toString();
         }
         return new FileSystemXmlApplicationContext(paths, true, parent);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create the ApplicationContext", e);
      }
   }
}
