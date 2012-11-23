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

import java.lang.reflect.Constructor;
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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.component.ComponentLifecyclePlugin;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.management.ManageableContainer;
import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.container.tenant.TenantsContainerContext;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Date: Jul 18, 2004 Time: 12:15:28 AM
 */
public class ExoContainer extends ManageableContainer
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

      //
      return Collections.unmodifiableSet(profiles);      
   }
   
   protected static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ExoContainer");

   private Map<String, ComponentLifecyclePlugin> componentLifecylePlugin_ =
      new HashMap<String, ComponentLifecyclePlugin>();

   private List<ContainerLifecyclePlugin> containerLifecyclePlugin_ = new ArrayList<ContainerLifecyclePlugin>();

   protected ExoContainerContext context;

   protected PicoContainer parent;

   public ExoContainer()
   {
      context = new ExoContainerContext(this, this.getClass().getSimpleName());
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            registerComponentInstance(context);
            return null;
         }
      });
      
      this.parent = null;
   }

   public ExoContainer(PicoContainer parent)
   {
      super(parent);
      context = new ExoContainerContext(this, this.getClass().getSimpleName());
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            registerComponentInstance(context);
            return null;
         }
      });
      this.parent = parent;
   }

   public ExoContainer(ComponentAdapterFactory factory, PicoContainer parent)
   {
      super(factory, parent);
      context = new ExoContainerContext(this, this.getClass().getSimpleName());
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            registerComponentInstance(context);
            return null;
         }
      });
      this.parent = parent;
   }

   protected ExoContainer(PicoContainer parent, boolean initContext)
   {
      super(parent);
      if (initContext) 
      {
        context = new ExoContainerContext(this, this.getClass().getSimpleName());
        SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
        {
           public Void run()
           {
              registerComponentInstance(context);
              return null;
           }
        });
      }
      this.parent = parent;
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
   
   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated
   public void initContainer() throws Exception
   {      
   }
      
   private void initContainerInternal()
   {
      ConfigurationManager manager = (ConfigurationManager)getComponentInstanceOfType(ConfigurationManager.class);
      tenantsContainerContext = ContainerUtil.createTenantsContext(this, manager);
      if (tenantsContainerContext != null)
      {
         registerComponentInstance(tenantsContainerContext);
      }
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

   @Override
   public synchronized void dispose()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);
      
      if (canBeDisposed())
      {
         destroyContainerInternal();
         super.dispose();         
      }
   }

   /**
    * Starts the container
    * @param init indicates if the container must be initialized first
    */
   public synchronized void start(boolean init)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);
      
      if (init)
      {
         // Initialize the container first
         initContainerInternal();
      }
      start();
   }
   
   @Override
   public synchronized void start()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);
      
      if (canBeStarted())
      {
         super.start();
         startContainerInternal();         
      }
   }

   @Override
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
      }
   }

   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated 
   public void startContainer() throws Exception
   {      
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

   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated 
   public void stopContainer() throws Exception
   {      
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

   /**
    * Explicit calls are not allowed anymore
    */
   @Deprecated 
   public void destroyContainer() throws Exception
   {      
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

   public <T> T createComponent(Class<T> clazz) throws Exception
   {
      return createComponent(clazz, null);
   }

   public <T> T createComponent(Class<T> clazz, InitParams params) throws Exception
   {
      if (LOG.isDebugEnabled())
         LOG.debug(clazz.getName() + " " + ((params != null) ? params : "") + " added to " + getContext().getName());
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
               args[i] = getComponentInstanceOfType(parameters[i]);
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
      Collection<ComponentAdapter> adapters = getComponentAdapters();
      for (ComponentAdapter adapter : adapters)
      {
         unregisterComponent(adapter.getComponentKey());
      }
   }
}
