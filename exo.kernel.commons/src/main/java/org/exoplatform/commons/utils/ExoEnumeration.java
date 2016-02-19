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
package org.exoplatform.commons.utils;

/** * Jul 11, 2004 
 * @author: Tuan Nguyen
 * @version: $Id: ExoEnumeration.java,v 1.3 2004/07/13 02:46:19 tuan08 Exp $
 */

import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author Ove Ranheim (oranheim@users.sourceforge.net)
 * @since Nov 9, 2003 4:01:29 PM
 */
public class ExoEnumeration implements Enumeration
{
   private Iterator iterator_;

   public ExoEnumeration(Iterator i)
   {
      iterator_ = i;
   }

   public boolean hasMoreElements()
   {
      return iterator_.hasNext();
   }

   public Object nextElement()
   {
      return iterator_.next();
   }
}
