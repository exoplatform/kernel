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

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

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
    * Flag which is set to true if we closed DataSource.
    */
   private final AtomicBoolean closed = new AtomicBoolean(false);

   /**
    * Exception instance for logging of call stack which called a closing of DataSource. Need for finding where exists usage of closed sessions.
    */
   private Exception closedByCallStack;

   private static final Log log = ExoLogger.getLogger("exo.kernel.component.common.CloseableDataSource");

   /**
    * Property value which responsible for allowing of closed DataSource usage.
    */
   private static final boolean PROHIBIT_CLOSED_DATASOURCE_USAGE = Boolean.valueOf(PrivilegedSystemHelper.getProperty("exo.jcr.prohibit.closed.datasource.usage", "true"));


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
      closed.set(true);
      if (PROHIBIT_CLOSED_DATASOURCE_USAGE)
      {
         ds = null;
      }
      if (PROHIBIT_CLOSED_DATASOURCE_USAGE || PropertyManager.isDevelopping())
      {
         this.closedByCallStack = new Exception("The datasource has been closed by the following call stack");
      }
   }

   /**
    * Check if datasouce already closed.
    *
    * @throws SQLException
    *          if datasource is closed
    */
   private void checkValid() throws SQLException
   {
      if (closed.get())
      {
         if (ds == null)
         {
            throw new SQLException("The datasource is closed", closedByCallStack);
         }
         else if (PropertyManager.isDevelopping())
         {
            log.warn("This kind of operation is forbidden after a DataSource closed, "
               + "please note that an exception will be raised in the next jcr version.", new Exception(
               closedByCallStack));
         }
      }
   }

   /**
    * @see javax.sql.CommonDataSource#getParentLogger()
    */
   public Logger getParentLogger() throws SQLFeatureNotSupportedException
   {
      try
      {
         checkValid();
         Method m = ds.getClass().getMethod("getParentLogger");
         return (Logger)m.invoke(ds);
      }
      catch (Exception e)
      {
         throw new SQLFeatureNotSupportedException(e);
      }
   }

}
