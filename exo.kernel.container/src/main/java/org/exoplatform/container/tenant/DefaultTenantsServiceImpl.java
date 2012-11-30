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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.ComponentAdapter;

/**
 * Default implementation of {@link TenantsService}. <br>
 * It provides ready to use method-helpers <code>asMultitenant()</code> and relies on
 * {@link CurrentTenantLookup} for discovery of Current Tenant.
 * 
 */
public class DefaultTenantsServiceImpl implements TenantsService 
{

  class MultitenantComponent<T> implements Multitenant<T> 
  {

    protected final Class<T>      compType;

    protected final Set<Class<?>> compTypes;

    @SuppressWarnings("unchecked")
    MultitenantComponent(T comp) {
      this.compType = (Class<T>) comp.getClass();

      this.compTypes = new LinkedHashSet<Class<?>>();
      
      // TODO do we need this?
      // alternative keys to get the component from the container
      // fill with actual keys in adapters
      for (Object ao : container().getComponentAdaptersOfType(compType)) 
      {
        ComponentAdapter adapter = (ComponentAdapter) ao;
        Object ckey = adapter.getComponentKey();
        if (ckey instanceof Class) 
        {
          this.compTypes.add((Class<?>) ckey);
        }
      }
      
      // fill with actual impl interfaces (less possible we'll need it, but if later this component 
      // will be registered by one of its interfaces it will be still reachable) 
      for (Class<?> itype : comp.getClass().getInterfaces()) 
      { 
        this.compTypes.add(itype);
      }
    }

    MultitenantComponent(Class<T> type) 
    {
      this.compType = type;
      this.compTypes = null;
    }

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public T get() 
    {
      ExoContainer container = container();
      Object c = container.getComponentInstanceOfType(compType);
      if (c == null) 
      {
        if (compTypes != null) 
        {
          // try other keys
          for (Iterator<Class<?>> ctiter = compTypes.iterator(); ctiter.hasNext();) 
          {
            c = container.getComponentInstanceOfType(ctiter.next());
            if (c != null) 
            {
              return (T) c;
            }
          }
        }
        return null;
      }
      else 
      {
        return compType.cast(c);
      }
    }
  }

  private static final Log            LOG               = ExoLogger.getLogger(DefaultTenantsServiceImpl.class);

  protected final CurrentTenantLookup currentTenantLookup;

  protected ThreadLocal<String>       currentTenantName = new ThreadLocal<String>();

  public DefaultTenantsServiceImpl(CurrentTenantLookup currentTenantLookup) 
  {
    this.currentTenantLookup = currentTenantLookup;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getCurrentTanantName() throws CurrentTenantNotSetException 
  {
    String current = currentTenantName.get();
    if (current == null) 
    {
      currentTenantName.set(current = currentTenantLookup.getCurrentTenant().getName());
    }

    return current;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Multitenant<T> asMultitenant(T componnet) 
  {
    return new MultitenantComponent<T>(componnet);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public <T> Multitenant<T> asMultitenant(Class<T> componentType) 
  {
    return new MultitenantComponent<T>(componentType);
  }

  // ****** internals ******

  /**
   * Find most suitable container. Never return null.
   * 
   * @return {@link ExoContainer} instance
   */
  protected static ExoContainer container() 
  {
    PortalContainer portalContainer = PortalContainer.getInstanceIfPresent();
    if (portalContainer != null) 
    {
      return portalContainer;
    }
    else 
    {
      return ExoContainerContext.getCurrentContainer();
    }
  }

}
