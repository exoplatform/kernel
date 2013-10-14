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
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ComponentAdapterDependenciesAware;
import org.exoplatform.container.ComponentTask;
import org.exoplatform.container.ComponentTaskContext;
import org.exoplatform.container.ComponentTaskType;
import org.exoplatform.container.ConcurrentContainer.CreationalContextComponentAdapter;
import org.exoplatform.container.ConcurrentContainerMT;
import org.exoplatform.container.CyclicDependencyException;
import org.exoplatform.container.Dependency;
import org.exoplatform.container.DependencyStackListener;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.LockManager;
import org.exoplatform.container.component.ComponentLifecycle;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicReference;

import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.CreationalContext;
import javax.inject.Singleton;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class MX4JComponentAdapterMT<T> extends MX4JComponentAdapter<T> implements DependencyStackListener,
   ComponentAdapterDependenciesAware<T>
{

   /**
    * Serial Version ID
    */
   private static final long serialVersionUID = -9001193588034229411L;

   private transient final AtomicReference<Collection<Dependency>> createDependencies =
      new AtomicReference<Collection<Dependency>>();

   private transient final AtomicReference<Collection<Dependency>> initDependencies =
      new AtomicReference<Collection<Dependency>>();

   /**
    * The task to use to create the component
    */
   private transient final AtomicReference<ComponentTask<T>> createTask = new AtomicReference<ComponentTask<T>>();

   /**
    * The task to use to init the component
    */
   private transient final AtomicReference<Collection<ComponentTask<Void>>> initTasks =
      new AtomicReference<Collection<ComponentTask<Void>>>();

   /** . */
   private transient final ConcurrentContainerMT container;

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.MX4JComponentAdapterMT");

   public MX4JComponentAdapterMT(ExoContainer holder, ConcurrentContainerMT container, Object key,
      Class<T> implementation)
   {
      super(holder, container, key, implementation, LockManager.getInstance().createLock());
      this.container = container;
   }

   private void addComponentPlugin(List<ComponentTask<Void>> tasks, Set<Dependency> dependencies, boolean debug,
      List<org.exoplatform.container.xml.ComponentPlugin> plugins) throws Exception
   {
      if (plugins == null)
         return;
      for (org.exoplatform.container.xml.ComponentPlugin plugin : plugins)
      {
         try
         {
            Class<?> pluginClass = ClassLoading.forName(plugin.getType(), this);
            List<Dependency> lDependencies = new ArrayList<Dependency>();
            @SuppressWarnings("unchecked")
            Constructor<T> constructor = (Constructor<T>)container.getConstructor(pluginClass, lDependencies);
            dependencies.addAll(lDependencies);
            tasks.add(createPlugin(this, container, pluginClass, debug, plugin, constructor, plugin.getInitParams(),
               lDependencies));
         }
         catch (CyclicDependencyException e)
         {
            throw e;
         }
         catch (Exception ex)
         {
            LOG.error("Failed to instanciate plugin " + plugin.getName() + " for component "
               + getComponentImplementation() + ": " + ex.getMessage(), ex);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Dependency> getCreateDependencies()
   {
      return createDependencies.get();
   }

   /**
    * {@inheritDoc}
    */
   public Collection<Dependency> getInitDependencies()
   {
      return initDependencies.get();
   }

   /**
    * {@inheritDoc}
    */
   public void callDependency(ComponentTask<?> task, Dependency dep)
   {
      if (PropertyManager.isDevelopping())
      {
         if (dep.getKey() instanceof String
            || (dep.getKey() instanceof Class && ((Class<?>)dep.getKey()).isAnnotation()))
         {
            LOG.warn("An unexpected call of getComponentInstance(" + dep.getKey() + "," + dep.getBindType().getName()
               + ") has been detected please add the component in your constructor instead", new Exception(
               "This is the stack trace allowing you to identify where the unexpected "
                  + "call of getComponentInstanceOfType has been done"));
         }
         else if (dep.getKey() instanceof Class)
         {
            LOG.warn("An unexpected call of getComponentInstanceOfType(" + ((Class<?>)dep.getKey()).getName()
               + ") has been detected please add the component in your constructor instead", new Exception(
               "This is the stack trace allowing you to identify where the unexpected "
                  + "call of getComponentInstanceOfType has been done"));
         }
      }
      if (dep.getKey().equals(getComponentKey()))
      {
         return;
      }
      if (task.getType() == ComponentTaskType.CREATE)
      {
         getCreateDependencies().add(dep);
      }
      else if (task.getType() == ComponentTaskType.INIT)
      {
         getInitDependencies().add(dep);
      }
      container.getComponentTaskContext().checkDependency(dep.getKey(), task.getType());
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   protected ComponentTask<T> getCreateTask()
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
         List<Dependency> lDependencies = new ArrayList<Dependency>();
         Constructor<?> constructor = container.getConstructor(getComponentImplementation(), lDependencies);
         setCreateDependencies(lDependencies);
         if (debug)
            LOG.debug("==> create component : " + getComponentImplementation());
         return (ComponentTask<T>)container.createComponentTask(constructor, params, lDependencies, this);
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

   protected void setCreateDependencies(List<Dependency> lDependencies)
   {
      if (createDependencies.get() == null)
      {
         createDependencies.compareAndSet(null, new CopyOnWriteArraySet<Dependency>(lDependencies));
      }
   }

   /**
    * {@inheritDoc}
    */
   protected Collection<ComponentTask<Void>> getInitTasks()
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
         Set<Dependency> dependencies = new HashSet<Dependency>();

         final Class<T> implementationClass = getComponentImplementation();
         boolean isSingleton = this.isSingleton;
         boolean isInitialized = this.isInitialized;
         if (debug)
            LOG.debug("==> create  component : " + implementationClass.getName());
         boolean hasInjectableConstructor = !isSingleton || ContainerUtil.hasInjectableConstructor(implementationClass);
         boolean hasOnlyEmptyPublicConstructor =
            !isSingleton || ContainerUtil.hasOnlyEmptyPublicConstructor(implementationClass);
         if (hasInjectableConstructor || hasOnlyEmptyPublicConstructor)
         {
            // There is at least one constructor JSR 330 compliant or we already know 
            // that it is not a singleton such that the new behavior is expected
            List<Dependency> lDependencies = new ArrayList<Dependency>();
            boolean isInjectPresent = container.initializeComponent(implementationClass, lDependencies, tasks, this);
            dependencies.addAll(lDependencies);
            isSingleton = manageScope(isSingleton, isInitialized, hasInjectableConstructor, isInjectPresent);
         }
         else if (!isInitialized)
         {
            // The adapter has not been initialized yet
            // The old behavior is expected as there is no constructor JSR 330 compliant 
            isSingleton = this.isSingleton = true;
            scope.set(Singleton.class);
         }
         if (component != null && component.getComponentPlugins() != null)
         {
            addComponentPlugin(tasks, dependencies, debug, component.getComponentPlugins());
         }
         ExternalComponentPlugins ecplugins =
            manager == null ? null : manager.getConfiguration().getExternalComponentPlugins(componentKey);
         if (ecplugins != null)
         {
            addComponentPlugin(tasks, dependencies, debug, ecplugins.getComponentPlugins());
         }
         initDependencies.compareAndSet(null, new CopyOnWriteArraySet<Dependency>(dependencies));
         tasks.add(new ComponentTask<Void>("initialize component " + getComponentImplementation().getName(), container,
            this, ComponentTaskType.INIT)
         {
            public Void execute(CreationalContextComponentAdapter<?> cCtx) throws Exception
            {
               // check if component implement the ComponentLifecycle
               if (cCtx.get() instanceof ComponentLifecycle && exocontainer instanceof ExoContainer)
               {
                  ComponentLifecycle lc = (ComponentLifecycle)cCtx.get();
                  lc.initComponent((ExoContainer)exocontainer);
               }
               return null;
            }
         });
         if (!isInitialized)
         {
            this.isInitialized = true;
         }
         return tasks;
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

   private ComponentTask<Void> createPlugin(final MX4JComponentAdapterMT<T> caller,
      final ConcurrentContainerMT exocontainer, final Class<?> pluginClass, final boolean debug,
      final org.exoplatform.container.xml.ComponentPlugin plugin, final Constructor<T> constructor, InitParams params,
      List<Dependency> lDependencies) throws Exception
   {
      final Object[] args = exocontainer.getArguments(constructor, params, lDependencies);
      return new ComponentTask<Void>("create/add plugin " + plugin.getName() + " for component "
         + getComponentImplementation().getName(), exocontainer, caller, ComponentTaskType.INIT)
      {
         public Void execute(final CreationalContextComponentAdapter<?> cCtx) throws Exception
         {
            try
            {
               getContainer().loadArguments(args);
               ComponentPlugin cplugin = (ComponentPlugin)constructor.newInstance(args);
               cplugin.setName(plugin.getName());
               cplugin.setDescription(plugin.getDescription());
               Class<?> clazz = getComponentImplementation();

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
                     m.invoke(cCtx.get(), params);
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

   protected T createInstance(final Context ctx)
   {
      T result = ctx.get(this);
      if (result != null)
      {
         return result;
      }
      return create(new Callable<T>()
      {
         public T call() throws Exception
         {
            try
            {
               return ctx.get(MX4JComponentAdapterMT.this, container.<T> addComponentToCtx(getComponentKey()));
            }
            finally
            {
               container.removeComponentFromCtx(getComponentKey());
            }
         }
      });
   }

   /**
    * Must be used to create Singleton or Prototype only
    */
   protected T create()
   {
      return create(new Callable<T>()
      {
         public T call() throws Exception
         {
            return doCreate();
         }
      });
   }

   /**
    * Must be used to create Singleton or Prototype only
    */
   protected T doCreate()
   {
      return doCreate(false);
   }

   /**
    * Must be used to create Singleton or Prototype only
    */
   protected T doCreate(boolean useSharedMemory)
   {
      if (instance_ != null)
      {
         return instance_;
      }
      boolean toBeLocked = isSingleton;
      boolean skipFinally = false;
      try
      {
         CreationalContextComponentAdapter<T> ctx;
         if (toBeLocked)
         {
            if (useSharedMemory)
            {
               T result = container.<T> getComponentFromSharedMemory(getComponentKey());
               if (result != null)
               {
                  LOG.debug("The value could be found from the shared memory");
                  skipFinally = true;
                  return result;
               }
               LOG.debug("The value could not be found from the shared memory");
            }
            if (!lock.tryLock())
            {
               // The lock has already been acquired, let's make sure that we
               // don't have any deadlocks
               lock.lockInterruptibly();
            }
            ctx = container.<T> addComponentToCtx(getComponentKey());
         }
         else
         {
            // Don't add to context non singleton
            skipFinally = true;
            ctx = new CreationalContextComponentAdapter<T>();
         }
         return create(ctx);
      }
      catch (InterruptedException e)
      {
         // We make sure that the state of the Thread is back to normal
         Thread.interrupted();
         skipFinally = true;
         LOG.debug("A deadlock has been detected, let's retry using the shared memory");
         return doCreate(true);
      }
      finally
      {
         if (!skipFinally)
         {
            lock.unlock();
            container.removeComponentFromCtx(getComponentKey());
         }
      }
   }

   private T create(Callable<T> mainCreateTask)
   {
      ComponentTaskContext ctx = container.getComponentTaskContext();
      try
      {
         loadTasks();
         loadDependencies(ctx);
         return mainCreateTask.call();
      }
      catch (CyclicDependencyException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot create component " + getComponentImplementation(), e);
      }
      finally
      {
         if (ctx == null)
         {
            container.setComponentTaskContext(null);
         }
      }
   }

   private void loadDependencies(ComponentTaskContext ctx) throws Exception
   {
      ComponentTaskContext createCtx = ctx;
      if (createCtx == null)
      {
         createCtx = new ComponentTaskContext(getComponentKey(), ComponentTaskType.CREATE);
         container.setComponentTaskContext(createCtx);
      }
      else if (!createCtx.isLast(getComponentKey()))
      {
         createCtx = createCtx.addToContext(getComponentKey());
         container.setComponentTaskContext(createCtx);
      }
      container.loadDependencies(getComponentKey(), createCtx, getCreateDependencies(), ComponentTaskType.CREATE);
   }

   /**
    * {@inheritDoc}
    */
   public T create(CreationalContext<T> creationalContext)
   {
      CreationalContextComponentAdapter<T> ctx = (CreationalContextComponentAdapter<T>)creationalContext;
      // Avoid to create duplicate instances if it is called at the same time by several threads
      if (instance_ != null)
         return instance_;
      else if (ctx.get() != null)
         return ctx.get();
      ComponentTaskContext taskCtx = container.getComponentTaskContext();
      boolean isRoot = taskCtx.isRoot();
      if (!isRoot)
      {
         container.setComponentTaskContext(taskCtx = taskCtx.setLastTaskType(ComponentTaskType.CREATE));
      }
      try
      {
         ComponentTask<T> task = createTask.get();
         T result = task.call(ctx);
         if (instance_ != null)
         {
            // Avoid instantiating twice the same component in case of a cyclic reference due
            // to component plugins
            return instance_;
         }
         else if (ctx.get() != null)
            return ctx.get();

         ctx.push(result);
      }
      catch (CyclicDependencyException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Cannot create component " + getComponentImplementation(), e);
      }
      if (isRoot)
      {
         container.setComponentTaskContext(taskCtx =
            taskCtx.resetDependencies(getComponentKey(), ComponentTaskType.INIT));
      }
      else
      {
         container.setComponentTaskContext(taskCtx = taskCtx.setLastTaskType(ComponentTaskType.INIT));
      }

      Collection<ComponentTask<Void>> tasks = initTasks.get();
      ComponentTask<Void> task = null;
      try
      {
         if (tasks != null && !tasks.isEmpty())
         {
            container.loadDependencies(getComponentKey(), taskCtx, getInitDependencies(), ComponentTaskType.INIT);
            for (Iterator<ComponentTask<Void>> it = tasks.iterator(); it.hasNext();)
            {
               task = it.next();
               task.call(ctx);
               task = null;
            }
         }
         if (instance_ != null)
         {
            return instance_;
         }
         else if (instance_ == null && isSingleton)
         {
            // In case of cyclic dependency the component could be already initialized
            // so we need to recheck the state
            instance_ = ctx.get();
         }
      }
      catch (CyclicDependencyException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         if (task != null)
         {
            throw new RuntimeException("Cannot " + task.getName() + " for the component "
               + getComponentImplementation(), e);
         }
         throw new RuntimeException("Cannot initialize component " + getComponentImplementation(), e);
      }
      if (ctx.get() instanceof Startable && exocontainer.canBeStopped())
      {
         try
         {
            // Start the component if the container is already started
            ((Startable)ctx.get()).start();
         }
         catch (Exception e)
         {
            throw new RuntimeException("Cannot auto-start component " + getComponentImplementation(), e);
         }
      }
      return ctx.get();
   }

   private void loadTasks()
   {

      if (createTask.get() == null)
      {
         try
         {
            createTask.compareAndSet(null, getCreateTask());
         }
         catch (RuntimeException e)
         {
            throw new RuntimeException("Cannot get the create task of the component " + getComponentImplementation(), e);
         }
      }
      if (initTasks.get() == null)
      {
         try
         {
            initTasks.compareAndSet(null, getInitTasks());
         }
         catch (RuntimeException e)
         {
            throw new RuntimeException("Cannot get the init tasks of the component " + getComponentImplementation(), e);
         }
      }
   }
}
