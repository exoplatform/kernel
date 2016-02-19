/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: PrivilegedSystemHelper.java 111 2010-11-11 11:11:11Z tolusha $
 *
 */
public class PrivilegedSystemHelper
{

   /**
    * Gets system property in privileged mode.
    * 
    * @param key
    * @return
    */
   public static String getProperty(final String key)
   {
      PrivilegedAction<String> action = new PrivilegedAction<String>()
      {
         public String run()
         {
            return System.getProperty(key);
         }
      };
      return SecurityHelper.doPrivilegedAction(action);
   }

   /**
    * Gets system properties in privileged mode.
    * 
    * @return
    */
   public static Properties getProperties()
   {
      PrivilegedAction<Properties> action = new PrivilegedAction<Properties>()
      {
         public Properties run()
         {
            return System.getProperties();
         }
      };
      return SecurityHelper.doPrivilegedAction(action);
   }

   /**
    * Gets system property in privileged mode.
    * 
    * @param key
    */
   public static void setProperty(final String key, final String value)
   {
      PrivilegedAction<Void> action = new PrivilegedAction<Void>()
      {
         public Void run()
         {
            System.setProperty(key, value);
            return null;
         }
      };
      SecurityHelper.doPrivilegedAction(action);
   }

   /**
    * Gets system property in privileged mode.
    * 
    * @param key
    * @param def
    * @return
    */
   public static String getProperty(final String key, final String def)
   {
      PrivilegedAction<String> action = new PrivilegedAction<String>()
      {
         public String run()
         {
            return System.getProperty(key, def);
         }
      };
      return SecurityHelper.doPrivilegedAction(action);
   }

   /**
    * Get resource in privileged mode.
    * 
    * @param name
    * @return
    */
   public static URL getResource(final String name)
   {
      PrivilegedAction<URL> action = new PrivilegedAction<URL>()
      {
         public URL run()
         {
            return Thread.currentThread().getContextClassLoader().getResource(name);
         }
      };
      return SecurityHelper.doPrivilegedAction(action);
   }

   /**
    * Get resource as stream in privileged mode.
    * 
    * @param name
    * @return
    */
   public static InputStream getResourceAsStream(final String name)
   {
      PrivilegedAction<InputStream> action = new PrivilegedAction<InputStream>()
      {
         public InputStream run()
         {
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(name);
         }
      };
      return SecurityHelper.doPrivilegedAction(action);
   }

}
