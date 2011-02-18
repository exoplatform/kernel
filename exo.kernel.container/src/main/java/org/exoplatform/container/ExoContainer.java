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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.component.ComponentLifecyclePlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.management.ManageableContainer;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Jul 18, 2004 Time: 12:15:28 AM
 */
public class ExoContainer extends ManageableContainer
{

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = -8068506531004854036L;

   /**
    * Returns an unmodifable set of profiles defined by the value returned by invoking
    * {@link PropertyManager#getProperty(String)} with the {@link org.exoplatform.commons.utils.PropertyManager#RUNTIME_PROFILES}
    * property.
    *
    * @return the set of profiles
    */
   public static Set<String> getProfiles()
   {
      //
      Set<String> profiles = new HashSet<String>();

      // Obtain profile list by runtime properties
      String profileList = PropertyManager.getProperty(PropertyManager.RUNTIME_PROFILES);
      if (profileList != null)
      {
         for (String profile : profileList.split(","))
         {
            profiles.add(profile.trim());
         }
      }

      //
      return Collections.unmodifiableSet(profiles);
   }

   static Log log = ExoLogger.getLogger("exo.kernel.container.ExoContainer");
   
   /**
    * Context used to keep in memory the components that are currently being created.
    * This context is used to prevent cyclic resolution due to component plugins.
    */
   private final ThreadLocal<Map<Object, Object>> depResolutionCtx = new ThreadLocal<Map<Object, Object>>();

   private Map<String, ComponentLifecyclePlugin> componentLifecylePlugin_ =
      new HashMap<String, ComponentLifecyclePlugin>();

   private List<ContainerLifecyclePlugin> containerLifecyclePlugin_ = new ArrayList<ContainerLifecyclePlugin>();

   protected ExoContainerContext context;

   protected PicoContainer parent;

   public ExoContainer()
   {
      context = new ExoContainerContext(this);
      context.setName(this.getClass().getName());
      registerComponentInstance(context);
      this.parent = null;
   }

   public ExoContainer(PicoContainer parent)
   {
      super(parent);
      context = new ExoContainerContext(this);
      context.setName(this.getClass().getName());
      registerComponentInstance(context);
      this.parent = parent;
   }

   public ExoContainer(ComponentAdapterFactory factory, PicoContainer parent)
   {
      super(factory, parent);
      context = new ExoContainerContext(this);
      context.setName(this.getClass().getName());
      registerComponentInstance(context);
      this.parent = parent;
   }

   public ExoContainerContext getContext()
   {
      return context;
   }

   /**
    * @return the name of the plugin if it is not empty, the FQN of the plugin otherwise
    */
   private static String getPluginName(ContainerLifecyclePlugin plugin)
   {
      String name = plugin.getName();
      if (name == null || name.length() == 0)
      {
         name = plugin.getClass().getName();
      }
      return name;
   }
   
   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated
   public void initContainer() throws Exception
   {      
   }
      
   private void initContainerInternal()
   {
      ConfigurationManager manager = (ConfigurationManager)getComponentInstanceOfType(ConfigurationManager.class);
      ContainerUtil.addContainerLifecyclePlugin(this, manager);
      ContainerUtil.addComponentLifecyclePlugin(this, manager);
      ContainerUtil.addComponents(this, manager);
      for (ContainerLifecyclePlugin plugin : containerLifecyclePlugin_)
      {
         try
         {
            plugin.initContainer(this);
         }
         catch (Exception e)
         {
            log.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
   }

   @Override
   public void dispose()
   {
      destroyContainerInternal();
      super.dispose();
   }

   /**
    * Starts the container
    * @param init indicates if the container must be initialized first
    */
   public void start(boolean init)
   {
      if (init)
      {
         // Initialize the container first
         initContainerInternal();
      }
      start();
   }
   
   @Override
   public void start()
   {
      super.start();
      startContainerInternal();
   }

   @Override
   public void stop()
   {
      stopContainerInternal();
      super.stop();
   }

   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated 
   public void startContainer() throws Exception
   {      
   }
   
   private void startContainerInternal()
   {
      for (ContainerLifecyclePlugin plugin : containerLifecyclePlugin_)
      {
         try
         {
            plugin.startContainer(this);
         }
         catch (Exception e)
         {
            log.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
   }

   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated 
   public void stopContainer() throws Exception
   {      
   }

   private void stopContainerInternal()
   {
      for (ContainerLifecyclePlugin plugin : containerLifecyclePlugin_)
      {
         try
         {
            plugin.stopContainer(this);
         }
         catch (Exception e)
         {
            log.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
   }

   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated 
   public void destroyContainer() throws Exception
   {      
   }

   private void destroyContainerInternal()
   {
      for (ContainerLifecyclePlugin plugin : containerLifecyclePlugin_)
      {
         try
         {
            plugin.destroyContainer(this);
         }
         catch (Exception e)
         {
            log.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
   }

   public void addComponentLifecylePlugin(ComponentLifecyclePlugin plugin)
   {
      List<String> list = plugin.getManageableComponents();
      for (String component : list)
         componentLifecylePlugin_.put(component, plugin);
   }

   public void addContainerLifecylePlugin(ContainerLifecyclePlugin plugin)
   {
      containerLifecyclePlugin_.add(plugin);
   }

   public <T> T createComponent(Class<T> clazz) throws Exception
   {
      return createComponent(clazz, null);
   }

   public <T> T createComponent(Class<T> clazz, InitParams params) throws Exception
   {
      if (log.isDebugEnabled())
         log.debug(clazz.getName() + " " + ((params != null) ? params : "") + " added to " + getContext().getName());
      Constructor<?>[] constructors = new Constructor<?>[0];
      try
      {
         constructors = ContainerUtil.getSortedConstructors(clazz);
      }
      catch (NoClassDefFoundError err)
      {
         throw new Exception("Cannot resolve constructor for class " + clazz.getName(), err);
      }
      Class<?> unknownParameter = null;
      for (int k = 0; k < constructors.length; k++)
      {
         Constructor<?> constructor = constructors[k];
         Class<?>[] parameters = constructor.getParameterTypes();
         Object[] args = new Object[parameters.length];
         boolean satisfied = true;
         for (int i = 0; i < args.length; i++)
         {
            if (parameters[i].equals(InitParams.class))
            {
               args[i] = params;
            }
            else
            {
               args[i] = getComponentInstanceOfTypeInternal(parameters[i]);
               if (args[i] == null)
               {
                  satisfied = false;
                  unknownParameter = parameters[i];
                  break;
               }
            }
         }
         if (satisfied)
            return clazz.cast(constructor.newInstance(args));
      }
      throw new Exception("Cannot find a satisfying constructor for " + clazz + " with parameter " + unknownParameter);
   }
   
   /**
    * Gets the {@link ConfigurationManager} from the given {@link ExoContainer} if it exists, 
    * then returns the nested {@link Configuration} otherwise it returns <code>null</code>
    */
   protected Configuration getConfiguration()
   {
      ConfigurationManager cm = (ConfigurationManager)getComponentInstanceOfType(ConfigurationManager.class);
      return cm == null ? null : cm.getConfiguration();
   }
   
   /**
    * Add the component corresponding to the given key, to the dependency resolution
    * context
    * @param key The key of the component to add to the context
    * @param component The instance of the component to add to the context
    */
   public void addComponentToCtx(Object key, Object component)
   {
      Map<Object, Object> map = depResolutionCtx.get();
      if (map == null)
      {
         map = new HashMap<Object, Object>();
         depResolutionCtx.set(map);
      }
      map.put(key, component);
   }
   
   /**
    * Remove the component corresponding to the given key, from the dependency resolution
    * context
    * @param key The key of the component to remove from the context
    */
   public void removeComponentFromCtx(Object key)
   {
      Map<Object, Object> map = depResolutionCtx.get();
      if (map != null)
      {
         map.remove(key);
         if (map.isEmpty())
         {
            depResolutionCtx.set(null);
         }
      }
   }
   
   /**
    * Try to get the component from the dependency resolution context and if it cannot be found
    * it will try to get it from the method <code>getComponentInstanceOfType</code>
    * @param componentType the type of the component to find
    * @return the instance of the component corresponding to the given component type
    */
   private Object getComponentInstanceOfTypeInternal(Class componentType)
   {
      Map<Object, Object> map = depResolutionCtx.get();
      if (map != null)
      {
         Object result = map.get(componentType);
         if (result != null)
         {
            return result;
         }
      }
      return getComponentInstanceOfType(componentType);
   }
}
