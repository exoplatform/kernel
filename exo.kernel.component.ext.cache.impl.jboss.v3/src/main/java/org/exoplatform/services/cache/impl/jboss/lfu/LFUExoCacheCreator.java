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
package org.exoplatform.services.cache.impl.jboss.lfu;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.impl.jboss.AbstractExoCache;
import org.exoplatform.services.cache.impl.jboss.ExoCacheCreator;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.config.Configuration;
import org.jboss.cache.config.EvictionConfig;
import org.jboss.cache.config.EvictionRegionConfig;
import org.jboss.cache.eviction.LFUAlgorithmConfig;

import java.io.Serializable;

/**
 * The LFU Implementation of an {@link org.exoplatform.services.cache.impl.jboss.ExoCacheCreator}
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 21 juil. 2009  
 */
public class LFUExoCacheCreator implements ExoCacheCreator
{

   /**
    * The expected implementation name
    */
   public static final String EXPECTED_IMPL = "LFU";

   /**
    * The default value for the parameter minNodes
    */
   protected int defaultMinNodes;

   /**
    * {@inheritDoc}
    */
   public ExoCache<Serializable, Object> create(ExoCacheConfig config, Cache<Serializable, Object> cache) throws ExoCacheInitException
   {
      if (config instanceof LFUExoCacheConfig)
      {
         final LFUExoCacheConfig lfuConfig = (LFUExoCacheConfig)config;
         return create(config, cache, lfuConfig.getMaxNodes(), lfuConfig.getMinNodes(), lfuConfig.getMinTimeToLive());
      }
      else
      {
         final long period = config.getLiveTime();
         return create(config, cache, config.getMaxSize(), defaultMinNodes, period > 0 ? period * 1000 : 0);
      }
   }

   /**
    * Creates a new ExoCache instance with the relevant parameters
    */
   private ExoCache<Serializable, Object> create(ExoCacheConfig config, Cache<Serializable, Object> cache, int maxNodes, int minNodes,
      long minTimeToLive) throws ExoCacheInitException
   {
      final Configuration configuration = cache.getConfiguration();
      final LFUAlgorithmConfig lfu = new LFUAlgorithmConfig(maxNodes, minNodes);
      lfu.setMinTimeToLive(minTimeToLive);
      // Create an eviction region config
      final EvictionRegionConfig erc = new EvictionRegionConfig(Fqn.ROOT, lfu);

      final EvictionConfig evictionConfig = configuration.getEvictionConfig();
      evictionConfig.setDefaultEvictionRegionConfig(erc);
      return new LFUExoCache(config, cache, lfu);
   }

   /**
    * {@inheritDoc}
    */
   public Class<? extends ExoCacheConfig> getExpectedConfigType()
   {
      return LFUExoCacheConfig.class;
   }

   /**
    * {@inheritDoc}
    */
   public String getExpectedImplementation()
   {
      return EXPECTED_IMPL;
   }

   /**
    * The LRU implementation of an ExoCache
    */
   public static class LFUExoCache extends AbstractExoCache<Serializable, Object>
   {

      private final LFUAlgorithmConfig lfu;

      public LFUExoCache(ExoCacheConfig config, Cache<Serializable, Object> cache, LFUAlgorithmConfig lfu)
      {
         super(config, cache);
         this.lfu = lfu;
      }

      @ManagedName("MinTimeToLive")
      @ManagedDescription("the minimum amount of time a node must be allowed to live after being accessed before it is allowed to be considered for eviction. 0 denotes that this feature is disabled, which is the default value.")
      public long getLiveTime()
      {
         return lfu.getMinTimeToLive();
      }

      @ManagedName("MaxNodes")
      @ManagedDescription("This is the maximum number of nodes allowed in this region. 0 denotes immediate expiry, -1 denotes no limit.")
      public int getMaxSize()
      {
         return lfu.getMaxNodes();
      }

      @Managed
      @ManagedName("MinNodes")
      @ManagedDescription("This is the minimum number of nodes allowed in this region. This value determines what the eviction queue should prune down to per pass. e.g. If minNodes is 10 and the cache grows to 100 nodes, the cache is pruned down to the 10 most frequently used nodes when the eviction timer makes a pass through the eviction algorithm.")
      public long getMinNodes()
      {
         return lfu.getMinNodes();
      }

      public void setLiveTime(long period)
      {
         lfu.setMinTimeToLive(period);
      }

      public void setMaxSize(int max)
      {
         lfu.setMaxNodes(max);
      }

      @Managed
      public void setMinNodes(int minNodes)
      {
         lfu.setMinNodes(minNodes);
      }
   }
}
