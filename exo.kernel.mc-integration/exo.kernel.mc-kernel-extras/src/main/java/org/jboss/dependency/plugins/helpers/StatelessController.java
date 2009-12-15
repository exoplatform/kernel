/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.jboss.dependency.plugins.helpers;

import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.dependency.plugins.AbstractController;
import org.jboss.dependency.spi.ControllerContext;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.jboss.kernel.spi.event.KernelEvent;
import org.jboss.kernel.spi.event.KernelEventFilter;
import org.jboss.kernel.spi.event.KernelEventListener;

import java.util.Set;

/**
 * Stateless controller.
 *
 * @author <a href="ales.justin@jboss.com">Ales Justin</a>
 */
public class StatelessController extends AbstractController implements KernelController
{
   private KernelController controller;

   public StatelessController(KernelController controller)
   {
      if (controller == null)
         throw new IllegalArgumentException("Null controller");

      this.controller = controller;
      for (ControllerState state : controller.getStates())
         addState(state, null);
   }

   public void enableOnDemand(ControllerContext context) throws Throwable
   {
      // ignore
   }

   @Override
   protected void registerControllerContext(ControllerContext context)
   {
      // do nothing
   }

   @Override
   public void install(ControllerContext context) throws Throwable
   {
      super.install(context);
   }

   public ControllerContext uninstall(Object name)
   {
      return null;
   }

   public ControllerContext getContext(Object name, ControllerState state)
   {
      return controller.getContext(name, state);
   }

   public ControllerContext getInstalledContext(Object name)
   {
      return controller.getInstalledContext(name);
   }

   public boolean isShutdown()
   {
      return controller.isShutdown();
   }

   public void shutdown()
   {
   }

   public Kernel getKernel()
   {
      return controller.getKernel();
   }

   public void setKernel(Kernel kernel) throws Throwable
   {
      controller.setKernel(kernel);
   }

   public KernelControllerContext install(BeanMetaData metaData) throws Throwable
   {
      return controller.install(metaData);
   }

   public KernelControllerContext install(BeanMetaData metaData, Object target) throws Throwable
   {
      return controller.install(metaData, target);
   }

   public void addSupplies(KernelControllerContext context)
   {
      controller.addSupplies(context);
   }

   public void removeSupplies(KernelControllerContext context)
   {
      controller.removeSupplies(context);
   }

   public Set<KernelControllerContext> getInstantiatedContexts(Class<?> clazz)
   {
      return controller.getInstantiatedContexts(clazz);
   }

   public Set<KernelControllerContext> getContexts(Class<?> clazz, ControllerState state)
   {
      return controller.getContexts(clazz, state);
   }

   public KernelControllerContext getContextByClass(Class<?> clazz)
   {
      return controller.getContextByClass(clazz);
   }

   public void addInstantiatedContext(KernelControllerContext context)
   {
      controller.addInstantiatedContext(context);
   }

   public void removeInstantiatedContext(KernelControllerContext context)
   {
      controller.removeInstantiatedContext(context);
   }

   public void registerListener(KernelEventListener listener, KernelEventFilter filter, Object handback) throws Throwable
   {
      controller.registerListener(listener, filter, handback);
   }

   public void unregisterListener(KernelEventListener listener, KernelEventFilter filter, Object handback) throws Throwable
   {
      controller.unregisterListener(listener, filter, handback);
   }

   public void fireKernelEvent(KernelEvent event)
   {
      controller.fireKernelEvent(event);
   }
}