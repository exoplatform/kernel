/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.services.jdbc;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * This provider is used to get a {@link DataSource} in an uniform manner.
 * It allows to wrap the {@link DataSource} in case it is defined as managed
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public interface DataSourceProvider
{
   /**
    * Try to get the data source from a lookup, if it can't a {@link NamingException}
    * will be thrown
    * @param dataSourceName the name of the data source to lookup
    * @return the {@link DataSource} found thanks to the lookup. The original
    * object could be wrap to another {@link DataSource} in order to support
    * managed data source. 
    * @throws NamingException if the data source could not be found
    */
   DataSource getDataSource(String dataSourceName) throws NamingException;
   
   /**
    * Indicates whether or not the given data source is managed
    * @param dataSourceName the data source to check
    * @return <code>true</code> if the data source is managed, 
    * <code>false</code> otherwise
    */
   boolean isManaged(String dataSourceName);  
}
