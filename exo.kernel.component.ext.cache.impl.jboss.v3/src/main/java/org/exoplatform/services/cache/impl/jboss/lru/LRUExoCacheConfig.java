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
package org.exoplatform.services.cache.impl.jboss.lru;

import org.exoplatform.services.cache.ExoCacheConfig;

/**
 * The {@link org.exoplatform.services.cache.ExoCacheConfig} for the LRU implementation
 * 
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 21 juil. 2009  
 */
public class LRUExoCacheConfig extends ExoCacheConfig
{

   private int maxNodes;

   private long timeToLive;

   private long maxAge;

   private long minTimeToLive;

   public int getMaxNodes()
   {
      return maxNodes;
   }

   public void setMaxNodes(int maxNodes)
   {
      this.maxNodes = maxNodes;
   }

   public long getTimeToLive()
   {
      return timeToLive;
   }

   public void setTimeToLive(long timeToLive)
   {
      this.timeToLive = timeToLive;
   }

   public long getMaxAge()
   {
      return maxAge;
   }

   public void setMaxAge(long maxAge)
   {
      this.maxAge = maxAge;
   }

   public long getMinTimeToLive()
   {
      return minTimeToLive;
   }

   public void setMinTimeToLive(long minTimeToLive)
   {
      this.minTimeToLive = minTimeToLive;
   }
}
