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
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.StandaloneContainer;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation can be used to provide a default definition of the component when we try to access it by name thanks
 * to {@link javax.inject.Named}
 *
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface DefinitionByName
{
   /**
    * The key to use for the auto registration of a component that we would like
    * to be able to retrieve thanks to {@link javax.inject.Named}.
    */
   String named() default "";

   /**
    * The default implementation to use in case the annotation has not been added on a concrete class.
    */
   Class<?> type() default void.class;

   /**
    * The list of target {@link ExoContainer} on which we allow the component to be automatically registered.
    * By default, we allow the kernel to register the component on the {@link PortalContainer} and
    * {@link StandaloneContainer} for respectively the portal and standalone modes.
    */
   Class<? extends ExoContainer>[] target() default {PortalContainer.class, StandaloneContainer.class};
}
