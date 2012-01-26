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

import org.exoplatform.management.ManagementAware;
import org.exoplatform.management.ManagementContext;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@Managed
@NameTemplate(@Property(key = "service", value = "cachemanager"))
@ManagedDescription("Cache manager")
public class CacheServiceManaged implements ManagementAware
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.cache.CacheServiceManaged");

   /** . */
   private ManagementContext context;

   /** . */
   private CacheServiceImpl cacheService;

   public CacheServiceManaged(CacheServiceImpl cacheService)
   {
      this.cacheService = cacheService;

      //
      cacheService.managed = this;
   }

   @Managed
   @ManagedDescription("Clear all registered cache instances")
   public void clearCaches()
   {
      for (Object o : cacheService.getAllCacheInstances())
      {
         try
         {
            ((ExoCache)o).clearCache();
         }
         catch (Exception wtf)
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("An exception occurred: " + wtf.getMessage());
            }
         }
      }
   }

   public void setContext(ManagementContext context)
   {
      this.context = context;
   }

   void registerCache(ExoCache cache)
   {
      if (context != null)
      {
         context.register(cache);
      }
   }
}
