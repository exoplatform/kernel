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

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com
 */
public class TestMapResourceBundle extends TestCase
{
   public void testResolveDependencies()
   {
      ResourceBundle rB = ResourceBundle.getBundle("org.exoplatform.commons.utils.resources");
      MapResourceBundle mapRB = new MapResourceBundle(rB, new Locale("en"));
      mapRB.resolveDependencies();
      assertEquals("attention", mapRB.getString("key1"));
      assertEquals("attention please", mapRB.getString("key3"));
      assertEquals("attention and attention please alors", mapRB.getString("key2"));
      assertEquals("non-existing", mapRB.getString("key4"));
      assertEquals("key5 StackOverflowError", mapRB.getString("key5"));
      assertTrue(mapRB.getString("key6").startsWith("key"));
      assertTrue(mapRB.getString("key7").startsWith("key"));
      assertEquals("some other value value", mapRB.getString("key8"));
      assertEquals("other value", mapRB.getString("key9"));
      assertEquals("value", mapRB.getString("key10"));
      assertEquals("X", mapRB.getString("key11"));
      assertEquals("-X-", mapRB.getString("key12"));
      assertEquals("-X-", mapRB.getString("key13"));
      assertEquals("--X--", mapRB.getString("key14"));
   }
}
