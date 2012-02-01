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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
public class Configuration implements Cloneable
{

   public static final String KERNEL_CONFIGURATION_1_0_URI = "http://www.exoplaform.org/xml/ns/kernel_1_0.xsd";

   private static final Log log = ExoLogger.getLogger("exo.kernel.container.Configuration");

   private Map<String, ContainerLifecyclePlugin> containerLifecyclePlugin_ =
      new HashMap<String, ContainerLifecyclePlugin>();

   private Map<String, ComponentLifecyclePlugin> componentLifecyclePlugin_ =
      new HashMap<String, ComponentLifecyclePlugin>();

   private Map<String, Component> component_ = new HashMap<String, Component>();

   private Map<String, ExternalComponentPlugins> externalComponentPlugins_ =
      new HashMap<String, ExternalComponentPlugins>();

   private ArrayList<String> imports_;

   private ArrayList<String> removeConfiguration_;

   public Collection<ContainerLifecyclePlugin> getContainerLifecyclePlugins()
   {
      List<ContainerLifecyclePlugin> plugins =
         new ArrayList<ContainerLifecyclePlugin>(containerLifecyclePlugin_.values());
      Collections.sort(plugins);
      return plugins;
   }

   public void addContainerLifecyclePlugin(Object object)
   {
      ContainerLifecyclePlugin plugin = (ContainerLifecyclePlugin)object;
      String key = plugin.getType();
      containerLifecyclePlugin_.put(key, plugin);
   }

   public Iterator<ContainerLifecyclePlugin> getContainerLifecyclePluginIterator()
   {
      return getContainerLifecyclePlugins().iterator();
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
      if (o instanceof ExternalComponentPlugins)
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
         addExternalComponentPlugins(i.next());
      }

      if (other.getRemoveConfiguration() == null)
         return;
      if (removeConfiguration_ == null)
         removeConfiguration_ = new ArrayList<String>();
      removeConfiguration_.addAll(other.getRemoveConfiguration());
   }

   /**
    * Merge all the given configurations and return a safe copy of the result
    * @param configs the list of configurations to merge ordered by priority, the second
    * configuration will override the configuration of the first one and so on.
    * @return the merged configuration
    */
   public static Configuration merge(Configuration... configs)
   {
      if (configs == null || configs.length == 0)
      {
         return null;
      }
      Configuration result = null;
      for (Configuration conf : configs)
      {
         if (conf == null)
         {
            // Ignore the null configuration
            continue;
         }
         else if (result == null)
         {
            // Initialize with the clone of the first non null configuration 
            result = (Configuration)conf.clone();
         }
         else
         {
            // The merge the current configuration with this new configuration
            result.mergeConfiguration(conf);
         }
      }
      return result;
   }
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   protected Object clone()
   {
      try
      {
         Configuration conf = (Configuration)super.clone();
         conf.component_ = (Map<String, Component>)((HashMap<String, Component>)component_).clone();
         conf.componentLifecyclePlugin_ =
            (Map<String, ComponentLifecyclePlugin>)((HashMap<String, ComponentLifecyclePlugin>)componentLifecyclePlugin_)
               .clone();
         conf.containerLifecyclePlugin_ =
            (Map<String, ContainerLifecyclePlugin>)((HashMap<String, ContainerLifecyclePlugin>)containerLifecyclePlugin_)
               .clone();
         conf.externalComponentPlugins_ =
            (Map<String, ExternalComponentPlugins>)((HashMap<String, ExternalComponentPlugins>)externalComponentPlugins_)
               .clone();
         if (imports_ != null)
         {
            conf.imports_ = (ArrayList<String>)imports_.clone();
         }
         if (removeConfiguration_ != null)
         {
            conf.removeConfiguration_ = (ArrayList<String>)removeConfiguration_.clone();
         }
         return conf;
      }
      catch (CloneNotSupportedException e)
      {
         throw new AssertionError("Could not clone the configuration");
      }
   }
   
   /**
    * Dumps the configuration in XML format into the given {@link Writer}
    */
   public void toXML(Writer w)
   {
      try
      {
         IBindingFactory bfact = BindingDirectory.getFactory(Configuration.class);
         IMarshallingContext mctx = bfact.createMarshallingContext();
         mctx.setIndent(2);
         mctx.marshalDocument(this, "UTF-8", null, w);
      }
      catch (Exception e)
      {
         log.warn("Couldn't dump the runtime configuration in XML Format", e);
      }
   }

   /**
    * Dumps the configuration in XML format into a {@link StringWriter} and 
    * returns the content
    */
   public String toXML()
   {
      StringWriter sw = new StringWriter();
      try
      {
         toXML(sw);
      }
      catch (Exception e)
      {
         log.warn("Cannot convert the configuration to XML format", e);
         return null;
      }
      finally
      {
         try
         {
            sw.close();
         }
         catch (IOException ignore)
         {
         }            
      }
      return sw.toString();
   }
}
