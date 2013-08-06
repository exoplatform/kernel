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
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.component.ComponentLifecyclePlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.context.ContextManager;
import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.InterceptorChainFactoryProvider;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by The eXo Platform SAS<br>
 * The Exo Container is an object responsible for loading services/components.
 * The eXoContainer class is inherited by all the containers, including RootContainer, PortalContainer,
 * and StandaloneContainer. It itself inherits from a {@link Container} which allows eXo to apply
 * the Inversion of Control (also known as IoC) principles.
 *
 * @author <a href="mailto:tuan08@users.sourceforge.net">Tuan Nguyen</a>
 * @LevelAPI Provisional
 */
public class ExoContainer extends AbstractContainer
{

   /**
    * Serial Version UID
    */
   private static final long serialVersionUID = -8068506531004854036L;

   /**
    * The current list of profiles
    */
   private static volatile String PROFILES;

   /**
    * The current set of profiles
    */
   private static Set<String> SET_PROFILES = Collections.unmodifiableSet(new HashSet<String>());

   protected final AtomicBoolean stopping = new AtomicBoolean();

   private final AtomicBoolean started = new AtomicBoolean();

   private final AtomicBoolean disposed = new AtomicBoolean();

   private final AtomicBoolean initialized = new AtomicBoolean();

   private final AtomicBoolean ctxManagerLoaded = new AtomicBoolean();

   private ContextManager ctxManager;

   /**
    * Returns an unmodifiable set of profiles defined by the value returned by invoking
    * {@link PropertyManager#getProperty(String)} with the {@link org.exoplatform.commons.utils.PropertyManager#RUNTIME_PROFILES}
    * property.
    *
    * @return the set of profiles
    */
   public static Set<String> getProfiles()
   {
      String profiles = PropertyManager.getProperty(PropertyManager.RUNTIME_PROFILES);
      if ((profiles == null && PROFILES != null) || (profiles != null && !profiles.equals(PROFILES)))
      {
         synchronized (ExoContainer.class)
         {
            if ((profiles == null && PROFILES != null) || (profiles != null && !profiles.equals(PROFILES)))
            {
               SET_PROFILES = getProfiles(profiles);
               PROFILES = profiles;
            }
         }
      }
      return SET_PROFILES;
   }

   /**
    * Indicates whether or not a given profile exists
    * @param profileName the name of the profile to check
    * @return <code>true</code> if the profile exists, <code>false</code> otherwise.
    */
   public static boolean hasProfile(String profileName)
   {
      return getProfiles().contains(profileName);
   }

   /**
    * Convert the list of profiles into a Set of String
    */
   private static Set<String> getProfiles(String profileList)
   {
      Set<String> profiles = new HashSet<String>();

      // Obtain profile list by runtime properties
      if (profileList != null)
      {
         for (String profile : profileList.split(","))
         {
            profiles.add(profile.trim());
         }
      }
      return Collections.unmodifiableSet(profiles);
   }

   protected static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ExoContainer");

   private final Map<String, ComponentLifecyclePlugin> componentLifecylePlugin_ =
      new HashMap<String, ComponentLifecyclePlugin>();

   private final List<ContainerLifecyclePlugin> containerLifecyclePlugin_ = new ArrayList<ContainerLifecyclePlugin>();

   protected final ExoContainerContext context;

   protected final ExoContainer parent;

   public ExoContainer()
   {
      this(null);
   }

   public ExoContainer(ExoContainer parent)
   {
      this.context = new ExoContainerContext(this, this.getClass().getSimpleName());
      this.parent = parent;
      this.delegate = InterceptorChainFactoryProvider.getInterceptorChainFactory().getInterceptorChain(this, parent);
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            registerComponentInstance(context);
            return null;
         }
      });
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
            LOG.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
   }

   public synchronized void dispose()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);

      if (canBeDisposed())
      {
         destroyContainerInternal();
         super.dispose();
         disposed.set(true);
      }
   }

   /**
    * Starts the container
    * @param init indicates if the container must be initialized first
    */
   public synchronized void start(boolean init)
   {
      if (init)
      {
         initialize();
      }
      start();
   }

   public synchronized void initialize()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);

      if (canBeInitialized())
      {
         // Initialize the successors
         super.initialize();
         // Initialize the container
         initContainerInternal();
         initialized.set(true);
      }
   }

   public synchronized void start()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);

      if (canBeStarted())
      {
         super.start();
         startContainerInternal();
         started.set(true);
      }
   }

   public synchronized void stop()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);

      if (canBeStopped())
      {
         stopping.set(true);
         stopContainerInternal();
         super.stop();
         started.set(false);
      }
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
            LOG.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
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
            LOG.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
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
            LOG.warn("An error occurs with the ContainerLifecyclePlugin '" + getPluginName(plugin) + "'", e);
         }
      }
   }

   public void addComponentLifecylePlugin(ComponentLifecyclePlugin plugin)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);

      List<String> list = plugin.getManageableComponents();
      for (String component : list)
         componentLifecylePlugin_.put(component, plugin);
   }

   public void addContainerLifecylePlugin(ContainerLifecyclePlugin plugin)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);

      containerLifecyclePlugin_.add(plugin);
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
    * Unregister all components from container to avoid keeping instances in memory.
    */
   protected void unregisterAllComponents()
   {
      Collection<ComponentAdapter<?>> adapters = getComponentAdapters();
      for (ComponentAdapter<?> adapter : adapters)
      {
         unregisterComponent(adapter.getComponentKey());
      }
   }

   /**
    * Register a component using the componentImplementation as key. Calling this method is equivalent to calling
    * <code>registerComponentImplementation(componentImplementation, componentImplementation)</code>.
    *
    * @param componentImplementation the concrete component class.
    * @return the ComponentAdapter that has been associated with this component. In the majority of cases, this return
    *         value can be safely ignored, as one of the <code>getXXX()</code> methods of the
    *         {@link Container} interface can be used to retrieve a reference to the component later on.
    * @throws ContainerException if registration fails.
    */
   public <T> ComponentAdapter<T> registerComponentImplementation(Class<T> componentImplementation)
      throws ContainerException
   {
      return registerComponentImplementation(componentImplementation, componentImplementation);
   }

   /**
    * Register an arbitrary object. The class of the object will be used as a key. Calling this method is equivalent to
    * calling     * <code>registerComponentImplementation(componentImplementation, componentImplementation)</code>.
    *
    * @param componentInstance
    * @return the ComponentAdapter that has been associated with this component. In the majority of cases, this return
    *         value can be safely ignored, as one of the <code>getXXX()</code> methods of the
    *         {@link Container} interface can be used to retrieve a reference to the component later on.
    * @throws ContainerException if registration fails.
    */
   public <T> ComponentAdapter<T> registerComponentInstance(T componentInstance) throws ContainerException
   {
      return registerComponentInstance(componentInstance.getClass(), componentInstance);
   }

   /**
    * Creates a component corresponding to the given {@link Class} with no parameters
    * This is equivalent to call {@link #createComponent(Class, InitParams)} with
    * <code>null</code> as {@link InitParams}
    * @param clazz the Class of the object to create
    * @return an instance of the component
    * @throws Exception if any issue occurs while creating the component.
    */
   public <T> T createComponent(Class<T> clazz) throws Exception
   {
      return createComponent(clazz, null);
   }

   /**
    * Find a component adapter associated with the specified key. If a component adapter cannot be found in this
    * container, the parent container (if one exists) will be searched.
    * 
    * @param componentKey the key that the component was registered with.
    * @return the component adapter associated with this key, or <code>null</code> if no component has been registered
    *         for the specified key.
    */
   public ComponentAdapter<?> getComponentAdapter(Object componentKey)
   {
      return getComponentAdapter(componentKey, Object.class);
   }

   /**
    * Retrieve a component instance registered with a specific key. If a component cannot be found in this container,
    * the parent container (if one exists) will be searched.
    * 
    * @param componentKey the key that the component was registered with.
    * @return an instantiated component, or <code>null</code> if no component has been registered for the specified
    *         key.
    */
   public Object getComponentInstance(Object componentKey)
   {
      return getComponentInstance(componentKey, Object.class);
   }

   @Managed
   @ManagedName("RegisteredComponentNames")
   @ManagedDescription("Return the list of the registered component names")
   public Set<String> getRegisteredComponentNames() throws ContainerException
   {
      Set<String> names = new HashSet<String>();
      Collection<ComponentAdapter<?>> adapters = getComponentAdapters();
      for (ComponentAdapter<?> adapter : adapters)
      {
         Object key = adapter.getComponentKey();
         String name = String.valueOf(key);
         names.add(name);
      }
      return names;
   }

   /**
    * Gives the parent container of this container.
    * 
    * @return a {@link ExoContainer} instance, or <code>null</code> if this container does not have a parent.
    */
   public ExoContainer getParent()
   {
      return parent;
   }

   /**
    * Indicates whether or not the container can be started
    * @return <code>true</code> if it can be started, <code>false</code> otherwise.
    */
   public boolean canBeStarted()
   {
      return !disposed.get() && !started.get();
   }

   /**
    * Indicates whether or not the container can be stopped
    * @return <code>true</code> if it can be stopped, <code>false</code> otherwise.
    */
   public boolean canBeStopped()
   {
      return !disposed.get() && started.get();
   }

   /**
    * Indicates whether or not the container can be disposed
    * @return <code>true</code> if it can be disposed, <code>false</code> otherwise.
    */
   public boolean canBeDisposed()
   {
      return !disposed.get();
   }

   /**
    * Indicates whether or not the container can be initialized
    * @return <code>true</code> if it can be initialized, <code>false</code> otherwise.
    */
   protected boolean canBeInitialized()
   {
      return !initialized.get();
   }

   /**
    * Gives the {@link ContextManager} that has been registered
    * @return the {@link ContextManager} related to this container, <code>null</code> otherwise.
    */
   public ContextManager getContextManager()
   {
      if (ctxManagerLoaded.get())
         return ctxManager;
      synchronized (this)
      {
         if (ctxManagerLoaded.get())
            return ctxManager;
         ctxManager = getComponentInstanceOfType(ContextManager.class);
         ctxManagerLoaded.set(true);
      };
      return ctxManager;
   }

   /**
    * Indicates whether or not the {@link ContextManager} has already been loaded
    * @return <code>true</code> if the {@link ContextManager} has been loaded,
    * <code>false</code> otherwise.
    */
   public boolean isContextManagerLoaded()
   {
      return ctxManagerLoaded.get();
   }
}
