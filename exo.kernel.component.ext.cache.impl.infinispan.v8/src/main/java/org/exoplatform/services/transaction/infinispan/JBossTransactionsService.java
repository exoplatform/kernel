/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.services.transaction.infinispan;

import org.exoplatform.container.xml.InitParams;
import org.infinispan.transaction.lookup.TransactionManagerLookup;

import javax.transaction.UserTransaction;

/**
 * Add the specific part for Arjuna
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class JBossTransactionsService extends GenericTransactionService
{

   public JBossTransactionsService(TransactionManagerLookup tmLookup)
   {
      super(tmLookup);
   }

   public JBossTransactionsService(TransactionManagerLookup tmLookup, InitParams params)
   {
      super(tmLookup, params);
   }

   /**
    * {@inheritDoc} 
    */
   @Override
   protected UserTransaction findUserTransaction() throws Exception
   {
      return com.arjuna.ats.jta.UserTransaction.userTransaction();
   }
}
