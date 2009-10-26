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
package org.exoplatform.services.log;

/**
 * This factory garantees the plugability of the logging system. 
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Id: ExoLogFactory.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public interface ExoLogFactory
{

   /**
    * Obtain a logger for the specified name.
    *
    * @param name the logger name
    * @return the logger
    * @throws NullPointerException if the name is null
    */
   Log getExoLogger(String name) throws NullPointerException;

   /**
    * Obtain a logger for the specified name.
    *
    * @param clazz the logger name
    * @return the logger
    * @throws NullPointerException if the name is null
    */
   Log getExoLogger(Class clazz) throws NullPointerException;

}
