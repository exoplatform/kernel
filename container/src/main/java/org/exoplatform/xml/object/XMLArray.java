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
package org.exoplatform.xml.object;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 11, 2005
 * @version $Id: XMLArray.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class XMLArray
{

   private List list_ = new ArrayList();

   public XMLArray()
   {
   }

   public XMLArray(Object o) throws Exception
   {
      if (o instanceof int[])
      {
         int[] array = (int[])o;
         for (int i = 0; i < array.length; i++)
         {
            list_.add(new Integer(array[i]));
         }
      }
   }

   public void addValue(Object object)
   {
      XMLValue value = (XMLValue)object;
      list_.add(value);
   }

   public Iterator getIterator()
   {
      return list_.iterator();
   }
}
