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
package org.exoplatform.kernel.demos.mc;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * A simple POJO that we use for injection.
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class InjectedBean
{
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.mc-int-demo.InjectedBean");

   public static final String SOME_PROPERTY_VALUE = "[This is some property value]";

   private String anotherProperty;

   public InjectedBean()
   {
      LOG.info("InjectedBean instantiated :: " + this);
   }

   public String getSomeString()
   {
      return SOME_PROPERTY_VALUE;
   }

   public void start()
   {
      LOG.info("Method start() called on InjectedBean");
   }

   public String getAnotherProperty()
   {
      return anotherProperty;
   }

   public void setAnotherProperty(String anotherProperty)
   {
      this.anotherProperty = anotherProperty;
   }
}
