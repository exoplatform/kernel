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
package org.exoplatform.services.jdbc.impl;

import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

/**
 * This class is used to wrap the original {@link DataSource}
 * in order to be able to support close operation.
 * 
 * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
 * @version $Id$
 */
public class CloseableDataSource implements DataSource
{
   /**
    * The wrapped {@link DataSource}
    */
   private DataSource ds;

   /**
    * Constructor CloseableDataSource.
    */
   public CloseableDataSource(DataSource ds)
   {
      this.ds = ds;
   }

   /**
    * {@inheritDoc}
    */
   public PrintWriter getLogWriter() throws SQLException
   {
      checkValid();
      return ds.getLogWriter();
   }

   /**
    * {@inheritDoc}
    */
   public int getLoginTimeout() throws SQLException
   {
      checkValid();
      return ds.getLoginTimeout();
   }

   /**
    * {@inheritDoc}
    */
   public void setLogWriter(PrintWriter out) throws SQLException
   {
      checkValid();
      ds.setLogWriter(out);
   }

   /**
    * {@inheritDoc}
    */
   public void setLoginTimeout(int seconds) throws SQLException
   {
      checkValid();
      ds.setLoginTimeout(seconds);
   }

   /**
    * {@inheritDoc}
    */
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      checkValid();
      return ds.isWrapperFor(iface);
   }

   /**
    * {@inheritDoc}
    */
   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      checkValid();
      return ds.unwrap(iface);
   }

   /**
    * {@inheritDoc}
    */
   public Connection getConnection() throws SQLException
   {
      checkValid();
      return ds.getConnection();
   }

   /**
    * {@inheritDoc}
    */
   public Connection getConnection(String username, String password) throws SQLException
   {
      checkValid();
      return ds.getConnection(username, password);
   }

   /**
    * Closes datasource to release all idle connections.
    */
   public void close()
   {
      ds = null;
   }

   /**
    * Check if datasouce already closed.
    * 
    * @throws SQLException
    *          if datasource is closed
    */
   private void checkValid() throws SQLException
   {
      if (ds == null)
      {
         throw new SQLException("The datasource is closed");
      }
   }
}
