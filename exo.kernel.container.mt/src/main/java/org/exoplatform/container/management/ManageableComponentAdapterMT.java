/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container.management;

import org.exoplatform.container.ComponentTaskContext;
import org.exoplatform.container.ConcurrentContainerMT;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterMT;
import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ManageableComponentAdapterMT extends MX4JComponentAdapterMT
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 8922696628680586728L;

   /** . */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.ManageableComponentAdapterMT");

   /** . */
   private final AtomicBoolean registered = new AtomicBoolean();

   public ManageableComponentAdapterMT(ExoContainer holder, ConcurrentContainerMT container, Object key,
      Class<?> implementation)
   {
      super(holder, container, key, implementation);
   }

   public Object getComponentInstance() throws ContainerException
   {
      Object instance = super.getComponentInstance();

      //
      if (instance != null)
      {
         register(exocontainer, instance);
      }
      return instance;
   }

   protected void register(Container co, Object instance)
   {
      do
      {
         if (co instanceof ManageableContainer)
         {
            break;
         }
      }
      while ((co = co.getSuccessor()) != null);
      if (co instanceof ManageableContainer && registered.compareAndSet(false, true))
      {
         ManageableContainer container = (ManageableContainer)co;
         if (container.managementContext != null)
         {
            // Registry the instance against the management context
            if (LOG.isDebugEnabled())
               LOG.debug("==> add " + instance + " to a mbean server");
            container.managementContext.register(instance);

            // Register if it is a management provider
            if (instance instanceof ManagementProvider)
            {
               ManagementProvider provider = (ManagementProvider)instance;
               container.addProvider(provider);
            }
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public Object getComponentInstance(ComponentTaskContext ctx)
   {
      Object instance = super.getComponentInstance(ctx);
      if (instance != null)
      {
         register(exocontainer, instance);
      }
      return instance;
   }
}