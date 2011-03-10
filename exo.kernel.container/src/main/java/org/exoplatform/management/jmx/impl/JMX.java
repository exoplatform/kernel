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
package org.exoplatform.management.jmx.impl;

import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Various JMX utilities.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JMX
{

   private JMX()
   {
   }

   /**
    * This method create an object name from a generic map argument.
    *
    * @param domain  The domain part of the object name.
    * @param table A hash table containing one or more key
    * properties.  The key of each entry in the table is the key of a
    * key property in the object name.  The associated value in the
    * table is the associated value in the object name.
    *
    * @return an ObjectName corresponding to the given domain and
    * key mappings.
    * @exception MalformedObjectNameException The <code>domain</code>
    * contains an illegal character, or one of the keys or values in
    * <code>table</code> contains an illegal character, or one of the
    * values in <code>table</code> does not follow the rules for
    * quoting.
    * @exception NullPointerException One of the parameters is null.
    */
   public static ObjectName createObjectName(String domain, Map<String, String> table)
      throws MalformedObjectNameException, NullPointerException
   {
      StringBuilder name = new StringBuilder(128);
      name.append(domain).append(':');
      int i = 0;
      for (Map.Entry<String, String> entry : table.entrySet())
      {
         if (i++ > 0)
         {
            name.append(",");            
         }
         name.append(entry.getKey()).append('=').append(entry.getValue());
      }
      return  ObjectName.getInstance(name.toString());
   }
}
