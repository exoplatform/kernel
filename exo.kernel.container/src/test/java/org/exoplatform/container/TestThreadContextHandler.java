/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import org.exoplatform.container.component.ThreadContext;
import org.exoplatform.container.component.ThreadContextHandler;
import org.exoplatform.container.component.ThreadContextHolder;
import org.exoplatform.container.jmx.AbstractTestContainer;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestThreadContextHandler extends AbstractTestContainer
{
   
   private ThreadContextHandler handler;
   
   private TestHolder holder;
   
   @Override
   protected void setUp() throws Exception
   {
      ExoContainer container = createRootContainer("thread-context-handler-configuration.xml");
      this.handler = new ThreadContextHandler(container);
      this.holder = (TestHolder)container.getComponentInstanceOfType(TestHolder.class);
   }
   
   public void testTLNullValue()
   {
      assertNull(holder.tl.get());
      handler.store();
      holder.tl.set("foo");
      handler.push();
      assertNull(holder.tl.get());
      handler.restore();
      assertEquals("foo", holder.tl.get());
   }
   
   public void testTLNotNullValue()
   {
      holder.tl.set("foo");
      handler.store();
      assertEquals("foo", holder.tl.get());
      holder.tl.set("foo2");
      handler.push();
      assertEquals("foo", holder.tl.get());
      handler.restore();
      assertEquals("foo2", holder.tl.get());
   }
   
   public static class TestHolder implements ThreadContextHolder
   {
      public ThreadLocal<String> tl = new ThreadLocal<String>();
      public ThreadContext getThreadContext()
      {
         return new ThreadContext(tl);
      }
   }
}
