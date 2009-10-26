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

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestLazyList extends TestCase
{

   public TestLazyList()
   {
   }

   public TestLazyList(String s)
   {
      super(s);
   }

   SimpleListAccess<Integer> access = new SimpleListAccess<Integer>();

   LazyList<Integer> list = new LazyList<Integer>(access, 3);

   @Override
   protected void setUp() throws Exception
   {
      access = new SimpleListAccess<Integer>();
      access.add(0);
      access.add(1);
      access.add(2);
      access.add(3);
      access.add(4);
      access.add(5);
      access.add(6);
      access.add(7);
      access.add(8);
      access.add(9);
      list = new LazyList<Integer>(access, 3);
   }

   public void loadFailure()
   {
      try
      {
         new LazyList(new ListAccess()
         {
            public Object[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               throw new Exception();
            }

            public int getSize() throws Exception
            {
               throw new Exception();
            }
         }, 3).get(4);
         fail();
      }
      catch (IllegalStateException ignore)
      {
      }
      try
      {
         new LazyList(new ListAccess()
         {
            public Object[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               throw new Error();
            }

            public int getSize()
            {
               throw new Error();
            }
         }, 3).get(4);
         fail();
      }
      catch (Error ignore)
      {
      }
      try
      {
         new LazyList(new ListAccess()
         {
            public Object[] load(int index, int length) throws Exception, IllegalArgumentException
            {
               throw new ClassCastException();
            }

            public int getSize()
            {
               throw new ClassCastException();
            }
         }, 3).get(4);
         fail();
      }
      catch (ClassCastException ignore)
      {
      }
   }

   public void testIllegalArgumentException()
   {
      try
      {
         new LazyList(null, 3);
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }
      try
      {
         new LazyList(access, 0);
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }
      try
      {
         new LazyList(access, -1);
         fail();
      }
      catch (IllegalArgumentException ignore)
      {
      }
   }

   public void testSize()
   {
      assertEquals(10, list.size());
   }

   public void testOutOfBounds()
   {
      try
      {
         list.get(-1);
         fail();
      }
      catch (ArrayIndexOutOfBoundsException ignore)
      {
      }
      try
      {
         list.get(10);
         fail();
      }
      catch (ArrayIndexOutOfBoundsException ignore)
      {
      }
   }

   public void testLoading()
   {
      // Load the first element of the second batch
      assertEquals(3, (int)list.get(3));
      assertEquals(3, (int)access.indexes.removeFirst());
      assertEquals(3, (int)access.lengths.removeFirst());
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(5, (int)list.get(5));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(4, (int)list.get(4));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(3, (int)list.get(3));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());

      // Load the first element of the last batch
      assertEquals(9, (int)list.get(9));
      assertEquals(9, (int)access.indexes.removeFirst());
      assertEquals(1, (int)access.lengths.removeFirst());
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(9, (int)list.get(9));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());

      // Load the last element of the third batch
      assertEquals(8, (int)list.get(8));
      assertEquals(6, (int)access.indexes.removeFirst());
      assertEquals(3, (int)access.lengths.removeFirst());
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(7, (int)list.get(7));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(6, (int)list.get(6));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(8, (int)list.get(8));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());

      // Load the second element of the first batch
      assertEquals(1, (int)list.get(1));
      assertEquals(0, (int)access.indexes.removeFirst());
      assertEquals(3, (int)access.lengths.removeFirst());
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(0, (int)list.get(0));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(2, (int)list.get(2));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
      assertEquals(1, (int)list.get(1));
      assertTrue(access.indexes.isEmpty());
      assertTrue(access.lengths.isEmpty());
   }

   private static class SimpleListAccess<E> extends ArrayList<E> implements ListAccess<E>
   {

      private LinkedList<Integer> indexes = new LinkedList<Integer>();

      private LinkedList<Integer> lengths = new LinkedList<Integer>();

      public E[] load(int index, int length) throws Exception, IllegalArgumentException
      {
         indexes.addFirst(index);
         lengths.addFirst(length);
         try
         {
            return (E[])subList(index, index + length).toArray();
         }
         catch (ArrayIndexOutOfBoundsException e)
         {
            throw new IllegalArgumentException(e);
         }
      }

      public int getSize()
      {
         return size();
      }
   }
}
