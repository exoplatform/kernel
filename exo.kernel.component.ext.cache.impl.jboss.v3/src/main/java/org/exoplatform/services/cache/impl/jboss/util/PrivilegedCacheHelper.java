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
package org.exoplatform.services.cache.impl.jboss.util;

import org.jboss.cache.Cache;
import org.jboss.cache.CacheException;
import org.jboss.cache.CacheFactory;
import org.jboss.cache.DefaultCacheFactory;
import org.jboss.cache.Fqn;
import org.jboss.cache.config.ConfigurationException;

import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: PrivilegedCacheHelper.java 111 2010-11-11 11:11:11Z tolusha $
 *
 */
public class PrivilegedCacheHelper
{
   /**
    * Start cache in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> void start(final Cache<K, V> cache)
   {
      PrivilegedAction<Object> action = new PrivilegedAction<Object>()
      {
         public Object run()
         {
            cache.start();
            return null;
         }
      };
      AccessController.doPrivileged(action);
   }

   /**
    * Stop cache in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> void stop(final Cache<K, V> cache)
   {
      PrivilegedAction<Object> action = new PrivilegedAction<Object>()
      {
         public Object run()
         {
            cache.stop();
            return null;
         }
      };
      AccessController.doPrivileged(action);
   }

   /**
    * Create cache in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> void create(final Cache<K, V> cache)
   {
      PrivilegedAction<Object> action = new PrivilegedAction<Object>()
      {
         public Object run()
         {
            cache.create();
            return null;
         }
      };
      AccessController.doPrivileged(action);
   }

   /**
    * End batch in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> void endBatch(final Cache<K, V> cache, final boolean successful)
   {
      PrivilegedAction<Object> action = new PrivilegedAction<Object>()
      {
         public Object run()
         {
            cache.endBatch(successful);
            return null;
         }
      };
      AccessController.doPrivileged(action);
   }

   /**
    * Create cache in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> Cache<K, V> createCache(final CacheFactory<K, V> factory, final InputStream is,
      final boolean start)
   {
      PrivilegedExceptionAction<Cache<K, V>> action = new PrivilegedExceptionAction<Cache<K, V>>()
      {
         public Cache<K, V> run() throws Exception
         {
            return factory.createCache(is, start);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof ConfigurationException)
         {
            throw (ConfigurationException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Put in cache in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> V put(final Cache<K, V> cache, final String fqn, final K key, final V value)
      throws CacheException
   {
      PrivilegedExceptionAction<V> action = new PrivilegedExceptionAction<V>()
      {
         public V run() throws Exception
         {
            return cache.put(fqn, key, value);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IllegalStateException)
         {
            throw (IllegalStateException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Remove fomr cache in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> boolean removeNode(final Cache<K, V> cache, final Fqn fqn) throws CacheException
   {
      PrivilegedExceptionAction<Boolean> action = new PrivilegedExceptionAction<Boolean>()
      {
         public Boolean run() throws Exception
         {
            return cache.removeNode(fqn);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IllegalStateException)
         {
            throw (IllegalStateException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Put in cache in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> V put(final Cache<K, V> cache, final Fqn fqn, final K key, final V value) throws CacheException
   {
      PrivilegedExceptionAction<V> action = new PrivilegedExceptionAction<V>()
      {
         public V run() throws Exception
         {
            return cache.put(fqn, key, value);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IllegalStateException)
         {
            throw (IllegalStateException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create cache factory in privileged mode.
    * 
    * @param cache
    */
   public static <K, V> DefaultCacheFactory<K, V> createCacheFactory() throws CacheException
   {
      PrivilegedAction<DefaultCacheFactory<K, V>> action = new PrivilegedAction<DefaultCacheFactory<K, V>>()
      {
         public DefaultCacheFactory<K, V> run()
         {
            return new DefaultCacheFactory<K, V>();
         }
      };
      return AccessController.doPrivileged(action);
   }
}
