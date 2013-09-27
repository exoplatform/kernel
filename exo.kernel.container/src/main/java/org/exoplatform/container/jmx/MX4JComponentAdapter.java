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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.AbstractComponentAdapter;
import org.exoplatform.container.ConcurrentContainer;
import org.exoplatform.container.ConcurrentContainer.CreationalContextComponentAdapter;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.component.ComponentLifecycle;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.context.ContextManager;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.reflect.Method;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.NormalScope;
import javax.enterprise.context.spi.Context;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.DefinitionException;
import javax.enterprise.inject.spi.PassivationCapable;
import javax.inject.Provider;
import javax.inject.Scope;
import javax.inject.Singleton;

/**
 * @author James Strachan
 * @author Mauro Talevi
 * @author Jeppe Cramon
 * @author Benjamin Mestrallet
 * @version $Revision: 1.5 $
 */
public class MX4JComponentAdapter<T> extends AbstractComponentAdapter<T> implements Contextual<T>, PassivationCapable
{

   /**
    * The prefix of the id
    */
   private static final String PREFIX = MX4JComponentAdapter.class.getPackage().getName();

   /**
    * Serial Version ID
    */
   private static final long serialVersionUID = -9001193588034229411L;

   protected transient volatile T instance_;

   private transient volatile T proxy;

   private transient volatile String id;

   protected transient final Lock lock = new ReentrantLock();

   /**
    * Indicates whether or not it should be managed as a singleton
    */
   protected volatile boolean isSingleton = true;

   protected transient volatile boolean isInitialized;

   /**
    * The scope of the adapter
    */
   protected transient final AtomicReference<Class<? extends Annotation>> scope =
      new AtomicReference<Class<? extends Annotation>>();

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.MX4JComponentAdapter");

   /** . */
   protected transient final ExoContainer exocontainer;

   /** . */
   protected transient final ConcurrentContainer container;

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
      else if (proxy != null)
         return proxy;

      if (!exocontainer.isContextManagerLoaded() && ContextManager.class.isAssignableFrom(getComponentImplementation()))
      {
         return create();
      }
      ContextManager manager = exocontainer.getContextManager();
      if (manager == null)
      {
         return create();
      }
      return create(manager, true);
   }

   private T create(ContextManager manager, boolean retryAllowed)
   {
      Class<? extends Annotation> scope = getScope(true, false);
      if (scope.equals(Unknown.class) || scope.equals(Singleton.class) || scope.equals(Dependent.class)
         || scope.equals(ApplicationScoped.class))
      {
         return create();
      }
      final Context ctx = manager.getContext(scope);
      if (ctx == null)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("The scope {} is unknown, thus we will create the component {} out of a scope context.",
               scope.getName(), getComponentImplementation().getName());
         }
         if (!retryAllowed)
            throw new IllegalArgumentException("The scope and the default scope of the class "
               + getComponentImplementation().getName() + " are unknown");
         try
         {
            Class<? extends Annotation> defaultScope = ContainerUtil.getScope(getComponentImplementation(), true);
            setScope(scope, defaultScope);
            return create(manager, false);
         }
         catch (DefinitionException e)
         {
            throw new IllegalArgumentException("The scope of the class " + getComponentImplementation().getName()
               + " is unknown and we cannot get a clear default scope: " + e.getMessage());
         }
      }
      NormalScope normalScope = scope.getAnnotation(NormalScope.class);
      if (normalScope != null)
      {
         // A proxy is expected
         if (normalScope.passivating() && !Serializable.class.isAssignableFrom(getComponentImplementation()))
         {
            throw new IllegalArgumentException("As the scope " + scope.getName()
               + " is a passivating scope, we expect only serializable objects and "
               + getComponentImplementation().getName() + " is not serializable.");
         }
         try
         {
            lock.lock();
            if (proxy != null)
               return proxy;
            T result = ContainerUtil.createProxy(getComponentImplementation(), new Provider<T>()
            {
               public T get()
               {
                  return createInstance(ctx);
               }
            });
            return proxy = result;
         }
         finally
         {
            lock.unlock();
         }
      }
      return createInstance(ctx);
   }

   /**
    * Gives the scope of the adapter
    */
   public Class<? extends Annotation> getScope()
   {
      return getScope(false, false);
   }

   protected Class<? extends Annotation> getScope(boolean initializeIfNull, boolean ignoreExplicit)
   {
      Class<? extends Annotation> scope = this.scope.get();
      if (scope == null && initializeIfNull)
      {
         scope = ContainerUtil.getScope(getComponentImplementation(), ignoreExplicit);
         scope = setScope(null, scope);
      }
      return scope;
   }

   private Class<? extends Annotation> setScope(Class<? extends Annotation> expect, Class<? extends Annotation> scope)
   {
      if (scope == null)
      {
         scope = Unknown.class;
         isSingleton = true;
      }
      else
      {
         isSingleton = scope.equals(Singleton.class) || scope.equals(ApplicationScoped.class);
      }
      if (this.scope.compareAndSet(expect, scope))
      {
         return scope;
      }
      return this.scope.get();
   }

   protected T createInstance(Context ctx)
   {
      T result = ctx.get(this);
      if (result != null)
      {
         return result;
      }
      try
      {
         return ctx.get(this, container.<T> addComponentToCtx(getComponentKey()));
      }
      finally
      {
         container.removeComponentFromCtx(getComponentKey());
      }
   }

   private T createInstance(final CreationalContextComponentAdapter<T> ctx, final Component component,
      final ConfigurationManager manager, final String componentKey, final InitParams params, final boolean debug)
      throws Exception
   {
      try
      {
         return SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<T>()
         {
            public T run() throws Exception
            {
               T instance;
               final Class<T> implementationClass = getComponentImplementation();
               // Please note that we cannot fully initialize the Object "instance_" before releasing other
               // threads because it could cause StackOverflowError due to recursive calls
               instance = exocontainer.createComponent(implementationClass, params);
               if (instance_ != null)
               {
                  // Avoid instantiating twice the same component in case of a cyclic reference due
                  // to component plugins
                  return instance_;
               }
               else if (ctx.get() != null)
                  return ctx.get();

               ctx.push(instance);
               boolean isSingleton = MX4JComponentAdapter.this.isSingleton;
               boolean isInitialized = MX4JComponentAdapter.this.isInitialized;
               if (debug)
                  LOG.debug("==> create  component : " + instance);
               boolean hasInjectableConstructor =
                  !isSingleton || ContainerUtil.hasInjectableConstructor(implementationClass);
               boolean hasOnlyEmptyPublicConstructor =
                  !isSingleton || ContainerUtil.hasOnlyEmptyPublicConstructor(implementationClass);
               if (hasInjectableConstructor || hasOnlyEmptyPublicConstructor)
               {
                  // There is at least one constructor JSR 330 compliant or we already know 
                  // that it is not a singleton such that the new behavior is expected
                  boolean isInjectPresent = container.initializeComponent(instance);
                  isSingleton = manageScope(isSingleton, isInitialized, hasInjectableConstructor, isInjectPresent);
               }
               else if (!isInitialized)
               {
                  // The adapter has not been initialized yet
                  // The old behavior is expected as there is no constructor JSR 330 compliant 
                  isSingleton = MX4JComponentAdapter.this.isSingleton = true;
                  scope.set(Singleton.class);
               }
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
               if (!isInitialized)
               {
                  if (isSingleton)
                  {
                     instance_ = instance;
                  }
                  MX4JComponentAdapter.this.isInitialized = true;
               }
               return instance;
            }
         });
      }
      catch (PrivilegedActionException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof Exception)
         {
            throw (Exception)cause;
         }
         throw new Exception(cause);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isSingleton()
   {
      return isInitialized ? isSingleton : (isSingleton = ContainerUtil.isSingleton(getComponentImplementation()));
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
   protected static Method getSetMethod(Class<?> clazz, String name, Class<?> pluginClass)
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
    * Must be used to create Singleton or Prototype only
    */
   protected T create()
   {
      boolean toBeLocked = !isInitialized;
      try
      {
         if (toBeLocked)
         {
            lock.lock();
         }
         return create(container.<T> addComponentToCtx(getComponentKey()));
      }
      finally
      {
         if (toBeLocked)
         {
            lock.unlock();
         }
         container.removeComponentFromCtx(getComponentKey());
      }
   }

   /**
    * {@inheritDoc}
    */
   public T create(CreationalContext<T> creationalContext)
   {
      //
      T instance;
      Component component = null;
      ConfigurationManager manager;
      String componentKey;
      InitParams params = null;
      boolean debug = false;
      CreationalContextComponentAdapter<T> ctx = (CreationalContextComponentAdapter<T>)creationalContext;
      try
      {
         // Avoid to create duplicate instances if it is called at the same time by several threads
         if (instance_ != null)
            return instance_;
         else if (ctx.get() != null)
            return ctx.get();
         // Get the component
         Object key = getComponentKey();
         if (key instanceof String)
            componentKey = (String)key;
         else
            componentKey = ((Class<?>)key).getName();
         manager = exocontainer.getComponentInstanceOfType(ConfigurationManager.class);
         component = manager == null ? null : manager.getComponent(componentKey);
         if (component != null)
         {
            params = component.getInitParams();
            debug = component.getShowDeployInfo();
         }
         instance = createInstance(ctx, component, manager, componentKey, params, debug);
         if (instance instanceof Startable && exocontainer.canBeStopped())
         {
            // Start the component if the container is already started
            ((Startable)instance).start();
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
      return instance;
   }

   /**
    * {@inheritDoc}
    */
   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      try
      {
         creationalContext.release();
      }
      catch (Exception e)
      {
         LOG.error("Could not destroy the instance " + instance + ": " + e.getMessage());
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((getComponentKey() == null) ? 0 : getComponentKey().hashCode());
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      MX4JComponentAdapter<?> other = (MX4JComponentAdapter<?>)obj;
      if (getComponentKey() == null)
      {
         if (other.getComponentKey() != null)
            return false;
      }
      else if (!getComponentKey().equals(other.getComponentKey()))
         return false;
      return true;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      if (id != null)
         return id;
      StringBuilder sb = new StringBuilder(PREFIX);
      Object key = getComponentKey();
      String componentKey;
      if (key instanceof String)
         componentKey = (String)key;
      else
         componentKey = ((Class<?>)key).getName();
      return id = sb.append(componentKey).toString();
   }

   /**
    * @param isSingleton
    * @param isInitialized
    * @param hasInjectableConstructor
    * @param isInjectPresent
    * @return
    */
   protected boolean manageScope(boolean isSingleton, boolean isInitialized, boolean hasInjectableConstructor,
      boolean isInjectPresent)
   {
      if (!isInitialized)
      {
         // The adapter has not been initialized yet
         if (isInjectPresent || hasInjectableConstructor)
         {
            // The component is JSR 330 compliant, so we expect the new behavior
            Class<? extends Annotation> currentScope = scope.get();
            if (currentScope == null)
            {
               // The scope has not been set which means that the Context Manager has not been defined
               currentScope = getScope(true, false);
               if (!currentScope.equals(Unknown.class) && !currentScope.equals(Singleton.class)
                  && !currentScope.equals(Dependent.class) && !currentScope.equals(ApplicationScoped.class))
               {
                  // The context manager has not been defined and the defined scope is not part of the supported ones
                  // so we will check the default one and reset the scope
                  scope.compareAndSet(currentScope, null);
                  currentScope = getScope(true, true);
                  if (!currentScope.equals(Unknown.class) && !currentScope.equals(Singleton.class)
                     && !currentScope.equals(Dependent.class)
                     && !currentScope.equals(ApplicationScoped.class))
                  {
                     // The context manager has not been defined and the defined default scope is not part of the supported ones
                     // so we will check the default one and set the scope to unknown
                     scope.compareAndSet(currentScope, Unknown.class);
                     currentScope = Unknown.class;
                  }
               }
            }
            if (currentScope.equals(Unknown.class))
            {
               // The scope is unknown so far
               isSingleton = MX4JComponentAdapter.this.isSingleton = false;
               scope.set(Dependent.class);
            }
            else
            {
               isSingleton = MX4JComponentAdapter.this.isSingleton;
            }
         }
         else
         {
            // The old behavior is expected as there the component is not JSR 330 compliant 
            isSingleton = MX4JComponentAdapter.this.isSingleton = true;
            scope.set(Singleton.class);
         }
      }
      return isSingleton;
   }

   @Scope
   @Documented
   @Retention(RUNTIME)
   protected static @interface Unknown {
   }
}
