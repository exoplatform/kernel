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

import java.util.Arrays;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestThreadContextHandler extends AbstractTestContainer
{

   private ThreadContextHandler handler;

   private TestHolder holder;
   private TestHolder6 holder6;

   @Override
   protected void setUp() throws Exception
   {
      ExoContainer container = createRootContainer("thread-context-handler-configuration.xml");
      this.handler = new ThreadContextHandler(container);
      this.holder = container.getComponentInstanceOfType(TestHolder.class);
      this.holder6 = container.getComponentInstanceOfType(TestHolder6.class);
   }

   public void testTLNullValue()
   {
      assertNull(holder.tl.get());
      assertNull(holder6.tl.get());
      assertNull(holder6.tl2.get());
      assertNull(holder6.tl3.get());
      handler.store();
      holder.tl.set("foo");
      holder6.tl.set("foo");
      holder6.tl2.set("foo");
      holder6.tl3.set("foo");
      handler.push();
      assertNull(holder.tl.get());
      assertNull(holder6.tl.get());
      assertNull(holder6.tl2.get());
      assertNull(holder6.tl3.get());
      handler.restore();
      assertEquals("foo", holder.tl.get());
      assertEquals("foo", holder6.tl.get());
      assertEquals("foo", holder6.tl2.get());
      assertEquals("foo", holder6.tl3.get());
   }

   public void testTLNotNullValue()
   {
      holder.tl.set("foo");
      holder6.tl.set("foo");
      holder6.tl2.set("foo");
      holder6.tl3.set("foo");
      handler.store();
      assertEquals("foo", holder.tl.get());
      assertEquals("foo", holder6.tl.get());
      assertEquals("foo", holder6.tl2.get());
      assertEquals("foo", holder6.tl3.get());
      holder.tl.set("foo2");
      holder6.tl.set("foo2");
      holder6.tl2.set("foo2");
      holder6.tl3.set("foo2");
      handler.push();
      assertEquals("foo", holder.tl.get());
      assertEquals("foo", holder6.tl.get());
      assertEquals("foo", holder6.tl2.get());
      assertEquals("foo", holder6.tl3.get());
      handler.restore();
      assertEquals("foo2", holder.tl.get());
      assertEquals("foo2", holder6.tl.get());
      assertEquals("foo2", holder6.tl2.get());
      assertEquals("foo2", holder6.tl3.get());
   }

   public static class TestHolder implements ThreadContextHolder
   {
      public ThreadLocal<String> tl = new ThreadLocal<String>();

      public ThreadContext getThreadContext()
      {
         return new ThreadContext(tl);
      }
   }

   public static class TestHolder2 implements ThreadContextHolder
   {
      public ThreadContext getThreadContext()
      {
         return null;
      }
   }

   public static class TestHolder3 implements ThreadContextHolder
   {
      public ThreadContext getThreadContext()
      {
         return new ThreadContext();
      }
   }

   public static class TestHolder4 implements ThreadContextHolder
   {
      public ThreadContext getThreadContext()
      {
         return new ThreadContext(null);
      }
   }

   public static class TestHolder5 implements ThreadContextHolder
   {
      public ThreadLocal<String> tl = new ThreadLocal<String>();

      public ThreadContext getThreadContext()
      {
         return new ThreadContext(tl, null, tl);
      }
   }

   public static class TestHolder6 implements ThreadContextHolder
   {
      public ThreadLocal<String> tl = new ThreadLocal<String>();
      public ThreadLocal<String> tl2 = new ThreadLocal<String>();
      public ThreadLocal<String> tl3 = new ThreadLocal<String>();

      public ThreadContext getThreadContext()
      {
         return ThreadContext.merge(Arrays.asList(new ThreadContext(tl), null, new ThreadContext(), new ThreadContext(
            null), new ThreadContext(tl2, null, tl3)));
      }
   }
}
