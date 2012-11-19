package org.exoplatform.container.tenant;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import java.util.List;

public class TenantContextTestImpl implements  TenantContainerContext {

  public ExoContainer container;
  private final PicoContainer defaultContainer;

  public TenantContextTestImpl(ExoContainer container, InitParams params)
  {
    this.container = container;
    this.defaultContainer =  new ExoContainer(container);
  }

  @Override
  public List getComponentAdaptersOfType(Class componentType)
  {
    return null;
  }

  @Override
  public List getComponentInstancesOfType(Class componentType)
  {
    return null;
  }

  @Override
  public ComponentAdapter getComponentAdapterOfType(Class key)
  {
    return null;
  }

  @Override
  public Object getComponentInstance(Object componentKey)
  {
    return defaultContainer.getComponentInstance(componentKey);
  }

  @Override
  public Object getComponentInstanceOfType(Class<?> componentType)
  {
    return null;
  }

  @Override
  public boolean accept(ComponentAdapter adapter)
  {
    return true;
  }

  @Override
  public void registerComponent(ComponentAdapter component)
  {
    ((MutablePicoContainer)this.defaultContainer).registerComponent(component);
  }


  public ExoContainer getDefaultContainer(){
    return (ExoContainer)defaultContainer;
  }
}