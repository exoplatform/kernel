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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 18, 2005
 * @version $Id: ExternalComponentPlugins.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ExternalComponentPlugins
{
   String targetComponent;

   ArrayList<ComponentPlugin> componentPlugins;

   public String getTargetComponent()
   {
      return targetComponent;
   }

   public void setTargetComponent(String s)
   {
      targetComponent = s;
   }

   public List getComponentPlugins()
   {
      return componentPlugins;
   }

   public void setComponentPlugins(ArrayList<ComponentPlugin> list)
   {
      componentPlugins = list;
   }

   public void merge(ExternalComponentPlugins other)
   {
      if (other == null)
         return;
      List otherPlugins = other.getComponentPlugins();
      if (otherPlugins == null)
         return;
      if (componentPlugins == null)
         componentPlugins = new ArrayList();
      componentPlugins.addAll(otherPlugins);
   }
}
