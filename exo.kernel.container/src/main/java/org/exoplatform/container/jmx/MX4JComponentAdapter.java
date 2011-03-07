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

import org.exoplatform.commons.utils.SecurityHelper;
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
import java.security.PrivilegedExceptionAction;
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
            Object instance = exocontainer.createComponent(getComponentImplementation(), params);
            if (instance_ != null)
            {
               // Avoid instantiating twice the same component in case of a cyclic reference due
               // to component plugins
               return instance_;
            }
            exocontainer.addComponentToCtx(getComponentKey(), instance);
            if (debug)
               log.debug("==> create  component : " + instance_);
            if (component != null && component.getComponentPlugins() != null)
            {
               addComponentPlugin(debug, instance, component.getComponentPlugins(), exocontainer);
            }
            ExternalComponentPlugins ecplugins = manager.getConfiguration().getExternalComponentPlugins(componentKey);
            if (ecplugins != null)
            {
               addComponentPlugin(debug, instance, ecplugins.getComponentPlugins(), exocontainer);
            }
            // check if component implement the ComponentLifecycle
            if (instance instanceof ComponentLifecycle)
            {
               ComponentLifecycle lc = (ComponentLifecycle)instance;
               lc.initComponent(exocontainer);
            }
            instance_ = instance;
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
      finally
      {
         exocontainer.removeComponentFromCtx(getComponentKey());
      }
      return instance_;
   }

   private void addComponentPlugin(boolean debug, final Object component,
      List<org.exoplatform.container.xml.ComponentPlugin> plugins, ExoContainer container) throws Exception
   {
      if (plugins == null)
         return;
      for (org.exoplatform.container.xml.ComponentPlugin plugin : plugins)
      {

         try
         {
            Class pluginClass = Class.forName(plugin.getType());
            ComponentPlugin cplugin = (ComponentPlugin)container.createComponent(pluginClass, plugin.getInitParams());
            cplugin.setName(plugin.getName());
            cplugin.setDescription(plugin.getDescription());
            Class clazz = component.getClass();

            final Method m = getSetMethod(clazz, plugin.getSetMethod(), pluginClass);
            if (m == null)
            {
               log.error("Cannot find the method '" + plugin.getSetMethod() + "' that has only one parameter of type '"
                  + pluginClass.getName() + "' in the class '" + clazz.getName() + "'.");
               continue;
            }
            final Object[] params = {cplugin};

            SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Void>()
            {
               public Void run() throws Exception
               {
                  m.invoke(component, params);
                  return null;
               }
            });

            if (debug)
               log.debug("==> add component plugin: " + cplugin);

            cplugin.setName(plugin.getName());
            cplugin.setDescription(plugin.getDescription());
         }
         catch (Exception ex)
         {
            log.error("Failed to instanciate plugin " + plugin.getName() + " for component " + component + ": "
               + ex.getMessage(), ex);
         }
      }
   }

   /**
    * Finds the best "set method" according to the given method name and type of plugin
    * @param clazz the {@link Class} of the target component
    * @param name the name of the method
    * @param pluginClass the {@link Class} of the plugin
    * @return the "set method" corresponding to the given context
    */
   private Method getSetMethod(Class clazz, String name, Class pluginClass)
   {
      Method[] methods = clazz.getMethods();
      Method bestCandidate = null;
      int depth = -1;
      for (Method m : methods)
      {
         if (name.equals(m.getName()))
         {
            Class[] types = m.getParameterTypes();
            if (types != null && types.length == 1 && ComponentPlugin.class.isAssignableFrom(types[0]))
            {
               int currentDepth = getClosestMatchDepth(pluginClass, types[0]);
               if (currentDepth == 0)
               {
                  return m;
               }
               else if (depth == -1 || depth > currentDepth)
               {
                  bestCandidate = m;
                  depth = currentDepth;
               }
            }
         }
      }
      return bestCandidate;
   }

   /**
    * Check if the given plugin class is assignable from the given type, if not we recheck with its parent class
    * until we find the closest match.
    * @param pluginClass the class of the plugin
    * @param type the class from which the plugin must be assignable
    * @return The total amount of times we had to up the hierarchy of the plugin
    */
   private static int getClosestMatchDepth(Class pluginClass, Class type)
   {
      return getClosestMatchDepth(pluginClass, type, 0);
   }
   
   /**
    * Check if the given plugin class is assignable from the given type, if not we recheck with its parent class
    * until we find the closest match.
    * @param pluginClass the class of the plugin
    * @param type the class from which the plugin must be assignable
    * @param depth the current amount of times that we had to up the hierarchy of the plugin
    * @return The total amount of times we had to up the hierarchy of the plugin
    */
   private static int getClosestMatchDepth(Class pluginClass, Class type, int depth)
   {
      if (pluginClass == null || pluginClass.isAssignableFrom(type))
      {
         return depth;
      }
      return getClosestMatchDepth(pluginClass.getSuperclass(), type, depth + 1);
   }
   
   public void verify(PicoContainer container)
   {
   }

}
