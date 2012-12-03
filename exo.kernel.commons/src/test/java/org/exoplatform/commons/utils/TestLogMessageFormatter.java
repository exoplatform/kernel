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

import junit.framework.TestCase;

import org.exoplatform.services.log.impl.LogMessageFormatter;

/**
 * @author <a href="mailto:dkuleshov@exoplatform.com">Dmitry Kuleshov</a>
 * @version $Revision$
 */
public class TestLogMessageFormatter extends TestCase
{
   public void testMessageParsing() throws Exception
   {
      assertEquals("Sometimes even the wisest of man or machine can make an error.",
         LogMessageFormatter.getMessage("Sometimes even the wisest of man or machine can make an {}.", "error"));

      assertEquals("Time makes all things possible. I can wait.",
         LogMessageFormatter.getMessage("Time makes all {} possible. I can wait{}", "things", "."));

      assertEquals("Give it up, Megatron!", LogMessageFormatter.getMessage("Give it up, Megatron!", new Throwable()));

      assertEquals("Just remember, there's a thin line between being a hero and being a memory.",
         LogMessageFormatter.getMessage("Just remember, there's a thin line between being a hero and being a {}.",
            "memory", new Throwable()));

      assertEquals("We can't stand by and watch the destruction of this beautiful planet.",
         LogMessageFormatter.getMessage("{}", "We can't stand by and watch the destruction of this beautiful planet.",
            new Throwable()));
   }
   
   public void testGetThrowable() throws Exception
   {
      Throwable t = new Throwable();

      assertNull(LogMessageFormatter.getThrowable());
      assertNull(LogMessageFormatter.getThrowable(new Object()));
      assertNull(LogMessageFormatter.getThrowable(new Object(), t, new String()));
      assertEquals(t, LogMessageFormatter.getThrowable(new Object(), t));
      assertEquals(t, LogMessageFormatter.getThrowable("Just testing{}", new Object(), t));
   }

   public void testGetMessage() throws Exception
   {
      //if object.toString return null
      LogMessageFormatter.getMessage("Hello, World {} ", new A());
      //if object is null
      LogMessageFormatter.getMessage("Hello, World {} {}", new Object(), null);
   }

   class A
   {
      public String toString()
      {
         return "";
      }
   }
}
