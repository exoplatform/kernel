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
package org.exoplatform.services.transaction.infinispan;

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.transaction.TransactionService;
import org.infinispan.transaction.lookup.TransactionManagerLookup;

import java.lang.reflect.Method;

import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

/**
 * This class is used to replace the one provided by default in ISPN 5.0 since
 * it implicitly requires that ISPN is initialized before the rest which is actually
 * the exact opposite. Indeed Arjuna accessors are initialized in the init method which is
 * called at ISPN initialization but we expect to get the {@link TransactionManager}
 * from it through the {@link TransactionService} before initializing the JCR so before
 * initializing ISPN.
 * 
 * The code below is a simple copy/paste of the code of 
 * {@link org.infinispan.transaction.lookup.JBossStandaloneJTAManagerLookup} of ISPN 4.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class JBossStandaloneJTAManagerLookup implements TransactionManagerLookup
{
   /**
    * The logger 
    */
   private static final Log LOG = //NOSONAR
      ExoLogger.getLogger("exo.jcr.component.core.JBossStandaloneJTAManagerLookup");//NOSONAR

   private Method manager, user;

   public JBossStandaloneJTAManagerLookup()
   {
      try
      {
         manager = loadClassStrict("com.arjuna.ats.jta.TransactionManager").getMethod("transactionManager");
         user = loadClassStrict("com.arjuna.ats.jta.UserTransaction").getMethod("userTransaction");
      }
      catch (ClassNotFoundException e)
      {
         throw new RuntimeException(e);//NOSONAR
      }
      catch (SecurityException e)
      {
         throw new RuntimeException(e);//NOSONAR
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException(e);//NOSONAR
      }
   }

   public TransactionManager getTransactionManager() throws Exception
   {
      TransactionManager tm = (TransactionManager)manager.invoke(null);
      if (tm == null && LOG.isWarnEnabled())
      {
         LOG.warn("The transaction manager could not be found");
      }
      return tm;
   }

   public UserTransaction getUserTransaction() throws Exception
   {
      UserTransaction ut = (UserTransaction)user.invoke(null);
      if (ut == null && LOG.isWarnEnabled())
      {
         LOG.warn("The user transaction could not be found");
      }
      return ut;
   }

   /**
    * Loads the specified class using this class's classloader, or, if it is <code>null</code> (i.e. this class was
    * loaded by the bootstrap classloader), the system classloader. <br> If loadtime instrumentation via
    * GenerateInstrumentedClassLoader is used, this class may be loaded by the bootstrap classloader. <br>
    *
    * @param classname name of the class to load
    * @return the class
    * @throws ClassNotFoundException
    */
   private static Class<?> loadClassStrict(String classname) throws ClassNotFoundException
   {
      return ClassLoading.loadClass(classname, JBossStandaloneJTAManagerLookup.class);
   }
}
