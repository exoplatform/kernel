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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.objectweb.jotm.Current;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Created by The eXo Platform SAS .<br/> Prerequisites: default-context-factory
 * = org.exoplatform.services.naming.impl.SimpleContextFactory
 * 
 * @author <a href="mailto:geaz@users.sourceforge.net">Gennady Azarenkov </a>
 * @version $Id: $
 */
public class TransactionTest extends TestCase
{

   private static Log log = ExoLogger.getLogger("tx.TransactionTest");

   private StandaloneContainer container;

   private TransactionService ts;

   public void setUp() throws Exception
   {

      StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");

      container = StandaloneContainer.getInstance();

      ts = (TransactionService)container.getComponentInstanceOfType(TransactionService.class);

   }

   public void testUserTransactionBeforeResource() throws Exception
   {

      UserTransaction ut = ts.getUserTransaction();
      ut.begin();

      Current c = (Current)ut;
      // System.out.printf(">>>>>>>>>>>"+c.getAllTx()[0]);
      // c.getAllXid();
      // System.out.printf(">>>>>>>>>>>"+c.getAllXid());
      // fail();

      // c.getTransactionManager().

      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      ts.enlistResource(xares);

      xares.setFlag(5);
      assertEquals(0, xares.getOldFlag());
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());

      ts.delistResource(xares);

   }

   public void testUserTransactionAfterResource() throws Exception
   {

      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      ts.enlistResource(xares);

      assertEquals(0, xares.getFlag());
      UserTransaction ut = ts.getUserTransaction();

      ut.begin();
      xares.setFlag(5);
      assertEquals(0, xares.getOldFlag());
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());

      ts.delistResource(xares);

   }

   public void testUserTransactionRollback() throws Exception
   {

      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      ts.enlistResource(xares);

      assertEquals(0, xares.getFlag());
      UserTransaction ut = ts.getUserTransaction();
      ut.begin();
      xares.setFlag(5);
      assertEquals(5, xares.getFlag());
      ut.rollback();
      assertEquals(0, xares.getFlag());
      assertEquals(0, xares.getOldFlag());

      ts.delistResource(xares);
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
      // assertEquals(5, xares.getFlag());
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());

      ts.delistResource(xares);

   }

   public void testReuseUT() throws Exception
   {

      InitialContext ctx = new InitialContext();
      Object obj = ctx.lookup("UserTransaction");
      UserTransaction ut = (UserTransaction)obj;

      ut.begin();
      XAResourceTestImpl xares = new XAResourceTestImpl(ts); //(XAResourceTestImpl)f
      // .createResoure();
      ts.enlistResource(xares);

      xares.setFlag(5);
      ut.commit();
      assertEquals(5, xares.getFlag());
      assertEquals(5, xares.getOldFlag());

      // In a case of reusing Have to enlist the resource once again!
      ts.enlistResource(xares);

      ut.begin();
      xares.setFlag(2);
      ut.commit();
      assertEquals(2, xares.getFlag());
      assertEquals(2, xares.getOldFlag());

      ts.delistResource(xares);

   }

   public void testGenerateXid() throws Exception
   {
      Xid id = ts.createXid();
      log.info("XID ==== " + id);
      assertNotNull(id);
   }

   public void testSimpleGlobalTransaction() throws Exception
   {
      Xid id = ts.createXid();
      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      xares.start(id, XAResource.TMNOFLAGS);
      assertEquals(0, xares.getFlag());
      xares.setFlag(1);
      xares.commit(id, true);
      assertEquals(1, xares.getFlag());
      assertEquals(1, xares.getOldFlag());
   }

   public void test2GlobalTransactions() throws Exception
   {
      Xid id1 = ts.createXid();
      XAResourceTestImpl xares = new XAResourceTestImpl(ts);
      xares.start(id1, XAResource.TMNOFLAGS);
      assertEquals(0, xares.getFlag());
      xares.setFlag(1);
      xares.end(id1, XAResource.TMSUSPEND);
      assertEquals(1, xares.getFlag());

      Xid id2 = ts.createXid();
      xares.start(id2, XAResource.TMNOFLAGS);
      xares.setFlag(2);

      // End work
      xares.end(id2, XAResource.TMSUCCESS);

      // Resume work with former transaction
      xares.start(id1, XAResource.TMRESUME);

      // Commit work recorded when associated with xid2
      xares.commit(id2, true);
      assertEquals(2, xares.getFlag());
      assertEquals(2, xares.getOldFlag());
   }

}
