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
import com.google.inject.ConfigurationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.AbstractComponentAdapter;
import org.exoplatform.container.AbstractInterceptor;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.Interceptor;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.security.PrivilegedAction;
import java.util.Collection;

/**
 * The implementation of an {@link Interceptor} allowing eXo Kernel to interact with a google guice container
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
    * The google guice injector
    */
   private Injector injector;

   /**
    * {@inheritDoc}
    */
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
      PrivilegedAction<T> action = new PrivilegedAction<T>()
      {
         public T run()
         {
            try
            {
               Binding<?> binding = injector.getBinding(componentType);
               if (!(binding.getProvider() instanceof ComponentAdapterProvider))
               {
                  return componentType.cast(binding.getProvider().get());
               }
            }
            catch (ConfigurationException e)
            {
               LOG.debug("Could not find a binding for " + componentType, e);
            }
            return null;
         }
      };
      return SecurityHelper.doPrivilegedAction(action);
   }

   /**
    * {@inheritDoc}
    */
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
   public ComponentAdapter getComponentAdapter(Object componentKey)
   {
      ComponentAdapter result = super.getComponentAdapter(componentKey);
      if (result == null && componentKey instanceof Class<?>)
      {
         Class<?> type = (Class<?>)componentKey;
         result = getComponentAdapter(type);
      }
      return result;
   }

   protected ComponentAdapter getComponentAdapter(final Class<?> type)
   {
      PrivilegedAction<ComponentAdapter> action = new PrivilegedAction<ComponentAdapter>()
      {
         public ComponentAdapter run()
         {
            try
            {
               final Binding<?> binding = injector.getBinding(type);
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
            catch (ConfigurationException e)
            {
               LOG.debug("Could not find a binding for " + type, e);
            }
            return null;
         }
      };
      return SecurityHelper.doPrivilegedAction(action);
   }

   /**
    * {@inheritDoc}
    */
   public ComponentAdapter getComponentAdapterOfType(Class<?> componentType)
   {
      ComponentAdapter result = super.getComponentAdapter(componentType);
      if (result == null)
      {
         result = getComponentAdapter(componentType);
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public void start()
   {
      if (injector == null)
      {
         injector = Guice.createInjector(new AbstractModule()
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
      }
      super.start();
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
