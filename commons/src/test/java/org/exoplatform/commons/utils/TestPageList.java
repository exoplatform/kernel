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
package org.exoplatform.commons.utils;

import junit.framework.TestCase;

import org.exoplatform.commons.exception.ExoMessageException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestPageList extends TestCase
{

   public TestPageList()
   {
   }

   public TestPageList(String s)
   {
      super(s);
   }

   private final List<String> l = Collections.unmodifiableList(Arrays.asList("0", "1", "2", "3", "4", "5", "6"));

   public void testObjectPageList() throws Exception
   {
      ObjectPageList<String> list = new ObjectPageList<String>(l, 3);
      testBehavior(list);
   }

   public void testLazyList() throws Exception
   {
      ListAccess<String> list = new ListAccess<String>()
      {
         public String[] load(int index, int length) throws Exception, IllegalArgumentException
         {
            return l.subList(index, index + length).toArray(new String[length]);
         }

         public int getSize() throws Exception
         {
            return l.size();
         }
      };
      testBehavior(new LazyPageList<String>(list, 3));
   }

   public void testBehavior(PageList list) throws Exception
   {

      // Initial state
      assertEquals(0, list.getFrom());
      assertEquals(3, list.getTo());
      assertEquals(1, list.getCurrentPage());
      assertEquals(3, list.getAvailablePage());
      assertEquals(7, list.getAvailable());

      //
      List<String> s;

      //
      s = list.getPage(1);
      assertNotNull(s);
      assertEquals(3, s.size());
      assertEquals("0", s.get(0));
      assertEquals("1", s.get(1));
      assertEquals("2", s.get(2));
      assertEquals(0, list.getFrom());
      assertEquals(3, list.getTo());
      assertEquals(1, list.getCurrentPage());
      assertEquals(3, list.getAvailablePage());
      assertEquals(7, list.getAvailable());

      //
      s = list.getPage(2);
      assertNotNull(s);
      assertEquals(3, s.size());
      assertEquals("3", s.get(0));
      assertEquals("4", s.get(1));
      assertEquals("5", s.get(2));
      assertEquals(3, list.getFrom());
      assertEquals(6, list.getTo());
      assertEquals(2, list.getCurrentPage());
      assertEquals(3, list.getAvailablePage());
      assertEquals(7, list.getAvailable());

      //
      s = list.getPage(3);
      assertNotNull(s);
      assertEquals(1, s.size());
      assertEquals("6", s.get(0));
      assertEquals(6, list.getFrom());
      assertEquals(7, list.getTo());
      assertEquals(3, list.getCurrentPage());
      assertEquals(3, list.getAvailablePage());
      assertEquals(7, list.getAvailable());

      //
      try
      {
         list.getPage(4);
         fail();
      }
      catch (ExoMessageException e)
      {
      }

      //
      try
      {
         list.getPage(0);
         fail();
      }
      catch (ExoMessageException e)
      {
      }

      //
      try
      {
         list.getPage(-1);
         fail();
      }
      catch (ExoMessageException e)
      {
      }
   }
}
