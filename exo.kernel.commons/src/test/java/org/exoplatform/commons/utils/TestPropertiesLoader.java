/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
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
