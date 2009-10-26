/*
 * Copyright (C) 2009 eXo Platform SAS.
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
package org.exoplatform.container;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.picocontainer.Startable;

/**
 * A management facade for the {@link PropertyManager} static methods. The object does implement
 * the startable interface in order to trigger pico container eager initialization otherwise
 * it would not appear in the mbean server until someone access it but as no one is accessing it
 * it would never appear.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
@Managed
@ManagedDescription("The property manager")
@NameTemplate(@Property(key = "service", value = "propertymanager"))
public class PropertyManagerManaged implements Startable
{

   @Managed
   @ManagedDescription("Returns the value of a property")
   public String getProperty(@ManagedDescription("The property name to return") String propertyName)
   {
      return PropertyManager.getProperty(propertyName);
   }

   @Managed
   @ManagedDescription("Returns true if the portal is in development mode")
   public boolean isDevelopping()
   {
      return PropertyManager.isDevelopping();
   }

   @Managed
   @ManagedDescription("Update the value of a property")
   public static void setProperty(@ManagedDescription("The property name") String propertyName,
      @ManagedDescription("The property value") String propertyValue)
   {
      PropertyManager.setProperty(propertyName, propertyValue);
   }

   @Managed
   @ManagedDescription("Returns true if the property manager cache is enabled")
   public static boolean getUseCache()
   {
      return PropertyManager.getUseCache();
   }

   @Managed
   @ManagedDescription("Refresh the property manager cache")
   public static void refresh()
   {
      PropertyManager.refresh();
   }

   public void start()
   {
      // Nothing to do
   }

   public void stop()
   {
      // Nothing to do
   }
}
