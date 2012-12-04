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
import org.exoplatform.container.multitenancy.DefaultTenantsServiceImpl;
import org.exoplatform.container.multitenancy.Multitenant;
import org.exoplatform.container.multitenancy.TenantsService;

/**
 * Tests of {@link DefaultTenantsServiceImpl}.
 * 
 */
public class TestDefaultTenantsServiceImpl extends TestCase 
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

  public static class ObjectTester 
  {

    final Multitenant<SimpleComponent> simple;

    final Multitenant<DummyComponent>  dummy;

    public ObjectTester(SimpleComponent simple, DummyComponent dummy, TenantsService tenantsService) 
    {
      this.simple = tenantsService.asMultitenant(simple);
      this.dummy = tenantsService.asMultitenant(dummy);
    }
  }

  public static class ClassTester 
  {

    final Multitenant<SimpleComponent> simple;

    final Multitenant<DummyComponent>  dummy;

    public ClassTester(SimpleComponent simple, DummyComponent dummy, TenantsService tenantsService) 
    {
      this.simple = tenantsService.asMultitenant(SimpleComponent.class);
      this.dummy = tenantsService.asMultitenant(DummyComponent.class);
    }
  }
  
  public static class InterfaceTester 
  {

    final Multitenant<InterfaceA> a;

    final Multitenant<InterfaceB> b;

    public InterfaceTester(InterfaceA a, InterfaceB b, TenantsService tenantsService) 
    {
      this.a = tenantsService.asMultitenant(a);
      this.b = tenantsService.asMultitenant(b);
    }
  }
  
  public static class MixedTester 
  {

    final Multitenant<BaseComponent> base;

    final Multitenant<InterfaceBB> bb;

    public MixedTester(BaseComponent base, InterfaceBB bb, TenantsService tenantsService) 
    {
      this.base = tenantsService.asMultitenant(base);
      this.bb = tenantsService.asMultitenant(bb);
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

  public void testMultitenantObject() {
    parent.registerComponentImplementation(SimpleComponent.class, SimpleComponent.class);
    parent.registerComponentImplementation(DummyComponent.class, DummyComponent.class);
    
    parent.registerComponentImplementation(ObjectTester.class, ObjectTester.class);

    Object obj = parent.getComponentInstanceOfType(ObjectTester.class);

    assertTrue(obj instanceof ObjectTester);
    ObjectTester service = (ObjectTester) obj;
    
    assertNotNull(service.simple.get());
    assertTrue(service.simple.get() instanceof SimpleComponent);
    assertSame(parent.getComponentInstanceOfType(SimpleComponent.class), service.simple.get());
    
    assertNotNull(service.dummy.get());
    assertTrue(service.dummy.get() instanceof DummyComponent);
    assertSame(parent.getComponentInstanceOfType(DummyComponent.class), service.dummy.get());
  }

  public void testMultitenantClass() {
    parent.registerComponentImplementation(SimpleComponent.class, SimpleComponent.class);
    parent.registerComponentImplementation(DummyComponent.class, DummyComponent.class);
    
    parent.registerComponentImplementation(ClassTester.class, ClassTester.class);

    Object obj = parent.getComponentInstanceOfType(ClassTester.class);

    assertTrue(obj instanceof ClassTester);
    ClassTester service = (ClassTester) obj;
    
    assertNotNull(service.simple.get());
    assertTrue(service.simple.get() instanceof SimpleComponent);
    assertSame(parent.getComponentInstanceOfType(SimpleComponent.class), service.simple.get());
    
    assertNotNull(service.dummy.get());
    assertTrue(service.dummy.get() instanceof DummyComponent);
    assertSame(parent.getComponentInstanceOfType(DummyComponent.class), service.dummy.get());
  }
  
  public void testMultitenantInterface() {
    parent.registerComponentImplementation (InterfaceA.class, SimpleComponent.class);
    parent.registerComponentImplementation(InterfaceB.class, DummyComponent.class);
    
    parent.registerComponentImplementation(InterfaceTester.class, InterfaceTester.class);

    Object obj = parent.getComponentInstanceOfType(InterfaceTester.class);

    assertTrue(obj instanceof InterfaceTester);
    InterfaceTester service = (InterfaceTester) obj;
    
    assertNotNull(service.a.get());
    assertTrue(service.a.get() instanceof InterfaceA);
    assertSame(parent.getComponentInstanceOfType(InterfaceA.class), service.a.get());
    
    assertNotNull(service.b.get());
    assertTrue(service.b.get() instanceof InterfaceB);
    assertSame(parent.getComponentInstanceOfType(InterfaceB.class), service.b.get());
  }
  
  public void testMultitenantImplementation() {
    parent.registerComponentImplementation(SimpleComponent.class);
    parent.registerComponentImplementation(DummyComponent.class);
    
    parent.registerComponentImplementation(InterfaceTester.class, InterfaceTester.class);

    Object obj = parent.getComponentInstanceOfType(InterfaceTester.class);

    assertTrue(obj instanceof InterfaceTester);
    InterfaceTester service = (InterfaceTester) obj;
    
    assertNotNull(service.a.get());
    assertTrue(service.a.get() instanceof InterfaceA);
    assertSame(parent.getComponentInstanceOfType(InterfaceA.class), service.a.get());
    
    assertNotNull(service.b.get());
    assertTrue(service.b.get() instanceof InterfaceB);
    assertSame(parent.getComponentInstanceOfType(InterfaceB.class), service.b.get());
  }
  
  public void testMultitenantInstance() {
    parent.registerComponentInstance(new SimpleComponent());
    parent.registerComponentInstance(new DummyComponent());
    
    parent.registerComponentImplementation(InterfaceTester.class, InterfaceTester.class);

    Object obj = parent.getComponentInstanceOfType(InterfaceTester.class);

    assertTrue(obj instanceof InterfaceTester);
    InterfaceTester service = (InterfaceTester) obj;
    
    assertNotNull(service.a.get());
    assertTrue(service.a.get() instanceof InterfaceA);
    assertSame(parent.getComponentInstanceOfType(InterfaceA.class), service.a.get());
    
    assertNotNull(service.b.get());
    assertTrue(service.b.get() instanceof InterfaceB);
    assertSame(parent.getComponentInstanceOfType(InterfaceB.class), service.b.get());
  }

  public void testMultitenantMixed() {
    parent.registerComponentImplementation(SimpleComponent.class);
    parent.registerComponentImplementation(DummyComponent.class);
    
    parent.registerComponentImplementation(MixedTester.class, MixedTester.class);

    Object obj = parent.getComponentInstanceOfType(MixedTester.class);

    assertTrue(obj instanceof MixedTester);
    MixedTester service = (MixedTester) obj;
    
    assertNotNull(service.base.get());
    assertTrue(service.base.get() instanceof BaseComponent);
    assertSame(parent.getComponentInstanceOfType(BaseComponent.class), service.base.get());
    
    assertNotNull(service.bb.get());
    assertTrue(service.bb.get() instanceof InterfaceBB);
    assertSame(parent.getComponentInstanceOfType(InterfaceBB.class), service.bb.get());
  }
  
}
