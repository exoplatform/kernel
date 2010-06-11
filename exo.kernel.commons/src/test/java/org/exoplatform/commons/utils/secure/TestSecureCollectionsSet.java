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

import java.security.Permissions;
import java.security.PrivilegedExceptionAction;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;

/**
 * @author <a href="mailto:natasha.vakulenko@gmail.com">Natasha Vakulenko</a>
 * @version $Revision$
 */

public class TestSecureCollectionsSet extends AbstractSecureCollectionsTest
{
   private Set<String> set;

   /**
    * establishment of protected set prior to each test 
    */
   protected void setUp()
   {
      set = SecureCollections.secureSet(new HashSet<String>(), MODIFY_PERMISSION);
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               set.add("firstString");
               set.add("secondString");
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
      }
   }

   /**
    * cleaning protected set after each test
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
               set.clear();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
      }
   }

   public void testSecureSetAddPermitted()
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               set.add("string");
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be permitted.");
      }
   }

   public void testSecureSetAddDenied()
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               set.add("string");
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (Exception e)
      {
      }
   }

   public void testSecureSetRemoveDenied()
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               set.remove(0);
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (Exception e)
      {
      }
   }

   public void testSecureSetRemovePermitted()
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               set.remove(0);
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be permitted.");
      }
   }

   public void testSecureSetIteratorPermitted()
   {
      try
      {
         // giving MODIFY_PERMISSION
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               Iterator<String> iterator = set.iterator();
               return null;
            }
         }, MODIFY_PERMISSION);
      }
      catch (Exception e)
      {
         fail("Modification should be permitted.");
      }
   }

   public void testSecureSetClearDenied()
   {
      try
      {
         // giving no permissions
         doActionWithPermissions(new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               set.clear();
               return null;
            }
         });
         fail("Modification should be denied.");
      }
      catch (Exception e)
      {
      }
   }
}
