/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.services.cache.impl.jboss.ea;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.cache.impl.jboss.AbstractExoCache;
import org.exoplatform.services.cache.impl.jboss.AbstractExoCacheCreator;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.eviction.ExpirationAlgorithm;
import org.jboss.cache.eviction.ExpirationAlgorithmConfig;

import java.io.Serializable;

/**
 * The {@link ExpirationAlgorithm} Implementation of an {@link org.exoplatform.services.cache.impl.jboss.ExoCacheCreator}
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 8 mars 2010  
 */
public class EAExoCacheCreator extends AbstractExoCacheCreator
{

   /**
    * The expected implementation name
    */
   public static final String EXPECTED_IMPL = "EA";

   /**
    * The default value for the parameter expirationTimeout
    */
   protected long defaultExpirationTimeout;

   /**
    * {@inheritDoc}
    */
   public String getExpectedImplementation()
   {
      return EXPECTED_IMPL;
   }

   /**
    * {@inheritDoc}
    */
   public Class<? extends ExoCacheConfig> getExpectedConfigType()
   {
      return EAExoCacheConfig.class;
   }

   /**
    * @see org.exoplatform.services.cache.impl.jboss.ExoCacheCreator#create(org.exoplatform.services.cache.ExoCacheConfig, org.jboss.cache.Cache)
    */
   public ExoCache<Serializable, Object> create(ExoCacheConfig config, Cache<Serializable, Object> cache)
      throws ExoCacheInitException
   {
      if (config instanceof EAExoCacheConfig)
      {
         final EAExoCacheConfig eaConfig = (EAExoCacheConfig)config;
         return create(config, cache, eaConfig.getMaxNodes(), eaConfig.getMinTimeToLive(), eaConfig
            .getExpirationTimeout());
      }
      else
      {
         final long period = config.getLiveTime();
         return create(config, cache, config.getMaxSize(), period > 0 ? period * 1000 : 0, defaultExpirationTimeout);
      }
   }

   /**
    * Creates a new ExoCache instance with the relevant parameters
    */
   private ExoCache<Serializable, Object> create(ExoCacheConfig config, Cache<Serializable, Object> cache,
      int maxNodes, long minTimeToLive, long expirationTimeout) throws ExoCacheInitException
   {
      final ExpirationAlgorithmConfig ea = new ExpirationAlgorithmConfig();
      ea.setMaxNodes(maxNodes);
      ea.setMinTimeToLive(minTimeToLive);
      ea.setExpirationKeyName(ExpirationAlgorithmConfig.EXPIRATION_KEY);

      Fqn<String> rooFqn = addEvictionRegion(config, cache, ea);

      return new EAExoCache(config, cache, rooFqn, ea, expirationTimeout);
   }

   /**
    * The {@link ExpirationAlgorithm} implementation of an ExoCache
    */
   public static class EAExoCache extends AbstractExoCache<Serializable, Object>
   {
      private long expirationTimeout;

      private final ExpirationAlgorithmConfig ea;

      public EAExoCache(ExoCacheConfig config, Cache<Serializable, Object> cache, Fqn<String> rooFqn,
         ExpirationAlgorithmConfig ea, long expirationTimeout)
      {
         super(config, cache, rooFqn);
         this.ea = ea;
         this.expirationTimeout = expirationTimeout;
      }

      /**
       * We set the <code>ExpirationAlgorithmConfig.EXPIRATION_KEY</code> based on the <code>expirationTimeout</code>
       * @see org.exoplatform.services.cache.impl.jboss.AbstractExoCache#putOnly(java.io.Serializable, java.lang.Object)
       */
      @Override
      protected Object putOnly(Serializable key, Object value)
      {
         Fqn<Serializable> fqn = getFqn(key);
         Long future = new Long(System.currentTimeMillis() + expirationTimeout);
         cache.put(fqn, ExpirationAlgorithmConfig.EXPIRATION_KEY, future);
         return cache.put(fqn, key, value);
      }

      public void setMaxSize(int max)
      {
         ea.setMaxNodes(max);
      }

      public void setLiveTime(long period)
      {
         ea.setMinTimeToLive(period);
      }

      @ManagedName("MaxNodes")
      @ManagedDescription("This is the maximum number of nodes allowed in this region. 0 denotes immediate expiry, -1 denotes no limit.")
      public int getMaxSize()
      {
         return ea.getMaxNodes();
      }

      @ManagedName("MinTimeToLive")
      @ManagedDescription("the minimum amount of time a node must be allowed to live after being accessed before it is allowed to be considered for eviction. 0 denotes that this feature is disabled, which is the default value.")
      public long getLiveTime()
      {
         return ea.getMinTimeToLive();
      }

      @Managed
      @ManagedName("ExpirationTimeout")
      @ManagedDescription("This is the timeout after which the cache entry must be evicted.")
      public long getExpirationTimeout()
      {
         return expirationTimeout;
      }

      @Managed
      public void setExpirationTimeout(long expirationTimeout)
      {
         this.expirationTimeout = expirationTimeout;
      }
   }
}
