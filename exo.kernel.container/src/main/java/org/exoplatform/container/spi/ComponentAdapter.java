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


/**
 * A component adapter is responsible for providing a specific component instance. An instance of an implementation of
 * this interface is used inside a {@link Container} for every registered component or instance.  Each
 * <code>ComponentAdapter</code> instance has to have a key which is unique within that container. The key itself is
 * either a class type (normally an interface) or an identifier.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface ComponentAdapter
{
   /**
    * Retrieve the key associated with the component.
    * 
    * @return the component's key. Should either be a class type (normally an interface) or an identifier that is
    *         unique (within the scope of the current Container).
    */
   Object getComponentKey();

   /**
    * Retrieve the class of the component.
    * 
    * @return the component's implementation class. Should normally be a concrete class (ie, a class that can be
    *         instantiated).
    */
   Class<?> getComponentImplementation();

   /**
    * Retrieve the component instance. This method will usually create a new instance each time it is called, but that
    * is not required.
    * 
    * @return the component instance.
    * @throws ContainerException if the component could not be instantiated. Or if the component has dependencies which could not be resolved, or
    *                   instantiation of the component lead to an ambiguous situation within the container.
    */
   Object getComponentInstance() throws ContainerException;
}
