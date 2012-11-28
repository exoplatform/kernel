package org.exoplatform.container;


import java.util.List;

import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapter;
import org.exoplatform.container.management.ManageableComponentAdapter;
import org.exoplatform.container.tenant.DummyTenantsContainerContext;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.Startable;

public class TestTenantsContainer extends AbstractTestContainer
{
  
  protected Object getLastRegisteredKey(ExoContainer container) {
    return ((DummyTenantsContainerContext)container.tenantsContainerContext).lastRegisteredKey;
  }
  
  protected Object getLastUnregisteredKey(ExoContainer container) {
    return ((DummyTenantsContainerContext)container.tenantsContainerContext).lastUnregisteredKey;
  }
  
  protected Object getLastGetKey(ExoContainer container) {
    return ((DummyTenantsContainerContext)container.tenantsContainerContext).lastGetKey;
  }
  
  protected Object getLastGetListKey(ExoContainer container) {
    return ((DummyTenantsContainerContext)container.tenantsContainerContext).lastGetListKey;
  }
  
  protected List<Class<?>> getRegisteredTypes(ExoContainer container, Class<?> type) {
    return ((DummyTenantsContainerContext)container.tenantsContainerContext).getRegisteredTypes(type);
  }
  
  /**
   * Ensure that context only created if its configuration exists.
   */
  public void testTenantContextConfigured()
  {
    RootContainer root = createRootContainer("test-tenant-container.xml");
    assertNotNull(root.tenantsContainerContext);
  }
  
  /**
   * Ensure that context not created if its configuration not exists.
   */
  public void testTenantContextNotConfigured()
  {
    RootContainer root = createRootContainer("empty-config.xml");
    assertNull(root.tenantsContainerContext);
  }

  /**
   * Check that registration goes through the tenant context 
   * if this context exists and accept given component. 
   */
  public void testRegisterComponent()
  {
    final RootContainer root = createRootContainer("test-tenant-container.xml");
    
    ManageableComponentAdapter adapter = new ManageableComponentAdapter(root,
                                                                        new MX4JComponentAdapter(C1.class,
                                                                                                 C1.class));
    
    root.registerComponent(adapter);
    assertEquals(C1.class, getLastRegisteredKey(root));
  }

  /**
   * Ensure that component instances (singletons by creation) 
   * are not registered in the tenant context, but are in the parent container. 
   */
  public void testNotRegisterComponentInstance()
  {
    final RootContainer root = createRootContainer("test-tenant-container.xml");
    ComponentAdapter adapter = root.registerComponentInstance(new C2());
    assertNotNull(adapter);
    assertNull(getLastRegisteredKey(root)); //Must not be registered in TenantsContainer
    assertNotNull(root.getComponentInstanceOfType(C2.class));
  }
  
  public void testGetComponents()
  {
    final RootContainer root = createRootContainer("test-tenant-container.xml");
    root.registerComponentImplementation(C1.class, C1.class);
    
    assertEquals(C1.class, getLastRegisteredKey(root));
    
    root.registerComponentImplementation(C2.class, C2.class);
    assertEquals(C2.class, getLastRegisteredKey(root));
    
    try
    {
      root.getComponentAdaptersOfType(Startable.class);
    }
    catch (Throwable e)
    {
      e.printStackTrace();
      fail(e.getMessage());
    }
    
    assertEquals(Startable.class, getLastGetListKey(root));
    
    List<Class<?>> startable = getRegisteredTypes(root, Startable.class);
    
    assertEquals(2, startable.size());
    assertTrue(startable.contains(C1.class));
    assertTrue(startable.contains(C2.class));
  }

  public static class C1 implements Startable
  {
    public boolean started;

    /**
     * @see org.picocontainer.Startable#start()
     */
    public void start()
    {
      started = true;
    }

    /**
     * @see org.picocontainer.Startable#stop()
     */
    public void stop()
    {
    }
  }
  
  public static class C2 implements Startable
  {
    public boolean started;

    /**
     * @see org.picocontainer.Startable#start()
     */
    public void start()
    {
      started = true;
    }

    /**
     * @see org.picocontainer.Startable#stop()
     */
    public void stop()
    {
    }
  }

}
