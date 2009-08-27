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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.AbstractList;
import java.util.RandomAccess;

/**
 * <p>A lazy list and uses a {@link org.exoplatform.commons.utils.ListAccess} object to load
 * the elements of the list. The list is read only and any write access to the list will
 * not be permitted.</p>
 *
 * <p>The loading policy is based on a simple batch algorithm that loads the elements by batches.</p>
 *
 * <p>The list also keeps a cache of the retrieved elements. The cache use soft references to provide
 * eviction of the elements if necessary. When a soft reference is cleared and access is made to an
 * evicted element then the elements will be reloaded from the list access object.</p>
 *
 * <p>If the list access fails to load a batch by throwing a checked exception, it will cause the
 * list to throw an {@link IllegalStateException} wrapping the original exception. Any other kind
 * of non checked throwable will be propagated to the caller as it is.</p>
 *
 * <p>The implementation does not perform any kind of versionning check of the underlying data
 * and if the underlying list access changes the state it exposes the lazy list will not be aware
 * of it and may behave in an unexpected manner.</p>
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class LazyList<E> extends AbstractList<E> implements RandomAccess
{

   /** The batch size. */
   private final int batchSize;

   /** The pages. */
   private Batch[] batches;

   /** The list access. */
   private ListAccess<E> listAccess;

   public LazyList(ListAccess<E> listAccess, int batchSize)
   {
      if (listAccess == null)
      {
         throw new IllegalArgumentException("The list access object cannot be null");
      }
      if (batchSize < 1)
      {
         throw new IllegalArgumentException("No batch size < 1 is accepted");
      }

      //
      this.listAccess = listAccess;
      this.batchSize = batchSize;
   }

   public E get(int index)
   {
      int size = size();

      //
      if (index < 0)
      {
         throw new ArrayIndexOutOfBoundsException();
      }
      if (index >= size)
      {
         throw new ArrayIndexOutOfBoundsException();
      }

      //
      if (batches == null)
      {
         batches = new Batch[1 + size / batchSize];
      }

      //
      Object[] elements = null;
      int batchIndex = index / batchSize;
      Batch batch = batches[batchIndex];
      if (batch != null)
      {
         elements = batch.elements.get();
      }

      //
      if (elements == null)
      {
         try
         {
            int loadedIndex = batchIndex * batchSize;
            int loadedLength = Math.min(batchSize, size - loadedIndex);
            elements = listAccess.load(loadedIndex, loadedLength);
            batches[batchIndex] = new Batch(elements);
         }
         catch (Exception e)
         {
            e.printStackTrace();
            throw new IllegalStateException("Cannot load resource at index " + index, e);
         }
      }

      //
      return (E)elements[index % batchSize];
   }

   public int size()
   {
      try
      {
         return listAccess.getSize();
      }
      catch (Exception e)
      {
         throw new IllegalStateException("Cannot access resource size");
      }
   }

   private static class Batch
   {

      /** . */
      private final Reference<Object[]> elements;

      private Batch(Object[] elements)
      {
         this.elements = new SoftReference<Object[]>(elements);
      }
   }
}
