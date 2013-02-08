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

import java.security.PrivilegedAction;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.configuration.ConfigurationManager;

/**
 * Tests of {@link DefaultTenantsServiceImpl}.
 * 
 */
public class TestTenantsServiceImpl extends TestCase 
{

  interface InterfaceA {
    void doA();
  }
  
  interface InterfaceB {
    void doB();
  }
  
  interface InterfaceBB {
    void doBB();
  }
  
  public static abstract class BaseComponent {
  }
  
  public static class SimpleComponent extends BaseComponent implements InterfaceA 
  {
    @Override
    public void doA() 
    { 
    }
  }
  
  public static class DummyComponent implements InterfaceB, InterfaceBB 
  {
    @Override
    public void doB() 
    { 
    }
    @Override
    public void doBB() 
    { 
    }
  }

  protected ExoContainer                parent;
  
  /**
   * {@inheritDoc}
   */
  protected void setUp() throws Exception {
    super.setUp();

    // create new root each test to have portal container clear 
    final RootContainer root = new RootContainer();
    SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
    {
       public Void run()
       {
         RootContainer singleton = RootContainer.getInstance();
         root.registerComponentInstance(ConfigurationManager.class, singleton.getComponentInstance(ConfigurationManager.class));
         root.start(true);
         return null;
       }
    });
    
    parent = root.getPortalContainer(PortalContainer.DEFAULT_PORTAL_CONTAINER_NAME);
  }
  
}
