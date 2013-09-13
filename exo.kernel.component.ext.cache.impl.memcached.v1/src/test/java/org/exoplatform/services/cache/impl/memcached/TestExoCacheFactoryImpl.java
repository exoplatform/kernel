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
package org.exoplatform.services.cache.impl.memcached;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.cache.ExoCache;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestExoCacheFactoryImpl extends TestCase
{
   private PortalContainer pc;

   @Override
   protected void setUp() throws Exception
   {
      pc = PortalContainer.getInstance();
   }

   public void testArguments() throws Exception
   {
      try
      {
         new ExoCacheFactoryImpl(pc.getContext(), null);
         fail("An IllegalArgumentException should occur");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      try
      {
         new ExoCacheFactoryImpl(pc.getContext(), new InitParams());
         fail("An IllegalArgumentException should occur");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      InitParams params;
      try
      {
         params = new InitParams();
         ValueParam vp = new ValueParam();
         vp.setName(ExoCacheFactoryImpl.MEMCACHED_LOCATIONS);
         params.addParam(vp);
         new ExoCacheFactoryImpl(pc.getContext(), params);
         fail("An IllegalArgumentException should occur");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      try
      {
         params = new InitParams();
         ValueParam vp = new ValueParam();
         vp.setName(ExoCacheFactoryImpl.MEMCACHED_LOCATIONS);
         vp.setValue("");
         params.addParam(vp);
         new ExoCacheFactoryImpl(pc.getContext(), params);
         fail("An IllegalArgumentException should occur");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      try
      {
         params = new InitParams();
         ValueParam vp = new ValueParam();
         vp.setName(ExoCacheFactoryImpl.MEMCACHED_LOCATIONS);
         vp.setValue("localhost:11211");
         params.addParam(vp);
         new ExoCacheFactoryImpl(pc.getContext(), params);
      }
      catch (Exception e)
      {
         fail("No exception was expected");
      }
      try
      {
         params = new InitParams();
         ValueParam vp = new ValueParam();
         vp.setName(ExoCacheFactoryImpl.MEMCACHED_LOCATIONS);
         vp.setValue("localhost:11211");
         params.addParam(vp);
         ObjectParameter op = new ObjectParameter();
         op.setName(ExoCacheFactoryImpl.CONNECTION_FACTORY_CREATOR);
         op.setObject(new Object());
         params.addParam(op);
         new ExoCacheFactoryImpl(pc.getContext(), params);
         fail("An IllegalArgumentException should occur");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      try
      {
         params = new InitParams();
         ValueParam vp = new ValueParam();
         vp.setName(ExoCacheFactoryImpl.MEMCACHED_LOCATIONS);
         vp.setValue("localhost:11211");
         params.addParam(vp);
         ObjectParameter op = new ObjectParameter();
         op.setName(ExoCacheFactoryImpl.CONNECTION_FACTORY_CREATOR);
         op.setObject(new BinaryConnectionFactoryCreator());
         params.addParam(op);
         new ExoCacheFactoryImpl(pc.getContext(), params);
      }
      catch (Exception e)
      {
         fail("No exception was expected");
      }
      try
      {
         params = new InitParams();
         ValueParam vp = new ValueParam();
         vp.setName(ExoCacheFactoryImpl.MEMCACHED_LOCATIONS);
         vp.setValue("localhost:11211");
         params.addParam(vp);
         ObjectParameter op = new ObjectParameter();
         op.setName(ExoCacheFactoryImpl.CONNECTION_FACTORY_CREATOR);
         op.setObject(new BinaryConnectionFactoryCreator());
         params.addParam(op);
         ValueParam vp2 = new ValueParam();
         vp2.setName(ExoCacheFactoryImpl.DEFAULT_EXPIRATION_TIMEOUT);
         vp2.setValue("foo");
         params.addParam(vp2);
         new ExoCacheFactoryImpl(pc.getContext(), params);
         fail("An exception was expected");
      }
      catch (Exception e)
      {
         //OK
      }
      try
      {
         params = new InitParams();
         ValueParam vp = new ValueParam();
         vp.setName(ExoCacheFactoryImpl.MEMCACHED_LOCATIONS);
         vp.setValue("localhost:11211");
         params.addParam(vp);
         ObjectParameter op = new ObjectParameter();
         op.setName(ExoCacheFactoryImpl.CONNECTION_FACTORY_CREATOR);
         op.setObject(new BinaryConnectionFactoryCreator());
         params.addParam(op);
         ValueParam vp2 = new ValueParam();
         vp2.setName(ExoCacheFactoryImpl.DEFAULT_EXPIRATION_TIMEOUT);
         vp2.setValue("1000");
         params.addParam(vp2);
         new ExoCacheFactoryImpl(pc.getContext(), params);
      }
      catch (Exception e)
      {
         fail("No exception was expected");
      }
   }

   public void testCacheFactory()
   {
      CacheService service_ =
         (CacheService)pc.getComponentInstanceOfType(CacheService.class);
      @SuppressWarnings("rawtypes")
      ExoCache cache = service_.getCacheInstance("myCache");
      assertTrue("expect an instance of MCExoCache but was " + cache, cache instanceof MCExoCache);
   }
}
