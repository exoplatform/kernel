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

package org.exoplatform.management.annotations;

/**
 * The type of the impact of a managed method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public enum ImpactType
{

   /**
    * Read operation, does not affect state of the managed resource.
    * Equivalent of {@link javax.management.MBeanOperationInfo#INFO} for JMX and GET method for Rest.
    */
   READ,

   /**
    * Write operation, changes the state of the managed resource.
    * Equivalent of {@link javax.management.MBeanOperationInfo#INFO} for JMX and POST method for Rest.
    */
   WRITE,

   /**
    * Write operation, changes the state of the managed resource in an idempotent manner.
    * Equivalent of {@link javax.management.MBeanOperationInfo#INFO} for JMX and PUT method for Rest.
    */
   IDEMPOTENT_WRITE

}
