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

import java.util.Properties;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPropertyManager extends TestCase
{

   public void testDevelopping()
   {
      _testDeveloppingIsNull();
      _testDeveloppingIsTrue();
      _testDeveloppingIsFalse();
      _testDeveloppingIsMaybe();
   }

   public void testGetPropertiesByPattern()
   {
      System.setProperty("exodev.prop1.enabled", "value1");
      System.setProperty("exodev.prop2.disabled", "value2");
      System.setProperty("my.prop3.enabled", "value3");
      System.setProperty("my.exodev.enabled", "value1");

      PropertyManager.refresh();
      assertTrue(PropertyManager.getUseCache());
      assertFalse(PropertyManager.isDevelopping());

      Properties result = PropertyManager.getPropertiesByPattern("^exodev\\..*$");
      assertNotNull(result);
      assertEquals(result.size(),2);

      result.forEach((k,v)->{
         assertTrue(k.toString().startsWith("exodev"));
      });

      result = PropertyManager.getPropertiesByPattern("exodev\\..*\\.enabled");
      assertNotNull(result);
      assertEquals(result.size(),1);

      assertTrue("exodev.prop1.enabled".equals(result.propertyNames().nextElement()));
   }

   private void _testDeveloppingIsNull()
   {
      assertNull(System.getProperty(PropertyManager.DEVELOPING));
      PropertyManager.refresh();
      assertTrue(PropertyManager.getUseCache());
      assertFalse(PropertyManager.isDevelopping());

      //
      assertNull(PropertyManager.getProperty("foo"));
      System.setProperty("foo", "bar");
      assertEquals("bar", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "juu");
      PropertyManager.refresh();
      assertEquals("juu", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "daa");
      assertEquals("juu", PropertyManager.getProperty("foo"));
      PropertyManager.setProperty("foo", "daa");
      assertEquals("daa", PropertyManager.getProperty("foo"));
      assertEquals("daa", System.getProperty("foo"));
   }

   private void _testDeveloppingIsTrue()
   {
      System.setProperty(PropertyManager.DEVELOPING, "true");
      PropertyManager.refresh();
      assertFalse(PropertyManager.getUseCache());
      assertTrue(PropertyManager.isDevelopping());

      //
      System.setProperty("foo", "bar");
      assertEquals("bar", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "juu");
      PropertyManager.refresh();
      assertEquals("juu", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "daa");
      assertEquals("daa", PropertyManager.getProperty("foo"));
      PropertyManager.setProperty("foo", "daa");
      assertEquals("daa", PropertyManager.getProperty("foo"));
      assertEquals("daa", System.getProperty("foo"));
   }

   private void _testDeveloppingIsFalse()
   {
      System.setProperty(PropertyManager.DEVELOPING, "false");
      PropertyManager.refresh();
      assertTrue(PropertyManager.getUseCache());
      assertFalse(PropertyManager.isDevelopping());

      //
      System.setProperty("foo", "bar");
      assertEquals("bar", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "juu");
      PropertyManager.refresh();
      assertEquals("juu", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "daa");
      assertEquals("juu", PropertyManager.getProperty("foo"));
      PropertyManager.setProperty("foo", "daa");
      assertEquals("daa", PropertyManager.getProperty("foo"));
      assertEquals("daa", System.getProperty("foo"));
   }

   private void _testDeveloppingIsMaybe()
   {
      System.setProperty(PropertyManager.DEVELOPING, "maybe");
      PropertyManager.refresh();
      assertTrue(PropertyManager.getUseCache());
      assertFalse(PropertyManager.isDevelopping());

      //
      System.setProperty("foo", "bar");
      assertEquals("bar", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "juu");
      PropertyManager.refresh();
      assertEquals("juu", PropertyManager.getProperty("foo"));
      System.setProperty("foo", "daa");
      assertEquals("juu", PropertyManager.getProperty("foo"));
      PropertyManager.setProperty("foo", "daa");
      assertEquals("daa", PropertyManager.getProperty("foo"));
      assertEquals("daa", System.getProperty("foo"));
   }
}
