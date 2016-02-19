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
package org.exoplatform.services.transaction.impl.jotm;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.exoplatform.services.transaction.impl.AbstractTransactionService;
import org.objectweb.jotm.Current;
import org.objectweb.jotm.TransactionFactory;
import org.objectweb.jotm.TransactionFactoryImpl;

import java.rmi.RemoteException;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * Created by The eXo Platform SAS.<br> JOTM based implementation of
 * TransactionService
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: $
 */
public class TransactionServiceJotmImpl extends AbstractTransactionService
{

   protected static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.TransactionServiceJotmImpl");

   private final int defaultTimeout;
   
   /**
    * Default constructor
    * @param initializer we enforce a dependency with the InitialContextInitializer to
    * ensure that the related binded resources have been defined
    * @param params the init parameters
    */
   public TransactionServiceJotmImpl(InitialContextInitializer initializer, InitParams params)
   {
      if (params != null && params.getValueParam("timeout") != null)
      {
         this.defaultTimeout = Integer.parseInt(params.getValueParam("timeout").getValue());
      }
      else
      {
         this.defaultTimeout = -1;
      }
   }

   /**
    * {@inheritDoc}
    */
   public TransactionManager findTransactionManager() throws Exception
   {
      Current current = Current.getCurrent();
      if (current == null)
      {
         try
         {
            current = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Current>()
            {
               public Current run() throws Exception
               {
                  TransactionFactory tm = new TransactionFactoryImpl();
                  return new Current(tm);
               }
            });
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof RemoteException)
            {
               throw (RemoteException)cause;
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

         // Change the timeout only if JOTM is not initialized yet
         if (defaultTimeout > 0)
         {
            current.setDefaultTimeout(defaultTimeout);
         }
      }
      else
      {
         LOG.info("Use externally initialized JOTM: " + current);
      }
      return current;
   }

   /**
    * {@inheritDoc}
    */
   public UserTransaction findUserTransaction()
   {
      return (UserTransaction)getTransactionManager();
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public int getDefaultTimeout()
   {
      return ((Current)getTransactionManager()).getDefaultTimeout();
   }
}
