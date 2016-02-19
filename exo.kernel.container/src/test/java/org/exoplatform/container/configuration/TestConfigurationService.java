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
package org.exoplatform.container.configuration;

import junit.framework.TestCase;

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.monitor.jvm.JVMRuntimeInfo;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;

import java.io.FileOutputStream;
import java.net.URL;

/*
 * Thu, May 15, 2003 @   
 * @author: Tuan Nguyen
 * @version: $Id: TestConfigurationService.java 5799 2006-05-28 17:55:42Z geaz $
 * @since: 0.0
 */
public class TestConfigurationService extends TestCase
{

   public TestConfigurationService(String name)
   {
      super(name);
   }

   public void testXSDBadSchema() throws Exception
   {
      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();
      URL url = TestConfigurationService.class.getResource("../../../../configuration-bad-schema.xml");
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
      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();
      URL url = TestConfigurationService.class.getResource("../../../../configuration-no-schema.xml");
      Configuration conf = unmarshaller.unmarshall(url);
      assertNotNull(conf);
   }

   public void testMarshallAndUnmarshall() throws Exception
   {
      String basedir = System.getProperty("basedir");

      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();

      URL url = TestConfigurationService.class.getResource("../../../../configuration.xml");

      Object obj = unmarshaller.unmarshall(url);

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
   }

   protected String getDescription()
   {
      return "Test Configuration Service";
   }
}
