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
package org.exoplatform.container.test;

import org.exoplatform.container.component.ExecutionContext;
import org.exoplatform.container.component.ExecutionUnit;
import org.exoplatform.test.BasicTestCase;

/**
 * Created by the Exo Development team. Author : Mestrallet Benjamin
 * benjamin.mestrallet@exoplatform.com
 */
public class TestChaining extends BasicTestCase
{
   public void testChain() throws Throwable
   {
      TextExecutionUnit chain = new TextExecutionUnit("unit-1");
      chain.addExecutionUnit(new TextExecutionUnit("unit-2"));
      chain.addExecutionUnit(new TextExecutionUnit("unit-3"));
      TextExcutionContext context = new TextExcutionContext("context");
      context.setCurrentExecutionUnit(chain);
      context.execute();
   }

   static class TextExecutionUnit extends ExecutionUnit
   {
      String name_;

      public TextExecutionUnit(String name)
      {
         name_ = name;
      }

      public Object execute(ExecutionContext context) throws Throwable
      {
         System.out.println("start execution: " + name_);
         Object result = context.executeNextUnit();
         System.out.println("end execution: " + name_);
         return result;
      }
   }

   static class TextExcutionContext extends ExecutionContext
   {
      String name_;

      public TextExcutionContext(String name)
      {
         name_ = name;
      }
   }
}
