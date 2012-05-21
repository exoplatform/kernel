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

import junit.framework.TestCase;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jdbc.DataSourceProvider;
import org.exoplatform.services.transaction.TransactionService;

import java.io.PrintWriter;
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
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestDataSourceProviderImpl extends TestCase
{
   private static String DS_NAME = "TestDataSourceProviderImpl-DS";
   
   private String oldFactoryName;
   
   private MyDataSource mds;
   
   /**
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      oldFactoryName = System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "org.exoplatform.services.naming.SimpleContextFactory");
      new InitialContext().bind(DS_NAME, mds = new MyDataSource());
   }

   /**
    * @see junit.framework.TestCase#tearDown()
    */
   @Override
   protected void tearDown() throws Exception
   {
      try
      {
         new InitialContext().unbind(DS_NAME);
      }
      finally
      {
         if (oldFactoryName == null)
         {
            System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
         }
         else
         {
            System.setProperty(Context.INITIAL_CONTEXT_FACTORY, oldFactoryName);
         }
         super.tearDown();         
      }
   }

   public void testIsManaged() throws Exception
   {
      DataSourceProvider dsp = new DataSourceProviderImpl(null);
      assertFalse(dsp.isManaged(DS_NAME));
      
      InitParams params = new InitParams();
      dsp = new DataSourceProviderImpl(params);
      assertFalse(dsp.isManaged(DS_NAME));
      
      ValueParam paramConf = new ValueParam();
      paramConf.setName(DataSourceProviderImpl.PARAM_ALWAYS_MANAGED);
      paramConf.setValue("true");
      params.addParameter(paramConf);
      dsp = new DataSourceProviderImpl(params);
      assertTrue(dsp.isManaged(DS_NAME));
      
      paramConf.setValue("false");
      dsp = new DataSourceProviderImpl(params);
      assertFalse(dsp.isManaged(DS_NAME));
      
      ValuesParam paramsConf = new ValuesParam();
      paramsConf.setName(DataSourceProviderImpl.PARAM_MANAGED_DS);
      ArrayList<String> values = new ArrayList<String>();
      values.add(DS_NAME);
      values.add(" ds-foo1, ds-foo2 ");
      values.add("ds-foo3");
      paramsConf.setValues(values);
      params.addParameter(paramsConf);
      dsp = new DataSourceProviderImpl(params);
      assertTrue(dsp.isManaged(DS_NAME));
      assertTrue(dsp.isManaged("ds-foo1"));
      assertTrue(dsp.isManaged("ds-foo2"));
      assertTrue(dsp.isManaged("ds-foo3"));
   }
   
   public void testGetDataSource() throws Exception
   {
      DataSourceProvider dsp = new DataSourceProviderImpl(null);
      DataSource ds = dsp.getDataSource(DS_NAME);
      assertNotNull(ds);
      Connection con = ds.getConnection();
      con.commit();
      assertTrue(mds.con.committed);
      con = ds.getConnection(null, null);
      con.commit();
      assertTrue(mds.con.committed);
      
      MyTransactionService mts = new MyTransactionService();
      mts.tm.setStatus(Status.STATUS_ACTIVE);
      dsp = new DataSourceProviderImpl(null, mts);
      ds = dsp.getDataSource(DS_NAME);
      assertNotNull(ds);
      con = ds.getConnection();
      con.commit();
      assertTrue(mds.con.committed);
      con = ds.getConnection(null, null);
      con.commit();
      assertTrue(mds.con.committed);
      mts.tm.setStatus(Status.STATUS_NO_TRANSACTION);
      con = ds.getConnection();
      con.commit();
      assertTrue(mds.con.committed);
      con = ds.getConnection(null, null);
      con.commit();
      assertTrue(mds.con.committed);

      InitParams params = new InitParams();
      ValueParam paramConf = new ValueParam();
      paramConf.setName(DataSourceProviderImpl.PARAM_ALWAYS_MANAGED);
      paramConf.setValue("true");
      params.addParameter(paramConf);
      dsp = new DataSourceProviderImpl(params, mts);      
      ds = dsp.getDataSource(DS_NAME);
      assertNotNull(ds);
      mts.tm.setStatus(Status.STATUS_ACTIVE);
      con = ds.getConnection();
      con.commit();
      assertFalse(mds.con.committed);
      con = ds.getConnection(null, null);
      con.commit();
      assertFalse(mds.con.committed);
      mts.tm.setStatus(Status.STATUS_NO_TRANSACTION);
      con = ds.getConnection();
      con.commit();
      assertTrue(mds.con.committed);
      con = ds.getConnection(null, null);
      con.commit();
      assertTrue(mds.con.committed);
      
      paramConf = new ValueParam();
      paramConf.setName(DataSourceProviderImpl.PARAM_CHECK_TX);
      paramConf.setValue("false");
      params.addParameter(paramConf);
      dsp = new DataSourceProviderImpl(params, mts);      
      ds = dsp.getDataSource(DS_NAME);
      assertNotNull(ds);
      mts.tm.setStatus(Status.STATUS_ACTIVE);
      con = ds.getConnection();
      con.commit();
      assertFalse(mds.con.committed);
      con = ds.getConnection(null, null);
      con.commit();
      assertFalse(mds.con.committed);
      mts.tm.setStatus(Status.STATUS_NO_TRANSACTION);
      con = ds.getConnection();
      con.commit();
      assertFalse(mds.con.committed);
      con = ds.getConnection(null, null);
      con.commit();
      assertFalse(mds.con.committed);
   }
   
   private static class MyDataSource implements DataSource
   {
      public MyConnection con;

      /**
       * @see javax.sql.CommonDataSource#getLogWriter()
       */
      public PrintWriter getLogWriter() throws SQLException
      {
         return null;
      }

      /**
       * @see javax.sql.CommonDataSource#getLoginTimeout()
       */
      public int getLoginTimeout() throws SQLException
      {
         return 0;
      }

      /**
       * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
       */
      public void setLogWriter(PrintWriter arg0) throws SQLException
      {
      }

      /**
       * @see javax.sql.CommonDataSource#setLoginTimeout(int)
       */
      public void setLoginTimeout(int arg0) throws SQLException
      {
      }

      /**
       * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
       */
      public boolean isWrapperFor(Class<?> arg0) throws SQLException
      {
         return false;
      }

      /**
       * @see java.sql.Wrapper#unwrap(java.lang.Class)
       */
      public <T> T unwrap(Class<T> arg0) throws SQLException
      {
         return null;
      }

      /**
       * @see javax.sql.DataSource#getConnection()
       */
      public Connection getConnection() throws SQLException
      {
         return con = new MyConnection();
      }

      /**
       * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
       */
      public Connection getConnection(String username, String password) throws SQLException
      {
         return con = new MyConnection();
      }      
   }
   
   private static class MyConnection implements Connection
   {

      public boolean committed;
      
      /**
       * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
       */
      public boolean isWrapperFor(Class<?> iface) throws SQLException
      {
         return false;
      }

      /**
       * @see java.sql.Wrapper#unwrap(java.lang.Class)
       */
      public <T> T unwrap(Class<T> iface) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareStatement(java.lang.String)
       */
      public PreparedStatement prepareStatement(String sql) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareCall(java.lang.String)
       */
      public CallableStatement prepareCall(String sql) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#nativeSQL(java.lang.String)
       */
      public String nativeSQL(String sql) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#setAutoCommit(boolean)
       */
      public void setAutoCommit(boolean autoCommit) throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#commit()
       */
      public void commit() throws SQLException
      {
         committed = true;
      }

      /**
       * @see java.sql.Connection#rollback()
       */
      public void rollback() throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#close()
       */
      public void close() throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#setReadOnly(boolean)
       */
      public void setReadOnly(boolean readOnly) throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#setCatalog(java.lang.String)
       */
      public void setCatalog(String catalog) throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#setTransactionIsolation(int)
       */
      public void setTransactionIsolation(int level) throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#clearWarnings()
       */
      public void clearWarnings() throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#createArrayOf(java.lang.String, java.lang.Object[])
       */
      public Array createArrayOf(String arg0, Object[] arg1) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#createBlob()
       */
      public Blob createBlob() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#createClob()
       */
      public Clob createClob() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#createNClob()
       */
      public NClob createNClob() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#createSQLXML()
       */
      public SQLXML createSQLXML() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#createStatement()
       */
      public Statement createStatement() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#createStatement(int, int)
       */
      public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareStatement(java.lang.String, int, int)
       */
      public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
         throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareCall(java.lang.String, int, int)
       */
      public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#setTypeMap(java.util.Map)
       */
      public void setTypeMap(Map<String, Class<?>> map) throws SQLException
      {         
      }

      /**
       * @see java.sql.Connection#setSavepoint()
       */
      public Savepoint setSavepoint() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#setSavepoint(java.lang.String)
       */
      public Savepoint setSavepoint(String name) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#rollback(java.sql.Savepoint)
       */
      public void rollback(Savepoint savepoint) throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#releaseSavepoint(java.sql.Savepoint)
       */
      public void releaseSavepoint(Savepoint savepoint) throws SQLException
      {
      }

      /**
       * @see java.sql.Connection#createStatement(int, int, int)
       */
      public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
         throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#createStruct(java.lang.String, java.lang.Object[])
       */
      public Struct createStruct(String arg0, Object[] arg1) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#getAutoCommit()
       */
      public boolean getAutoCommit() throws SQLException
      {
         return false;
      }

      /**
       * @see java.sql.Connection#isClosed()
       */
      public boolean isClosed() throws SQLException
      {
         return false;
      }

      /**
       * @see java.sql.Connection#isReadOnly()
       */
      public boolean isReadOnly() throws SQLException
      {
         return false;
      }

      /**
       * @see java.sql.Connection#getCatalog()
       */
      public String getCatalog() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#getClientInfo()
       */
      public Properties getClientInfo() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#getClientInfo(java.lang.String)
       */
      public String getClientInfo(String arg0) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#getMetaData()
       */
      public DatabaseMetaData getMetaData() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#getTransactionIsolation()
       */
      public int getTransactionIsolation() throws SQLException
      {
         return 0;
      }

      /**
       * @see java.sql.Connection#getWarnings()
       */
      public SQLWarning getWarnings() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#getTypeMap()
       */
      public Map<String, Class<?>> getTypeMap() throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#getHoldability()
       */
      public int getHoldability() throws SQLException
      {
         return 0;
      }

      /**
       * @see java.sql.Connection#isValid(int)
       */
      public boolean isValid(int arg0) throws SQLException
      {
         return false;
      }

      /**
       * @see java.sql.Connection#prepareStatement(java.lang.String, int, int, int)
       */
      public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
         int resultSetHoldability) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareCall(java.lang.String, int, int, int)
       */
      public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
         int resultSetHoldability) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareStatement(java.lang.String, int)
       */
      public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareStatement(java.lang.String, int[])
       */
      public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#prepareStatement(java.lang.String, java.lang.String[])
       */
      public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
      {
         return null;
      }

      /**
       * @see java.sql.Connection#setClientInfo(java.util.Properties)
       */
      public void setClientInfo(Properties arg0) throws SQLClientInfoException
      {
      }

      /**
       * @see java.sql.Connection#setClientInfo(java.lang.String, java.lang.String)
       */
      public void setClientInfo(String arg0, String arg1) throws SQLClientInfoException
      {
      }

      /**
       * @see java.sql.Connection#setHoldability(int)
       */
      public void setHoldability(int holdability) throws SQLException
      {
      }      
   }
   
   private static class MyTransactionService implements TransactionService
   {
      public MyTransactionManager tm = new MyTransactionManager();
      
      /**
       * @see org.exoplatform.services.transaction.TransactionService#getTransactionManager()
       */
      public TransactionManager getTransactionManager()
      {
         return tm;
      }

      /**
       * @see org.exoplatform.services.transaction.TransactionService#getUserTransaction()
       */
      public UserTransaction getUserTransaction()
      {
         return null;
      }

      /**
       * @see org.exoplatform.services.transaction.TransactionService#getDefaultTimeout()
       */
      public int getDefaultTimeout()
      {
         return 0;
      }

      /**
       * @see org.exoplatform.services.transaction.TransactionService#setTransactionTimeout(int)
       */
      public void setTransactionTimeout(int seconds) throws SystemException
      {
      }

      /**
       * @see org.exoplatform.services.transaction.TransactionService#enlistResource(javax.transaction.xa.XAResource)
       */
      public boolean enlistResource(XAResource xares) throws RollbackException, SystemException, IllegalStateException
      {
         return false;
      }

      /**
       * @see org.exoplatform.services.transaction.TransactionService#delistResource(javax.transaction.xa.XAResource)
       */
      public boolean delistResource(XAResource xares) throws RollbackException, SystemException, IllegalStateException
      {
         return false;
      }      
   }
   
   private static class MyTransactionManager implements TransactionManager
   {

      private int status;
      
      public void setStatus(int status)
      {
         this.status = status;
      }
      
      /**
       * @see javax.transaction.TransactionManager#begin()
       */
      public void begin() throws NotSupportedException, SystemException
      {
      }

      /**
       * @see javax.transaction.TransactionManager#commit()
       */
      public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
         SecurityException, IllegalStateException, SystemException
      {
      }

      /**
       * @see javax.transaction.TransactionManager#getStatus()
       */
      public int getStatus() throws SystemException
      {
         return status;
      }

      /**
       * @see javax.transaction.TransactionManager#getTransaction()
       */
      public Transaction getTransaction() throws SystemException
      {
         return null;
      }

      /**
       * @see javax.transaction.TransactionManager#resume(javax.transaction.Transaction)
       */
      public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException
      {
      }

      /**
       * @see javax.transaction.TransactionManager#rollback()
       */
      public void rollback() throws IllegalStateException, SecurityException, SystemException
      {
      }

      /**
       * @see javax.transaction.TransactionManager#setRollbackOnly()
       */
      public void setRollbackOnly() throws IllegalStateException, SystemException
      {
      }

      /**
       * @see javax.transaction.TransactionManager#setTransactionTimeout(int)
       */
      public void setTransactionTimeout(int seconds) throws SystemException
      {
      }

      /**
       * @see javax.transaction.TransactionManager#suspend()
       */
      public Transaction suspend() throws SystemException
      {
         return null;
      }
      
   }
}
