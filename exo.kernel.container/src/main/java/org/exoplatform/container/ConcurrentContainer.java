/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.container;

import org.exoplatform.container.management.ManageableComponentAdapterFactory;
import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ComponentAdapterFactory;
import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.ContainerVisitor;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.management.MBeanServer;
import javax.management.ObjectName;

/**
 * This class is aimed to be a ThreadSafe implementation of an {@link Container} based on
 * java.util.concurrent collections.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 6 mai 2010
 * @version $Revision$  
 */
public class ConcurrentContainer extends AbstractInterceptor
{

   /**
    * The serial version UID 
    */
   private static final long serialVersionUID = -2275793454555604533L;

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ConcurrentContainer");

   protected final ConcurrentMap<Object, ComponentAdapter<?>> componentKeyToAdapterCache =
      new ConcurrentHashMap<Object, ComponentAdapter<?>>();

   private ComponentAdapterFactory componentAdapterFactory;

   protected final Set<ComponentAdapter<?>> componentAdapters = new CopyOnWriteArraySet<ComponentAdapter<?>>();

   // Keeps track of instantiation order.
   protected final CopyOnWriteArrayList<ComponentAdapter<?>> orderedComponentAdapters =
      new CopyOnWriteArrayList<ComponentAdapter<?>>();

   protected final Set<ExoContainer> children = new CopyOnWriteArraySet<ExoContainer>();

   /**
    * Context used to keep in memory the components that are currently being created.
    * This context is used to prevent cyclic resolution due to component plugins.
    */
   private final transient ThreadLocal<Map<Object, Object>> depResolutionCtx = new ThreadLocal<Map<Object, Object>>();

   /**
    * Creates a new container with the default {@link ComponentAdapterFactory} and a parent container.
    */
   public ConcurrentContainer()
   {
   }

   /**
    * Creates a new container with the default {@link ComponentAdapterFactory} and a parent container.
    *
    * @param holder                  the holder of the container
    * @param parent                  the parent container (used for component dependency lookups).
    */
   public ConcurrentContainer(ExoContainer holder, ExoContainer parent)
   {
      setParent(parent);
      setHolder(holder);
   }

   public Collection<ComponentAdapter<?>> getComponentAdapters()
   {
      return Collections.unmodifiableSet(componentAdapters);
   }

   public void setHolder(ExoContainer holder)
   {
      this.holder = holder;
      this.componentAdapterFactory = getDefaultComponentAdapterFactory();
   }

   protected ComponentAdapterFactory getDefaultComponentAdapterFactory()
   {
      return new ManageableComponentAdapterFactory(holder, this);
   }

   public ComponentAdapter<?> getComponentAdapter(Object componentKey) throws ContainerException
   {
      ComponentAdapter<?> adapter = componentKeyToAdapterCache.get(componentKey);
      if (adapter == null && parent != null)
      {
         adapter = parent.getComponentAdapter(componentKey);
      }
      return adapter;
   }

   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType)
   {
      // See http://jira.codehaus.org/secure/ViewIssue.jspa?key=PICO-115
      @SuppressWarnings("unchecked")
      ComponentAdapter<T> adapterByKey = (ComponentAdapter<T>)getComponentAdapter(componentType);
      if (adapterByKey != null)
      {
         return adapterByKey;
      }

      List<ComponentAdapter<T>> found = getComponentAdaptersOfType(componentType);

      if (found.size() == 1)
      {
         return found.get(0);
      }
      else if (found.size() == 0)
      {
         if (parent != null)
         {
            return parent.getComponentAdapterOfType(componentType);
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
            ComponentAdapter<T> componentAdapter = found.get(i);
            foundClasses[i] = componentAdapter.getComponentImplementation();
         }
         throw new ContainerException("Several ComponentAdapter found for " + componentType);
      }
   }

   @SuppressWarnings("unchecked")
   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      if (componentType == null)
      {
         return Collections.emptyList();
      }
      List<ComponentAdapter<T>> found = new ArrayList<ComponentAdapter<T>>();
      for (Iterator<ComponentAdapter<?>> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapter<?> componentAdapter = iterator.next();

         if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation()))
         {
            found.add((ComponentAdapter<T>)componentAdapter);
         }
      }
      return found;
   }

   /**
    * Register a component via a ComponentAdapter. Use this if you need fine grained control over what
    * ComponentAdapter to use for a specific component.
    * 
    * @param componentAdapter the adapter
    * @return the same adapter that was passed as an argument.
    * @throws ContainerException if registration fails.
    */
   protected ComponentAdapter<?> registerComponent(ComponentAdapter<?> componentAdapter) throws ContainerException
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_COMPONENT_PERMISSION);

      Object componentKey = componentAdapter.getComponentKey();

      if (componentKeyToAdapterCache.putIfAbsent(componentKey, componentAdapter) != null)
      {
         throw new ContainerException("Key " + componentKey + " duplicated");
      }
      componentAdapters.add(componentAdapter);
      return componentAdapter;
   }

   public ComponentAdapter<?> unregisterComponent(Object componentKey)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_COMPONENT_PERMISSION);

      ComponentAdapter<?> adapter = componentKeyToAdapterCache.remove(componentKey);
      if (adapter instanceof InstanceComponentAdapter)
      {
         Object value = adapter.getComponentInstance();
         if (value instanceof Container)
         {
            children.remove((Container)value);
         }
      }
      componentAdapters.remove(adapter);
      orderedComponentAdapters.remove(adapter);
      return adapter;
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be an {@link InstanceComponentAdapter}.
    */
   public <T> ComponentAdapter<T> registerComponentInstance(Object componentKey, T componentInstance)
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
      ComponentAdapter<T> componentAdapter = new InstanceComponentAdapter<T>(componentKey, componentInstance);
      registerComponent(componentAdapter);
      return componentAdapter;
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be instantiated by the {@link ComponentAdapterFactory}
    * passed to the container's constructor.
    */
   public <T> ComponentAdapter<T> registerComponentImplementation(Object componentKey, Class<T> componentImplementation)
      throws ContainerException
   {
      ComponentAdapter<T> componentAdapter =
         componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation);
      registerComponent(componentAdapter);
      return componentAdapter;
   }

   protected void addOrderedComponentAdapter(ComponentAdapter<?> componentAdapter)
   {
      orderedComponentAdapters.addIfAbsent(componentAdapter);
   }

   public List<Object> getComponentInstances() throws ContainerException
   {
      return getComponentInstancesOfType(Object.class);
   }

   @SuppressWarnings("unchecked")
   public <T> List<T> getComponentInstancesOfType(Class<T> componentType) throws ContainerException
   {
      if (componentType == null)
      {
         return Collections.emptyList();
      }

      Map<ComponentAdapter<T>, Object> adapterToInstanceMap = new HashMap<ComponentAdapter<T>, Object>();
      for (Iterator<ComponentAdapter<?>> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapter<?> componentAdapter = iterator.next();
         if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation()))
         {
            Object componentInstance = getInstance(componentAdapter);
            adapterToInstanceMap.put((ComponentAdapter<T>)componentAdapter, componentInstance);

            // This is to ensure all are added. (Indirect dependencies will be added
            // from InstantiatingComponentAdapter).
            addOrderedComponentAdapter(componentAdapter);
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

   public Object getComponentInstance(Object componentKey) throws ContainerException
   {
      ComponentAdapter<?> componentAdapter = getComponentAdapter(componentKey);
      if (componentAdapter != null)
      {
         return getInstance(componentAdapter);
      }
      else
      {
         return null;
      }
   }

   /**
    * If no {@link ComponentAdapter} can be found it returns <tt>null</tt> otherwise
    * it first try to get it from the dependency resolution context if it still cannot
    * be found we get the instance from the {@link ComponentAdapter}.
    * @see Container#getComponentInstanceOfType(java.lang.Class)
    */
   public <T> T getComponentInstanceOfType(Class<T> componentType)
   {
      final ComponentAdapter<T> componentAdapter = getComponentAdapterOfType(componentType);
      if (componentAdapter == null)
         return null;
      Map<Object, Object> map = depResolutionCtx.get();
      if (map != null)
      {
         Object result = map.get(componentAdapter.getComponentKey());
         if (result != null)
         {
            return componentType.cast(result);
         }
      }
      return getInstance(componentAdapter);
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

   @SuppressWarnings("unchecked")
   private <T> T getInstance(ComponentAdapter<T> componentAdapter)
   {
      // check wether this is our adapter
      // we need to check this to ensure up-down dependencies cannot be followed
      final boolean isLocal = componentAdapters.contains(componentAdapter);

      if (isLocal)
      {
         T instance = componentAdapter.getComponentInstance();

         addOrderedComponentAdapter(componentAdapter);

         return instance;
      }
      else if (parent != null)
      {
         return (T)parent.getComponentInstance(componentAdapter.getComponentKey());
      }

      return null;
   }

   /**
    * Start the components of this Container and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Startable} will be started.
    */
   public void start()
   {
      LifecycleVisitor.start(this);
   }

   /**
    * Stop the components of this Container and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Startable} will be stopped.
    */
   public void stop()
   {
      LifecycleVisitor.stop(this);
   }

   /**
    * Dispose the components of this Container and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Disposable} will be disposed.
    */
   public void dispose()
   {
      LifecycleVisitor.dispose(this);
   }

   public void accept(ContainerVisitor visitor)
   {
      visitor.visitContainer(holder);
      for (Iterator<ExoContainer> iterator = children.iterator(); iterator.hasNext();)
      {
         ExoContainer child = iterator.next();
         child.accept(visitor);
      }
   }

   /**
    * @return the holder
    */
   public ExoContainer getHolder()
   {
      return holder;
   }

   /**
    * {@inheritDoc}
    */
   public ManagementContext getManagementContext()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public MBeanServer getMBeanServer()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public ObjectName getScopingObjectName()
   {
      return null;
   }

   public <T> T createComponent(Class<T> clazz) throws Exception
   {
      return createComponent(clazz, null);
   }

   /**
    * {@inheritDoc}
    */
   public <T> T createComponent(Class<T> clazz, InitParams params) throws Exception
   {
      if (LOG.isDebugEnabled())
         LOG.debug(clazz.getName() + " " + ((params != null) ? params : "") + " added to "
            + getHolder().getContext().getName());
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
               args[i] = holder.getComponentInstanceOfType(parameters[i]);
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
    * {@inheritDoc}
    */
   public void initialize()
   {
   }
}
