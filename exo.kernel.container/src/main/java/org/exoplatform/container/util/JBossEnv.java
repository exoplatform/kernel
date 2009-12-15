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
package org.exoplatform.container.util;

import javax.servlet.ServletContext;
import org.exoplatform.container.mc.MCIntegrationInvoker;

/**
 * This class makes thread context initialization when GateIn runs inside JBossAS or MC.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class JBossEnv
{
   /**
    * A servlet context attribute to check for mc kernel.
    * The value is equal to org.jboss.kernel.plugins.bootstrap.basic.KernelConstants.KERNEL_NAME.
    */
   private static final String MC_KERNEL_NAME = "jboss.kernel:service=Kernel";

   /**
    * Check if MC Kernel is available.
    *
    * @param ctx ServletContext
    * @return true if we're running within JBoss MC environment
    */
   public static boolean isAvailable(ServletContext ctx)
   {
      return ctx.getAttribute(MC_KERNEL_NAME) != null;
   }

   /**
    * Perform JBoss MC environment specific thread context initialization.
    *
    * @param ctx ServletContext
    */
   public static void initThreadEnv(ServletContext ctx)
   {
      MCIntegrationInvoker.initThreadCtx(ctx);
   }

   /**
    * Perform thread context cleanup.
    *
    * @param ctx ServletContext
    */
   public static void cleanupThreadEnv(ServletContext ctx)
   {
      MCIntegrationInvoker.resetThreadCtx(ctx);
   }
}