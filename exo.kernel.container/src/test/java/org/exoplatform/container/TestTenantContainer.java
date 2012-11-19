package org.exoplatform.container;


import org.exoplatform.container.jmx.AbstractTestContainer;
import org.exoplatform.container.tenant.TenantContextTestImpl;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVisitor;
import org.picocontainer.Startable;

import java.util.List;

public class TestTenantContainer extends AbstractTestContainer
{
  public void testTenantContextCreated()
  {
    RootContainer root = createRootContainer("test-tenant-container.xml");
    assertNotNull(root.tenantContainerContext);
    root = createRootContainer("empty-config.xml");
    assertNull(root.tenantContainerContext);
  }

  public void testRegisterComponent()
  {
    final RootContainer root = createRootContainer("test-tenant-container.xml");
    root.registerComponent(new DummyAdapter());
    ExoContainer defaultContainer = ((TenantContextTestImpl)root.tenantContainerContext).getDefaultContainer();
    assertTrue(defaultContainer.hasComponentInstanceOfType(DummyAdapter.class));
  }

  public void testRegisterComponentInstance()
  {
    final RootContainer root = createRootContainer("test-tenant-container.xml");
    ComponentAdapter adapter = root.registerComponentInstance(new C2());
    assertNotNull(adapter);
    ExoContainer defaultContainer = ((TenantContextTestImpl)root.tenantContainerContext).getDefaultContainer();
    assertFalse(defaultContainer.hasComponentInstanceOfType(C2.class)); //Must not be registered in TenantContainer
    assertTrue(root.hasComponentInstanceOfType(C2.class));
  }
  
  public void testGetComponentsFromDefaultContainer()
  {
    final RootContainer root = createRootContainer("test-tenant-container.xml");
    root.registerComponentImplementation(C1.class, C1.class);
    ExoContainer defaultContainer = ((TenantContextTestImpl)root.tenantContainerContext).getDefaultContainer();
    defaultContainer.registerComponentImplementation(C2.class, C2.class);
    List<? extends ComponentAdapter> adapters = null;
    try
    {
      adapters = defaultContainer.getComponentAdaptersOfType(Startable.class);
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
    assertEquals(2, adapters.size());
    assertTrue(adapters.get(0).getComponentKey().equals(C1.class));
    assertTrue(adapters.get(1).getComponentKey().equals(C2.class));
  }




  private class DummyAdapter implements ComponentAdapter
  {

    public void verify(PicoContainer arg0) throws PicoIntrospectionException
    {
    }

    public Object getComponentKey()
    {
      return "testKey";
    }

    public Object getComponentInstance(PicoContainer arg0) throws PicoInitializationException,
      PicoIntrospectionException
    {
      // Used to check a situation when RunTimeException occurs while retrieving an instance.
      // This reproduces usecase from JCR-1565
      throw new RuntimeException();
    }

    public Class getComponentImplementation()
    {
      return this.getClass();
    }

    public void accept(PicoVisitor arg0)
    {
    }
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
