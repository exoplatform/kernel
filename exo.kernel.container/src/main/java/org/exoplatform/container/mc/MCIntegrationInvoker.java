/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.exoplatform.container.mc;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.ComponentAdapter;

import java.lang.reflect.Method;

import javax.servlet.ServletContext;

/**
 * This class performs a loosely coupled integration to mc-integration logic.
 * MC integration provides the ability to inject JBoss MC configured beans into
 * exo-kernel configured services.
 *
 * MC integration is only available when GateIn is running inside JBossAS or on top of JBoss MC.
 * When integration is not available the default behavior is used - as if this class was never called.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class MCIntegrationInvoker
{
   /**
    * Logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.MCIntegrationInvoker");

   /**
    * Reference to actual implementation of mc-integration - the one with hard dependencies on JBoss MC Kernel.
    */
   private static volatile MCIntegration mcInt;

   /**
    * Error flag. When set it signals that MC integration is not available in current runtime environment.
    */
   private static volatile boolean permFailure;

   /**
    * @see MCIntegration#getMCAdapter(org.picocontainer.ComponentAdapter)
    */
   public static synchronized ComponentAdapter getMCAdapter(ComponentAdapter componentAdapter)
   {
      MCIntegration mcInt = getMCIntegration();
      if (mcInt == null)
      {
         return componentAdapter;
      }

      return mcInt.getMCAdapter(componentAdapter);
   }

   /**
    * @see MCIntegration#hasMCKernel(org.picocontainer.ComponentAdapter)
    */
   public static synchronized boolean hasMCKernel(ComponentAdapter adapter)
   {
      MCIntegration mcInt = getMCIntegration();
      if (mcInt == null)
      {
         return false;
      }
      return mcInt.hasMCKernel(adapter);
   }

   /**
    * @see MCIntegration#initThreadCtx(javax.servlet.ServletContext)
    */
   public static synchronized void initThreadCtx(ServletContext ctx)
   {
      MCIntegration mcInt = getMCIntegration();
      if (mcInt == null)
      {
         return;
      }
      mcInt.initThreadCtx(ctx);
   }

   /**
    * @see MCIntegration#resetThreadCtx(javax.servlet.ServletContext)
    */
   public static synchronized void resetThreadCtx(ServletContext ctx)
   {
      MCIntegration mcInt = getMCIntegration();
      if (mcInt == null)
      {
         return;
      }
      mcInt.resetThreadCtx(ctx);
   }

   /**
    * Get a reference to actual implementation of MCIntegration if one is available.
    * Log a warning if mc integration is not available.
    * @return MCIntegration implementation
    */
   private static MCIntegration getMCIntegration()
   {
      if (mcInt == null && permFailure == false)
      {
         synchronized (MCIntegrationInvoker.class)
         {
            if (mcInt == null && permFailure == false)
            {
               Class<?> clazz = null;
               try
               {
                  clazz = loadClass("org.exoplatform.container.mc.impl.MCIntegrationImpl");
                  Method m = clazz.getMethod("getInstance");
                  mcInt = (MCIntegration) m.invoke(null);
               }
               catch (ClassNotFoundException ignored)
               {
                  permFailure = true;
                  LOG.debug("MC integration not available in this environment (missing class: "
                        + ignored.getMessage() + ")");

                  return null;
               }
               catch (Exception e)
               {
                  permFailure = true;
                  throw new RuntimeException("MC Integration initialization error", e);
               }   
            }
         }
      }
      return mcInt;
   }

   /**
    * Helper method for proper classloading fallback.
    */
   static Class loadClass(String name) throws ClassNotFoundException
   {
      Class clazz = null;
      try
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         if (cl != null)
         {
            clazz = cl.loadClass(name);
         }
      }
      catch (ClassNotFoundException ignored)
      {
         clazz = Class.forName(name);
      }
      return clazz;
   }
}
