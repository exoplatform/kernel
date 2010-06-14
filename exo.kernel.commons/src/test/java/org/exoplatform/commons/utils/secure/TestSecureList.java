/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.commons.utils.secure;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.AccessControlException;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * @author <a href="mailto:natasha.vakulenko@gmail.com">Natasha Vakulenko</a>
 * @version $Id: 
 */

public class TestSecureList extends AbstractSecureCollectionsTest
{
   private List<String> list;

   @Override
   protected void setUp() throws PrivilegedActionException
   {
      // establishment of protected set prior to each test 
      list = SecureCollections.secureList(new ArrayList<String>(), MODIFY_PERMISSION);
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               list.add("firstString");
               list.add("secondString");
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (AccessControlException e)
      {
         // ok
      }
   }

   @Override
   protected void tearDown() throws PrivilegedActionException
   {
      // cleaning protected list after each test
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               list.clear();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (AccessControlException e)
      {
         // ok
      }
   }

   public void testSecureListAddDenied() throws PrivilegedActionException
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               list.add("string");
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (AccessControlException e)
      {
         // ok
      }
   }

   public void testSecureListAddPermitted() throws PrivilegedActionException
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               list.add(0, "string");
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (AccessControlException e)
      {
         fail("Modification should be permitted.");
      }
   }

   public void testSecureListClearDenied() throws PrivilegedActionException
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               list.clear();
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (AccessControlException e)
      {
         // ok
      }
   }

   public void testSecureListIteratorRemovePermitted() throws PrivilegedActionException
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               ListIterator<String> iterator = list.listIterator();
               iterator.next();
               iterator.remove();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (AccessControlException e)
      {
         fail("Modification should be permitted.");
      }
   }

   public void testSecureListRemoveDenied() throws PrivilegedActionException
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               list.remove(0);
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (AccessControlException e)
      {
         // ok
      }
   }

   public void testSecureIteratorPermitted() throws PrivilegedActionException
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws AccessControlException
            {
               Iterator<String> it = list.iterator();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (AccessControlException e)
      {
         fail("Modification should be permitted.");
      }
   }
}
