/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.container.definition;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.picocontainer.Startable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;

/**
 * This class is used to define the configuration related to the portal containers themselves. 
 * It is mainly dedicated to the {@link RootContainer} to allows to understand how to manage and 
 * deploy all the portal containers
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 26 aožt 2009  
 */
public class PortalContainerConfig implements Startable
{

   /**
    * The default name of a portal container
    */
   public static final String DEFAULT_PORTAL_CONTAINER_NAME = "portal";

   /**
    * The default name of a the {@link ServletContext} of the rest web application
    */
   public static final String DEFAULT_REST_CONTEXT_NAME = "rest";

   /**
    * The default realm name
    */
   public static final String DEFAULT_REALM_NAME = "exo-domain";

   /**
    * The name of the default portal container
    */
   private String defaultPortalContainerName;

   /**
    * The name of the default rest {@link ServletContext}
    */
   private String defaultRestContextName;

   /**
    * The name of the default realm
    */
   private String defaultRealmName;

   /**
    * Indicates if the component has already been initialized
    */
   private volatile boolean initialized;

   /**
    * The list of all the portal containers
    */
   private List<String> portalContainerNames;

   /**
    * The list of all the web application scopes
    */
   private Map<String, List<String>> scopes;

   /**
    * The list of all the {@link PortalContainerDefinition} that have been registered
    */
   private Map<String, PortalContainerDefinition> definitions =
      Collections.unmodifiableMap(new HashMap<String, PortalContainerDefinition>());

   public PortalContainerConfig()
   {
      this(null);
   }

   public PortalContainerConfig(InitParams params)
   {
      if (params == null)
      {
         return;
      }
      final ValueParam vDpc = params.getValueParam("default.portal.container");
      if (vDpc != null && vDpc.getValue().trim().length() > 0)
      {
         this.defaultPortalContainerName = vDpc.getValue().trim();
      }
      final ValueParam vRc = params.getValueParam("default.rest.context");
      if (vRc != null && vRc.getValue().trim().length() > 0)
      {
         this.defaultRestContextName = vRc.getValue().trim();
      }
      final ValueParam vRn = params.getValueParam("default.realm.name");
      if (vRn != null && vRn.getValue().trim().length() > 0)
      {
         this.defaultRealmName = vRn.getValue().trim();
      }
   }

   /**
    * @return the default name of the portal container
    */
   public String getDefaultPortalContainer()
   {
      return defaultPortalContainerName;
   }

   /**
    * @return the default name of the rest {@link ServletContext}
    */
   public String getDefaultRestContext()
   {
      return defaultRestContextName;
   }

   /**
    * @return the default name of the realm
    */
   public String getDefaultRealmName()
   {
      return defaultRealmName;
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
    * Registers a name of a portal container if it has not yet been registered
    * @param name the name of the portal container to register
    */
   public synchronized void registerPortalContainerName(String name)
   {
      if (!portalContainerNames.contains(name))
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
      if (portalContainerNames.contains(name))
      {
         final List<String> lPortalContainerNames = new ArrayList<String>(portalContainerNames);
         lPortalContainerNames.remove(name);
         this.portalContainerNames = Collections.unmodifiableList(lPortalContainerNames);
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
      final List<String> result = scopes.get(contextName);
      if (result == null || result.isEmpty())
      {
         // we assume the old behavior is expected         
         final String portalContainerName =
            portalContainerNames.contains(contextName) ? contextName : defaultPortalContainerName;
         return Collections.singletonList(portalContainerName);
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
      final List<String> result = scopes.get(contextName);
      if (result == null || result.isEmpty())
      {
         // we assume the old behavior is expected         
         return defaultPortalContainerName;
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
      return definition == null ? null : definition.getDependencies();
   }

   /**
    * Gives the name of the rest {@link ServletContext} related to the given portal container
    * @param portalContainerName the name of the portal container for which we want the rest context name
    * @return the name of the related rest context name. It tries to get it from the {@link PortalContainerDefinition}
    * if it has not been set it will return <code>defaultRestContextName</code> 
    */
   public String getRestContextName(String portalContainerName)
   {
      final PortalContainerDefinition definition = definitions.get(portalContainerName);
      if (definition == null)
      {
         return defaultRestContextName;
      }
      else
      {
         String contextName = definition.getRestContextName();
         return contextName == null ? defaultRestContextName : contextName;
      }
   }

   /**
    * Gives the name of the realm related to the given portal container
    * @param portalContainerName the name of the portal container for which we want the realm name
    * @return the name of the related realm name. It tries to get it from the {@link PortalContainerDefinition}
    * if it has not been set it will return <code>defaultRealmName</code> 
    */
   public String getRealmName(String portalContainerName)
   {
      final PortalContainerDefinition definition = definitions.get(portalContainerName);
      if (definition == null)
      {
         return defaultRealmName;
      }
      else
      {
         String realmName = definition.getRealmName();
         return realmName == null ? defaultRealmName : realmName;
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
      final List<String> result = scopes.get(contextName);
      if (result == null || result.isEmpty())
      {
         // we assume the old behavior is expected
         return true;
      }
      else
      {
         return result.contains(portalContainerName);
      }
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
               new HashMap<String, PortalContainerDefinition>(definitions);
            for (PortalContainerDefinition def : lDefs)
            {
               String name = def.getName();
               if (name == null || (name = name.trim()).length() == 0)
               {
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
    * Construct the scopes of all the web applications from the given {@link PortalContainerDefinition}
    * @param definition the definition of a {@link PortalContainer} that contains the dependencies with
    * the web application
    * @param scopes the map in which the scope must be defined
    */
   private void registerDependencies(PortalContainerDefinition definition, Map<String, List<String>> scopes)
   {
      final List<String> dependencies = definition.getDependencies();
      if (definition == null || dependencies.isEmpty())
      {
         return;
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
    * Initialize the current component
    * @param mDefinitions the list of all the portal container definition to treat
    */
   private void initialize(Map<String, PortalContainerDefinition> mDefinitions)
   {
      if (mDefinitions.isEmpty())
      {
         // No definitions have been found, the default values will be set
         if (defaultPortalContainerName == null)
         {
            this.defaultPortalContainerName = DEFAULT_PORTAL_CONTAINER_NAME;
         }
      }
      final List<String> lPortalContainerNames = new ArrayList<String>(mDefinitions.size() + 1);
      // Add the default portal container name
      if (defaultPortalContainerName != null)
      {
         lPortalContainerNames.add(defaultPortalContainerName);
      }
      final Map<String, List<String>> mScopes = new HashMap<String, List<String>>();
      for (Map.Entry<String, PortalContainerDefinition> entry : mDefinitions.entrySet())
      {
         PortalContainerDefinition definition = entry.getValue();
         String name = definition.getName();
         if (!name.equals(defaultPortalContainerName))
         {
            if (defaultPortalContainerName == null)
            {
               this.defaultPortalContainerName = name;
            }
            lPortalContainerNames.add(name);
         }
         if (defaultRestContextName == null)
         {
            this.defaultRestContextName = definition.getRestContextName();
         }
         if (defaultRealmName == null)
         {
            this.defaultRealmName = definition.getRealmName();
         }
         registerDependencies(definition, mScopes);
      }
      this.portalContainerNames = Collections.unmodifiableList(lPortalContainerNames);
      this.scopes = Collections.unmodifiableMap(mScopes);
      if (defaultRestContextName == null)
      {
         this.defaultRestContextName = DEFAULT_REST_CONTEXT_NAME;
      }
      if (defaultRealmName == null)
      {
         this.defaultRealmName = DEFAULT_REALM_NAME;
      }
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
