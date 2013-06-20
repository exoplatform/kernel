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
package org.exoplatform.container;

import org.exoplatform.container.management.ManageableContainer;
import org.exoplatform.container.spi.After;
import org.exoplatform.container.spi.Before;
import org.exoplatform.container.spi.Interceptor;
import org.exoplatform.container.spi.InterceptorChainFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The "multi-threaded" implementation of a {@link InterceptorChainFactory}. This implementation
 * uses 3 static {@link Interceptor} which are {@link ConcurrentContainerMT},
 * {@link CachingContainer} and {@link ManageableContainer} and uses a list of dynamic {@link Interceptor}
 * that are retrieved thanks to the {@link ServiceLoader}. Then according to the annotations {@link Before}
 * and {@link After} defined on the dynamic {@link Interceptor}, it will define an ordered list of {@link Interceptor}
 * classes which will be used at each next calls of {@link #getInterceptorChain(ExoContainer, ExoContainer)} to
 * re-create the exact same chain of {@link Interceptor}.

 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class MTInterceptorChainFactory extends DefaultInterceptorChainFactory
{
   /**
    * {@inheritDoc}
    */
   protected List<Interceptor> getStaticInterceptors(ExoContainer holder, ExoContainer parent)
   {
      List<Interceptor> list = new ArrayList<Interceptor>(4);
      list.add(new ConcurrentContainerMT(holder, parent));
      list.add(new CachingContainer());
      list.add(new ManageableContainer(holder, parent));
      return list;
   }
}
