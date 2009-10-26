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

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestExoMBeanOperation extends AbstractTestExoMBean
{

   public void test1() throws Exception
   {
      Bean bean = register("domain:name=mbean", MBean1.class);
      MBeanOperationInfo[] operationInfos = bean.info.getOperations();
      assertNotNull(operationInfos);
      assertEquals(0, operationInfos.length);
      MBeanAttributeInfo[] mbeanAttributeInfos = bean.info.getAttributes();
      assertNotNull(mbeanAttributeInfos);
      assertEquals(0, mbeanAttributeInfos.length);
   }

   @Managed
   public static class MBean1
   {
      public String op(Object arg)
      {
         return null;
      }
   }

   public void test2() throws Exception
   {
      Bean bean = register("domain:name=mbean", MBean2.class);
      MBeanOperationInfo[] operationInfos = bean.info.getOperations();
      assertNotNull(operationInfos);
      assertEquals(1, operationInfos.length);
      MBeanOperationInfo operationInfo = operationInfos[0];
      assertNotNull(operationInfo);
      assertEquals("op", operationInfo.getName());
      assertEquals("op_desc", operationInfo.getDescription());
      assertEquals("java.lang.String", operationInfo.getReturnType());
      MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
      assertNotNull(parameterInfos);
      assertEquals(1, parameterInfos.length);
      MBeanParameterInfo parameterInfo = parameterInfos[0];
      assertNotNull(parameterInfo);
      assertEquals("arg_desc", parameterInfo.getDescription());
      assertEquals("java.lang.Integer", parameterInfo.getType());
      MBeanAttributeInfo[] mbeanAttributeInfos = bean.info.getAttributes();
      assertNotNull(mbeanAttributeInfos);
      assertEquals(0, mbeanAttributeInfos.length);
      assertEquals("5", server.invoke(bean.name, "op", new Object[]{5}, new String[]{"java.lang.Integer"}));
   }

   @Managed
   public static class MBean2
   {
      @Managed
      @ManagedDescription("op_desc")
      public String op(@ManagedDescription("arg_desc") Integer arg)
      {
         return Integer.toString(arg);
      }
   }

   public void test3() throws Exception
   {
      Bean bean = register("domain:name=mbean", MBean3.class);
      MBeanOperationInfo[] operationInfos = bean.info.getOperations();
      assertNotNull(operationInfos);
      assertEquals(1, operationInfos.length);
      MBeanOperationInfo operationInfo = operationInfos[0];
      assertNotNull(operationInfo);
      assertEquals("op", operationInfo.getName());
      assertEquals("java.lang.String", operationInfo.getReturnType());
      MBeanParameterInfo[] parameterInfos = operationInfo.getSignature();
      assertNotNull(parameterInfos);
      assertEquals(1, parameterInfos.length);
      MBeanParameterInfo parameterInfo = parameterInfos[0];
      assertNotNull(parameterInfo);
      assertEquals("_arg", parameterInfo.getName());
      assertEquals("java.lang.Integer", parameterInfo.getType());
      MBeanAttributeInfo[] mbeanAttributeInfos = bean.info.getAttributes();
      assertNotNull(mbeanAttributeInfos);
      assertEquals(0, mbeanAttributeInfos.length);
      assertEquals("7", server.invoke(bean.name, "op", new Object[]{7}, new String[]{"java.lang.Integer"}));
   }

   @Managed
   public static class MBean3
   {
      @Managed
      public String op(@ManagedName("_arg") Integer arg)
      {
         return Integer.toString(arg);
      }
   }

   public void test4() throws Exception
   {
      Bean bean = register("domain:name=mbean", MBean4.class);
      MBeanOperationInfo[] operationInfos = bean.info.getOperations();
      assertNotNull(operationInfos);
      assertEquals(2, operationInfos.length);
      MBeanOperationInfo operation1Info = bean.info.getOperation("op1");
      assertNotNull(operation1Info);
      assertEquals("op1", operation1Info.getName());
      assertEquals("java.lang.String", operation1Info.getReturnType());
      MBeanParameterInfo[] operation1ParameterInfos = operation1Info.getSignature();
      assertNotNull(operation1ParameterInfos);
      assertEquals(1, operation1ParameterInfos.length);
      MBeanParameterInfo operation1ParameterInfo = operation1ParameterInfos[0];
      assertNotNull(operation1ParameterInfo);
      assertEquals("java.lang.Boolean", operation1ParameterInfo.getType());
      MBeanOperationInfo operation2Info = bean.info.getOperation("op2");
      assertNotNull(operation2Info);
      assertEquals("op2", operation2Info.getName());
      assertEquals("java.lang.String", operation2Info.getReturnType());
      MBeanParameterInfo[] operation2ParameterInfos = operation2Info.getSignature();
      assertNotNull(operation2ParameterInfos);
      assertEquals(1, operation2ParameterInfos.length);
      MBeanParameterInfo operation2ParameterInfo = operation2ParameterInfos[0];
      assertNotNull(operation2ParameterInfo);
      assertEquals("java.lang.Integer", operation2ParameterInfo.getType());
      MBeanAttributeInfo[] mbeanAttributeInfos = bean.info.getAttributes();
      assertNotNull(mbeanAttributeInfos);
      assertEquals(0, mbeanAttributeInfos.length);
      assertEquals("true", server.invoke(bean.name, "op1", new Object[]{true}, new String[]{"java.lang.Boolean"}));
      assertEquals("7", server.invoke(bean.name, "op2", new Object[]{7}, new String[]{"java.lang.Integer"}));
   }

   public static class ParentMBean4
   {
      @Managed
      public String op1(Boolean arg)
      {
         return Boolean.toString(arg);
      }
   }

   @Managed
   public static class MBean4 extends ParentMBean4
   {
      @Managed
      public String op2(Integer arg)
      {
         return Integer.toString(arg);
      }
   }

   public void test5()
   {
      assertNotBuildable(MBean5.class);
   }

   @Managed
   public static class MBean5
   {
      @Managed
      @ManagedName("foo")
      public String op2(Integer arg)
      {
         return Integer.toString(arg);
      }
   }
}
