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
package org.exoplatform.container.weld;

import org.jboss.weld.environment.se.Weld;

import java.util.List;

import javax.enterprise.inject.spi.Extension;

/**
 * The main purpose of this class is to provide all the {@link Extension} to the {@link WeldContainer}
 * and to limit the scope of the {@link WeldContainer} in order to make sure that {@link Weld} won't
 * manage all the components.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface WeldContainerHelper
{
   /**
    * Gives the list of all the {@link Extension} to be added to {@link Weld}
    */
   List<Extension> getExtensions();

   /**
    * Indicates whether or not a given class must be managed by {@link Weld}
    * @param clazz the class of the component
    * @return <code>true</code> if the component is part of the scope of {@link Weld},
    * <code>false</code> otherwise.
    */
   boolean isIncluded(Class<?> clazz);
}
