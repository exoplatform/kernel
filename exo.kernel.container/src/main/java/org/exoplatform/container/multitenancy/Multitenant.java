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
package org.exoplatform.container.multitenancy;

/**
 * Annotate components as mutlitenant capable. <br>
 * If applied to a class then it means 
 * that such class should be treated in special way by the container to provide per-tenant
 * instances of the component in container. <br>
 * When applied to a method of the class already annotated as multitenant then such method's 
 * return type (if it has such one) should be also treated in special way to provide per-tenant
 * isolation of returned instance in future. But take in account, such annotation on a method 
 * doesn't guaranty a type and its instances will be treated by the multitenant container in 
 * all cases. In general only interfaces and non final classes with default (empty) constructor  
 * are good candidates for such manipulation. <br>
 * To skip such manipulation on a method use {@code @Multitenant(false)}, it can be useful if 
 * the method implements support of multitenancy natively but others methods need to be handled 
 * by the container.
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: Multitenant.java 00000 Mar 20, 2013 11:10:17 PM pnedonosko $
 *
 */
public @interface Multitenant {

   /**
    * Value {@code true} means 'make it multitenant', {@code false} means 'skip it and don't do anything'.   
    * 
    * @return boolean, {@code true} if special treating should be performed on a type tp make it 
    *    multitenant capable. 
    */
   boolean value() default true;
}
