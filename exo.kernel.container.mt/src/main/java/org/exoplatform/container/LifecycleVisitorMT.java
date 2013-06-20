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

import org.exoplatform.container.spi.Container;
import org.exoplatform.container.spi.ContainerException;
import org.exoplatform.container.spi.ContainerVisitor;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Disposable;
import org.picocontainer.Startable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class LifecycleVisitorMT implements ContainerVisitor
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.LifecycleVisitorMT");

   private static final Method START;

   private static final Method STOP;

   private static final Method DISPOSE;
   static
   {
      try
      {
         START = Startable.class.getMethod("start", (Class<?>[])null);
         STOP = Startable.class.getMethod("stop", (Class<?>[])null);
         DISPOSE = Disposable.class.getMethod("dispose", (Class<?>[])null);
      }
      catch (NoSuchMethodException e)
      {
         throw new InternalError(e.getMessage());
      }
   }

   private final Method method;

   protected final Class<?> type;

   private final boolean visitInInstantiationOrder;

   protected final List<Object> componentInstances;

   private final boolean ignoreError;

   public LifecycleVisitorMT(Method method, Class<?> ofType, boolean visitInInstantiationOrder, boolean ignoreError)
   {
      this.method = method;
      this.type = ofType;
      this.visitInInstantiationOrder = visitInInstantiationOrder;
      this.componentInstances = new ArrayList<Object>();
      this.ignoreError = ignoreError;
   }

   protected Object traverse(Container container)
   {
      componentInstances.clear();
      try
      {
         visitContainer(container);
         if (!visitInInstantiationOrder)
         {
            Collections.reverse(componentInstances);
         }
         for (Iterator<?> iterator = componentInstances.iterator(); iterator.hasNext();)
         {
            Object o = iterator.next();
            invoke(o);
         }
      }
      finally
      {
         componentInstances.clear();
      }
      return Void.TYPE;
   }

   protected void invoke(Object o)
   {
      try
      {
         method.invoke(o, (Object[])null);
      }
      catch (IllegalArgumentException e)
      {
         if (ignoreError)
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug("Can't call " + method.getName() + " on " + o, e);
            }
            return;
         }
         throw new ContainerException("Can't call " + method.getName() + " on " + o, e);
      }
      catch (IllegalAccessException e)
      {
         if (ignoreError)
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug("Can't call " + method.getName() + " on " + o, e);
            }
            return;
         }
         throw new ContainerException("Can't call " + method.getName() + " on " + o, e);
      }
      catch (InvocationTargetException e)
      {
         if (ignoreError)
         {
            if (LOG.isDebugEnabled())
            {
               LOG.debug("Failed when calling " + method.getName() + " on " + o, e.getTargetException());
            }
            return;
         }
         throw new ContainerException("Failed when calling " + method.getName() + " on " + o, e.getTargetException());
      }
   }

   public void visitContainer(Container container)
   {
      componentInstances.addAll(container.getComponentInstancesOfType(type));
   }

   /**
    * Invoke the standard Container lifecycle for {@link Startable#start()}.
    * @param node The node to start the traversal.
    */
   public static void start(Container container)
   {
      new StartVisitorMT().traverse(container);
   }

   /**
    * Invoke the standard Container lifecycle for {@link Startable#stop()}.
    * @param node The node to start the traversal.
    */
   public static void stop(Container container)
   {
      new LifecycleVisitorMT(STOP, Startable.class, false, true).traverse(container);
   }

   /**
    * Invoke the standard Container lifecycle for {@link Disposable#dispose()}.
    * @param node The node to start the traversal.
    */
   public static void dispose(Container container)
   {
      new LifecycleVisitorMT(DISPOSE, Disposable.class, false, true).traverse(container);
   }

   private static class StartVisitorMT extends LifecycleVisitorMT
   {
      public StartVisitorMT()
      {
         super(START, Startable.class, true, false);
      }

      public void visitContainer(Container container)
      {
         Container co = container;
         do
         {
            if (co instanceof ConcurrentContainerMT)
            {
               break;
            }
         }
         while ((co = co.getSuccessor()) != null);
         if (co instanceof ConcurrentContainerMT)
         {
            ConcurrentContainerMT containerMT = (ConcurrentContainerMT)co;
            componentInstances.addAll(containerMT.getComponentAdapterTaskContextAwaresOfType(type));
         }
         else
         {
            super.visitContainer(container);
         }
      }
      
      protected void invoke(Object o)
      {
         if (o instanceof ComponentAdapterTaskContextAware)
         {
            ((ComponentAdapterTaskContextAware)o).start(null);
         }
         else
         {
            super.invoke(o);
         }
      }
   }
}
