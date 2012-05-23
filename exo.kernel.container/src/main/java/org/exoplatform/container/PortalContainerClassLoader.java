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
package org.exoplatform.container;

import java.lang.ref.WeakReference;
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
    * The weak reference to the related portal container
    */
   private volatile WeakReference<PortalContainer> containerRef;

   /**
    * The name of the related portal container used in case of developing mode
    */
   private final String portalContainerName;

   PortalContainerClassLoader(PortalContainer container)
   {
      super(getClassLoaders(container));
      // In case of developing mode we want to avoid to use hard reference in case 
      // we would like to reload the container
      this.containerRef = new WeakReference<PortalContainer>(container);
      this.portalContainerName = container.getName();
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
      return getClassLoaders(getPortalContainer());
   }
   
   private PortalContainer getPortalContainer()
   {
      PortalContainer container = containerRef.get();
      if (container != null)
      {
         return container;
      }
      container = RootContainer.getInstance().getPortalContainer(portalContainerName);
      containerRef = new WeakReference<PortalContainer>(container);
      return container;
   }
}
