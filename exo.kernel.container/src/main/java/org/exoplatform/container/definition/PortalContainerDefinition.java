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

import java.util.List;

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

   public String getName()
   {
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
      return realmName;
   }

   public void setRealmName(String realmName)
   {
      this.realmName = realmName;
   }

   public String getRestContextName()
   {
      return restContextName;
   }

   public void setRestContextName(String restContextName)
   {
      this.restContextName = restContextName;
   }
}
