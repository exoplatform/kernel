/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.exoplatform.kernel.it;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * This test needs to be executed in integration-test phase
 *
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class TestMCInjectionIntegration
{
   protected String testClass;

   protected Map<String, CopiedException> failures = new HashMap<String, CopiedException>();


   public TestMCInjectionIntegration()
   {
      testClass = MCInjectionTest.class.getName();
   }

   @Before
   public void init()
   {
      URL url = null;
      try
      {
         String host = System.getProperty("server.host");
         if (host == null)
         {
            host = "localhost";
         }
         String port = System.getProperty("server.port");
         if (port == null)
         {
            port = "8080";
         }
         // invoke servlet
         url = new URL("http://" + host + ":" + port + "/mc-int-tests/integration-tests?class=" + testClass);
         System.out.println("Executing remote tests: " + url);
         InputStream is = (InputStream) url.getContent();
         LineReader lineReader = new LineReader(new InputStreamReader(is));
         failures = new FailuresParser(lineReader).getFailures();
         checkFailed(testClass);
      }
      catch (MalformedURLException ex)
      {
         throw new RuntimeException("Buggy test case", ex);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to retrieve response for url: " + url, e);
      }
   }

   private void checkFailed(String method)
   {
      RuntimeException ex = failures.get(method);
      if (ex != null)
      {
         throw ex;
      }
   }

   @Test
   public void test()
   {
      checkFailed("test");
   }
}
