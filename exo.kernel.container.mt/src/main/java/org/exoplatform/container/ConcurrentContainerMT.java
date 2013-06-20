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
package org.exoplatform.container;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.management.ManageableComponentAdapterFactoryMT;
import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ComponentAdapterFactory;
import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.InitParams;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ConcurrentContainerMT extends ConcurrentContainer
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -1059330085804288350L;

   private static volatile transient ExecutorService EXECUTOR;

   private final transient ThreadLocal<ComponentTaskContext> currentCtx = new ThreadLocal<ComponentTaskContext>();

   /**
    * Used to detect all the dependencies not properly defined
    */
   protected final transient ThreadLocal<Deque<DependencyStack>> dependencyStacks = Mode
      .hasMode(Mode.AUTO_SOLVE_DEP_ISSUES) ? new ThreadLocal<Deque<DependencyStack>>() : null;

   private static ExecutorService getExecutor()
   {
      if (EXECUTOR == null)
      {
         synchronized (ConcurrentContainerMT.class)
         {
            if (EXECUTOR == null)
            {
               EXECUTOR =
                  Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new KernelThreadFactory());
            }
         }
      }
      return EXECUTOR;
   }

   /**
    * Creates a new container with the default {@link ComponentAdapterFactory} and a parent container.
    */
   public ConcurrentContainerMT()
   {
   }

   /**
    * Creates a new container with the default {@link ComponentAdapterFactory} and a parent container.
    *
    * @param holder                  the holder of the container
    * @param parent                  the parent container (used for component dependency lookups).
    */
   public ConcurrentContainerMT(ExoContainer holder, ExoContainer parent)
   {
      setParent(parent);
      setHolder(holder);
   }

   protected ComponentAdapterFactory getDefaultComponentAdapterFactory()
   {
      return new ManageableComponentAdapterFactoryMT(holder, this);
   }

   public ComponentAdapter getComponentAdapter(Object componentKey) throws ContainerException
   {
      ComponentAdapter adapter = getComponentAdapterInternal(componentKey);
      if (adapter instanceof WrapperComponentAdapterStateAware)
      {
         adapter = ((WrapperComponentAdapterStateAware)adapter).getNestedComponentAdapter();
      }
      return adapter;
   }

   protected final ComponentAdapterTaskContextAware getComponentAdapterInternal(Object componentKey)
      throws ContainerException
   {
      ComponentAdapterTaskContextAware adapter =
         (ComponentAdapterTaskContextAware)componentKeyToAdapterCache.get(componentKey);
      if (adapter == null && parent != null)
      {
         adapter = getParent().getComponentAdapterInternal(componentKey);
      }
      return adapter;
   }

   public ComponentAdapter getComponentAdapterOfType(Class<?> componentType)
   {
      ComponentAdapter adapter = getComponentAdapterOfTypeInternal(componentType);
      if (adapter instanceof WrapperComponentAdapterStateAware)
      {
         adapter = ((WrapperComponentAdapterStateAware)adapter).getNestedComponentAdapter();
      }
      return adapter;
   }

   protected ComponentAdapterTaskContextAware getComponentAdapterOfTypeInternal(Class<?> componentType)
   {
      ComponentAdapterTaskContextAware adapterByKey = getComponentAdapterInternal(componentType);
      if (adapterByKey != null)
      {
         return adapterByKey;
      }

      List<ComponentAdapter> found = getComponentAdaptersOfTypeInternal(componentType);

      if (found.size() == 1)
      {
         return (ComponentAdapterTaskContextAware)found.get(0);
      }
      else if (found.size() == 0)
      {
         if (parent != null)
         {
            return getParent().getComponentAdapterOfTypeInternal(componentType);
         }
         else
         {
            return null;
         }
      }
      else
      {
         Class<?>[] foundClasses = new Class<?>[found.size()];
         for (int i = 0; i < foundClasses.length; i++)
         {
            ComponentAdapter componentAdapter = (ComponentAdapter)found.get(i);
            foundClasses[i] = componentAdapter.getComponentImplementation();
         }
         throw new ContainerException("Several ComponentAdapter found for " + componentType);
      }
   }

   public List<ComponentAdapter> getComponentAdaptersOfType(Class<?> componentType)
   {
      return getComponentAdaptersOfType(componentType, true);
   }

   protected List<ComponentAdapter> getComponentAdaptersOfTypeInternal(Class<?> componentType)
   {
      return getComponentAdaptersOfType(componentType, false);
   }

   private List<ComponentAdapter> getComponentAdaptersOfType(Class<?> componentType, boolean withNativeAdapters)
   {
      if (componentType == null)
      {
         return Collections.emptyList();
      }
      List<ComponentAdapter> found = new ArrayList<ComponentAdapter>();
      for (Iterator<ComponentAdapter> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapterTaskContextAware componentAdapter = (ComponentAdapterTaskContextAware)iterator.next();

         if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation()))
         {
            if (withNativeAdapters && componentAdapter instanceof WrapperComponentAdapterStateAware)
            {
               found.add(((WrapperComponentAdapterStateAware)componentAdapter).getNestedComponentAdapter());
            }
            else
            {
               found.add(componentAdapter);
            }
         }
      }
      return found;
   }

   /**
    * {@inheritDoc} 
    */
   protected ComponentAdapter registerComponent(ComponentAdapter componentAdapter) throws ContainerException
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_COMPONENT_PERMISSION);

      Object componentKey = componentAdapter.getComponentKey();
      ComponentAdapterTaskContextAware componentAdapterTaskContextAware;
      if (componentAdapter instanceof ComponentAdapterTaskContextAware)
      {
         componentAdapterTaskContextAware = (ComponentAdapterTaskContextAware)componentAdapter;
      }
      else
      {
         componentAdapterTaskContextAware = new WrapperComponentAdapterStateAware(this, componentAdapter);
      }
      if (componentKeyToAdapterCache.putIfAbsent(componentKey, componentAdapterTaskContextAware) != null)
      {
         throw new ContainerException("Key " + componentKey + " duplicated");
      }
      componentAdapters.add(componentAdapterTaskContextAware);
      return componentAdapter;
   }

   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      ComponentAdapter adapter = super.unregisterComponent(componentKey);
      if (adapter instanceof WrapperComponentAdapterStateAware)
      {
         adapter = ((WrapperComponentAdapterStateAware)adapter).getNestedComponentAdapter();
      }
      return adapter;
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be an {@link InstanceComponentAdapterStateAware}.
    */
   public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
      throws ContainerException
   {
      if (componentInstance instanceof ExoContainer)
      {
         ExoContainer pc = (ExoContainer)componentInstance;
         Object contrivedKey = new Object();
         String contrivedComp = "";
         pc.registerComponentInstance(contrivedKey, contrivedComp);
         try
         {
            if (this.getComponentInstance(contrivedKey) != null)
            {
               throw new ContainerException(
                  "Cannot register a container to itself. The container is already implicitly registered.");
            }
         }
         finally
         {
            pc.unregisterComponent(contrivedKey);
         }
         children.add(pc);
      }
      ComponentAdapter componentAdapter = new InstanceComponentAdapterStateAware(componentKey, componentInstance);
      registerComponent(componentAdapter);
      return componentAdapter;
   }

   private List<Object> getComponentsOfType(Class<?> componentType, boolean instance) throws ContainerException
   {
      if (componentType == null)
      {
         return Collections.emptyList();
      }

      Map<ComponentAdapterTaskContextAware, Object> adapterToInstanceMap =
         new HashMap<ComponentAdapterTaskContextAware, Object>();
      for (Iterator<ComponentAdapter> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapterTaskContextAware componentAdapter = (ComponentAdapterTaskContextAware)iterator.next();
         if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation()))
         {
            Object componentInstance = getInstance(componentAdapter, null);
            adapterToInstanceMap.put(componentAdapter, componentInstance);

            // This is to ensure all are added. (Indirect dependencies will be added
            // from InstantiatingComponentAdapter).
            addOrderedComponentAdapter(componentAdapter);
         }
      }
      List<Object> result = new ArrayList<Object>();
      for (Iterator<ComponentAdapter> iterator = orderedComponentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapterTaskContextAware componentAdapter = (ComponentAdapterTaskContextAware)iterator.next();
         final Object componentInstance = adapterToInstanceMap.get(componentAdapter);
         if (componentInstance != null)
         {
            // may be null in the case of the "implicit" adapter
            // representing "this".
            result.add(instance ? componentInstance : componentAdapter);
         }
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   public <T> List<T> getComponentInstancesOfType(Class<T> componentType) throws ContainerException
   {
      return (List<T>)getComponentsOfType(componentType, true);
   }

   protected List<ComponentAdapterTaskContextAware> getComponentAdapterTaskContextAwaresOfType(Class<?> componentType)
      throws ContainerException
   {
      List<ComponentAdapterTaskContextAware> result = new ArrayList<ComponentAdapterTaskContextAware>();
      for (Iterator<Object> iterator = getComponentsOfType(componentType, false).iterator(); iterator.hasNext();)
      {
         Object componentAdapter = iterator.next();
         result.add((ComponentAdapterTaskContextAware)componentAdapter);
      }
      return result;
   }

   public Object getComponentInstance(Object componentKey) throws ContainerException
   {
      return getComponentInstance(componentKey, null);
   }

   protected Object getComponentInstance(Object componentKey, ComponentTaskContext ctx) throws ContainerException
   {
      ComponentAdapterTaskContextAware componentAdapter = getComponentAdapterInternal(componentKey);
      if (componentAdapter != null)
      {
         return getInstance(componentAdapter, ctx);
      }
      else
      {
         return null;
      }
   }

   public <T> T getComponentInstanceOfType(Class<T> componentType)
   {
      Deque<DependencyStack> stacks = dependencyStacks != null ? dependencyStacks.get() : null;
      DependencyStack stack = null;
      T instance;
      try
      {
         if (stacks != null)
         {
            stack = stacks.getLast();
            stack.add(componentType);
         }
         instance = getComponentInstanceOfType(componentType, null);
      }
      finally
      {
         if (stack != null && !stack.isEmpty())
         {
            stack.removeLast();
         }
      }
      return instance;
   }

   protected <T> T getComponentInstanceOfType(Class<T> componentType, ComponentTaskContext ctx)
   {
      final ComponentAdapterTaskContextAware componentAdapter = getComponentAdapterOfTypeInternal(componentType);
      if (componentAdapter == null)
         return null;
      return componentType.cast(getInstance(componentAdapter, ctx));
   }

   private Object getInstance(ComponentAdapterTaskContextAware componentAdapter, ComponentTaskContext ctx)
   {
      if (ctx == null)
      {
         ctx = currentCtx.get();
      }
      if (ctx != null)
      {
         Object result = ctx.getComponentFromContext(componentAdapter.getComponentKey());
         if (result != null)
         {
            return result;
         }
      }
      // check whether this is our adapter
      // we need to check this to ensure up-down dependencies cannot be followed
      final boolean isLocal = componentAdapters.contains(componentAdapter);

      if (isLocal)
      {
         Object instance = componentAdapter.getComponentInstance(ctx);
         addOrderedComponentAdapter(componentAdapter);

         return instance;
      }
      else if (parent != null)
      {
         return getParent().getComponentInstance(componentAdapter.getComponentKey(), ctx);
      }

      return null;
   }

   private ConcurrentContainerMT getParent()
   {
      Container co = parent;
      do
      {
         if (co instanceof ConcurrentContainerMT)
         {
            return (ConcurrentContainerMT)co;
         }
      }
      while ((co = co.getSuccessor()) != null);
      return null;
   }

   /**
    * Start the components of this Container and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Startable} will be started.
    */
   public void start()
   {
      LifecycleVisitorMT.start(this);
   }

   /**
    * Stop the components of this Container and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Startable} will be stopped.
    */
   public void stop()
   {
      LifecycleVisitorMT.stop(this);
   }

   /**
    * Dispose the components of this Container and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Disposable} will be disposed.
    */
   public void dispose()
   {
      LifecycleVisitorMT.dispose(this);
   }

   @SuppressWarnings("unchecked")
   public <T> Constructor<T> getConstructor(Class<T> clazz) throws Exception
   {
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
         boolean satisfied = true;
         for (int i = 0; i < parameters.length; i++)
         {
            Class<?> parameter = parameters[i];
            if (!parameter.equals(InitParams.class) && holder.getComponentAdapterOfType(parameter) == null)
            {
               satisfied = false;
               unknownParameter = parameter;
               break;
            }
         }
         if (satisfied)
         {
            return (Constructor<T>)constructor;
         }
      }
      throw new Exception("Cannot find a satisfying constructor for " + clazz + " with parameter " + unknownParameter);
   }

   public <T> T createComponent(Class<T> clazz) throws Exception
   {
      return createComponent(clazz, null);
   }

   public <T> T createComponent(Class<T> clazz, InitParams params) throws Exception
   {
      Constructor<T> constructor = getConstructor(clazz);
      final Object[] args =
         getArguments(constructor, params, new ComponentTaskContext(clazz, ComponentTaskType.CREATE),
            ComponentTaskType.CREATE);
      return constructor.getDeclaringClass().cast(constructor.newInstance(args));
   }

   public <T> ComponentTask<T> createComponentTask(final Constructor<T> constructor, InitParams params,
      final ComponentTaskContext ctx, DependencyStackListener caller) throws Exception
   {
      final Object[] args = getArguments(constructor, params, ctx, ComponentTaskType.CREATE);
      return new ComponentTask<T>(this, ctx, caller, ComponentTaskType.CREATE)
      {
         public T execute() throws Exception
         {
            return constructor.getDeclaringClass().cast(constructor.newInstance(args));
         }
      };
   }

   protected <T> T execute(ComponentTask<T> task) throws Exception
   {
      ComponentTaskContext previousCtx = currentCtx.get();
      Deque<DependencyStack> stacks = null;
      try
      {
         if (dependencyStacks != null)
         {
            stacks = dependencyStacks.get();
            if (stacks == null)
            {
               stacks = new LinkedList<DependencyStack>();
               dependencyStacks.set(stacks);
            }
            DependencyStack stack = new DependencyStack(task);
            stacks.add(stack);
         }
         currentCtx.set(task.getContext().toDisableMultiThreading());
         return task.execute();
      }
      catch (InvocationTargetException e)
      {
         if (e.getCause() instanceof Exception)
         {
            throw (Exception)e.getCause();
         }
         throw e;
      }
      finally
      {
         currentCtx.set(previousCtx);
         if (dependencyStacks != null)
         {
            stacks.removeLast();
            if (stacks.isEmpty())
            {
               dependencyStacks.set(null);
            }
         }
      }
   }

   public <T> Object[] getArguments(Constructor<T> constructor, InitParams params, final ComponentTaskContext ctx,
      final ComponentTaskType type) throws Exception
   {
      return getArguments(null, constructor, params, ctx, type);
   }

   @SuppressWarnings("unchecked")
   public <T> Object[] getArguments(ComponentAdapterStateAware caller, Constructor<T> constructor, InitParams params,
      final ComponentTaskContext ctx, final ComponentTaskType type) throws Exception
   {
      Class<?>[] parameters = constructor.getParameterTypes();
      Object[] args = new Object[parameters.length];
      if (args.length == 0)
         return args;
      boolean hasSubmittedTasks = false;
      boolean enableMultiThreading =
         Mode.hasMode(Mode.MULTI_THREADED)
            && !ctx.disableMultiThreading()
            && (parameters.length > 2 || (parameters.length == 2 && !parameters[0].equals(InitParams.class) && !parameters[1]
               .equals(InitParams.class)));
      for (int i = 0; i < parameters.length; i++)
      {
         final Class<?> parameter = parameters[i];
         if (parameter.equals(InitParams.class))
         {
            args[i] = params;
         }
         else if (enableMultiThreading)
         {
            final ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Callable<Object> task = new Callable<Object>()
            {
               public Object call() throws Exception
               {
                  return SecurityHelper.doPrivilegedAction(new PrivilegedAction<Object>()
                  {
                     public Object run()
                     {
                        ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
                        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                        ComponentTaskContext previousCtx = currentCtx.get();
                        try
                        {
                           ExoContainerContext.setCurrentContainer(container);
                           Thread.currentThread().setContextClassLoader(cl);
                           currentCtx.set(ctx.addToContext(parameter, type).toDisableMultiThreading());
                           return holder.getComponentInstanceOfType(parameter);
                        }
                        finally
                        {
                           Thread.currentThread().setContextClassLoader(oldCl);
                           ExoContainerContext.setCurrentContainer(oldContainer);
                           currentCtx.set(previousCtx);
                        }
                     }
                  });
               }
            };
            args[i] = getExecutor().submit(task);
            hasSubmittedTasks = true;
         }
         else
         {
            ComponentTaskContext previousCtx = currentCtx.get();
            try
            {
               currentCtx.set(ctx.addToContext(parameter, type));
               args[i] = holder.getComponentInstanceOfType(parameter);
            }
            finally
            {
               currentCtx.set(previousCtx);
            }
         }
      }
      if (hasSubmittedTasks)
      {
         for (int i = 0; i < args.length; i++)
         {
            Object o = args[i];
            if (o instanceof Future<?>)
            {
               try
               {
                  args[i] = ((Future<Object>)o).get();
               }
               catch (ExecutionException e)
               {
                  Throwable cause = e.getCause();
                  if (cause instanceof Exception)
                  {
                     throw (Exception)cause;
                  }
                  throw e;
               }
            }
         }
      }
      return args;
   }

   public void startComponents(Collection<Class<?>> components, ComponentTaskContext ctx) throws Exception
   {
      if (components == null || components.isEmpty())
         return;
      List<Future<?>> futureTasks = null;
      boolean enableMultiThreading =
         Mode.hasMode(Mode.MULTI_THREADED) && !ctx.disableMultiThreading() && components.size() > 1;
      CyclicDependencyException ex = null;
      for (Class<?> component : components)
      {
         final ComponentAdapterTaskContextAware adapter = getComponentAdapterOfTypeInternal(component);
         boolean isLocal = componentAdapters.contains(adapter);
         if (!isLocal)
         {
            // To prevent infinite loop we assume that component adapters of
            // parent container are already started so we skip them
            continue;
         }
         try
         {
            futureTasks = startAdapter(ctx, futureTasks, enableMultiThreading, adapter);
         }
         catch (CyclicDependencyException e)
         {
            ex = e;
         }
      }
      if (futureTasks != null)
      {
         for (Future<?> task : futureTasks)
         {
            try
            {
               task.get();
            }
            catch (CyclicDependencyException e)
            {
               ex = e;
            }
            catch (ExecutionException e)
            {
               Throwable cause = e.getCause();
               if (cause instanceof Exception)
               {
                  throw (Exception)cause;
               }
               throw e;
            }
         }
      }
      if (ex != null)
      {
         throw ex;
      }
   }

   private List<Future<?>> startAdapter(final ComponentTaskContext ctx, List<Future<?>> futureTasks,
      boolean enableMultiThreading, final ComponentAdapterTaskContextAware adapter)
   {
      if (enableMultiThreading)
      {
         final ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
         final ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Runnable task = new Runnable()
         {
            public void run()
            {
               SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
               {
                  public Void run()
                  {
                     ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
                     ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                     try
                     {
                        ExoContainerContext.setCurrentContainer(container);
                        Thread.currentThread().setContextClassLoader(cl);
                        adapter.start(ctx.addToContext(adapter.getComponentKey(), ComponentTaskType.START)
                           .toDisableMultiThreading());
                     }
                     finally
                     {
                        Thread.currentThread().setContextClassLoader(oldCl);
                        ExoContainerContext.setCurrentContainer(oldContainer);
                     }
                     return null;
                  }
               });
            }
         };
         if (futureTasks == null)
         {
            futureTasks = new LinkedList<Future<?>>();
         }
         futureTasks.add(getExecutor().submit(task));
      }
      else
      {
         adapter.start(ctx.addToContext(adapter.getComponentKey(), ComponentTaskType.START));
      }
      return futureTasks;
   }

   public void initialize(Collection<Class<?>> components, boolean disableMultiThreading)
   {
      if (components == null)
         return;
      List<Future<?>> futureTasks = null;
      boolean enableMultiThreading =
         Mode.hasMode(Mode.MULTI_THREADED) && components.size() > 1 && !disableMultiThreading;
      for (final Class<?> component : components)
      {
         if (enableMultiThreading)
         {
            final ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Runnable task = new Runnable()
            {
               public void run()
               {
                  SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
                  {
                     public Void run()
                     {
                        ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
                        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                        ComponentTaskContext previousCtx = currentCtx.get();
                        try
                        {
                           ExoContainerContext.setCurrentContainer(container);
                           Thread.currentThread().setContextClassLoader(cl);
                           currentCtx.set(new ComponentTaskContext(component, ComponentTaskType.CREATE)
                              .toDisableMultiThreading());
                           holder.getComponentInstanceOfType(component);
                        }
                        catch (CyclicDependencyException e)
                        {
                           // We ignore it as it means that the component is already planned to be started
                        }
                        finally
                        {
                           Thread.currentThread().setContextClassLoader(oldCl);
                           ExoContainerContext.setCurrentContainer(oldContainer);
                           currentCtx.set(previousCtx);
                        }
                        return null;
                     }
                  });
               }
            };
            if (futureTasks == null)
            {
               futureTasks = new LinkedList<Future<?>>();
            }
            futureTasks.add(getExecutor().submit(task));
         }
         else
         {
            ComponentTaskContext previousCtx = currentCtx.get();
            try
            {
               currentCtx.set(new ComponentTaskContext(component, ComponentTaskType.CREATE));
               holder.getComponentInstanceOfType(component);
            }
            finally
            {
               currentCtx.set(previousCtx);
            }
         }
      }
      if (futureTasks != null)
      {
         for (Future<?> task : futureTasks)
         {
            try
            {
               task.get();
            }
            catch (ExecutionException e)
            {
               throw new RuntimeException(e.getCause());
            }
            catch (InterruptedException e)
            {
               throw new RuntimeException(e);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getId()
   {
      return "ConcurrentContainer";
   }

   private static class KernelThreadFactory implements ThreadFactory
   {
      final ThreadGroup group;

      final AtomicInteger threadNumber = new AtomicInteger(1);

      final String namePrefix;

      KernelThreadFactory()
      {
         SecurityManager s = System.getSecurityManager();
         group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
         namePrefix = "kernel-thread-";
      }

      /**
       * {@inheritDoc}
       */
      public Thread newThread(Runnable r)
      {
         Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
         if (t.isDaemon())
            t.setDaemon(false);
         if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
         return t;
      }
   }
}
