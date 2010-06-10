/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.commons.utils.secure;

import java.security.AllPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * SecureList is a wrapper over given List instance providing additional security check. 
 * To be able to modify this list, invoking code must have permission given in SecureList's 
 * constructor or {@link AllPermission}.
 *  
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: SecureList.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class SecureList<E> implements List<E>
{

   // base list
   private final List<E> list;

   // required permission
   private final Permission permission;

   public SecureList(Permission permission)
   {
      super();
      this.list = new ArrayList<E>();
      this.permission = permission;
   }

   public SecureList(List<E> list, Permission permission)
   {
      super();
      this.list = list;
      this.permission = permission;
   }

   public void add(int index, E pd)
   {
      checkPermissions();
      list.add(index, pd);
   }

   public boolean add(E pd)
   {
      checkPermissions();
      return list.add(pd);
   }

   public boolean addAll(Collection<? extends E> pds)
   {
      checkPermissions();
      return list.addAll(pds);
   }

   public boolean addAll(int index, Collection<? extends E> pds)
   {
      checkPermissions();
      return list.addAll(index, pds);
   }

   public void clear()
   {
      checkPermissions();
      list.clear();
   }

   public boolean contains(Object o)
   {
      return list.contains(o);
   }

   public boolean containsAll(Collection<?> coll)
   {
      return list.containsAll(coll);
   }

   @Override
   public boolean equals(Object o)
   {
      return o == this || list.equals(o);
   }

   public E get(int index)
   {
      return list.get(index);
   }

   @Override
   public int hashCode()
   {
      return list.hashCode();
   }

   public int indexOf(Object o)
   {
      return list.indexOf(o);
   }

   public boolean isEmpty()
   {
      return list.isEmpty();
   }

   public Iterator<E> iterator()
   {
      return new Iterator<E>()
      {
         Iterator<? extends E> i = list.iterator();

         public boolean hasNext()
         {
            return i.hasNext();
         }

         public E next()
         {
            return i.next();
         }

         public void remove()
         {
            checkPermissions();
            i.remove();
         }
      };
   }

   public int lastIndexOf(Object o)
   {
      return list.lastIndexOf(o);
   }

   public ListIterator<E> listIterator()
   {
      return listIterator(0);
   }

   public ListIterator<E> listIterator(final int index)
   {
      return new ListIterator<E>()
      {
         ListIterator<E> li = list.listIterator(index);

         public void add(E pd)
         {
            checkPermissions();
            li.add(pd);
         }

         public boolean hasNext()
         {
            return li.hasNext();
         }

         public boolean hasPrevious()
         {
            return li.hasPrevious();
         }

         public E next()
         {
            return li.next();
         }

         public int nextIndex()
         {
            return li.nextIndex();
         }

         public E previous()
         {
            return li.previous();
         }

         public int previousIndex()
         {
            return li.previousIndex();
         }

         public void remove()
         {
            checkPermissions();
            li.remove();
         }

         public void set(E pd)
         {
            checkPermissions();
            li.set(pd);
         }
      };
   }

   public E remove(int index)
   {
      checkPermissions();
      return list.remove(index);
   }

   public boolean remove(Object o)
   {
      checkPermissions();
      return list.remove(o);
   }

   public boolean removeAll(Collection<?> pds)
   {
      checkPermissions();
      return list.removeAll(pds);
   }

   public boolean retainAll(Collection<?> pds)
   {
      checkPermissions();
      return list.retainAll(pds);
   }

   public E set(int index, E pd)
   {
      checkPermissions();
      return list.set(index, pd);
   }

   public int size()
   {
      return list.size();
   }

   public List<E> subList(int fromIndex, int toIndex)
   {
      return new SecureList<E>(list.subList(fromIndex, toIndex), permission);
   }

   public Object[] toArray()
   {
      return list.toArray();
   }

   public <T> T[] toArray(T[] a)
   {
      return list.toArray(a);
   }

   @Override
   public String toString()
   {
      return list.toString();
   }

   /**
    * Checks if code has a permission
    */
   private void checkPermissions()
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
      {
         sm.checkPermission(permission);
      }
   }

}
