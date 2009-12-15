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
package org.exoplatform.container.mc.impl;

import org.exoplatform.container.mc.MCIntegration;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jboss.beans.metadata.plugins.AbstractBeanMetaData;
import org.jboss.beans.metadata.spi.AnnotationMetaData;
import org.jboss.kernel.Kernel;
import org.jboss.kernel.plugins.dependency.AbstractKernelController;
import org.jboss.mc.common.ThreadLocalUtils;
import org.jboss.mc.servlet.vdf.api.VDFThreadLocalUtils;
import org.picocontainer.ComponentAdapter;

import javax.servlet.ServletContext;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Implementation of MCIntegration that contains hard dependencies on JBoss Microcontainer.
 *
 * There is a reflection style dependency on this class in
 * {@link org.exoplatform.container.mc.MCIntegrationInvoker}.
 * 
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class MCIntegrationImpl implements MCIntegration
{
   /**
    * Logger
    */
   private static Log log = ExoLogger.getLogger(MCIntegrationImpl.class);

   /**
    * A singleton MCIntegration implementation
    */
   private static MCIntegration mcint;

   /**
    * JBoss MC kernel controller that we use
    */
   private AbstractKernelController rootController;

   /**
    * Configuration parser and holder
    */
   private MCIntConfig conf;

   /**
    * A factory method. In principle can return different implementations based on the running environment.
    * Currently only one implementation exists.
    */
   public synchronized static MCIntegration getInstance()
   {
      if (mcint == null)
      {
         mcint = new MCIntegrationImpl();
      }
      return mcint;
   }

   /**
    * Single constructor - private to force usage of factory method {@link MCIntegrationImpl#getInstance()}.
    */
   private MCIntegrationImpl()
   {
      // load conf/mc-int-config.xml
      conf = new MCIntConfig();
   }

   /**
    * Get kernel controller associated with appropriate mc kernel
    * @return kernel controller
    */
   public synchronized AbstractKernelController getRootController()
   {
      if (rootController == null)
      {
         Kernel kernel = ThreadLocalUtils.getKernel();
         if (kernel != null)
         {
            rootController = (AbstractKernelController) kernel.getController();
         }
         else
         {
            log.warn("GateIn - MC integration not available");
            return null;
         }
      }
      return rootController;
   }

   /**
    * Check if component should pass through mc kernel injection based on class annotations,
    * and mc-int-config.xml configuration.
    * Wrap with intercepting component adapter (@link MCComponentAdapter} or return original adapter.
    *
    * @see MCIntegration#getMCAdapter(org.picocontainer.ComponentAdapter)
    */
   public ComponentAdapter getMCAdapter(ComponentAdapter adapter)
   {
      InterceptMC interceptAnnotation = null;

      AbstractBeanMetaData data = conf.getByAdapter(adapter);
      if (data != null)
      {
         Set<AnnotationMetaData> annotationMetaData = data.getAnnotations();
         if (annotationMetaData != null)
         {
            for (AnnotationMetaData annMeta : annotationMetaData)
            {
               Annotation ann = annMeta.getAnnotationInstance();
               if (ann.annotationType() == InterceptMC.class)
               {
                  interceptAnnotation = (InterceptMC) ann;
               }
            }
         }
      }
      else
      {
         Class<?> clazz = adapter.getComponentImplementation();
         interceptAnnotation = clazz.getAnnotation(InterceptMC.class);
      }

      if (interceptAnnotation == null)
      {
         return adapter;
      }

      AbstractKernelController controller = getRootController();
      if (controller != null)
      {
         adapter = new MCComponentAdapter(controller, adapter, interceptAnnotation, data);
      }
      return adapter;
   }

   /**
    * @see MCIntegration#hasMCKernel(org.picocontainer.ComponentAdapter)
    */
   public boolean hasMCKernel(ComponentAdapter adapter)
   {
      return ThreadLocalUtils.getKernel() != null;
   }

   /**
    * @see MCIntegration#initThreadCtx(javax.servlet.ServletContext)
    */
   public void initThreadCtx(ServletContext ctx)
   {
      VDFThreadLocalUtils.init(ctx);
   }

   /**
    * @see MCIntegration#resetThreadCtx(javax.servlet.ServletContext)
    */
   public void resetThreadCtx(ServletContext ctx)
   {
      VDFThreadLocalUtils.reset();
   }
}
