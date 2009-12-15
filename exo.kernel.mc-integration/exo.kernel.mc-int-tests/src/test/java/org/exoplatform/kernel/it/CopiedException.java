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

import java.io.BufferedReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class CopiedException extends RuntimeException
{
   private String msg;
   private String trace;

   public CopiedException(String trace)
   {
      this.trace = trace;
      try
      {
         final String EOL = System.getProperty("line.separator");
         StringBuilder sb = new StringBuilder();
         BufferedReader lineReader = new BufferedReader(new StringReader(trace));
         String line = null;
         while ((line = lineReader.readLine()) != null)
         {
            if (line.trim().startsWith("at "))
            {
               break;
            }
            sb.append(line).append(EOL);
         }
         msg = sb.toString();
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Failed to initialize from stacktrace: ", ex);
      }
   }

   @Override
   public String getMessage()
   {
      return msg;
   }

   @Override
   public String toString()
   {
      return msg;
   }

   @Override
   public void printStackTrace()
   {
      System.out.println(trace);
   }

   @Override
   public void printStackTrace(PrintStream s)
   {
      s.println(trace);
   }

   @Override
   public void printStackTrace(PrintWriter s)
   {
      s.println(trace);
   }
}
