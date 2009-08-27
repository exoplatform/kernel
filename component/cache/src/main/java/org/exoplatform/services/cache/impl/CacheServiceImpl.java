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

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.management.annotations.ManagedBy;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheConfigPlugin;
import org.exoplatform.services.cache.SimpleExoCache;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by The eXo Platform SAS. Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Sat, Sep 13, 2003 @ Time: 1:12:22 PM
 */
@ManagedBy(CacheServiceManaged.class)
public class CacheServiceImpl implements CacheService
{
   private HashMap<String, ExoCacheConfig> configs_ = new HashMap<String, ExoCacheConfig>();

   private final ConcurrentHashMap<String, ExoCache<? extends Serializable, ?>> cacheMap_ =
      new ConcurrentHashMap<String, ExoCache<? extends Serializable, ?>>();

   private ExoCacheConfig defaultConfig_;

   private LoggingCacheListener loggingListener_;

   CacheServiceManaged managed;

   public CacheServiceImpl(InitParams params) throws Exception
   {
      List<ExoCacheConfig> configs = params.getObjectParamValues(ExoCacheConfig.class);
      for (ExoCacheConfig config : configs)
      {
         configs_.put(config.getName(), config);
      }
      defaultConfig_ = configs_.get("default");
      loggingListener_ = new LoggingCacheListener();
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

   public <K extends Serializable, V> ExoCache<K, V> getCacheInstance(String region)
   {
      if (region == null)
      {
         throw new NullPointerException("region cannot be null");
      }
      if (region.length() == 0)
      {
         throw new IllegalArgumentException("region cannot be empty");
      }
      ExoCache<? extends Serializable, ?> cache = cacheMap_.get(region);
      if (cache == null)
      {
         try
         {
            cache = createCacheInstance(region);
            ExoCache<? extends Serializable, ?> existing = cacheMap_.putIfAbsent(region, cache);
            if (existing != null)
            {
               cache = existing;
            }
         }
         catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      return (ExoCache<K, V>)cache;
   }

   synchronized private ExoCache<? extends Serializable, ?> createCacheInstance(String region) throws Exception
   {
      ExoCacheConfig config = configs_.get(region);
      if (config == null)
         config = defaultConfig_;
      ExoCache<? extends Serializable, ?> cache;
      if (config.getImplementation() == null)
      {
         cache = new SimpleExoCache<Serializable, Object>();
      }
      else
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         Class<ExoCache<? extends Serializable, ?>> clazz =
            (Class<ExoCache<? extends Serializable, ?>>)cl.loadClass(config.getImplementation());
         cache = clazz.newInstance();
      }
      cache.setName(region);
      cache.setLabel(config.getLabel());
      cache.setMaxSize(config.getMaxSize());
      cache.setLiveTime(config.getLiveTime());
      cache.setLogEnabled(config.isLogEnabled());
      if (cache.isLogEnabled())
      {
         cache.addCacheListener(loggingListener_);
      }

      //
      if (managed != null)
      {
         managed.registerCache(cache);
      }

      //
      return cache;
   }

   public Collection<ExoCache<? extends Serializable, ?>> getAllCacheInstances()
   {
      return cacheMap_.values();
   }
}
