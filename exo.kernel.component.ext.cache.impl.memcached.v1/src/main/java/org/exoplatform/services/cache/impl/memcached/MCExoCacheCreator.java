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
package org.exoplatform.services.cache.impl.memcached;

import net.spy.memcached.MemcachedClient;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;

import java.io.Serializable;

/**
 * The implementation of an {@link ExoCacheCreator} based on the LRU expiration algorithm
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class MCExoCacheCreator
{

   /**
    * The default value for the parameter expirationTimeout
    */
   private final long defaultExpirationTimeout;

   public MCExoCacheCreator(long defaultExpirationTimeout)
   {
      this.defaultExpirationTimeout = defaultExpirationTimeout;
   }

   /**
    * {@inheritDoc}
    */
   public ExoCache<Serializable, Object> create(ExoContainerContext ctx, ExoCacheConfig config, MemcachedClient cache)
      throws ExoCacheInitException
   {
      if (config instanceof MCExoCacheConfig)
      {
         final MCExoCacheConfig eaConfig = (MCExoCacheConfig)config;
         return create(ctx, config, cache, eaConfig.getExpirationTimeout());
      }
      else
      {
         final long period = config.getLiveTime();
         return create(ctx, config, cache, period > 0 ? period * 1000 : defaultExpirationTimeout);
      }
   }

   /**
    * Creates a new ExoCache instance with the relevant parameters
    */
   private ExoCache<Serializable, Object> create(ExoContainerContext ctx, ExoCacheConfig config, MemcachedClient cache,
      long expirationTimeout) throws ExoCacheInitException
   {
      return new MCExoCache<Serializable, Object>(ctx, config, cache, expirationTimeout);
   }
}
