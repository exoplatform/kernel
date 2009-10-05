/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.container;

import java.util.Set;

/**
 * This class defined the {@link ClassLoader} of a portal container
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 15 sept. 2009  
 */
class PortalContainerClassLoader extends UnifiedClassLoader
{

   /**
    * The related portal container
    */
   private final PortalContainer container;

   PortalContainerClassLoader(PortalContainer container)
   {
      super(getClassLoaders(container));
      this.container = container;
   }

   /**
    * Retrieves the list of all the {@link ClassLoader} that are associated to the given
    * portal container
    */
   private static ClassLoader[] getClassLoaders(PortalContainer container)
   {
      final Set<WebAppInitContext> contexts = container.getWebAppInitContexts();
      final ClassLoader[] cls = new ClassLoader[contexts.size()];
      int i = 0;
      for (WebAppInitContext ctx : contexts)
      {
         cls[i++] = ctx.getWebappClassLoader();
      }
      return cls;
   }

   /**
    * {@inheritDoc}
    */
   protected ClassLoader[] getClassLoaders()
   {
      return getClassLoaders(container);
   }
}
