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
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
public class FailuresParser
{
   private static final String END_OF_REPORT = "FAILURES!!!";
   private Map<String, CopiedException> failures = new HashMap<String, CopiedException>();

   FailuresParser(LineReader lineReader)
   {
      try
      {
         String line = null;
         while ((line = lineReader.readLine()) != null)
         {
            String testMethodName = getNameIfStartOfFailure(line);

            if (testMethodName == END_OF_REPORT)
            {
               break;
            }

            if (testMethodName != null)
            {
               failures.put(testMethodName, new CopiedException(readStackTrace(lineReader)));
               lineReader.unreadLastLine();
            }
         }
      }
      catch (RuntimeException ex)
      {
         throw ex;
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Failed to read response from server: ", ex);
      }
   }

   public Map<String, CopiedException> getFailures()
   {
      return failures;
   }

   private String getNameIfStartOfFailure(String line)
   {
      // Number + ") " + methodName + "(" + className + ")"
      // Number + ") " + className
      if (END_OF_REPORT.equals(line))
      {
         return END_OF_REPORT;
      }

      int max = line.length() > 4 ? 4 : line.length();
      int pos = line.substring(0, max).indexOf(")");
      if (pos == -1)
      {
         return null;
      }

      try
      {
         Integer.valueOf(line.substring(0, pos));
      }
      catch (NumberFormatException ignored)
      {
         return null;
      }

      int endOfMethodPos = line.lastIndexOf("(", line.length() - 1);
      if (endOfMethodPos == -1)
      {
         endOfMethodPos = line.length();
      }

      String ret = line.substring(pos + 2, endOfMethodPos);
      // it should not contain any spaces, tabs, newlines, colons ...
      String[] split = ret.split("(\\s|:)");
      if (split.length > 1)
      {
         return null;
      }
      return ret;
   }

   private String readStackTrace(BufferedReader lineReader) throws IOException
   {
      final String EOL = System.getProperty("line.separator");
      StringBuilder sb = new StringBuilder();

      // Read for as long as lines don't look like beginning of another failure
      String line = null;
      while ((line = lineReader.readLine()) != null)
      {
         String testMethodName = getNameIfStartOfFailure(line);
         if (testMethodName != null)
         {
            break;
         }
         sb.append(line).append(EOL);
      }
      return sb.toString();
   }
}
