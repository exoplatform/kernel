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
package org.exoplatform.services.naming;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;

/**
 * Created by The eXo Platform SAS .<br/> Prerequisites: default-context-factory
 * = org.exoplatform.services.naming.impl.SimpleContextFactory
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: InitialContextTest.java 5655 2006-05-22 14:19:41Z geaz $
 */
public class InitialContextTest extends TestCase
{

   private static String TEST_CONTEXT_FACTORY = "org.exoplatform.services.naming.SimpleContextFactory";

   private PortalContainer container;

   public void setUp() throws Exception
   {

      //      StandaloneContainer.setConfigurationPath("src/test/resources/conf/standalone/test-configuration.xml");

      container = PortalContainer.getInstance();
   }

   public void testConfig() throws Exception
   {

      InitialContextInitializer initializer =
         (InitialContextInitializer)container.getComponentInstanceOfType(InitialContextInitializer.class);

      assertNotNull(initializer);

      assertNotNull(initializer.getDefaultContextFactory());

      assertEquals(TEST_CONTEXT_FACTORY, initializer.getDefaultContextFactory());

      List plugins = (List)initializer.getPlugins();

      assertFalse("No plugins configured", plugins.isEmpty());

      assertTrue("Plugin is not BindReferencePlugin type", plugins.get(0) instanceof BindReferencePlugin);

      BindReferencePlugin plugin = (BindReferencePlugin)plugins.get(0);

      assertNotNull(plugin.getBindName());
      assertNotNull(plugin.getReference());

   }

   public void testGetContext() throws Exception
   {
      assertNotNull(System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
      InitialContext ctx = new InitialContext();
      assertNotNull(ctx);
      ctx.bind("test", "test");
      assertEquals("test", ctx.lookup("test"));
   }

   public void testCompositeNameUsing() throws Exception
   {
      Name name = new CompositeName("java:comp/env/jdbc/jcr");
      System.out.println("NAME ---- " + name.get(0) + " " + name.getPrefix(1) + " " + name.getSuffix(1) + " "
         + name.getPrefix(0) + " " + name.getSuffix(0));
      Enumeration en = name.getAll();
      while (en.hasMoreElements())
      {
         System.out.println("---- " + en.nextElement());
      }
   }

   /* 
    * Tests if InitialContextInitializer correctly gets bindings-store-path from 
    * param-value and pass it to InitialContexBinder, thus provides usage of different files
    * for different instances of the class. 
    */
   public void testDifferentFileUsage() throws FileNotFoundException, NamingException, XMLStreamException
   {

      InitialContextInitializer initializer =
         (InitialContextInitializer)container.getComponentInstanceOfType(InitialContextInitializer.class);

      Map<String, String> refAddr = new HashMap<String, String>();
      refAddr.put("driverClassName", "org.hsqldb.jdbcDriver");
      refAddr.put("url", "jdbc:hsqldb:file:target/temp/data/portal");
      refAddr.put("username", "sa");
      refAddr.put("password", "");

      initializer.bind("testjdbcjcr1", "javax.sql.DataSource", "org.apache.commons.dbcp.BasicDataSourceFactory", null,
         refAddr);

      assertTrue(new File("target/store-path.xml").exists());
   }

}
