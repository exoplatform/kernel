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
package org.exoplatform.container.jmx;

import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.annotations.ManagedName;

import javax.management.Attribute;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestExoMBeanAttribute extends AbstractTestExoMBean
{

   public void test1() throws Exception
   {
      test1(register("domain:name=mbean0", MBean1_0.class), "getString");
      test1(register("domain:name=mbean1", MBean1_1.class), "getString");
      test1(register("domain:name=mbean2", MBean1_2.class), "getInteger");
   }

   private void test1(Bean bean, String getterName) throws Exception
   {
      MBeanOperationInfo[] operationInfos = bean.info.getOperations();
      assertNotNull(operationInfos);
      assertEquals(1, operationInfos.length);
      MBeanOperationInfo operationInfo = operationInfos[0];
      assertNotNull(operationInfo);
      assertEquals(getterName, operationInfo.getName());
      assertEquals("java.lang.String", operationInfo.getReturnType());
      assertEquals(0, operationInfo.getSignature().length);
      MBeanAttributeInfo[] attributeInfos = bean.info.getAttributes();
      assertNotNull(attributeInfos);
      assertEquals(1, attributeInfos.length);
      MBeanAttributeInfo attributeInfo = attributeInfos[0];
      assertEquals("String", attributeInfo.getName());
      assertEquals("java.lang.String", attributeInfo.getType());
      assertEquals(true, attributeInfo.isReadable());
      assertEquals(false, attributeInfo.isWritable());
      MBean1_0.string = "Foo";
      assertEquals("Foo", server.invoke(bean.name, getterName, new Object[0], new String[0]));
      MBean1_0.string = "Bar";
      assertEquals("Bar", server.getAttribute(bean.name, "String"));
   }

   @Managed
   public static class MBean1_0
   {
      static String string;

      @Managed
      public String getString()
      {
         return string;
      }
   }

   @Managed
   public static class MBean1_1
   {
      public void setString(String s)
      {
         MBean1_0.string = s;
      }

      @Managed
      public String getString()
      {
         return MBean1_0.string;
      }
   }

   @Managed
   public static class MBean1_2
   {
      @Managed
      @ManagedName("String")
      public String getInteger()
      {
         return MBean1_0.string;
      }
   }

   public void test2() throws Exception
   {
      test2(register("domain:name=mbean0", MBean2_0.class), "setString");
      test2(register("domain:name=mbean1", MBean2_1.class), "setString");
      test2(register("domain:name=mbean2", MBean2_2.class), "setInteger");
   }

   public void test2(Bean bean, String setterName) throws Exception
   {
      MBeanOperationInfo[] operationInfos = bean.info.getOperations();
      assertNotNull(operationInfos);
      assertEquals(1, operationInfos.length);
      MBeanOperationInfo operationInfo = operationInfos[0];
      assertNotNull(operationInfo);
      assertEquals(setterName, operationInfo.getName());
      assertEquals("void", operationInfo.getReturnType());
      MBeanParameterInfo[] attributeParameterInfos = operationInfo.getSignature();
      assertEquals(1, attributeParameterInfos.length);
      MBeanParameterInfo attributeParameterInfo = attributeParameterInfos[0];
      assertEquals("java.lang.String", attributeParameterInfo.getType());
      MBeanAttributeInfo[] attributeInfos = bean.info.getAttributes();
      assertNotNull(attributeInfos);
      assertEquals(1, attributeInfos.length);
      MBeanAttributeInfo attributeInfo = attributeInfos[0];
      assertEquals("String", attributeInfo.getName());
      assertEquals("java.lang.String", attributeInfo.getType());
      assertEquals(false, attributeInfo.isReadable());
      assertEquals(true, attributeInfo.isWritable());
      MBean2_0.string = null;
      assertEquals(null, server.invoke(bean.name, setterName, new Object[]{"Foo"}, new String[]{"java.lang.String"}));
      assertEquals("Foo", MBean2_0.string);
      server.setAttribute(bean.name, new Attribute("String", "Bar"));
      assertEquals("Bar", MBean2_0.string);
   }

   @Managed
   public static class MBean2_0
   {
      static String string;

      @Managed
      public void setString(String s)
      {
         string = s;
      }
   }

   @Managed
   public static class MBean2_1
   {
      @Managed
      public void setString(String s)
      {
         MBean2_0.string = s;
      }

      public String getString()
      {
         return MBean2_0.string;
      }
   }

   @Managed
   public static class MBean2_2
   {
      @Managed
      @ManagedName("String")
      public void setInteger(String s)
      {
         MBean2_0.string = s;
      }
   }

   public void test3() throws Exception
   {
      test3(register("domain:name=mbean0", MBean3_0.class), "getString", "setString");
      test3(register("domain:name=mbean1", MBean3_1.class), "getInteger", "setInteger");
      test3(register("domain:name=mbean2", MBean3_2.class), "getInteger", "setInteger");
      test3(register("domain:name=mbean3", MBean3_3.class), "getInteger", "setInteger");
   }

   public void test3(Bean bean, String getterName, String setterName) throws Exception
   {
      MBeanOperationInfo[] operationInfos = bean.info.getOperations();
      assertNotNull(operationInfos);
      assertEquals(2, operationInfos.length);
      MBeanOperationInfo setStringInfo = bean.info.getOperation(setterName);
      assertNotNull(setStringInfo);
      assertEquals(setterName, setStringInfo.getName());
      assertEquals("void", setStringInfo.getReturnType());
      MBeanParameterInfo[] setStringParameterInfos = setStringInfo.getSignature();
      assertEquals(1, setStringParameterInfos.length);
      MBeanParameterInfo attributeParameterInfo = setStringParameterInfos[0];
      assertEquals("java.lang.String", attributeParameterInfo.getType());
      MBeanOperationInfo getStringInfo = bean.info.getOperation(getterName);
      assertNotNull(getStringInfo);
      assertEquals(getterName, getStringInfo.getName());
      assertEquals("java.lang.String", getStringInfo.getReturnType());
      assertEquals(0, getStringInfo.getSignature().length);
      MBeanAttributeInfo[] attributeInfos = bean.info.getAttributes();
      assertNotNull(attributeInfos);
      assertEquals(1, attributeInfos.length);
      MBeanAttributeInfo attributeInfo = attributeInfos[0];
      assertEquals("String", attributeInfo.getName());
      assertEquals("java.lang.String", attributeInfo.getType());
      assertEquals(true, attributeInfo.isReadable());
      assertEquals(true, attributeInfo.isWritable());
      MBean3_0.string = null;
      assertEquals(null, server.invoke(bean.name, setterName, new Object[]{"Foo"}, new String[]{"java.lang.String"}));
      assertEquals("Foo", MBean3_0.string);
      server.setAttribute(bean.name, new Attribute("String", "Bar"));
      assertEquals("Bar", MBean3_0.string);
      MBean3_0.string = "Juu";
      assertEquals("Juu", server.invoke(bean.name, getterName, new Object[0], new String[0]));
      MBean3_0.string = "Daa";
      assertEquals("Daa", server.getAttribute(bean.name, "String"));
   }

   @Managed
   public static class MBean3_0
   {
      static String string;

      @Managed
      public String getString()
      {
         return string;
      }

      @Managed
      public void setString(String s)
      {
         string = s;
      }
   }

   @Managed
   public static class MBean3_1
   {
      @Managed
      @ManagedName("String")
      public String getInteger()
      {
         return MBean3_0.string;
      }

      @Managed
      public void setInteger(String s)
      {
         MBean3_0.string = s;
      }
   }

   @Managed
   public static class MBean3_2
   {
      @Managed
      public String getInteger()
      {
         return MBean3_0.string;
      }

      @Managed
      @ManagedName("String")
      public void setInteger(String s)
      {
         MBean3_0.string = s;
      }
   }

   @Managed
   public static class MBean3_3
   {
      @Managed
      @ManagedName("String")
      public String getInteger()
      {
         return MBean3_0.string;
      }

      @Managed
      @ManagedName("String")
      public void setInteger(String s)
      {
         MBean3_0.string = s;
      }
   }

   public void test4() throws Exception
   {
      assertNotBuildable(MBean4_0.class);
      assertNotBuildable(MBean4_1.class);
      assertNotBuildable(MBean4_2.class);
      assertNotBuildable(MBean4_3.class);
   }

   @Managed
   public static class MBean4_0
   {
      @Managed
      @ManagedName("String")
      public String getInteger()
      {
         return null;
      }

      @Managed
      @ManagedName("Integer")
      public void setInteger(String s)
      {
      }
   }

   @Managed
   public static class MBean4_1
   {
      @Managed
      public void setInteger(String s)
      {
      }

      @Managed
      public void setInteger(Integer s)
      {
      }
   }

   @Managed
   public static class MBean4_2
   {
      @Managed
      @ManagedName("Integer")
      public void setInteger(String s)
      {
      }

      @Managed
      @ManagedName("Integer")
      public void setString(Integer s)
      {
      }
   }

   @Managed
   public static class MBean4_3
   {
      @Managed
      @ManagedName("Integer")
      public Integer getInteger()
      {
         return null;
      }

      @Managed
      @ManagedName("Integer")
      public String getString()
      {
         return null;
      }
   }

   public void test5() throws Exception
   {
      test5(register("domain:name=mbean0", MBean5_0.class));
      test5(register("domain:name=mbean1", MBean5_1.class));
   }

   public void test5(Bean bean) throws Exception
   {
      MBeanAttributeInfo[] attributeInfos = bean.info.getAttributes();
      assertNotNull(attributeInfos);
      assertEquals(1, attributeInfos.length);
      MBeanAttributeInfo attributeInfo = attributeInfos[0];
      assertEquals("String", attributeInfo.getName());
      assertEquals("String_description", attributeInfo.getDescription());
      assertEquals("java.lang.String", attributeInfo.getType());
   }

   @Managed
   public static class MBean5_0
   {
      @Managed
      @ManagedDescription("String_description")
      public String getString()
      {
         return null;
      }
   }

   @Managed
   public static class MBean5_1
   {
      @Managed
      @ManagedDescription("String_description")
      public void setString(String string)
      {
      }
   }

   @Managed
   public static class MBean5_2
   {
      @Managed
      @ManagedDescription("String_description")
      public void setString(String string)
      {
      }

      @Managed
      @ManagedDescription("String_description 2")
      public String getString()
      {
         return null;
      }
   }

   public void test6() throws Exception
   {
      test6(register("domain:name=mbean0", MBean6_0.class), "Name");
      test6(register("domain:name=mbean1", MBean6_1.class), "Foo");
      test6(register("domain:name=mbean2", MBean6_2.class), "Bar");
      test6(register("domain:name=mbean3", MBean6_3.class), "Juu");
   }

   private void test6(Bean bean, String expectedName) throws Exception
   {
      MBeanAttributeInfo[] attributeInfos = bean.info.getAttributes();
      assertEquals(1, attributeInfos.length);
      MBeanAttributeInfo attributeInfo = attributeInfos[0];
      assertEquals(expectedName, attributeInfo.getName());
   }

   public static interface Interface6_0
   {
      @Managed
      String getName();
   }

   @Managed
   public static class MBean6_0 implements Interface6_0
   {
      public String getName()
      {
         return "Foo";
      }
   }

   public static interface Interface6_1
   {
      @Managed
      @ManagedName("Foo")
      String getName();
   }

   @Managed
   public static class MBean6_1 implements Interface6_1
   {
      public String getName()
      {
         return "Foo";
      }
   }

   public static interface Interface6_2
   {
      @Managed
      String getName();
   }

   @Managed
   public static class MBean6_2 implements Interface6_2
   {
      @ManagedName("Bar")
      public String getName()
      {
         return "Foo";
      }
   }

   public static interface Interface6_3
   {
      @Managed
      @ManagedName("Foo")
      String getName();
   }

   @Managed
   public static class MBean6_3 implements Interface6_3
   {
      @ManagedName("Juu")
      public String getName()
      {
         return "Foo";
      }
   }

   public void test7() throws Exception
   {
      test7(register("domain:name=mbean0", MBean7_0.class), "Name");
      test7(register("domain:name=mbean1", MBean7_1.class), "Foo");
      test7(register("domain:name=mbean2", MBean7_2.class), "Bar");
      test7(register("domain:name=mbean3", MBean7_3.class), "Juu");
   }

   private void test7(Bean bean, String expectedName) throws Exception
   {
      MBeanAttributeInfo[] attributeInfos = bean.info.getAttributes();
      assertEquals(1, attributeInfos.length);
      MBeanAttributeInfo attributeInfo = attributeInfos[0];
      assertEquals(expectedName, attributeInfo.getName());
   }

   public static class SuperMBean7_0
   {
      @Managed
      public String getName()
      {
         return "Foo";
      }
   }

   @Managed
   public static class MBean7_0 extends SuperMBean7_0
   {
      public String getName()
      {
         return "Foo";
      }
   }

   public static class SuperMBean7_1
   {
      @Managed
      @ManagedName("Foo")
      public String getName()
      {
         return "Foo";
      }
   }

   @Managed
   public static class MBean7_1 extends SuperMBean7_1
   {
      public String getName()
      {
         return "Foo";
      }
   }

   public static class SuperMBean7_2
   {
      @Managed
      public String getName()
      {
         return "Foo";
      }
   }

   @Managed
   public static class MBean7_2 extends SuperMBean7_2
   {
      @ManagedName("Bar")
      public String getName()
      {
         return "Foo";
      }
   }

   public static class SuperMBean7_3
   {
      @Managed
      @ManagedName("Foo")
      public String getName()
      {
         return "Foo";
      }
   }

   @Managed
   public static class MBean7_3 extends SuperMBean7_3
   {
      @ManagedName("Juu")
      public String getName()
      {
         return "Foo";
      }
   }
}
