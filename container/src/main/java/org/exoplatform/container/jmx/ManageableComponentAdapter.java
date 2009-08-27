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
package org.exoplatform.container.jmx;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManageableComponentAdapter implements ComponentAdapter
{

   /** . */
   private Log log = ExoLogger.getLogger(ManageableComponentAdapter.class);

   /** . */
   private ComponentAdapter delegate;

   /** . */
   private final ManageableContainer container;

   /** . */
   private volatile boolean registered = false;

   public ManageableComponentAdapter(ManageableContainer container, ComponentAdapter delegate)
   {
      this.delegate = delegate;
      this.container = container;
   }

   public Object getComponentKey()
   {
      return delegate.getComponentKey();
   }

   public Class getComponentImplementation()
   {
      return delegate.getComponentImplementation();
   }

   public Object getComponentInstance(PicoContainer pico) throws PicoInitializationException,
      PicoIntrospectionException
   {
      Object instance = delegate.getComponentInstance(pico);

      //
      if (!registered)
      {
         registered = true;

         //
         if (container.managementContext != null)
         {
            log.debug("==> add " + instance + " to a mbean server");
            container.managementContext.register(instance);
         }
      }
      return instance;
   }

   public void verify(PicoContainer container) throws PicoIntrospectionException
   {
      delegate.verify(container);
   }

   public void accept(PicoVisitor visitor)
   {
      delegate.accept(visitor);
   }
}
