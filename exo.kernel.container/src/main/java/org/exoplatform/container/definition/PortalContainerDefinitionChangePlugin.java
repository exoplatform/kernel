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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class allows you to dynamically apply a list of changes to one or several portal containers.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 11 juin 2010  
 */
public class PortalContainerDefinitionChangePlugin extends BaseComponentPlugin
{
   
   /**
    * Indicates whether the changes have to be applied to all the portal containers or not.
    */
   private boolean all;
   
   /**
    * Indicates whether the changes have to be applied to the default portal container or not. 
    */
   private boolean bDefault;

   /**
   * Profiles to inject to {@link RootContainer} before start parsing
   * configurations of {@link PortalContainer}
   */
   private List<String> profiles;

   /**
    * A set of specific portal container names on which we want to apply the changes.
    */
   private Set<String> names;
   
   /**
    * The list of changes to apply
    */
   private List<PortalContainerDefinitionChange> changes;
   
   public PortalContainerDefinitionChangePlugin(InitParams params)
   {
      ValueParam vp = params.getValueParam("apply.all");
      if (vp != null && vp.getValue().length() > 0)
      {
         this.all = Boolean.valueOf(Deserializer.resolveVariables(vp.getValue()));
      }
      vp = params.getValueParam("apply.default");
      if (vp != null && vp.getValue().length() > 0)
      {
         this.bDefault = Boolean.valueOf(Deserializer.resolveVariables(vp.getValue()));
      }
      ValuesParam vsp = params.getValuesParam("apply.specific");
      if (vsp != null && !vsp.getValues().isEmpty())
      {
         this.names = new HashSet<>(vsp.getValues().size());
         List<String> lnames = vsp.getValues();
         for (String name : lnames)
         {
            names.add(Deserializer.resolveVariables(name));
         }
      }
      this.changes = params.getObjectParamValues(PortalContainerDefinitionChange.class);

      ValuesParam profilesParameters = params.getValuesParam("add.profiles");
      if (profilesParameters != null && !profilesParameters.getValues().isEmpty()) {
        this.profiles = profilesParameters.getValues();
        RootContainer.addProfiles(this.profiles);
      }
   }

   public boolean isAll()
   {
      return all;
   }

   public boolean isDefault()
   {
      return bDefault;
   }

   public Set<String> getNames()
   {
      return names;
   }

   public List<PortalContainerDefinitionChange> getChanges()
   {
      return changes;
   }
}
