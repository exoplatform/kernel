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
package org.exoplatform.services.transaction;

import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

/**
 * Created by The eXo Platform SAS.<br> The transaction service
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: $
 */

public interface TransactionService
{

   /**
    * @return TransactionManager
    */
   TransactionManager getTransactionManager();

   /**
    * @return UserTransaction
    */
   UserTransaction getUserTransaction();

   /**
    * @return default timeout in seconds
    */
   int getDefaultTimeout();

   /**
    * Sets timeout in seconds,
    * 
    * @param seconds int
    * @throws SystemException
    */
   void setTransactionTimeout(int seconds) throws SystemException;

   /**
    * Enlists XA resource in transaction manager.
    * 
    * @param xares XAResource
    * @return <i>true</i> if the resource was enlisted successfully; otherwise
    *    <i>false</i>.
    *
    * @exception RollbackException Thrown to indicate that
    *    the transaction has been marked for rollback only.
    *
    * @exception IllegalStateException Thrown if the transaction in the
    *    target object is in the prepared state or the transaction is
    *    inactive.
    *
    * @exception SystemException Thrown if the transaction manager
    *    encounters an unexpected error condition.
    */
   boolean enlistResource(XAResource xares) throws RollbackException, SystemException, IllegalStateException;

   /**
    * Delists XA resource from transaction manager.
    * 
    * @param xares XAResource
    * @exception IllegalStateException Thrown if the transaction in the
    *    target object is inactive.
    *
    * @exception SystemException Thrown if the transaction manager
    *    encounters an unexpected error condition.
    *
    * @return <i>true</i> if the resource was delisted successfully; otherwise
    *     <i>false</i>.
    */
   boolean delistResource(XAResource xares) throws RollbackException, SystemException, IllegalStateException;

}
