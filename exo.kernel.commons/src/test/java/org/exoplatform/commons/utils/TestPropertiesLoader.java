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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPropertiesLoader extends TestCase
{

   public void testLoad1() throws IOException
   {
      String s = "a=b\nc=d\ne=f";
      LinkedHashMap<String, String> props = PropertiesLoader.load(new ByteArrayInputStream(s.getBytes("ISO8859-1")));
      Iterator<Map.Entry<String, String>> i = props.entrySet().iterator();
      assertTrue(i.hasNext());
      Map.Entry<String, String> entry = i.next();
      assertEquals("a", entry.getKey());
      assertEquals("b", entry.getValue());
      assertTrue(i.hasNext());
      entry = i.next();
      assertEquals("c", entry.getKey());
      assertEquals("d", entry.getValue());
      assertTrue(i.hasNext());
      entry = i.next();
      assertEquals("e", entry.getKey());
      assertEquals("f", entry.getValue());
      assertFalse(i.hasNext());
   }

   public void testLoad2() throws IOException
   {
      String s = "a=b\nc=d\ne=f\na=b";
      LinkedHashMap<String, String> props = PropertiesLoader.load(new ByteArrayInputStream(s.getBytes("ISO8859-1")));
      Iterator<Map.Entry<String, String>> i = props.entrySet().iterator();
      assertTrue(i.hasNext());
      Map.Entry<String, String> entry = i.next();
      assertEquals("c", entry.getKey());
      assertEquals("d", entry.getValue());
      assertTrue(i.hasNext());
      entry = i.next();
      assertEquals("e", entry.getKey());
      assertEquals("f", entry.getValue());
      assertTrue(i.hasNext());
      entry = i.next();
      assertEquals("a", entry.getKey());
      assertEquals("b", entry.getValue());
      assertFalse(i.hasNext());
   }
}
