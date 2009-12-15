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
package org.exoplatform.kernel.it;

import org.exoplatform.commons.Environment;
import org.exoplatform.container.RootContainer;
import org.exoplatform.kernel.demos.mc.ExternallyControlledInjectingBean;
import org.exoplatform.kernel.demos.mc.InjectedBean;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.dependency.spi.Controller;
import org.junit.Assert;
import org.junit.Test;

import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class ExternalMCInjectionTest
{
   protected Log log = ExoLogger.getLogger(getClass());

   protected String beanName;
   protected ExternallyControlledInjectingBean bean;
   protected boolean inJboss;
   protected boolean mcIntActive = true;

   public ExternalMCInjectionTest()
   {
      beanName = "ExternallyControlledInjectingBean";
   }

   protected void init()
   {
      log.info("init() method called");
      RootContainer rootContainer = RootContainer.getInstance();
      bean = (ExternallyControlledInjectingBean) rootContainer.getComponentInstance(beanName);
      log.info("Retrieved " + beanName + ": " + bean);
      Assert.assertNotNull(beanName + " not installed", bean);
      inJboss = Environment.getInstance().getPlatform() == Environment.JBOSS_PLATFORM;
      log.info("Running inside JBoss? " + inJboss);
   }

   @Test
   public void test()
   {
      init();
      tests();
   }

   protected void tests()
   {
      testTransactionManager();
      testNameLookupMethodInjection();
      testMapInjection();
      testPropertyValueMethodInjection();
      testNestedPropertyInjection();
      testInstallMethod();
      testStarted();
   }

   protected void testTransactionManager()
   {
      TransactionManager tm = bean.getTransactionManager();

      if (inJboss && mcIntActive)
      {
         try
         {
            int status = tm.getStatus();
            log.info("Status before tx: " + tm.getStatus());
            tm.begin();
            Assert.assertFalse("TX status didn't change: ", status == tm.getStatus());
            log.info("Status in tx: " + tm.getStatus());
            tm.commit();
            Assert.assertTrue("TX status didn't return to original: ", status == tm.getStatus());
            log.info("Status after tx: " + tm.getStatus());
         }
         catch (Exception ex)
         {
            throw new RuntimeException("Failed to use TransactionManager: ", ex);
         }
      }
      else
      {
         Assert.assertNull("Injection should not have worked", tm);
      }
      log.info("testTransactionManager passed");
   }

   protected void testNameLookupMethodInjection()
   {
      boolean found = bean.getBean() != null;
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Method injection by name lookup not executed", found);
      }
      else
      {
         Assert.assertFalse("Method injection by name lookup should not have worked", found);
      }
      log.info("testNameLookupMethodInjection passed");
   }

   protected void testMapInjection()
   {
      Map bindings = bean.getBindingsMap();
      boolean found = bindings != null;
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Map injection not executed", found);
         Assert.assertEquals("Bindings size", bindings.size(), 1);
         Assert.assertTrue("Controller not bound", bindings.get(Controller.class) instanceof Controller);
      }
      else
      {
         Assert.assertFalse("Map injection should not have worked", found);
      }
      log.info("testMapInjection passed");
   }

   protected void testPropertyValueMethodInjection()
   {
      String propertyValue = bean.getStringValue();
      boolean found = propertyValue != null;
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Property value method injection not executed", found);
         Assert.assertEquals("Invalid injected value", propertyValue, InjectedBean.SOME_PROPERTY_VALUE);
      }
      else
      {
         Assert.assertFalse("Property value method injection should not have worked", found);
      }
      log.info("testPropertyValueMethodInjection passed");
   }

   protected void testNestedPropertyInjection()
   {
      String propertyValue = bean.getConfig().getSomeProperty();
      boolean found = propertyValue != null;
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Nested property value method injection not executed", found);
         Assert.assertEquals("Invalid injected value", propertyValue, "Test value");
      }
      else
      {
         Assert.assertFalse("Nested property value method injection should not have worked", found);
      }
      log.info("testNestedPropertyInjection passed");
   }

   protected void testInstallMethod()
   {
      boolean installOk = bean.isInstallOk();
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Install method not executed as expected", installOk);
      }
      else
      {
         Assert.assertFalse("Install method should not have worked", installOk);
      }
      log.info("testInstallMethod passed");
   }

   protected void testStarted()
   {
      Assert.assertEquals("start() method not called exactly once", 1, bean.getStartCount());
      log.info("testStarted passed");
   }
}
