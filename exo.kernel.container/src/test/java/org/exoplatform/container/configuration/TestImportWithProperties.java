/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.container.configuration;

import org.exoplatform.container.xml.Configuration;

/**
 * Test usage of system properties in import configuration declaration
 */
public class TestImportWithProperties extends AbstractProfileTest
{
   private String oldValue;

   /**
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      oldValue = System.getProperty("db.configuration.path");
      System.clearProperty("db.configuration.path");
   }

   /**
    * @see junit.framework.TestCase#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      if (oldValue == null)
      {
         System.clearProperty("db.configuration.path");
      }
      else
      {
         System.setProperty("db.configuration.path", oldValue);
      }
      super.tearDown();
   }

   /**
    * Test if used system property not defined. String ${db.configuration.path}
    * should not be replaced.
    * 
    * @throws Exception
    */
   public void testWithNoPropertyDefined() throws Exception
   {
      assertNull(System.getProperty("db.configuration.path"));
      Configuration config = getConfiguration("import-with-parameter-configuration.xml");
      assertEquals(3, config.getImports().size());
      assertEquals("${db.configuration.path}/db.xml", config.getImports().get(0));
      assertEquals(System.getProperty("java.io.tmpdir") + "/bindfile.xml", config.getImports().get(1));
      assertEquals("simple.xml", config.getImports().get(2));

   }

   /**
    * Test if system property t defined. String ${db.configuration.path} should
    * be replaced with property value.
    * 
    * @throws Exception
    */
   public void testWithPropertyDefined() throws Exception
   {
      System.setProperty("db.configuration.path", "/home/admin/db");
      assertNotNull(System.getProperty("db.configuration.path"));
      Configuration config = getConfiguration("import-with-parameter-configuration.xml");
      assertEquals(3, config.getImports().size());
      assertEquals("/home/admin/db/db.xml", config.getImports().get(0));
      assertEquals(System.getProperty("java.io.tmpdir") + "/bindfile.xml", config.getImports().get(1));
      assertEquals("simple.xml", config.getImports().get(2));

   }
}
