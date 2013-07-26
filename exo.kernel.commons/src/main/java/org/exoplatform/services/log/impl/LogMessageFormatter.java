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
package org.exoplatform.services.log.impl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple class to provide format parsing of log messages similar to what slf4j library does.
 * @author <a href="mailto:dkuleshov@exoplatform.com">Dmitry Kuleshov</a>
 */
public class LogMessageFormatter
{
   /**
    * '{}' - anchor which will be replaced by corresponding 
    * object's string representation
    */
   private static final Pattern REPLACE_PATTERN = Pattern.compile("\\{\\}");

   public static String getMessage(String str, Object... argsArray)
   {
      if (argsArray != null && argsArray.length > 0)
      {
         for (int i = 0; i < argsArray.length; i++)
         {
            if (i != argsArray.length - 1 || !(argsArray[i] instanceof Throwable))
            {
               String message = String.valueOf(argsArray[i]);
               str =
                  REPLACE_PATTERN.matcher(str).replaceFirst(
                     message != null ? Matcher.quoteReplacement(message) : "null");
            }
         }
      }
      return str;
   }

   public static Throwable getThrowable(Object... argsArray)
   {
      if (argsArray != null && argsArray.length > 0)
      {
         // we assume that last element in argsArray may be a Throwable
         if (argsArray[argsArray.length - 1] instanceof Throwable)
         {
            return (Throwable)argsArray[argsArray.length - 1];
         }
      }
      return null;
   }
}
