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
package org.exoplatform.services.listener;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;

/**
 * Created by The eXo Platform SAS Author : Chung Nguyen
 * nguyenchung136@yahoo.com Feb 13, 2006
 */
public class TestListenerService extends TestCase
{

   private ListenerService service_;

   public TestListenerService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      PortalContainer manager = PortalContainer.getInstance();
      service_ = (ListenerService)manager.getComponentInstanceOfType(ListenerService.class);
   }

   public void testListener() throws Exception
   {
      assertTrue(service_ != null);

      BeanHandler handler = new BeanHandler();
      handler.setValue("thuan");
   }

   public class BeanHandler
   {

      private Bean bean;

      public BeanHandler() throws Exception
      {
         bean = new Bean("test", "listener1");
         service_.broadcast(new Event<BeanHandler, Bean>("new.bean", this, bean));
      }

      public void setValue(String value) throws Exception
      {
         bean.value = value;
         service_.broadcast(new Event<BeanHandler, Bean>("set.value.bean", this, bean));
      }

   }

   static class Bean
   {

      @SuppressWarnings("unused")
      private String name;

      private String value;

      Bean(String n, String v)
      {
         name = n;
         value = v;
      }

      String getValue()
      {
         return value;
      }

      String getName()
      {
         return name;
      }
   }

}
