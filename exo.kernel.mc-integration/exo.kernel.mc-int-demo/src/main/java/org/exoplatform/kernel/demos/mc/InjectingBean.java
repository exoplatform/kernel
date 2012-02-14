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
package org.exoplatform.kernel.demos.mc;

import org.exoplatform.container.mc.impl.InterceptMC;
import org.exoplatform.container.mc.impl.MCInjectionMode;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.beans.metadata.api.annotations.EntryValue;
import org.jboss.beans.metadata.api.annotations.Inject;
import org.jboss.beans.metadata.api.annotations.InstallMethod;
import org.jboss.beans.metadata.api.annotations.MapValue;
import org.jboss.beans.metadata.api.annotations.StringValue;
import org.jboss.beans.metadata.api.annotations.Value;
import org.jboss.dependency.spi.Controller;
import org.jboss.kernel.plugins.bootstrap.basic.KernelConstants;
import org.jboss.kernel.spi.config.KernelConfigurator;

import java.util.Map;

import javax.transaction.TransactionManager;

/**
 * This POJO demonstrates how to use annotations to perform injections of JBoss Microcontainer components
 * into a service object configured and managed through exo-kernel.
 * 
 * If we want our class to be instantiated at deploy time,
 * it has to implement org.picocontainer.Startable, otherwise it will only
 * be registered, but not instantiated. MC integration takes place
 * at instantiation time, not at component registration time.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
// Enable field injection by setting injectionMode
@InterceptMC(injectionMode = MCInjectionMode.ALL)
public class InjectingBean implements org.picocontainer.Startable
{
   /**
    * Logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.mc-int-demo.InjectingBean");

   /**
    * Field to be injected through setter method by mc kernel.
    */
   private InjectedBean bean;

   /**
    * Field to be injected through setter method by mc kernel.
    */
   private KernelConfigurator configurator;

   /**
    * Field to be injected directly through field injection by mc kernel.
    * This is an anti-pattern as you're exposing implementation details,
    * and giving up interception capability.
    */
   @Inject(bean = "InjectedBean")
   private InjectedBean injectedBean;

   /**
    * An example of getting the JBoss MC configured transaction manager
    * into an exo-kernel installed service.
    * This field is to be injected through setter method by mc kernel.
    */
   private TransactionManager tm;

   /**
    * A map field to be injected through setter method by mc kernel.
    */
   private Map bindingsMap;

   /**
    * A field to be injected with a property value of existing mc bean.
    * Demonstrates another kind of injection.
    */
   private String stringValue;

   /**
    * Field that counts how many times start() method is called.
    * This is to demonstrate that start() method is called exactly once.
    */
   private int startCount;

   /**
    * Field that tells if @Install method was successfully called
    */
   private boolean installOk;

   /**
    * In this demo this bean is to be instantiated by exo-kernel.
    * We can not use constructor based injection that mc kernel supports,
    * since it's not mc kernel that does the instantiation.
    */
   public InjectingBean()
   {
      LOG.info("Injecting bean instantiated");
   }

   /**
    * Getter method.
    * @return injected bean
    */
   public InjectedBean getBean()
   {
      return bean;
   }

   /**
    * Setter method, annotated as injection point for type matching injection.
    * @param bean
    */
   @Inject
   public void setBean(InjectedBean bean)
   {
      this.bean = bean;
      LOG.info("Received InjectedBean: " + bean);
   }

   /**
    * Getter method.
    * @return injected component
    */
   public KernelConfigurator getConfigurator()
   {
      return configurator;
   }

   /**
    * Setter method, annotated as injection point for name matching injection.
    * @param configurator
    */
   @Inject(bean = KernelConstants.KERNEL_CONFIGURATOR_NAME)
   public void setConfigurator(KernelConfigurator configurator)
   {
      this.configurator = configurator;
      LOG.info("InjectingBean Received KernelConfigurator: " + configurator);
   }

   /**
    * Getter method.
    * @return
    */
   public Map getBindings()
   {
      return bindingsMap;
   }

   /**
    * Setter method, annotated as injection point with map injecting annotation.
    * @param bindings
    */
   @MapValue(
      value = {
         @EntryValue(
            key = @Value(type = Class.class, 
                         string = @StringValue(value = "org.jboss.dependency.spi.Controller", type = Class.class)),
            value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_CONTROLLER_NAME))
         ),
         @EntryValue(
            key = @Value(type = Class.class, 
                         string = @StringValue(value = "org.jboss.kernel.Kernel", type = Class.class)),
            value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_NAME))
         ),
         @EntryValue(
            key = @Value(type = Class.class, 
                         string = @StringValue(value = "org.jboss.kernel.spi.dependency.KernelController", type = Class.class)),
            value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_CONTROLLER_NAME))
         ),
         @EntryValue(
            key = @Value(type = Class.class, 
                         string = @StringValue(value = "org.jboss.kernel.spi.registry.KernelBus", type = Class.class)),
            value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_BUS_NAME))
         ),
         @EntryValue(
            key = @Value(type = Class.class, 
                         string = @StringValue(value = "org.jboss.kernel.spi.registry.KernelRegistry", type = Class.class)),
            value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_REGISTRY_NAME))
         ),
         @EntryValue(
            key = @Value(type = Class.class, 
                         string = @StringValue(value = "org.jboss.kernel.spi.config.KernelConfigurator", type = Class.class)),
            value = @Value(inject = @Inject(bean = KernelConstants.KERNEL_CONFIGURATOR_NAME))
         )
      }
   )
   public void setBindings(Map<Class<?>, Object> bindings)
   {
      LOG.info("Received a map with bindings: " + bindings);
      this.bindingsMap = bindings;
   }

   /**
    * Getter method.
    * @return
    */
   public String getSomeStringProperty()
   {
      return stringValue;
   }

   /**
    * Setter method annotated as injection point for property getter injection.
    * @param value
    */
   @Inject(bean = "InjectedBean", property = "someString")
   public void setSomeStringProperty(String value)
   {
      LOG.info("Received SomeStringProperty value: " + value);
      this.stringValue = value;
   }

   /**
    * org.picocontainer.Startable lifecycle method. Supposed to be called exactly once.
    */
   public void start()
   {
      LOG.warn("start() called (injectedBean is set to: " + injectedBean + ", transactionManager is setTo: " + tm + ")");
      this.startCount++;
   }

   /**
    * org.picocontainer.Startable lifecycle method.
    */
   public void stop()
   {
      LOG.info("stop() called");
   }

   /**
    * Getter method for field-injected bean.
    * @return field-injected bean
    */
   public InjectedBean getInjectedBean()
   {
      return injectedBean;
   }

   /**
    * Getter method.
    * @return start count
    */
   public int getStartCount()
   {
      return startCount;
   }

   /**
    * Setter method annotated as injection point for type matching injection
    * @param tm
    */
   @Inject
   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
   }

   /**
    * Getter method.
    * @return
    */
   public TransactionManager getTransactionManager()
   {
      return tm;
   }

   /**
    * Non-setter method annotated as injection method called with injected parameters
    * @param param1
    * @param param2
    * @param param3
    */
   @InstallMethod
   public void initialize(@Inject @StringValue("parameter1") String param1, 
                          @Inject(bean = "InjectedBean", property = "someString") String param2,
                          @Inject(bean = KernelConstants.KERNEL_CONTROLLER_NAME) Controller param3)
   {
      if (param1 == null)
      {
         throw new IllegalArgumentException("param1 == null");
      }
      if (param2 == null)
      {
         throw new IllegalArgumentException("param2 == null");
      }
      if (param3 == null)
      {
         throw new IllegalArgumentException("param3 == null");
      }
      installOk = true;
   }

   /**
    * Getter method.
    * @return true if install method was called successfully
    */
   public boolean isInstallOk()
   {
      return installOk;
   }
}
