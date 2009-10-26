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
package org.exoplatform.container.xml;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ValuesParam.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ValuesParam extends Parameter
{

   private ArrayList values = new ArrayList(2);

   public ArrayList getValues()
   {
      return values;
   }

   public void setValues(ArrayList values)
   {
      this.values = values;
   }

   public String getValue()
   {
      if (values.size() == 0)
         return null;
      return (String)values.get(0);
   }

   public String toString()
   {
      Iterator it = values.iterator();
      StringBuilder builder = new StringBuilder();
      while (it.hasNext())
      {
         Object object = (Object)it.next();
         builder.append(object);
         if (it.hasNext())
         {
            builder.append(",");
         }
      }
      return builder.toString();
   }
}
