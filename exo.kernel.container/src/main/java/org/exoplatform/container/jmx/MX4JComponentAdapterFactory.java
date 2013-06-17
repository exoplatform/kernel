/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.container.jmx;

import org.exoplatform.container.ConcurrentContainer;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.spi.ComponentAdapter;
import org.exoplatform.container.spi.ComponentAdapterFactory;
import org.exoplatform.container.spi.ContainerException;

import java.io.Serializable;

public class MX4JComponentAdapterFactory implements ComponentAdapterFactory, Serializable
{

   /** . */
   private final ExoContainer holder;

   /** . */
   private final ConcurrentContainer container;

   public MX4JComponentAdapterFactory(ExoContainer holder, ConcurrentContainer container)
   {
      this.holder = holder;
      this.container = container;
   }

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 1715363032066303387L;

   public ComponentAdapter createComponentAdapter(Object key, Class<?> impl) throws ContainerException
   {
      return new MX4JComponentAdapter(holder, container, key, impl);
   }
}
