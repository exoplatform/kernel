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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.container.management.ManageableContainer;
import org.exoplatform.container.spi.After;
import org.exoplatform.container.spi.Before;
import org.exoplatform.container.spi.Interceptor;
import org.exoplatform.container.spi.InterceptorChainFactory;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * The default implementation of a {@link InterceptorChainFactory}. This implementation
 * uses 3 static {@link Interceptor} which are {@link ConcurrentContainer},
 * {@link CachingContainer} and {@link ManageableContainer} and uses a list of dynamic {@link Interceptor}
 * that are retrieved thanks to the {@link ServiceLoader}. Then according to the annotations {@link Before}
 * and {@link After} defined on the dynamic {@link Interceptor}, it will define an ordered list of {@link Interceptor}
 * classes which will be used at each next calls of {@link #getInterceptorChain(ExoContainer, ExoContainer)} to
 * re-create the exact same chain of {@link Interceptor}.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DefaultInterceptorChainFactory implements InterceptorChainFactory
{
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.DefaultInterceptorChainFactory");

   /**
    * The list of class that will defined the {@link Interceptor} chain
    */
   private volatile List<Class<? extends Interceptor>> chain;

   /**
    * {@inheritDoc}
    */
   public Interceptor getInterceptorChain(ExoContainer holder, ExoContainer parent)
   {
      if (chain == null)
      {
         synchronized (this)
         {
            if (chain == null)
            {
               List<Interceptor> staticInts = getStaticInterceptors(holder, parent);
               List<Interceptor> dynamicInts = getDynamicInterceptors(holder, parent);
               List<Interceptor> interceptors = resolve(staticInts, dynamicInts);
               Interceptor result = null;
               List<Class<? extends Interceptor>> chain = new LinkedList<Class<? extends Interceptor>>();
               StringBuilder sb = null;
               boolean isDevelopping = PropertyManager.isDevelopping();
               if (isDevelopping)
               {
                  sb = new StringBuilder();
               }
               for (int i = 0, length = interceptors.size(); i < length; i++)
               {
                  Interceptor it = interceptors.get(i);
                  it.setSuccessor(result);
                  chain.add(it.getClass());
                  if (isDevelopping)
                  {
                     sb.insert(0, "-> " + it.getClass().getName() + " ");
                  }
                  result = it;
               }
               if (isDevelopping)
               {
                  LOG.info("The interceptor chain used is " + sb);
               }
               this.chain = chain;
               return result;
            }
         }
      }
      Interceptor result = null;
      for (Iterator<Class<? extends Interceptor>> iter = chain.iterator(); iter.hasNext();)
      {
         Class<? extends Interceptor> iClass = iter.next();
         try
         {
            Interceptor it = iClass.cast(iClass.newInstance());
            it.setHolder(holder);
            it.setParent(parent);
            it.setSuccessor(result);
            result = it;
         }
         catch (Exception e)
         {
             LOG.error("Cannot instantiate inteceptor of class " + iClass, e);
         }
      }
      return result;
   }

   /**
    * Gives the static {@link Interceptor} from the last to the head
    */
   protected List<Interceptor> getStaticInterceptors(ExoContainer holder, ExoContainer parent)
   {
      List<Interceptor> list = new ArrayList<Interceptor>(4);
      list.add(new ConcurrentContainer(holder, parent));
      list.add(new CachingContainer());
      list.add(new ManageableContainer(holder, parent));
      return list;
   }

   /**
    * Gives the dynamic {@link Interceptor} from the last to the head
    */
   protected List<Interceptor> getDynamicInterceptors(ExoContainer holder, ExoContainer parent)
   {
      List<Interceptor> list = new ArrayList<Interceptor>();
      ServiceLoader<Interceptor> loader = ServiceLoader.load(Interceptor.class);
      for (Iterator<Interceptor> it = loader.iterator(); it.hasNext();)
      {
         Interceptor interceptor = it.next();
         interceptor.setHolder(holder);
         interceptor.setParent(parent);
         list.add(interceptor);
      }
      return list;
   }

   /**
    * Resolves all the dynamic {@link Interceptor} and inject them into a list of {@link Interceptor}
    * according to the annotation {@link Before} and {@link After} 
    * @return the ordered list of interceptors
    */
   protected List<Interceptor> resolve(List<Interceptor> staticInts, List<Interceptor> dynamicInts)
   {
      List<Interceptor> alreadyResolved = new ArrayList<Interceptor>(staticInts);
      List<Interceptor> toBeResolved = new LinkedList<Interceptor>(dynamicInts);
      for (Iterator<Interceptor> iter = toBeResolved.iterator(); iter.hasNext();)
      {
         resolveNext(alreadyResolved, iter, false);
      }
      for (Iterator<Interceptor> iter = toBeResolved.iterator(); iter.hasNext();)
      {
         resolveNext(alreadyResolved, iter, true);
      }
      return alreadyResolved;
   }

   /**
    * Resolves the next dynamic {@link Interceptor} using the annotation {@link Before} and {@link After}
    * @param alreadyResolved the list of {@link Interceptor} already resolved
    * @param iter the Iterator containing the remaining dynamic {@link Interceptor} to resolve
    * @param resolveIfAbsent indicates if the interceptor must be resolved if the referred interceptor could not
    * be found
    */
   protected void resolveNext(List<Interceptor> alreadyResolved, Iterator<Interceptor> iter, boolean resolveIfAbsent)
   {
      Interceptor it = iter.next();
      Before b = it.getClass().getAnnotation(Before.class);
      if (b != null)
      {
         // An annotation Before has been defined
         String id = b.value();
         if (id == null || (id = id.trim()).isEmpty())
         {
            // No id set
            if (PropertyManager.isDevelopping())
            {
               LOG.warn("No value set for the annotation Before of the interceptor " + it.getClass());
            }
            alreadyResolved.add(it);
            iter.remove();
            return;
         }
         // The id has been set
         for (int i = 0, length = alreadyResolved.size(); i < length; i++)
         {
            Interceptor interceptor = alreadyResolved.get(i);
            if (id.equals(interceptor.getId()))
            {
               // The id has been found
               if (i < length - 1)
               {
                  alreadyResolved.add(i + 1, it);
               }
               else
               {
                  alreadyResolved.add(it);
               }
               iter.remove();
               return;
            }
         }
         if (resolveIfAbsent)
         {
            if (PropertyManager.isDevelopping())
            {
                LOG.warn("Could not find the interceptor of " + id + " required by the interceptor "
                        + it.getClass());
            }
            alreadyResolved.add(it);
            iter.remove();
         }
         return;
      }
      After a = it.getClass().getAnnotation(After.class);
      if (a != null)
      {
         // An annotation After has been defined
         String id = a.value();
         if (id == null || (id = id.trim()).isEmpty())
         {
            // No id set
            if (PropertyManager.isDevelopping())
            {
                LOG.warn("No value set for the annotation After of the interceptor " + it.getClass());
            }
            alreadyResolved.add(it);
            iter.remove();
            return;
         }
         // The id has been set
         for (int i = 0, length = alreadyResolved.size(); i < length; i++)
         {
            Interceptor interceptor = alreadyResolved.get(i);
            if (id.equals(interceptor.getId()))
            {
               // The id has been found
               alreadyResolved.add(i, it);
               iter.remove();
               return;
            }
         }
         if (resolveIfAbsent)
         {
            if (PropertyManager.isDevelopping())
            {
                LOG.warn("Could not find the interceptor of " + id + " required by the interceptor "
                        + it.getClass());
            }
            alreadyResolved.add(it);
            iter.remove();
         }
         return;
      }
      // No annotation has been defined
      alreadyResolved.add(it);
      iter.remove();
   }
}
