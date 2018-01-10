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
package org.exoplatform.services.cache.concurrent;

import org.exoplatform.services.cache.CacheInfo;
import org.exoplatform.services.cache.CacheListener;
import org.exoplatform.services.cache.CacheListenerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ListenerContext<K, V> implements CacheListenerContext, CacheInfo
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.cache.ListenerContext");

   /** . */
   private final ConcurrentFIFOExoCache cache;

   /** . */
   final CacheListener<? super K, ? super V> listener;

   public ListenerContext(CacheListener<? super K, ? super V> listener, ConcurrentFIFOExoCache cache)
   {
      this.listener = listener;
      this.cache = cache;
   }

   public CacheInfo getCacheInfo()
   {
      return this;
   }

   public String getName()
   {
      return cache.getName();
   }

   public int getMaxSize()
   {
      return cache.getMaxSize();
   }

   public long getLiveTime()
   {
      return cache.getLiveTime();
   }

   public int getSize()
   {
      return cache.getCacheSize();
   }

   public void onExpire(K key, V obj)
   {
      try
      {
         listener.onExpire(this, key, obj);
      }
      catch (Exception ignore)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + ignore.getMessage());
         }
      }
   }

   public void onRemove(K key, V obj)
   {
      try
      {
         listener.onRemove(this, key, obj);
      }
      catch (Exception ignore)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + ignore.getMessage());
         }
      }
   }

   public void onPut(K key, V obj)
   {
      try
      {
         listener.onPut(this, key, obj);
      }
      catch (Exception ignore)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + ignore.getMessage());
         }
      }
   }

   public void onPutLocal(K key, V obj)
   {
      try
      {
         listener.onPutLocal(this, key, obj);
      }
      catch (Exception ignore)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + ignore.getMessage());
         }
      }
   }

   public void onGet(K key, V obj)
   {
      try
      {
         listener.onGet(this, key, obj);
      }
      catch (Exception ignore)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + ignore.getMessage());
         }
      }
   }

   public void onClearCache()
   {
      try
      {
         listener.onClearCache(this);
      }
      catch (Exception ignore)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + ignore.getMessage());
         }
      }
   }
}
