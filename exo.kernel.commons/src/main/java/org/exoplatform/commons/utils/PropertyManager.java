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
package org.exoplatform.commons.utils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A property manager that acts as a facade of the system properties. The manager has a cache that is only disabled
 * if the property exo.product.developing is set to the false string. The cache usage is read once during the static
 * initialization of the cache and it can be programmatically triggered by calling the {@link #refresh()} method.
 *
 * TODO add security privileged blocks used when eXo is executed under a security manager
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class PropertyManager
{

   /** . */
   public static final String DEVELOPING = "exo.product.developing";

   /** . */
   public static final String RUNTIME_PROFILES = "exo.profiles";

   /** . */
   public static final String PROPERTIES_URL = "exo.properties.url";

   /** . */
   private static final ConcurrentMap<String, String> cache = new ConcurrentHashMap<String, String>();

   /** This is read only once at startup. */
   private static volatile boolean useCache;

   /** . */
   private static volatile boolean developping;

   static
   {
      refresh();
   }

   /**
    * Returns a property from the provided property name. If the property value is not found it returns null.
    *
    * @param propertyName the property name
    * @return the property value
    */
   public static String getProperty(String propertyName)
   {
      if (useCache)
      {
         if (DEVELOPING.equals(propertyName))
         {
            return developping ? "true" : "false";
         }
         else
         {
            String propertyValue = cache.get(propertyName);
            if (propertyValue == null)
            {
               propertyValue = PrivilegedSystemHelper.getProperty(propertyName);
               if (propertyValue != null)
               {
                  cache.put(propertyName, propertyValue);
               }
            }
            return propertyValue;
         }
      }
      else
      {
         return PrivilegedSystemHelper.getProperty(propertyName);
      }
   }

   /**
    * Returns true if the product developing mode is enabled.
    *
    * @return the product developing mode
    */
   public static boolean isDevelopping()
   {
      if (useCache)
      {
         return developping;
      }
      else
      {
         return internalIsDevelopping();
      }
   }

   private static boolean internalIsDevelopping()
   {
      return "true".equals(PrivilegedSystemHelper.getProperty(DEVELOPING, "false"));
   }

   /**
    * Update a property in the system properties and in the cache.
    *
    * @param propertyName the property name
    * @param propertyValue the property value
    */
   public synchronized static void setProperty(String propertyName, String propertyValue)
   {
      PrivilegedSystemHelper.setProperty(propertyName, propertyValue);

      // Remove instead of put to avoid concurrent race
      cache.remove(propertyName);

      //
      if (DEVELOPING.equals(propertyName))
      {
         developping = internalIsDevelopping();
      }
   }

   /**
    * Returns true if the cache is enabled.
    *
    * @return the use cache value
    */
   public synchronized static boolean getUseCache()
   {
      return useCache;
   }

   /**
    * Refresh the property manager. The cache is cleared and the cache usage is read from the system properties.
    */
   public synchronized static void refresh()
   {
      useCache = !internalIsDevelopping();
      developping = internalIsDevelopping();
      cache.clear();
   }
}
