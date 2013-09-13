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
package org.exoplatform.container.spring;

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * This is the implementation of the {@link ApplicationContextProvider} based on the
 * {@link AnnotationConfigApplicationContext} allowing to configure Spring programmatically.
 * It can be configured using a values-param, each value will be the FQN of the configuration class
 * to be registered.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class AnnotationConfigApplicationContextProvider implements ApplicationContextProvider
{

   /**
    * The name of the values parameter that will contain the fqn of the configuration
    * classes
    */
   private static final String CONFIG_CLASSES_PARAM_NAME = "config.classes";

   /**
    * The values param containing the configuration
    */
   private final ValuesParam params;

   /**
    * The default constructor
    * @param p the initial parameters
    */
   public AnnotationConfigApplicationContextProvider(InitParams p)
   {
      if (p == null || p.getValuesParam(CONFIG_CLASSES_PARAM_NAME) == null)
      {
         throw new IllegalArgumentException("The values parameter " + CONFIG_CLASSES_PARAM_NAME
            + " is mandatory, please set at least one value.");
      }
      this.params = p.getValuesParam(CONFIG_CLASSES_PARAM_NAME);
   }

   /**
    * {@inheritDoc}
    */
   public ApplicationContext getApplicationContext(ApplicationContext parent)
   {
      try
      {
         AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
         ctx.setParent(parent);
         for (String value : params.getValues())
         {
            Class<?> clazz = ClassLoading.forName(value, AnnotationConfigApplicationContextProvider.class);
            ctx.register(clazz);
         }
         ctx.refresh();
         return ctx;
      }
      catch (Exception e)
      {
         throw new RuntimeException("Could not create the ApplicationContext", e);
      }
   }
}
