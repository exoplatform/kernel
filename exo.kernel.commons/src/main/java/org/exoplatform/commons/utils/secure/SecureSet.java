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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * SecureSet is a wrapper over given Set instance providing additional security check. 
 * To be able to modify set, invoking code must have the same permission as given in SecureSet's 
 * constructor or {@link AllPermission}.
 * 
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: SecureSet.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class SecureSet<E> implements Set<E>
{

   // base set
   private final Set<E> set;

   // required permission
   private final Permission permission;

   /**
    * Constructs a SecureSet using new {@link HashSet} inside. 
    * @param permission
    *    Permission that will be required for modificaiton.
    */
   public SecureSet(Permission permission)
   {
      super();
      this.set = new HashSet<E>();
      this.permission = permission;
   }

   /**
    * Constructs a SecureSet using new given {@link Set} instance.
    * @param set
    *    Set, to be based on
    * @param permission
    *    Permission that will be required for modificaiton.
    */
   public SecureSet(Set<E> set, Permission permission)
   {
      super();
      this.set = set;
      this.permission = permission;
   }

   public boolean add(E e)
   {
      checkPermission();
      return set.add(e);
   }

   public boolean addAll(Collection<? extends E> elements)
   {
      checkPermission();
      return set.addAll(elements);
   }

   public void clear()
   {
      checkPermission();
      set.clear();
   }

   public boolean contains(Object o)
   {
      return set.contains(o);
   }

   public boolean containsAll(Collection<?> coll)
   {
      return set.containsAll(coll);
   }

   @Override
   public boolean equals(Object o)
   {
      return o == this || set.equals(o);
   }

   @Override
   public int hashCode()
   {
      return set.hashCode();
   }

   public boolean isEmpty()
   {
      return set.isEmpty();
   }

   public Iterator<E> iterator()
   {
      return new Iterator<E>()
      {
         Iterator<? extends E> i = set.iterator();

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
            checkPermission();
            i.remove();
         }
      };
   }

   public boolean remove(Object o)
   {
      checkPermission();
      return set.remove(o);
   }

   public boolean removeAll(Collection<?> pds)
   {
      checkPermission();
      return set.removeAll(pds);
   }

   public boolean retainAll(Collection<?> pds)
   {
      checkPermission();
      return set.retainAll(pds);
   }

   public int size()
   {
      return set.size();
   }

   public Object[] toArray()
   {
      return set.toArray();
   }

   public <T> T[] toArray(T[] a)
   {
      return set.toArray(a);
   }

   @Override
   public String toString()
   {
      return set.toString();
   }

   /**
    * Checks if code has a permission
    */
   private void checkPermission()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(permission);
      }
   }
}
