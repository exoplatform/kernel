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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

import javax.sql.DataSource;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

/**
 * This classes is used to wrap the original {@link DataSource}
 * in order to be able to support the managed data sources with a limited 
 * amount of changes. A {@link DataSource} is expected to be wrapped only 
 * when a data source is defined and has been configured as managed
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ManagedDataSource implements DataSource
{
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.ManagedDataSource");
   
   /**
    * The transaction manager
    */
   private final TransactionManager tm;

   /**
    * The wrapped {@link DataSource}
    */
   private final DataSource ds;

   /**
    * Indicates whether the tx status need to be check first to know
    * if the provided connection needs to be managed or not
    */
   private final boolean checkIfTxActive;
   
   /**
    * default constructor
    */
   public ManagedDataSource(DataSource ds, TransactionManager tm, boolean checkIfTxActive)
   {
      this.tm = tm;
      this.ds = ds;
      this.checkIfTxActive = checkIfTxActive;
   }
   
   /**
    * @see javax.sql.CommonDataSource#getLogWriter()
    */
   public PrintWriter getLogWriter() throws SQLException
   {
      return ds.getLogWriter();
   }

   /**
    * @see javax.sql.CommonDataSource#getLoginTimeout()
    */
   public int getLoginTimeout() throws SQLException
   {
      return ds.getLoginTimeout();
   }

   /**
    * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
    */
   public void setLogWriter(PrintWriter out) throws SQLException
   {
      ds.setLogWriter(out);
   }

   /**
    * @see javax.sql.CommonDataSource#setLoginTimeout(int)
    */
   public void setLoginTimeout(int seconds) throws SQLException
   {
      ds.setLoginTimeout(seconds);
   }

   /**
    * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
    */
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      return ds.isWrapperFor(iface);
   }

   /**
    * @see java.sql.Wrapper#unwrap(java.lang.Class)
    */
   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      return ds.unwrap(iface);
   }

   /**
    * @see javax.sql.DataSource#getConnection()
    */
   public Connection getConnection() throws SQLException
   {
      Connection con = ds.getConnection();
      return providesManagedConnection() ? new ManagedConnection(con) : con;
   }

   /**
    * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
    */
   public Connection getConnection(String username, String password) throws SQLException
   {
      Connection con = ds.getConnection(username, password);
      return providesManagedConnection() ? new ManagedConnection(con) : con;
   }

   /**
    * Indicates whether or not a global tx is active
    * @return <code>true</code> if a tx is active, <code>false</code> otherwise
    */
   private boolean isTxActive()
   {
      try
      {
         return tm != null && tm.getStatus() != Status.STATUS_NO_TRANSACTION;
      }
      catch (SystemException e)
      {
         LOG.warn("We cannot know if a global tx is active", e);
      }
      return false;
   }
   
   /**
    * Indicates whether or not the provided connection needs to be managed
    */
   private boolean providesManagedConnection()
   {
      return !checkIfTxActive || isTxActive();
   }

   /**
    * @see javax.sql.CommonDataSource#getParentLogger()
    */
   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      try
      {
         Method m = ds.getClass().getMethod("getParentLogger");
         return (Logger)m.invoke(ds);
      }
      catch (Exception e)
      {
         throw new SQLFeatureNotSupportedException(e);
      }
   }
}
