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

import org.exoplatform.services.cache.impl.jboss.AbstractExoCacheConfig;
import org.jboss.cache.eviction.ExpirationAlgorithm;


/**
 * The {@link org.exoplatform.services.cache.ExoCacheConfig} for the {@link ExpirationAlgorithm} 
 * implementation
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 8 mars 2010  
 */
public class EAExoCacheConfig extends AbstractExoCacheConfig
{

   private int maxNodes;
   
   private long minTimeToLive;
   
   private long expirationTimeout;

   public int getMaxNodes()
   {
      return maxNodes;
   }

   public void setMaxNodes(int maxNodes)
   {
      this.maxNodes = maxNodes;
   }

   public long getMinTimeToLive()
   {
      return minTimeToLive;
   }

   public void setMinTimeToLive(long minTimeToLive)
   {
      this.minTimeToLive = minTimeToLive;
   }
   
   public long getExpirationTimeout()
   {
      return expirationTimeout;
   }

   public void setExpirationTimeout(long expirationTimeout)
   {
      this.expirationTimeout = expirationTimeout;
   }
}
