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
package org.picocontainer;

/**
 * An interface which is implemented by components that need to dispose of resources during the shutdown of that
 * component. The {@link Disposable#dispose()} must be called once during shutdown, directly after {@link
 * Startable#stop()} (if the component implements the {@link Startable} interface).
 * @version $Revision: 1.7 $
 * @see org.picocontainer.Startable the Startable interface if you need to <code>start()</code> and
 *      <code>stop()</code> semantics.
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface Disposable
{
   /**
    * Dispose this component. The component should deallocate all resources. The contract for this method defines a
    * single call at the end of this component's life.
    */
   void dispose();
}