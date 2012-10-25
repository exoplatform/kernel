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
package org.exoplatform.container.tenant;

import org.exoplatform.container.ExoContainer;
import org.picocontainer.ComponentAdapter;

public class TenantsContainerControllerImpl implements TenantsContainerController {

  private final ExoContainer container;

  public TenantsContainerControllerImpl(ExoContainer container) {
    this.container = container;
    System.out.println("Executed TenantsContainerControllerImpl.TenantsContainerControllerImpl(ExoContainer container)");
  }

  @Override
  public void createTenantContainer(String tenant) {
    System.out.println("Executed TenantsContainerControllerImpl.createTenantContainer(String tenant)");
  }

  @Override
  public void removeTenantContainer(String tenant) {
    System.out.println("Executed TenantsContainerControllerImpl.removeTenantContainer(String tenant)");
  }

  @Override
  public ComponentAdapter getComponentOfType(Object key) {
    System.out.println("Executed TenantsContainerControllerImpl.getComponentOfType(Object key)");
    return null;
  }

  @Override
  public void registerComponent(ComponentAdapter component) {
    System.out.println("Executed TenantsContainerControllerImpl.registerComponent(ComponentAdapter component)");
  }

}
