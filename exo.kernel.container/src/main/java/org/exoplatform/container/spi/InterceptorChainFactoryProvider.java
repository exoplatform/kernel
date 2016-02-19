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
package org.exoplatform.container.spi;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.DefaultInterceptorChainFactory;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * This class will provide the {@link InterceptorChainFactory} to use. It will try to get it thanks to the {@link ServiceLoader}
 * if none can be found it will provide a default one.
 * 
 * It will try to load the {@link InterceptorChainFactory} using the
 * current thread's {@linkplain Thread#getContextClassLoader()} context class loader. By default it will use the
 * {@link DefaultInterceptorChainFactory}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class InterceptorChainFactoryProvider
{

   /**
    * The {@link InterceptorChainFactory} that will be used to create all the {@link Container} instances
    */
   private static final InterceptorChainFactory FACTORY;

   static
   {
      InterceptorChainFactory factory = null;
      ServiceLoader<InterceptorChainFactory> loader = ServiceLoader.load(InterceptorChainFactory.class);
      Iterator<InterceptorChainFactory> it = loader.iterator();
      if (it.hasNext())
      {
         factory = it.next();
      }
      else
      {
         factory = new DefaultInterceptorChainFactory();
      }
      if (PropertyManager.isDevelopping())
      {
         System.out.println("The container factory used is " + factory.getClass().getName()); //NOSONAR
      }
      FACTORY = factory;
   }

   /**
    * Prevents instantiation
    */
   private InterceptorChainFactoryProvider()
   {
   }

   /**
    * Gives the {@link InterceptorChainFactory} that must be used to create all the {@link Interceptor} chains 
    */
   public static InterceptorChainFactory getInterceptorChainFactory()
   {
      return FACTORY;
   }
}
