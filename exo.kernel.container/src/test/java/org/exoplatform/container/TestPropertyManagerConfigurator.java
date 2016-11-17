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
package org.exoplatform.container;

import junit.framework.TestCase;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.Tools;
import org.exoplatform.container.configuration.ConfigurationManagerImpl;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;

import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPropertyManagerConfigurator extends TestCase
{
   /** . */
   private Map<String, String> previous;

   private Map<String, String> reset()
   {
      Map<String, String> current = Tools.asMap(System.getProperties());
      Map<String, String> additions = new HashMap<String, String>();
      if (previous != null)
      {
         for (Map.Entry<String, String> entry : current.entrySet())
         {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();
            if (!previous.containsKey(propertyName))
            {
               additions.put(propertyName, propertyValue);
            }
         }
         System.setProperties(Tools.asProperties(previous));
      }
      previous = current;
      return additions;
   }
   
   @Override
   protected void tearDown() throws Exception
   {
      PropertyManager.refresh();
   }

   public void testFromXML() throws Exception
   {
      reset();
      URL propertiesURL = TestPropertyManagerConfigurator.class.getResource("property-configurator.xml");
      assertNotNull(propertiesURL);
      System.setProperty(PropertyManager.PROPERTIES_URL, propertiesURL.toString());
      System.setProperty("property_2", "property_value_2");
      PropertiesParam propertiesParam = new PropertiesParam();
      InitParams params = new InitParams();
      params.put("properties", propertiesParam);
      new PropertyConfigurator(params, new ConfigurationManagerImpl(new HashSet<String>()));
      Map<String, String> additions = reset();
      assertEquals("property_value_1", additions.get("property_1"));
      assertEquals("property_value_2", additions.get("property_2"));
      assertEquals("${property_3}", additions.get("property_3"));
      assertEquals("property_value_1-property_value_2", additions.get("property_4"));
   }

   public void testSimple()
   {
      reset();
      PropertiesParam propertiesParam = new PropertiesParam();
      propertiesParam.setProperty("property_1", "property_value_1");
      InitParams params = new InitParams();
      params.put("properties", propertiesParam);
      new PropertyConfigurator(params, new ConfigurationManagerImpl(new HashSet<String>()));
      Map<String, String> additions = reset();
      assertEquals(Collections.singletonMap("property_1", "property_value_1"), additions);
   }

   public void testFromConfig() throws Exception
   {
      reset();
      System.setProperty("property_2", "property_value_2");
      URL url = TestPropertyManagerConfigurator.class.getResource("property-configurator-configuration.xml");
      assertNotNull(url);
      ContainerBuilder.bootstrap(url);
      Map<String, String> additions = reset();
      assertEquals("property_value_1", additions.get("property_1"));
      assertEquals("property_value_2", additions.get("property_2"));
      assertEquals("${property_3}", additions.get("property_3"));
   }

   public void testFromConfigWithProfile() throws Exception
   {
      reset();
      URL url = TestPropertyManagerConfigurator.class.getResource("property-configurator-with-profile-configuration.xml");
      assertNotNull(url);
      ContainerBuilder.bootstrap(url, "foo");
      Map<String, String> additions = reset();
      assertEquals("property_value_1_foo", additions.get("property_1"));
   }

   public void testFromConfigByParam() throws Exception
   {
      reset();
      URL propertiesURL = TestPropertyManagerConfigurator.class.getResource("property-configurator.properties");
      assertNotNull(propertiesURL);
      System.setProperty("properties.url", propertiesURL.toString());
      System.setProperty("property_2", "property_value_2");
      URL url = TestPropertyManagerConfigurator.class.getResource("property-configurator-with-path-configuration.xml");
      ContainerBuilder.bootstrap(url);
      Map<String, String> additions = reset();
      assertEquals("property_value_1", additions.get("property_1"));
      assertEquals("property_value_2", additions.get("property_2"));
      assertEquals("${property_3}", additions.get("property_3"));
      assertEquals("property_value_1-property_value_2", additions.get("property_4"));
   }

   public void testFromPropertiesByParam() throws Exception
   {
      reset();
      URL propertiesURL = TestPropertyManagerConfigurator.class.getResource("property-configurator.properties");
      assertNotNull(propertiesURL);
      System.setProperty("property_2", "property_value_2");
      ValueParam propertiesPathParam = new ValueParam();
      propertiesPathParam.setName("properties.url");
      propertiesPathParam.setValue(propertiesURL.toString());
      InitParams params = new InitParams();
      params.put("properties.url", propertiesPathParam);
      new PropertyConfigurator(params, new ConfigurationManagerImpl(new HashSet<String>()));
      Map<String, String> additions = reset();
      assertEquals("property_value_1", additions.get("property_1"));
      assertEquals("property_value_2", additions.get("property_2"));
      assertEquals("${property_3}", additions.get("property_3"));
      assertEquals("property_value_1-property_value_2", additions.get("property_4"));
   }

   public void testFromProperties() throws Exception
   {
      reset();
      URL propertiesURL = TestPropertyManagerConfigurator.class.getResource("property-configurator.properties");
      assertNotNull(propertiesURL);
      System.setProperty(PropertyManager.PROPERTIES_URL, propertiesURL.toString());
      System.setProperty("property_2", "property_value_2");
      PropertiesParam propertiesParam = new PropertiesParam();
      InitParams params = new InitParams();
      params.put("properties", propertiesParam);
      new PropertyConfigurator(params, new ConfigurationManagerImpl(new HashSet<String>()));
      Map<String, String> additions = reset();
      assertEquals("property_value_1", additions.get("property_1"));
      assertEquals("property_value_2", additions.get("property_2"));
      assertEquals("${property_3}", additions.get("property_3"));
      assertEquals("property_value_1-property_value_2", additions.get("property_4"));
   }

   public void testFromPropertiesSkipBracket() throws Exception
   {
      reset();
      URL propertiesURL = TestPropertyManagerConfigurator.class.getResource("property-configurator.properties");
      assertNotNull(propertiesURL);
      System.setProperty(PropertyManager.PROPERTIES_URL, propertiesURL.toString());
      PropertiesParam propertiesParam = new PropertiesParam();
      InitParams params = new InitParams();
      params.put("properties", propertiesParam);
      new PropertyConfigurator(params, new ConfigurationManagerImpl(new HashSet<String>()));
      Map<String, String> additions = reset();
      assertEquals("property {0} value {1}", additions.get("property_5"));
      assertEquals("property_value_1-property {0} value {1}", additions.get("property_6"));
   }
}
