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
package org.exoplatform.container.mc.impl;

import org.jboss.beans.info.spi.BeanAccessMode;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.builder.BeanMetaDataBuilder;
import org.jboss.dependency.plugins.helpers.StatelessController;
import org.jboss.dependency.spi.ControllerState;
import org.jboss.kernel.plugins.dependency.AbstractKernelControllerContext;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.dependency.KernelControllerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;

/**
 * A wrapping adapter, that takes care of mc integration at component instantiation time.
 *
 * @author <a href="mailto:ajustin@redhat.com">Ales Justin</a>
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class MCComponentAdapter
   implements ComponentAdapter
{
   /**
    * Kernel controller retrieved from associated mc kernel.
    */
   private KernelController controller;

   /**
    * Original component adapter.
    */
   private ComponentAdapter delegate;

   /**
    * Interception marking annotation, configured for the component.
    */
   private InterceptMC interceptMC;

   /**
    * A component instance handled by this component adapter.
    */
   private Object lastComponentInstance;

   /**
    * BeanMetaData parsed from mc-int-config.xml
    */
   private AbstractBeanMetaData metaData;

   /**
    * The only constructor.
    *
    * @param controller Kernel controller to use
    * @param delegate original component adapter
    * @param interceptMC mc-integration configuration in the form of annotation
    */
   public MCComponentAdapter(KernelController controller, ComponentAdapter delegate, InterceptMC interceptMC,
            AbstractBeanMetaData data)
   {
      if (controller == null)
      {
         throw new IllegalArgumentException("Null controller");
      }
      if (delegate == null)
      {
         throw new IllegalArgumentException("Null delegate");
      }

      if (controller instanceof StatelessController)
      {
         throw new IllegalArgumentException("controller is instanceof StatelessController");
      }

      this.controller = controller;
      this.delegate = delegate;
      this.interceptMC = interceptMC;
      this.metaData = data;
   }

   /**
    * Getter method for component key.
    * 
    * @return String or Class representing a key
    */
   public Object getComponentKey()
   {
      return delegate.getComponentKey();
   }

   /**
    * Getter for component implementation class.
    *
    * @return component implementation class
    */
   public Class getComponentImplementation()
   {
      return delegate.getComponentImplementation();
   }

   /**
    * This is where mc integration happens.
    * Instantiation is first delegated to original component adapter, the resulting
    * instance is then sent through kernel controller to be injected by mc kernel.
    *
    * @param container pico container that holds this component adapter
    * @return object with injections already performed on it
    * @throws PicoInitializationException
    * @throws PicoIntrospectionException
    */
   public Object getComponentInstance(PicoContainer container) throws PicoInitializationException,
            PicoIntrospectionException
   {
      Object target = lastComponentInstance;
      if (target != null)
      {
         return target;
      }

      String key = delegate.getComponentKey().toString();
      Object instance = delegate.getComponentInstance(container);
      try
      {
         if (metaData == null)
         {
            metaData = new AbstractBeanMetaData();
         }
         metaData.setName(key);
         metaData.setBean(instance.getClass().getName());
         BeanMetaDataBuilder builder = BeanMetaDataBuilder.createBuilder(metaData);
         builder.setConstructorValue(instance);
         builder.ignoreCreate();
         builder.ignoreStart();
         builder.ignoreStop();
         builder.ignoreDestroy();
         builder.setAccessMode(getInjectionMode(interceptMC));
         KernelControllerContext ctx = new AbstractKernelControllerContext(null, builder.getBeanMetaData(), null);

         StatelessController ctrl = new StatelessController(controller);
         ctrl.install(ctx);
         if (ctx.getError() != null)
         {
            throw ctx.getError();
         }
         if (ctrl.getStates().isBeforeState(ctx.getState(), ControllerState.INSTALLED))
         {
            throw new IllegalArgumentException("Missing some dependency: "
                     + ctx.getDependencyInfo().getUnresolvedDependencies(null));
         }

         target = ctx.getTarget();
         lastComponentInstance = target;
         return target;
      }
      catch (Throwable ex) //NOSONAR
      {
         throw new RuntimeException("Failed to perform MC interception on component: "
                  + delegate.getComponentImplementation(), ex);
      }
   }

   /**
    * Method to determine injection mode to be used by mc kernel.
    *
    * @param interceptMC configuration in form of InterceptMC annotation
    * @return jboss-beans-info BeanAccessMode
    */
   private BeanAccessMode getInjectionMode(InterceptMC interceptMC)
   {
      MCInjectionMode mode = interceptMC.injectionMode();

      switch (mode)
      {
         case ALL :
            return BeanAccessMode.ALL;
         case FIELDS :
            return BeanAccessMode.FIELDS;
         default :
            return BeanAccessMode.STANDARD;
      }
   }

   /**
    * Delegation-only method.
    *
    * @param container
    * @throws PicoIntrospectionException
    */
   public void verify(PicoContainer container) throws PicoIntrospectionException
   {
      delegate.verify(container);
   }

   /**
    * Delegation-only method.
    *
    * @param visitor
    */
   public void accept(PicoVisitor visitor)
   {
      delegate.accept(visitor);
   }
}