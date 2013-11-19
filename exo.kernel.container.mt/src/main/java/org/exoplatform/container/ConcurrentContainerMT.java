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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.management.ManageableComponentAdapterFactoryMT;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ComponentAdapterFactory;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ConcurrentContainerMT extends ConcurrentContainer implements TopExoContainerListener
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -1059330085804288350L;

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.ConcurrentContainerMT");

   private static volatile transient ThreadPoolExecutor EXECUTOR;

   private final transient ThreadLocal<ComponentTaskContext> currentCtx = new ThreadLocal<ComponentTaskContext>();

   /**
    * Needed to fix the deadlocks
    */
   private final transient ConcurrentMap<Object, CreationalContextComponentAdapter<?>> sharedMemory =
      new ConcurrentHashMap<Object, CreationalContextComponentAdapter<?>>();

   /**
    * The name of the system parameter to indicate the total amount of threads to use for the kernel
    */
   public static final String THREAD_POOL_SIZE_PARAM_NAME = "org.exoplatform.container.mt.tps";

   private static ThreadPoolExecutor getExecutor()
   {
      if (EXECUTOR == null && Mode.hasMode(Mode.MULTI_THREADED))
      {
         synchronized (ConcurrentContainerMT.class)
         {
            if (EXECUTOR == null)
            {
               String sValue = PropertyManager.getProperty(THREAD_POOL_SIZE_PARAM_NAME);
               int threadPoolSize;
               if (sValue != null)
               {
                  LOG.debug("A value for the thread pool size has been found, it has been set to '" + sValue + "'");
                  threadPoolSize = Integer.parseInt(sValue);
               }
               else
               {
                  threadPoolSize = Math.min(2 * Runtime.getRuntime().availableProcessors(), 30);
               }
               LOG.debug("The size of the thread pool used by the kernel has been set to " + threadPoolSize);
               EXECUTOR = new KernelThreadPoolExecutor(threadPoolSize);
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

   /**
    * {@inheritDoc}
    */
   @Override
   public void initialize()
   {
      if (holder instanceof TopExoContainer)
      {
         ((TopExoContainer)holder).addListener(this);
      }
   }

   @Override
   protected ComponentAdapterFactory getDefaultComponentAdapterFactory()
   {
      return new ManageableComponentAdapterFactoryMT(holder, this);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   protected <T> T getComponentInstanceFromContext(ComponentAdapter<T> componentAdapter, Class<T> bindType)
   {
      ComponentTaskContext ctx = currentCtx.get();
      if (ctx != null)
      {
         T result = ctx.getComponentInstanceFromContext(componentAdapter.getComponentKey(), bindType);
         if (result != null)
         {
            // Don't keep in cache a component that has not been created yet
            getCache().disable();
            return result;
         }
      }
      return null;
   }

   /**
    * Gives a value from the shared memory 
    */
   @SuppressWarnings("unchecked")
   public <T> T getComponentFromSharedMemory(Object key)
   {
      CreationalContextComponentAdapter<?> ccca = sharedMemory.get(key);
      return ccca == null ? null : (T)ccca.get();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> CreationalContextComponentAdapter<T> addComponentToCtx(Object key)
   {
      ComponentTaskContext ctx = currentCtx.get();
      CreationalContextComponentAdapter<T> ccca = new CreationalContextComponentAdapter<T>();
      sharedMemory.put(key, ccca);
      return ctx.addComponentToContext(key, ccca);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void removeComponentFromCtx(Object key)
   {
      ComponentTaskContext ctx = currentCtx.get();
      CreationalContextComponentAdapter<?> ccca = ctx.removeComponentFromContext(key);
      sharedMemory.remove(key, ccca);
   }

   /**
    * A multi-threaded implementation of the start method
    */
   public <T> List<T> getComponentInstancesOfType(final Class<T> componentType) throws ContainerException
   {
      if (componentType == null)
      {
         return Collections.emptyList();
      }
      List<ComponentAdapter<T>> adapters = getComponentAdaptersOfType(componentType);
      if (adapters == null || adapters.isEmpty())
         return Collections.emptyList();
      boolean enableMultiThreading = Mode.hasMode(Mode.MULTI_THREADED) && adapters.size() > 1;
      List<Future<?>> submittedTasks = null;
      final Map<ComponentAdapter<T>, Object> adapterToInstanceMap =
         enableMultiThreading ? new ConcurrentHashMap<ComponentAdapter<T>, Object>()
            : new HashMap<ComponentAdapter<T>, Object>();
      ThreadPoolExecutor executor = enableMultiThreading ? getExecutor() : null;
      if (enableMultiThreading && executor == null)
      {
         enableMultiThreading = false;
      }
      for (final ComponentAdapter<T> adapter : adapters)
      {
         if (enableMultiThreading && LockManager.getInstance().getTotalUncompletedTasks() < executor.getCorePoolSize()
            && !(adapter instanceof InstanceComponentAdapter))
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
                           Object o = getInstance(adapter, componentType, false);
                           if (o != null)
                              adapterToInstanceMap.put(adapter, o);
                           // This is to ensure all are added. (Indirect dependencies will be added
                           // from InstantiatingComponentAdapter).
                           addOrderedComponentAdapter(adapter);
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
            if (submittedTasks == null)
            {
               submittedTasks = new ArrayList<Future<?>>();
            }
            submittedTasks.add(executor.submit(task));
         }
         else if (enableMultiThreading)
         {
            Object o = getInstance(adapter, componentType, false);
            if (o != null)
               adapterToInstanceMap.put(adapter, o);
            // This is to ensure all are added. (Indirect dependencies will be added
            // from InstantiatingComponentAdapter).
            addOrderedComponentAdapter(adapter);
         }
         else
         {
            adapterToInstanceMap.put(adapter, getInstance(adapter, componentType, false));
            // This is to ensure all are added. (Indirect dependencies will be added
            // from InstantiatingComponentAdapter).
            addOrderedComponentAdapter(adapter);
         }
      }
      if (submittedTasks != null)
      {
         for (int i = 0, length = submittedTasks.size(); i < length; i++)
         {
            Future<?> task = submittedTasks.get(i);
            try
            {
               task.get();
            }
            catch (ExecutionException e)
            {
               Throwable cause = e.getCause();
               if (cause instanceof RuntimeException)
               {
                  throw (RuntimeException)cause;
               }
               throw new RuntimeException(cause);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }
      }
      List<T> result = new ArrayList<T>();
      for (Iterator<ComponentAdapter<?>> iterator = orderedComponentAdapters.iterator(); iterator.hasNext();)
      {
         Object componentAdapter = iterator.next();
         final Object componentInstance = adapterToInstanceMap.get(componentAdapter);
         if (componentInstance != null)
         {
            // may be null in the case of the "implicit" adapter
            // representing "this".
            result.add(componentType.cast(componentInstance));
         }
      }
      return result;
   }

   /**
    * A multi-threaded implementation of the start method
    */
   @Override
   public void start()
   {
      // First we get the context manager to prevent deadlock
      holder.getContextManager();
      // Then, create and initialize the components
      getComponentInstancesOfType(Startable.class);
      Object startables = getComponentAdaptersOfType(Startable.class);
      @SuppressWarnings("unchecked")
      List<ComponentAdapter<?>> adapters = (List<ComponentAdapter<?>>)startables;
      final Map<ComponentAdapter<?>, Object> alreadyStarted = new ConcurrentHashMap<ComponentAdapter<?>, Object>();
      final AtomicReference<Exception> error = new AtomicReference<Exception>();
      // We first start all the non containers
      start(adapters, alreadyStarted, new HashSet<ComponentAdapter<?>>(), error, true);
      if (error.get() != null)
      {
         throw new RuntimeException("Could not start the container", error.get());
      }
      // Then we start the sub containers
      for (Iterator<ExoContainer> iterator = children.iterator(); iterator.hasNext();)
      {
         ExoContainer child = iterator.next();
         child.start();
      }
   }

   /**
    * Starts all the provided adapters
    */
   protected void start(Collection<ComponentAdapter<?>> adapters,
      final Map<ComponentAdapter<?>, Object> alreadyStarted, final Set<ComponentAdapter<?>> startInProgress,
      final AtomicReference<Exception> error, final boolean skippable)
   {
      if (adapters == null || adapters.isEmpty())
         return;
      boolean enableMultiThreading = Mode.hasMode(Mode.MULTI_THREADED) && adapters.size() > 1;
      List<Future<?>> submittedTasks = null;
      ThreadPoolExecutor executor = enableMultiThreading ? getExecutor() : null;
      if (enableMultiThreading && executor == null)
      {
         enableMultiThreading = false;
      }
      for (final ComponentAdapter<?> adapter : adapters)
      {
         if (error.get() != null)
            break;
         if (ExoContainer.class.isAssignableFrom(adapter.getComponentImplementation()))
         {
            // Only non containers are expected and it is a container
            continue;
         }
         else if (alreadyStarted.containsKey(adapter) || (skippable && startInProgress.contains(adapter)))
         {
            // The component has already been started or is in progress
            continue;
         }
         if (enableMultiThreading && LockManager.getInstance().getTotalUncompletedTasks() < executor.getCorePoolSize()
            && !(adapter instanceof InstanceComponentAdapter))
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
                        if (error.get() != null)
                        {
                           return null;
                        }
                        else if (alreadyStarted.containsKey(adapter)
                           || (skippable && startInProgress.contains(adapter)))
                        {
                           // The component has already been started or is in progress
                           return null;
                        }
                        ExoContainer oldContainer = ExoContainerContext.getCurrentContainerIfPresent();
                        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
                        try
                        {
                           ExoContainerContext.setCurrentContainer(container);
                           Thread.currentThread().setContextClassLoader(cl);
                           if (adapter instanceof ComponentAdapterDependenciesAware)
                           {
                              ComponentAdapterDependenciesAware<?> cada = (ComponentAdapterDependenciesAware<?>)adapter;
                              startDependencies(alreadyStarted, startInProgress, error, cada);
                           }
                           if (!Startable.class.isAssignableFrom(adapter.getComponentImplementation()))
                           {
                              alreadyStarted.put(adapter, adapter);
                              return null;
                           }
                           else if (alreadyStarted.containsKey(adapter))
                           {
                              // The component has already been started
                              return null;
                           }
                           synchronized (adapter)
                           {
                              if (alreadyStarted.containsKey(adapter))
                              {
                                 // The component has already been started
                                 return null;
                              }
                              try
                              {
                                 Startable startable = (Startable)adapter.getComponentInstance();
                                 startable.start();
                              }
                              finally
                              {
                                 alreadyStarted.put(adapter, adapter);
                              }
                           }
                        }
                        catch (Exception e)
                        {
                           error.compareAndSet(null, e);
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
            if (submittedTasks == null)
            {
               submittedTasks = new ArrayList<Future<?>>();
            }
            submittedTasks.add(executor.submit(task));
         }
         else
         {
            if (adapter instanceof ComponentAdapterDependenciesAware)
            {
               ComponentAdapterDependenciesAware<?> cada = (ComponentAdapterDependenciesAware<?>)adapter;
               startDependencies(alreadyStarted, startInProgress, error, cada);
            }
            if (!Startable.class.isAssignableFrom(adapter.getComponentImplementation()))
            {
               alreadyStarted.put(adapter, adapter);
               continue;
            }
            else if (alreadyStarted.containsKey(adapter))
            {
               // The component has already been started
               continue;
            }
            synchronized (adapter)
            {
               if (alreadyStarted.containsKey(adapter))
               {
                  // The component has already been started
                  continue;
               }
               try
               {
                  Startable startable = (Startable)adapter.getComponentInstance();
                  startable.start();
               }
               catch (Exception e)
               {
                  error.compareAndSet(null, e);
               }
               finally
               {
                  alreadyStarted.put(adapter, adapter);
               }
            }
         }
      }
      if (submittedTasks != null)
      {
         for (int i = 0, length = submittedTasks.size(); i < length; i++)
         {
            Future<?> task = submittedTasks.get(i);
            try
            {
               task.get();
            }
            catch (ExecutionException e)
            {
               Throwable cause = e.getCause();
               if (cause instanceof RuntimeException)
               {
                  throw (RuntimeException)cause;
               }
               throw new RuntimeException(cause);
            }
            catch (InterruptedException e)
            {
               Thread.currentThread().interrupt();
            }
         }
      }
   }

   private Collection<ComponentAdapter<?>> getDependencies(Collection<Dependency> dependencies, boolean withLazy,
      boolean withNonLazy)
   {
      if (dependencies == null || dependencies.isEmpty())
         return null;
      Collection<ComponentAdapter<?>> result = new LinkedHashSet<ComponentAdapter<?>>();
      for (Dependency dep : dependencies)
      {
         if ((dep.isLazy() && !withLazy) || (!dep.isLazy() && !withNonLazy))
            continue;
         ComponentAdapter<?> adapter = dep.getAdapter(holder);
         boolean isLocal = componentAdapters.contains(adapter);
         if (!isLocal)
         {
            // To prevent infinite loop we assume that component adapters of
            // parent container are already started so we skip them
            continue;
         }
         result.add(adapter);
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   public <T> Constructor<T> getConstructor(Class<T> clazz, List<Dependency> dependencies) throws Exception
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
         Object[] args = new Object[parameters.length];
         boolean constructorWithInject = constructors.length == 1 && constructor.isAnnotationPresent(Inject.class);
         boolean satisfied = true;
         String logMessagePrefix = null;
         Type[] genericTypes = null;
         Annotation[][] parameterAnnotations = null;
         if (constructorWithInject)
         {
            genericTypes = constructor.getGenericParameterTypes();
            parameterAnnotations = constructor.getParameterAnnotations();
         }
         if (LOG.isDebugEnabled() && constructorWithInject)
         {
            logMessagePrefix = "Could not call the constructor of the class " + clazz.getName();
         }
         for (int i = 0; i < args.length; i++)
         {
            if (!parameters[i].equals(InitParams.class))
            {
               if (constructorWithInject)
               {
                  Object result =
                     resolveType(parameters[i], genericTypes[i], parameterAnnotations[i], logMessagePrefix,
                        dependencies);
                  if (!(result instanceof Integer))
                  {
                     args[i] = result;
                  }
               }
               else
               {
                  final Class<?> componentType = parameters[i];
                  args[i] = holder.getComponentAdapterOfType(componentType);
                  dependencies.add(new DependencyByType(componentType));
               }
               if (args[i] == null)
               {
                  satisfied = false;
                  unknownParameter = parameters[i];
                  dependencies.clear();
                  break;
               }
            }
         }
         if (satisfied)
         {
            if ((!Modifier.isPublic(constructor.getModifiers()) || !Modifier.isPublic(constructor.getDeclaringClass()
               .getModifiers())) && !constructor.isAccessible())
               constructor.setAccessible(true);
            return (Constructor<T>)constructor;
         }
      }
      throw new Exception("Cannot find a satisfying constructor for " + clazz + " with parameter " + unknownParameter);
   }

   /**
    * Initializes the instance by injecting objects into fields and the methods with the
    * annotation {@link Inject}
    * @return <code>true</code> if at least Inject annotation has been found, <code>false</code> otherwise
    */
   public <T> boolean initializeComponent(Class<T> targetClass, List<Dependency> dependencies,
      List<ComponentTask<Void>> componentInitTasks, DependencyStackListener caller)
   {
      LinkedList<Class<?>> hierarchy = new LinkedList<Class<?>>();
      Class<?> clazz = targetClass;
      do
      {
         hierarchy.addFirst(clazz);
      }
      while (!(clazz = clazz.getSuperclass()).equals(Object.class));
      // Fields and methods in superclasses are injected before those in subclasses. 
      Map<String, Method> methodAlreadyRegistered = new HashMap<String, Method>();
      Map<Class<?>, Collection<Method>> methodsPerClass = new HashMap<Class<?>, Collection<Method>>();
      for (Class<?> c : hierarchy)
      {
         addMethods(c, methodAlreadyRegistered, methodsPerClass);
      }
      boolean isInjectPresent = !methodAlreadyRegistered.isEmpty();
      for (Class<?> c : hierarchy)
      {
         if (initializeFields(targetClass, c, dependencies, componentInitTasks, caller))
         {
            isInjectPresent = true;
         }
         initializeMethods(targetClass, methodsPerClass.get(c), dependencies, componentInitTasks, caller);
      }
      return isInjectPresent;
   }

   /**
    * Initializes the instance by calling all the methods with the
    * annotation {@link Inject}
    */
   private <T> void initializeMethods(final Class<T> targetClass, Collection<Method> methods,
      List<Dependency> dependencies, List<ComponentTask<Void>> componentInitTasks, DependencyStackListener caller)
   {
      if (methods == null)
      {
         return;
      }
      main : for (final Method m : methods)
      {
         if (m.isAnnotationPresent(Inject.class))
         {
            if (Modifier.isAbstract(m.getModifiers()))
            {
               LOG.warn("Could not call the method " + m.getName() + " of the class " + targetClass.getName()
                  + ": The method cannot be abstract");
               continue;
            }
            else if (Modifier.isStatic(m.getModifiers()))
            {
               LOG.warn("Could not call the method " + m.getName() + " of the class " + targetClass.getName()
                  + ": The method cannot be static");
               continue;
            }
            // The method is annotated with Inject and is not abstract and has not been called yet
            Class<?>[] paramTypes = m.getParameterTypes();
            final Object[] params = new Object[paramTypes.length];
            Type[] genericTypes = m.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = m.getParameterAnnotations();
            String logMessagePrefix = null;
            if (LOG.isDebugEnabled())
            {
               logMessagePrefix = "Could not call the method " + m.getName() + " of the class " + targetClass.getName();
            }
            for (int j = 0, l = paramTypes.length; j < l; j++)
            {
               Object result =
                  resolveType(paramTypes[j], genericTypes[j], parameterAnnotations[j], logMessagePrefix, dependencies);
               if (result instanceof Integer)
               {
                  int r = (Integer)result;
                  if (r == 1 || r == 2)
                  {
                     continue main;
                  }
                  params[j] = null;
                  continue;
               }
               else
               {
                  params[j] = dependencies.get(dependencies.size() - 1);
               }
            }
            try
            {
               if ((!Modifier.isPublic(m.getModifiers()) || !Modifier.isPublic(m.getDeclaringClass().getModifiers()))
                  && !m.isAccessible())
                  m.setAccessible(true);
               componentInitTasks.add(new ComponentTask<Void>("Call the method " + m.getName() + " of the class "
                  + targetClass.getName(), this, caller, ComponentTaskType.INIT)
               {
                  public Void execute(CreationalContextComponentAdapter<?> cCtx) throws Exception
                  {
                     try
                     {
                        loadArguments(params);
                        m.invoke(cCtx.get(), params);
                     }
                     catch (Exception e)
                     {
                        throw new RuntimeException("Could not call the method " + m.getName() + " of the class "
                           + targetClass.getName() + ": " + e.getMessage(), e);
                     }
                     return null;
                  }
               });
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not call the method " + m.getName() + " of the class "
                  + targetClass.getName() + ": " + e.getMessage(), e);
            }
         }
      }
   }

   /**
    * Initializes the fields of the instance by injecting objects into fields with the
    * annotation {@link Inject} for a given class
    */
   private <T> boolean initializeFields(final Class<T> targetClass, Class<?> clazz, List<Dependency> dependencies,
      List<ComponentTask<Void>> componentInitTasks, DependencyStackListener caller)
   {
      boolean isInjectPresent = false;
      Field[] fields = clazz.getDeclaredFields();
      for (int i = 0, length = fields.length; i < length; i++)
      {
         final Field f = fields[i];
         if (f.isAnnotationPresent(Inject.class))
         {
            isInjectPresent = true;
            if (Modifier.isFinal(f.getModifiers()))
            {
               LOG.warn("Could not set a value to the field " + f.getName() + " of the class " + targetClass.getName()
                  + ": The field cannot be final");
               continue;
            }
            else if (Modifier.isStatic(f.getModifiers()))
            {
               LOG.warn("Could not set a value to the field " + f.getName() + " of the class " + targetClass.getName()
                  + ": The field cannot be static");
               continue;
            }
            // The field is annotated with Inject and is not final and/or static
            try
            {
               if ((!Modifier.isPublic(f.getModifiers()) || !Modifier.isPublic(f.getDeclaringClass().getModifiers()))
                  && !f.isAccessible())
                  f.setAccessible(true);
               String logMessagePrefix = null;
               if (LOG.isDebugEnabled())
               {
                  logMessagePrefix =
                     "Could not set a value to the field " + f.getName() + " of the class " + targetClass.getName();
               }
               Object result =
                  resolveType(f.getType(), f.getGenericType(), f.getAnnotations(), logMessagePrefix, dependencies);
               if (result instanceof Integer)
               {
                  continue;
               }
               final Dependency dependency = dependencies.get(dependencies.size() - 1);
               componentInitTasks.add(new ComponentTask<Void>("Set a value to the field " + f.getName()
                  + " of the class " + targetClass.getName(), this, caller, ComponentTaskType.INIT)
               {
                  public Void execute(CreationalContextComponentAdapter<?> cCtx) throws Exception
                  {
                     try
                     {
                        f.set(cCtx.get(), dependency.load(holder));
                     }
                     catch (Exception e)
                     {
                        throw new RuntimeException("Could not set a value to the field " + f.getName()
                           + " of the class " + targetClass.getName() + ": " + e.getMessage(), e);
                     }
                     return null;
                  }
               });
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not set a value to the field " + f.getName() + " of the class "
                  + targetClass.getName() + ": " + e.getMessage(), e);
            }
         }
      }
      return isInjectPresent;
   }

   /**
    * Resolves the given type and generic type
    */
   private Object resolveType(final Class<?> type, Type genericType, Annotation[] annotations, String logMessagePrefix,
      List<Dependency> dependencies)
   {
      if (type.isPrimitive())
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug(logMessagePrefix + ": Primitive types are not supported");
         }
         return 1;
      }
      Named named = null;
      Class<?> qualifier = null;
      for (int i = 0, length = annotations.length; i < length; i++)
      {
         Annotation a = annotations[i];
         if (a instanceof Named)
         {
            named = (Named)a;
            break;
         }
         else if (a.annotationType().isAnnotationPresent(Qualifier.class))
         {
            qualifier = a.annotationType();
            break;
         }
      }
      if (type.isInterface() && type.equals(Provider.class))
      {
         if (!(genericType instanceof ParameterizedType))
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug(logMessagePrefix + ": The generic type is not of type ParameterizedType");
            }
            return 2;
         }
         ParameterizedType aType = (ParameterizedType)genericType;
         Type[] typeVars = aType.getActualTypeArguments();
         Class<?> expectedType = (Class<?>)typeVars[0];
         final ComponentAdapter<?> adapter;
         final Object key;
         if (named != null)
         {
            adapter = holder.getComponentAdapter(key = named.value(), expectedType);
         }
         else if (qualifier != null)
         {
            adapter = holder.getComponentAdapter(key = qualifier, expectedType);
         }
         else
         {
            key = expectedType;
            adapter = holder.getComponentAdapterOfType(expectedType);
         }

         if (adapter == null)
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug(logMessagePrefix + ": We have no value to set so we skip it");
            }
            return 3;
         }
         final Provider<Object> result = new Provider<Object>()
         {
            public Object get()
            {
               return adapter.getComponentInstance();
            }
         };
         dependencies.add(new DependencyByProvider(key, expectedType, result, adapter));
         return result;
      }
      else
      {
         if (named != null)
         {
            final String name = named.value();
            dependencies.add(new DependencyByName(name, type));
            return holder.getComponentAdapter(name, type);
         }
         else if (qualifier != null)
         {
            dependencies.add(new DependencyByQualifier(qualifier, type));
            return holder.getComponentAdapter(qualifier, type);
         }
         else
         {
            dependencies.add(new DependencyByType(type));
            return holder.getComponentAdapterOfType(type);
         }
      }
   }

   public <T> T createComponent(Class<T> clazz) throws Exception
   {
      return createComponent(clazz, null);
   }

   public <T> T createComponent(Class<T> clazz, InitParams params) throws Exception
   {
      List<Dependency> dependencies = new ArrayList<Dependency>();
      Constructor<T> constructor = getConstructor(clazz, dependencies);
      final Object[] args = getArguments(constructor, params, dependencies);
      loadArguments(args);
      return constructor.getDeclaringClass().cast(constructor.newInstance(args));
   }

   public <T> ComponentTask<T> createComponentTask(final Constructor<T> constructor, InitParams params,
      List<Dependency> dependencies, DependencyStackListener caller) throws Exception
   {
      final Object[] args = getArguments(constructor, params, dependencies);
      return new ComponentTask<T>(this, caller, ComponentTaskType.CREATE)
      {
         public T execute(CreationalContextComponentAdapter<?> cCtx) throws Exception
         {
            loadArguments(args);
            return constructor.getDeclaringClass().cast(constructor.newInstance(args));
         }
      };
   }

   public void loadArguments(Object[] args)
   {
      try
      {
         for (int i = 0, length = args.length; i < length; i++)
         {
            if (args[i] instanceof Dependency)
            {
               args[i] = ((Dependency)args[i]).load(holder);
            }
         }
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not load the arguments", e);
      }
   }

   public void loadDependencies(Object originalComponentKey, final ComponentTaskContext ctx,
      Collection<Dependency> dependencies, final ComponentTaskType type) throws Exception
   {
      if (dependencies.isEmpty())
         return;
      List<Future<?>> submittedTasks = null;
      boolean enableMultiThreading = Mode.hasMode(Mode.MULTI_THREADED) && dependencies.size() > 1;
      ThreadPoolExecutor executor = enableMultiThreading ? getExecutor() : null;
      if (enableMultiThreading && executor == null)
      {
         enableMultiThreading = false;
      }
      for (final Dependency dependency : dependencies)
      {
         if (dependency.getKey().equals(originalComponentKey) || dependency.isLazy())
         {
            // Prevent infinite loop
            continue;
         }
         if (enableMultiThreading && LockManager.getInstance().getTotalUncompletedTasks() < executor.getCorePoolSize()
            && !(dependency.getAdapter(holder) instanceof InstanceComponentAdapter))
         {
            final ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
            final ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Runnable task = new Runnable()
            {
               public void run()
               {
                  SecurityHelper.doPrivilegedAction(new PrivilegedAction<Object>()
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
                           currentCtx.set(ctx.addToContext(dependency.getKey(), type));
                           return dependency.load(holder);
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
            if (submittedTasks == null)
            {
               submittedTasks = new ArrayList<Future<?>>();
            }
            submittedTasks.add(executor.submit(task));
         }
         else
         {
            ComponentTaskContext previousCtx = currentCtx.get();
            try
            {
               currentCtx.set(ctx.addToContext(dependency.getKey(), type));
               dependency.load(holder);
            }
            finally
            {
               currentCtx.set(previousCtx);
            }
         }
      }
      if (submittedTasks != null)
      {
         for (int i = 0, length = submittedTasks.size(); i < length; i++)
         {
            Future<?> task = submittedTasks.get(i);
            try
            {
               task.get();
            }
            catch (ExecutionException e)
            {
               Throwable cause = e.getCause();
               if (cause instanceof Exception)
               {
                  throw (Exception)cause;
               }
               throw new Exception(cause);
            }
         }
      }
   }

   /**
    * Gives the current context
    */
   public ComponentTaskContext getComponentTaskContext()
   {
      return currentCtx.get();
   }

   /**
    * Set the current context
    */
   public void setComponentTaskContext(ComponentTaskContext ctx)
   {
      currentCtx.set(ctx);
   }

   protected <T> T execute(ComponentTask<T> task, CreationalContextComponentAdapter<?> cCtx) throws Exception
   {
      Deque<DependencyStack> stacks = null;
      CachingContainerMT cache = (CachingContainerMT)getCache();
      ThreadLocal<Deque<DependencyStack>> dependencyStacks = cache.dependencyStacks;
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
         return task.execute(cCtx);
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

   public <T> Object[] getArguments(Constructor<T> constructor, InitParams params, List<Dependency> dependencies)
   {
      Class<?>[] parameters = constructor.getParameterTypes();
      Object[] args = new Object[parameters.length];
      if (args.length == 0)
         return args;
      Iterator<Dependency> tasks = dependencies.iterator();
      for (int i = 0; i < parameters.length; i++)
      {
         final Class<?> parameter = parameters[i];
         if (parameter.equals(InitParams.class))
         {
            args[i] = params;
            continue;
         }
         args[i] = tasks.next();
      }
      return args;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public String getId()
   {
      return "ConcurrentContainer";
   }

   /**
    * {@inheritDoc}
    */
   public void onStartupComplete()
   {
      if (Mode.removeModes(Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE))
      {
         synchronized (ConcurrentContainerMT.class)
         {
            // Both modes could be removed so we can shutdown the executor
            ThreadPoolExecutor executor = EXECUTOR;
            if (executor != null && !executor.isShutdown())
            {
               executor.shutdown();
               // Release the executor for the GC
               EXECUTOR = null;
            }
         }
      }
   }

   /**
    * Starts all the dependencies of the adapter
    */
   private void startDependencies(final Map<ComponentAdapter<?>, Object> alreadyStarted,
      final Set<ComponentAdapter<?>> startInProgress, final AtomicReference<Exception> error,
      ComponentAdapterDependenciesAware<?> cada)
   {
      if (cada.getCreateDependencies() != null)
      {
         // Start first the create dependencies
         Collection<ComponentAdapter<?>> dep = getDependencies(cada.getCreateDependencies(), false, true);
         if (dep != null && !dep.isEmpty())
         {
            Set<ComponentAdapter<?>> startInProgressNew = new HashSet<ComponentAdapter<?>>(startInProgress);
            startInProgressNew.add(cada);
            start(dep, alreadyStarted, startInProgressNew, error, false);
         }
         dep = getDependencies(cada.getCreateDependencies(), true, false);
         if (dep != null && !dep.isEmpty())
         {
            Set<ComponentAdapter<?>> startInProgressNew = new HashSet<ComponentAdapter<?>>(startInProgress);
            startInProgressNew.add(cada);
            start(dep, alreadyStarted, startInProgressNew, error, true);
         }
      }
      if (cada.getInitDependencies() != null)
      {
         // Then start the init dependencies
         Collection<ComponentAdapter<?>> dep = getDependencies(cada.getInitDependencies(), true, true);
         if (dep != null && !dep.isEmpty())
         {
            Set<ComponentAdapter<?>> startInProgressNew = new HashSet<ComponentAdapter<?>>(startInProgress);
            startInProgressNew.add(cada);
            // remove the current adapter to prevent loop
            dep.remove(cada);
            start(dep, alreadyStarted, startInProgressNew, error, true);
         }
      }
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
         if (!t.isDaemon())
            t.setDaemon(true);
         if (t.getPriority() != Thread.NORM_PRIORITY)
            t.setPriority(Thread.NORM_PRIORITY);
         return t;
      }
   }

   private static class KernelThreadPoolExecutor extends ThreadPoolExecutor
   {
      public KernelThreadPoolExecutor(int threadPoolSize)
      {
         super(threadPoolSize, threadPoolSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
            new KernelThreadFactory(), new ThreadPoolExecutor.CallerRunsPolicy());
      }

      /**
       * {@inheritDoc}
       */
      protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value)
      {
         return LockManager.getInstance().createRunnableFuture(runnable, value);
      }

      /**
       * {@inheritDoc}
       */
      protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable)
      {
         return LockManager.getInstance().createRunnableFuture(callable);
      }

      /**
       * {@inheritDoc}
       */
      public Future<?> submit(Runnable task)
      {
         if (task == null)
            throw new NullPointerException();
         RunnableFuture<Object> ftask = newTaskFor(task, null);
         if (LockManager.getInstance().incrementAndGetTotalUncompletedTasks() <= getCorePoolSize())
            execute(ftask);
         else
            ftask.run();
         return ftask;
      }
   }
}
