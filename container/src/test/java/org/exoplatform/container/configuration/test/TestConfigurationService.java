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
package org.exoplatform.container.configuration.test;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.configuration.ConfigurationUnmarshaller;
import org.exoplatform.container.monitor.jvm.JVMRuntimeInfo;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.test.BasicTestCase;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/*
 * Thu, May 15, 2003 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestConfigurationService.java 5799 2006-05-28 17:55:42Z geaz $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class TestConfigurationService extends BasicTestCase
{
   private ConfigurationManager service_;

   public TestConfigurationService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      PortalContainer manager = PortalContainer.getInstance();
      service_ = (ConfigurationManager)manager.getComponentInstanceOfType(ConfigurationManager.class);
   }

   public void testXSDBadSchema() throws Exception
   {
      String basedir = System.getProperty("basedir");
      File f = new File(basedir + "/src/main/resources/configuration-bad-schema.xml");
      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();
      URL url = f.toURI().toURL();
      try
      {
         unmarshaller.unmarshall(url);
         fail("JIBX should have failed");
      }
      catch (org.jibx.runtime.JiBXException ignore)
      {
         // JIBX error is normal
      }
   }

   public void testXSDNoSchema() throws Exception
   {
      String basedir = System.getProperty("basedir");
      File f = new File(basedir + "/src/main/resources/configuration-no-schema.xml");
      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();
      URL url = f.toURI().toURL();
      Configuration conf = unmarshaller.unmarshall(url);
      assertNotNull(conf);
   }

   public void testMarshallAndUnmarshall() throws Exception
   {
      String basedir = System.getProperty("basedir");

      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();

      File f = new File(basedir + "/src/main/resources/configuration.xml");

      Object obj = unmarshaller.unmarshall(f.toURI().toURL());
      System.out.print(obj);

      IBindingFactory bfact = BindingDirectory.getFactory(Configuration.class);
      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      mctx.marshalDocument(obj, "UTF-8", null, new FileOutputStream(basedir + "/target/configuration.xml"));
   }

   public void testConfigurationService(InitParams params) throws Exception
   {
      ObjectParameter objParam = params.getObjectParam("new.user.configuration");
      objParam.getObject();
   }

   public void testJVMEnvironment() throws Exception
   {
      JVMRuntimeInfo jvm = (JVMRuntimeInfo)RootContainer.getInstance().getComponentInstanceOfType(JVMRuntimeInfo.class);
      System.out.println(jvm.getSystemPropertiesAsText());
   }

   protected String getDescription()
   {
      return "Test Configuration Service";
   }
}
