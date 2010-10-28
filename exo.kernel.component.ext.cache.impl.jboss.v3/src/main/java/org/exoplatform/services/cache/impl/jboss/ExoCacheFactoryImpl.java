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
package org.exoplatform.services.cache.impl.jboss;

import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.impl.jboss.fifo.FIFOExoCacheCreator;
import org.exoplatform.services.cache.impl.jboss.util.PrivilegedCacheHelper;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.cache.Cache;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.Configuration.CacheMode;
import org.jboss.cache.config.EvictionConfig;
import org.jboss.cache.config.EvictionRegionConfig;

import java.io.Serializable;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This class is the JBoss Cache implementation of the {@link org.exoplatform.services.cache.ExoCacheFactory}
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 17 juil. 2009  
 */
public class ExoCacheFactoryImpl implements ExoCacheFactory
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.ext.cache.impl.jboss.v3.ExoCacheFactoryImpl");

   /**
    * The initial parameter key that defines the full path of the configuration template
    */
   private static final String CACHE_CONFIG_TEMPLATE_KEY = "cache.config.template";
   
   /**
    * Indicate whether the JBoss Cache instance used can be shared with other caches
    */
   public static final String ALLOW_SHAREABLE_CACHE = "allow.shareable.cache";

   public static final boolean ALLOW_SHAREABLE_CACHE_DEFAULT = false;

   /**
    * The configuration manager that allows us to retrieve a configuration file in several different
    * manners
    */
   private final ConfigurationManager configManager;

   /**
    * The full path of the configuration template
    */
   private final String cacheConfigTemplate;
   
   /**
    * Indicates whether the JBossCache instances can be shared between several eXo Cache by default
    */
   private final boolean allowShareableCache;

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
    * A Map that contains all the registered JBC instances.
    */
   private final Map<ConfigurationKey, Cache<Serializable, Object>> caches =
      new HashMap<ConfigurationKey, Cache<Serializable, Object>>();

   /**
    * The default creator
    */
   private final ExoCacheCreator defaultCreator = new FIFOExoCacheCreator();

   public ExoCacheFactoryImpl(InitParams params, ConfigurationManager configManager)
   {
      this(getValueParam(params, CACHE_CONFIG_TEMPLATE_KEY), configManager, getBooleanParam(params,
         ALLOW_SHAREABLE_CACHE, ALLOW_SHAREABLE_CACHE_DEFAULT));
   }

   ExoCacheFactoryImpl(String cacheConfigTemplate, ConfigurationManager configManager, boolean allowShareableCache)
   {
      this.configManager = configManager;
      this.cacheConfigTemplate = cacheConfigTemplate;
      this.allowShareableCache = allowShareableCache;
      if (cacheConfigTemplate == null)
      {
         throw new RuntimeException("The parameter '" + CACHE_CONFIG_TEMPLATE_KEY + "' must be set");
      }
   }
   
   /**
    * To create a new cache instance according to the given configuration, we follow the steps below:
    * 
    * 1. We first try to find if a specific location of the cache configuration has been defined thanks
    * to an external component plugin of type ExoCacheFactoryConfigPlugin
    * 2. If no specific location has been defined, we use the default configuration which is
    * "${CACHE_CONFIG_TEMPLATE_KEY}"
    */
   public ExoCache<Serializable, Object> createCache(ExoCacheConfig config) throws ExoCacheInitException
   {
      final String region = config.getName();
      final String customConfig = mappingCacheNameConfig.get(region);
      Cache<Serializable, Object> cache;

      final CacheFactory<Serializable, Object> factory = PrivilegedCacheHelper.createCacheFactory();
      final ExoCache<Serializable, Object> eXoCache;
      try
      {
         if (customConfig != null)
         {
            // A custom configuration has been set
            if (LOG.isInfoEnabled())
               LOG.info("A custom configuration has been set for the cache '" + region + "'.");
            cache = PrivilegedCacheHelper.createCache(factory, configManager.getInputStream(customConfig), false);
         }
         else
         {
            // No custom configuration has been found, a configuration template will be used 
            if (LOG.isInfoEnabled())
               LOG.info("The configuration template will be used for the the cache '" + region + "'.");

            cache =
               PrivilegedCacheHelper.createCache(factory, configManager.getInputStream(cacheConfigTemplate), false);
            if (!config.isDistributed())
            {
               // The cache is local
               cache.getConfiguration().setCacheMode(CacheMode.LOCAL);
            }
            // Re initialize the template to avoid conflicts
            cleanConfigurationTemplate(cache, region);
         }
         final ExoCacheCreator creator = getExoCacheCreator(config);
         // Ensure that new created cache doesn't exist
         cache = getUniqueInstance(cache, config);
         // Create the cache
         eXoCache = creator.create(config, cache);
         // Create the cache
         PrivilegedCacheHelper.create(cache);
         // Start the cache
         PrivilegedCacheHelper.start(cache);
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
         mappingImplCreators.put(creator.getExpectedImplementation(), creator);
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
    * Returns the boolean value of the parameter corresponding to the given key. If no value can
    * be found the default value will be returned 
    */
   private static boolean getBooleanParam(InitParams params, String key, boolean defaultValue)
   {
      String value = getValueParam(params, key);
      return value == null ? defaultValue : Boolean.valueOf(value).booleanValue();
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
   protected void cleanConfigurationTemplate(Cache<Serializable, Object> cache, String region)
   {
      final Configuration config = cache.getConfiguration();
      // Reset the eviction policies 
      EvictionConfig evictionConfig = config.getEvictionConfig();
      if (evictionConfig == null)
      {
         // If not eviction config exists, we create an empty one
         evictionConfig = new EvictionConfig();
         config.setEvictionConfig(evictionConfig);
      }
      evictionConfig.setEvictionRegionConfigs(new LinkedList<EvictionRegionConfig>());
   } 
   
   /**
    * Try to find if a Cache of the same type (i.e. their {@link Configuration} are equals)
    * has already been registered.
    * If no cache has been registered, we register the given cache otherwise we
    * use the previously registered cache. If the config given is of type {@link AbstractExoCacheConfig}
    * we will try to get the value of the parameter allowShareableCache, if it is not set we will use the
    * default value defined in the current instance of {@link ExoCacheFactoryImpl}. If the cache
    * is not shareable then no cache will be registered.
    * @param cache the cache to register
    * @param config the configuration of the cache
    * @return the given cache if has not been registered otherwise the cache of the same
    * type that has already been registered 
    * @throws ExoCacheInitException
    */
   private synchronized Cache<Serializable, Object> getUniqueInstance(Cache<Serializable, Object> cache, ExoCacheConfig config)
      throws ExoCacheInitException
   {
      String region = config.getName();
      boolean allowShareableCache = this.allowShareableCache;
      if (config instanceof AbstractExoCacheConfig)
      {
         AbstractExoCacheConfig aConfig = (AbstractExoCacheConfig)config;
         if (aConfig.getAllowShareableCache() != null)
         {
            allowShareableCache = aConfig.getAllowShareableCache().booleanValue();
         }
      }
      Configuration cfg = cache.getConfiguration();
      if (!allowShareableCache)
      {
         // Rename the cluster name
         String clusterName = cfg.getClusterName();
         if (clusterName != null && (clusterName = clusterName.trim()).length() > 0)
         {
            cfg.setClusterName(clusterName + "-" + region);
         }
         return cache;
      }
      ConfigurationKey key;
      try
      {
         key = new ConfigurationKey(cfg);
      }
      catch (CloneNotSupportedException e)
      {
         throw new ExoCacheInitException("Cannot clone the configuration.", e);
      }
      if (caches.containsKey(key))
      {
         cache = caches.get(key);
      }
      else
      {
         caches.put(key, cache);
         if (LOG.isInfoEnabled())
            LOG.info("A new eXo Cache based on JBoss Cache instance has been registered for the region " + region);
      }
      return cache;
   }
   
   /**
    * This class is used to make {@link Configuration} being usable as a Key in an HashMap since
    * some variables such as <code>jgroupsConfigFile</code> are not managed as expected in the
    * methods equals and hashCode. Moreover two cache with same config except the EvictionConfig
    * are considered as identical
    */
   private static class ConfigurationKey
   {
      private final URL jgroupsConfigFile;
      private final Configuration conf;
      
      public ConfigurationKey(Configuration initialConf) throws CloneNotSupportedException
      {
         // Clone it first since it will be modified
         this.conf = initialConf.clone();
         this.jgroupsConfigFile = conf.getJGroupsConfigFile();
         // remove the jgroupsConfigFile from the conf
         conf.setJgroupsConfigFile(null);
         // remove the EvictionConfig to ignore it
         conf.setEvictionConfig(null);
      }

      @Override
      public int hashCode()
      {
         final int prime = 31;
         int result = 1;
         result = prime * result + ((conf == null) ? 0 : conf.hashCode());
         result = prime * result + ((jgroupsConfigFile == null) ? 0 : jgroupsConfigFile.hashCode());
         return result;
      }

      @Override
      public boolean equals(Object obj)
      {
         if (this == obj)
            return true;
         if (obj == null)
            return false;
         if (getClass() != obj.getClass())
            return false;
         ConfigurationKey other = (ConfigurationKey)obj;
         if (conf == null)
         {
            if (other.conf != null)
               return false;
         }
         else if (!conf.equals(other.conf))
            return false;
         if (jgroupsConfigFile == null)
         {
            if (other.jgroupsConfigFile != null)
               return false;
         }
         else if (!jgroupsConfigFile.equals(other.jgroupsConfigFile))
            return false;
         return true;
      }
   }   
}
