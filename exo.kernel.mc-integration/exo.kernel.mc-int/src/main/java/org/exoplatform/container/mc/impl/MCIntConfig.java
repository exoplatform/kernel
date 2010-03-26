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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.BeanMetaData;
import org.jboss.kernel.spi.deployment.KernelDeployment;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.resolver.MutableSchemaResolver;
import org.jboss.xb.binding.sunday.unmarshalling.SingletonSchemaResolverFactory;
import org.picocontainer.ComponentAdapter;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration reader and holder for conf/mc-int-config.xml
 *
 * mc-int-config.xml uses jboss bean deployer syntax (the one used in jboss-beans.xml).
 * It is only interested in annotation and injection configuration though, and doesn't treat <bean> element attributes
 * as referring to mc bean declarations - it treats them as references to exo-kernel components.
 * <p>
 * Example:
 * <pre>
 * <deployment xmlns="urn:jboss:bean-deployer:2.0">
 * <bean class="org.exoplatform.container.definition.PortalContainerConfig">
 *    <annotation>@org.exoplatform.container.mc.impl.InterceptMC</annotation>
 * </bean>
 * <bean name="InjectingBean2">
 *    <annotation>@org.exoplatform.container.mc.impl.InterceptMC(injectionMode=org.exoplatform.container.mc.impl.MCInjectionMode.STANDARD)</annotation>
 * </bean>
 * </pre>
 * <p>
 * In the above example the first <em>bean</em> declaration refers to exo-kernel configured service that
 * is returned by exo container when calling getComponentInstanceOfType(clazz).
 *
 * The second <em>bean</em> declaration refers to exo-kernel configured service that is returned by exo
 * container when calling getComponentInstance(name).
 *
 * This configuration makes it possible to activate injection outside of you classes. When annotations are declared both
 * inside the class, and in mc-int-config.xml, the latter completely overrides the former.
 * It is also possible to use mc-int-config.xml to nullify an existing annotation in your service class.
 *
 * <em>Warning:</em> Make sure there are no leading or trailing white spaces in the body of <em>annotation</em> element.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class MCIntConfig
{
   /**
    * Logger.
    */
   private static final Log log = ExoLogger.getLogger("exo.kernel.mc-int.MCIntConfig");

   /**
    * Configuration for <em>beans</em> specified with <em>name</em>.
    */
   private Map<String, DeploymentData> confByKey = new HashMap<String, DeploymentData>();

   /**
    * Configuration for <em>beans</em> specified with <em>class</em>.
    */
   private Map<String, DeploymentData> confByBean = new HashMap<String, DeploymentData>();

   /**
    * The only constructor.
    */
   MCIntConfig()
   {
      Enumeration<URL> urls = null;
      try
      {
         urls = Thread.currentThread().getContextClassLoader().getResources("conf/mc-int-config.xml");
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to get resources: conf/mc-int-config.xml", e);
      }

      UnmarshallerFactory factory = UnmarshallerFactory.newInstance();
      MutableSchemaResolver resolver = SingletonSchemaResolverFactory.getInstance().getSchemaBindingResolver();

      while (urls.hasMoreElements())
      {
         URL url = urls.nextElement();

         KernelDeployment deployment = parseConfigURL(factory, resolver, url);

         List<BeanMetaData> beans = deployment.getBeans();
         for (BeanMetaData mdata : beans)
         {
            String name = mdata.getName();
            DeploymentData data = confByKey.get(name);
            if (data == null)
            {
               String bean = mdata.getBean();
               data = confByBean.get(bean);
            }
            if (data != null)
            {
               log.warn("Overriding existing mc-int-beans configuration for bean: " + data.data
                     + " from " + data.deployment.getName() + " with one from " + deployment.getName());
            }
            addConf(new DeploymentData(deployment, mdata));
         }
      }
   }

   /**
    * Retrieve configuration by component key.
    *
    * @param key component key
    * @return configuration for specific component
    */
   public AbstractBeanMetaData getByKey(String key)
   {
      DeploymentData dd = confByKey.get(key);
      if (dd == null)
      {
         return null;
      }
      return (AbstractBeanMetaData) dd.data;
   }

   /**
    * Retrieve configuration by component type.
    *
    * @param bean component type
    * @return configuration for specific component
    */
   public AbstractBeanMetaData getByBean(String bean)
   {
      DeploymentData dd = confByBean.get(bean);
      if (dd == null)
      {
         return null;
      }
      return (AbstractBeanMetaData) dd.data;
   }

   /**
    * Retrieve configuration for specific component adapter.
    * First lookup by key, then by type.
    *
    * @param adapter component adapter
    * @return configuration for specific component
    */
   public AbstractBeanMetaData getByAdapter(ComponentAdapter adapter)
   {
      Object key = adapter.getComponentKey();
      String strKey = key instanceof Class ? ((Class) key).getName() : String.valueOf(key);
      BeanMetaData ret = getByKey(strKey);
      if (ret != null)
      {
         return (AbstractBeanMetaData) ret;
      }

      ret = getByBean(strKey);
      return (AbstractBeanMetaData) ret;
   }

   /**
    * Add parsed configuration for one component.
    *
    * @param deploymentData confiuration for specific component
    */
   private void addConf(DeploymentData deploymentData)
   {
      String name = deploymentData.data.getName();
      if (name != null)
      {
         confByKey.put(name, deploymentData);
      }
      String bean = deploymentData.data.getBean();
      if (bean != null)
      {
         confByBean.put(bean, deploymentData);
      }
      if (name == null && bean == null)
      {
         throw new RuntimeException("Configuration error: bean found with no name and no class in "
               + deploymentData.deployment.getName());
      }
   }

   /**
    * Parse the configuration.
    *
    * @param factory JBoss XB unmarshaller factory
    * @param resolver JBoss XB schema resolver
    * @param url Url pointing to mc-int-config.xml file
    * @return JBoss XB bean representing a parsed configuration
    */
   private KernelDeployment parseConfigURL(UnmarshallerFactory factory, MutableSchemaResolver resolver, URL url)
   {
      final boolean trace = log.isTraceEnabled();
      if (trace)
      {
         log.trace("Parsing " + url);
      }
      long start = System.currentTimeMillis();

      Unmarshaller unmarshaller = factory.newUnmarshaller();
      KernelDeployment deployment = null;
      try
      {
         deployment = (KernelDeployment) unmarshaller.unmarshal(url.toString(), resolver);
      }
      catch (Exception e)
      {
         throw new RuntimeException("Failed to parse xml " + url, e);
      }

      if (deployment == null)
      {
         throw new RuntimeException("The xml " + url + " is not well formed!");
      }
      deployment.setName(url.toString());

      if (trace)
      {
         long now = System.currentTimeMillis();
         log.trace("Parsing " + url + " took " + (now - start) + " milliseconds");
      }

      return deployment;
   }

   /**
    * Data structure that holds component configuration - deployment configuration association.
    */
   static class DeploymentData
   {
      KernelDeployment deployment;
      BeanMetaData data;

      public DeploymentData(KernelDeployment deployment, BeanMetaData data)
      {
         this.deployment = deployment;
         this.data = data;
      }
   }
}
