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

import org.exoplatform.container.xml.InitParams;

abstract public class BaseContainerLifecyclePlugin implements ContainerLifecyclePlugin
{

   private String name;

   private String description;

   private InitParams params;

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String s)
   {
      description = s;
   }

   public InitParams getInitParams()
   {
      return params;
   }

   public void setInitParams(InitParams params)
   {
      this.params = params;
   }

   public void initContainer(ExoContainer container) throws Exception
   {

   }

   public void startContainer(ExoContainer container) throws Exception
   {

   }

   public void stopContainer(ExoContainer container) throws Exception
   {

   }

   public void destroyContainer(ExoContainer container) throws Exception
   {

   }
}
