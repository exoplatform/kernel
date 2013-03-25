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

import org.exoplatform.container.security.ContainerPermissions;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.PicoVerificationException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.alternatives.ImmutablePicoContainer;
import org.picocontainer.defaults.AbstractPicoVisitor;
import org.picocontainer.defaults.AmbiguousComponentResolutionException;
import org.picocontainer.defaults.CachingComponentAdapter;
import org.picocontainer.defaults.CachingComponentAdapterFactory;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;
import org.picocontainer.defaults.InstanceComponentAdapter;
import org.picocontainer.defaults.VerifyingVisitor;

import java.io.Serializable;
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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class is aimed to be the ThreadSafe counterpart of {@link DefaultPicoContainer} based on
 * java.util.concurrent collections.
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 6 mai 2010
 * @version $Revision$  
 */
@SuppressWarnings("unchecked")
public class ConcurrentPicoContainer implements MutablePicoContainer, Serializable
{

   /**
    * The serial version UID 
    */
   private static final long serialVersionUID = -2275793454555604533L;

   private final ConcurrentMap<Object, ComponentAdapter> componentKeyToAdapterCache =
      new ConcurrentHashMap<Object, ComponentAdapter>();

   private final ComponentAdapterFactory componentAdapterFactory;

   private final PicoContainer parent;

   private final Set<ComponentAdapter> componentAdapters = new CopyOnWriteArraySet<ComponentAdapter>();

   // Keeps track of instantiation order.
   private final CopyOnWriteArrayList<ComponentAdapter> orderedComponentAdapters = new CopyOnWriteArrayList<ComponentAdapter>();

   private final AtomicBoolean started = new AtomicBoolean();

   private final AtomicBoolean disposed = new AtomicBoolean();

   private final Set<PicoContainer> children = new CopyOnWriteArraySet<PicoContainer>();
   
   /**
    * Context used to keep in memory the components that are currently being created.
    * This context is used to prevent cyclic resolution due to component plugins.
    */
   private final transient ThreadLocal<Map<Object, Object>> depResolutionCtx = new ThreadLocal<Map<Object, Object>>();

   /**
    * Creates a new container with a custom ComponentAdapterFactory and a parent container.
    * <p/>
    * <em>
    * Important note about caching: If you intend the components to be cached, you should pass
    * in a factory that creates {@link CachingComponentAdapter} instances, such as for example
    * {@link CachingComponentAdapterFactory}. CachingComponentAdapterFactory can delegate to
    * other ComponentAdapterFactories.
    * </em>
    *
    * @param componentAdapterFactory the factory to use for creation of ComponentAdapters.
    * @param parent                  the parent container (used for component dependency lookups).
    */
   public ConcurrentPicoContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
   {
      if (componentAdapterFactory == null)
      {
         throw new IllegalArgumentException("componentAdapterFactory");
      }
      this.componentAdapterFactory = componentAdapterFactory;
      this.parent = parent == null ? null : new ImmutablePicoContainer(parent);
   }

   /**
    * Creates a new container with a (caching) {@link DefaultComponentAdapterFactory}
    * and a parent container.
    */
   public ConcurrentPicoContainer(PicoContainer parent)
   {
      this(new DefaultComponentAdapterFactory(), parent);
   }

   /**
    * Creates a new container with a custom ComponentAdapterFactory and no parent container.
    *
    * @param componentAdapterFactory the ComponentAdapterFactory to use.
    */
   public ConcurrentPicoContainer(ComponentAdapterFactory componentAdapterFactory)
   {
      this(componentAdapterFactory, null);
   }

   /**
    * Creates a new container with a (caching) {@link DefaultComponentAdapterFactory} and no parent container.
    */
   public ConcurrentPicoContainer()
   {
      this(new DefaultComponentAdapterFactory(), null);
   }

   public Collection getComponentAdapters()
   {
      return Collections.unmodifiableSet(componentAdapters);
   }

   public final ComponentAdapter getComponentAdapter(Object componentKey) throws AmbiguousComponentResolutionException
   {
      ComponentAdapter adapter = componentKeyToAdapterCache.get(componentKey);
      if (adapter == null && parent != null)
      {
         adapter = parent.getComponentAdapter(componentKey);
      }
      return adapter;
   }

   public ComponentAdapter getComponentAdapterOfType(Class componentType)
   {
      // See http://jira.codehaus.org/secure/ViewIssue.jspa?key=PICO-115
      ComponentAdapter adapterByKey = getComponentAdapter(componentType);
      if (adapterByKey != null)
      {
         return adapterByKey;
      }

      List found = getComponentAdaptersOfType(componentType);

      if (found.size() == 1)
      {
         return ((ComponentAdapter)found.get(0));
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
         Class[] foundClasses = new Class[found.size()];
         for (int i = 0; i < foundClasses.length; i++)
         {
            ComponentAdapter componentAdapter = (ComponentAdapter)found.get(i);
            foundClasses[i] = componentAdapter.getComponentImplementation();
         }

         throw new AmbiguousComponentResolutionException(componentType, foundClasses);
      }
   }

   public List getComponentAdaptersOfType(Class componentType)
   {
      if (componentType == null)
      {
         return Collections.EMPTY_LIST;
      }
      List<ComponentAdapter> found = new ArrayList<ComponentAdapter>();
      for (Iterator<ComponentAdapter> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapter componentAdapter = iterator.next();

         if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation()))
         {
            found.add(componentAdapter);
         }
      }
      return found;
   }

   /**
    * {@inheritDoc}
    * This method can be used to override the ComponentAdapter created by the {@link ComponentAdapterFactory}
    * passed to the constructor of this container.
    */
   public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
      throws DuplicateComponentKeyRegistrationException
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_COMPONENT_PERMISSION);     
      
      Object componentKey = componentAdapter.getComponentKey();

      if (componentKeyToAdapterCache.putIfAbsent(componentKey, componentAdapter) != null)
      {
         throw new DuplicateComponentKeyRegistrationException(componentKey);
      }
      componentAdapters.add(componentAdapter);
      return componentAdapter;
   }

   public ComponentAdapter unregisterComponent(Object componentKey)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_COMPONENT_PERMISSION);     
      
      ComponentAdapter adapter = componentKeyToAdapterCache.remove(componentKey);
      componentAdapters.remove(adapter);
      orderedComponentAdapters.remove(adapter);
      return adapter;
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be an {@link InstanceComponentAdapter}.
    */
   public ComponentAdapter registerComponentInstance(Object component) throws PicoRegistrationException
   {
      return registerComponentInstance(component.getClass(), component);
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be an {@link InstanceComponentAdapter}.
    */
   public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
      throws PicoRegistrationException
   {
      if (componentInstance instanceof MutablePicoContainer)
      {
         MutablePicoContainer pc = (MutablePicoContainer)componentInstance;
         Object contrivedKey = new Object();
         String contrivedComp = "";
         pc.registerComponentInstance(contrivedKey, contrivedComp);
         try
         {
            if (this.getComponentInstance(contrivedKey) != null)
            {
               throw new PicoRegistrationException(
                  "Cannot register a container to itself. The container is already implicitly registered.");
            }
         }
         finally
         {
            pc.unregisterComponent(contrivedKey);
         }

      }
      ComponentAdapter componentAdapter = new InstanceComponentAdapter(componentKey, componentInstance);
      registerComponent(componentAdapter);
      return componentAdapter;
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be instantiated by the {@link ComponentAdapterFactory}
    * passed to the container's constructor.
    */
   public ComponentAdapter registerComponentImplementation(Class componentImplementation)
      throws PicoRegistrationException
   {
      return registerComponentImplementation(componentImplementation, componentImplementation);
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be instantiated by the {@link ComponentAdapterFactory}
    * passed to the container's constructor.
    */
   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation)
      throws PicoRegistrationException
   {
      return registerComponentImplementation(componentKey, componentImplementation, (Parameter[])null);
   }

   /**
    * {@inheritDoc}
    * The returned ComponentAdapter will be instantiated by the {@link ComponentAdapterFactory}
    * passed to the container's constructor.
    */
   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
      Parameter[] parameters) throws PicoRegistrationException
   {
      ComponentAdapter componentAdapter =
         componentAdapterFactory.createComponentAdapter(componentKey, componentImplementation, parameters);
      registerComponent(componentAdapter);
      return componentAdapter;
   }

   /**
    * Same as {@link #registerComponentImplementation(java.lang.Object, java.lang.Class, org.picocontainer.Parameter[])}
    * but with parameters as a {@link List}. Makes it possible to use with Groovy arrays (which are actually Lists).
    */
   public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation,
      List parameters) throws PicoRegistrationException
   {
      Parameter[] parametersAsArray = (Parameter[])parameters.toArray(new Parameter[parameters.size()]);
      return registerComponentImplementation(componentKey, componentImplementation, parametersAsArray);
   }

   private void addOrderedComponentAdapter(ComponentAdapter componentAdapter)
   {
      orderedComponentAdapters.addIfAbsent(componentAdapter);
   }

   public List getComponentInstances() throws PicoException
   {
      return getComponentInstancesOfType(Object.class);
   }

   public List getComponentInstancesOfType(Class componentType) throws PicoException
   {
      if (componentType == null)
      {
         return Collections.EMPTY_LIST;
      }

      Map<ComponentAdapter, Object> adapterToInstanceMap = new HashMap<ComponentAdapter, Object>();
      for (Iterator<ComponentAdapter> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapter componentAdapter = iterator.next();
         if (componentType.isAssignableFrom(componentAdapter.getComponentImplementation()))
         {
            Object componentInstance = getInstance(componentAdapter);
            adapterToInstanceMap.put(componentAdapter, componentInstance);

            // This is to ensure all are added. (Indirect dependencies will be added
            // from InstantiatingComponentAdapter).
            addOrderedComponentAdapter(componentAdapter);
         }
      }
      List<Object> result = new ArrayList<Object>();
      for (Iterator<ComponentAdapter> iterator = orderedComponentAdapters.iterator(); iterator.hasNext();)
      {
         Object componentAdapter = iterator.next();
         final Object componentInstance = adapterToInstanceMap.get(componentAdapter);
         if (componentInstance != null)
         {
            // may be null in the case of the "implicit" adapter
            // representing "this".
            result.add(componentInstance);
         }
      }
      return result;
   }

   public Object getComponentInstance(Object componentKey) throws PicoException
   {
      ComponentAdapter componentAdapter = getComponentAdapter(componentKey);
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
    * @see org.picocontainer.PicoContainer#getComponentInstanceOfType(java.lang.Class)
    */
   public Object getComponentInstanceOfType(Class componentType)
   {
      final ComponentAdapter componentAdapter = getComponentAdapterOfType(componentType);
      if (componentAdapter == null)
         return null;
      Map<Object, Object> map = depResolutionCtx.get();
      if (map != null)
      {
         Object result = map.get(componentAdapter.getComponentKey());
         if (result != null)
         {
            return result;
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
   
   private Object getInstance(ComponentAdapter componentAdapter)
   {
      // check wether this is our adapter
      // we need to check this to ensure up-down dependencies cannot be followed
      final boolean isLocal = componentAdapters.contains(componentAdapter);

      if (isLocal)
      {
         Object instance = componentAdapter.getComponentInstance(this);

         addOrderedComponentAdapter(componentAdapter);

         return instance;
      }
      else if (parent != null)
      {
         return parent.getComponentInstance(componentAdapter.getComponentKey());
      }

      return null;
   }

   public PicoContainer getParent()
   {
      return parent;
   }

   public ComponentAdapter unregisterComponentByInstance(Object componentInstance)
   {
      for (Iterator<ComponentAdapter> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapter componentAdapter = iterator.next();
         Object o;
         try
         {
            o = getInstance(componentAdapter);
         }
         catch (Exception e)
         {
            // Ignore me,
            // may be caused by lazy initialization mechanism
            continue;
         }
         if (componentInstance.equals(o))
         {
            return unregisterComponent(componentAdapter.getComponentKey());
         }
      }
      return null;
   }

   /**
    * @deprecated since 1.1 - Use new VerifyingVisitor().traverse(this)
   */
   public void verify() throws PicoVerificationException
   {
      new VerifyingVisitor().traverse(this);
   }

   /**
    * Indicates whether or not the container can be started
    */
   protected boolean canBeStarted()
   {
      return !disposed.get() && !started.get();
   }

   /**
    * Indicates whether or not the container can be stopped
    */
   protected boolean canBeStopped()
   {
      return !disposed.get() && started.get();
   }

   /**
    * Indicates whether or not the container can be disposed
    */
   protected boolean canBeDisposed()
   {
      return !disposed.get();
   }
   
   /**
    * Start the components of this PicoContainer and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Startable} will be started.
    * @see #makeChildContainer()
    * @see #addChildContainer(PicoContainer)
    * @see #removeChildContainer(PicoContainer)
    */
   public void start()
   {
      if (!canBeStarted())
         return;
      LifecycleVisitor.start(this);
      started.set(true);
   }

   /**
    * Stop the components of this PicoContainer and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Startable} will be stopped.
    * @see #makeChildContainer()
    * @see #addChildContainer(PicoContainer)
    * @see #removeChildContainer(PicoContainer)
    */
   public void stop()
   {
      if (!canBeStopped())
         return;
      LifecycleVisitor.stop(this);
      started.set(false);
   }

   /**
    * Dispose the components of this PicoContainer and all its logical child containers.
    * Any component implementing the lifecycle interface {@link org.picocontainer.Disposable} will be disposed.
    * @see #makeChildContainer()
    * @see #addChildContainer(PicoContainer)
    * @see #removeChildContainer(PicoContainer)
    */
   public void dispose()
   {
      if (!canBeDisposed())
         return;
      LifecycleVisitor.dispose(this);
      disposed.set(true);
   }

   public MutablePicoContainer makeChildContainer()
   {
      DefaultPicoContainer pc = new DefaultPicoContainer(componentAdapterFactory, this);
      addChildContainer(pc);
      return pc;
   }

   public boolean addChildContainer(PicoContainer child)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      return children.add(child);
   }

   public boolean removeChildContainer(PicoContainer child)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     

      return children.remove(child);
   }

   public void accept(PicoVisitor visitor)
   {
      visitor.visitContainer(this);
      for (Iterator<ComponentAdapter> iterator = componentAdapters.iterator(); iterator.hasNext();)
      {
         ComponentAdapter componentAdapter = iterator.next();
         componentAdapter.accept(visitor);
      }
      for (Iterator<PicoContainer> iterator = children.iterator(); iterator.hasNext();)
      {
         PicoContainer child = iterator.next();
         child.accept(visitor);
      }
   }

   /**
    * Accepts a visitor that should visit the child containers only.
    */
   protected void accept(ContainerVisitor visitor)
   {
      visitor.visitContainer(this);
      for (Iterator<PicoContainer> iterator = children.iterator(); iterator.hasNext();)
      {
         PicoContainer child = iterator.next();
         child.accept(visitor);
      }
   }

   /**
    * Cans be used to indicate that we only want to visit Containers
    */
   protected static abstract class ContainerVisitor extends AbstractPicoVisitor
   {
      public final void visitComponentAdapter(ComponentAdapter componentAdapter)
      {
      }

      public final void visitParameter(Parameter parameter)
      {
      }
   }
}
