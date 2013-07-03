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
package org.exoplatform.container.weld;

import org.exoplatform.container.AbstractComponentAdapter;
import org.exoplatform.container.AbstractInterceptor;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.Interceptor;
import org.exoplatform.container.xml.Component;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.weld.environment.se.Weld;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * The implementation of an {@link Interceptor} allowing eXo Kernel to interact with a weld container
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class WeldContainer extends AbstractInterceptor
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -5805946626633663689L;

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ext.provider.impl.weld.v2.WeldContainer");

   /**
    * The name of the weld package
    */
   private static final String WELD_PACKAGE_NAME = org.jboss.weld.Container.class.getPackage().getName();

   /**
    * The weld object allowing to initialize and stop the weld container
    */
   private Weld weld;

   /**
    * The weld container
    */
   private org.jboss.weld.environment.se.WeldContainer container;

   /**
    * The helper used to access to the extensions and to know if a given class is part of the scope of
    * {@link Weld}
    */
   private WeldContainerHelper helper;

   /**
    * The weld container, we will use it mainly to shutdown it manually in case of a failure
    */
   private org.jboss.weld.Container weldContainer;

   /**
    * {@inheritDoc}
    */
   @Override
   public Object getComponentInstance(Object componentKey)
   {
      Object result = super.getComponentInstance(componentKey);
      if (weld != null && result == null && componentKey instanceof Class<?>)
      {
         Class<?> type = (Class<?>)componentKey;
         if (helper.isIncluded(type))
         {
            Instance<?> instance = container.instance().select(type);
            if (instance != null)
            {
               result = instance.get();
            }
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
      if (weld != null && result == null && helper.isIncluded(componentType))
      {
         Instance<T> instance = container.instance().select(componentType);
         if (instance != null)
         {
            result = instance.get();
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public ComponentAdapter<?> getComponentAdapter(Object componentKey)
   {
      ComponentAdapter<?> result = super.getComponentAdapter(componentKey);
      if (weld != null && result == null && componentKey instanceof Class<?>)
      {
         Class<?> type = (Class<?>)componentKey;
         if (helper.isIncluded(type))
         {
            @SuppressWarnings("rawtypes")
            Instance instance = container.instance().select(type);
            if (instance != null)
            {
               result = createComponentAdapter(type, instance);
            }
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType)
   {
      ComponentAdapter<T> result = super.getComponentAdapterOfType(componentType);
      if (weld != null && result == null && helper.isIncluded(componentType))
      {
         Instance<T> instance = container.instance().select(componentType);
         if (instance != null)
         {
            result = createComponentAdapter(componentType, instance);
         }
      }
      return result;
   }

   private <T> ComponentAdapter<T> createComponentAdapter(final Class<T> type, final Instance<T> instance)
   {
      return new AbstractComponentAdapter<T>(type, type)
      {
         /**
          * The serial UID
          */
         private static final long serialVersionUID = 8230487164261120364L;

         public T getComponentInstance() throws ContainerException
         {
            return instance.get();
         }
      };
   }

   @SuppressWarnings("unchecked")
   private <T> ComponentAdapter<T> createComponentAdapter(final Class<T> type, final Bean<?> b)
   {
      return new AbstractComponentAdapter<T>(type, (Class<T>)b.getBeanClass())
      {
         /**
          * The serial UID
          */
         private static final long serialVersionUID = -2398896047339159840L;

         public T getComponentInstance() throws ContainerException
         {
            return type.cast(container.instance().select(b.getBeanClass()).get());
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      List<ComponentAdapter<T>> result = super.getComponentAdaptersOfType(componentType);
      if (weld != null)
      {
         result = new ArrayList<ComponentAdapter<T>>(result);
         Set<Bean<?>> beans = container.getBeanManager().getBeans(componentType);
         if (beans != null)
         {
            for (Bean<?> b : beans)
            {
               result.add(createComponentAdapter(componentType, b));
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
      if (weld != null)
      {
         Instance<T> instance = container.instance().select(componentType);
         if (instance != null)
         {
            for (T t : instance)
            {
               result.add(t);
            }
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void start()
   {
      ConfigurationManager cm = super.getComponentInstanceOfType(ConfigurationManager.class);
      // We check if the component has been defined in the configuration of the current container
      // The goal is to enable the WeldContainer only if it is needed
      Component component = cm.getComponent(WeldContainerHelper.class);
      if (component == null)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("No WeldContainerHelper has been defined, thus the WeldContainer will be disabled."
               + " To enable the Weld Integration please define an WeldContainerHelper");
         }
      }
      else
      {
         Weld weld = new Weld();
         weld.addExtension(new WeldExtension());
         WeldContainerHelper helper = super.getComponentInstanceOfType(WeldContainerHelper.class);
         List<Extension> extensions = helper.getExtensions();
         if (extensions != null)
         {
            for (Extension e : extensions)
            {
               weld.addExtension(e);
            }
         }
         this.helper = helper;
         this.container = weld.initialize();
         // This is an ugly hack to make sure that the BeanManagerProxy is initialized with the right Container
         // This is needed especially when we intend to initialize several weld containers within the same instance
         container.getBeanManager().getBeans(org.jboss.weld.environment.se.WeldContainer.class);
         this.weldContainer = org.jboss.weld.Container.instance();
         this.weld = weld;
         LOG.info("A WeldContainer has been enabled using the WeldContainerHelper " + helper.getClass());
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
      if (weld != null)
      {
         org.jboss.weld.Container currentContainer =
            org.jboss.weld.Container.available() ? org.jboss.weld.Container.instance() : null;
         try
         {
            weld.shutdown();
         }
         catch (RuntimeException e)
         {
            // In case we have several weld container initialized, we will get an IllegalStateException because weld doesn't allow to have several weld containers
            if (LOG.isDebugEnabled())
            {
               LOG.debug("Could not shutdown the weld container properly", e);
            }
         }
         if (currentContainer != weldContainer)
         {
            // Clean up manually the container in case the current container is not the container corresponding to the container of this weld container
            weldContainer.cleanup();
         }
         weld = null;
         container = null;
         helper = null;
         weldContainer = null;
      }
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return "WeldIntegration";
   }

   private class WeldExtension implements Extension
   {
      @SuppressWarnings({"unchecked", "rawtypes", "unused"})
      void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm)
      {
         Collection<ComponentAdapter<?>> adapters = delegate.getComponentAdapters();
         for (ComponentAdapter<?> adapter : adapters)
         {
            abd.addBean(new ComponentAdapterBean(adapter));
         }
      }

      @SuppressWarnings("unused")
      <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
      {
         Class<T> clazz = pat.getAnnotatedType().getJavaClass();
         if (clazz.getName().startsWith(WELD_PACKAGE_NAME))
            return;
         if (!helper.isIncluded(clazz))
         {
            pat.veto();
         }
      }
   }

   private static class ComponentAdapterBean<T> implements Bean<T>
   {
      private final ComponentAdapter<T> adapter;

      public ComponentAdapterBean(ComponentAdapter<T> adapter)
      {
         this.adapter = adapter;
      }

      /**
       * {@inheritDoc}
       */
      public T create(CreationalContext<T> ctx)
      {
         return adapter.getComponentInstance();
      }

      /**
       * {@inheritDoc}
       */
      public void destroy(T instance, CreationalContext<T> ctx)
      {
         ctx.release();
      }

      /**
       * {@inheritDoc}
       */
      public Set<Type> getTypes()
      {
         Set<Type> types = new HashSet<Type>();
         types.add(adapter.getComponentImplementation());
         if (adapter.getComponentKey() instanceof Class<?>)
         {
            types.add((Class<?>)adapter.getComponentKey());
         }
         types.add(Object.class);
         return types;
      }

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings("serial")
      public Set<Annotation> getQualifiers()
      {
         Set<Annotation> qualifiers = new HashSet<Annotation>();
         qualifiers.add(new AnnotationLiteral<Default>()
         {
         });
         qualifiers.add(new AnnotationLiteral<Any>()
         {
         });
         if (adapter.getComponentKey() instanceof String)
         {
            qualifiers.add(new Named()
            {
               public String value()
               {
                  return (String)adapter.getComponentKey();
               }

               public Class<? extends Annotation> annotationType()
               {
                  return Named.class;
               }
            });
         }
         return qualifiers;
      }

      /**
       * {@inheritDoc}
       */
      public Class<? extends Annotation> getScope()
      {
         return Singleton.class;
      }

      /**
       * {@inheritDoc}
       */
      public String getName()
      {
         return null;
      }

      /**
       * {@inheritDoc}
       */
      public Set<Class<? extends Annotation>> getStereotypes()
      {
         return Collections.emptySet();
      }

      /**
       * {@inheritDoc}
       */
      public boolean isAlternative()
      {
         return false;
      }

      /**
       * {@inheritDoc}
       */
      public Class<?> getBeanClass()
      {
         return adapter.getComponentImplementation();
      }

      /**
       * {@inheritDoc}
       */
      public Set<InjectionPoint> getInjectionPoints()
      {
         return Collections.emptySet();
      }

      /**
       * {@inheritDoc}
       */
      public boolean isNullable()
      {
         return false;
      }
   }
}
