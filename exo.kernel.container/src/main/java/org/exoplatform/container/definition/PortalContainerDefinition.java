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
package org.exoplatform.container.definition;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.Deserializer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.ServletContext;

/**
 * This class is used to define a {@link PortalContainer} and its dependencies. The dependencies
 * are in fact all the web applications that the {@link PortalContainer} needs to be properly 
 * initialized. Be aware that the dependency order is used to define the initialization order. 
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 8 sept. 2009  
 */
public class PortalContainerDefinition
{

   /**
    * Indicates whether the current instance has been initialized
    */
   private final AtomicBoolean initialized = new AtomicBoolean();
   
   /**
    * The name of the related {@link PortalContainer}
    */
   private String name;

   /**
    * The realm name of the related {@link PortalContainer}
    */
   private String realmName;

   /**
    * The name of the {@link ServletContext} of the rest web application
    */
   private String restContextName;

   /**
    * The list of all the context names that are needed to initialized properly the
    * {@link PortalContainer}. The order of all the dependencies will define the initialization order 
    */
   private List<String> dependencies;

   /**
    * A {@link Map} of parameters that we would like to tie the portal container. Those parameters
    * could have any type of value.
    */
   private Map<String, Object> settings;

   /**
    * The path of the external properties file to load as default settings to the portal container. 
    */
   private String externalSettingsPath;

   public String getName()
   {
      init();
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public List<String> getDependencies()
   {
      return dependencies;
   }

   public void setDependencies(List<String> dependencies)
   {
      this.dependencies = dependencies;
   }

   public String getRealmName()
   {
      init();
      return realmName;
   }

   public void setRealmName(String realmName)
   {
      this.realmName = realmName;
   }

   public String getRestContextName()
   {
      init();
      return restContextName;
   }

   public void setRestContextName(String restContextName)
   {
      this.restContextName = restContextName;
   }

   public Map<String, Object> getSettings()
   {
      return settings;
   }

   public void setSettings(Map<String, Object> settings)
   {
      this.settings = settings;
   }

   public String getExternalSettingsPath()
   {
      return externalSettingsPath;
   }

   public void setExternalSettingsPath(String externalSettingsPath)
   {
      this.externalSettingsPath = externalSettingsPath;
   }
   
   /**
    * Ensure that all the parameter values have been resolved in order to allow to
    * use variables to define their values. It will be executed only if it has the current
    * instance has not been initialized
    */
   private void init()
   {
      if (!initialized.get())
      {
         synchronized (this)
         {
            if (!initialized.get())
            {
               setName(Deserializer.resolveVariables(name));
               setRestContextName(Deserializer.resolveVariables(restContextName));
               setRealmName(Deserializer.resolveVariables(realmName));
               initialized.set(true);
            }
         }
      }
   }   
}
