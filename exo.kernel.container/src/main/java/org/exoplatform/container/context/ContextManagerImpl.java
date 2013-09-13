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
package org.exoplatform.container.context;

import org.exoplatform.container.component.ThreadContext;
import org.exoplatform.container.component.ThreadContextHolder;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The basic implementation of a {@link ContextManager} based on a {@link ConcurrentHashMap} that is
 * filled thanks to a component plugins.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ContextManagerImpl implements ContextManager, ThreadContextHolder
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ContextManagerImpl");

   /**
    * All the registered contexts
    */
   private volatile Map<String, AdvancedContext<?>> contexts = Collections
      .unmodifiableMap(new HashMap<String, AdvancedContext<?>>());

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public <K> AdvancedContext<K> getContext(Class<? extends Annotation> scope) throws IllegalStateException
   {
      return (AdvancedContext<K>)contexts.get(scope.getName());
   }

   /**
    * {@inheritDoc}
    */
   public boolean hasContext(Class<? extends Annotation> scope)
   {
      return contexts.containsKey(scope.getName());
   }

   /**
    * {@inheritDoc}
    */
   public <K> void addContext(AdvancedContext<K> ctx)
   {
      synchronized (this)
      {
         Map<String, AdvancedContext<?>> contextsTmp = new HashMap<String, AdvancedContext<?>>(contexts);
         contextsTmp.put(ctx.getScope().getName(), ctx);
         this.contexts = Collections.unmodifiableMap(contextsTmp);
         LOG.info("A context of type {} has been defined for the scope {}", ctx.getClass().getName(), ctx.getScope()
            .getName());
      }
   }

   /**
    * Adds all the contexts defined in the given plugin
    */
   public void addContexts(ContextPlugin plugin)
   {
      @SuppressWarnings("rawtypes")
      List<AdvancedContext> contexts = plugin.getContexts();
      if (contexts == null)
         return;
      for (AdvancedContext<?> ctx : contexts)
      {
         addContext(ctx);
      }
   }

   /**
    * {@inheritDoc}
    */
   public ThreadContext getThreadContext()
   {
      Collection<AdvancedContext<?>> allContexts = contexts.values();
      List<ThreadContext> result = new ArrayList<ThreadContext>();
      for (AdvancedContext<?> ctx : allContexts)
      {
         if (ctx instanceof ThreadContextHolder)
         {
            ThreadContextHolder holder = (ThreadContextHolder)ctx;
            result.add(holder.getThreadContext());
         }
      }
      return ThreadContext.merge(result);
   }
}
