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

import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ComponentLifecyclePlugin;
import org.exoplatform.container.xml.ComponentPlugin;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.ContainerLifecyclePlugin;
import org.exoplatform.container.xml.ExternalComponentPlugins;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ManageableComponents;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.xml.object.XMLObject;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestConfigurationXML extends TestCase
{
   
   @SuppressWarnings("unchecked")
   public void testTrimValue() throws Exception
   {
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("test-trim-value.xml");
      assertNotNull(is);
      try
      {
         IBindingFactory bfact = BindingDirectory.getFactory(XMLObject.class);
         IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
         Configuration conf =
            (Configuration)uctx.unmarshalDocument(is, null);

         assertNotNull(conf);
         Iterator it = conf.getContainerLifecyclePluginIterator();
         assertNotNull(it);
         assertTrue(it.hasNext());
         ContainerLifecyclePlugin conlp = (ContainerLifecyclePlugin)it.next();
         assertEquals("container-lifecycle-plugin-type", conlp.getType());
         assertNotNull(conlp.getInitParams());
         assertEquals("container-lifecycle-plugin-value-param-value", (conlp.getInitParams().getValueParam("container-lifecycle-plugin-value-param-name")).getValue());
         it = conf.getComponentLifecyclePluginIterator();
         assertNotNull(it);
         assertTrue(it.hasNext());         
         ComponentLifecyclePlugin comlp = (ComponentLifecyclePlugin)it.next();
         assertEquals("component-lifecycle-plugin", comlp.getType());
         ManageableComponents mc = comlp.getManageableComponents();
         assertNotNull(mc);
         assertEquals("manageable-components-component-type", mc.getComponentsType().get(0));
         ValuesParam valuesParam = comlp.getInitParams().getValuesParam("component-lifecycle-plugin-values-param-name");
         assertNotNull(valuesParam);
         assertNotNull(valuesParam.getValues());
         assertTrue(valuesParam.getValues().contains("component-lifecycle-plugin-values-param-value1"));
         assertTrue(valuesParam.getValues().contains("component-lifecycle-plugin-values-param-value2"));
         Component c = conf.getComponent("component-key1");
         assertNotNull(c);
         assertEquals("component-type1", c.getType());
         PropertiesParam propertiesParam = c.getInitParams().getPropertiesParam("component-key1-properties-param-name");
         assertNotNull(propertiesParam);
         assertEquals("component-key1-properties-param-prop-value", propertiesParam.getProperty("component-key1-properties-param-prop-name"));
         c = conf.getComponent("component-type2");
         assertNotNull(c);
         ObjectParameter objectParameter = c.getInitParams().getObjectParam("component-key2-object-param-name"); 
         assertNotNull(objectParameter);
         MyObject o = (MyObject)objectParameter.getObject();
         assertNotNull(o);
         assertEquals("string-value", o.field1);
         assertEquals(1, o.field2);
         assertEquals(1l, o.field3);
         assertEquals(1d, o.field4);
         assertEquals(true, o.field5);
         assertNotNull(o.field6);
         assertEquals("entry-value", o.field6.get("entry-name"));
         assertNotNull(o.field7);
         assertTrue(o.field7.contains("string-value"));
         assertNotNull(o.field8);
         assertEquals(1, o.field8[0]);
         List list = c.getComponentPlugins();
         assertNotNull(list);
         assertFalse(list.isEmpty());
         ComponentPlugin cp = (ComponentPlugin)list.get(0);
         assertEquals("component-plugins-name", cp.getName());
         assertEquals("set-method-name", cp.getSetMethod());
         assertEquals("component-plugins-type", cp.getType());
         assertEquals(1, cp.getPriority());
         it = conf.getExternalComponentPluginsIterator();
         assertNotNull(it);
         assertTrue(it.hasNext());         
         ExternalComponentPlugins ecps = (ExternalComponentPlugins)it.next();
         assertEquals("target-component-name", ecps.getTargetComponent());
         list = ecps.getComponentPlugins();
         assertNotNull(list);
         assertFalse(list.isEmpty());
         cp = (ComponentPlugin)list.get(0);
         assertEquals("component-plugins-name", cp.getName());
         assertEquals("set-method-name", cp.getSetMethod());
         assertEquals("component-plugins-type", cp.getType());
         assertEquals(1, cp.getPriority());
         list = conf.getImports();
         assertNotNull(list);
         assertFalse(list.isEmpty());
         assertEquals("import-value", list.get(0));
         list = conf.getRemoveConfiguration();
         assertNotNull(list);
         assertFalse(list.isEmpty());
         assertEquals("remove-configuration-value", list.get(0));
      }
      finally
      {
         try
         {
            is.close();
         }
         catch (Exception e)
         {
            // ignore me
         }
      }
   }   

   public void testSystemPropertyResolving() throws Exception
   {

      System.setProperty("c_value", "c_external_value");
      System.setProperty("d_value", "d_external_value");
      System.setProperty("false_value", "false");
      System.setProperty("true_value", "true");
      System.setProperty("FALSE_value", "FALSE");
      System.setProperty("TRUE_value", "TRUE");
      System.setProperty("integer_value", "5");
      System.setProperty("long_value", "41");
      System.setProperty("double_value", "172.5");

      //
      String projectdir = System.getProperty("basedir");
      IBindingFactory bfact = BindingDirectory.getFactory(XMLObject.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      Configuration conf =
         (Configuration)uctx.unmarshalDocument(new FileInputStream(projectdir
            + "/src/test/resources/test-resolved-property.xml"), null);

      assertNotNull(conf);

      //
      Component component = conf.getComponent("component");
      assertNotNull(component);

      //
      assertValueParam("a_value", component, "a");
      assertValueParam("${b_value}", component, "b");
      assertValueParam("c_external_value", component, "c");
      assertValueParam("_d_external_value_", component, "d");

      //
      assertPropertyParam("a_value", component, "e", "e_a");
      assertPropertyParam("${b_value}", component, "e", "e_b");
      assertPropertyParam("c_external_value", component, "e", "e_c");
      assertPropertyParam("_d_external_value_", component, "e", "e_d");

      //
      ObjectParameter o = component.getInitParams().getObjectParam("f");
      assertNotNull(o);
      Person p = (Person)o.getObject();
      assertNotNull(p);
      assertEquals("a_value", p.address_a);
      assertEquals("${b_value}", p.address_b);
      assertEquals("c_external_value", p.address_c);
      assertEquals("_d_external_value_", p.address_d);
      assertEquals(true, p.male_a);
      assertEquals(false, p.male_b);
      assertEquals(true, p.male_c);
      assertEquals(false, p.male_d);
      assertEquals(true, p.male_e);
      assertEquals(false, p.male_f);
      assertEquals(4, p.age_a);
      assertEquals(5, p.age_b);
      assertEquals(40, p.weight_a);
      assertEquals(41, p.weight_b);
      assertEquals(172.4D, p.size_a);
      assertEquals(172.5D, p.size_b);
   }

   private void assertPropertyParam(String expectedValue, Component component, String paramName, String propertyName)
   {
      InitParams initParams = component.getInitParams();
      assertNotNull(initParams);
      PropertiesParam propertiesParam = initParams.getPropertiesParam(paramName);
      assertNotNull(paramName);
      assertEquals(expectedValue, propertiesParam.getProperty(propertyName));
   }

   private void assertValueParam(String expectedValue, Component component, String paramName)
   {
      InitParams initParams = component.getInitParams();
      assertNotNull(initParams);
      ValueParam valueParam = initParams.getValueParam(paramName);
      assertNotNull(paramName);
      assertEquals(expectedValue, valueParam.getValue());
   }
   
   public static class MyObject
   {
      public String field1;
      public int field2;
      public long field3;
      public double field4;
      public boolean field5;
      public Map field6;
      public Collection field7;
      public int[] field8;
   }

}