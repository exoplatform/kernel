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
   @Override
   public Object getComponentInstance(Object componentKey)
   {
      Object result = super.getComponentInstance(componentKey);
      if (result == null && componentKey instanceof Class<?>)
      {
         result = getComponentInstance((Class<?>)componentKey);
      }
      return result;
   }

   protected <T> T getComponentInstance(final Class<T> componentType)
   {
      if (injector == null)
      {
         return null;
      }
      Binding<?> binding = injector.getExistingBinding(Key.get(componentType));
      if (binding == null || binding.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
      {
         return null;
      }
      return componentType.cast(binding.getProvider().get());
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public <T> T getComponentInstanceOfType(Class<T> componentType)
   {
      T result = super.getComponentInstanceOfType(componentType);
      if (result == null)
      {
         result = getComponentInstance(componentType);
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
      if (result == null && componentKey instanceof Class<?>)
      {
         result = getComponentAdapter((Class<?>)componentKey);
      }
      return result;
   }

   protected ComponentAdapter getComponentAdapter(final Class<?> type)
   {
      if (injector == null)
      {
         return null;
      }
      final Binding<?> binding = injector.getExistingBinding(Key.get(type));
      if (binding == null || binding.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
      {
         return null;
      }
      return createComponentAdapter(type, binding);
   }

   private ComponentAdapter createComponentAdapter(final Class<?> type, final Binding<?> binding)
   {
      return new AbstractComponentAdapter(type, type)
      {
         /**
          * The serial UID
          */
         private static final long serialVersionUID = 4241559622835718141L;

         public Object getComponentInstance() throws ContainerException
         {
            return binding.getProvider().get();
         }
      };
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public ComponentAdapter getComponentAdapterOfType(Class<?> componentType)
   {
      ComponentAdapter result = super.getComponentAdapterOfType(componentType);
      if (result == null)
      {
         result = getComponentAdapter(componentType);
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public List<ComponentAdapter> getComponentAdaptersOfType(Class<?> componentType)
   {
      List<ComponentAdapter> result = super.getComponentAdaptersOfType(componentType);
      if (injector != null)
      {
         result = new ArrayList<ComponentAdapter>(result);
         for (Binding<?> b : injector.getAllBindings().values())
         {
            if (b.getProvider().toString().startsWith(ComponentAdapterProvider.class.getName()))
            {
               continue;
            }
            else if (componentType.isAssignableFrom(b.getKey().getTypeLiteral().getRawType()))
            {
               result.add(createComponentAdapter(b.getKey().getTypeLiteral().getRawType(), b));
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
      Component component = null;
      try
      {
         // We check if the component has been defined in the configuration of the current container
         // The goal is to enable the GuicegContainer only if it is needed
         component = cm.getComponent(ModuleProvider.class);
      }
      catch (Exception e)
      {
         if (LOG.isDebugEnabled())
         {
            LOG.debug("Could not check if a ModuleProvider has been defined: " + e.getMessage());
         }
      }
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
               Collection<ComponentAdapter> adapters = delegate.getComponentAdapters();
               Binder binder = binder();
               for (ComponentAdapter adapter : adapters)
               {
                  Object key = adapter.getComponentKey();
                  Class<?> type;
                  String name = null;
                  if (key instanceof Class<?>)
                  {
                     type = (Class<?>)key;
                  }
                  else
                  {
                     if (key instanceof String)
                     {
                        name = (String)key;
                     }
                     type = adapter.getComponentImplementation();
                  }
                  if (name == null)
                  {
                     binder.bind(type).toProvider(new ComponentAdapterProvider(type, adapter));
                  }
                  else
                  {
                     binder.bind(type).annotatedWith(Names.named(name))
                        .toProvider(new ComponentAdapterProvider(type, adapter));
                  }
               }
            }
         });
         LOG.info("A GuiceContainer has been enabled using the ModuleProvider " + provider.getClass());
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

      private final ComponentAdapter adapter;

      private ComponentAdapterProvider(Class<T> type, ComponentAdapter adapter)
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
