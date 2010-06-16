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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is a decorator used to protect the nested {@link PortalContainerDefinition}. It mainly
 * forbid access to setter. If a setter is called an {@link UnsupportedOperationException} will be thrown.
 * This class is mainly used to prevent any unsupported {@link PortalContainerDefinitionChange}.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 8 sept. 2009  
 */
public class SafePortalContainerDefinition extends PortalContainerDefinition
{

   /**
    * The {@link PortalContainerDefinition} to protected
    */
   private final PortalContainerDefinition definition;

   /**
    * The default {@link PortalContainerDefinition}
    */
   private final PortalContainerDefinition defaultDefinition;   
   
   /**
    * Default constructor
    */
   public SafePortalContainerDefinition(PortalContainerDefinition definition,
      PortalContainerDefinition defaultDefinition)
   {
      this.definition = definition;
      this.defaultDefinition = defaultDefinition;
   }

   public String getName()
   {
      return definition.getName();
   }

   public void setName(String name)
   {
      throw new UnsupportedOperationException();
   }

   public List<String> getDependencies()
   {
      // We ensure that the dependency list is not null to simplify the code in 
      // all instances of PortalContainerDefinitionChange
      List<String> dependencies = definition.getDependencies();
      if (dependencies == null || dependencies.isEmpty())
      {
         // Try to get the default dependencies
         dependencies = defaultDefinition.getDependencies();
         if (dependencies == null || dependencies.isEmpty())
         {
            dependencies = new ArrayList<String>();
         }
         else
         {
            dependencies = new ArrayList<String>(dependencies);
         }
         definition.setDependencies(dependencies);
      }
      return dependencies;
   }

   public void setDependencies(List<String> dependencies)
   {
      throw new UnsupportedOperationException();
   }

   public String getRealmName()
   {
      return definition.getRealmName();
   }

   public void setRealmName(String realmName)
   {
      throw new UnsupportedOperationException();
   }

   public String getRestContextName()
   {
      return definition.getRestContextName();
   }

   public void setRestContextName(String restContextName)
   {
      throw new UnsupportedOperationException();
   }

   public Map<String, Object> getSettings()
   {
      // We ensure that the settings are not null to simplify the code in 
      // all instances of PortalContainerDefinitionChange     
      Map<String, Object> settings = definition.getSettings();
      if (settings == null)
      {
         settings = new HashMap<String, Object>();
      }
      else
      {
         settings = new HashMap<String, Object>(settings);
      }
      definition.setSettings(settings);
      return settings;
   }

   public void setSettings(Map<String, Object> settings)
   {
      throw new UnsupportedOperationException();
   }

   public String getExternalSettingsPath()
   {
      return definition.getExternalSettingsPath();
   }

   public void setExternalSettingsPath(String externalSettingsPath)
   {
      throw new UnsupportedOperationException();
   }
}
