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
package org.exoplatform.container.spi;

import org.exoplatform.container.ExoContainer;

import java.util.ServiceLoader;

/**
 * This interface describes an {@link Interceptor} which can be injected into the chain of {@link Interceptor}
 * Dynamic {@link Interceptor} will be created thanks to the {@link ServiceLoader} so it must provide a constructor with no
 * parameter.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface Interceptor extends Container
{
   /**
    * Sets the successor of the Interceptor in the chain of {@link Interceptor}
    * @param successor the successor
    */
   void setSuccessor(Interceptor successor);

   /**
    * Sets the holder which is mostly used when it is required to be able to go through the chain of {@link Interceptor}
    * @param holder the holder of the container
    */
   void setHolder(ExoContainer holder);

   /**
    * Sets the parent container
    * @param parent the parent container
    */
   void setParent(ExoContainer parent);

   /**
    * Gives an identifier to the Container, allowing to inject an {@link Interceptor} into the {@link Interceptor}
    * chain.
    */
   String getId();
}
