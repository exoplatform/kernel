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

import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.jibx.runtime.IMarshallingContext;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 18, 2005
 * @version $Id: Component.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class Component
{

   final URL documentURL;

   String key;

   String type;

   String jmxName;

   String description;

   private List<ComponentPlugin> componentPlugins;

   InitParams initParams;

   boolean showDeployInfo = false;

   boolean multiInstance = false;

   public Component()
   {
      documentURL = ConfigurationManagerImpl.getCurrentURL();
   }

   public URL getDocumentURL()
   {
      return documentURL;
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String s)
   {
      this.key = s;
   }

   public String getJMXName()
   {
      return jmxName;
   }

   public void setJMXName(String s)
   {
      jmxName = s;
   }

   public String getType()
   {
      return type;
   }

   public void setType(String s)
   {
      type = s;
   }

   public String getDescription()
   {
      return description;
   }

   public void setDescription(String s)
   {
      description = s;
   }

   public List<ComponentPlugin> getComponentPlugins()
   {
      return componentPlugins;
   }

   public void setComponentPlugins(List<ComponentPlugin> list)
   {
      if (list != null)
      {
         // Sort the list of component plugins first
         Collections.sort(list);
      }
      componentPlugins = list;
   }

   public InitParams getInitParams()
   {
      return initParams;
   }

   public void setInitParams(InitParams ips)
   {
      initParams = ips;
   }

   public boolean getShowDeployInfo()
   {
      return showDeployInfo;
   }

   public void setShowDeployInfo(boolean b)
   {
      showDeployInfo = b;
   }

   public boolean isMultiInstance()
   {
      return multiInstance;
   }

   public void setMultiInstance(boolean b)
   {
      multiInstance = b;
   }

   public void preGet(IMarshallingContext ictx)
   {
      ConfigurationMarshallerUtil.addURLToContent(documentURL, ictx);
   }
}
