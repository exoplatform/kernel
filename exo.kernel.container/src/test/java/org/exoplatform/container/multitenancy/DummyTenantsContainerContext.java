package org.exoplatform.container.multitenancy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapter;
import org.exoplatform.container.multitenancy.TenantComponentRegistrationException;
import org.exoplatform.container.multitenancy.TenantsContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.defaults.InstanceComponentAdapter;

public class DummyTenantsContainerContext implements TenantsContainerContext 
{

  public Object lastGetKey;
  
  public Object lastGetListKey;
  
  public Object lastRegisteredKey;
  
  public Object lastUnregisteredKey;
  
  private Set<Object> registeredKeys = new HashSet<Object>();

  public DummyTenantsContainerContext(ExoContainer parent, InitParams config) 
  {
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
  public ComponentAdapter registerComponent(ComponentAdapter component) throws TenantComponentRegistrationException 
  {
    if (!TenantsContainerContext.class.equals(component.getComponentKey())) {
      lastRegisteredKey = component.getComponentKey();
      registeredKeys.add(component.getComponentKey());
      return component;
    }
    return new MX4JComponentAdapter(component.getComponentKey(), component.getComponentImplementation()); // dummy stuff to return not null
  }

  @Override
  public ComponentAdapter unregisterComponent(Object componentKey) throws TenantComponentRegistrationException 
  {
    if (!TenantsContainerContext.class.equals(componentKey)) {
      lastUnregisteredKey = componentKey;
      registeredKeys.remove(componentKey);
      return new MX4JComponentAdapter(componentKey, this.getClass()); // dummy stuff to return not null
    }
    return null;
  }
}
