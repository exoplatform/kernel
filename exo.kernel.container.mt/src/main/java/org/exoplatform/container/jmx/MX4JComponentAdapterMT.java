/*
 * Copyright (C) 2013 eXo Platform SAS.
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
import org.exoplatform.container.ComponentAdapterStateAware;
import org.exoplatform.container.ComponentState;
import org.exoplatform.container.ComponentTask;
import org.exoplatform.container.ComponentTaskContext;
import org.exoplatform.container.ComponentTaskType;
import org.exoplatform.container.ConcurrentContainerMT;
import org.exoplatform.container.CyclicDependencyException;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentLifecycle;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class MX4JComponentAdapterMT extends ComponentAdapterStateAware
{
   /**
    * Serial Version ID
    */
   private static final long serialVersionUID = -9001193588034229411L;

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.MX4JComponentAdapterMT");

   private final AtomicReference<Collection<Class<?>>> createDependencies = new AtomicReference<Collection<Class<?>>>();

   private final AtomicReference<Collection<Class<?>>> initDependencies = new AtomicReference<Collection<Class<?>>>();

   /** . */
   protected final ExoContainer exocontainer;

   /** . */
   protected final ConcurrentContainerMT container;

   public MX4JComponentAdapterMT(ExoContainer holder, ConcurrentContainerMT container, Object key, Class<?> implementation)
   {
      super(container, key, implementation);
      this.exocontainer = holder;
      this.container = container;
   }

   private void addComponentPlugin(List<ComponentTask<Void>> tasks, Set<Class<?>> dependencies, boolean debug,
      final Object component, List<org.exoplatform.container.xml.ComponentPlugin> plugins,
      ComponentTaskContext ctx) throws Exception
   {
      if (plugins == null)
         return;
      for (org.exoplatform.container.xml.ComponentPlugin plugin : plugins)
      {
         try
         {
            Class<?> pluginClass = ClassLoading.forName(plugin.getType(), this);
            Constructor<?> constructor = container.getConstructor(pluginClass);
            Class<?>[] parameters = constructor.getParameterTypes();
            for (int i = 0; i < parameters.length; i++)
            {
               Class<?> parameter = parameters[i];
               if (!parameter.equals(InitParams.class) && !parameter.equals(getComponentKey()))
               {
                  ctx.checkDependency(parameter, ComponentTaskType.INIT);
                  dependencies.add(parameter);
               }
            }
            tasks.add(createPlugin(this, container, pluginClass, debug, component, plugin, constructor,
               plugin.getInitParams(), ctx));
         }
         catch (CyclicDependencyException e)
         {
            throw e;
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
   private static Method getSetMethod(Class<?> clazz, String name, Class<?> pluginClass)
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

   /**
    * {@inheritDoc}
    */
   protected Collection<Class<?>> getCreateDependencies()
   {
      return createDependencies.get();
   }

   /**
    * {@inheritDoc}
    */
   protected Collection<Class<?>> getInitDependencies()
   {
      return initDependencies.get();
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   protected ComponentTask<Object> getCreateTask(ComponentTaskContext ctx)
   {
      Component component = null;
      String componentKey;
      InitParams params = null;
      boolean debug = false;

      // Get the component
      Object key = getComponentKey();
      if (key instanceof String)
         componentKey = (String)key;
      else
         componentKey = ((Class<?>)key).getName();
      try
      {
         ConfigurationManager manager =
            (ConfigurationManager)exocontainer.getComponentInstanceOfType(ConfigurationManager.class);
         component = manager == null ? null : manager.getComponent(componentKey);
         if (component != null)
         {
            params = component.getInitParams();
            debug = component.getShowDeployInfo();
         }
         if (debug)
            LOG.debug("==> get constructor of the component : " + getComponentImplementation());
         Constructor<?> constructor = container.getConstructor(getComponentImplementation());
         setCreateDependencies(constructor, ctx);
         if (debug)
            LOG.debug("==> create component : " + getComponentImplementation());
         return (ComponentTask<Object>)container.createComponentTask(constructor, params, ctx, this);
      }
      catch (CyclicDependencyException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         String msg = "Cannot instantiate component " + getComponentImplementation();
         if (component != null)
         {
            msg =
               "Cannot instantiate component key=" + component.getKey() + " type=" + component.getType() + " found at "
                  + component.getDocumentURL();
         }
         throw new RuntimeException(msg, e);
      }
   }

   protected void setCreateDependencies(Constructor<?> constructor, ComponentTaskContext ctx)
   {
      if (createDependencies.get() == null)
      {
         List<Class<?>> classes = new ArrayList<Class<?>>(constructor.getParameterTypes().length);
         Class<?>[] parameters = constructor.getParameterTypes();
         for (int i = 0; i < parameters.length; i++)
         {
            Class<?> parameter = parameters[i];
            if (!parameter.equals(InitParams.class))
            {
               ctx.checkDependency(parameter, ComponentTaskType.CREATE);
               classes.add(parameter);
            }
         }
         createDependencies.compareAndSet(null, classes);
      }
   }

   /**
    * {@inheritDoc}
    */
   protected Collection<ComponentTask<Void>> getInitTasks(final Object instance, ComponentTaskContext ctx)
   {
      Component component = null;
      String componentKey;
      boolean debug = false;

      // Get the component
      Object key = getComponentKey();
      if (key instanceof String)
         componentKey = (String)key;
      else
         componentKey = ((Class<?>)key).getName();
      try
      {
         ConfigurationManager manager =
            (ConfigurationManager)exocontainer.getComponentInstanceOfType(ConfigurationManager.class);
         component = manager == null ? null : manager.getComponent(componentKey);
         if (component != null)
         {
            debug = component.getShowDeployInfo();
         }
         List<ComponentTask<Void>> tasks = new ArrayList<ComponentTask<Void>>();
         Set<Class<?>> dependencies = new HashSet<Class<?>>();
         if (component != null && component.getComponentPlugins() != null)
         {
            addComponentPlugin(tasks, dependencies, debug, instance, component.getComponentPlugins(), ctx);
         }
         ExternalComponentPlugins ecplugins =
            manager == null ? null : manager.getConfiguration().getExternalComponentPlugins(componentKey);
         if (ecplugins != null)
         {
            addComponentPlugin(tasks, dependencies, debug, instance, ecplugins.getComponentPlugins(), ctx);
         }
         initDependencies.compareAndSet(null, dependencies);
         tasks.add(new ComponentTask<Void>("initialize component", container, ctx, this, ComponentTaskType.INIT)
         {
            public Void execute() throws Exception
            {
               // check if component implement the ComponentLifecycle
               if (instance instanceof ComponentLifecycle && exocontainer instanceof ExoContainer)
               {
                  ComponentLifecycle lc = (ComponentLifecycle)instance;
                  lc.initComponent((ExoContainer)exocontainer);
               }
               return null;
            }
         });
         return tasks;
      }
      catch (CyclicDependencyException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         String msg = "Cannot initialize component " + getComponentImplementation();
         if (component != null)
         {
            msg =
               "Cannot initialize component key=" + component.getKey() + " type=" + component.getType() + " found at "
                  + component.getDocumentURL();
         }
         throw new RuntimeException(msg, e);
      }
   }

   private static <T> ComponentTask<Void> createPlugin(final ComponentAdapterStateAware caller,
      final ConcurrentContainerMT exocontainer, final Class<?> pluginClass, final boolean debug, final Object component,
      final org.exoplatform.container.xml.ComponentPlugin plugin, final Constructor<T> constructor, InitParams params,
      final ComponentTaskContext ctx) throws Exception
   {
      final Object[] args = exocontainer.getArguments(constructor, params, ctx, ComponentTaskType.INIT);
      return new ComponentTask<Void>("create/add plugin " + plugin.getName() + " for component " + component,
         exocontainer, ctx, caller, ComponentTaskType.INIT)
      {
         public Void execute() throws Exception
         {
            try
            {
               ComponentPlugin cplugin = (ComponentPlugin)constructor.newInstance(args);
               ComponentState state = caller.getState();
               if (state != ComponentState.CREATED)
               {
                  // Prevent adding several times the plugin in case of cyclic dependency
                  return null;
               }
               cplugin.setName(plugin.getName());
               cplugin.setDescription(plugin.getDescription());
               Class<?> clazz = component.getClass();

               final Method m = getSetMethod(clazz, plugin.getSetMethod(), pluginClass);
               if (m == null)
               {
                  LOG.error("Cannot find the method '" + plugin.getSetMethod()
                     + "' that has only one parameter of type '" + pluginClass.getName() + "' in the class '"
                     + clazz.getName() + "'.");
                  return null;
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
               return null;
            }
            catch (InvocationTargetException e)
            {
               if (e.getCause() instanceof Exception)
               {
                  throw (Exception)e.getCause();
               }
               throw e;
            }
         }
      };
   }
}
