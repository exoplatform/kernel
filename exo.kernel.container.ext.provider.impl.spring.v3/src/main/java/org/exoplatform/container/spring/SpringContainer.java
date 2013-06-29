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
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
   private static final long serialVersionUID = -6662420267945445921L;

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
   public Object getComponentInstance(Object componentKey)
   {
      Object result = super.getComponentInstance(componentKey);
      if (ctx != null && result == null)
      {
         String name = keyToBeanName(componentKey);
         if (ctx.containsBean(name))
         {
            result = ctx.getBean(name);
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getComponentInstanceOfType(Class<T> componentType)
   {
      T result = super.getComponentInstanceOfType(componentType);
      if (ctx != null && result == null)
      {
         String[] names = ctx.getBeanNamesForType(componentType);
         if (names != null && names.length > 0)
         {
            result = componentType.cast(ctx.getBean(names[0]));
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ComponentAdapter getComponentAdapter(Object componentKey)
   {
      ComponentAdapter result = super.getComponentAdapter(componentKey);
      if (ctx != null && result == null)
      {
         String name = keyToBeanName(componentKey);
         if (ctx.containsBean(name))
         {
            result =
               createComponentAdapter(componentKey instanceof Class<?> ? (Class<?>)componentKey : Object.class, name);
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ComponentAdapter getComponentAdapterOfType(Class<?> componentType)
   {
      ComponentAdapter result = super.getComponentAdapterOfType(componentType);
      if (ctx != null && result == null)
      {
         String[] names = ctx.getBeanNamesForType(componentType);
         if (names != null && names.length > 0)
         {
            result = createComponentAdapter(componentType, names[0]);
         }
      }
      return result;
   }

   private ComponentAdapter createComponentAdapter(final Class<?> type, final String name)
   {
      return new AbstractComponentAdapter(type, type)
      {
         /**
          * The serial UID
          */
         private static final long serialVersionUID = -4625398501079851570L;

         public Object getComponentInstance() throws ContainerException
         {
            return ctx.getBean(name);
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<ComponentAdapter> getComponentAdaptersOfType(Class<?> componentType)
   {
      List<ComponentAdapter> result = super.getComponentAdaptersOfType(componentType);
      if (ctx != null)
      {
         result = new ArrayList<ComponentAdapter>(result);
         String[] names = ctx.getBeanNamesForType(componentType);
         if (names != null)
         {
            for (int i = 0, length = names.length; i < length; i++)
            {
               String name = names[i];
               result.add(createComponentAdapter(ctx.getType(name), name));
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
      ConfigurationManager cm = super.getComponentInstanceOfType(ConfigurationManager.class);
      Component component = null;
      try
      {
         // We check if the component has been defined in the configuration of the current container
         // The goal is to enable the SpringContainer only if it is needed
         component = cm.getComponent(ApplicationContextProvider.class);
      }
      catch (Exception e)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Could not check if an ApplicationContextProvider has been defined: " + e.getMessage());
         }
      }
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
         Collection<ComponentAdapter> adapters = delegate.getComponentAdapters();
         for (ComponentAdapter adapter : adapters)
         {
            Object key = adapter.getComponentKey();
            Class<?> type;
            String name = keyToBeanName(key);
            if (key instanceof Class<?>)
            {
               type = (Class<?>)key;
            }
            else
            {
               type = adapter.getComponentImplementation();
            }
            bf.registerSingleton(name, new ComponentAdapterFactoryBean(type, adapter));
         }
         GenericApplicationContext parentContext = new GenericApplicationContext(bf);
         parentContext.refresh();
         ApplicationContextProvider provider = super.getComponentInstanceOfType(ApplicationContextProvider.class);
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

   private static class ComponentAdapterFactoryBean<T> implements FactoryBean<T>
   {
      private final Class<T> type;

      private final ComponentAdapter adapter;

      private ComponentAdapterFactoryBean(Class<T> type, ComponentAdapter adapter)
      {
         this.type = type;
         this.adapter = adapter;
      }

      /**
       * {@inheritDoc}
       */
      public T getObject() throws Exception
      {
         return type.cast(adapter.getComponentInstance());
      }

      /**
       * {@inheritDoc}
       */
      public Class<?> getObjectType()
      {
         return type;
      }

      /**
       * {@inheritDoc}
       */
      public boolean isSingleton()
      {
         return true;
      }
   }
}
