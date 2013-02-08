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
package org.exoplatform.container.multitenancy.bridge;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.multitenancy.CurrentTenantNotSetException;
import org.exoplatform.container.multitenancy.Tenant;
import org.exoplatform.container.multitenancy.TenantsService;
import org.exoplatform.container.multitenancy.TenantsStateListener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * TenantsService implementation based on plugins. Following kinds of plugin supported: <ul>
 * <li>
 * CurrentTenantLookup
 * </li>
 * <li>
 * TenantStateObserver
 * </li>
 * </ul>
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * 
 */
public class TenantsServiceImpl implements TenantsService {

  protected static final Log                LOG       = ExoLogger.getLogger(TenantsServiceImpl.class);

  /**
   * List of registered {@link CurrentTenantLookup} implementations.
   */
  protected final List<CurrentTenantLookup> lookups   = new ArrayList<CurrentTenantLookup>();

  /**
   * List of registered {@link TenantStateObserver} implementations.
   */
  protected final List<TenantStateObserver> observers = new ArrayList<TenantStateObserver>();

  /**
   * Constructor without dependencies.
   */
  public TenantsServiceImpl() {
  }

  public void addPlugin(ComponentPlugin plugin) {
    if (plugin instanceof CurrentTenantLookup) {
      lookups.add((CurrentTenantLookup) plugin);
      LOG.info("CurrentTenantLookup instance registered: " + plugin.toString());
    } else if (plugin instanceof TenantStateObserver) {
      observers.add((TenantStateObserver) plugin);
      LOG.info("TenantStateObserver instance registered: " + plugin.toString());
    } else {
      LOG.warn("Not supported component plugin: " + plugin.getName() + ", type " + plugin.getClass());
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void addListener(TenantsStateListener listener) {
    for (TenantStateObserver o : observers) {
      o.addListener(listener);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void removeListener(TenantsStateListener listener) {
    for (TenantStateObserver o : observers) {
      o.removeListener(listener);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tenant getCurrentTanant() throws CurrentTenantNotSetException {
    for (CurrentTenantLookup l : lookups) {
      if (l.hasCurrentTenant()) {
        return l.getCurrentTenant();
      }
    }

    throw new CurrentTenantNotSetException("Current Tenant not set.");
  }

}
