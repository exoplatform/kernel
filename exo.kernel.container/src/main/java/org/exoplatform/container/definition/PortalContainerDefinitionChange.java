/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.container.definition;

import java.util.List;
import java.util.Map;

/**
 * This interface describes the change that cans be applied on a given {@link PortalContainerDefinition} 
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 11 juin 2010  
 */
public interface PortalContainerDefinitionChange
{

   /**
    * Apply the corresponding change of the given {@link PortalContainerDefinition}
    * @param pcd the {@link PortalContainerDefinition} on which the change has to be applied
    */
   void apply(PortalContainerDefinition pcd);

   /**
    * This class is an {@link PortalContainerDefinitionChange} that will add the nested dependencies
    * at the end of the dependency list of the {@link PortalContainerDefinition}. If the nested 
    * dependency list is empty, this change will be ignored.
    */
   public static class AddDependencies implements PortalContainerDefinitionChange
   {
      /**
       * The list of name of the dependencies to add
       */
      public List<String> dependencies;

      /**
       * {@inheritDoc}
       */
      public void apply(PortalContainerDefinition pcd)
      {
         if (dependencies == null || dependencies.isEmpty())
         {
            return;
         }
         pcd.getDependencies().addAll(dependencies);
      }
   }
   
   /**
    * This class is an {@link PortalContainerDefinitionChange} that will add the nested dependencies
    * before a target dependency to the dependency list of the {@link PortalContainerDefinition}. 
    * If the target dependency is empty or cannot be found, the nested dependencies will be added at
    * the head of the dependency list. If the nested dependency list is empty, this change will be ignored.
    */
   public static class AddDependenciesBefore implements PortalContainerDefinitionChange
   {
      /**
       * The list of name of the dependencies to add
       */
      public List<String> dependencies;
      
      /**
       * The name of the target dependency
       */
      public String target;

      /**
       * {@inheritDoc}
       */
      public void apply(PortalContainerDefinition pcd)
      {
         if (dependencies == null || dependencies.isEmpty())
         {
            return;
         }
         if (target != null && target.length() > 0)
         {
            List<String> lDependencies = pcd.getDependencies();
            for (int i = lDependencies.size() - 1; i >= 0; i--)
            {
               String dep = lDependencies.get(i);
               if (target.equals(dep))
               {
                  // The target could be found
                  lDependencies.addAll(i, dependencies);
                  return;
               }
            }
         }
         // The target is empty or cannot be found
         pcd.getDependencies().addAll(0, dependencies);
      }
   }
   
   /**
    * This class is an {@link PortalContainerDefinitionChange} that will add the nested dependencies
    * after a target dependency to the dependency list of the {@link PortalContainerDefinition}. 
    * If the target dependency is empty or cannot be found, the nested dependencies will be added at
    * the end of the dependency list. If the nested dependency list is empty, this change will be ignored.
    */
   public static class AddDependenciesAfter implements PortalContainerDefinitionChange
   {
      /**
       * The list of name of the dependencies to add
       */
      public List<String> dependencies;
      
      /**
       * The name of the target dependency
       */
      public String target;

      /**
       * {@inheritDoc}
       */
      public void apply(PortalContainerDefinition pcd)
      {
         if (dependencies == null || dependencies.isEmpty())
         {
            return;
         }
         if (target != null && target.length() > 0)
         {
            List<String> lDependencies = pcd.getDependencies();
            for (int i = 0, length = lDependencies.size(); i < length; i++)
            {
               String dep = lDependencies.get(i);
               if (target.equals(dep))
               {
                  // The target could be found
                  lDependencies.addAll(i + 1, dependencies);
                  return;
               }
            }
         }
         // The target is empty or cannot be found
         pcd.getDependencies().addAll(dependencies);
      }
   }
   
   /**
    * This class is an {@link PortalContainerDefinitionChange} that will add new internal settings
    * to the {@link PortalContainerDefinition}. If the nested settings are empty, this change will
    * be ignored.
    */
   public static class AddSettings implements PortalContainerDefinitionChange
   {
      /**
       * The settings to add to the internal settings
       */
      public Map<String, Object> settings;
      
      public void apply(PortalContainerDefinition pcd)
      {
         if (settings == null || settings.isEmpty())
         {
            return;
         }
         pcd.getSettings().putAll(settings);
      }      
   }
}
