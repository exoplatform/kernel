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
package org.exoplatform.services.transaction;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

/**
 * This class describes all the actions that are not supposed to be called
 * within a transaction
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class ActionNonTxAware<R, A, E extends Exception>
{
   /**
    * The logger
    */
   protected static final Log LOG = ExoLogger.getLogger("exo.jcr.component.core.ActionNonTxAware");
   
   /**
    * Executes the action outside the context of the current tx
    * @param arg the argument to use to execute the action
    * @return the result of the action
    * @throws E if an error occurs while executing the action
    */
   public R run(A... arg) throws E
   {
      final TransactionManager tm = getTransactionManager();
      Transaction tx = null;
      try
      {
         if (tm != null)
         {
            try
            {
               tx = tm.suspend();
            }
            catch (SystemException e)
            {
               LOG.warn("Cannot suspend the current transaction", e);
            }
         }
         return execute(arg);
      }
      finally
      {
         if (tx != null)
         {
            try
            {
               tm.resume(tx);
            }
            catch (Exception e)
            {
               LOG.warn("Cannot resume the current transaction", e);
            }
         }
      }      
   }
   
   /**
    * Executes the action outside the context of the current tx. This
    * method is equivalent to {@link ActionNonTxAware#run(Object[])}} but
    * with <tt>null</tt> as parameter.
    * @return the result of the action
    * @throws E if an error occurs while executing the action
    */
   public R run() throws E
   {
      return run((A[])null);     
   }   
   
   /**
    * Executes the action
    * @param arg the argument to use to execute the action
    * @return the result of the action
    * @throws E if an error occurs while executing the action
    */
   protected R execute(A... arg) throws E
   {
      if (arg == null || arg.length == 0)
      {
         return execute((A)null);
      }
      return execute(arg[0]);
   }
   
   /**
    * Executes the action
    * @param arg the argument to use to execute the action
    * @return the result of the action
    * @throws E if an error occurs while executing the action
    */
   protected R execute(A arg) throws E
   {
      return null;
   }
   
   /**
    * Gives the Transaction Manager that will be used while executing the action
    * @return the {@link TransactionManager} to use
    */
   protected abstract TransactionManager getTransactionManager();
}
