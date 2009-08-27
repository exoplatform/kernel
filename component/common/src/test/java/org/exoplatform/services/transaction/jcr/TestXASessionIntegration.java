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

import junit.framework.TestCase;

import org.exoplatform.services.transaction.TransactionService;
import org.exoplatform.services.transaction.impl.jotm.TransactionServiceJotmImpl;

/**
 * Integration test between the behavior of an XASession implementing the ExoResource interface
 * and the JOTM implementation.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestXASessionIntegration extends TestCase
{

   /** . */
   private TransactionService txservice;

   @Override
   protected void setUp() throws Exception
   {
      txservice = new TransactionServiceJotmImpl(null, null);
   }

   public void testLoginLogout() throws Exception
   {
      XASession session = new XASession(txservice);
      txservice.enlistResource(session);
      txservice.delistResource(session);
   }

   public void testLogout() throws Exception
   {
      XASession session = new XASession(txservice);
      txservice.delistResource(session);
   }
}
