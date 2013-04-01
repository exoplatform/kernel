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
 * An element annotated with {@code @Multitenant} will be enhanced to be mutlitenant capable in eXo container.<br>
 * <br>
 * If applied to a class then it means that such class should be enhanced in special way by 
 * the container to provide per-tenant instances of the component. <br>
 * When applied to a method of the class already annotated as multitenant, then the method's 
 * return type (if it is not void) should be also enhanced to provide per-tenant
 * isolation of the returned instance in future. But take in account, such annotation on a method 
 * doesn't guaranty the returned object will be enhanced by the multitenant container in 
 * all cases. In general only interfaces and non final classes with default (empty) constructor  
 * are good candidates for such manipulation. Refer to the actual implementation of multitenancy 
 * for details of what and how can be enhanced.<br>
 * To skip such manipulation on a method use {@code @Multitenant(false)}, it can be useful if 
 * the method already supports multitenancy (natively or not requires to be such) but other methods 
 * should be still enhanced.<br>
 * It also possible to enable enhancement explicitly for only selected methods using 
 * {@code @Multitenant(explicitly=true)} on a class and then annotate target methods with {@code @Multitenant}.<br>
 * <br>
 * Example 1: Enhance all methods of the class except of {@code getInfo()}.
 * <pre>
 * {@literal @}Multitenant
 * public class AcmeComponent {
 *   
 *   // This method, and optionally its return value, will be enhanced.  
 *   public AcmeData getData() {
 *     return new AcmeData();
 *   }
 *   
 *   // This method will not be enhanced.
 *   {@literal @}Multitenant(false)  
 *   public InfoData getInfo() {
 *     return new InfoData();
 *   }
 * }
 * </pre>
 * 
 * Example 2: enhance only explicitly selected methods.
 * <pre>
 * {@literal @}Multitenant(explicitly=true)
 * public class AcmeComponent {
 *   
 *   // This method, and optionally its return value, will be enhanced.
 *   {@literal @}Multitenant  
 *   public AcmeData getData() {
 *     return new AcmeData();
 *   }
 *   
 *   // This method will not be enhanced.
 *   public InfoData getInfo() {
 *     return new InfoData();
 *   }
 * }
 * </pre>
 * 
 * @author <a href="mailto:pnedonosko@exoplatform.com">Peter Nedonosko</a>
 * @version $Id: Multitenant.java 00000 Mar 20, 2013 11:10:17 PM pnedonosko $
 *
 */
public @interface Multitenant {

   /**
    * Value {@code true} means 'make it multitenant', {@code false} means 'skip it and don't do anything'.   
    * 
    * @return boolean, {@code true} by default and means enhance a type to make it multitenant capable, 
    *    {@code false} means to skip enhancement. 
    */
   boolean value() default true;
<<<<<<< HEAD
   
=======

>>>>>>> feature/multitenancy
   /**
    * If will be set in an annotation of a class, then it will mean to enhance only explicitly 
    * annotated methods of the class. By default all methods will be enhanced to be multitenant capable.
    *  
    * @return boolean, {@code true} if special treating should be performed on a type to make it 
    *    multitenant capable, {@code false} by default and it means enhance all methods.
    */
   boolean explicitly() default false;
}
