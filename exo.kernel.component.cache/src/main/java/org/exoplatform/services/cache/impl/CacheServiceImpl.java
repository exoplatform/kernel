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
package org.exoplatform.services.cache.impl;

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheConfigPlugin;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.SimpleExoCache;
import org.exoplatform.services.cache.invalidation.InvalidationExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

/**
 * Created by The eXo Platform SAS. Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Sat, Sep 13, 2003 @ Time: 1:12:22 PM
 */
@SuppressWarnings("deprecation")
@ManagedBy(CacheServiceManaged.class)
public class CacheServiceImpl implements CacheService
{
   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.cache.CacheServiceImpl");

   private final ExoCacheFactory DEFAULT_FACTORY = new SimpleExoCacheFactory();

   private final HashMap<String, ExoCacheConfig> configs_ = new HashMap<String, ExoCacheConfig>();

   private final ConcurrentHashMap<String, FutureExoCacheCreationTask> cacheMap_ =
      new ConcurrentHashMap<String, FutureExoCacheCreationTask>();
   
   private final ExoCacheConfig defaultConfig_;

   private final LoggingCacheListener loggingListener_;

   private final ExoCacheFactory factory_;

   CacheServiceManaged managed;

   /**
    * 
    */
   public CacheServiceImpl(InitParams params) throws Exception
   {
      this(params, null);
   }

   public CacheServiceImpl(InitParams params, ExoCacheFactory factory) throws Exception
   {
      List<ExoCacheConfig> configs = params.getObjectParamValues(ExoCacheConfig.class);
      for (ExoCacheConfig config : configs)
      {
         configs_.put(config.getName(), config);
      }
      defaultConfig_ = configs_.get("default");
      loggingListener_ = new LoggingCacheListener();
      factory_ = factory == null ? DEFAULT_FACTORY : factory;
   }

   public void addExoCacheConfig(ComponentPlugin plugin)
   {
      addExoCacheConfig((ExoCacheConfigPlugin)plugin);
   }

   public void addExoCacheConfig(ExoCacheConfigPlugin plugin)
   {
      List<ExoCacheConfig> configs = plugin.getConfigs();
      for (ExoCacheConfig config : configs)
      {
         configs_.put(config.getName(), config);
      }
   }

   @SuppressWarnings("unchecked")
   public <K extends Serializable, V> ExoCache<K, V> getCacheInstance(final String region)
   {
      if (region == null)
      {
         throw new IllegalArgumentException("region cannot be null");
      }
      if (region.length() == 0)
      {
         throw new IllegalArgumentException("region cannot be empty");
      }
      FutureExoCacheCreationTask creationTask = cacheMap_.get(region);
      if (creationTask == null)
      {
         Callable<ExoCache<? extends Serializable,?>> task = new Callable<ExoCache<? extends Serializable,?>>()
         {
            public ExoCache<? extends Serializable, ?> call() throws Exception
            {
               return createCacheInstance(region);
            }
         };
         creationTask = new FutureExoCacheCreationTask(task);
         FutureExoCacheCreationTask existingTask = cacheMap_.putIfAbsent(region, creationTask);
         if (existingTask != null)
         {
            creationTask = existingTask;
         }
         else
         {
            creationTask.run();
         }
      }
      try
      {
         return (ExoCache<K, V>)creationTask.get();
      }
      catch (CancellationException e)
      {
         cacheMap_.remove(region, creationTask);
      }
      catch (InterruptedException e)
      {
         Thread.currentThread().interrupt();
      }
      catch (ExecutionException e)
      {
         LOG.error("Could not create the cache for the region '" + region + "'", e.getCause());
      }
      return null;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private ExoCache<? extends Serializable, ?> createCacheInstance(String region) throws Exception
   {
      ExoCacheConfig config = configs_.get(region);
      if (config == null)
         config = defaultConfig_;

      // Ensure the configuration integrity
      final ExoCacheConfig safeConfig = config.clone();
      // Set the region as name 
      safeConfig.setName(region);
      
      ExoCache simple = null;
      if (factory_ != DEFAULT_FACTORY && safeConfig.getClass().isAssignableFrom(ExoCacheConfig.class) //NOSONAR
         && safeConfig.getImplementation() != null)
      {
         // The implementation exists and the config is not a sub class of ExoCacheConfig
         // we assume that we expect to use the default cache factory
         try
         {
            // We check if the given implementation is a known class
            Class<?> implClass = ClassLoading.loadClass(safeConfig.getImplementation(), this); 
            // Implementation is an existing class
            if (ExoCache.class.isAssignableFrom(implClass))
            {
               // The implementation is a sub class of eXo Cache so we use the default factory
               simple = DEFAULT_FACTORY.createCache(safeConfig);               
            }
         }
         catch (ClassNotFoundException e)
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("An exception occurred: " + e.getMessage());
            }
         }
      }
      if (simple == null)
      {
         // We use the configured cache factory
         simple = factory_.createCache(safeConfig);
      }
      
      if (managed != null)
      {
         managed.registerCache(simple);
      }
      // If the flag avoid value replication is enabled and the cache is replicated
      // or distributed we wrap the eXo cache instance into an InvalidationExoCache 
      // to enable the invalidation
      return safeConfig.avoidValueReplication() && (safeConfig.isRepicated() || safeConfig.isDistributed())
         ? new InvalidationExoCache(simple) : simple;
   }

   public Collection<ExoCache<? extends Serializable, ?>> getAllCacheInstances()
   {
      Collection<ExoCache<? extends Serializable, ?>> caches = 
         new ArrayList<ExoCache<? extends Serializable,?>>(cacheMap_.size());
      for (FutureTask<ExoCache<? extends Serializable,?>> task : cacheMap_.values())
      {
         ExoCache<? extends Serializable, ?> cache = null;
         try
         {
            cache = task.get();
         }
         catch (Exception e)
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("An exception occurred: " + e.getMessage());
            }
         }
         if (cache != null)
         {
            caches.add(cache);            
         }
      }
      return caches;
   }

   /**
    * Default implementation of an {@link org.exoplatform.services.cache.ExoCacheFactory}
    */
   private class SimpleExoCacheFactory implements ExoCacheFactory
   {

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings({"rawtypes", "unchecked"})
      public ExoCache createCache(ExoCacheConfig config) throws ExoCacheInitException
      {
         final ExoCache simple = createCacheInstance(config);
         simple.setName(config.getName());
         simple.setLabel(config.getLabel());
         simple.setMaxSize(config.getMaxSize());
         simple.setLiveTime(config.getLiveTime());
         simple.setLogEnabled(config.isLogEnabled());
         if (simple.isLogEnabled())
         {
            simple.addCacheListener(loggingListener_);
         }
         return simple;
      }

      /**
       * Create a new instance of ExoCache according to the given configuration
       * @param config the ExoCache configuration
       * @return a new instance of ExoCache
       * @throws ExoCacheInitException if any exception happens while initializing the cache
       */
      @SuppressWarnings("rawtypes")
      private ExoCache createCacheInstance(ExoCacheConfig config) throws ExoCacheInitException
      {
         if (config.getImplementation() == null)
         {
            // No implementation has been defined
            return new SimpleExoCache();
         }
         else
         {
            // An implementation has been defined
            try
            {
               final Class<?> clazz = ClassLoading.loadClass(config.getImplementation(), this);
               return (ExoCache)clazz.newInstance();
            }
            catch (Exception e)
            {
               throw new ExoCacheInitException("Cannot create instance of ExoCache of type "
                  + config.getImplementation(), e);
            }
         }
      }
   }
   
   /**
    * This class is used to reduce the contention when the cache is already created
    */
   private static class FutureExoCacheCreationTask extends FutureTask<ExoCache<? extends Serializable, ?>>
   {

      private volatile ExoCache<? extends Serializable, ?> cache;
      
      /**
       * @param callable
       */
      public FutureExoCacheCreationTask(Callable<ExoCache<? extends Serializable, ?>> callable)
      {
         super(callable);
      }

      @Override
      public ExoCache<? extends Serializable, ?> get() throws InterruptedException, ExecutionException
      {
         if (cache != null)
         {
            return cache;
         }
         return cache = super.get();
      }
   }
}
