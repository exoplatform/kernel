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
import org.jboss.weld.environment.se.Weld;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.Dependent;
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
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ext.provider.impl.weld.v1.WeldContainer");

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
   @SuppressWarnings("unchecked")
   @Override
   public <T> T getComponentInstance(final Object componentKey, Class<T> bindType)
   {
      T result = super.getComponentInstance(componentKey, bindType);
      if (weld != null && result == null)
      {
         if (componentKey instanceof Class<?> && !((Class<?>)componentKey).isAnnotation())
         {
            return getInstanceOfType((Class<T>)componentKey);
         }
         else if (componentKey instanceof String)
         {
            Set<Bean<?>> beans = container.getBeanManager().getBeans(bindType, createNamed((String)componentKey));
            if (beans != null && !beans.isEmpty())
            {
               return bindType.cast(container.instance().select(beans.iterator().next().getBeanClass()).get());
            }
         }
         else if (componentKey instanceof Class<?>)
         {
            final Class<? extends Annotation> annotationType = (Class<? extends Annotation>)componentKey;
            Annotation annotation = createAnnotation(annotationType);

            Set<Bean<?>> beans = container.getBeanManager().getBeans(bindType, annotation);
            if (beans != null && !beans.isEmpty())
            {
               return bindType.cast(container.instance().select(beans.iterator().next().getBeanClass()).get());
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
      if (weld != null && result == null)
      {
         result = getInstanceOfType(componentType);
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   private <T> T getInstanceOfType(final Class<T> componentType)
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<T>()
      {
         public T run()
         {
            if (helper.isIncluded(componentType))
            {
               Instance<T> instance = container.instance().select(componentType);
               if (instance != null)
               {
                  if (instance.isAmbiguous())
                  {
                     Set<Bean<?>> beans = container.getBeanManager().getBeans(componentType);
                     for (Bean<?> b : beans)
                     {
                        if (b.getBeanClass().isAnnotationPresent(Default.class))
                        {
                           instance = (Instance<T>)container.instance().select(b.getBeanClass());
                           break;
                        }
                     }
                  }
                  return instance.get();
               }
            }
            return null;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> ComponentAdapter<T> getComponentAdapter(final Object componentKey, Class<T> bindType)
   {
      ComponentAdapter<T> result = super.getComponentAdapter(componentKey, bindType);
      if (weld != null && result == null)
      {
         if (componentKey instanceof Class<?> && !((Class<?>)componentKey).isAnnotation())
         {
            return getAdapterOfType((Class<T>)componentKey);
         }
         else if (componentKey instanceof String)
         {
            Set<Bean<?>> beans = container.getBeanManager().getBeans(bindType, createNamed((String)componentKey));
            if (beans != null && !beans.isEmpty())
            {
               return createComponentAdapter(bindType,
                  (Instance<T>)container.instance().select(beans.iterator().next().getBeanClass()));
            }
         }
         else if (componentKey instanceof Class<?>)
         {
            final Class<? extends Annotation> annotationType = (Class<? extends Annotation>)componentKey;
            Annotation annotation = createAnnotation(annotationType);
            Set<Bean<?>> beans = container.getBeanManager().getBeans(bindType, annotation);
            if (beans != null && !beans.isEmpty())
            {
               return createComponentAdapter(bindType,
                  (Instance<T>)container.instance().select(beans.iterator().next().getBeanClass()));
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
      if (weld != null && result == null)
      {
         result = getAdapterOfType(componentType);
      }
      return result;
   }

   @SuppressWarnings("unchecked")
   private <T> ComponentAdapter<T> getAdapterOfType(Class<T> componentType)
   {
      if (helper.isIncluded(componentType))
      {
         Instance<T> instance = container.instance().select(componentType);
         if (instance != null)
         {
            if (instance.isAmbiguous())
            {
               Set<Bean<?>> beans = container.getBeanManager().getBeans(componentType);
               for (Bean<?> b : beans)
               {
                  if (b.getBeanClass().isAnnotationPresent(Default.class))
                  {
                     instance = (Instance<T>)container.instance().select(b.getBeanClass());
                     break;
                  }
               }
            }
            return createComponentAdapter(componentType, instance);
         }
      }
      return null;
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
            return SecurityHelper.doPrivilegedAction(new PrivilegedAction<T>()
            {
               public T run()
               {
                  return instance.get();
               }
            });
         }

         public boolean isSingleton()
         {
            return false;
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

         public boolean isSingleton()
         {
            return Singleton.class.equals(b.getScope());
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
         weld.addExtension(new WeldExtension(this));
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

   private static Annotation createAnnotation(final Class<? extends Annotation> annotationType)
   {
      return (Annotation)Proxy.newProxyInstance(annotationType.getClassLoader(), annotationType.getInterfaces(),
         new InvocationHandler()
         {
            public Object invoke(Object proxy, Method method, Object[] args)
            {
               if ("hashCode".equals(method.getName()))
               {
                  return annotationType.getName().hashCode();
               }
               else if ("equals".equals(method.getName()))
               {
                  return args[0].hashCode() == annotationType.getName().hashCode()
                     && args[0].toString().equals(annotationType.getName());
               }
               else if ("toString".equals(method.getName()))
               {
                  return annotationType.getName();
               }

               return annotationType;
            }
         });
   }

   private static Named createNamed(final String name)
   {
      return new Named()
      {
         public Class<? extends Annotation> annotationType()
         {
            return Named.class;
         }

         public String value()
         {
            return name;
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return "WeldIntegration";
   }

   /**
    * We switch to a public static class to allow weld to create a proxy the class
    */
   public static class WeldExtension implements Extension
   {
      private final WeldContainer container;

      public WeldExtension()
      {
         this(null);
      }

      public WeldExtension(WeldContainer container)
      {
         this.container = container;
      }

      @SuppressWarnings({"unchecked", "rawtypes"})
      void afterBeanDiscovery(@Observes AfterBeanDiscovery abd, BeanManager bm)
      {
         Collection<ComponentAdapter<?>> adapters = container.delegate.getComponentAdapters();
         for (ComponentAdapter<?> adapter : adapters)
         {
            abd.addBean(new ComponentAdapterBean(adapter));
         }
      }

      <T> void processAnnotatedType(@Observes ProcessAnnotatedType<T> pat)
      {
         Class<T> clazz = pat.getAnnotatedType().getJavaClass();
         if (clazz.getName().startsWith(WELD_PACKAGE_NAME))
            return;
         if (!container.helper.isIncluded(clazz))
         {
            pat.veto();
         }
      }
   }

   private static void bindAll(Class<?> clazz, Set<Type> types)
   {
      if (clazz == null || clazz.equals(Object.class))
         return;
      types.add(clazz);
      for (Class<?> c : clazz.getInterfaces())
      {
         bindAll(c, types);
      }
      bindAll(clazz.getSuperclass(), types);
   }

   private static class ComponentAdapterBean<T> implements Bean<T>
   {
      private final ComponentAdapter<T> adapter;
      private Set<Type> types;
      private Set<Annotation> qualifiers;

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
         if (types != null)
         {
            return types;
         }
         Set<Type> types = new HashSet<Type>();
         if (adapter.getComponentKey() instanceof Class<?> && !((Class<?>)adapter.getComponentKey()).isAnnotation())
         {
            types.add((Class<?>)adapter.getComponentKey());
            types.add(adapter.getComponentImplementation());
         }
         else
         {
            bindAll(adapter.getComponentImplementation(), types);
         }
         types.add(Object.class);
         return this.types = types;
      }

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings({"serial", "unchecked"})
      public Set<Annotation> getQualifiers()
      {
         if (qualifiers != null)
         {
            return qualifiers;
         }
         Set<Annotation> qualifiers = new HashSet<Annotation>();
         if (adapter.getComponentKey() instanceof String)
         {
            qualifiers.add(createNamed((String)adapter.getComponentKey()));
         }
         else if (adapter.getComponentKey() instanceof Class<?> && ((Class<?>)adapter.getComponentKey()).isAnnotation())
         {
            qualifiers.add(createAnnotation((Class<? extends Annotation>)adapter.getComponentKey()));
         }
         else
         {
            qualifiers.add(new AnnotationLiteral<Default>()
            {
            });
            qualifiers.add(new AnnotationLiteral<Any>()
            {
            });

         }
         return this.qualifiers = qualifiers;
      }

      /**
       * {@inheritDoc}
       */
      public Class<? extends Annotation> getScope()
      {
         return adapter.isSingleton() ? Singleton.class : Dependent.class;
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
