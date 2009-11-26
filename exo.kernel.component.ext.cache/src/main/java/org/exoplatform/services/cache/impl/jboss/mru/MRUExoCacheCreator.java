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
package org.exoplatform.services.cache.impl.jboss.mru;

import java.io.Serializable;

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
import org.jboss.cache.eviction.MRUAlgorithmConfig;

/**
 * The MRU Implementation of an {@link org.exoplatform.services.cache.impl.jboss.ExoCacheCreator}
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 21 juil. 2009  
 */
public class MRUExoCacheCreator implements ExoCacheCreator
{

   /**
    * The expected implementation name
    */
   public static final String EXPECTED_IMPL = "MRU";

   /**
    * {@inheritDoc}
    */
   public ExoCache create(ExoCacheConfig config, Cache<Serializable, Object> cache) throws ExoCacheInitException
   {
      if (config instanceof MRUExoCacheConfig)
      {
         final MRUExoCacheConfig mruConfig = (MRUExoCacheConfig)config;
         return create(config, cache, mruConfig.getMaxNodes(), mruConfig.getMinTimeToLive());
      }
      else
      {
         final long period = config.getLiveTime();
         return create(config, cache, config.getMaxSize(), period > 0 ? period * 1000 : 0);
      }
   }

   /**
    * Creates a new ExoCache instance with the relevant parameters
    */
   private ExoCache create(ExoCacheConfig config, Cache<Serializable, Object> cache, int maxNodes, long minTimeToLive)
      throws ExoCacheInitException
   {
      final Configuration configuration = cache.getConfiguration();
      final MRUAlgorithmConfig mru = new MRUAlgorithmConfig(maxNodes);
      mru.setMinTimeToLive(minTimeToLive);
      // Create an eviction region config
      final EvictionRegionConfig erc = new EvictionRegionConfig(Fqn.ROOT, mru);

      final EvictionConfig evictionConfig = configuration.getEvictionConfig();
      evictionConfig.setDefaultEvictionRegionConfig(erc);

      return new AbstractExoCache(config, cache)
      {

         public void setMaxSize(int max)
         {
            mru.setMaxNodes(max);
         }

         public void setLiveTime(long period)
         {
            mru.setMinTimeToLive(period);
         }

         @ManagedName("MaxNodes")
         @ManagedDescription("This is the maximum number of nodes allowed in this region. 0 denotes immediate expiry, -1 denotes no limit.")
         public int getMaxSize()
         {
            return mru.getMaxNodes();
         }

         @ManagedName("MinTimeToLive")
         @ManagedDescription("the minimum amount of time a node must be allowed to live after being accessed before it is allowed to be considered for eviction. 0 denotes that this feature is disabled, which is the default value.")
         public long getLiveTime()
         {
            return mru.getMinTimeToLive();
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   public Class<? extends ExoCacheConfig> getExpectedConfigType()
   {
      return MRUExoCacheConfig.class;
   }

   /**
    * {@inheritDoc}
    */
   public String getExpectedImplementation()
   {
      return EXPECTED_IMPL;
   }
}
