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

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;

import java.util.Map;

/**
 * A deserializer used by JIBX that resolve system properties to allow runtime configuration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Deserializer
{
   /**
    * The name of the variable to use to get the current container name as a suffix
    * if the current container is a portal container, the value of the variable
    * will be "-${portal-container-name}", it will be an empty String otherwise 
    */
   public static final String EXO_CONTAINER_PROP_NAME = "container.name.suffix";

   /**
    * The prefix of the name of all the variables tied to the current portal container
    */
   public static final String PORTAL_CONTAINER_VARIABLE_PREFIX = "portal.container.";
   
   /**
    * Resolve a string value.
    * If the input value is null then the returned value is null.
    *
    * @param s the input value
    * @return the resolve value
    */
   public static String resolveString(String s)
   {
      return Deserializer.resolveNClean(s);
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
      s = Deserializer.resolveNClean(s);
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
      s = Deserializer.resolveNClean(s);
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
      s = Deserializer.resolveNClean(s);
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
      s = Deserializer.resolveNClean(s);
      try
      {
         return Double.parseDouble(s);
      }
      catch (NumberFormatException e)
      {
         throw new IllegalArgumentException("Cannot accept integer value " + s, e);
      }
   }

   /**
    * Resolve the variables of type ${my.var} for the current context which is composed
    * of the system properties and the portal container settings
    * @param input the input value
    * @return the resolve value
    */
   public static String resolveVariables(String input)
   {
      return resolveVariables(input, null);
   }
   
   /**
    * Resolve the variables of type ${my.var} for the current context which is composed
    * of the system properties, the portal container settings and the given settings
    * @param input the input value
    * @param props a set of parameters to add for the variable resolution
    * @return the resolve value
    */
   public static String resolveVariables(String input, Map<String, Object> props)   
   {
      final int NORMAL = 0;
      final int SEEN_DOLLAR = 1;
      final int IN_BRACKET = 2;
      if (input == null)
         return input;
      char[] chars = input.toCharArray();
      StringBuffer buffer = new StringBuffer();
      boolean properties = false;
      int state = NORMAL;
      int start = 0;
      for (int i = 0; i < chars.length; ++i)
      {
         char c = chars[i];
         if (c == '$' && state != IN_BRACKET)
            state = SEEN_DOLLAR;
         else if (c == '{' && state == SEEN_DOLLAR)
         {
            buffer.append(input.substring(start, i - 1));
            state = IN_BRACKET;
            start = i - 1;
         }
         else if (state == SEEN_DOLLAR)
            state = NORMAL;
         else if (c == '}' && state == IN_BRACKET)
         {
            if (start + 2 == i)
            {
               buffer.append("${}");
            }
            else
            {
               String value = null;
               String key = input.substring(start + 2, i);
               if (key.equals(Deserializer.EXO_CONTAINER_PROP_NAME))
               {
                  // The requested key is the name of current container
                  ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
                  if (container instanceof PortalContainer)
                  {
                     // The current container is a portal container
                     RootContainer rootContainer = (RootContainer)ExoContainerContext.getTopContainer();
                     value = rootContainer.isPortalContainerConfigAware() ? "_" + container.getContext().getName() : "";
                  }
               }
               else if (key.startsWith(Deserializer.PORTAL_CONTAINER_VARIABLE_PREFIX))
               {
                  // We try to get a value tied to the current portal container.
                  ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
                  if (container instanceof PortalContainer)
                  {
                     // The current container is a portal container
                     Object oValue =
                        ((PortalContainer)container).getSetting(key
                           .substring(Deserializer.PORTAL_CONTAINER_VARIABLE_PREFIX.length()));
                     value = oValue == null ? null : oValue.toString();
                  }
               }
               else
               {
                  if (props != null)
                  {
                     // Some parameters have been given thus we need to check inside first
                     Object oValue = props.get(key);
                     value = oValue == null ? null : oValue.toString();
                  }
                  if (value == null)
                  {
                     // No value could be found so far, thus we try to get it from the 
                     // system properties
                     value = PrivilegedSystemHelper.getProperty(key);
                  }
               }
               if (value != null)
               {
                  properties = true;
                  buffer.append(value);
               }
            }
            start = i + 1;
            state = NORMAL;
         }
      }
      if (properties == false)
         return input;
      if (start != chars.length)
         buffer.append(input.substring(start, chars.length));
      return buffer.toString();
   
   }
   
   /**
    * This methods will remove useless characters from the given {@link String} and return the result
    * @param s the input value
    * @return <code>null</code> if the input value is <code>null</code>, <code>s.trim()</code>
    * otherwise
    */
   public static String cleanString(String s)
   {
      return s == null ? null : s.trim();
   }
   
   /**
    * This method will first resolves the variables then it will clean the results
    * @param s the input value
    * @return the resolve and clean value
    */
   public static String resolveNClean(String s)
   {
      return cleanString(resolveVariables(s));
   }
}
