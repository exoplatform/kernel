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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class ComponentAdapterStateAware extends AbstractComponentAdapter implements
   ComponentAdapterTaskContextAware, DependencyStackListener
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.ComponentAdapterStateAware");

   /**
    * Serial Version ID
    */
   private static final long serialVersionUID = -4278401979557595903L;

   /**
    * The current state of the component instance
    */
   private final AtomicReference<ComponentState> state;

   /**
    * The component instance
    */
   private volatile Object instance;

   /**
    * The non initialized component instance 
    */
   private volatile Object tempInstance;

   /**
    * Index of the init task from which we are supposed to start
    * it is required in case of failure to be able to skip tasks already
    * done
    */
   private AtomicInteger initTaskStartingIndex = new AtomicInteger();

   /**
    * All the dependencies for the create operation that have not been properly declared
    */
   private final Set<Class<?>> missingCreateDependencies;

   /**
    * All the dependencies for the init operation that have not been properly declared
    */
   private final Set<Class<?>> missingInitDependencies;

   /**
    * The container that holds the adapter
    */
   protected final ConcurrentContainerMT exocontainer;

   protected ComponentAdapterStateAware(ConcurrentContainerMT exocontainer, Object componentKey,
      Class<?> componentImplementation)
   {
      this(exocontainer, componentKey, null, componentImplementation, ComponentState.INITIAL);
   }

   protected ComponentAdapterStateAware(ConcurrentContainerMT exocontainer, Object componentKey,
      Object componentInstance)
   {
      this(exocontainer, componentKey, componentInstance, null, ComponentState.INITIALIZED);
   }

   private ComponentAdapterStateAware(ConcurrentContainerMT exocontainer, Object componentKey,
      Object componentInstance, Class<?> componentImplementation, ComponentState state)
   {
      super(componentKey, componentInstance == null ? (componentImplementation == null ? Object.class
         : componentImplementation) : componentInstance.getClass());
      if (state == null)
         throw new IllegalArgumentException("The state of the component cannot be null");
      this.exocontainer = exocontainer;
      this.state = new AtomicReference<ComponentState>(state);
      this.instance = componentInstance;
      if (Mode.hasMode(Mode.AUTO_SOLVE_DEP_ISSUES))
      {
         missingCreateDependencies = new CopyOnWriteArraySet<Class<?>>();
         missingInitDependencies = new CopyOnWriteArraySet<Class<?>>();
      }
      else
      {
         missingCreateDependencies = null;
         missingInitDependencies = null;
      }
   }

   /**
    * Gives the current state of the component adapter
    * @return
    */
   public ComponentState getState()
   {
      return state.get();
   }

   /**
    * Changes of state
    * @param state
    */
   private void setState(ComponentState state)
   {
      this.state.set(state);
   }

   private boolean compareAndSet(ComponentState expect, ComponentState update)
   {
      return state.compareAndSet(expect, update);
   }

   /**
    * {@inheritDoc}
    */
   public Object getComponentInstance()
   {
      return getComponentInstance(null);
   }

   /**
    * {@inheritDoc}
    */
   public Object getComponentInstance(ComponentTaskContext ctx)
   {
      if (instance != null)
         return instance;
      if (ctx != null && ctx.skipDependencyChecking())
      {
         Object o = tempInstance;
         if (o != null)
         {
            return o;
         }
      }
      create(ctx);
      if (ctx != null && ctx.skipDependencyChecking())
      {
         Object o = tempInstance;
         return o == null ? instance : o;
      }
      init(ctx);
      return instance;
   }

   /**
    * Gives the create dependencies of the component instance
    * @return a {@link Collection} of Class objects representing the dependencies
    * of the component instance for the creation phase
    */
   protected abstract Collection<Class<?>> getCreateDependencies();

   /**
    * Gives the task that will create the component instance
    * @param ctx the initialization's context of the component
    * @return an {@link ComponentTask} that will create the component instance 
    */
   protected abstract ComponentTask<Object> getCreateTask(ComponentTaskContext ctx);

   /**
    * Creates the component instance
    * @param ctx the initialization's context of the component
    */
   protected final void create(ComponentTaskContext ctx)
   {
      ComponentState state = getState();
      if (state != ComponentState.INITIAL)
         return;
      if (ctx == null)
      {
         ctx = new ComponentTaskContext(getComponentKey(), ComponentTaskType.CREATE);
      }
      ComponentTask<Object> task;
      try
      {
         task = getCreateTask(ctx);
      }
      catch (CyclicDependencyException e)
      {
         if (ctx.isRoot() && !ctx.skipDependencyChecking())
         {
            create(ctx.toSkipDependencyChecking());
            return;
         }
         throw e;
      }
      catch (RuntimeException e)
      {
         if (compareAndSet(state, ComponentState.UNKNOWN))
         {
            throw new RuntimeException("Cannot create component " + getComponentImplementation(), e);
         }
         else if (LOG.isDebugEnabled())
         {
            LOG.debug("Cannot create the component " + getComponentImplementation(), e);
         }
         return;
      }
      synchronized (this)
      {
         state = getState();
         if (state != ComponentState.INITIAL)
            return;
         try
         {
            tempInstance = task.call();
            setState(ComponentState.CREATED);
         }
         catch (CyclicDependencyException e)
         {
            if (ctx.isRoot() && !ctx.skipDependencyChecking())
            {
               create(ctx.toSkipDependencyChecking());
               return;
            }
            throw e;
         }
         catch (Exception e)
         {
            setState(ComponentState.UNKNOWN);
            throw new RuntimeException("Cannot create component " + getComponentImplementation(), e);
         }
      }
      if (ctx.isRoot() && ctx.skipDependencyChecking())
      {
         // In case we detected a CyclicDependencyException, the dependencies
         // could be partially initialized thus as we can assume that the
         // cyclic dependency issue is fixed we can retry to initialize them
         exocontainer.initialize(getEffectiveCreateDependencies(), ctx.disableMultiThreading());
      }
   }

   /**
    * Gives the initialization dependencies of the component instance
    * @return a {@link Collection} of Class objects representing the dependencies
    * of the component instance for the initialization phase
    */
   protected abstract Collection<Class<?>> getInitDependencies();

   /**
    * Gives the tasks that will initialize the component instance
    * @param instance the object instance that is currently being initialized
    * @param ctx the initialization's context of the component
    * @return a collection of {@link ComponentTask} that will initialize the component instance 
    */
   protected abstract Collection<ComponentTask<Void>> getInitTasks(Object instance, ComponentTaskContext ctx);

   /**
    * Initializes the component instance
    * @param ctx the initialization's context of the component
    */
   protected final void init(ComponentTaskContext ctx)
   {
      ComponentState state = getState();
      if (state != ComponentState.CREATED)
         return;
      if (ctx == null)
      {
         ctx = new ComponentTaskContext(getComponentKey(), ComponentTaskType.INIT);
      }
      ctx = ctx.addComponentToContext(getComponentKey(), tempInstance);
      Collection<ComponentTask<Void>> tasks;
      try
      {
         tasks = getInitTasks(tempInstance, ctx);
      }
      catch (CyclicDependencyException e)
      {
         if (ctx.isRoot() && !ctx.skipDependencyChecking())
         {
            init(ctx.toSkipDependencyChecking());
            return;
         }
         throw e;
      }
      catch (RuntimeException e)
      {
         if (compareAndSet(state, ComponentState.UNKNOWN))
         {
            throw new RuntimeException("Cannot initialize component " + getComponentImplementation(), e);
         }
         else if (LOG.isDebugEnabled())
         {
            LOG.debug("Cannot initialize the component " + getComponentImplementation(), e);
         }
         return;
      }
      synchronized (this)
      {
         state = getState();
         if (state != ComponentState.CREATED)
            return;
         ComponentTask<Void> task = null;
         try
         {
            if (tasks != null)
            {
               int taskIndex = 0;
               int startIndex = initTaskStartingIndex.get();
               for (Iterator<ComponentTask<Void>> it = tasks.iterator(); it.hasNext(); taskIndex++)
               {
                  if (taskIndex < startIndex)
                  {
                     // Skip tasks already done
                     it.next();
                     continue;
                  }
                  initTaskStartingIndex.set(taskIndex);
                  task = it.next();
                  task.call();
                  task = null;
               }
            }
            state = getState();
            if (state == ComponentState.CREATED)
            {
               // In case of cyclic dependency the component could be already initialized
               // so we need to recheck the state
               instance = tempInstance;
               tempInstance = null;
               setState(ComponentState.INITIALIZED);
            }
         }
         catch (CyclicDependencyException e)
         {
            if (ctx.isRoot() && !ctx.skipDependencyChecking())
            {
               init(ctx.toSkipDependencyChecking());
               return;
            }
            throw e;
         }
         catch (Exception e)
         {
            setState(ComponentState.UNKNOWN);
            if (task != null)
            {
               throw new RuntimeException("Cannot " + task.getName() + " for the component "
                  + getComponentImplementation(), e);
            }
            throw new RuntimeException("Cannot initialize component " + getComponentImplementation(), e);
         }
      }
      if (ctx.isRoot() && ctx.skipDependencyChecking())
      {
         // In case we detected a CyclicDependencyException, the dependencies
         // could be partially initialized thus as we can assume that the
         // cyclic dependency issue is fixed we can retry to initialize them
         exocontainer.initialize(getEffectiveInitDependencies(), ctx.disableMultiThreading());
      }
   }

   /**
    * Starts the component instance if needed
    * @param ctx the context of the startup
    */
   public final void start(ComponentTaskContext ctx)
   {
      ComponentState state = getState();
      if (state != ComponentState.INITIALIZED && state != ComponentState.STOPPED)
         return;
      if (!Startable.class.isAssignableFrom(getComponentImplementation()))
      {
         compareAndSet(state, ComponentState.STARTED);
         return;
      }
      if (ctx == null)
      {
         ctx = new ComponentTaskContext(getComponentKey(), ComponentTaskType.START);
      }
      if (!ctx.skipDependencyChecking())
      {
         try
         {
            // Start first the create dependencies
            exocontainer.startComponents(getEffectiveCreateDependencies(), ctx);
         }
         catch (CyclicDependencyException e)
         {
            // We ignore it
            if (ctx.isRoot() && !ctx.skipDependencyChecking())
            {
               try
               {
                  exocontainer.startComponents(getEffectiveCreateDependencies(), ctx.toSkipDependencyChecking());
               }
               catch (Exception ex)
               {
                  setState(ComponentState.UNKNOWN);
                  throw new RuntimeException("Cannot start create dependencies of the component "
                     + getComponentImplementation(), ex);
               }
            }
            throw e;
         }
         catch (Exception e)
         {
            setState(ComponentState.UNKNOWN);
            throw new RuntimeException("Cannot start create dependencies of the component "
               + getComponentImplementation(), e);
         }
         try
         {
            // Then we start the init dependencies
            exocontainer.startComponents(getEffectiveInitDependencies(), ctx);
         }
         catch (CyclicDependencyException e)
         {
            // We ignore it
            if (LOG.isDebugEnabled())
            {
               LOG.debug("A CyclicDependencyException has been detected while trying to start an init dependency of "
                  + getComponentImplementation(), e);
            }
         }
         catch (Exception e)
         {
            setState(ComponentState.UNKNOWN);
            throw new RuntimeException("Cannot start init dependencies of the component "
               + getComponentImplementation(), e);
         }
      }

      synchronized (this)
      {
         state = getState();
         if (state != ComponentState.INITIALIZED && state != ComponentState.STOPPED)
            return;
         try
         {
            if (instance instanceof Startable)
            {
               ComponentTask<Void> task = new ComponentTask<Void>(exocontainer, ctx, this, ComponentTaskType.START)
               {

                  @Override
                  protected Void execute() throws Exception
                  {
                     ((Startable)instance).start();
                     return null;
                  }
               };
               task.call();
            }
            setState(ComponentState.STARTED);
         }
         catch (Exception e)
         {
            setState(ComponentState.UNKNOWN);
            throw new RuntimeException("Cannot start component " + getComponentImplementation(), e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void callDependency(ComponentTask<?> task, Class<?> c)
   {
      if (PropertyManager.isDevelopping())
      {
         LOG.warn("An unexpected call of getComponentInstanceOfType('" + c.getName()
            + "') has been detected please add the component in your constructor instead", new Exception(
            "This is the stack trace allowing you to identify where the unexpected "
               + "call of getComponentInstanceOfType has been done"));
      }
      if (c.equals(getComponentKey()))
      {
         return;
      }
      Set<Class<?>> missingDependencies = null;
      if (task.getType() == ComponentTaskType.CREATE)
      {
         missingDependencies = missingCreateDependencies;
      }
      else if (task.getType() == ComponentTaskType.INIT)
      {
         missingDependencies = missingInitDependencies;
      }
      if (missingDependencies != null)
      {
         task.getContext().checkDependency(c, task.getType());
         missingDependencies.add(c);
      }
   }

   private Collection<Class<?>> getEffectiveCreateDependencies()
   {
      if (missingCreateDependencies == null || missingCreateDependencies.isEmpty())
      {
         return getCreateDependencies();
      }
      else if (getCreateDependencies() == null)
      {
         return missingCreateDependencies;
      }
      Set<Class<?>> dependencies = new HashSet<Class<?>>(getCreateDependencies());
      dependencies.addAll(missingCreateDependencies);
      return dependencies;
   }

   private Collection<Class<?>> getEffectiveInitDependencies()
   {
      if (missingInitDependencies == null || missingInitDependencies.isEmpty())
      {
         return getInitDependencies();
      }
      else if (getInitDependencies() == null)
      {
         return missingInitDependencies;
      }
      Set<Class<?>> dependencies = new HashSet<Class<?>>(getInitDependencies());
      dependencies.addAll(missingInitDependencies);
      return dependencies;
   }

   //   /**
   //    * Stops the component instance if needed
   //    */
   //   protected final void stop()
   //   {
   //      if (getState() != ComponentState.STARTED)
   //         return;
   //      synchronized (this)
   //      {
   //         if (getState() != ComponentState.STARTED)
   //            return;
   //         try
   //         {
   //            if (instance instanceof Startable)
   //            {
   //               ((Startable)instance).stop();
   //            }
   //         }
   //         catch (RuntimeException e)
   //         {
   //            if (LOG.isDebugEnabled())
   //            {
   //               LOG.debug("Cannot stop the component " + getComponentImplementation(), e);
   //            }
   //         }
   //         finally
   //         {
   //            setState(ComponentState.STOPPED);
   //         }
   //      }
   //   }
   //
   //   /**
   //    * Disposes the component instance if needed
   //    */
   //   protected final void dispose()
   //   {
   //      if (getState() == ComponentState.DISPOSED)
   //         return;
   //      synchronized (this)
   //      {
   //         if (getState() == ComponentState.DISPOSED)
   //            return;
   //         try
   //         {
   //            Object instance = this.tempInstance == null ? this.instance : this.tempInstance;
   //            if (instance instanceof Disposable)
   //            {
   //               ((Disposable)instance).dispose();
   //            }
   //         }
   //         catch (RuntimeException e)
   //         {
   //            if (LOG.isDebugEnabled())
   //            {
   //               LOG.debug("Cannot dispose the component " + getComponentImplementation(), e);
   //            }
   //         }
   //         finally
   //         {
   //            setState(ComponentState.DISPOSED);
   //         }
   //      }
   //   }
}
