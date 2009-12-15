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

import org.jboss.dependency.spi.Controller;
import org.picocontainer.Startable;

import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * This POJO demonstrates how to use external configuration (mc-int-config.xml)
 * to perform injections of JBoss Microcontainer components
 * into a service object configured and managed through exo-kernel.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ExternallyControlledInjectingBean implements Startable
{
   /**
    * An example of getting the JBoss MC configured transaction manager
    * into an exo-kernel installed service.
    * This field is to be injected through setter method by mc kernel.
    */
   private TransactionManager tm;

   /**
    * Field to be injected through setter method by mc kernel.
    */
   private InjectedBean bean;

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
    * Internal datastructure to demonstrate nested property injection
    */
   private ConfigurationHolder config;

   /**
    * In this demo this bean is to be instantiated by exo-kernel.
    * We can not use constructor based injection that mc kernel supports,
    * since it's not mc kernel that does the instantiation.
    */
   public ExternallyControlledInjectingBean()
   {
      config = new ConfigurationHolder();
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
    * Setter method, externally configured as injection point for type matching injection
    * @param tm
    */
   public void setTransactionManager(TransactionManager tm)
   {
      this.tm = tm;
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
    * Setter method, externally configured as injection point for type matching injection.
    * @param bean
    */
   public void setBean(InjectedBean bean)
   {
      this.bean = bean;
   }

   /**
    * Getter method.
    * @return
    */
   public Map getBindingsMap()
   {
      return bindingsMap;
   }

   /**
    * Setter method, externally configured as injection point.
    * @param bindingsMap
    */
   public void setBindingsMap(Map bindingsMap)
   {
      this.bindingsMap = bindingsMap;
   }

   /**
    * Getter method.
    * @return
    */
   public String getStringValue()
   {
      return stringValue;
   }

   /**
    * Setter method externally configured as injection point for property getter injection.
    * @param stringValue
    */
   public void setStringValue(String stringValue)
   {
      this.stringValue = stringValue;
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
    * org.picocontainer.Startable lifecycle method. Supposed to be called exactly once.
    */
   public void start()
   {
      startCount++;
   }

   /**
    * org.picocontainer.Startable lifecycle method.
    */
   public void stop()
   {
   }

   /**
    * Getter method.
    * @return
    */
   public ConfigurationHolder getConfig()
   {
      return config;
   }

   /**
    * Non-setter method externally configured as injection method called with injected parameters
    * @param param1
    * @param param2
    * @param param3
    */
   public void initialize(String param1, String param2, Controller param3)
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

   /**
    * Inner data structure for testing nested property injection
    */
   public static class ConfigurationHolder
   {
      /**
       * Property to be injected
       */
      private String someProperty;

      /**
       * Getter method.
       * @return someProperty
       */
      public String getSomeProperty()
      {
         return someProperty;
      }

      /**
       * Setter method.
       * @param someProperty
       */
      public void setSomeProperty(String someProperty)
      {
         this.someProperty = someProperty;
      }
   }
}
