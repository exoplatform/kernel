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
package org.exoplatform.container.definition;

import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.PropertyConfigurator;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.monitor.jvm.J2EEServerInfo;
import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.container.util.ContainerUtil;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * This class is used to define the configuration related to the portal containers themselves. 
 * It is mainly dedicated to the {@link RootContainer} to allows to understand how to manage and 
 * deploy all the portal containers
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 26 ao�t 2009  
 */
public class PortalContainerConfig implements Startable
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.PortalContainerConfig");// NOSONAR

   /**
    * The name of the setting corresponding to the portal container name
    */
   public static final String PORTAL_CONTAINER_SETTING_NAME = "name";

   /**
    * The name of the setting corresponding to the rest context name
    */
   public static final String REST_CONTEXT_SETTING_NAME = "rest";

   /**
    * The name of the setting corresponding to the relam name
    */
   public static final String REALM_SETTING_NAME = "realm";

   /**
    * The default name of a portal container
    */
   // We use new String to create a new object in order to use the operator ==
   public static final String DEFAULT_PORTAL_CONTAINER_NAME = new String("portal");

   /**
    * The default name of a the {@link ServletContext} of the rest web application
    */
   // We use new String to create a new object in order to use the operator ==
   public static final String DEFAULT_REST_CONTEXT_NAME = new String("rest");

   /**
    * The default realm name
    */
   // We use new String to create a new object in order to use the operator ==
   public static final String DEFAULT_REALM_NAME = new String("exo-domain");

   /**
    * The default {@link PortalContainerDefinition} 
    */
   private final PortalContainerDefinition defaultDefinition;

   /**
    * Indicates if the component has already been initialized
    */
   private volatile boolean initialized;

   /**
    * The list of all the portal containers
    */
   private List<String> portalContainerNames = Collections.unmodifiableList(new ArrayList<String>());

   /**
    * The set of all the portal containers that have been disabled
    */
   private Set<String> portalContainerNamesDisabled = Collections.unmodifiableSet(new HashSet<String>());

   /**
    * The list of all the web application scopes
    */
   private Map<String, List<String>> scopes = Collections.unmodifiableMap(new HashMap<String, List<String>>());

   /**
    * The list of all the {@link PortalContainerDefinition} that have been registered
    */
   private Map<String, PortalContainerDefinition> definitions =
      Collections.unmodifiableMap(new HashMap<String, PortalContainerDefinition>());

   /**
    * The list of all the changes to apply to the registered {@link PortalContainerDefinition}
    */
   private List<PortalContainerDefinitionChangePlugin> changes = new ArrayList<PortalContainerDefinitionChangePlugin>();

   /**
    * The configuration manager
    */
   private final ConfigurationManager cm;

   /**
    * Some info about the current server
    */
   private final J2EEServerInfo serverInfo;

   /**
    * Indicates if new system properties have been added
    */
   private final PropertyConfigurator pc;
   
   /**
    * Indicates whether the unregistered webapps have to be ignored. If a webapp has not been registered as a dependency
    * of any portal containers, we will use the value of this parameter to know what to do.
    * If it is set to false, this webapp will be considered by default as a dependency of all the portal containers.
    * If it is set to true, this webapp won't be considered by default as a dependency of any portal container, it will
    * be simply ignored.
    */
   private final boolean ignoreUnregisteredWebapp;

   public PortalContainerConfig(ConfigurationManager cm)
   {
      this(null, cm, new J2EEServerInfo(), null);
   }

   public PortalContainerConfig(ConfigurationManager cm, PropertyConfigurator pc)
   {
      this(null, cm, new J2EEServerInfo(), pc);
   }

   public PortalContainerConfig(ConfigurationManager cm, J2EEServerInfo serverInfo)
   {
      this(null, cm, serverInfo, null);
   }

   public PortalContainerConfig(ConfigurationManager cm, J2EEServerInfo serverInfo, PropertyConfigurator pc)
   {
      this(null, cm, serverInfo, pc);
   }

   public PortalContainerConfig(InitParams params, ConfigurationManager cm)
   {
      this(params, cm, new J2EEServerInfo(), null);
   }

   public PortalContainerConfig(InitParams params, ConfigurationManager cm, PropertyConfigurator pc)
   {
      this(params, cm, new J2EEServerInfo(), pc);
   }

   public PortalContainerConfig(InitParams params, ConfigurationManager cm, J2EEServerInfo serverInfo)
   {
      this(params, cm, serverInfo, null);
   }

   /**
    * We add the {@link PropertyConfigurator} in the constructor, in order to make sure that it is
    * created before since it could define some JVM parameters that could be used internally by the
    * {@link PortalContainerConfig}
    */
   public PortalContainerConfig(InitParams params, ConfigurationManager cm, J2EEServerInfo serverInfo,
      PropertyConfigurator pc)
   {
      this.pc = pc;
      this.cm = cm;
      this.serverInfo = serverInfo;
      this.defaultDefinition = create(params);
      this.ignoreUnregisteredWebapp =
         params != null
            && params.getValueParam("ignore.unregistered.webapp") != null
            && Boolean.valueOf(Deserializer.resolveVariables(params.getValueParam("ignore.unregistered.webapp")
               .getValue()));
   }

   /**
    * Create the default {@link PortalContainerDefinition} corresponding to the given parameters
    * @param params the parameter to initialize
    */
   private PortalContainerDefinition create(InitParams params)
   {
      ObjectParameter oDpd = null;
      if (params != null)
      {
         oDpd = params.getObjectParam("default.portal.definition");
      }
      PortalContainerDefinition def = null;
      if (oDpd != null)
      {
         // A default portal definition has been found
         final Object o = oDpd.getObject();
         if (o instanceof PortalContainerDefinition)
         {
            // The nested object is of the right type
            def = (PortalContainerDefinition)o;
         }
         else
         {
            // The nested object is not of the right type, thus it will be ignored
            LOG.warn("The object parameter 'default.portal.definition' should be of type "
               + PortalContainerDefinition.class);
         }
      }
      if (def == null)
      {
         def = new PortalContainerDefinition();
      }
      initName(params, def);
      initRestContextName(params, def);
      initRealmName(params, def);
      initializeSettings(def, false);
      return def;
   }

   /**
    * Initialize the value of the realm name
    */
   private void initRealmName(InitParams params, PortalContainerDefinition def)
   {
      if (def.getRealmName() == null || def.getRealmName().trim().length() == 0)
      {
         // The realm name is empty
         // We first set the default value
         def.setRealmName(DEFAULT_REALM_NAME);
         if (params == null)
         {
            return;
         }
         final ValueParam vp = params.getValueParam("default.realm.name");
         if (vp != null && vp.getValue().trim().length() > 0)
         {
            // A realm name has been defined in the value parameter, thus we use it
            def.setRealmName(Deserializer.resolveVariables(vp.getValue().trim()));
         }
      }
      else
      {
         // We ensure that the realm name doesn't contain any useless characters
         def.setRealmName(def.getRealmName().trim());
      }
   }

   /**
    * Initialize the value of the rest context name
    */
   private void initRestContextName(InitParams params, PortalContainerDefinition def)
   {
      if (def.getRestContextName() == null || def.getRestContextName().trim().length() == 0)
      {
         // The rest context name is empty
         // We first set the default value
         def.setRestContextName(DEFAULT_REST_CONTEXT_NAME);
         if (params == null)
         {
            return;
         }
         final ValueParam vp = params.getValueParam("default.rest.context");
         if (vp != null && vp.getValue().trim().length() > 0)
         {
            // A rest context name has been defined in the value parameter, thus we use it
            def.setRestContextName(Deserializer.resolveVariables(vp.getValue().trim()));
         }
      }
      else
      {
         // We ensure that the rest context name doesn't contain any useless characters
         def.setRestContextName(def.getRestContextName().trim());
      }
   }

   /**
    * Initialize the value of the portal container name
    */
   private void initName(InitParams params, PortalContainerDefinition def)
   {
      if (def.getName() == null || def.getName().trim().length() == 0)
      {
         // The name is empty
         // We first set the default value
         def.setName(DEFAULT_PORTAL_CONTAINER_NAME);
         if (params == null)
         {
            return;
         }
         final ValueParam vp = params.getValueParam("default.portal.container");
         if (vp != null && vp.getValue().trim().length() > 0)
         {
            // A name has been defined in the value parameter, thus we use it
            def.setName(Deserializer.resolveVariables(vp.getValue().trim()));
         }
      }
      else
      {
         // We ensure that the name doesn't contain any useless characters
         def.setName(def.getName().trim());
      }
   }

   /**
    * @return the default name of the portal container
    */
   public String getDefaultPortalContainer()
   {
      return defaultDefinition.getName();
   }

   /**
    * @return the default name of the rest {@link ServletContext}
    */
   public String getDefaultRestContext()
   {
      return defaultDefinition.getRestContextName();
   }

   /**
    * @return the default name of the realm
    */
   public String getDefaultRealmName()
   {
      return defaultDefinition.getRealmName();
   }

   /**
    * Indicates if at least one portal container definition has been defined. If no portal definition
    * has been defined, we assume that we want the old behavior
    * @return <code>true</code> if at least one definition has been set, <code>false</code> otherwise
    */
   public boolean hasDefinition()
   {
      return !definitions.isEmpty();
   }
   
   /**
    * Disables a portal container if it has not yet been disabled.
    * @param name the name of the portal container to disable
    */
   public synchronized void disablePortalContainer(String name)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      if (!portalContainerNamesDisabled.contains(name))
      {
         if (PropertyManager.isDevelopping())
         {
            LOG.info("The portal container '" + name + "' will be disabled");
         }
         final Set<String> lPortalContainerNames = new HashSet<String>(portalContainerNamesDisabled.size() + 1);
         lPortalContainerNames.add(name);
         lPortalContainerNames.addAll(portalContainerNamesDisabled);
         this.portalContainerNamesDisabled = Collections.unmodifiableSet(lPortalContainerNames);
         if (hasDefinition())
         {
            // The new behavior is expected
            // Remove the portal container from the registered portal container list
            unregisterPortalContainerName(name);
         }
      }
   }
   
   /**
    * Remove the given portal container name from all the existing scopes
    * 
    * @param name the name of the portal container to remove from all the scopes
    */
   private void removePortalContainerNameFromScopes(String name)
   {
      final Map<String, List<String>> tmpScopes = new HashMap<String, List<String>>(scopes);
      boolean changed = false;
      for (String ctx : tmpScopes.keySet())
      {
         List<String> portalContainerNames = tmpScopes.get(ctx);
         if (portalContainerNames.contains(name))
         {
            List<String> tmpPortalContainerNames = new ArrayList<String>(portalContainerNames);
            tmpPortalContainerNames.remove(name);
            tmpScopes.put(ctx, Collections.unmodifiableList(tmpPortalContainerNames));
            changed = true;
         }
      }
      if (changed)
      {
         this.scopes = Collections.unmodifiableMap(tmpScopes);
      }
   }

   /**
    * Indicates if the given name is the name of a portal container that has been registered as disabled
    * @param name the name to check
    * @return <code>true</code> if the name is a name of a disabled portal container, <code>false</code>
    *        otherwise.
    */
   public boolean isPortalContainerNameDisabled(String name)
   {
      return name == null ? false : portalContainerNamesDisabled.contains(name);
   }
   
   /**
    * Registers a name of a portal container if it has not yet been registered
    * @param name the name of the portal container to register
    */
   public synchronized void registerPortalContainerName(String name)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      if (!portalContainerNames.contains(name) && !portalContainerNamesDisabled.contains(name))
      {
         final List<String> lPortalContainerNames = new ArrayList<String>(portalContainerNames.size() + 1);
         lPortalContainerNames.add(name);
         lPortalContainerNames.addAll(portalContainerNames);
         this.portalContainerNames = Collections.unmodifiableList(lPortalContainerNames);
      }
   }

   /**
    * Unregisters a name of a portal container if it has not yet been unregistered
    * @param name the name of the portal container to register
    */
   public synchronized void unregisterPortalContainerName(String name)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      
      if (portalContainerNames.contains(name))
      {
         final List<String> lPortalContainerNames = new ArrayList<String>(portalContainerNames);
         lPortalContainerNames.remove(name);
         this.portalContainerNames = Collections.unmodifiableList(lPortalContainerNames);
         if (hasDefinition())
         {
            // The new behavior is expected
            // Remove the portal container from all the scopes
            removePortalContainerNameFromScopes(name);
         }         
      }
   }

   /**
    * Indicates if the given name is the name of a registered portal container
    * @param name the name to check
    * @return <code>true</code> if the name is a name of a portal container, <code>false</code>
    *        otherwise.
    */
   public boolean isPortalContainerName(String name)
   {
      return name == null ? false : portalContainerNames.contains(name);
   }

   /**
    * Gives the list of all the portal container names for which the web application is available
    * @param contextName the context name of the web application
    * @return the list of all the portal container names for which the web application is available
    */
   public List<String> getPortalContainerNames(String contextName)
   {
      if (contextName == null)
      {
         throw new IllegalArgumentException("The context name cannot be null");
      }
      if (definitions.isEmpty())
      {
         // we assume that the old behavior is expected         
         final String portalContainerName =
            portalContainerNames.contains(contextName) ? contextName : defaultDefinition.getName();
         return Collections.singletonList(portalContainerName);
      }
      final List<String> result = scopes.get(contextName);
      if (result == null || result.isEmpty())
      {
         // This context has not been added as dependency of any portal containers
         if (portalContainerNames.contains(contextName))
         {
            // The given context is a portal container
            return Collections.singletonList(contextName);
         }
         else if (portalContainerNamesDisabled.contains(contextName) || ignoreUnregisteredWebapp)
         {
            // The given context name is a context name of a disabled portal container or
            // the webapp is ignored since it has not been registered in any portal container dependency list
            return Collections.emptyList();
         }
         // By default we scope it to all the portal containers
         return portalContainerNames;
      }
      return result;
   }

   /**
    * Gives the portal container names for which the web application is available if several
    * portal container are available only the fist one will be returned
    * @param contextName the context name of the web application
    * @return the portal container names for which the web application is available
    */
   public String getPortalContainerName(String contextName)
   {
      if (contextName == null)
      {
         throw new IllegalArgumentException("The context name cannot be null");
      }
      if (portalContainerNames.contains(contextName))
      {
         // The given context name is a context name of a portal container
         return contextName;
      }
      else if (definitions.isEmpty())
      {
         // we assume that the old behavior is expected         
         return defaultDefinition.getName();
      }
      final List<String> result = scopes.get(contextName);
      if (result == null || result.isEmpty())
      {
         // This context has not been added as dependency of any portal containers
         if (portalContainerNamesDisabled.contains(contextName) || ignoreUnregisteredWebapp)
         {
            // The given context name is a context name of a disabled portal container or
            // the webapp is ignored since it has not been registered in any portal container dependency list
            if (PropertyManager.isDevelopping())
            {
               LOG.warn("The context '" + contextName + "' has not been added as " +
                     "dependency of any portal containers");
            }
            return null;
         }
         if (PropertyManager.isDevelopping())
         {
            LOG.debug("The context '" + contextName + "' has not been added as " +
               "dependency of any portal containers");
         }
         // by default we will return the default portal container
         return defaultDefinition.getName();
      }
      return result.get(0);
   }

   /**
    * Gives all the dependencies related to the given portal container
    * @param portalContainerName the name of the portal container for which we want the dependencies
    * @return a list of sorted context names
    */
   public List<String> getDependencies(String portalContainerName)
   {
      final PortalContainerDefinition definition = definitions.get(portalContainerName);
      if (definition != null)
      {
         // A definition has been defined
         List<String> result = definition.getDependencies();
         return result == null || result.isEmpty() ? defaultDefinition.getDependencies() : result;         
      }
      else if (definitions.isEmpty())
      {
         // The old behavior is expected
         return defaultDefinition.getDependencies();
      }
      return null;
   }

   /**
    * Give the value of a given setting for a given portal container name
    * @param portalContainerName the name of the portal container for which we want the value of the
    * setting
    * @param settingName the name of the setting that we seek
    * @return the value of the setting, <code>null</code> if it cans not be found.
    */
   public Object getSetting(String portalContainerName, String settingName)
   {
      if (settingName == null)
      {
         throw new IllegalArgumentException("The setting name cannot be null");
      }
      final PortalContainerDefinition definition = definitions.get(portalContainerName);
      if (definition != null)
      {
         final Map<String, Object> settings = definition.getSettings();
         if (settings != null && !settings.isEmpty())
         {
            return settings.get(settingName);
         }
      }
      final Map<String, Object> defaultSettings = defaultDefinition.getSettings();
      return defaultSettings == null ? null : defaultSettings.get(settingName);
   }

   /**
    * Gives the name of the rest {@link ServletContext} related to the given portal container
    * @param portalContainerName the name of the portal container for which we want the rest context name
    * @return the name of the related rest context name. It tries to get it from the {@link PortalContainerDefinition}
    * if it has not been set it will return <code>defaultDefinition.getRestContextName()</code> 
    */
   public String getRestContextName(String portalContainerName)
   {
      final PortalContainerDefinition definition = definitions.get(portalContainerName);
      if (definition == null)
      {
         return defaultDefinition.getRestContextName();
      }
      else
      {
         String contextName = definition.getRestContextName();
         return contextName == null ? defaultDefinition.getRestContextName() : contextName;
      }
   }

   /**
    * Gives the name of the realm related to the given portal container
    * @param portalContainerName the name of the portal container for which we want the realm name
    * @return the name of the related realm name. It tries to get it from the {@link PortalContainerDefinition}
    * if it has not been set it will return <code>defaultDefinition.getRealmName()</code> 
    */
   public String getRealmName(String portalContainerName)
   {
      final PortalContainerDefinition definition = definitions.get(portalContainerName);
      if (definition == null)
      {
         return defaultDefinition.getRealmName();
      }
      else
      {
         String realmName = definition.getRealmName();
         return realmName == null ? defaultDefinition.getRealmName() : realmName;
      }
   }

   /**
    * Indicates if the given servlet context is a dependency of the given portal container
    * @param portalContainerName the name of the portal container
    * @param contextName the name of the {@link ServletContext}
    * @return <code>true</code> if the dependencies matches, <code>false</code> otherwise;
    */
   public boolean isScopeValid(String portalContainerName, String contextName)
   {
      if (portalContainerName == null)
      {
         throw new IllegalArgumentException("The portal container name cannot be null");
      }
      return getPortalContainerNames(contextName).contains(portalContainerName);
   }

   /**
    * Allow to define a set of {@link PortalContainerDefinition}
    * @param plugin the plugin that contains all the {@link PortalContainerDefinition} to define
    */
   public void registerPlugin(PortalContainerDefinitionPlugin plugin)
   {
      final List<PortalContainerDefinition> lDefs = plugin.getPortalContainerDefinitions();
      if (lDefs != null && !lDefs.isEmpty())
      {
         synchronized (this)
         {
            if (initialized)
            {
               throw new IllegalStateException("The PortalContainerConfig has already been initialized");
            }
            final Map<String, PortalContainerDefinition> tempDefinitions =
               new LinkedHashMap<String, PortalContainerDefinition>(definitions);
            for (PortalContainerDefinition def : lDefs)
            {
               String name = def.getName();
               if (name == null || (name = name.trim()).length() == 0)
               {
                  LOG.warn("A PortalContainerDefinition cannot have an empty name");
                  continue;
               }
               else
               {
                  // Ensure that the name doesn't contain any useless characters 
                  def.setName(name);
               }
               tempDefinitions.put(name, def);
            }
            this.definitions = Collections.unmodifiableMap(tempDefinitions);
         }
      }
   }

   /**
    * Allow to disable a set of portal containers
    * @param plugin the plugin that defines the name of portal containers to disable
    */
   public void registerDisablePlugin(PortalContainerDefinitionDisablePlugin plugin)
   {
      final Set<String> sPortalContainerNames = plugin.getNames();
      if (sPortalContainerNames != null && !sPortalContainerNames.isEmpty())
      {
         synchronized (this)
         {
            if (initialized)
            {
               throw new IllegalStateException("The PortalContainerConfig has already been initialized");
            }
            for (String name : sPortalContainerNames)
            {
               disablePortalContainer(name);
            }
         }         
      }
   }
   
   /**
    * Allow to define a set of changes to apply to the registered {@link PortalContainerDefinition}
    * @param plugin the plugin that defines the changes to apply
    */
   public void registerChangePlugin(PortalContainerDefinitionChangePlugin plugin)
   {
      final List<PortalContainerDefinitionChange> lchanges = plugin.getChanges();
      if (lchanges != null && !lchanges.isEmpty())
      {
         synchronized (this)
         {
            if (initialized)
            {
               throw new IllegalStateException("The PortalContainerConfig has already been initialized");
            }
            changes.add(plugin);
         }
      }
   }

   /**
    * Construct the scopes of all the web applications from the given {@link PortalContainerDefinition}
    * @param definition the definition of a {@link PortalContainer} that contains the dependencies with
    * the web application
    * @param scopes the map in which the scope must be defined
    */
   private void registerDependencies(PortalContainerDefinition definition, Map<String, List<String>> scopes)
   {
      List<String> dependencies = definition.getDependencies();
      if (dependencies == null || dependencies.isEmpty())
      {
         // Try to get the default dependencies
         dependencies = defaultDefinition.getDependencies();
         if (dependencies == null || dependencies.isEmpty())
         {
            return;
         }
      }
      for (String context : dependencies)
      {
         if (context == null || (context = context.trim()).length() == 0)
         {
            continue;
         }
         List<String> lPortalContainerNames = scopes.get(context);
         if (lPortalContainerNames == null)
         {
            // There is no 
            lPortalContainerNames = new ArrayList<String>();
         }
         else
         {
            // The existing collection is unmodifiable thus we need to create a new one
            lPortalContainerNames = new ArrayList<String>(lPortalContainerNames);
         }
         lPortalContainerNames.add(definition.getName());
         scopes.put(context, Collections.unmodifiableList(lPortalContainerNames));
      }
   }

   /**
    * Initialize all the settings tied to the corresponding portal container. It will 
    * first initialize a new {@link Map} of settings from the settings retrieved from 
    * <code>PortalContainerDefinition.getSettings()</code>, then it will add the 
    * external settings corresponding the properties file found at the path 
    * <code>PortalContainerDefinition.getExternalSettingsPath()</code>, if such file 
    * exists. If the same key has been defined in both, the value defined in the 
    * external settings will be kept. Then we will add the main settings such as the 
    * portal container name, the realm name and the rest context name.
    * @param def the {@link PortalContainerDefinition} from which we have the extract the 
    * settings and in which we have to re-inject the final settings
    * @param addDefaultSettings indicates whether the settings of the default portal
    * container definition has to be loaded first
    */
   private void initializeSettings(PortalContainerDefinition def, boolean addDefaultSettings)
   {
      // The list of portal container definition for which we want to load the settings
      final PortalContainerDefinition[] defs;
      if (addDefaultSettings)
      {
         // We need to load the default settings then the settings of the current portal
         // container definition
         defs = new PortalContainerDefinition[]{defaultDefinition, def};
      }
      else
      {
         // We only need to load the settings of the current portal container definition
         defs = new PortalContainerDefinition[]{def};
      }
      final Map<String, Object> settings = new HashMap<String, Object>();
      loadInternalSettings(defs, settings);
      if (pc != null)
      {
         resolveInternalSettings(settings);
      }

      final Map<String, String> externalSettings = new LinkedHashMap<String, String>();
      loadExternalSettings(def, defs, externalSettings);
      if (!externalSettings.isEmpty())
      {
         resolveExternalSettings(def, settings, externalSettings);
         // Merge the settings
         mergeSettings(settings, externalSettings);
      }
      // We then add the main settings
      settings.putAll(getMainSettings(def));
      // We re-inject the settings and we make sure it is thread safe
      def.setSettings(Collections.unmodifiableMap(settings));
   }

   /**
    * Creates a context from the internal settings, external settings and the main settings and
    * try to resolve variables defined in the external settings
    */
   private void resolveExternalSettings(PortalContainerDefinition def, final Map<String, Object> settings,
      final Map<String, String> externalSettings)
   {
      // Create the context for variable resolution
      final Map<String, Object> ctx = new LinkedHashMap<String, Object>();
      ctx.putAll(settings);
      ctx.putAll(externalSettings);
      ctx.putAll(getMainSettings(def));
      // Resolve variables
      for (Map.Entry<String, String> entry : externalSettings.entrySet())
      {
         String propertyName = entry.getKey();
         String propertyValue = entry.getValue();
         propertyValue = Deserializer.resolveVariables(propertyValue, ctx);
         externalSettings.put(propertyName, propertyValue);
         ctx.put(propertyName, propertyValue);
      }
   }

   /**
    * Loads the external settings of all the given {@link PortalContainerDefinition}
    */
   private void loadExternalSettings(PortalContainerDefinition def, final PortalContainerDefinition[] defs,
      final Map<String, String> externalSettings)
   {
      for (PortalContainerDefinition pcd : defs)
      {
         // We then load the external settings, if they exists
         String path = pcd.getExternalSettingsPath();
         if (path != null && (path = path.trim()).length() > 0)
         {
            final Map<String, String> props =
               loadExternalSettings(path, defaultDefinition == null || pcd == defaultDefinition, def);// NOSONAR
            if (props != null && !props.isEmpty())
            {
               externalSettings.putAll(props);
            }
         }
      }
   }

   /**
    * Try to resolve all the String values to ensure that there is no variables unresolved
    * The {@link PropertyConfigurator} cans create new system property so it could be
    * necessary to resolve the {@link String} settings one more time
    */
   private void resolveInternalSettings(final Map<String, Object> settings)
   {
      // New System properties have been added so we will try to re-resolve the String variables
      for (Map.Entry<String, Object> entry : settings.entrySet())
      {
         String propertyName = entry.getKey();
         Object propertyValue = entry.getValue();
         if (propertyValue instanceof String)
         {
            propertyValue = Deserializer.resolveVariables((String)propertyValue);
            settings.put(propertyName, propertyValue);
         }
      }
   }

   /**
    * Loads all the internal settings related to the given array of {@link PortalContainerDefinition}
    */
   private void loadInternalSettings(final PortalContainerDefinition[] defs, final Map<String, Object> settings)
   {
      for (PortalContainerDefinition pcd : defs)
      {
         // We first load the internal settings if they exists
         final Map<String, Object> tmpSettings = pcd.getSettings();
         if (tmpSettings != null && !tmpSettings.isEmpty())
         {
            settings.putAll(tmpSettings);
         }
      }
   }

   /**
    * This method gives the main settings such as the portal container name, the rest context name
    * and the realm name into a {@link Map}
    * @param def the {@link PortalContainerDefinition} from which we extract the value of the main
    * settings, if a main setting is null, we use the default value.
    * @return A {@link Map} of settings including the main settings
    */
   private Map<String, String> getMainSettings(PortalContainerDefinition def)
   {
      final Map<String, String> settings = new HashMap<String, String>(3);
      // We add the portal container name
      settings.put(PORTAL_CONTAINER_SETTING_NAME, def.getName());
      // We add the rest context name
      settings.put(REST_CONTEXT_SETTING_NAME, def.getRestContextName() == null ? defaultDefinition.getRestContextName()
         : def.getRestContextName());
      // We add the realm name
      settings.put(REALM_SETTING_NAME, def.getRealmName() == null ? defaultDefinition.getRealmName() : def
         .getRealmName());
      return settings;
   }

   /**
    * Loads the external settings corresponding to the given path. The target file cans be either
    * a file of type "properties" or "xml". The given path will be interpreted as follows:
    * <ol>
    * <li>The path doesn't contain any prefix of type "classpath:", "jar:" or "file:", we
    * assume that the file could be externalized so we apply the following rules:
    * <ol>
    * <li>The value of the parameter <code>isPath4DefaultPCD</code> is <code>true</code> which
    * means that the given url comes from the default portal container definition and a file 
    * exists at ${exo-conf-dir}/portal/${path}, we will load this file</li>
    * <li>The value of the parameter <code>isPath4DefaultPCD</code> is <code>false</code> which
    * means that the given url doesn't come from the default portal container definition and a file 
    * exists at ${exo-conf-dir}/portal/${portalContainerName}/${path}, we will load this file</li>
    * <li>No file exists at the previous path, we then assume that the path cans be 
    * interpreted by the {@link ConfigurationManager}</li>
    * </ol>
    * </li>
    * <li>The path contains a prefix, we then assume that the path cans be interpreted 
    * by the {@link ConfigurationManager}</li>
    * </ol>
    * @param path the path of the external settings to load
    * @param isPath4DefaultPCD indicates if the given path comes from the default portal
    * container definition
    * @param def the {@link PortalContainerDefinition} for which we load the external settings
    * @return A {@link Map} of settings if the file could be loaded, <code>null</code> otherwise
    */
   private Map<String, String> loadExternalSettings(String path, boolean isPath4DefaultPCD,
      PortalContainerDefinition def)
   {
      try
      {
         URL url = null;
         if (path.indexOf(':') == -1)
         {
            // We first check if the file is not in eXo configuration directory
            String fullPath =
               serverInfo.getExoConfigurationDirectory() + "/portal/" + (isPath4DefaultPCD ? "" : def.getName() + "/")
                  + path;
            File file = new File(fullPath);
            if (PrivilegedFileHelper.exists(file))
            {
               // The file exists so we will use it
               url = file.toURI().toURL();
               if (ConfigurationManager.LOG_DEBUG)
               {
                  LOG.info("The external settings could be found in the directory ${exo-conf}/portal, "
                     + "it will be used as external settings of the "
                     + (isPath4DefaultPCD ? "default portal container" : "portal container '" + def.getName() + "'"));
               }
            }
            else if (ConfigurationManager.LOG_DEBUG)
            {
               LOG.info("No external settings could be found in the directory ${exo-conf}/portal for the "
                  + (isPath4DefaultPCD ? "default portal container" : "portal container '" + def.getName() + "'"));
            }
         }
         if (url == null)
         {
            // We assume that the path is an eXo standard path
            url = cm.getURL(path);
            if (ConfigurationManager.LOG_DEBUG)
            {
               LOG.info("Trying to retrieve the external settings from the url '" + url
                  + "', it will be used as external settings of the "
                  + (isPath4DefaultPCD ? "default portal container" : "portal container '" + def.getName() + "'"));
            }
         }
         // We load the properties from the url found
         return ContainerUtil.loadProperties(url, false);
      }
      catch (Exception e)// NOSONAR
      {
         LOG.error("Cannot load property file " + path, e);
      }
      return null;
   }

   /**
    * Merge the internal settings with the external settings. If the same setting name exists
    *  in both settings, we apply the following rules:
    * <ol>
    * <li>The value of the external setting is <code>null</code>, we ignore the value</li>
    * <li>The value of the external setting is not <code>null</code> and the value of the 
    * internal setting is <code>null</code>, the final value will be the external setting
    *  value that is of type {@link String}</li>
    * <li>Both values are not <code>null</code>, we will have to convert the external 
    * setting value into the target type which is the type of the internal setting value, 
    * thanks to the static method <code>valueOf(String)</code>, the following sub-rules are 
    * then applied:
    * <ol>
    * <li>The method cannot be found, the final value will be the external setting value 
    * that is of type {@link String}</li>
    * <li>The method can be found and the external setting value is an empty 
    * {@link String}, we ignore the external setting value</li>
    * <li>The method can be found and the external setting value is not an empty String 
    * but the method call fails, we ignore the external setting value</li>
    * <li>The method can be found and the external setting value is not an empty String 
    * and the method call succeeds, the final value will be the external setting value 
    * that is of type of the internal setting value</li>
    * </ol>
    * </li>
    * </ol>
    * @param settings the internal settings
    * @param props the external settings
    */
   private void mergeSettings(final Map<String, Object> settings, final Map<String, String> props)
   {
      if (settings.isEmpty())
      {
         // No settings exist so we can add everything
         settings.putAll(props);
      }
      else
      {
         // Some settings exists so we need to be careful if we override properties
         // We need to try to keep the same type if possible
         for (Map.Entry<String, String> entry : props.entrySet())
         {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();
            propertyValue = Deserializer.resolveString((String)propertyValue);
            if (propertyValue == null)
            {
               // We skip null value
               continue;
            }
            Object oldValue = settings.get(propertyName);
            if (oldValue != null)
            {
               // The value is not null so we need to convert the String into
               // the target type, we will convert thanks to the static method
               // valueOf(String value) if it exist for the target type
               Method m = null;
               try
               {
                  // First we check if the method exists
                  m = oldValue.getClass().getMethod("valueOf", String.class);
               }
               catch (Exception e)// NOSONAR
               {
                  if (LOG.isDebugEnabled())
                  {
                     LOG.debug(
                        "The static method valueOf(String) cannot be found for the class " + oldValue.getClass(), e);
                  }
               }
               if (m != null)
               {
                  // The method could be found, thus we will try to convert the value
                  String sPropertyValue = ((String)propertyValue).trim();
                  if (sPropertyValue.length() == 0)
                  {
                     // We ignore empty value since it cannot be converted
                     continue;
                  }
                  try
                  {
                     propertyValue = m.invoke(null, propertyValue);
                  }
                  catch (Exception e)// NOSONAR
                  {
                     LOG.error("Cannot convert the value '" + propertyValue + "' to an Object of type "
                        + oldValue.getClass(), e);
                     // we ignore invalid value
                     continue;
                  }
               }
            }
            // We set the new value
            settings.put(propertyName, propertyValue);
         }
      }
   }

   /**
    * Initialize the current component
    * @param mDefinitions the list of all the portal container definition to treat
    */
   private void initialize(Map<String, PortalContainerDefinition> mDefinitions)
   {
      final List<String> lPortalContainerNames = new ArrayList<String>(mDefinitions.size() + 1);
      boolean isDefaultPortalDisabled = isPortalContainerNameDisabled(defaultDefinition.getName());
      if (mDefinitions.isEmpty() || !isDefaultPortalDisabled)
      {
         // Add the default portal container name if not disabled
         lPortalContainerNames.add(defaultDefinition.getName());         
      }
      final Map<String, List<String>> mScopes = new HashMap<String, List<String>>();
      boolean first = true;
      for (Map.Entry<String, PortalContainerDefinition> entry : mDefinitions.entrySet())
      {
         PortalContainerDefinition definition = entry.getValue();
         String name = definition.getName();
         if (isPortalContainerNameDisabled(name))
         {
            // The portal container has been disabled so the related portal container definition
            // will be ignored
            continue;
         }
         boolean hasChanged = false;
         if (!lPortalContainerNames.contains(name))
         {
            lPortalContainerNames.add(name);
         }
         if (first)
         {
            first = false;
            // Initialize the main fields thanks to the data found in the first portal container
            if (defaultDefinition.getName() == DEFAULT_PORTAL_CONTAINER_NAME || isDefaultPortalDisabled)
            {
               defaultDefinition.setName(name);
               hasChanged = true;
            }
            if ((defaultDefinition.getRestContextName() == DEFAULT_REST_CONTEXT_NAME || isDefaultPortalDisabled)
               && definition.getRestContextName() != null && definition.getRestContextName().trim().length() > 0)
            {
               defaultDefinition.setRestContextName(definition.getRestContextName().trim());
               hasChanged = true;
            }
            if ((defaultDefinition.getRealmName() == DEFAULT_REALM_NAME || isDefaultPortalDisabled)
               && definition.getRealmName() != null && definition.getRealmName().trim().length() > 0)
            {
               defaultDefinition.setRealmName(definition.getRealmName().trim());
               hasChanged = true;
            }
         }
         // Apply the changes corresponding to the given definition
         applyChanges(definition);
         registerDependencies(definition, mScopes);
         if (hasChanged)
         {
            initializeSettings(defaultDefinition, false);
         }
         initializeSettings(definition, true);
      }
      if (!mDefinitions.containsKey(defaultDefinition.getName()))
      {
         // Apply the changes corresponding to the default definition
         applyChanges(defaultDefinition);
         initializeSettings(defaultDefinition, false);
         if (defaultDefinition.getDependencies() != null && !defaultDefinition.getDependencies().isEmpty())
         {
            // The default portal container has not been defined and some default
            // dependencies have been defined
            registerDependencies(defaultDefinition, mScopes);
         }
         if (mDefinitions.isEmpty() && !portalContainerNamesDisabled.isEmpty())
         {
            if (PropertyManager.isDevelopping())
            {
               LOG.warn("No portal container definition has been registered, the old behavior is" +
                  " then expected so you cannot disable any portal container. The list of" +
                  " portal containers to disable will be ignored");               
            }
            portalContainerNamesDisabled = Collections.unmodifiableSet(new HashSet<String>());
         }
      }
      this.portalContainerNames = Collections.unmodifiableList(lPortalContainerNames);
      this.scopes = Collections.unmodifiableMap(mScopes);
      // clear the changes
      changes.clear();
   }

   /**
    * Apply the changes corresponding to the give {@link PortalContainerDefinition}
    * @param definition
    */
   private void applyChanges(PortalContainerDefinition definition)
   {
      for (PortalContainerDefinitionChangePlugin plugin : changes)
      {
         if (matches(definition, plugin))
         {
            // The definition matches with the scope of the changes
            for (PortalContainerDefinitionChange change : plugin.getChanges())
            {
               try
               {
                  // Secure access to definition's info
                  change.apply(new SafePortalContainerDefinition(definition, defaultDefinition));
               }
               catch (Exception e)// NOSONAR
               {
                  LOG.warn("Cannot apply the change " + change, e);
               }
            }
         }
      }
   }

   /**
    * Indicates whether the given definition matches with the scope of the {@link PortalContainerDefinitionChangePlugin}
    * @param definition the {@link PortalContainerDefinition} to test.
    * @param plugin the {@link PortalContainerDefinitionChangePlugin} from which we extract the scopes
    * of the embedded actions
    * @return <code>true</code> if it matches, <code>false</code> otherwise
    */
   private boolean matches(PortalContainerDefinition definition, PortalContainerDefinitionChangePlugin plugin)
   {
      if (plugin.isAll())
      {
         // The changes have to be applied to all the portal containers
         return true;
      }
      if ((plugin.getNames() == null || plugin.isDefault()) && defaultDefinition.getName().equals(definition.getName()))
      {
         // The changes have to be applied to the default portal container and the given definition is
         // the definition of the default portal container
         return true;
      }
      if (plugin.getNames() != null && plugin.getNames().contains(definition.getName()))
      {
         // The changes have to be applied to a specific list of portal containers and the given
         // definition is the a definition of one of them
         return true;
      }
      return false;
   }

   /**
    * {@inheritDoc}
    */
   public void start()
   {
      if (!initialized)
      {
         synchronized (this)
         {
            if (!initialized)
            {
               // Prevent to add new definitions after initializing the PortalContainerConfig
               this.initialized = true;
            }
            else
            {
               // The PortalContainerConfig has already been initialized 
               return;
            }
         }
      }
      else
      {
         // The PortalContainerConfig has already been initialized 
         return;
      }
      initialize(definitions);
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
   }
}
