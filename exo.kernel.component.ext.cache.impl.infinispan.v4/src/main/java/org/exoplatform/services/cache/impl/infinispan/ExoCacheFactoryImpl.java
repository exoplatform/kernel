/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.services.cache.impl.infinispan;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.impl.infinispan.generic.GenericExoCacheCreator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;
import org.infinispan.config.GlobalConfiguration;
import org.infinispan.config.Configuration.CacheMode;
import org.infinispan.eviction.EvictionStrategy;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.remoting.transport.jgroups.JGroupsTransport;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * This class is the Infinispan implementation of the {@link org.exoplatform.services.cache.ExoCacheFactory}
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ExoCacheFactoryImpl implements ExoCacheFactory
{

   /**
    * The logger
    */
   private static final Log LOG =
      ExoLogger.getLogger("exo.kernel.component.ext.cache.impl.infinispan.v4.ExoCacheFactoryImpl");

   /**
    * The initial parameter key that defines the full path of the configuration template
    */
   private static final String CACHE_CONFIG_TEMPLATE_KEY = "cache.config.template";

   /**
    * The current {@link ExoContainerContext}
    */
   private final ExoContainerContext ctx;
   
   /**
    * The configuration manager that allows us to retrieve a configuration file in several different
    * manners
    */
   private final ConfigurationManager configManager;

   /**
    * The {@link DefaultCacheManager} used for all the cache regions
    */
   private final DefaultCacheManager cacheManager;

   /**
    * The mapping between the configuration types and the creators
    */
   private final Map<Class<? extends ExoCacheConfig>, ExoCacheCreator> mappingConfigTypeCreators =
      new HashMap<Class<? extends ExoCacheConfig>, ExoCacheCreator>();

   /**
    * The mapping between the implementations and the creators. This is mainly used for backward compatibility
    */
   private final Map<String, ExoCacheCreator> mappingImplCreators = new HashMap<String, ExoCacheCreator>();

   /**
    * The mapping between the cache names and the configuration paths
    */
   private final Map<String, String> mappingCacheNameConfig = new HashMap<String, String>();
   
   /**
    * The mapping between the global configuration and the cache managers
    */   
   private final Map<GlobalConfiguration, DefaultCacheManager> mappingGlobalConfigCacheManager = 
      new HashMap<GlobalConfiguration, DefaultCacheManager>(); 

   /**
    * The default creator
    */
   private final ExoCacheCreator defaultCreator = new GenericExoCacheCreator();

   public ExoCacheFactoryImpl(ExoContainerContext ctx, InitParams params, ConfigurationManager configManager)
      throws ExoCacheInitException
   {
      this(ctx, getValueParam(params, CACHE_CONFIG_TEMPLATE_KEY), configManager);
   }

   ExoCacheFactoryImpl(ExoContainerContext ctx, String cacheConfigTemplate, ConfigurationManager configManager)
      throws ExoCacheInitException
   {
      this.ctx = ctx;
      this.configManager = configManager;
      if (cacheConfigTemplate == null)
      {
         throw new RuntimeException("The parameter '" + CACHE_CONFIG_TEMPLATE_KEY + "' must be set");
      }
      // Initialize the main cache manager
      this.cacheManager = initCacheManager(cacheConfigTemplate);
      // Register the main cache manager
      mappingGlobalConfigCacheManager.put(cacheManager.getGlobalConfiguration(), cacheManager);
   }

   /**
    * Initializes the {@link DefaultCacheManager}
    * @throws ExoCacheInitException if the cache manager cannot be initialized
    */
   private DefaultCacheManager initCacheManager(String cacheConfigTemplate)
      throws ExoCacheInitException
   {
      InputStream is = null;
      try
      {
         // Read the configuration file of the cache
         is = configManager.getInputStream(cacheConfigTemplate);
      }
      catch (Exception e)
      {
         throw new ExoCacheInitException("The configuration of the CacheManager cannot be loaded from '"
            + cacheConfigTemplate + "'", e);
      }
      if (is == null)
      {
         throw new ExoCacheInitException("The configuration of the CacheManager cannot be found at '"
            + cacheConfigTemplate + "'");
      }
      DefaultCacheManager cacheManager = null;
      try
      {
         // Create the CacheManager from the input stream
         cacheManager = new DefaultCacheManager(is, false);
      }
      catch (Exception e)
      {
         throw new ExoCacheInitException("Cannot initialize the CacheManager corresponding to the configuration '"
            + cacheConfigTemplate + "'", e);
      }

      GlobalConfiguration config = cacheManager.getGlobalConfiguration();
      
      configureJGroups(config);
      return cacheManager;
   }

   /**
    * If some JGoups properties has been set, it will load the configuration and set
    * the cluster name by adding as suffix the name of the {@link ExoContainerContext}
    * 
    * @param config the global configuration from which the JGroups config will be extracted
    * @throws ExoCacheInitException if any exception occurs while configuring JGroups
    */
   private void configureJGroups(GlobalConfiguration config) throws ExoCacheInitException
   {
      if (loadJGroupsConfig(config))
      {
         // The JGroups Config could be loaded which means that the configuration is for a cluster
         config.setClusterName(config.getClusterName() + "-" + ctx.getName());
      }
   }

   /**
    * Load the JGroups configuration file thanks to the {@link ConfigurationManager}
    * @param config the global configuration from which the JGroups config will be extracted
    * @return <code>true</code> if the JGoups config could be loaded successfully, 
    * <code>false</code> if there were no JGroups config to load
    * @throws ExoCacheInitException if the JGroups config could not be loaded
    */
   private boolean loadJGroupsConfig(GlobalConfiguration config) throws ExoCacheInitException
   {
      Properties properties = config.getTransportProperties();
      if (properties == null || !properties.containsKey(JGroupsTransport.CONFIGURATION_FILE))
      {
         return false;
      }
      InputStream is;
      String jgroupsFileLocation = properties.getProperty(JGroupsTransport.CONFIGURATION_FILE);
      try
      {
         // Read the jgroups configuration file
         URL url = configManager.getURL(jgroupsFileLocation);
         is = url == null ? null : url.openStream();
      }
      catch (Exception e)
      {
         throw new ExoCacheInitException("The jgroups configuration cannot be loaded from '" + jgroupsFileLocation
            + "'", e);
      }
      if (is != null)
      {
         try
         {
            // Set the jgroups configuration as XML
            properties.setProperty(JGroupsTransport.CONFIGURATION_XML, readStream(is));
         }
         catch (IOException e)
         {
            throw new ExoCacheInitException("The jgroups configuration cannot be read from '" + jgroupsFileLocation
               + "'");
         }
         // Remove the property corresponding to the configuration file
         properties.remove(JGroupsTransport.CONFIGURATION_FILE);
      }
      return true;
   }

   /**
    * Reads bytes from input stream and builds a string from them
    * 
    * @param inputStream
    * @return
    * @throws IOException
    */
   protected String readStream(InputStream inputStream) throws IOException
   {
      StringBuilder out = new StringBuilder(4096);
      byte[] b = new byte[4096];
      try
      {
         for (int length; (length = inputStream.read(b)) != -1;)
         {
            out.append(new String(b, 0, length));
         }
      }
      finally
      {
         try
         {
            inputStream.close();
         }
         catch (Exception e)
         {
            LOG.debug("Cannot close stream", e);
         }
      }
      return out.toString();
   }

   /**
    * To create a new cache instance according to the given configuration, we follow the steps below:
    * 
    * We first try to find if a specific location of the cache configuration has been defined thanks
    * to an external component plugin of type ExoCacheFactoryConfigPlugin. If so we use the default cache
    * configuration defined in this file otherwise we use the default cache configuration defined in
    * "${CACHE_CONFIG_TEMPLATE_KEY}"
    */
   public ExoCache<Serializable, Object> createCache(ExoCacheConfig config) throws ExoCacheInitException
   {
      final String region = config.getName();
      final String customConfig = mappingCacheNameConfig.get(region);
      final ExoCache<Serializable, Object> eXoCache;
      final DefaultCacheManager cacheManager;
      try
      {
         final Configuration conf;
         if (customConfig != null)
         {
            // A custom configuration has been set
            if (LOG.isInfoEnabled())
               LOG.info("A custom configuration has been set for the cache '" + region + "'.");
            // Create the CacheManager by loading the configuration
            DefaultCacheManager customCacheManager = new DefaultCacheManager(configManager.getInputStream(customConfig), false);
            GlobalConfiguration gc = customCacheManager.getGlobalConfiguration();
            // Configure JGroups since it could affect the state of the Global Config
            configureJGroups(gc);
            // Check if a CacheManager with the same GlobalConfiguration exists
            DefaultCacheManager currentCacheManager = mappingGlobalConfigCacheManager.get(gc);
            if (currentCacheManager == null)
            {
               // No cache manager has been defined so far for this Cache Configuration
               currentCacheManager = customCacheManager;
               // We register this new cache manager
               mappingGlobalConfigCacheManager.put(gc, customCacheManager);
            }
            conf = currentCacheManager.getDefaultConfiguration().clone();
            cacheManager = currentCacheManager;
         }
         else
         {
            cacheManager = this.cacheManager;
            // No custom configuration has been found, a configuration template will be used 
            if (LOG.isInfoEnabled())
               LOG.info("The configuration template will be used for the the cache '" + region + "'.");
            conf = cacheManager.getDefaultConfiguration().clone();
            if (!config.isDistributed() && !config.isRepicated())
            {
               // The cache is local
               conf.setCacheMode(CacheMode.LOCAL);
            }
         }
         // Reset the configuration to avoid conflicts
         resetConfiguration(conf);
         final ExoCacheCreator creator = getExoCacheCreator(config);
         // Create the cache
         eXoCache = creator.create(config, conf, new Callable<Cache<Serializable, Object>>()
         {
            @Override
            public Cache<Serializable, Object> call() throws Exception
            {
               // Define the configuration
               cacheManager.defineConfiguration(region, conf);
               // create and start the cache                 
               return cacheManager.getCache(region);
            }
         });
      }
      catch (Exception e)
      {
         throw new ExoCacheInitException("The cache '" + region + "' could not be initialized", e);
      }
      return eXoCache;
   }

   /**
    * Add a list of creators to register
    * @param plugin the plugin that contains the creators
    */
   public void addCreator(ExoCacheCreatorPlugin plugin)
   {
      final List<ExoCacheCreator> creators = plugin.getCreators();
      for (ExoCacheCreator creator : creators)
      {
         mappingConfigTypeCreators.put(creator.getExpectedConfigType(), creator);
         Set<String> implementations = creator.getExpectedImplementations();
         if (implementations == null)
         {
            throw new NullPointerException("The set of implementations cannot be null");
         }
         for (String imp : implementations)
         {
            mappingImplCreators.put(imp, creator);
         }
      }
   }

   /**
    * Add a list of custom configuration to register
    * @param plugin the plugin that contains the configs
    */
   public void addConfig(ExoCacheFactoryConfigPlugin plugin)
   {
      final Map<String, String> configs = plugin.getConfigs();
      mappingCacheNameConfig.putAll(configs);
   }

   /**
    * Returns the value of the ValueParam if and only if the value is not empty
    */
   private static String getValueParam(InitParams params, String key)
   {
      if (params == null)
      {
         return null;
      }
      final ValueParam vp = params.getValueParam(key);
      String result;
      if (vp == null || (result = vp.getValue()) == null || (result = result.trim()).length() == 0)
      {
         return null;
      }
      return result;
   }

   /**
    * Returns the most relevant ExoCacheCreator according to the give configuration
    */
   protected ExoCacheCreator getExoCacheCreator(ExoCacheConfig config)
   {
      ExoCacheCreator creator = mappingConfigTypeCreators.get(config.getClass());
      if (creator == null)
      {
         // No creator for this type has been found, let's try the implementation field
         creator = mappingImplCreators.get(config.getImplementation());
         if (creator == null)
         {
            // No creator can be found, we will use the default creator
            if (LOG.isInfoEnabled())
               LOG.info("No cache creator has been found for the the cache '" + config.getName()
                  + "', the default one will be used.");
            return defaultCreator;
         }
      }
      if (LOG.isInfoEnabled())
         LOG.info("The cache '" + config.getName() + "' will be created with '" + creator.getClass() + "'.");
      return creator;
   }

   /**
    * Clean the configuration template to prevent conflicts
    */
   protected void resetConfiguration(Configuration config)
   {
      config.setInvocationBatchingEnabled(true);
      config.setEvictionStrategy(EvictionStrategy.NONE);
      config.setEvictionMaxEntries(-1);
      config.setExpirationLifespan(-1);
      config.setExpirationMaxIdle(-1);
      config.setEvictionWakeUpInterval(5000);
   }
}
