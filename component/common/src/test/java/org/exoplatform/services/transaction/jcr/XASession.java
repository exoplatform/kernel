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
package org.exoplatform.services.transaction.jcr;

import org.exoplatform.services.transaction.ExoResource;
import org.exoplatform.services.transaction.TransactionService;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Simulate an xa resource.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class XASession implements XAResource, ExoResource
{

   /** Transaction service. */
   private final TransactionService tService;

   /** Start flags. */
   private int startFlags;

   /** . */
   private Object payload;

   /** . */
   private int txTimeout;

   public XASession(TransactionService tService)
   {
      this.tService = tService;
      this.txTimeout = tService.getDefaultTimeout();
      this.payload = null;
      this.startFlags = TMNOFLAGS;
   }

   // ExoResource implementation

   public XAResource getXAResource()
   {
      return this;
   }

   public void enlistResource() throws XAException
   {
      try
      {
         tService.enlistResource(this);
      }
      catch (RollbackException e)
      {
         throw new XAException(e.getMessage());
      }
      catch (SystemException e)
      {
         throw new XAException(e.getMessage());
      }
   }

   public void delistResource() throws XAException
   {
      try
      {
         tService.delistResource(this);
      }
      catch (RollbackException e)
      {
         throw new XAException(e.getMessage());
      }
      catch (SystemException e)
      {
         throw new XAException(e.getMessage());
      }
   }

   public Object getPayload()
   {
      return payload;
   }

   public void setPayload(Object payload)
   {
      this.payload = payload;
   }

   // XAResource implementation

   public void commit(Xid xid, boolean b) throws XAException
   {
      try
      {
         tService.getTransactionManager().commit();
      }
      catch (RollbackException e)
      {
         throw new XAException(XAException.XA_RBOTHER);
      }
      catch (HeuristicRollbackException e)
      {
         throw new XAException(XAException.XA_RBOTHER);
      }
      catch (HeuristicMixedException e)
      {
         throw new XAException(XAException.XA_RBOTHER);
      }
      catch (SystemException e)
      {
         throw new XAException(XAException.XA_RBOTHER);
      }
   }

   public void end(Xid xid, int flags) throws XAException
   {
      startFlags = flags;
   }

   public void forget(Xid xid) throws XAException
   {
   }

   public int getTransactionTimeout() throws XAException
   {
      return txTimeout;
   }

   public boolean isSameRM(XAResource resource) throws XAException
   {
      return resource == this;
   }

   public int prepare(Xid xid) throws XAException
   {
      return XA_OK;
   }

   public Xid[] recover(int i) throws XAException
   {
      return null;
   }

   public void rollback(Xid xid) throws XAException
   {
   }

   public void start(Xid xid, int flags) throws XAException
   {
      startFlags = flags;
   }

   public boolean setTransactionTimeout(int seconds) throws XAException
   {
      try
      {
         tService.setTransactionTimeout(seconds);
      }
      catch (SystemException e)
      {
         throw new XAException(e.getMessage());
      }
      this.txTimeout = seconds;
      return true;
   }
}
