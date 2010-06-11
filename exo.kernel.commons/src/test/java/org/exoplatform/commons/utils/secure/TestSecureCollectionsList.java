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

import java.security.Permission;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

/**
 * @author <a href="mailto:natasha.vakulenko@gmail.com">Natasha Vakulenko</a>
 * @version $Revision$
 */

public class TestSecureCollectionsList extends AbstractSecureCollectionsTest
{
   private List<String> list = SecureCollections.secureList(new ArrayList<String>(), MODIFY_PERMISSION);

   /**
    * establishment of protected list prior to each test 
    */
   protected void setUp()
   {
      list = SecureCollections.secureList(new ArrayList<String>(), MODIFY_PERMISSION);
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               list.add("firstString");
               list.add("secondString");
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
      }
   }

   /**
    * cleaning protected list after each test
    */

   protected void tearDown()
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               list.clear();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
      }
   }

   public void testSecureListAddDenied()
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               list.add("string");
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (Exception e)
      {
      }
   }

   public void testSecureListAddPermitted()
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               list.add(0, "string");
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be permitted.");
      }
   }

   public void testSecureListClearDenied()
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               list.clear();
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (Exception e)
      {
      }
   }

   public void testSecureListIteratorPermitted()
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               ListIterator<String> it = list.listIterator();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be permitted.");
      }
   }

   public void testSecureListRemoveDenied()
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               list.remove(0);
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (Exception e)
      {
      }
   }

   public void testSecureIteratorPermitted()
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               Iterator<String> it = list.iterator();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be permitted.");
      }
   }
}
