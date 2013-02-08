/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.container.multitenancy;

/**
 * Listener for Tenant state events in Multitenancy core.<br>
 * This listener can be explicitly added to (and later removed from) {@link TenantsService} or its
 * implementation can be registered in eXo container. In last case to remove the listener unregister
 * it from the container.
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * 
 */
public interface TenantsStateListener {

  /**
   * Action on tenant stop (removal or suspend).
   * 
   * @param tenant {@link Tenant}
   */
  void tenantStopped(Tenant tenant);

  /**
   * Action on tenant start (creation or resuming).
   * 
   * @param tenant {@link Tenant}
   */
  void tenantStarted(Tenant tenant);

}
