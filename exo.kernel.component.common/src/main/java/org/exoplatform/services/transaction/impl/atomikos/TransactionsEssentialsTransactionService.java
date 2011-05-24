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
package org.exoplatform.services.transaction.impl.atomikos;

import com.atomikos.icatch.jta.UserTransactionManager;

import org.exoplatform.services.transaction.TransactionService;
import org.exoplatform.services.transaction.impl.AbstractTransactionService;
import org.picocontainer.Startable;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * An implementation of a {@link TransactionService} for TransactionsEssentials from Atomikos
 * to be used in standalone mode
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TransactionsEssentialsTransactionService extends AbstractTransactionService implements Startable
{

   /**
    * @see org.exoplatform.services.transaction.impl.AbstractTransactionService#findTransactionManager()
    */
   @Override
   protected TransactionManager findTransactionManager() throws Exception
   {
      UserTransactionManager tm = new UserTransactionManager();
      tm.init();
      return tm;
   }

   /**
    * @see org.exoplatform.services.transaction.impl.AbstractTransactionService#findUserTransaction()
    */
   @Override
   protected UserTransaction findUserTransaction() throws Exception
   {
      return (UserTransaction)getTransactionManager();
   }

   /**
    * @see org.picocontainer.Startable#start()
    */
   public void start()
   {
   }

   /**
    * @see org.picocontainer.Startable#stop()
    */
   public void stop()
   {
      if (isTMInitialized())
      {
         TransactionManager tm = getTransactionManager();
         if (tm instanceof UserTransactionManager)
         {
            UserTransactionManager utm = (UserTransactionManager)tm;
            utm.close();
         }
      }
   }
}
