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

import java.lang.reflect.Method;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * This classes wraps a jdbc connection in order to prevent any forbidden
 * actions such as explicit commit/rollback.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ManagedConnection implements Connection
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.ManagedConnection");

   /**
    * The nested connection
    */
   private final Connection con;

   /**
    * default constructor
    */
   public ManagedConnection(Connection con)
   {
      this.con = con;
   }

   /**
    * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
    */
   public boolean isWrapperFor(Class<?> iface) throws SQLException
   {
      return con.isWrapperFor(iface);
   }

   /**
    * @see java.sql.Wrapper#unwrap(java.lang.Class)
    */
   public <T> T unwrap(Class<T> iface) throws SQLException
   {
      return con.unwrap(iface);
   }

   /**
    * @see java.sql.Connection#prepareStatement(java.lang.String)
    */
   public PreparedStatement prepareStatement(String sql) throws SQLException
   {
      return con.prepareStatement(sql);
   }

   /**
    * @see java.sql.Connection#prepareCall(java.lang.String)
    */
   public CallableStatement prepareCall(String sql) throws SQLException
   {
      return con.prepareCall(sql);
   }

   /**
    * @see java.sql.Connection#nativeSQL(java.lang.String)
    */
   public String nativeSQL(String sql) throws SQLException
   {
      return con.nativeSQL(sql);
   }

   /**
    * @see java.sql.Connection#setAutoCommit(boolean)
    */
   public void setAutoCommit(boolean autoCommit) throws SQLException
   {
      con.setAutoCommit(autoCommit);
   }

   /**
    * @see java.sql.Connection#commit()
    */
   public void commit() throws SQLException
   {
      // We cannot call commit explicitly, it will be done by the AS itself
   }

   /**
    * @see java.sql.Connection#rollback()
    */
   public void rollback() throws SQLException
   {
      // We cannot call rollback explicitly, it will be done by the AS itself
   }

   /**
    * @see java.sql.Connection#close()
    */
   public void close() throws SQLException
   {
      con.close();
   }

   /**
    * @see java.sql.Connection#setReadOnly(boolean)
    */
   public void setReadOnly(boolean readOnly) throws SQLException
   {
      con.setReadOnly(readOnly);
   }

   /**
    * @see java.sql.Connection#setCatalog(java.lang.String)
    */
   public void setCatalog(String catalog) throws SQLException
   {
      con.setCatalog(catalog);
   }

   /**
    * @see java.sql.Connection#setTransactionIsolation(int)
    */
   public void setTransactionIsolation(int level) throws SQLException
   {
      con.setTransactionIsolation(level);
   }

   /**
    * @see java.sql.Connection#clearWarnings()
    */
   public void clearWarnings() throws SQLException
   {
      con.clearWarnings();
   }

   /**
    * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
    */
   public Array createArrayOf(String typeName, Object[] elements) throws SQLException
   {
      return con.createArrayOf(typeName, elements);
   }

   /**
    * @see java.sql.Connection#createBlob()
    */
   public Blob createBlob() throws SQLException
   {
      return con.createBlob();
   }

   /**
    * @see java.sql.Connection#createClob()
    */
   public Clob createClob() throws SQLException
   {
      return con.createClob();
   }

   /**
    * @see java.sql.Connection#createNClob()
    */
   public NClob createNClob() throws SQLException
   {
      return con.createNClob();
   }

   /**
    * @see java.sql.Connection#createSQLXML()
    */
   public SQLXML createSQLXML() throws SQLException
   {
      return con.createSQLXML();
   }

   /**
    * @see java.sql.Connection#createStatement()
    */
   public Statement createStatement() throws SQLException
   {
      return con.createStatement();
   }

   /**
    * @see java.sql.Connection#createStatement(int, int)
    */
   public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return con.createStatement(resultSetType, resultSetConcurrency);
   }

   /**
    * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
    */
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException
   {
      return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
   }

   /**
    * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
    */
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
   {
      return con.prepareCall(sql, resultSetType, resultSetConcurrency);
   }

   /**
    * @see java.sql.Connection#setTypeMap(java.util.Map)
    */
   public void setTypeMap(Map<String, Class<?>> map) throws SQLException
   {
      con.setTypeMap(map);
   }

   /**
    * @see java.sql.Connection#setSavepoint()
    */
   public Savepoint setSavepoint() throws SQLException
   {
      return con.setSavepoint();
   }

   /**
    * @see java.sql.Connection#setSavepoint(java.lang.String)
    */
   public Savepoint setSavepoint(String name) throws SQLException
   {
      return con.setSavepoint(name);
   }

   /**
    * @see java.sql.Connection#rollback(java.sql.Savepoint)
    */
   public void rollback(Savepoint savepoint) throws SQLException
   {
      // We cannot call rollback explicitly, it will be done by the AS itself
   }

   /**
    * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
    */
   public void releaseSavepoint(Savepoint savepoint) throws SQLException
   {
      con.releaseSavepoint(savepoint);
   }

   /**
    * @see java.sql.Connection#createStatement(int, int, int)
    */
   public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException
   {
      return con.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
   }

   /**
    * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
    */
   public Struct createStruct(String typeName, Object[] attributes) throws SQLException
   {
      return con.createStruct(typeName, attributes);
   }

   /**
    * @see java.sql.Connection#getAutoCommit()
    */
   public boolean getAutoCommit() throws SQLException
   {
      return con.getAutoCommit();
   }

   /**
    * @see java.sql.Connection#isClosed()
    */
   public boolean isClosed() throws SQLException
   {
      return con.isClosed();
   }

   /**
    * @see java.sql.Connection#isReadOnly()
    */
   public boolean isReadOnly() throws SQLException
   {
      return con.isReadOnly();
   }

   /**
    * @see java.sql.Connection#getCatalog()
    */
   public String getCatalog() throws SQLException
   {
      return con.getCatalog();
   }

   /**
    * @see java.sql.Connection#getClientInfo()
    */
   public Properties getClientInfo() throws SQLException
   {
      return con.getClientInfo();
   }

   /**
    * @see java.sql.Connection#getClientInfo(java.lang.String)
    */
   public String getClientInfo(String name) throws SQLException
   {
      return con.getClientInfo(name);
   }

   /**
    * @see java.sql.Connection#getMetaData()
    */
   public DatabaseMetaData getMetaData() throws SQLException
   {
      return con.getMetaData();
   }

   /**
    * @see java.sql.Connection#getTransactionIsolation()
    */
   public int getTransactionIsolation() throws SQLException
   {
      return con.getTransactionIsolation();
   }

   /**
    * @see java.sql.Connection#getWarnings()
    */
   public SQLWarning getWarnings() throws SQLException
   {
      return con.getWarnings();
   }

   /**
    * @see java.sql.Connection#getTypeMap()
    */
   public Map<String, Class<?>> getTypeMap() throws SQLException
   {
      return con.getTypeMap();
   }

   /**
    * @see java.sql.Connection#getHoldability()
    */
   public int getHoldability() throws SQLException
   {
      return con.getHoldability();
   }

   /**
    * @see java.sql.Connection#isValid(int)
    */
   public boolean isValid(int timeout) throws SQLException
   {
      return con.isValid(timeout);
   }

   /**
    * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
    */
   public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException
   {
      return con.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
   }

   /**
    * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
    */
   public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException
   {
      return con.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
   }

   /**
    * @see java.sql.Connection#prepareStatement(java.lang.String, int)
    */
   public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
   {
      return con.prepareStatement(sql, autoGeneratedKeys);
   }

   /**
    * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
    */
   public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
   {
      return con.prepareStatement(sql, columnIndexes);
   }

   /**
    * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
    */
   public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
   {
      return con.prepareStatement(sql, columnNames);
   }

   /**
    * @see java.sql.Connection#setClientInfo(java.util.Properties)
    */
   public void setClientInfo(Properties properties) throws SQLClientInfoException
   {
      con.setClientInfo(properties);
   }

   /**
    * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
    */
   public void setClientInfo(String name, String value) throws SQLClientInfoException
   {
      con.setClientInfo(name, value);
   }

   /**
    * @see java.sql.Connection#setHoldability(int)
    */
   public void setHoldability(int holdability) throws SQLException
   {
      con.setHoldability(holdability);
   }

   /**
    * @see java.sql.Connection#setSchema(java.lang.String)
    */
   public void setSchema(String schema) throws SQLException
   {
      try
      {
         Method m = con.getClass().getMethod("setSchema", String.class);
         m.invoke(con, schema);
      }
      catch (NoSuchMethodException e)
      {
         LOG.debug("The method setSchema cannot be found in the class " + con.getClass() + 
                  ", so we assume it is not supported");
      }
      catch (Exception e)
      {
         throw new SQLException(e);
      }
   }

   /**
    * @see java.sql.Connection#getSchema()
    */
   public String getSchema() throws SQLException
   {
      try
      {
         Method m = con.getClass().getMethod("getSchema");
         return (String)m.invoke(con);
      }
      catch (NoSuchMethodException e)
      {
         LOG.debug("The method getSchema cannot be found in the class " + con.getClass() + 
                  ", so we assume it is not supported");
      }
      catch (Exception e)
      {
         throw new SQLException(e);
      }
      return null;
   }

   /**
    * @see java.sql.Connection#abort(java.util.concurrent.Executor)
    */
   public void abort(Executor executor) throws SQLException
   {
      try
      {
         Method m = con.getClass().getMethod("abort", Executor.class);
         m.invoke(con, executor);
      }
      catch (NoSuchMethodException e)
      {
         LOG.debug("The method abort cannot be found in the class " + con.getClass() + 
            ", so we assume it is not supported");
      }
      catch (Exception e)
      {
         throw new SQLException(e);
      }
   }

   /**
    * @see java.sql.Connection#setNetworkTimeout(java.util.concurrent.Executor, int)
    */
   public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException
   {
      try
      {
         Method m = con.getClass().getMethod("setNetworkTimeout", Executor.class, int.class);
         m.invoke(con, executor, milliseconds);
      }
      catch (NoSuchMethodException e)
      {
         LOG.debug("The method setNetworkTimeout cannot be found in the class " + con.getClass() + 
            ", so we assume it is not supported");
      }
      catch (Exception e)
      {
         throw new SQLException(e);
      }
   }

   /**
    * @see java.sql.Connection#getNetworkTimeout()
    */
   public int getNetworkTimeout() throws SQLException
   {
      try
      {
         Method m = con.getClass().getMethod("getNetworkTimeout");
         return (Integer)m.invoke(con);
      }
      catch (NoSuchMethodException e)
      {
         LOG.debug("The method getNetworkTimeout cannot be found in the class " + con.getClass() + 
                  ", so we assume it is not supported");
      }
      catch (Exception e)
      {
         throw new SQLException(e);
      }
      return 0;
   }
}
