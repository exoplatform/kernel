/*
 * Copyright (C) 2014 eXo Platform SAS.
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
package org.exoplatform.services.cache.concurrent;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.rpc.jgv3.RPCServiceImpl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

/**
 * This is the unit test for the class {@link SimpleReplicatedExoCache}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestSimpleReplicatedExoCache
{
   private PortalContainer container;
   private RPCServiceImpl service1, service2;

   private SimpleReplicatedExoCache<String, String> cache1, cache2;

   @Before
   public void init() throws Exception
   {
      container = PortalContainer.getInstance();
      ConfigurationManager configManager = container.getComponentInstanceOfType(ConfigurationManager.class);

      InitParams params = new InitParams();
      ValueParam paramConf = new ValueParam();
      paramConf.setName("jgroups-configuration");
      paramConf.setValue("jar:/conf/portal/udp.xml");
      params.addParameter(paramConf);
      service1 = new RPCServiceImpl(container.getContext(), params, configManager);
      service2 = new RPCServiceImpl(container.getContext(), params, configManager);
      cache1 = new SimpleReplicatedExoCache<String, String>(container.getContext(), service1);
      cache2 = new SimpleReplicatedExoCache<String, String>(container.getContext(), service2);
      cache1.setName("TestSimpleReplicatedExoCache");
      cache2.setName("TestSimpleReplicatedExoCache");
      service1.start();
      service2.start();
   }

   @After
   public void destroy() throws Exception
   {
      service1.stop();
      service2.stop();
   }

   @Test
   public void testPut() throws Exception
   {
      Assert.assertEquals(0, cache1.getCacheSize());
      Assert.assertEquals(0, cache2.getCacheSize());
      cache1.put("a", "value");
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      for (int i = 0; i < 5; i++)
      {
         if (cache2.getCacheSize() > 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals("value", cache2.get("a"));
      // Make sure that if the value is the same we don't replace it on other nodes
      String value1 = new String("value");
      Object value2 = cache2.get("a");
      cache1.put("a", value1);
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      Assert.assertTrue(value1 == cache1.get("a"));
      for (int i = 0; i < 5; i++)
      {
         Assert.assertEquals("value", cache2.get("a"));
         Assert.assertTrue(value2 == cache2.get("a"));
         Thread.sleep(200);
      }
      Assert.assertEquals("value", cache2.get("a"));
      Assert.assertTrue(value2 == cache2.get("a"));
      // Make sure that if the value has changed we replace it on other nodes
      cache1.put("a", "value2");
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals("value2", cache1.get("a"));
      for (int i = 0; i < 5; i++)
      {
         if (cache2.get("a").equals("value2"))
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals("value2", cache2.get("a"));
   }

   @Test
   public void testPutMap() throws Exception
   {
      Assert.assertEquals(0, cache1.getCacheSize());
      Assert.assertEquals(0, cache2.getCacheSize());
      cache1.putMap(Collections.singletonMap("a", "value"));
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      for (int i = 0; i < 5; i++)
      {
         if (cache2.getCacheSize() > 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals("value", cache2.get("a"));
      // Make sure that if the value is the same we don't replace it on other nodes
      String value1 = new String("value");
      Object value2 = cache2.get("a");
      cache1.putMap(Collections.singletonMap("a", value1));
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      Assert.assertTrue(value1 == cache1.get("a"));
      for (int i = 0; i < 5; i++)
      {
         Assert.assertEquals("value", cache2.get("a"));
         Assert.assertTrue(value2 == cache2.get("a"));
         Thread.sleep(200);
      }
      Assert.assertEquals("value", cache2.get("a"));
      Assert.assertTrue(value2 == cache2.get("a"));
      // Make sure that if the value has changed we replace it on other nodes
      cache1.putMap(Collections.singletonMap("a",  "value2"));
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals("value2", cache1.get("a"));
      for (int i = 0; i < 5; i++)
      {
         if (cache2.get("a").equals("value2"))
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals("value2", cache2.get("a"));
   }

   @Test
   public void testRemove() throws Exception
   {
      Assert.assertEquals(0, cache1.getCacheSize());
      Assert.assertEquals(0, cache2.getCacheSize());
      cache1.put("a", "value");
      Assert.assertEquals(1, cache1.getCacheSize());
      for (int i = 0; i < 5; i++)
      {
         if (cache2.getCacheSize() > 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals("value", cache1.remove("a"));
      Assert.assertEquals(0, cache1.getCacheSize());
      Assert.assertNull(cache1.get("a"));
      for (int i = 0; i < 5; i++)
      {
         if (cache2.getCacheSize() == 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(0, cache2.getCacheSize());
      Assert.assertNull(cache2.get("a"));
      Assert.assertNull(cache1.remove("a"));
   }

   @Test
   public void testClearCache() throws Exception
   {
      Assert.assertEquals(0, cache1.getCacheSize());
      Assert.assertEquals(0, cache2.getCacheSize());
      cache1.put("a", "value");
      cache1.put("b", "value");
      cache1.put("c", "value");
      Assert.assertEquals(3, cache1.getCacheSize());
      for (int i = 0; i < 5; i++)
      {
         if (cache2.getCacheSize() == 3)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(3, cache2.getCacheSize());
      cache1.clearCache();
      Assert.assertEquals(0, cache1.getCacheSize());
      Assert.assertNull(cache1.get("a"));
      Assert.assertNull(cache1.get("b"));
      Assert.assertNull(cache1.get("c"));
      for (int i = 0; i < 5; i++)
      {
         if (cache2.getCacheSize() == 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(0, cache2.getCacheSize());
      Assert.assertNull(cache2.get("a"));
      Assert.assertNull(cache2.get("b"));
      Assert.assertNull(cache2.get("c"));
   }

   @Test
   public void testIsolation() throws Exception
   {
      SimpleReplicatedExoCache<String, String> cache3 = new SimpleReplicatedExoCache<String, String>(container.getContext(), service1);
      SimpleReplicatedExoCache<String, String> cache4 = new SimpleReplicatedExoCache<String, String>(container.getContext(), service2);
      cache3.setName("TestSimpleReplicatedExoCache.testIsolation");
      cache4.setName("TestSimpleReplicatedExoCache.testIsolation");
      Assert.assertEquals(0, cache1.getCacheSize());
      Assert.assertEquals(0, cache2.getCacheSize());
      Assert.assertEquals(0, cache3.getCacheSize());
      Assert.assertEquals(0, cache4.getCacheSize());
      cache1.put("a", "value");
      Assert.assertEquals(1, cache1.getCacheSize());
      for (int i = 0; i < 5; i++)
      {
         if (cache2.getCacheSize() > 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals(0, cache3.getCacheSize());
      Assert.assertEquals(0, cache4.getCacheSize());
      cache4.put("a", "value2");
      Assert.assertEquals(1, cache1.getCacheSize());
      for (int i = 0; i < 5; i++)
      {
         if (cache3.getCacheSize() > 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals(1, cache3.getCacheSize());
      Assert.assertEquals(1, cache4.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      Assert.assertEquals("value", cache2.get("a"));
      Assert.assertEquals("value2", cache3.get("a"));
      Assert.assertEquals("value2", cache4.get("a"));
      Assert.assertEquals("value2", cache4.remove("a"));
      for (int i = 0; i < 5; i++)
      {
         if (cache3.getCacheSize() == 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals(0, cache3.getCacheSize());
      Assert.assertEquals(0, cache4.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      Assert.assertEquals("value", cache2.get("a"));
      Assert.assertNull(cache3.get("a"));
      Assert.assertNull(cache4.get("a"));
      cache4.putMap(Collections.singletonMap("a", "value2"));
      for (int i = 0; i < 5; i++)
      {
         if (cache3.getCacheSize() > 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals(1, cache3.getCacheSize());
      Assert.assertEquals(1, cache4.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      Assert.assertEquals("value", cache2.get("a"));
      Assert.assertEquals("value2", cache3.get("a"));
      Assert.assertEquals("value2", cache4.get("a"));
      cache4.clearCache();
      for (int i = 0; i < 5; i++)
      {
         if (cache3.getCacheSize() == 0)
            break;
         Thread.sleep(200);
      }
      Assert.assertEquals(1, cache1.getCacheSize());
      Assert.assertEquals(1, cache2.getCacheSize());
      Assert.assertEquals(0, cache3.getCacheSize());
      Assert.assertEquals(0, cache4.getCacheSize());
      Assert.assertEquals("value", cache1.get("a"));
      Assert.assertEquals("value", cache2.get("a"));
      Assert.assertNull(cache3.get("a"));
      Assert.assertNull(cache4.get("a"));
   }
}
