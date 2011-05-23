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
package org.exoplatform.services.transaction.impl;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.transaction.TransactionService;

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

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
 * This abstract class implements the main logic of all the methods expected for a
 * {@link TransactionService}. If you intend to use a {@link TransactionManager} in
 * standalone mode (not manager by your Application Server), you can set the
 * transaction timeout thanks to the <code>value-param</code> called <code>timeout</code>
 * the value of this parameter is expressed in seconds.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractTransactionService implements TransactionService
{
   /**
    * The logger 
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.AbstractTransactionService");

   /**
    * The default value of a transaction timeout in seconds
    */
   private static final int DEFAULT_TIME_OUT = 60;

   /**
    * The default timeout
    */
   protected final int defaultTimeout;

   /**
    * Indicates if the timeout has to be enforced
    */
   protected final boolean forceTimeout;

   /**
    * The current Transaction Manager
    */
   private volatile TransactionManager tm;

   /**
    * The current User Transaction
    */
   private volatile UserTransaction ut;

   public AbstractTransactionService()
   {
      this(null);
   }

   public AbstractTransactionService(InitParams params)
   {
      if (params != null && params.getValueParam("timeout") != null)
      {
         this.defaultTimeout = Integer.parseInt(params.getValueParam("timeout").getValue());
         this.forceTimeout = true;
      }
      else
      {
         this.defaultTimeout = DEFAULT_TIME_OUT;
         this.forceTimeout = false;
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean delistResource(final XAResource xares) throws RollbackException, SystemException
   {
      TransactionManager tm = getTransactionManager();
      final Transaction tx = tm.getTransaction();
      if (tx != null)
      {
         PrivilegedExceptionAction<Boolean> action = new PrivilegedExceptionAction<Boolean>()
         {
            public Boolean run() throws Exception
            {
               int flag = XAResource.TMSUCCESS;
               switch (tx.getStatus())
               {
                  case Status.STATUS_MARKED_ROLLBACK:
                  case Status.STATUS_ROLLEDBACK:
                  case Status.STATUS_ROLLING_BACK: flag = XAResource.TMFAIL;                     
               }      
               return tx.delistResource(xares, flag);
            }
         };
         try
         {
            return AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();

            if (cause instanceof RollbackException)
            {
               throw (RollbackException)cause;
            }
            else if (cause instanceof IllegalStateException)
            {
               throw (IllegalStateException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }
      else
      {
         throw new IllegalStateException("Could not delist the XA Resource since there is no active session");
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean enlistResource(final XAResource xares) throws RollbackException, SystemException
   {
      TransactionManager tm = getTransactionManager();
      final Transaction tx = tm.getTransaction();
      if (tx != null)
      {
         PrivilegedExceptionAction<Boolean> action = new PrivilegedExceptionAction<Boolean>()
         {
            public Boolean run() throws Exception
            {               
               return tx.enlistResource(xares);
            }
         };
         try
         {
            return AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();

            if (cause instanceof RollbackException)
            {
               throw (RollbackException)cause;
            }
            else if (cause instanceof IllegalStateException)
            {
               throw (IllegalStateException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }
      else
      {
         throw new IllegalStateException("Could not enlist the XA Resource since there is no active session");
      }
   }

   /**
    * {@inheritDoc}
    */
   public int getDefaultTimeout()
   {
      return defaultTimeout;
   }

   /**
    * {@inheritDoc}
    */
   public final TransactionManager getTransactionManager()
   {
      if (tm == null)
      {
         synchronized (this)
         {
            if (tm == null)
            {
               TransactionManager tm;
               try
               {
                  tm = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<TransactionManager>()
                  {
                     public TransactionManager run() throws Exception
                     {
                        return findTransactionManager();
                     }
                  });
               }
               catch (Exception e)
               {
                  throw new RuntimeException("Transaction manager not found", e);
               }
               if (forceTimeout)
               {
                  // Only set the timeout when a timeout has been given into the
                  // configuration otherwise we assume that the value will be
                  // set at the AS level
                  tm = new TransactionManagerTxTimeoutAware(tm, defaultTimeout);
               }
               this.tm = tm;
            }
         }
      }
      return tm;
   }

   /**
    * Indicates whether or not the {@link TransactionManager} has been initialized
    */
   protected boolean isTMInitialized()
   {
      return tm != null;
   }
   
   /**
    * This method will try to find the current {@link TransactionManager}
    * @return the current {@link TransactionManager}
    * @throws Exception if an error occurs while looking for the {@link TransactionManager}
    */
   protected abstract TransactionManager findTransactionManager() throws Exception;
   
   /**
    * {@inheritDoc}
    */
   public final UserTransaction getUserTransaction()
   {
      if (ut == null)
      {
         synchronized (this)
         {
            if (ut == null)
            {
               UserTransaction ut;
               try
               {
                  ut = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<UserTransaction>()
                  {
                     public UserTransaction run() throws Exception
                     {
                        return findUserTransaction();
                     }
                  });
               }
               catch (Exception e)
               {
                  throw new RuntimeException("UserTransaction not found", e);
               }
               this.ut = ut;
            }
         }         
      }
      return ut;
   }


   /**
    * This method will try to find the current {@link UserTransaction}, by default it will
    * simply wraps a {@link TransactionManager} 
    * @return the current {@link UserTransaction}
    * @throws Exception if an error occurs while looking for the {@link UserTransaction}
    */
   protected UserTransaction findUserTransaction() throws Exception
   {
      return new UserTransactionWrapper(getTransactionManager());
   }
      
   /**
    * {@inheritDoc}
    */
   public void setTransactionTimeout(int seconds) throws SystemException
   {
      TransactionManager tm = getTransactionManager();
      tm.setTransactionTimeout(seconds);
   }

   /**
    * This class is used to enforce the {@link Transaction} timeout when a new transaction is
    * created through the nested {@link TransactionManager}
    * 
    * Created by The eXo Platform SAS
    * Author : Nicolas Filotto 
    *          nicolas.filotto@exoplatform.com
    * 1 f�vr. 2010
    */
   private static class TransactionManagerTxTimeoutAware implements TransactionManager
   {
      /**
       * The nested {@link TransactionManager}
       */
      private final TransactionManager tm;

      /**
       * The default timeout of the {@link Transaction}
       */
      private final int defaultTimeout;

      /**
       * This is used to know if a timeout has already been set for the next transaction
       */
      private final ThreadLocal<Boolean> timeoutHasBeenSet = new ThreadLocal<Boolean>();

      public TransactionManagerTxTimeoutAware(TransactionManager tm, int defaultTimeout)
      {
         this.tm = tm;
         this.defaultTimeout = defaultTimeout;
      }

      /**
       * {@inheritDoc}
       */
      public void begin() throws NotSupportedException, SystemException
      {
         if (timeoutHasBeenSet.get() != null)
         {
            // clean the ThreadLocal
            timeoutHasBeenSet.set(null);
         }
         else
         {
            try
            {
               // Set the default transaction timeout
               tm.setTransactionTimeout(defaultTimeout);
            }
            catch (Exception e)
            {
               LOG.warn("Cannot set the transaction timeout", e);
            }
         }

         // Start the transaction
         PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               tm.begin();
               return null;
            }
         };
         try
         {
            AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof NotSupportedException)
            {
               throw (NotSupportedException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * {@inheritDoc}
       */
      public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
         SecurityException, IllegalStateException, SystemException
      {
         PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               tm.commit();
               return null;
            }
         };
         try
         {
            AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof RollbackException)
            {
               throw (RollbackException)cause;
            }
            else if (cause instanceof HeuristicMixedException)
            {
               throw (HeuristicMixedException)cause;
            }
            else if (cause instanceof HeuristicRollbackException)
            {
               throw (HeuristicRollbackException)cause;
            }
            else if (cause instanceof SecurityException)
            {
               throw (SecurityException)cause;
            }
            else if (cause instanceof IllegalStateException)
            {
               throw (IllegalStateException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * {@inheritDoc}
       */
      public int getStatus() throws SystemException
      {
         return tm.getStatus();
      }

      /**
       * {@inheritDoc}
       */
      public Transaction getTransaction() throws SystemException
      {
         try
         {
            return SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Transaction>()
            {
               public Transaction run() throws Exception
               {
                  return tm.getTransaction();
               }
            });
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * {@inheritDoc}
       */
      public void resume(final Transaction tx) throws InvalidTransactionException, IllegalStateException,
         SystemException
      {
         PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               tm.resume(tx);
               return null;
            }
         };
         try
         {
            AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof InvalidTransactionException)
            {
               throw (InvalidTransactionException)cause;
            }
            else if (cause instanceof IllegalStateException)
            {
               throw (IllegalStateException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * {@inheritDoc}
       */
      public void rollback() throws IllegalStateException, SecurityException, SystemException
      {
         PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               tm.rollback();
               return null;
            }
         };
         try
         {
            AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof IllegalStateException)
            {
               throw (IllegalStateException)cause;
            }
            else if (cause instanceof SecurityException)
            {
               throw (SecurityException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * {@inheritDoc}
       */
      public void setRollbackOnly() throws IllegalStateException, SystemException
      {
         tm.setRollbackOnly();
      }

      /**
       * {@inheritDoc}
       */
      public void setTransactionTimeout(int timeout) throws SystemException
      {
         tm.setTransactionTimeout(timeout);
         timeoutHasBeenSet.set(true);
      }

      /**
       * {@inheritDoc}
       */
      public Transaction suspend() throws SystemException
      {
         PrivilegedExceptionAction<Transaction> action = new PrivilegedExceptionAction<Transaction>()
         {
            public Transaction run() throws Exception
            {
               return tm.suspend();
            }
         };
         try
         {
            return AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }
   }
   
   /**
    * This class is used to propose a default implementation of a {@link UserTransaction}
    * from the {@link TransactionManager}
    * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
    * @version $Id$
    *
    */
   private static class UserTransactionWrapper implements UserTransaction
   {

      /**
       * The {@link TransactionManager} that we will use to simulate a {@link UserTransaction}
       */
      private final TransactionManager tm;
      
      /**
       * Default Constructor
       * @param tm
       */
      public UserTransactionWrapper(TransactionManager tm)
      {
         this.tm = tm;
      }
      
      /**
       * @see javax.transaction.UserTransaction#begin()
       */
      public void begin() throws NotSupportedException, SystemException
      {
         PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               tm.begin();
               return null;
            }
         };
         try
         {
            AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof NotSupportedException)
            {
               throw (NotSupportedException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * @see javax.transaction.UserTransaction#commit()
       */
      public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
         SecurityException, IllegalStateException, SystemException
      {
         PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               tm.commit();
               return null;
            }
         };
         try
         {
            AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof RollbackException)
            {
               throw (RollbackException)cause;
            }
            else if (cause instanceof HeuristicMixedException)
            {
               throw (HeuristicMixedException)cause;
            }
            else if (cause instanceof HeuristicRollbackException)
            {
               throw (HeuristicRollbackException)cause;
            }
            else if (cause instanceof SecurityException)
            {
               throw (SecurityException)cause;
            }
            else if (cause instanceof IllegalStateException)
            {
               throw (IllegalStateException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * @see javax.transaction.UserTransaction#rollback()
       */
      public void rollback() throws IllegalStateException, SecurityException, SystemException
      {
         PrivilegedExceptionAction<Object> action = new PrivilegedExceptionAction<Object>()
         {
            public Object run() throws Exception
            {
               tm.rollback();
               return null;
            }
         };
         try
         {
            AccessController.doPrivileged(action);
         }
         catch (PrivilegedActionException pae)
         {
            Throwable cause = pae.getCause();
            if (cause instanceof IllegalStateException)
            {
               throw (IllegalStateException)cause;
            }
            else if (cause instanceof SecurityException)
            {
               throw (SecurityException)cause;
            }
            else if (cause instanceof SystemException)
            {
               throw (SystemException)cause;
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
      }

      /**
       * @see javax.transaction.UserTransaction#setRollbackOnly()
       */
      public void setRollbackOnly() throws IllegalStateException, SystemException
      {
         tm.setRollbackOnly();
      }

      /**
       * @see javax.transaction.UserTransaction#getStatus()
       */
      public int getStatus() throws SystemException
      {
         return tm.getStatus();
      }

      /**
       * @see javax.transaction.UserTransaction#setTransactionTimeout(int)
       */
      public void setTransactionTimeout(int timeout) throws SystemException
      {
         tm.setTransactionTimeout(timeout);
      }      
   }
}
