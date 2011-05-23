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

import org.exoplatform.services.transaction.impl.AbstractTransactionService;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class JBossTransactionService extends AbstractTransactionService
{

   /**
    * {@inheritDoc}
    */
   public TransactionManager findTransactionManager()
   {
      try
      {
         return (TransactionManager)new InitialContext().lookup("java:/TransactionManager");
      }
      catch (NamingException e)
      {
         throw new IllegalStateException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public UserTransaction findUserTransaction()
   {
      try
      {
         return (UserTransaction)new InitialContext().lookup("java:comp/UserTransaction");
      }
      catch (NamingException e)
      {
         throw new IllegalStateException(e);
      }
   }

   /**
    * {@inheritDoc}
    */
   @Override
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
}
