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
package org.exoplatform.services.cache.impl.infinispan.generic;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.impl.infinispan.AbstractExoCache;
import org.exoplatform.services.cache.impl.infinispan.ExoCacheCreator;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * The generic {@link ExoCacheCreator} for all the expiration available in infinispan.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class GenericExoCacheCreator implements ExoCacheCreator
{

   /**
    * The default value for the eviction strategy
    */
   protected String defaultStrategy = "LRU";

   /**
    * The default value for maxIdle
    */
   protected long defaultMaxIdle = -1;

   /**
    * The default value for wakeUpInterval
    */
   protected long defaultWakeUpInterval = 5000;

   /**
    * A set of all the implementations supported by this creator
    */
   protected Set<String> implementations;

   /**
    * {@inheritDoc}
    */
   public Set<String> getExpectedImplementations()
   {
      return implementations;
   }

   /**
    * {@inheritDoc}
    */
   public Class<? extends ExoCacheConfig> getExpectedConfigType()
   {
      return GenericExoCacheConfig.class;
   }

   /**
    * {@inheritDoc}
    */
   public ExoCache<Serializable, Object> create(ExoCacheConfig config, Configuration cacheConfig,
      Callable<Cache<Serializable, Object>> cacheGetter) throws ExoCacheInitException
   {
      if (config instanceof GenericExoCacheConfig)
      {
         final GenericExoCacheConfig gConfig = (GenericExoCacheConfig)config;
         return create(config, cacheConfig, cacheGetter, gConfig.getStrategy(), gConfig.getMaxEntries(), gConfig
            .getLifespan(), gConfig.getMaxIdle() == 0 ? defaultMaxIdle : gConfig.getMaxIdle(), gConfig
            .getWakeUpInterval() == 0 ? defaultWakeUpInterval : gConfig.getWakeUpInterval());
      }
      else
      {
         final long period = config.getLiveTime();
         return create(config, cacheConfig, cacheGetter, config.getImplementation() == null ? defaultStrategy : config
            .getImplementation(), config.getMaxSize(), period > 0 ? period * 1000 : -1, defaultMaxIdle,
            defaultWakeUpInterval);
      }
   }

   /**
    * Creates a new ExoCache instance with the relevant parameters
    * @throws ExoCacheInitException If any exception occurs while creating the cache
    */
   private ExoCache<Serializable, Object> create(ExoCacheConfig config, Configuration cacheConfig,
      Callable<Cache<Serializable, Object>> cacheGetter, String strategy, int maxEntries, long lifespan, long maxIdle,
      long wakeUpInterval) throws ExoCacheInitException
   {
      cacheConfig.setEvictionStrategy(strategy);
      cacheConfig.setEvictionMaxEntries(maxEntries);
      cacheConfig.setExpirationLifespan(lifespan);
      cacheConfig.setExpirationMaxIdle(maxIdle);
      cacheConfig.setEvictionWakeUpInterval(wakeUpInterval);
      try
      {
         return new GenericExoCache(cacheConfig, config, cacheGetter.call());
      }
      catch (Exception e)
      {
         throw new ExoCacheInitException("Cannot create the cache '" + config.getName() + "'", e);
      }
   }

   /**
    * The Generic implementation of an ExoCache
    */
   public static class GenericExoCache extends AbstractExoCache<Serializable, Object>
   {

      private final Configuration cacheConfig;

      public GenericExoCache(Configuration cacheConfig, ExoCacheConfig config, Cache<Serializable, Object> cache)
      {
         super(config, cache);
         this.cacheConfig = cacheConfig;
      }

      public void setMaxSize(int max)
      {
         cacheConfig.setEvictionMaxEntries(max);
      }

      public void setLiveTime(long period)
      {
         cacheConfig.setExpirationLifespan(period);
      }

      public void setMaxIdle(long maxIdle)
      {
         cacheConfig.setExpirationMaxIdle(maxIdle);
      }

      public void setWakeUpInterval(long wakeUpInterval)
      {
         cacheConfig.setEvictionWakeUpInterval(wakeUpInterval);
      }

      @ManagedName("MaxEntries")
      @ManagedDescription("Maximum number of entries in a cache instance. -1 means no limit.")
      public int getMaxSize()
      {
         return cacheConfig.getEvictionMaxEntries();
      }

      @ManagedName("Lifespan")
      @ManagedDescription("Maximum lifespan of a cache entry, after which the entry is expired cluster-wide." +
      		" -1 means the entries never expire.")
      public long getLiveTime()
      {
         return cacheConfig.getExpirationLifespan();
      }

      @Managed
      @ManagedName("MaxIdle")
      @ManagedDescription("Maximum idle time a cache entry will be maintained in the cache. " +
      		"If the idle time is exceeded, the entry will be expired cluster-wide. -1 means the entries never expire.")
      public long getMaxIdle()
      {
         return cacheConfig.getExpirationMaxIdle();
      }

      @Managed
      @ManagedName("WakeUpInterval")
      @ManagedDescription("Interval between subsequent eviction runs. If you wish to disable the periodic eviction " +
      		"process altogether, set wakeupInterval to -1.")
      public long getWakeUpInterval()
      {
         return cacheConfig.getEvictionWakeUpInterval();
      }
   }
}