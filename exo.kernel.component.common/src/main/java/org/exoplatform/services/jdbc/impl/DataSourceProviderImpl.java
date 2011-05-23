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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.jdbc.DataSourceProvider;
import org.exoplatform.services.transaction.TransactionService;

import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

/**
 * The default implementation of {@link DataSourceProvider}. It allows you
 * to define a data source as managed thanks to the configuration of this 
 * component. When the data source is declared as managed, the {@link DataSource}
 * object will be wrap into a {@link ManagedDataSource}.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DataSourceProviderImpl implements DataSourceProvider
{

   /**
    * The name of the parameter to know if the tx has to be checked or not.
    */
   protected static final String PARAM_CHECK_TX = "check-tx-active";

   /**
    * The name of the parameter to know if the data sources are always managed.
    */
   protected static final String PARAM_ALWAYS_MANAGED = "always-managed";

   /**
    * The name of the parameter of all the managed data sources.
    */
   protected static final String PARAM_MANAGED_DS = "managed-data-sources";
 
   /**
    * The transaction manager
    */
   protected final TransactionManager tm;

   /**
    * Indicates if the data source needs to check if a tx is active
    * to decide if the provided connection needs to be managed or not.
    * If it is set to false, the data source will provide only
    * managed connections if the data source itself is managed. 
    */
   protected boolean checkIfTxActive = true;

   /**
    * Indicates that all the data sources are managed
    */
   protected boolean alwaysManaged;

   /**
    * A set of all the data sources that are managed
    */
   protected final Set<String> managedDS = new HashSet<String>(); 
   
   /**
    * The default constructor
    */
   public DataSourceProviderImpl(InitParams params)
   {
      this(params, null);
   }

   /**
    * The default constructor
    */
   public DataSourceProviderImpl(InitParams params, TransactionService tService)
   {
      this.tm = tService == null ? null : tService.getTransactionManager();
      if (params != null)
      {
         ValueParam param = params.getValueParam(PARAM_CHECK_TX);
         if (param != null)
         {
            this.checkIfTxActive = Boolean.valueOf(param.getValue());
         }
         param = params.getValueParam(PARAM_ALWAYS_MANAGED);
         if (param != null && Boolean.valueOf(param.getValue()))
         {
            this.alwaysManaged = true;
            return;
         }
         ValuesParam vp = params.getValuesParam(PARAM_MANAGED_DS);
         if (vp != null && vp.getValues() != null)
         {
            for (Object oValue : vp.getValues())
            {
               String s = (String)oValue;
               StringTokenizer st = new StringTokenizer(s, ",");
               while (st.hasMoreTokens())
               {
                  String dsName = st.nextToken().trim();
                  if (!dsName.isEmpty())
                  {
                     managedDS.add(dsName);                     
                  }
               }
            }
         }
      }
   }
   
   /**
    * @throws NamingException 
    * @see org.exoplatform.services.jdbc.DataSourceProvider#getDataSource(java.lang.String)
    */
   public DataSource getDataSource(String dataSourceName) throws NamingException
   {
      DataSource ds = (DataSource)new InitialContext().lookup(dataSourceName);
      // wrap the data source object if it is managed
      return isManaged(dataSourceName) ? new ManagedDataSource(ds, tm, checkIfTxActive) : ds;
   }
   
   /**
    * @see org.exoplatform.services.jdbc.DataSourceProvider#isManaged(java.lang.String)
    */
   public boolean isManaged(String dataSourceName)
   {
      if (alwaysManaged)
      {
         return true;
      }
      else if (managedDS.isEmpty())
      {
         return false;
      }
      return managedDS.contains(dataSourceName);
   }
}
