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
package org.exoplatform.container.xml;

import org.exoplatform.container.configuration.ConfigurationManagerImpl;

/**
 * A deserializer used by JIBX that resolve system properties to allow runtime configuration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Deserializer
{

   /**
    * Resolve a string value.
    * If the input value is null then the returned value is null.
    *
    * @param s the input value
    * @return the resolve value
    */
   public static String resolveString(String s)
   {
      return ConfigurationManagerImpl.resolveSystemProperties(s);
   }

   /**
    * Resolve a boolean value with the following algorithm:
    * <ol>
    * <li>Resolve any system property in the input value</li>
    * <li>If the input value is null then the returned value is null</li>
    * <li>If the value is equals to the string litteral true ignoring the case then true is returned</li>
    * <li>If the value is equals to the string litteral false ignoring the case then false is returned</li>
    * <li>Otherwise an <code>IllegalArgumentException</code> is thrown</li>
    * </ol>
    *
    * @param s the input value
    * @return the resolve value
    * @throws IllegalArgumentException if the argument is not correct
    */
   public static Boolean resolveBoolean(String s) throws IllegalArgumentException
   {
      if (s == null)
      {
         return null;
      }
      s = ConfigurationManagerImpl.resolveSystemProperties(s);
      if (s.equalsIgnoreCase("true"))
      {
         return true;
      }
      else if (s.equalsIgnoreCase("false"))
      {
         return false;
      }
      throw new IllegalArgumentException("Cannot accept boolean value " + s);
   }

   /**
    * Resolve an integer value with the following algorithm:
    * <ol>
    * <li>Resolve any system property in the input value</li>
    * <li>Attempt to parse the value using the {@link Integer#parseInt(String)} method and returns its value.</li>
    * <li>If the parsing fails then throws an <code>IllegalArgumentException</code></li>
    * </ol>
    *
    * @param s the input value
    * @return the resolve value
    * @throws IllegalArgumentException if the argument is not correct
    */
   public static Integer resolveInteger(String s) throws IllegalArgumentException
   {
      if (s == null)
      {
         return null;
      }
      s = ConfigurationManagerImpl.resolveSystemProperties(s);
      try
      {
         return Integer.parseInt(s);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("Cannot accept integer value " + s, e);
      }
   }

   /**
    * Resolve a long value with the following algorithm:
    * <ol>
    * <li>Resolve any system property in the input value</li>
    * <li>Attempt to parse the value using the {@link Long#parseLong(String)} method and returns its value.</li>
    * <li>If the parsing fails then throws an <code>IllegalArgumentException</code></li>
    * </ol>
    *
    * @param s the input value
    * @return the resolve value
    * @throws IllegalArgumentException if the argument is not correct
    */
   public static Long resolveLong(String s) throws IllegalArgumentException
   {
      if (s == null)
      {
         return null;
      }
      s = ConfigurationManagerImpl.resolveSystemProperties(s);
      try
      {
         return Long.parseLong(s);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("Cannot accept integer value " + s, e);
      }
   }

   /**
    * Resolve a double value with the following algorithm:
    * <ol>
    * <li>Resolve any system property in the input value</li>
    * <li>Attempt to parse the value using the {@link Double#parseDouble(String)} method and returns its value.</li>
    * <li>If the parsing fails then throws an <code>IllegalArgumentException</code></li>
    * </ol>
    *
    * @param s the input value
    * @return the resolve value
    * @throws IllegalArgumentException if the argument is not correct
    */
   public static Double resolveDouble(String s) throws IllegalArgumentException
   {
      if (s == null)
      {
         return null;
      }
      s = ConfigurationManagerImpl.resolveSystemProperties(s);
      try
      {
         return Double.parseDouble(s);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("Cannot accept integer value " + s, e);
      }
   }
}
