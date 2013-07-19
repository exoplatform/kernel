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
package org.exoplatform.container.context;

import java.lang.annotation.Annotation;

import javax.enterprise.context.spi.Context;

/**
 * The entry point to a {@link Context}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface ContextManager
{
   /**
    * Gives the context corresponding to the given scope
    * @param scope the annotation class corresponding to the scope
    * @return the {@link Context} corresponding to the given scope
    */
   <K> AdvancedContext<K> getContext(Class<? extends Annotation> scope);

   /**
    * Indicates whether or not a context has a been registered for the given scope 
    * @param scope the scope of the context
    * @return <code>true</code> if it exists a context for the given scope, <code>false</code> otherwise
    */
   boolean hasContext(Class<? extends Annotation> scope);

   /**
    * Registers a context
    * @param ctx the {@link Context} to register
    */
   <K> void addContext(AdvancedContext<K> ctx);
}
