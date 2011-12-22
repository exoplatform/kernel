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
package org.exoplatform.commons.test;

import java.io.IOException;
import java.io.InputStream;
import java.security.Permission;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * You can exclude methods by adding the file <code>tsm-excludes.properties</code> into the classpath. The expected format is:
 * <code>
 * ${fqn-of-the-class}.${method-name}=${called-method-name}(,${called-method-name})*
 * </code>
 * 
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: TestSecurityManager.java 2636 2010-06-16 14:18:23Z tolusha $
 *
 */
public class TestSecurityManager extends SecurityManager
{
   /**
    * Map of methods to exclude and for each method we define a list of method called to ignore
    */
   private final Map<String, Set<String>> excludes = getExcludes();

   /**
    * {@inheritDoc}
    */
   @Override
   public void checkPermission(Permission perm)
   {
      try
      {
         super.checkPermission(perm);
      }
      catch (SecurityException se)
      {
         Throwable e = se;

         boolean srcCode = false;
         boolean testCode = false;

         while (e != null)
         {
            StackTraceElement[] traceElements = e.getStackTrace();
            for (int i = 0; i < traceElements.length; i++)
            {
               String className = traceElements[i].getClassName();
               String fileName = traceElements[i].getFileName();
               String methodName = traceElements[i].getMethodName();
               if (excludes != null && i - 1 >= 0
                  && excludes.containsKey(className + "." + methodName)
                  && excludes.get(className + "." + methodName).contains(traceElements[i - 1].getMethodName()))
               {
                  // the called method is excluded thus we ignore the exception
                  return;
               }
               if (className.startsWith("org.exoplatform"))
               {
                  // known tests classes
                  if (fileName.startsWith("Test") || fileName.endsWith("Test.java")
                     || fileName.endsWith("TestBase.java") || fileName.endsWith("TestCase.java")
                     || fileName.equals("Probe.java") || fileName.equals("ExportBase.java")
                     || fileName.equals("AbstractTestContainer.java") || fileName.equals("ContainerBuilder.java")
                     || fileName.equals("WorkspaceStorageCacheBaseCase.java")
                     || fileName.equals("ExoRepositoryStub.java") || fileName.equals("CloseableDataSource.java"))
                  {
                     testCode = true;
                  }
                  else
                  {
                     srcCode = true;
                  }
               }
               else if (className.startsWith("org.apache.jackrabbit.test"))
               {
                  if (fileName.endsWith("Test.java") || fileName.equals("JCRTestResult.java")
                           || fileName.equals("RepositoryHelper.java") || fileName.equals("RepositoryStub.java"))
                  {
                     testCode = true;
                  }
               }
            }

            e = e.getCause();
         }

         // hide Exception if only test code exists
         if (!srcCode && testCode)
         {
            return;
         }
        
         // Only for debug purpose
         //if (!se
         // .getMessage()
         // .equals(
         //    "access denied (java.lang.RuntimePermission accessClassInPackage.com.sun.xml.internal.bind.v2.runtime.reflect)"))
         //{
         //    se.printStackTrace(); //NOSONAR
         //}

         throw se;
      }
   }

   /**
    * @return
    */
   private Map<String, Set<String>> getExcludes()
   {
      InputStream is = null;
      try
      {
         is = Thread.currentThread().getContextClassLoader().getResourceAsStream("tsm-excludes.properties");
      }
      catch (Exception e)
      {
         // ignore me
      }
      if (is != null)
      {
         try
         {
            System.out.println("A file 'tsm-excludes.properties' has been found"); //NOSONAR
            Properties p = new Properties();
            p.load(is);
            Map<String, Set<String>> excludes = new HashMap<String, Set<String>>();
            for (Object key : p.keySet())
            {
               String[] values = p.getProperty((String)key).split(",");
               excludes.put((String)key, new HashSet<String>(Arrays.asList(values)));
            }
            return excludes;
         }
         catch (Exception e)
         {
            e.printStackTrace(); //NOSONAR
         }
         finally
         {
            try
            {
               is.close();
            }
            catch (IOException e)
            {
               // ignore me
            }
         }         
      }
      return null;
   }
}
