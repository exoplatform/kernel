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
package org.exoplatform.xml.test;

import junit.framework.TestCase;

import org.exoplatform.xml.object.XMLCollection;
import org.exoplatform.xml.object.XMLObject;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Benjamin Mestrallet benjamin.mestrallet@exoplatform.com
 */
public class TestDataXML extends TestCase
{

   public void testMarshallAndUnmarshall() throws Exception
   {
      String projectdir = System.getProperty("basedir");
      IBindingFactory bfact = BindingDirectory.getFactory(XMLObject.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Object obj = uctx.unmarshalDocument(new FileInputStream(projectdir + "/src/test/resources/object.xml"), null);

      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      mctx.marshalDocument(obj, "UTF-8", null, new FileOutputStream(projectdir + "/target/object.xml"));
   }

   public void testConvert() throws Exception
   {
      String projectdir = System.getProperty("basedir");
      XMLObject xmlobj = new XMLObject(new TestObject());
      String xml1 = new String(xmlobj.toByteArray("UTF-8"));
      FileOutputStream os = new FileOutputStream(projectdir + "/target/test-object-1.xml");
      os.write(xml1.getBytes());
      os.close();

      File file = new File(projectdir + "/target/test-object-1.xml");
      FileInputStream is = new FileInputStream(file);

      FileChannel fchan = is.getChannel();
      ByteBuffer buff = ByteBuffer.allocate((int)file.length());
      fchan.read(buff);
      buff.rewind();
      byte[] data = buff.array();
      buff.clear();
      fchan.close();
      is.close();

      TestObject tobject = (TestObject)XMLObject.getObject(new ByteArrayInputStream(data));
      assertTrue(tobject.nested.intarray.length == 10);
      os = new FileOutputStream(projectdir + "/target/test-object-2.xml");
      xmlobj = new XMLObject(tobject);
      String xml2 = new String(xmlobj.toByteArray("UTF-8"));
      os.write(xml2.getBytes());
      os.close();
      assertTrue(xml1.equals(xml2));
      is.close();

      List list = new ArrayList();
      list.add("test.....................");
      list.add(new Date());
      XMLCollection xmllist = new XMLCollection(list);
      os = new FileOutputStream(projectdir + "/target/list.xml");
      os.write(xmllist.toByteArray("UTF-8"));
      os.close();
   }

   static public class TestObject
   {
      final static public String staticField = "staticField";

      String field = "hello";

      String method;

      Map map = new HashMap();

      List list = new ArrayList();

      NestedObject nested = new NestedObject();

      public TestObject()
      {
         Map nestedMap = new HashMap();
         nestedMap.put("nestedMapKey", "nestedMapvalue");
         map.put("string", "string");
         map.put("int", new Integer(10000));
         map.put("my", nestedMap);
         list.add("a list value");
         list.add("a list value");
      }

      public String getMethod()
      {
         return method;
      }

      public void setMethod(String s)
      {
         method = s;
      }

   }

   static public class NestedObject
   {

      String field = "field";

      String method;

      String xmlstring = "<xmlstring>this is a test</xmlstring>";

      int[] intarray = new int[10];

      int integer = 10;

      Map map = new HashMap();

      public NestedObject()
      {
         intarray[0] = 1;
         intarray[2] = 2;
      }

      public String getMethod()
      {
         return method;
      }

      public void setMethod(String s)
      {
         method = s;
      }
   }
}
