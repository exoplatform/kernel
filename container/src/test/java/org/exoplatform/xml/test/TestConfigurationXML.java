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
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.xml.object.XMLObject;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.FileInputStream;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 */
public class TestConfigurationXML extends TestCase
{

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

}