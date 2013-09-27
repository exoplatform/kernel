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

import org.exoplatform.container.ConcurrentContainerMT;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapterMT;
import org.exoplatform.container.spi.Container;
import org.exoplatform.management.spi.ManagementProvider;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.annotation.Annotation;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ManageableComponentAdapterMT<T> extends MX4JComponentAdapterMT<T>
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 5165449586256525854L;

   /** . */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.ManageableComponentAdapterMT");

   /** . */
   private final AtomicBoolean registered = new AtomicBoolean();

   public ManageableComponentAdapterMT(ExoContainer holder, ConcurrentContainerMT container, Object key,
      Class<T> implementation)
   {
      super(holder, container, key, implementation);
   }

   protected void register(Container co, Object instance)
   {
      if (registered.compareAndSet(false, true))
      {
         do
         {
            if (co instanceof ManageableContainer)
            {
               break;
            }
         }
         while ((co = co.getSuccessor()) != null);
         if (co instanceof ManageableContainer)
         {
            ManageableContainer container = (ManageableContainer)co;
            if (container.managementContext != null)
            {
               // Register the instance against the management context
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

   /**
    * {@inheritDoc}
    */
   @Override
   public T create(CreationalContext<T> creationalContext)
   {
      T instance = super.create(creationalContext);
      Class<? extends Annotation> scope = null;
      if (instance != null && (((scope = getScope()) != null && !scope.equals(Dependent.class))) || isSingleton())
      {
         register(exocontainer, instance);
      }
      return instance;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void destroy(T instance, CreationalContext<T> creationalContext)
   {
      try
      {
         Container co = exocontainer;
         do
         {
            if (co instanceof ManageableContainer)
            {
               break;
            }
         }
         while ((co = co.getSuccessor()) != null);
         if (co instanceof ManageableContainer)
         {
            ManageableContainer container = (ManageableContainer)co;
            if (container.managementContext != null)
            {
               // UnRegister the instance against the management context
               if (LOG.isDebugEnabled())
                  LOG.debug("==> remove " + instance + " from a mbean server");
               container.managementContext.unregister(instance);
            }
         }
         creationalContext.release();
      }
      catch (Exception e)
      {
         LOG.error("Could not destroy the instance " + instance + ": " + e.getMessage());
      }
   }
}