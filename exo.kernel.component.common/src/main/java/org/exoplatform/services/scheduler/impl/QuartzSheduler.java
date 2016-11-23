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
package org.exoplatform.services.scheduler.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Properties;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.BaseContainerLifecyclePlugin;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Dec 13, 2005
 * 
 * @version $Id: QuartzSheduler.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public class QuartzSheduler implements Startable
{
   private static final Log    LOG                        = ExoLogger.getLogger("exo.kernel.component.common.QuartzSheduler");

   private static final String defaultDriverDelegateClass = "org.quartz.impl.jdbcjobstore.StdJDBCDelegate";

   private static final String PGSQLDriverDelegateClass   = "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate";

   private static final String MSSQLlDriverDelegateClass  = "org.quartz.impl.jdbcjobstore.MSSQLDelegate";

   private static final String datasourceProperty         = "org.quartz.dataSource.quartzDS.jndiURL";

   private static final String delegateClassProperty      = "org.quartz.jobStore.driverDelegateClass";

   private final Scheduler scheduler_;

   public QuartzSheduler(ExoContainerContext ctx, InitParams params) throws Exception
   {
      final SchedulerFactory sf;

      if (params != null && !params.isEmpty())
      {
         final Properties props = new Properties();
         for (String key : params.keySet())
         {
            props.setProperty(key, params.getValueParam(key).getValue());
         }
         String oldValue = props.getProperty(delegateClassProperty);
         if (oldValue== null || oldValue.isEmpty() || oldValue.equals(defaultDriverDelegateClass))
         {
           String datasourceName = props.getProperty(datasourceProperty);
           if (datasourceName != null && !datasourceName.isEmpty())
           {
             try (Connection conn = getConnection(datasourceName);)
             {
               DatabaseMetaData meta = conn.getMetaData();
               String newValue = getDriverDelegateClass(meta);
               props.setProperty(delegateClassProperty, newValue);
             }
           }
         }
         sf = new StdSchedulerFactory(props);
      }
      /*Use default quartz configuration (utilizes RAM as its storage device,
           exo.quartz.jobStore.class=org.quartz.simpl.RAMJobStore).*/
      else
      {
         sf = new StdSchedulerFactory();
      }
      try
      {
         scheduler_ = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Scheduler>()
         {
            public Scheduler run() throws Exception
            {
               return sf.getScheduler();
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof SchedulerException)
         {
            throw (SchedulerException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
      
      // If the scheduler has already been started, it is necessary to put the scheduler
      // in standby mode to ensure that the jobs of the ExoContainer won't launched too early
      scheduler_.standby();
      // This will launch the scheduler when all the components will be started  
      ctx.getContainer().addContainerLifecylePlugin(new BaseContainerLifecyclePlugin()
      {

         @Override
         public void startContainer(ExoContainer container) throws Exception
         {
            scheduler_.start();
         }         
      });
   }

   public Scheduler getQuartzSheduler()
   {
      return scheduler_;
   }

   public void start()
   {
   }

   public void stop()
   {
      try
      {
         scheduler_.shutdown();
      }
      catch (SchedulerException ex)
      {
         LOG.warn("Could not shutdown the scheduler", ex);
      }
   }

   /**
    * Opens connection to quartz database.
    */
   private Connection getConnection(String dsName) throws Exception
   {
     final DataSource dsF = (DataSource) new InitialContext().lookup(dsName);
     Connection jdbcConn = SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction<Connection>()
     {
       public Connection run() throws Exception
       {
         return dsF.getConnection();
       }
     });
     return jdbcConn;
   }

    /**
     * Auto detect DriverDelegateClass  according to database name.
     */
   private String getDriverDelegateClass(final DatabaseMetaData metaData) throws Exception
   {
     String databaseName = (String) SecurityHelper.doPrivilegedSQLExceptionAction(new PrivilegedExceptionAction()
     {
       public String run() throws Exception
       {
         return metaData.getDatabaseProductName();
       }
     });
     if(databaseName == null || databaseName.isEmpty())
     {
         LOG.warn("The database name cannot be retrieve, the default DriverDelegateClass will be used for Quartz.");
         return defaultDriverDelegateClass;
     }
     if (databaseName.startsWith("Microsoft SQL Server"))
     {
        return MSSQLlDriverDelegateClass;
     }
     else if (databaseName.startsWith("PostgreSQL"))
     {
        return PGSQLDriverDelegateClass;
     }
     else
     {
        return defaultDriverDelegateClass;
     }
   }
}
