package org.exoplatform.container.tenant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.defaults.InstanceComponentAdapter;

public class DummyTenantsContainerContext implements TenantsContainerContext 
{

//  class TenantContainer extends ExoContainer {
//    private static final long serialVersionUID = 5287379492951109958L;
//
//    TenantContainer(PicoContainer parent) {
//      super(parent, false);
//    }
//  }

  //final ExoContainer          parent;

  // final TenantContainer container;
  
  public Object lastGetKey;
  
  public Object lastGetListKey;
  
  public Object lastRegisteredKey;
  
  public Object lastUnregisteredKey;
  
  private Set<Object> registeredKeys = new HashSet<Object>();

  public DummyTenantsContainerContext(ExoContainer parent, InitParams config) 
  {
    //this.container = new TenantContainer(parent);
  }

  @Override
  public List getComponentAdaptersOfType(Class componentType) 
  {
    lastGetListKey = componentType;
    return null;
  }

  @Override
  public List getComponentInstancesOfType(Class componentType) 
  {
    lastGetListKey = componentType;
    return null;
  }

  @Override
  public ComponentAdapter getComponentAdapterOfType(Class key) 
  {
    lastGetKey = key;
    return null;
  }

  @Override
  public Object getComponentInstance(Object componentKey) 
  {
    lastGetKey = componentKey;
    //return container.getComponentInstance(componentKey);
    return null;
  }

  @Override
  public Object getComponentInstanceOfType(Class<?> componentType) 
  {
    lastGetKey = componentType;
    return null;
  }

  @Override
  public boolean accept(ComponentAdapter adapter) 
  {
    return !(adapter instanceof InstanceComponentAdapter);
  }

  @Override
  public boolean accept(Object key) 
  {
    boolean res = registeredKeys.contains(key);
    if (res) 
    {
      return true;
    } 
    else if (key instanceof Class<?>)
    {
      List<Class<?>> types = getRegisteredTypes((Class<?>) key);
      return types.size() > 0;
    }
    
    return false;
  }
  
  public List<Class<?>> getRegisteredTypes(Class<?> keyType) {
    List<Class<?>> subclasses = new ArrayList<Class<?>>();
    for (Object k : registeredKeys) 
    {
      if (k instanceof Class ) {
        Class<?> componentType = (Class<?>) k; 
        if (keyType == null || keyType.isAssignableFrom(componentType))
        {
          subclasses.add(componentType);
        }
      }
    }
    return subclasses;
  }

  @Override
  public void registerComponent(ComponentAdapter component) throws TenantComponentRegistrationException 
  {
    if (!TenantsContainerContext.class.equals(component.getComponentKey())) {
      lastRegisteredKey = component.getComponentKey();
      registeredKeys.add(component.getComponentKey());
    }
    //container.registerComponent(component);
  }

  @Override
  public ComponentAdapter unregisterComponent(Object componentKey) throws TenantComponentRegistrationException 
  {
    if (!TenantsContainerContext.class.equals(componentKey)) {
      lastUnregisteredKey = componentKey;
      registeredKeys.remove(componentKey);
    }
    return null;
  }

//  public ExoContainer getTenantContainer() {
//    return container;
//  }

}
