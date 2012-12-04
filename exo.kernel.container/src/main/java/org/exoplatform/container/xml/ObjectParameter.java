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

import org.exoplatform.xml.object.XMLObject;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ObjectParameter.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ObjectParameter extends Parameter
{
   Object object;

   public Object getObject()
   {
      return object;
   }

   public void setObject(Object obj)
   {
      object = obj;
   }

   public XMLObject getXMLObject() throws Exception
   {
      if (object == null)
         return null;
      return new XMLObject(object);
   }

   public void setXMLObject(XMLObject xmlobject) throws Exception
   {
      if (xmlobject == null)
      {
         object = null;
         return;
      }
      object = xmlobject.toObject();
   }

   public String toString()
   {
      return object.toString();
   }
}
