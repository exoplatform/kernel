/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
 * along with this program; if not, see&lt;http://www.gnu.org/licenses/&gt;.
 */
package org.exoplatform.container;

import junit.framework.TestCase;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto 
 *          nicolas.filotto@exoplatform.com
 * 24 sept. 2009  
 */
public class TestUnifiedClassLoader extends TestCase
{

   public void testConstructor()
   {
      try
      {
         new UnifiedClassLoader();
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         new UnifiedClassLoader(new ClassLoader[0]);
         fail("An IllegalArgumentException is expected");
      }
      catch (IllegalArgumentException e)
      {
      }      
   }
   
   public void testGetResource() throws Exception
   {
      UnifiedClassLoader mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, null), new MockClassLoader(null, null)});
      assertNull(mcl.getResource(null));
      URL result = new URL("file:///foo");
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, null), new MockClassLoader(result, null)});
      assertEquals(result, mcl.getResource(null));      
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(result, null), new MockClassLoader(null, null)});
      assertEquals(result, mcl.getResource(null));      
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(new URL("file:///foo2"), null), new MockClassLoader(result, null)});
      assertEquals(result, mcl.getResource(null));      
   }
   
   public void testGetResources() throws Exception
   {
      UnifiedClassLoader mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, Collections.enumeration(new ArrayList<URL>())), new MockClassLoader(null, Collections.enumeration(new ArrayList<URL>()))});
      Enumeration<URL> eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertFalse(eResult.hasMoreElements());
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, null), new MockClassLoader(null, null)});
      eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertFalse(eResult.hasMoreElements());
      URL result = new URL("file:///foo");
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, Collections.enumeration(Arrays.asList(result))), new MockClassLoader(null, Collections.enumeration(new ArrayList<URL>()))});
      eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertTrue(eResult.hasMoreElements());
      assertEquals(result, eResult.nextElement());
      assertFalse(eResult.hasMoreElements());
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, Collections.enumeration(Arrays.asList(result))), new MockClassLoader(null, null)});
      eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertTrue(eResult.hasMoreElements());
      assertEquals(result, eResult.nextElement());
      assertFalse(eResult.hasMoreElements());
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, Collections.enumeration(new ArrayList<URL>())), new MockClassLoader(null, Collections.enumeration(Arrays.asList(result)))});
      eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertTrue(eResult.hasMoreElements());
      assertEquals(result, eResult.nextElement());
      assertFalse(eResult.hasMoreElements());
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, null), new MockClassLoader(null, Collections.enumeration(Arrays.asList(result)))});
      eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertTrue(eResult.hasMoreElements());
      assertEquals(result, eResult.nextElement());
      assertFalse(eResult.hasMoreElements());
      URL result1 = new URL("file:///foo");      
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, Collections.enumeration(Arrays.asList(result))), new MockClassLoader(null, Collections.enumeration(Arrays.asList(result1)))});
      eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertTrue(eResult.hasMoreElements());
      assertEquals(result, eResult.nextElement());
      assertFalse(eResult.hasMoreElements());
      URL result2 = new URL("file:///foo2");
      mcl = new UnifiedClassLoader(new ClassLoader[]{new MockClassLoader(null, Collections.enumeration(Arrays.asList(result))), new MockClassLoader(null, Collections.enumeration(Arrays.asList(result2)))});
      eResult = mcl.getResources(null);
      assertNotNull(eResult);
      assertTrue(eResult.hasMoreElements());
      assertEquals(result, eResult.nextElement());
      assertTrue(eResult.hasMoreElements());
      assertEquals(result2, eResult.nextElement());
      assertFalse(eResult.hasMoreElements());
   }

   private static class MockClassLoader extends ClassLoader
   {

      private final URL getResourceResult;

      private final Enumeration<URL> getResourcesResult;

      private MockClassLoader(URL getResourceResult, Enumeration<URL> getResourcesResult)
      {
         this.getResourceResult = getResourceResult;
         this.getResourcesResult = getResourcesResult;
      }

      public URL getResource(String name)
      {
         return getResourceResult;
      }

      public Enumeration<URL> getResources(String name)
      {
         return getResourcesResult;
      }
   }
}
