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
package org.exoplatform.container.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Binder;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import org.exoplatform.container.AbstractComponentAdapter;
import org.exoplatform.container.AbstractInterceptor;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.Interceptor;
import org.exoplatform.container.xml.Component;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The implementation of an {@link Interceptor} allowing eXo Kernel to interact with a Google Guice container
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class GuiceContainer extends AbstractInterceptor
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = -6662420267945445921L;

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ext.provider.impl.guice.v3.GuiceContainer");

   /**
    * The Google Guice injector
    */
   private Injector injector;

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> T getComponentInstance(Object componentKey, Class<T> bindType)
   {
      T result = super.getComponentInstance(componentKey, bindType);
      if (result == null && injector != null)
      {
         final Binding<?> binding;
         if (componentKey instanceof Class<?> && !((Class<?>)componentKey).isAnnotation())
         {
            binding = injector.getExistingBinding(Key.get((Class<?>)componentKey));
         }
         else
         {
            if (componentKey instanceof String)
            {
               binding = injector.getExistingBinding(Key.get(bindType, Names.named((String)componentKey)));
            }
            else if (componentKey instanceof Class<?>)
            {
               binding = injector.getExistingBinding(Key.get(bindType, (Class<? extends Annotation>)componentKey));
            }
            else
            {
               return null;
            }
         }
         if (binding == null || binding.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
         {
            return null;
         }
         result = bindType.cast(binding.getProvider().get());
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
      if (result == null && injector != null)
      {
         Binding<?> binding = injector.getExistingBinding(Key.get(componentType));
         if (binding == null || binding.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
         {
            return null;
         }
         result = componentType.cast(binding.getProvider().get());
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> ComponentAdapter<T> getComponentAdapter(Object componentKey, Class<T> bindType)
   {
      ComponentAdapter<T> result = super.getComponentAdapter(componentKey, bindType);
      if (result == null && injector != null)
      {
         final Binding<?> binding;
         if (componentKey instanceof Class<?> && !((Class<?>)componentKey).isAnnotation())
         {
            binding = injector.getExistingBinding(Key.get((Class<?>)componentKey));
         }
         else
         {
            if (componentKey instanceof String)
            {
               binding = injector.getExistingBinding(Key.get(bindType, Names.named((String)componentKey)));
            }
            else if (componentKey instanceof Class<?>)
            {
               binding = injector.getExistingBinding(Key.get(bindType, (Class<? extends Annotation>)componentKey));
            }
            else
            {
               return null;
            }
         }
         if (binding == null || binding.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
         {
            return null;
         }
         result = createComponentAdapter(bindType, binding);
      }
      return result;
   }

   private <T> ComponentAdapter<T> createComponentAdapter(final Class<T> type, final Binding<?> binding)
   {
      return new AbstractComponentAdapter<T>(type, type)
      {
         /**
          * The serial UID
          */
         private static final long serialVersionUID = 4241559622835718141L;

         public T getComponentInstance() throws ContainerException
         {
            return type.cast(binding.getProvider().get());
         }

         public boolean isSingleton()
         {
            return false;
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> componentType)
   {
      ComponentAdapter<T> result = super.getComponentAdapterOfType(componentType);
      if (result == null && injector != null)
      {
         final Binding<?> binding = injector.getExistingBinding(Key.get(componentType));
         if (binding == null || binding.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
         {
            return null;
         }
         result = createComponentAdapter(componentType, binding);
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   @Override
   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      List<ComponentAdapter<T>> result = super.getComponentAdaptersOfType(componentType);
      if (injector != null)
      {
         result = new ArrayList<ComponentAdapter<T>>(result);
         for (Binding<?> b : injector.getAllBindings().values())
         {
            if (b.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
            {
               continue;
            }
            else if (componentType.isAssignableFrom(b.getKey().getTypeLiteral().getRawType()))
            {
               result.add((ComponentAdapter<T>)createComponentAdapter(b.getKey().getTypeLiteral().getRawType(), b));
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
      if (injector != null)
      {
         result = new ArrayList<T>(result);
         for (Binding<?> b : injector.getAllBindings().values())
         {
            if (b.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
            {
               continue;
            }
            else if (componentType.isAssignableFrom(b.getKey().getTypeLiteral().getRawType()))
            {
               result.add(componentType.cast(b.getProvider().get()));
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
      // The goal is to enable the GuicegContainer only if it is needed
      Component component = cm.getComponent(ModuleProvider.class);
      if (component == null)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("No ModuleProvider has been defined, thus the GuiceContainer will be disabled."
               + " To enable the Guice Integration please define a ModuleProvider");
         }
      }
      else
      {
         ModuleProvider provider = super.getComponentInstanceOfType(ModuleProvider.class);
         injector = Guice.createInjector(provider.getModule(), new AbstractModule()
         {
            @SuppressWarnings({"unchecked", "rawtypes"})
            @Override
            protected void configure()
            {
               Collection<ComponentAdapter<?>> adapters = delegate.getComponentAdapters();
               Binder binder = binder();
               for (ComponentAdapter<?> adapter : adapters)
               {
                  Object key = adapter.getComponentKey();
                  Class<?> type;
                  Annotation annotation = null;
                  Class<? extends Annotation> annotationType = null;
                  if (key instanceof Class<?> && !((Class<?>)key).isAnnotation())
                  {
                     type = (Class<?>)key;
                  }
                  else
                  {
                     if (key instanceof String)
                     {
                        annotation = Names.named((String)key);
                     }
                     else if (key instanceof Class<?>)
                     {
                        annotationType = (Class<? extends Annotation>)key;
                     }
                     type = adapter.getComponentImplementation();
                  }
                  if (annotation == null && annotationType == null)
                  {
                     binder.bind(type).toProvider(new ComponentAdapterProvider(type, adapter));
                  }
                  else
                  {
                     // As we don't know the type, we will bind it for each super classes and interfaces too
                     ComponentAdapterProvider provider = new ComponentAdapterProvider(type, adapter);
                     bindAll(binder, type, provider, annotation, annotationType);
                  }
               }
            }
         });
         LOG.info("A GuiceContainer has been enabled using the ModuleProvider " + provider.getClass());
      }
      super.start();
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private static void bindAll(Binder binder, Class<?> clazz, ComponentAdapterProvider provider, Annotation annotation,
      Class<? extends Annotation> annotationType)
   {
      if (clazz == null || clazz.equals(Object.class))
         return;
      if (annotation == null)
      {
         binder.bind(clazz).annotatedWith(annotationType).toProvider(provider);
      }
      else
      {
         binder.bind(clazz).annotatedWith(annotation).toProvider(provider);
      }
      for (Class<?> c : clazz.getInterfaces())
      {
         bindAll(binder, c, provider, annotation, annotationType);
      }
      bindAll(binder, clazz.getSuperclass(), provider, annotation, annotationType);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public void stop()
   {
      super.stop();
      injector = null;
   }

   /**
    * {@inheritDoc}
    */
   public String getId()
   {
      return "GuiceIntegration";
   }

   private static class ComponentAdapterProvider<T> implements Provider<T>
   {

      private final Class<T> type;

      private final ComponentAdapter<T> adapter;

      private ComponentAdapterProvider(Class<T> type, ComponentAdapter<T> adapter)
      {
         this.type = type;
         this.adapter = adapter;
      }

      /**
       * {@inheritDoc}
       */
      public T get()
      {
         return type.cast(adapter.getComponentInstance());
      }
   }
}
