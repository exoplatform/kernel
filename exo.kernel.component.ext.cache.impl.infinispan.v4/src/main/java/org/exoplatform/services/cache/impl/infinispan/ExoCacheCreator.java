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

import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.infinispan.Cache;
import org.infinispan.config.Configuration;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * This class is used to create the cache according to the given 
 * configuration {@link org.exoplatform.services.cache.ExoCacheConfig}
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public interface ExoCacheCreator
{

   /**
    * Creates an eXo cache according to the given configuration {@link org.exoplatform.services.cache.ExoCacheConfig}
    * @param config the configuration of the cache to apply
    * @param cacheConfig the configuration of the infinispan cache
    * @param cacheGetter a {@link Callable} instance from which we can get the cache
    * @exception ExoCacheInitException if an exception happens while initializing the cache
    */
   public ExoCache<Serializable, Object> create(ExoCacheConfig config, Configuration cacheConfig, Callable<Cache<Serializable, Object>> cacheGetter) throws ExoCacheInitException;

   /**
    * Returns the type of {@link org.exoplatform.services.cache.ExoCacheConfig} expected by the creator  
    * @return the expected type
    */
   public Class<? extends ExoCacheConfig> getExpectedConfigType();

   /**
    * Returns a set of all the implementations expected by the creator. This is mainly used to be backward compatible
    * @return the expected by the creator
    */
   public Set<String> getExpectedImplementations();
}
