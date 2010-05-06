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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: Configuration.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class Configuration
{

   public static final String KERNEL_CONFIGURATION_1_0_URI = "http://www.exoplaform.org/xml/ns/kernel_1_0.xsd";

   private Map<String, ContainerLifecyclePlugin> containerLifecyclePlugin_ =
      new HashMap<String, ContainerLifecyclePlugin>();

   private Map<String, ComponentLifecyclePlugin> componentLifecyclePlugin_ =
      new HashMap<String, ComponentLifecyclePlugin>();

   private Map<String, Component> component_ = new HashMap<String, Component>();

   private Map<String, ExternalComponentPlugins> externalComponentPlugins_ =
      new HashMap<String, ExternalComponentPlugins>();

   private ArrayList<String> imports_;

   private ArrayList<String> removeConfiguration_;

   public Collection getContainerLifecyclePlugins()
   {
      return containerLifecyclePlugin_.values();
   }

   public void addContainerLifecyclePlugin(Object object)
   {
      ContainerLifecyclePlugin plugin = (ContainerLifecyclePlugin)object;
      String key = plugin.getType();
      containerLifecyclePlugin_.put(key, plugin);
   }

   public Iterator getContainerLifecyclePluginIterator()
   {
      return containerLifecyclePlugin_.values().iterator();
   }

   public boolean hasContainerLifecyclePlugin()
   {
      return containerLifecyclePlugin_.size() > 0;
   }

   public Collection getComponentLifecyclePlugins()
   {
      return componentLifecyclePlugin_.values();
   }

   public void addComponentLifecyclePlugin(Object object)
   {
      ComponentLifecyclePlugin plugin = (ComponentLifecyclePlugin)object;
      String key = plugin.getClass().getName();
      componentLifecyclePlugin_.put(key, plugin);
   }

   public Iterator getComponentLifecyclePluginIterator()
   {
      return componentLifecyclePlugin_.values().iterator();
   }

   public boolean hasComponentLifecyclePlugin()
   {
      return componentLifecyclePlugin_.size() > 0;
   }

   public Component getComponent(String s)
   {
      return component_.get(s);
   }

   public void addComponent(Object object)
   {
      Component comp = (Component)object;
      String key = comp.getKey();
      if (key == null)
      {
         key = comp.getType();
         comp.setKey(key);
      }
      component_.put(key, comp);
   }

   public Collection getComponents()
   {
      return component_.values();
   }

   public Iterator getComponentIterator()
   {
      return component_.values().iterator();
   }

   public boolean hasComponent()
   {
      return component_.size() > 0;
   }

   public ExternalComponentPlugins getExternalComponentPlugins(String s)
   {
      return externalComponentPlugins_.get(s);
   }

   public void addExternalComponentPlugins(Object o)
   {

      if (o != null)
      {
         ExternalComponentPlugins eps = (ExternalComponentPlugins)o;

         // Retrieve potential existing external component
         // plugins with same target component.
         String targetComponent = eps.getTargetComponent();
         ExternalComponentPlugins foundExternalComponentPlugins =
            (ExternalComponentPlugins)externalComponentPlugins_.get(targetComponent);

         if (foundExternalComponentPlugins == null)
         {
            // No external component plugins found. Create a new entry.
            externalComponentPlugins_.put(targetComponent, eps);
         }
         else
         {
            // Found external component plugins. Add the specified one.
            foundExternalComponentPlugins.merge(eps);
         }
      }
   }

   public Iterator getExternalComponentPluginsIterator()
   {
      return externalComponentPlugins_.values().iterator();
   }

   public boolean hasExternalComponentPlugins()
   {
      return externalComponentPlugins_.size() > 0;
   }

   public void addImport(String url)
   {
      if (imports_ == null)
         imports_ = new ArrayList<String>();
      imports_.add(url);
   }

   public List getImports()
   {
      return imports_;
   }

   public void addRemoveConfiguration(String type)
   {
      if (removeConfiguration_ == null)
         removeConfiguration_ = new ArrayList<String>();
      removeConfiguration_.add(type);
   }

   public List getRemoveConfiguration()
   {
      return removeConfiguration_;
   }

   public void removeConfiguration(String type)
   {
      component_.remove(type);
   }

   // -------------------------end new mapping configuration--------------------

   public void mergeConfiguration(Configuration other)
   {
      component_.putAll(other.component_);

      componentLifecyclePlugin_.putAll(other.componentLifecyclePlugin_);
      containerLifecyclePlugin_.putAll(other.containerLifecyclePlugin_);

      // merge the external plugins
      Iterator i = other.externalComponentPlugins_.values().iterator();
      while (i.hasNext())
      {
         ExternalComponentPlugins eplugins = (ExternalComponentPlugins)i.next();
         ExternalComponentPlugins foundExternalComponentPlugins =
            externalComponentPlugins_.get(eplugins.getTargetComponent());
         if (foundExternalComponentPlugins == null)
         {
            externalComponentPlugins_.put(eplugins.getTargetComponent(), eplugins);
         }
         else
         {
            foundExternalComponentPlugins.merge(eplugins);
         }
      }
      // externalListeners_.putAll(other.externalListeners_) ;

      if (other.getRemoveConfiguration() == null)
         return;
      if (removeConfiguration_ == null)
         removeConfiguration_ = new ArrayList<String>();
      removeConfiguration_.addAll(other.getRemoveConfiguration());
   }
}
