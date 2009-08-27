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
package org.exoplatform.container.xml;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Sep 8, 2005
 */
public class ComponentLifecyclePlugin
{
   private String type;

   private ManageableComponents manageableComponents;

   private InitParams initParams;

   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public ManageableComponents getManageableComponents()
   {
      return manageableComponents;
   }

   public void setManageableComponents(ManageableComponents mc)
   {
      manageableComponents = mc;
   }

   public InitParams getInitParams()
   {
      return initParams;
   }

   public void setInitParams(InitParams initParams)
   {
      this.initParams = initParams;
   }

}
