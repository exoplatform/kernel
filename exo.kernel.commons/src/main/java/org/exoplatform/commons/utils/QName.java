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

/**
 * Created by The eXo Platform SAS . Qualified name
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class QName
{

   protected final String namespace;

   protected final String name;

   protected final String stringName;

   protected final int hashCode;

   public QName(String namespace, String name)
   {
      if (namespace == null)
      {
         namespace = "";
      }
      if (name == null)
      {
         name = "";
      }
      String stringName = ("[" + namespace + "]" + name);
      int hashCode = (31 + namespace.hashCode()) * 31 + name.hashCode();

      this.namespace = namespace;
      this.name = name;
      this.stringName = stringName;
      this.hashCode = hashCode;
   }

   public String getNamespace()
   {
      return namespace;
   }

   public String getName()
   {
      return name;
   }

   public String getAsString()
   {
      return stringName;
   }

   /** For toString() use */
   protected String asString()
   {
      return stringName;
   }

   @Override
   public String toString()
   {
      return super.toString() + " (" + asString() + ")";
   }

   @Override
   public boolean equals(Object o)
   {
      if (o == this)
         return true;

      if (o == null)
         return false;

      if (!(o instanceof QName))
         return false;

      return hashCode == o.hashCode() && getAsString().equals(((QName) o).getAsString());
   }

   @Override
   public int hashCode()
   {
      return hashCode;
   }

}
