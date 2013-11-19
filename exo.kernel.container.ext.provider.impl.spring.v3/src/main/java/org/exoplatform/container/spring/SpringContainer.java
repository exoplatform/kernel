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
package org.exoplatform.container.spring;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.AbstractComponentAdapter;
import org.exoplatform.container.AbstractInterceptor;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.Interceptor;
import org.exoplatform.container.xml.Component;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.QualifierAnnotationAutowireCandidateResolver;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateQualifier;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.annotation.Annotation;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Named;

/**
 * The implementation of an {@link Interceptor} allowing eXo Kernel to interact with a spring container
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class SpringContainer extends AbstractInterceptor
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -4841328894117928913L;

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger
      .getLogger("exo.kernel.container.ext.provider.impl.spring.v3.SpringContainer");

   /**
    * The Spring {@link ApplicationContext}
    */
   private ApplicationContext ctx;

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getComponentInstance(Object componentKey, Class<T> bindType, boolean autoRegistration)
   {
      T result = super.getComponentInstance(componentKey, bindType, autoRegistration);
      if (ctx != null && result == null)
      {
         if (componentKey instanceof Class<?> && !((Class<?>)componentKey).isAnnotation())
         {
            return bindType.cast(getInstanceOfType((Class<?>)componentKey));
         }
         else if (!(componentKey instanceof String) && !(componentKey instanceof Class<?>))
         {
            return null;
         }
         String beanName = keyToBeanName(componentKey);
         if (ctx.containsBean(beanName) && bindType.isAssignableFrom(ctx.getType(beanName)))
         {
            return bindType.cast(ctx.getBean(beanName));
         }
         String[] names = ctx.getBeanNamesForType(bindType);
         if (names != null && names.length > 0)
         {
            for (int i = 0, length = names.length; i < length; i++)
            {
               String name = names[i];
               if (componentKey instanceof String)
               {
                  Named n = ctx.findAnnotationOnBean(name, Named.class);
                  if (n != null && componentKey.equals(n.value()))
                  {
                     return bindType.cast(ctx.getBean(name));
                  }
               }
               else
               {
                  @SuppressWarnings("unchecked")
                  Annotation a = ctx.findAnnotationOnBean(name, (Class<? extends Annotation>)componentKey);
                  if (a != null)
                  {
                     return bindType.cast(ctx.getBean(name));
                  }
               }
            }
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getComponentInstanceOfType(final Class<T> componentType, boolean autoRegistration)
   {
      T result = super.getComponentInstanceOfType(componentType, autoRegistration);
      if (ctx != null && result == null)
      {
         result = getInstanceOfType(componentType);
      }
      return result;
   }

   private <T> T getInstanceOfType(final Class<T> componentType)
   {
      T result;
      PrivilegedAction<T> action = new PrivilegedAction<T>()
      {
         public T run()
         {
            String name = classToBeanName(componentType);
            if (ctx.containsBean(name) && componentType.isAssignableFrom(ctx.getType(name)))
            {
               return componentType.cast(ctx.getBean(name));
            }
            String[] names = ctx.getBeanNamesForType(componentType);
            if (names != null && names.length > 0)
            {
               return componentType.cast(ctx.getBean(names[0]));
            }
            return null;
         }
      };
      result = SecurityHelper.doPrivilegedAction(action);
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> ComponentAdapter<T> getComponentAdapter(Object componentKey, Class<T> bindType, boolean autoRegistration)
   {
      ComponentAdapter<?> result = super.getComponentAdapter(componentKey, bindType, autoRegistration);
      if (ctx != null && result == null)
      {
         if (componentKey instanceof Class<?> && !((Class<?>)componentKey).isAnnotation())
         {
            return getAdapterOfType(bindType);
         }
         else if (!(componentKey instanceof String) && !(componentKey instanceof Class<?>))
         {
            return null;
         }
         String beanName = keyToBeanName(componentKey);
         if (ctx.containsBean(beanName) && bindType.isAssignableFrom(ctx.getType(beanName)))
         {
            return createComponentAdapter(bindType, beanName);
         }
         String[] names = ctx.getBeanNamesForType(bindType);
         if (names != null && names.length > 0)
         {
            for (int i = 0, length = names.length; i < length; i++)
            {
               String name = names[i];
               if (componentKey instanceof String)
               {
                  Named n = ctx.findAnnotationOnBean(name, Named.class);
                  if (n != null && componentKey.equals(n.value()))
                  {
                     return createComponentAdapter(bindType, name);
                  }
               }
               else
               {
                  Annotation a = ctx.findAnnotationOnBean(name, (Class<? extends Annotation>)componentKey);
                  if (a != null)
                  {
                     return createComponentAdapter(bindType, name);
                  }
               }
            }
         }
      }
      return (ComponentAdapter<T>)result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType, boolean autoRegistration)
   {
      ComponentAdapter<T> result = super.getComponentAdapterOfType(componentType, autoRegistration);
      if (ctx != null && result == null)
      {
         result = getAdapterOfType(componentType);
      }
      return result;
   }

   private <T> ComponentAdapter<T> getAdapterOfType(Class<T> componentType)
   {
      String name = classToBeanName(componentType);
      if (ctx.containsBean(name) && componentType.isAssignableFrom(ctx.getType(name)))
      {
         return createComponentAdapter(componentType, name);
      }
      String[] names = ctx.getBeanNamesForType(componentType);
      if (names != null && names.length > 0)
      {
         return createComponentAdapter(componentType, names[0]);
      }
      return null;
   }

   private <T> ComponentAdapter<T> createComponentAdapter(final Class<T> type, final String name)
   {
      return new AbstractComponentAdapter<T>(type, type)
      {
         /**
          * The serial UID
          */
         private static final long serialVersionUID = -4625398501079851570L;

         public T getComponentInstance() throws ContainerException
         {
            return type.cast(ctx.getBean(name));
         }

         public boolean isSingleton()
         {
            return ctx.isSingleton(name);
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      List<ComponentAdapter<T>> result = super.getComponentAdaptersOfType(componentType);
      if (ctx != null)
      {
         result = new ArrayList<ComponentAdapter<T>>(result);
         String[] names = ctx.getBeanNamesForType(componentType);
         if (names != null)
         {
            for (int i = 0, length = names.length; i < length; i++)
            {
               String name = names[i];
               result.add((ComponentAdapter<T>)createComponentAdapter(ctx.getType(name), name));
            }
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> List<T> getComponentInstancesOfType(Class<T> componentType) throws ContainerException
   {
      List<T> result = super.getComponentInstancesOfType(componentType);
      if (ctx != null)
      {
         result = new ArrayList<T>(result);
         result.addAll(ctx.getBeansOfType(componentType).values());
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void start()
   {
      ConfigurationManager cm = super.getComponentInstanceOfType(ConfigurationManager.class, false);
      // We check if the component has been defined in the configuration of the current container
      // The goal is to enable the SpringContainer only if it is needed
      Component component = cm.getComponent(ApplicationContextProvider.class);
      if (component == null)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("No ApplicationContextProvider has been defined, thus the SpringContainer will be disabled."
               + " To enable the Spring Integration please define an ApplicationContextProvider");
         }
      }
      else
      {
         DefaultListableBeanFactory bf = new DefaultListableBeanFactory();
         bf.setAutowireCandidateResolver(new QualifierAnnotationAutowireCandidateResolver());
         Collection<ComponentAdapter<?>> adapters = delegate.getComponentAdapters();
         for (ComponentAdapter<?> adapter : adapters)
         {
            Object key = adapter.getComponentKey();
            String name = keyToBeanName(key);
            String factoryName = name + "#factory";
            RootBeanDefinition def =
               new RootBeanDefinition(adapter.getComponentImplementation(), AbstractBeanDefinition.AUTOWIRE_NO, false);
            def.setScope(BeanDefinition.SCOPE_PROTOTYPE);
            def.setFactoryBeanName(factoryName);
            def.setFactoryMethodName("getInstance");
            def.setLazyInit(true);
            def.setTargetType(adapter.getComponentImplementation());
            if (key instanceof String)
            {
               def.addQualifier(new AutowireCandidateQualifier(Named.class, key));
            }
            else if (key instanceof Class<?> && ((Class<?>)key).isAnnotation())
            {
               def.addQualifier(new AutowireCandidateQualifier((Class<?>)key));
            }
            else
            {
               def.setPrimary(true);
            }
            bf.registerBeanDefinition(name, def);
            bf.registerSingleton(factoryName, new ComponentAdapterFactoryBean(adapter));
         }
         GenericApplicationContext parentContext = new GenericApplicationContext(bf);
         parentContext.refresh();
         ApplicationContextProvider provider =
            super.getComponentInstanceOfType(ApplicationContextProvider.class, false);
         ctx = provider.getApplicationContext(parentContext);
         LOG.info("A SpringContainer has been enabled using the ApplicationContextProvider " + provider.getClass());
      }
      super.start();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop()
   {
      super.stop();
      if (ctx instanceof DisposableBean)
      {
         try
         {
            ((DisposableBean)ctx).destroy();
         }
         catch (Exception e)
         {
            LOG.warn("Could not destroy the container: " + e.getMessage());
         }
         finally
         {
            ctx = null;
         }
      }
   }

   private static String classToBeanName(Class<?> type)
   {
      return type.getName();
   }

   private static String keyToBeanName(Object key)
   {
      if (key instanceof Class<?>)
      {
         return classToBeanName((Class<?>)key);
      }
      else
      {
         if (key instanceof String)
         {
            return (String)key;
         }
         else
         {
            return classToBeanName(key.getClass());
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return "SpringIntegration";
   }

   static class ComponentAdapterFactoryBean<T>
   {
      private final ComponentAdapter<T> adapter;

      private ComponentAdapterFactoryBean(ComponentAdapter<T> adapter)
      {
         this.adapter = adapter;
      }

      public T getInstance() throws Exception
      {
         return adapter.getComponentInstance();
      }
   }
}
