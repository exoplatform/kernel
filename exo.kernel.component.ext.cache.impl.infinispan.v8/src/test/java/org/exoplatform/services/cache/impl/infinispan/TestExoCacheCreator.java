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

import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CachedObjectSelector;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestExoCacheCreator implements ExoCacheCreator
{

   /**
    * {@inheritDoc}
    */
   @Override
   public ExoCache<Serializable, Object> create(ExoCacheConfig config, ConfigurationBuilder confBuilder,
      Callable<Cache<Serializable, Object>> cacheGetter) throws ExoCacheInitException
   {
      return new TestExoCache();
   }

   public Class<? extends ExoCacheConfig> getExpectedConfigType()
   {
      return TestExoCacheConfig.class;
   }

   public Set<String> getExpectedImplementations()
   {
      return Collections.singleton("TEST");
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   public static class TestExoCache implements ExoCache<Serializable, Object>
   {

      public void addCacheListener(CacheListener listener)
      {
      }

      public int getCacheHit()
      {
         return 0;
      }

      public int getCacheMiss()
      {
         return 0;
      }

      public int getCacheSize()
      {
         return 0;
      }

      public List getCachedObjects()
      {
         return null;
      }

      public String getLabel()
      {
         return null;
      }

      public long getLiveTime()
      {
         return 0;
      }

      public int getMaxSize()
      {
         return 0;
      }

      public String getName()
      {
         return "name";
      }

      public boolean isDistributed()
      {
         return false;
      }

      public boolean isLogEnabled()
      {
         return false;
      }

      public boolean isReplicated()
      {
         return false;
      }

      public void select(CachedObjectSelector selector) throws Exception
      {

      }

      public void setDistributed(boolean b)
      {

      }

      public void setLabel(String s)
      {

      }

      public void setLiveTime(long period)
      {

      }

      public void setLogEnabled(boolean b)
      {

      }

      public void setMaxSize(int max)
      {

      }

      public void setName(String name)
      {

      }

      public void setReplicated(boolean b)
      {

      }

      public void clearCache()
      {

      }

      public Object get(Serializable key)
      {
         return null;
      }

      public void put(Serializable key, Object value) throws NullPointerException
      {

      }

      public void putMap(Map objs) throws NullPointerException, IllegalArgumentException
      {

      }

      public Object remove(Serializable key) throws NullPointerException
      {
         return null;
      }

      public List removeCachedObjects()
      {
         return null;
      }

   }
}
