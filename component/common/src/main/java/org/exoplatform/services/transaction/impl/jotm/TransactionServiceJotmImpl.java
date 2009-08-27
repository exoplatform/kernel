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

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.naming.InitialContextInitializer;
import org.exoplatform.services.transaction.ExoResource;
import org.exoplatform.services.transaction.TransactionService;
import org.objectweb.jotm.Current;
import org.objectweb.jotm.TransactionFactory;
import org.objectweb.jotm.TransactionFactoryImpl;
import org.objectweb.jotm.XidImpl;

import java.rmi.RemoteException;
import java.util.List;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Created by The eXo Platform SAS.<br/> JOTM based implementation of
 * TransactionService
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: $
 */

public class TransactionServiceJotmImpl implements TransactionService
{

   protected static Log log = ExoLogger.getLogger("transaction.TransactionServiceJotmImpl");

   private Current current;

   public TransactionServiceJotmImpl(InitialContextInitializer initializer, InitParams params) throws RemoteException
   {
      current = Current.getCurrent();
      if (current == null)
      {
         TransactionFactory tm = new TransactionFactoryImpl();
         current = new Current(tm);

         // Change the timeout only if JOTM is not initialized yet
         if (params != null && params.getValueParam("timeout") != null)
         {
            int t = Integer.parseInt(params.getValueParam("timeout").getValue());
            current.setDefaultTimeout(t);
         }
      }
      else
      {
         log.info("Use externally initialized JOTM: " + current);
      }
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.transaction.TransactionService#getTransactionManager
    * ()
    */
   public TransactionManager getTransactionManager()
   {
      return current;
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.transaction.TransactionService#getUserTransaction
    * ()
    */
   public UserTransaction getUserTransaction()
   {
      return current;
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.transaction.TransactionService#enlistResource(
    * javax.transaction.xa.XAResource)
    */
   public void enlistResource(ExoResource exores) throws RollbackException, SystemException
   {
      XAResource xares = exores.getXAResource();
      ResourceEntry entry = new ResourceEntry(exores);
      exores.setPayload(entry);
      Transaction tx = getTransactionManager().getTransaction();
      if (tx != null)
         current.getTransaction().enlistResource(xares);
      else
         current.connectionOpened(entry);

      //
      entry.jotmResourceList = popThreadLocalRMEventList();
      pushThreadLocalRMEventList(entry.jotmResourceList);
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.transaction.TransactionService#delistResource(
    * javax.transaction.xa.XAResource)
    */
   public void delistResource(ExoResource exores) throws RollbackException, SystemException
   {
      XAResource xares = exores.getXAResource();
      ResourceEntry entry = (ResourceEntry)exores.getPayload();
      Transaction tx = getTransactionManager().getTransaction();
      if (tx != null)
         current.getTransaction().delistResource(xares, XAResource.TMNOFLAGS);
      else
         current.connectionClosed(entry);

      //
      exores.setPayload(null);
      if (entry != null && entry.jotmResourceList != null)
      {
         entry.jotmResourceList.remove(xares);
      }
   }

   /*
    * (non-Javadoc)
    * @see org.exoplatform.services.transaction.TransactionService#createXid()
    */
   public Xid createXid()
   {
      return new XidImpl();
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.transaction.TransactionService#getDefaultTimeout()
    */
   public int getDefaultTimeout()
   {
      return current.getDefaultTimeout();
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.transaction.TransactionService#setTransactionTimeout
    * (int)
    */
   public void setTransactionTimeout(int seconds) throws SystemException
   {
      current.setTransactionTimeout(seconds);
   }

   /**
    * Push a new event list on the stack of thread local resource event sets. The
    * list must contain only <code>ResourceManagerEvent</code> objects.
    * 
    * @param eventList the possibly null list of events to store forecoming
    *          <code>ResourceManagerEvent</code> events occuring in the current
    *          thread.
    */
   public void pushThreadLocalRMEventList(List eventList)
   {
      current.pushThreadLocalRMEventList(eventList);
   };

   /**
    * Pop the current set from the stack of thread local resource event sets The
    * list contains <code>ResourceManagerEvent</code> objects.
    * 
    * @return The possibly null <code>ResourceManagerEvent</code> list of events
    *         that have occured in the current thread since the last call of
    *         <code>pushThreadLocalRMEventList</code> or since the thread
    *         started.
    */
   public List popThreadLocalRMEventList()
   {
      return current.popThreadLocalRMEventList();
   };
}
