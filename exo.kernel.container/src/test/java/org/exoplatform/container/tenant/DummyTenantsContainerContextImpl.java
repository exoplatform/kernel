package org.exoplatform.container.tenant;

import java.util.List;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.defaults.InstanceComponentAdapter;

public class DummyTenantsContainerContextImpl implements TenantsContainerContext {

  class TenantContainer extends ExoContainer {
    private static final long serialVersionUID = 5287379492951109958L;

    TenantContainer(PicoContainer parent) {
      super(parent, false);
    }
  }

  private ExoContainer          parent;

  private final TenantContainer defaultContainer;

  public DummyTenantsContainerContextImpl(ExoContainer parent, InitParams params) {
    this.parent = parent;
    this.defaultContainer = new TenantContainer(parent);
  }

  @Override
  public List getComponentAdaptersOfType(Class componentType) {
    return null;
  }

  @Override
  public List getComponentInstancesOfType(Class componentType) {
    return null;
  }

  @Override
  public ComponentAdapter getComponentAdapterOfType(Class key) {
    return null;
  }

  @Override
  public Object getComponentInstance(Object componentKey) {
    return defaultContainer.getComponentInstance(componentKey);
  }

  @Override
  public Object getComponentInstanceOfType(Class<?> componentType) {
    return null;
  }

  @Override
  public boolean accept(ComponentAdapter adapter) {
    return !(adapter instanceof InstanceComponentAdapter);
  }

  @Override
  public boolean accept(Object key) {
    return true;
  }

  @Override
  public void registerComponent(ComponentAdapter component) throws TenantComponentRegistrationException {
    ((MutablePicoContainer) this.defaultContainer).registerComponent(component);
  }

  @Override
  public ComponentAdapter unregisterComponent(Object componentKey) throws TenantComponentRegistrationException {
    return ((MutablePicoContainer) this.defaultContainer).unregisterComponent(componentKey);
  }

  public ExoContainer getDefaultContainer() {
    return (ExoContainer) defaultContainer;
  }
}
