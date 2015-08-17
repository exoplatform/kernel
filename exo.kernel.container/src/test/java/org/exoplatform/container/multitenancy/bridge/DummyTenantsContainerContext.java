package org.exoplatform.container.multitenancy.bridge;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.jmx.MX4JComponentAdapter;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.spi.ComponentAdapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * {@link TenantsContainerContext} implementation for tests.
 *
 * @author <a href="mailto:pnedonosko@exoplatform.org">Peter Nedonosko</a>
 * @version $Id: DummyTenantsContainerContext.java 000000 Mar 6, 2013 4:32:48 PM pnedonosko $
 *
 */
public class DummyTenantsContainerContext implements TenantsContainerContext
{

   public Object lastGetKey;

   public Object lastGetListKey;

   public Object lastRegisteredKey;

   public Object lastUnregisteredKey;

   private Set<Object> registeredKeys = new HashSet<Object>();

   private ExoContainer parent;

   public DummyTenantsContainerContext(ExoContainer parent, InitParams config)
   {
      this.parent = parent;
   }


   public <T> List<ComponentAdapter<T>> getComponentAdaptersOfType(Class<T> componentType)
   {
      lastGetListKey = componentType;
      return null;
   }

   public <T> List<T> getComponentInstancesOfType(Class<T> componentType)
   {
      lastGetListKey = componentType;
      return null;
   }

   public <T> ComponentAdapter<T> getComponentAdapterOfType(Class<T> key)
   {
      lastGetKey = key;
      return null;
   }

   public <T> T getComponentInstance(Object componentKey)
   {
      lastGetKey = componentKey;
      return null;
   }

   public <T> T getComponentInstanceOfType(Class<T> componentType)
   {
      lastGetKey = componentType;
      return null;
   }

   public <T> boolean accept(ComponentAdapter<T> adapter)
   {
      return !(adapter instanceof org.exoplatform.container.InstanceComponentAdapter);
   }

   public boolean accept(Object key)
   {
      boolean res = registeredKeys.contains(key);
      if (res)
      {
         return true;
      }
      else if (key instanceof Class<?>)
      {
         List<Class<?>> types = getRegisteredTypes((Class<?>)key);
         return types.size() > 0;
      }

      return false;
   }

   public List<Class<?>> getRegisteredTypes(Class<?> keyType)
   {
      List<Class<?>> subclasses = new ArrayList<Class<?>>();
      for (Object k : registeredKeys)
      {
         if (k instanceof Class)
         {
            Class<?> componentType = (Class<?>)k;
            if (keyType == null || keyType.isAssignableFrom(componentType))
            {
               subclasses.add(componentType);
            }
         }
      }
      return subclasses;
   }

   public <T> ComponentAdapter<T> registerComponent(ComponentAdapter<T> component) throws TenantComponentRegistrationException
   {
      if (!TenantsContainerContext.class.equals(component.getComponentKey()))
      {
         lastRegisteredKey = component.getComponentKey();
         registeredKeys.add(component.getComponentKey());
         return component;
      }
      return new MX4JComponentAdapter(parent, null, component.getComponentKey(), component.getComponentImplementation()); // dummy stuff to return not null
   }

   public ComponentAdapter unregisterComponent(Object componentKey) throws TenantComponentRegistrationException
   {
      if (!TenantsContainerContext.class.equals(componentKey))
      {
         lastUnregisteredKey = componentKey;
         registeredKeys.remove(componentKey);
         return new MX4JComponentAdapter(parent, null, componentKey, this.getClass()); // dummy stuff to return not null
      }
      return null;
   }
}
