/*
 * Copyright (C) 2014 eXo Platform SAS.
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
package org.exoplatform.services.hikari;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;
import java.util.Hashtable;
import java.util.Properties;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;
import javax.sql.DataSource;


/**
 * @Created by The eXo Platform SAS
 * @Author : aboughzela@exoplatform.com
 * @Date: Feb 14, 2014 Time: 1:12:22 PM
 */
public class HikariDataSourceFactory implements ObjectFactory
{
   private final static String PROP_ACQUIREINCREMENT = "acquireIncrement";
   private final static String PROP_ACQUIRERETRIES = "acquireRetries";
   private final static String PROP_ACQUIRERETRYDELAY = "acquireRetryDelay";
   private final static String PROP_CONNECTIONTIMEOUT = "connectionTimeout";
   private final static String PROP_IDLETIMEOUT = "idleTimeout";
   private final static String PROP_LEAKDETECTIONTHRESHOLD = "leakDetectionThreshold";
   private final static String PROP_MAXLIFETIME = "maxLifetime";
   private final static String PROP_MAXPOOLSIZE = "maxPoolSize";
   private final static String PROP_MINPOOLSIZE = "minPoolSize";
   private final static String PROP_TRANSACTIONISOLATION = "transactionIsolation";
   private final static String PROP_CONNECTIONCUSTOMIZERCLASSNAME = "connectionCustomizerClassName";
   private final static String PROP_CONNECTIONINITSQL = "connectionInitSql";
   private final static String PROP_CONNECTIONTESTQUERY = "connectionTestQuery";
   private final static String PROP_POOLNAME = "poolName";
   private final static String PROP_ISAUTOCOMMIT = "isAutoCommit";
   private final static String PROP_DATASOURCECLASSNAME = "dataSourceClassName";
   private final static String PROP_ISINITIALIZATIONFAILFAST = "isInitializationFailFast";
   private final static String PROP_ISJDBC4CONNECTIONTEST = "isJdbc4connectionTest";
   private final static String PROP_ISREGISTERMBEANS = "isRegisterMbeans";


   /**
    * @inheritDoc
    */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable<?, ?> environment) throws Exception
   {
      if ((obj == null) || !(obj instanceof Reference))
      {
         return null;
      }

      Reference ref = (Reference)obj;
      if (!"javax.sql.DataSource".equals(ref.getClassName()))
      {
         return null;
      }

      Properties dataSourceProperties = new Properties();
      Properties properties = new Properties();
      for (int i = 0; i < ref.size(); i++)
      {
         String propertyName = ref.get(i).getType();
         RefAddr ra = ref.get(propertyName);
         if (ra != null)
         {
            String propertyValue = ra.getContent().toString();
            if (propertyName.startsWith("dataSource."))
            {
               dataSourceProperties.setProperty(propertyName.substring("dataSource.".length()), propertyValue);
            }
            else
            {
               properties.setProperty(propertyName, propertyValue);
            }
         }
      }
      return createDataSource(properties, dataSourceProperties);
   }


   public static DataSource createDataSource(Properties properties, Properties dataSourceProperties) throws Exception
   {
      HikariConfig config = new HikariConfig();

      String value;

      value = properties.getProperty(PROP_DATASOURCECLASSNAME);
      if (value != null)
      {
         config.setDataSourceClassName(value);
      }

      for (Object propKey : dataSourceProperties.keySet())
      {
         String propName = propKey.toString();
         Object propValue = dataSourceProperties.get(propKey);
         config.addDataSourceProperty(propName, propValue);
      }

      value = properties.getProperty(PROP_ACQUIREINCREMENT);
      if (value != null)
      {
         config.setAcquireIncrement(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_ACQUIRERETRIES);
      if (value != null)
      {
         config.setAcquireRetries(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_ACQUIRERETRYDELAY);
      if (value != null)
      {
         config.setAcquireRetryDelay(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_CONNECTIONTIMEOUT);
      if (value != null)
      {
         config.setConnectionTimeout(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_IDLETIMEOUT);
      if (value != null)
      {
         config.setIdleTimeout(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_LEAKDETECTIONTHRESHOLD);
      if (value != null)
      {
         config.setLeakDetectionThreshold(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_MAXLIFETIME);
      if (value != null)
      {
         config.setMaxLifetime(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_MAXPOOLSIZE);
      if (value != null)
      {
         config.setMaximumPoolSize(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_MINPOOLSIZE);
      if (value != null)
      {
         config.setMinimumPoolSize(Integer.parseInt(value));
      }

      value = properties.getProperty(PROP_TRANSACTIONISOLATION);
      if (value != null)
      {
         config.setTransactionIsolation(value);
      }

      value = properties.getProperty(PROP_CONNECTIONCUSTOMIZERCLASSNAME);
      if (value != null)
      {
         config.setConnectionCustomizerClassName(value);
      }

      value = properties.getProperty(PROP_CONNECTIONINITSQL);
      if (value != null)
      {
         config.setConnectionInitSql(value);
      }

      value = properties.getProperty(PROP_CONNECTIONTESTQUERY);
      if (value != null)
      {
         config.setConnectionTestQuery(value);
      }

      value = properties.getProperty(PROP_POOLNAME);
      if (value != null)
      {
         config.setPoolName(value);
      }

      value = properties.getProperty(PROP_ISAUTOCOMMIT);
      if (value != null)
      {
         config.setAutoCommit(Boolean.getBoolean(value));
      }

      value = properties.getProperty(PROP_ISINITIALIZATIONFAILFAST);
      if (value != null)
      {
         config.setInitializationFailFast(Boolean.getBoolean(value));
      }

      value = properties.getProperty(PROP_ISJDBC4CONNECTIONTEST);
      if (value != null)
      {
         config.setJdbc4ConnectionTest(Boolean.getBoolean(value));
      }

      value = properties.getProperty(PROP_ISREGISTERMBEANS);
      if (value != null)
      {
         config.setRegisterMbeans(Boolean.getBoolean(value));
      }

      return new HikariDataSource(config);
   }

}