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
public class ContainerLifecyclePlugin implements Comparable<ContainerLifecyclePlugin>
{
   private String name;
   
   private String type;
   
   private String description;
   
   private int priority;

   private InitParams initParams;
   
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }
   
   public String getType()
   {
      return type;
   }

   public void setType(String type)
   {
      this.type = type;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String desc)
   {
      this.description = desc;
   }

   public int getPriority()
   {
      return priority;
   }

   public void setPriority(int priority)
   {
      this.priority = priority;
   }
   
   public InitParams getInitParams()
   {
      return initParams;
   }

   public void setInitParams(InitParams initParams)
   {
      this.initParams = initParams;
   }

   public int compareTo(ContainerLifecyclePlugin o)
   {
      return getPriority() - o.getPriority();
   }
}
