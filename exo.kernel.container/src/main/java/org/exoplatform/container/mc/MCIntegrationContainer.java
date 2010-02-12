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
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.ComponentAdapterFactory;
import org.picocontainer.defaults.DefaultPicoContainer;

/**
 * Container class that serves as an interception point for MC integration.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class MCIntegrationContainer extends DefaultPicoContainer
{
   /**
    * Logger
    */
   private static Log log = ExoLogger.getLogger(MCIntegrationContainer.class);

   /**
    * Constructor that exposes super constructor.
    *
    * @param componentAdapterFactory
    * @param parent
    */
   public MCIntegrationContainer(ComponentAdapterFactory componentAdapterFactory, PicoContainer parent)
   {
      super(componentAdapterFactory, parent);
   }

   /**
    * Constructor that exposes super constructor.
    *
    * @param parent
    */
   public MCIntegrationContainer(PicoContainer parent)
   {
      super(parent);
   }

   /**
    * Constructor that exposes super constructor.
    *
    * @param componentAdapterFactory
    */
   public MCIntegrationContainer(ComponentAdapterFactory componentAdapterFactory)
   {
      super(componentAdapterFactory);
   }

   /**
    * Constructor that exposes super constructor.
    */
   public MCIntegrationContainer()
   {
   }

   /**
    * Method interception that swaps the original component adapter for intercepting one
    * for components that require mc integration.
    * If mc integration isn't available in the current runtime environment, then interception
    * simply delegates to parent without interfering.
    *
    * @param componentAdapter original component adapter
    * @return the original component adapter, or the intercepting component adapter
    * that takes care of mc integration
    */
   public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
   {
      ComponentAdapter adapter = componentAdapter;
      if (hasMCKernel(componentAdapter))
      {
         try
         {
            adapter = MCIntegrationInvoker.getMCAdapter(componentAdapter);
         }
         catch(Exception ignored)
         {
            log.debug("MC integration failed - maybe not supported in this environment (component: "
                  + componentAdapter.getComponentKey() + ")", ignored);
         }
      }

      super.registerComponent(adapter);
      return adapter;
   }

   /**
    * Check if runtime environment supports mc integration.
    *
    * @param componentAdapter original component adapter
    * @return true if mc integration is supported
    */
   public static boolean hasMCKernel(ComponentAdapter componentAdapter)
   {
      try
      {
         return MCIntegrationInvoker.hasMCKernel(componentAdapter);
      }
      catch (Exception ignored)
      {
         log.warn("MC integration failed - maybe not supported in this environment (component: "
               + componentAdapter.getComponentKey() + ")", ignored);
      }
      return false;
   }
}
