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
package org.exoplatform.container.jmx;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentLifecycle;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.AbstractComponentAdapter;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author James Strachan
 * @author Mauro Talevi
 * @author Jeppe Cramon
 * @author Benjamin Mestrallet
 * @version $Revision: 1.5 $
 */
@SuppressWarnings("unchecked")
public class MX4JComponentAdapter extends AbstractComponentAdapter
{
   /**
    * Serial Version ID
    */
   private static final long serialVersionUID = -9001193588034229411L;

   private volatile Object instance_;

   private Log log = ExoLogger.getLogger("exo.kernel.container.MX4JComponentAdapter");

   public MX4JComponentAdapter(Object key, Class implementation)
   {
      super(key, implementation);
   }

   public Object getComponentInstance(PicoContainer container)
   {
      if (instance_ != null)
         return instance_;

      //
      ExoContainer exocontainer = (ExoContainer)container;
      Component component = null;
      ConfigurationManager manager;
      String componentKey;
      try
      {
         InitParams params = null;
         boolean debug = false;
         synchronized (this)
         {
            // Avoid to create duplicate instances if it is called at the same time by several threads
            if (instance_ != null)
               return instance_;
            // Get the component
            Object key = getComponentKey();
            if (key instanceof String)
               componentKey = (String)key;
            else
               componentKey = ((Class)key).getName();
            manager = (ConfigurationManager)exocontainer.getComponentInstanceOfType(ConfigurationManager.class);
            component = manager.getComponent(componentKey);
            if (component != null)
            {
               params = component.getInitParams();
               debug = component.getShowDeployInfo();
            }
            // Please note that we cannot fully initialize the Object "instance_" before releasing other
            // threads because it could cause StackOverflowError due to recursive calls
            instance_ = exocontainer.createComponent(getComponentImplementation(), params);
         }

         if (debug)
            log.debug("==> create  component : " + instance_);
         if (component != null && component.getComponentPlugins() != null)
         {
            addComponentPlugin(debug, instance_, component.getComponentPlugins(), exocontainer);
         }
         ExternalComponentPlugins ecplugins = manager.getConfiguration().getExternalComponentPlugins(componentKey);
         if (ecplugins != null)
         {
            addComponentPlugin(debug, instance_, ecplugins.getComponentPlugins(), exocontainer);
         }
         // check if component implement the ComponentLifecycle
         if (instance_ instanceof ComponentLifecycle)
         {
            ComponentLifecycle lc = (ComponentLifecycle)instance_;
            lc.initComponent(exocontainer);
         }
      }
      catch (Exception ex)
      {
         String msg = "Cannot instantiate component " + getComponentImplementation();
         if (component != null)
         {
            msg =
               "Cannot instantiate component key=" + component.getKey() + " type=" + component.getType() + " found at "
                  + component.getDocumentURL();
         }
         throw new RuntimeException(msg, ex);
      }

      return instance_;
   }

   private void addComponentPlugin(boolean debug, Object component,
      List<org.exoplatform.container.xml.ComponentPlugin> plugins, ExoContainer container) throws Exception
   {
      if (plugins == null)
         return;
      for (org.exoplatform.container.xml.ComponentPlugin plugin : plugins)
      {

         try
         {
            Class clazz = Class.forName(plugin.getType());
            ComponentPlugin cplugin = (ComponentPlugin)container.createComponent(clazz, plugin.getInitParams());
            cplugin.setName(plugin.getName());
            cplugin.setDescription(plugin.getDescription());
            clazz = component.getClass();

            Method m = getSetMethod(clazz, plugin.getSetMethod());
            Object[] params = {cplugin};
            m.invoke(component, params);
            if (debug)
               log.debug("==> add component plugin: " + cplugin);

            cplugin.setName(plugin.getName());
            cplugin.setDescription(plugin.getDescription());
         }
         catch (Exception ex)
         {
            log.error("Failed to instanciate plugin " + plugin.getName() + "for component " + component + ": "
               + ex.getMessage(), ex);
         }
      }
   }

   private Method getSetMethod(Class clazz, String name)
   {
      Method[] methods = clazz.getMethods();
      for (Method m : methods)
      {
         if (name.equals(m.getName()))
         {
            Class[] types = m.getParameterTypes();
            if (types != null && types.length == 1 && ComponentPlugin.class.isAssignableFrom(types[0]))
            {
               return m;
            }
         }
      }
      return null;
   }

   public void verify(PicoContainer container)
   {
   }

}
