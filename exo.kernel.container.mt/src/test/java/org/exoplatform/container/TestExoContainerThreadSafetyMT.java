/*
 * Copyright (C) 2013 eXo Platform SAS.
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

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
@RunWith(Parameterized.class)
public class TestExoContainerThreadSafetyMT extends TestExoContainerThreadSafety
{
   private Mode[] modes;
   public TestExoContainerThreadSafetyMT(Mode... modes)
   {
      this.modes = modes;
   }

   @Before
   @Override
   public void setUp()
   {
      Mode.setModes(modes);
      super.setUp();
   }

   @After
   public void tearDown()
   {
      Mode.clearModes();
   }

   @Parameters
   public static List<Object[]> data()
   {
      return Arrays.asList(new Object[][]{{null}, {new Mode[]{Mode.MULTI_THREADED}},
         {new Mode[]{Mode.MULTI_THREADED, Mode.DISABLE_MT_ON_STARTUP_COMPLETE}}, {new Mode[]{Mode.AUTO_SOLVE_DEP_ISSUES}},
         {new Mode[]{Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES}},
         {new Mode[]{Mode.MULTI_THREADED, Mode.AUTO_SOLVE_DEP_ISSUES, Mode.DISABLE_MT_ON_STARTUP_COMPLETE}}});
   }
}
