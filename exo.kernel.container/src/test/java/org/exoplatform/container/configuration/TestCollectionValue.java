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

import org.exoplatform.container.RootContainer;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParam;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.xml.object.XMLCollection;
import org.exoplatform.xml.object.XMLField;
import org.exoplatform.xml.object.XMLObject;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestCollectionValue extends AbstractProfileTest
{

   public void testNoProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection();
      Collection coll = xc.getCollection();
      assertEquals(1, coll.size());
   }

   public void testNoProfileKernel() throws Exception
   {
      List l = getCollection();
      assertEquals(Arrays.asList("manager"), l);
   }

   public void testFooProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection("foo");
      Collection coll = xc.getCollection();
      assertEquals(3, coll.size());
   }

   public void testFooProfileKernel() throws Exception
   {
      List l = getCollection("foo");
      assertEquals(Arrays.asList("manager", "foo_manager", "foo_bar_manager"), l);
   }

   public void testBarProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection("bar");
      Collection coll = xc.getCollection();
      assertEquals(2, coll.size());
   }

   public void testBarProfileKernel() throws Exception
   {
      List l = getCollection("bar");
      assertEquals(Arrays.asList("manager", "foo_bar_manager"), l);
   }

   public void testFooBarProfile() throws Exception
   {
      XMLCollection xc = getConfiguredCollection("foo", "bar");
      Collection coll = xc.getCollection();
      assertEquals(3, coll.size());
   }

   public void testFooBarProfileKernel() throws Exception
   {
      List l = getCollection("foo", "bar");
      assertEquals(Arrays.asList("manager", "foo_manager", "foo_bar_manager"), l);
   }

   private XMLCollection getConfiguredCollection(String... profiles)
      throws Exception
   {
      Configuration config = getConfiguration("collection-configuration.xml", profiles);
      Component a = config.getComponent(InitParamsHolder.class.getName());
      ObjectParameter op = a.getInitParams().getObjectParam("test.configuration");
      XMLObject o = op.getXMLObject();
      XMLField xf = o.getField("role");
      return xf.getCollection();
   }

   private List getCollection(String... profiles)
      throws Exception
   {
      RootContainer config = getKernel("collection-configuration.xml", profiles);
      InitParamsHolder holder = (InitParamsHolder)config.getComponentInstanceOfType(InitParamsHolder.class);
      InitParams params = holder.getParams();
      ObjectParameter op = params.getObjectParam("test.configuration");
      ConfigParam cp = (ConfigParam)op.getObject();
      return cp.getRole();
   }
}
