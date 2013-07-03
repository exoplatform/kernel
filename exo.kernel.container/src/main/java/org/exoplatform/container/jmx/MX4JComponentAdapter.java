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

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.AbstractComponentAdapter;
import org.exoplatform.container.ConcurrentContainer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentLifecycle;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

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
public class MX4JComponentAdapter<T> extends AbstractComponentAdapter<T>
{
   /**
    * Serial Version ID
    */
   private static final long serialVersionUID = -9001193588034229411L;

   private volatile T instance_;

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.MX4JComponentAdapter");

   /** . */
   protected final ExoContainer exocontainer;

   /** . */
   protected final ConcurrentContainer container;

   public MX4JComponentAdapter(ExoContainer holder, ConcurrentContainer container, Object key, Class<T> implementation)
   {
      super(key, implementation);
      this.exocontainer = holder;
      this.container = container;
   }

   public T getComponentInstance()
   {
      if (instance_ != null)
         return instance_;

      //
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
               componentKey = ((Class<?>)key).getName();
            manager = (ConfigurationManager)exocontainer.getComponentInstanceOfType(ConfigurationManager.class);
            component = manager == null ? null : manager.getComponent(componentKey);
            if (component != null)
            {
               params = component.getInitParams();
               debug = component.getShowDeployInfo();
            }
            // Please note that we cannot fully initialize the Object "instance_" before releasing other
            // threads because it could cause StackOverflowError due to recursive calls
            T instance = exocontainer.createComponent(getComponentImplementation(), params);
            if (instance_ != null)
            {
               // Avoid instantiating twice the same component in case of a cyclic reference due
               // to component plugins
               return instance_;
            }
            container.addComponentToCtx(getComponentKey(), instance);
            if (debug)
               LOG.debug("==> create  component : " + instance_);
            if (component != null && component.getComponentPlugins() != null)
            {
               addComponentPlugin(debug, instance, component.getComponentPlugins(), exocontainer);
            }
            ExternalComponentPlugins ecplugins =
               manager == null ? null : manager.getConfiguration().getExternalComponentPlugins(componentKey);
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
         container.removeComponentFromCtx(getComponentKey());
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
            Class<?> pluginClass = ClassLoading.forName(plugin.getType(), this);
            ComponentPlugin cplugin = (ComponentPlugin)container.createComponent(pluginClass, plugin.getInitParams());
            cplugin.setName(plugin.getName());
            cplugin.setDescription(plugin.getDescription());
            Class<?> clazz = component.getClass();

            final Method m = getSetMethod(clazz, plugin.getSetMethod(), pluginClass);
            if (m == null)
            {
               LOG.error("Cannot find the method '" + plugin.getSetMethod() + "' that has only one parameter of type '"
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
               LOG.debug("==> add component plugin: " + cplugin);

            cplugin.setName(plugin.getName());
            cplugin.setDescription(plugin.getDescription());
         }
         catch (Exception ex)
         {
            LOG.error(
               "Failed to instanciate plugin " + plugin.getName() + " for component " + component + ": "
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
   private Method getSetMethod(Class<?> clazz, String name, Class<?> pluginClass)
   {
      Method[] methods = clazz.getMethods();
      Method bestCandidate = null;
      int depth = -1;
      for (Method m : methods)
      {
         if (name.equals(m.getName()))
         {
            Class<?>[] types = m.getParameterTypes();
            if (types != null && types.length == 1 && ComponentPlugin.class.isAssignableFrom(types[0])
               && types[0].isAssignableFrom(pluginClass))
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
   private static int getClosestMatchDepth(Class<?> pluginClass, Class<?> type)
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
   private static int getClosestMatchDepth(Class<?> pluginClass, Class<?> type, int depth)
   {
      if (pluginClass == null || pluginClass.isAssignableFrom(type))
      {
         return depth;
      }
      return getClosestMatchDepth(pluginClass.getSuperclass(), type, depth + 1);
   }
}
