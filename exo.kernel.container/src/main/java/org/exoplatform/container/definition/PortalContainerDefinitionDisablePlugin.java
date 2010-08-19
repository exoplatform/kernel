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

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;

import java.util.HashSet;
import java.util.Set;

/**
 * This class allows you to dynamically disable one or several portal containers.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 9 juil. 2010  
 */
public class PortalContainerDefinitionDisablePlugin extends BaseComponentPlugin
{
   
   /**
    * A set of specific portal container names that we want to disable.
    */
   private Set<String> names;
   
   @SuppressWarnings("unchecked")
   public PortalContainerDefinitionDisablePlugin(InitParams params)
   {
      ValuesParam vsp = params.getValuesParam("names");
      if (vsp != null && !vsp.getValues().isEmpty())
      {
         this.names = new HashSet<String>(vsp.getValues());
      }
   }

   public Set<String> getNames()
   {
      return names;
   }
}
