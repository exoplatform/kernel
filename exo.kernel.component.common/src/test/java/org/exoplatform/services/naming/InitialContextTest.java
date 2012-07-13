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

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;

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
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.xml.stream.XMLStreamException;

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

   private InitialContextInitializer initializer;
   
   public void setUp() throws Exception
   {
      PortalContainer container = PortalContainer.getInstance();
      initializer = (InitialContextInitializer)container.getComponentInstanceOfType(InitialContextInitializer.class);
      assertNotNull(initializer);
   }

   public void testCompositeNameUsing() throws Exception
   {
      Name name = new CompositeName("java:comp/env/jdbc/jcr");
      Enumeration en = name.getAll();
      while (en.hasMoreElements())
      {
         en.nextElement();
      }
      InitialContext ctx = new InitialContext();
      ctx.bind(name, "foo");
      assertEquals("foo", ctx.lookup(name));
      try
      {
         ctx.bind(name, "foo2");
         fail("A NameAlreadyBoundException is expected here");
      }
      catch (NameAlreadyBoundException e)
      {
         // expected exception
      }
      assertEquals("foo", ctx.lookup(name));
      assertEquals("foo", ctx.lookup("java:comp/env/jdbc/jcr"));
      ctx.unbind(name);
      try
      {
         ctx.lookup(name);
         fail("A NameNotFoundException is expected here");
      }
      catch (NameNotFoundException e)
      {
         // expected exception
      }
   }
   
   public void testGetContext() throws Exception
   {    
      assertNotNull(System.getProperty(Context.INITIAL_CONTEXT_FACTORY));
      InitialContext ctx = new InitialContext();
      assertNotNull(ctx);
      ctx.bind("test", "test");
      assertEquals("test", ctx.lookup("test"));
      try
      {
         ctx.bind("test", "test2");
         fail("A NameAlreadyBoundException is expected here");
      }
      catch (NameAlreadyBoundException e)
      {
         // expected exception
      }
      assertEquals("test", ctx.lookup("test"));
      ctx.rebind("test", "test2");
      assertEquals("test2", ctx.lookup("test"));

      initializer.getInitialContext().bind("test", "test3");
      assertEquals("test3", ctx.lookup("test"));
      ctx.rebind("test", "test4");
      assertEquals("test3", ctx.lookup("test"));
      initializer.getInitialContext().rebind("test", "test5");
      assertEquals("test5", ctx.lookup("test"));
      initializer.getInitialContext().unbind("test");
      try
      {
         initializer.getInitialContext().lookup("test");
         fail("A NameNotFoundException is expected here");
      }
      catch (NameNotFoundException e)
      {
         // expected exception
      }
      assertEquals("test4", ctx.lookup("test"));
      ctx.unbind("test");
      try
      {
         ctx.lookup("test");
         fail("A NameNotFoundException is expected here");
      }
      catch (NameNotFoundException e)
      {
         // expected exception
      }
      try
      {
         initializer.getInitialContext().unbind("test2");
         fail("A NameNotFoundException is expected here");
      }
      catch (NameNotFoundException e)
      {
         // expected exception
      }
      initializer.getInitialContext().bind("foo", "foo");
      assertEquals("foo", ctx.lookup("foo"));
      initializer.getInitialContext().bind("foo2", "foo2");
      assertEquals("foo2", ctx.lookup("foo2"));
      try
      {
         initializer.getInitialContext().rename("foo", "foo2");
         fail("A NameAlreadyBoundException is expected here");
      }
      catch (NameAlreadyBoundException e)
      {
         // expected exception
      }
      assertEquals("foo", ctx.lookup("foo"));
      assertEquals("foo2", ctx.lookup("foo2"));
      try
      {
         initializer.getInitialContext().rename("foo3", "foo4");
         fail("A NameNotFoundException is expected here");
      }
      catch (NameNotFoundException e)
      {
         // expected exception
      }
      initializer.getInitialContext().rename("foo", "foo3");
      assertEquals("foo", ctx.lookup("foo3"));
      assertEquals("foo2", ctx.lookup("foo2"));
      try
      {
         initializer.getInitialContext().lookup("foo");
         fail("A NameNotFoundException is expected here");
      }
      catch (NameNotFoundException e)
      {
         // expected exception
      }
      
      // check same instance
      initializer.getInitialContext().bind("bla", "bla");
      Object obj1 = initializer.getInitialContext().lookup("bla");
      Object obj2 = initializer.getInitialContext().lookup("bla");
      assertTrue(obj1 == obj2);
   }
   
   public void testConfig() throws Exception
   {

      assertNotNull(initializer.getDefaultContextFactory());

      assertEquals(TEST_CONTEXT_FACTORY, initializer.getDefaultContextFactory());

      List plugins = (List)initializer.getPlugins();

      assertFalse("No plugins configured", plugins.isEmpty());

      assertTrue("Plugin is not BindReferencePlugin type", plugins.get(0) instanceof BindReferencePlugin);

      BindReferencePlugin plugin = (BindReferencePlugin)plugins.get(0);

      assertNotNull(plugin.getBindName());
      assertNotNull(plugin.getReference());

   }

   /* 
    * Tests if InitialContextInitializer correctly gets bindings-store-path from 
    * param-value and pass it to InitialContexBinder, thus provides usage of different files
    * for different instances of the class. 
    */
   public void testDifferentFileUsage() throws FileNotFoundException, NamingException, XMLStreamException
   {
      Map<String, String> refAddr = new HashMap<String, String>();
      refAddr.put("driverClassName", "org.hsqldb.jdbcDriver");
      refAddr.put("url", "jdbc:hsqldb:file:target/temp/data/portal");
      refAddr.put("username", "sa");
      refAddr.put("password", "");

      initializer.getInitialContextBinder().bind("testjdbcjcr1", "javax.sql.DataSource",
         "org.apache.commons.dbcp.BasicDataSourceFactory", null, refAddr);

      assertTrue(new File("target/store-path.xml").exists());
   }

}
