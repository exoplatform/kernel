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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.enterprise.context.spi.CreationalContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.inject.Qualifier;
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

   private volatile CachingContainer cache;

   /**
    * Context used to keep in memory the components that are currently being created.
    * This context is used to prevent cyclic resolution due to component plugins.
    */
   private final transient ThreadLocal<Map<Object, CreationalContextComponentAdapter<?>>> depResolutionCtx =
      new ThreadLocal<Map<Object, CreationalContextComponentAdapter<?>>>();

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

   @SuppressWarnings("unchecked")
   public <T> ComponentAdapter<T> getComponentAdapter(Object componentKey, Class<T> bindType) throws ContainerException
   {
      ComponentAdapter<?> adapter = componentKeyToAdapterCache.get(componentKey);
      if (adapter == null && parent != null)
      {
         adapter = parent.getComponentAdapter(componentKey, bindType);
      }
      if (adapter != null && !bindType.isAssignableFrom(adapter.getComponentImplementation()))
      {
         throw new ClassCastException("The adpater found is not of the expected type which was " + bindType.getName()
            + " and the implementation type is " + adapter.getComponentImplementation().getName());
      }
      return (ComponentAdapter<T>)adapter;
   }

   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType)
   {
      // See http://jira.codehaus.org/secure/ViewIssue.jspa?key=PICO-115
      ComponentAdapter<T> adapterByKey = (ComponentAdapter<T>)getComponentAdapter(componentType, componentType);
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
            if (getComponentInstance(contrivedKey, Object.class) != null)
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
            T componentInstance = getInstance((ComponentAdapter<T>)componentAdapter, componentType);
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

   public <T> T getComponentInstance(Object componentKey, Class<T> bindType) throws ContainerException
   {
      ComponentAdapter<T> componentAdapter = getComponentAdapter(componentKey, bindType);
      if (componentAdapter == null)
      {
         return null;
      }
      T value = getComponentInstanceFromContext(componentAdapter, bindType);
      return value != null ? value : getInstance(componentAdapter, bindType);
   }

   /**
    * Gets the component instance from the context
    */
   protected <T> T getComponentInstanceFromContext(ComponentAdapter<T> componentAdapter, Class<T> bindType)
   {
      Map<Object, CreationalContextComponentAdapter<?>> map = depResolutionCtx.get();
      if (map != null)
      {
         CreationalContextComponentAdapter<?> result = map.get(componentAdapter.getComponentKey());
         if (result != null && result.get() != null) 
         {
            // Don't keep in cache a component that has not been created yet
            getCache().disable();
            return bindType.cast(result.get());
         }
      }
      return null;
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
      T value = getComponentInstanceFromContext(componentAdapter, componentType);
      return value != null ? value : getInstance(componentAdapter, componentType);
   }

   /**
    * Add the {@link CreationalContext} corresponding to the given key, to the dependency resolution
    * context
    * @param key The key of the component to add to the resolution context
    */
   public <T> CreationalContextComponentAdapter<T> addComponentToCtx(Object key)
   {
      Map<Object, CreationalContextComponentAdapter<?>> map = depResolutionCtx.get();
      if (map == null)
      {
         map = new HashMap<Object, CreationalContextComponentAdapter<?>>();
         depResolutionCtx.set(map);
      }
      @SuppressWarnings("unchecked")
      CreationalContextComponentAdapter<T> result = (CreationalContextComponentAdapter<T>)map.get(key);
      if (result == null)
      {
         result = new CreationalContextComponentAdapter<T>();
         map.put(key, result);
      }
      return result;
   }

   /**
    * Remove the component corresponding to the given key, from the dependency resolution
    * context
    * @param key The key of the component to remove from the context
    */
   public void removeComponentFromCtx(Object key)
   {
      Map<Object, CreationalContextComponentAdapter<?>> map = depResolutionCtx.get();
      if (map != null)
      {
         map.remove(key);
         if (map.isEmpty())
         {
            depResolutionCtx.set(null);
         }
      }
   }

   private <T> T getInstance(ComponentAdapter<T> componentAdapter, Class<T> type)
   {
      // check whether this is our adapter
      // we need to check this to ensure up-down dependencies cannot be followed
      final boolean isLocal = componentAdapters.contains(componentAdapter);

      if (isLocal)
      {
         T instance = componentAdapter.getComponentInstance();

         addOrderedComponentAdapter(componentAdapter);

         if (!componentAdapter.isSingleton())
         {
            // Don't keep in cache a component that is not a singleton
            getCache().disable();
         }

         return instance;
      }
      else if (parent != null)
      {
         return parent.getComponentInstance(componentAdapter.getComponentKey(), type);
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
            if (parameters[i].equals(InitParams.class))
            {
               args[i] = params;
            }
            else
            {
               if (constructorWithInject)
               {
                  Object result =
                     resolveType(parameters[i], genericTypes[i], parameterAnnotations[i], logMessagePrefix);
                  if (!(result instanceof Integer))
                  {
                     args[i] = result;
                  }
               }
               else
               {
                  args[i] = holder.getComponentInstanceOfType(parameters[i]);
               }
               if (args[i] == null)
               {
                  satisfied = false;
                  unknownParameter = parameters[i];
                  break;
               }
            }
         }
         if (satisfied)
         {
            if ((!Modifier.isPublic(constructor.getModifiers()) || !Modifier.isPublic(constructor.getDeclaringClass()
               .getModifiers())) && !constructor.isAccessible())
               constructor.setAccessible(true);
            return clazz.cast(constructor.newInstance(args));
         }
      }
      throw new Exception("Cannot find a satisfying constructor for " + clazz.getName() + " with parameter "
         + unknownParameter);
   }

   /**
    * Initializes the instance by injecting objects into fields and the methods with the
    * annotation {@link Inject}
    * @return <code>true</code> if at least Inject annotation has been found, <code>false</code> otherwise
    */
   public <T> boolean initializeComponent(T instance)
   {
      LinkedList<Class<?>> hierarchy = new LinkedList<Class<?>>();
      Class<?> clazz = instance.getClass();
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
         if (initializeFields(instance, c))
         {
            isInjectPresent = true;
         }
         initializeMethods(instance, methodsPerClass.get(c));
      }
      return isInjectPresent;
   }

   private void addMethods(Class<?> c, Map<String, Method> methodAlreadyRegistered,
      Map<Class<?>, Collection<Method>> methodsPerClass)
   {
      Method[] methods = c.getDeclaredMethods();
      for (int i = 0, length = methods.length; i < length; i++)
      {
         Method m = methods[i];
         boolean addMethod = false;
         Method methodToRemove = null;
         if (m.isAnnotationPresent(Inject.class))
         {
            addMethod = true;
            methodToRemove = methodAlreadyRegistered.put(getMethodId(m), m);
         }
         else if (!methodAlreadyRegistered.isEmpty())
         {
            String id = getMethodId(m);
            if (methodAlreadyRegistered.containsKey(id))
            {
               addMethod = true;
               methodToRemove = methodAlreadyRegistered.put(id, m);
            }
         }
         if (addMethod)
         {
            Collection<Method> cMethods = methodsPerClass.get(c);
            if (cMethods == null)
            {
               cMethods = new HashSet<Method>();
               methodsPerClass.put(c, cMethods);
            }
            cMethods.add(m);
         }
         if (methodToRemove != null)
         {
            Collection<Method> cMethods = methodsPerClass.get(methodToRemove.getDeclaringClass());
            if (cMethods != null)
            {
               cMethods.remove(methodToRemove);
            }
         }
      }
   }

   /**
    * Initializes the instance by calling all the methods with the
    * annotation {@link Inject}
    */
   private <T> void initializeMethods(T instance, Collection<Method> methods)
   {
      if (methods == null)
      {
         return;
      }
      main : for (Method m : methods)
      {
         if (m.isAnnotationPresent(Inject.class))
         {
            if (Modifier.isAbstract(m.getModifiers()))
            {
               LOG.warn("Could not call the method " + m.getName() + " of the class " + instance.getClass().getName()
                  + ": The method cannot be abstract");
               continue;
            }
            else if (Modifier.isStatic(m.getModifiers()))
            {
               LOG.warn("Could not call the method " + m.getName() + " of the class " + instance.getClass().getName()
                  + ": The method cannot be static");
               continue;
            }
            // The method is annotated with Inject and is not abstract and has not been called yet
            Class<?>[] paramTypes = m.getParameterTypes();
            Object[] params = new Object[paramTypes.length];
            Type[] genericTypes = m.getGenericParameterTypes();
            Annotation[][] parameterAnnotations = m.getParameterAnnotations();
            String logMessagePrefix = null;
            if (LOG.isDebugEnabled())
            {
               logMessagePrefix =
                  "Could not call the method " + m.getName() + " of the class " + instance.getClass().getName();
            }
            for (int j = 0, l = paramTypes.length; j < l; j++)
            {
               Object result = resolveType(paramTypes[j], genericTypes[j], parameterAnnotations[j], logMessagePrefix);
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
                  params[j] = result;
               }
            }
            try
            {
               if ((!Modifier.isPublic(m.getModifiers()) || !Modifier.isPublic(m.getDeclaringClass().getModifiers()))
                  && !m.isAccessible())
                  m.setAccessible(true);
               m.invoke(instance, params);
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not call the method " + m.getName() + " of the class "
                  + instance.getClass().getName() + ": " + e.getMessage(), e);
            }
         }
      }
   }

   /**
    * Gives an id to a method to be able to identify if a given method has already been called from the parent class@return
    */
   private static String getMethodId(Method m)
   {
      StringBuilder sb = new StringBuilder();
      int modifier = m.getModifiers();
      if (Modifier.isPrivate(modifier))
      {
         sb.append(m.getDeclaringClass().getName());
      }
      else if (!Modifier.isPublic(modifier) && !Modifier.isProtected(modifier))
      {
         sb.append(m.getDeclaringClass().getPackage().getName());
      }
      sb.append(m.getName());
      sb.append('(');
      Class<?>[] paramTypes = m.getParameterTypes();
      for (int i = 0, l = paramTypes.length; i < l; i++)
      {
         sb.append(paramTypes[i].getName());
         if (i < i - 1)
            sb.append(',');
      }
      sb.append(')');
      return sb.toString();
   }

   /**
    * Initializes the fields of the instance by injecting objects into fields with the
    * annotation {@link Inject} for a given class
    */
   private <T> boolean initializeFields(T instance, Class<?> clazz)
   {
      boolean isInjectPresent = false;
      Field[] fields = clazz.getDeclaredFields();
      for (int i = 0, length = fields.length; i < length; i++)
      {
         Field f = fields[i];
         if (f.isAnnotationPresent(Inject.class))
         {
            isInjectPresent = true;
            if (Modifier.isFinal(f.getModifiers()))
            {
               LOG.warn("Could not set a value to the field " + f.getName() + " of the class "
                  + instance.getClass().getName() + ": The field cannot be final");
               continue;
            }
            else if (Modifier.isStatic(f.getModifiers()))
            {
               LOG.warn("Could not set a value to the field " + f.getName() + " of the class "
                  + instance.getClass().getName() + ": The field cannot be static");
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
                     "Could not set a value to the field " + f.getName() + " of the class "
                        + instance.getClass().getName();
               }
               Object result = resolveType(f.getType(), f.getGenericType(), f.getAnnotations(), logMessagePrefix);
               if (result instanceof Integer)
               {
                  continue;
               }
               f.set(instance, result);
            }
            catch (Exception e)
            {
               throw new RuntimeException("Could not set a value to the field " + f.getName() + " of the class "
                  + instance.getClass().getName() + ": " + e.getMessage(), e);
            }
         }
      }
      return isInjectPresent;
   }

   /**
    * Resolves the given type and generic type
    */
   private Object resolveType(Class<?> type, Type genericType, Annotation[] annotations, String logMessagePrefix)
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
         if (named != null)
         {
            adapter = holder.getComponentAdapter(named.value(), expectedType);
         }
         else if (qualifier != null)
         {
            adapter = holder.getComponentAdapter(qualifier, expectedType);
         }
         else
         {
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
         return new Provider<Object>()
         {
            public Object get()
            {
               return adapter.getComponentInstance();
            }
         };
      }
      else
      {
         if (named != null)
         {
            return holder.getComponentInstance(named.value(), type);
         }
         else if (qualifier != null)
         {
            return holder.getComponentInstance(qualifier, type);
         }
         else
         {
            return holder.getComponentInstanceOfType(type);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public void initialize()
   {
   }

   /**
    * Gives the cache if already found otherwise it will get it from the interceptor chain
    */
   protected CachingContainer getCache()
   {
      if (cache != null)
         return cache;
      Container co = holder;
      do
      {
         if (co instanceof CachingContainer)
         {
            cache = (CachingContainer)co;
         }
      }
      while ((co = co.getSuccessor()) != null);
      return cache;
   }

   /**
    * This class is used as value holder
    */
   public static class CreationalContextComponentAdapter<T> implements CreationalContext<T>
   {
      /**
       * The current value;
       */
      private T instance;

      /**
       * {@inheritDoc}
       */
      public void push(T incompleteInstance)
      {
         this.instance = incompleteInstance;
      }

      /**
       * {@inheritDoc}
       */
      public void release()
      {
      }

      /**
       * Gives the current value
       */
      public T get()
      {
         return instance;
      }
   }
}
