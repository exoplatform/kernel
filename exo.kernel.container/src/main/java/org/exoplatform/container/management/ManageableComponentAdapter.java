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
package org.exoplatform.container.management;

import org.exoplatform.container.ConcurrentContainer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapter;
import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManageableComponentAdapter<T> extends MX4JComponentAdapter<T>
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 8922696628680586728L;

   /** . */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ManageableComponentAdapter");

   /** . */
   private final AtomicBoolean registered = new AtomicBoolean();

   public ManageableComponentAdapter(ExoContainer holder, ConcurrentContainer container, Object key,
      Class<T> implementation)
   {
      super(holder, container, key, implementation);
   }

   public T getComponentInstance() throws ContainerException
   {
      T instance = super.getComponentInstance();

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
}
