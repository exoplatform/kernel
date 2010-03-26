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
import org.exoplatform.kernel.demos.mc.InjectedBean;
import org.exoplatform.kernel.demos.mc.InjectingBean;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.dependency.spi.Controller;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.spi.config.KernelConfigurator;
import org.jboss.kernel.spi.dependency.KernelController;
import org.jboss.kernel.spi.registry.KernelBus;
import org.jboss.kernel.spi.registry.KernelRegistry;
import org.junit.Assert;
import org.junit.Test;

import javax.transaction.TransactionManager;
import java.util.Map;

/**
 * Presumption when running this test from within servlet container is that
 * org.exoplatform.kernel.demos:exo.kernel.mc-int-demo jar has been deployed
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class MCInjectionTest
{
   protected Log log = ExoLogger.getLogger("exo.kernel.mc-int-tests.MCInjectionTest");

   protected String beanName;
   protected InjectingBean bean;
   protected boolean inJboss;
   protected boolean mcIntActive = true;

   public MCInjectionTest()
   {
      beanName = "InjectingBean";
   }

   protected void init()
   {
      log.info("init() method called");
      RootContainer rootContainer = RootContainer.getInstance();
      bean = (InjectingBean) rootContainer.getComponentInstance(beanName);
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
      testFieldInjection();
      testTypeMatchingMethodInjection();
      testNameLookupMethodInjection();
      testNameLookupMapInjection();
      testPropertyValueMethodInjection();
      testTransactionManager();
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

   protected void testFieldInjection()
   {
      boolean found = bean.getInjectedBean() != null;
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Field injection not executed", found);
      }
      else
      {
         Assert.assertFalse("Field injection should not have worked", found);
      }
      log.info("testFieldInjection passed");
   }

   protected void testTypeMatchingMethodInjection()
   {
      boolean found = bean.getBean() != null;
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Method injection by type matching not executed", found);
      }
      else
      {
         Assert.assertFalse("Method injection by type matching should not have worked", found);
      }
      log.info("testTypeMatchingMethodInjection passed");
   }

   protected void testNameLookupMethodInjection()
   {
      boolean found = bean.getConfigurator() != null;
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

   protected void testNameLookupMapInjection()
   {
      Map bindings = bean.getBindings();
      boolean found = bindings != null;
      if (inJboss && mcIntActive)
      {
         Assert.assertTrue("Name lookup Map injection not executed", found);
         Assert.assertEquals("Bindings size", bindings.size(), 6);
         Assert.assertTrue("Controller not bound", bindings.get(Controller.class) instanceof Controller);
         Assert.assertTrue("Kernel not bound", bindings.get(Kernel.class) instanceof Kernel);
         Assert.assertTrue("KernelController not bound", bindings.get(KernelController.class) instanceof KernelController);
         Assert.assertTrue("KernelBus not bound", bindings.get(KernelBus.class) instanceof KernelBus);
         Assert.assertTrue("KernelRegistry not bound", bindings.get(KernelRegistry.class) instanceof KernelRegistry);
         Assert.assertTrue("KernelConfigurator not bound", bindings.get(KernelConfigurator.class) instanceof KernelConfigurator);
      }
      else
      {
         Assert.assertFalse("Name lookup Map injection should not have worked", found);
      }
      log.info("testNameLookupMapInjection passed");
   }

   protected void testPropertyValueMethodInjection()
   {
      String propertyValue = bean.getSomeStringProperty();
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
