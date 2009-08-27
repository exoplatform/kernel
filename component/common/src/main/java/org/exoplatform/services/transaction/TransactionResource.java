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

/**
 * Created by The eXo Platform SAS.<br/> Internal resource manager for support
 * transaction
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: TransactionResource.java 6853 2006-07-07 11:41:24Z geaz $
 */

public interface TransactionResource
{

   /**
    * called when transaction is starting (by
    * javax.transaction.xa.XAResource#start(javax.transaction.xa.Xid, int))
    * 
    * @throws TransactionException
    */
   void start() throws TransactionException;

   /**
    * called when transaction is committing (by
    * javax.transaction.xa.XAResource#commit(javax.transaction.xa.Xid, boolean))
    * 
    * @throws TransactionException
    */
   void commit() throws TransactionException;

   /**
    * called when transaction is rolling back (by
    * javax.transaction.xa.XAResource#rollback(javax.transaction.xa.Xid))
    * 
    * @throws TransactionException
    */
   void rollback() throws TransactionException;

}
