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
package org.exoplatform.services.transaction.impl.jboss;

import org.exoplatform.services.transaction.ExoResource;
import org.exoplatform.services.transaction.TransactionService;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JBossTransactionService implements TransactionService
{

   /** . */
   private volatile TransactionManager tm;

   public TransactionManager getTransactionManager()
   {
      if (tm == null)
      {
         try
         {
            tm = (TransactionManager)new InitialContext().lookup("java:/TransactionManager");
         }
         catch (NamingException e)
         {
            throw new IllegalStateException(e);
         }
      }
      return tm;
   }

   public UserTransaction getUserTransaction()
   {
      try
      {
         return (UserTransaction)new InitialContext().lookup("java:/TransactionManager");
      }
      catch (NamingException e)
      {
         throw new IllegalStateException(e);
      }
   }

   public int getDefaultTimeout()
   {
      try
      {
         MBeanServer server = (MBeanServer)MBeanServerFactory.findMBeanServer(null).iterator().next();
         return (Integer)server.getAttribute(ObjectName.getInstance("jboss:service=TransactionManager"),
            "TransactionTimeout");
      }
      catch (Exception e)
      {
         throw new IllegalStateException(e);
      }
   }

   public void setTransactionTimeout(int seconds) throws SystemException
   {
      TransactionManager tm = getTransactionManager();
      tm.setTransactionTimeout(seconds);
   }

   public void enlistResource(ExoResource exores) throws RollbackException, SystemException
   {
      TransactionManager tm = getTransactionManager();
      Transaction tx = tm.getTransaction();
      tx.enlistResource(exores.getXAResource());
   }

   public void delistResource(ExoResource exores) throws RollbackException, SystemException
   {
      TransactionManager tm = getTransactionManager();
      Transaction tx = tm.getTransaction();
      tx.delistResource(exores.getXAResource(), XAResource.TMNOFLAGS);
   }

   public Xid createXid()
   {
      // Convenient method used by JCR tests to manufacture an xid
      // it should be removed
      //
      throw new UnsupportedOperationException("Only used by JCR impl in tests");
   }
}
