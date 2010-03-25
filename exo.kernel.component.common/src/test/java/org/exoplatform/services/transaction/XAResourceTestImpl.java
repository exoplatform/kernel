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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: $
 */

public class XAResourceTestImpl implements ExoResource, XAResource
{

   private static Log log = ExoLogger.getLogger("exo.kernel.component.common.XAResourceTestImpl");

   private int timeout = 5;

   // private transient TransactionService ts;

   private int oldFlag;

   private int flag = oldFlag = 0;

   private final TransactionService ts;

   private Object payload;

   public XAResourceTestImpl(TransactionService ts)
   {
      this.ts = ts;
   }

   // public XAResourceTestImpl(TransactionService ts)
   // throws RollbackException, SystemException {
   // this.ts = ts;
   // ts.enlistResource(this);
   // }

   public XAResource getXAResource()
   {
      return this;
   }

   public void commit(Xid arg0, boolean arg1) throws XAException
   {
      oldFlag = flag;
      log.info("Commit " + arg0);
   }

   public void end(Xid arg0, int arg1) throws XAException
   {
      log.info("End " + arg0);
   }

   public void forget(Xid arg0) throws XAException
   {
      log.info("Forget " + arg0);
   }

   public int getTransactionTimeout() throws XAException
   {
      return timeout;
   }

   public boolean isSameRM(XAResource arg0) throws XAException
   {
      return false;
   }

   public int prepare(Xid arg0) throws XAException
   {
      log.info("Prepare " + arg0);
      return Status.STATUS_PREPARED;
   }

   public Xid[] recover(int arg0) throws XAException
   {
      log.info("recover " + arg0);
      return null;
   }

   public void rollback(Xid arg0) throws XAException
   {
      flag = oldFlag;
      log.info("Rollback " + arg0);
   }

   public boolean setTransactionTimeout(int arg0) throws XAException
   {
      this.timeout = arg0;
      return true;
   }

   public void start(Xid arg0, int arg1) throws XAException
   {
      log.info("Start " + arg0);
   }

   public int getFlag()
   {
      return flag;
   }

   public void setFlag(int flag)
   {
      this.flag = flag;
   }

   public void enlistResource() throws XAException
   {
      try
      {
         ts.enlistResource(this);
      }
      catch (IllegalStateException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (RollbackException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
      catch (SystemException e)
      {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   public void delistResource() throws XAException
   {
   }

   public int getOldFlag()
   {
      return oldFlag;
   }

   public Object getPayload()
   {
      return payload;
   }

   public void setPayload(Object payload)
   {
      this.payload = payload;
   }
}
