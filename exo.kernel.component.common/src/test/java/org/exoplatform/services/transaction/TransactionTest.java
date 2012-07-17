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

import junit.framework.TestCase;

import org.exoplatform.container.StandaloneContainer;

import javax.naming.InitialContext;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * Created by The eXo Platform SAS .<br/> Prerequisites: default-context-factory
 * = org.exoplatform.services.naming.impl.SimpleContextFactory
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: $
 */
public class TransactionTest extends TestCase
{

   private StandaloneContainer container;

   private TransactionService ts;

   public void setUp() throws Exception
   {

      StandaloneContainer.setConfigurationPath("src/test/resources/conf/standalone/test-configuration.xml");

      container = StandaloneContainer.getInstance();

      ts = (TransactionService)container.getComponentInstanceOfType(TransactionService.class);
      // Needed to ensure that the TM is properly initialized before calling testUserTransactionFromJndi
      // otherwise we can get a NPE
      ts.getUserTransaction();
   }

   public void testUserTransactionFromJndi() throws Exception
   {

      InitialContext ctx = new InitialContext();
      Object obj = ctx.lookup("UserTransaction");
      UserTransaction ut = (UserTransaction)obj;

      ut.begin();
      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      ts.enlistResource(xares);

      assertEquals(0, xares.getFlag());

      xares.setFlag(5);
      ts.delistResource(xares);
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());
   }

   public void testUserTransactionBeforeResource() throws Exception
   {

      UserTransaction ut = ts.getUserTransaction();
      ut.begin();

      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      ts.enlistResource(xares);

      xares.setFlag(5);
      assertEquals(0, xares.getOldFlag());
      ts.delistResource(xares);
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());
   }

   public void testUserTransactionAfterResource() throws Exception
   {

      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      try
      {
         ts.enlistResource(xares);
         fail("IllegalStateException is expected since it cannot be enlisted without an active tx");
      }
      catch (IllegalStateException e)
      {
         // OK
      }

      assertEquals(0, xares.getFlag());
      UserTransaction ut = ts.getUserTransaction();
      ut.begin();
      ts.enlistResource(xares);
      xares.setFlag(5);
      assertEquals(0, xares.getOldFlag());
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());

      try
      {
         ts.delistResource(xares);
         fail("IllegalStateException is expected since it cannot be delisted without an active tx");
      }
      catch (IllegalStateException e)
      {
         // OK
      }

   }

   public void testUserTransactionRollback() throws Exception
   {
      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      assertEquals(0, xares.getFlag());
      UserTransaction ut = ts.getUserTransaction();
      ut.begin();
      ts.enlistResource(xares);      
      xares.setFlag(5);
      assertEquals(5, xares.getFlag());
      ts.delistResource(xares);
      ut.rollback();
      assertEquals(0, xares.getFlag());
      assertEquals(0, xares.getOldFlag());

   }

   public void testReuseUT() throws Exception
   {

      InitialContext ctx = new InitialContext();
      Object obj = ctx.lookup("UserTransaction");
      UserTransaction ut = (UserTransaction)obj;

      ut.begin();
      XAResourceTestImpl xares = new XAResourceTestImpl(ts); 
      ts.enlistResource(xares);

      xares.setFlag(5);
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());

      ut.begin();
      // In a case of reusing Have to enlist the resource once again!
      ts.enlistResource(xares);
      xares.setFlag(2);
      ts.delistResource(xares);
      ut.commit();
      assertEquals(2, xares.getFlag());
      assertEquals(2, xares.getOldFlag());
   }

   public void testSimpleGlobalTransaction() throws Exception
   {
      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      UserTransaction ut = ts.getUserTransaction();
      ut.begin();
      ts.enlistResource(xares);      
      assertEquals(0, xares.getFlag());
      xares.setFlag(1);
      ut.commit();
      assertEquals(1, xares.getFlag());
      assertEquals(1, xares.getOldFlag());
   }

   public void test2GlobalTransactions() throws Exception
   {
      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      TransactionManager tm = ts.getTransactionManager();
      tm.begin();
      ts.enlistResource(xares);      
      assertEquals(0, xares.getFlag());
      xares.setFlag(1);
      Transaction tx = tm.suspend();
      assertEquals(1, xares.getFlag());

      tm.begin();
      ts.enlistResource(xares);      
      xares.setFlag(2);

      // End work
      tm.commit();

      // Resume work with former transaction
      tm.resume(tx);

      // Commit work recorded when associated with xid2
      tm.commit();
      assertEquals(2, xares.getFlag());
      assertEquals(2, xares.getOldFlag());
   }

}
