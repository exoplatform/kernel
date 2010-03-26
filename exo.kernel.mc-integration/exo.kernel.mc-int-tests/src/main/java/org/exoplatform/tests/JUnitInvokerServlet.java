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
package org.exoplatform.tests;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.junit.internal.JUnitSystem;
import org.junit.runner.JUnitCore;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class JUnitInvokerServlet extends HttpServlet
{
   private static final Log log = ExoLogger.getLogger("exo.kernel.mc-int-tests.JUnitInvokerServlet");

   protected void doGet(HttpServletRequest request, HttpServletResponse response)
         throws ServletException, IOException
   {
      String className = request.getParameter("class");
      response.setContentType("text/plain");
      OutputStream out = response.getOutputStream();
      final PrintStream fOut = new PrintStream(new TeeOutputStream(out, new LogOutputStream(log)));
      //final PrintStream fOut = new PrintStream(out);
      JUnitSystem sys = new JUnitSystem()
      {
         public PrintStream out()
         {
            return fOut;
         }

         public void exit(int arg)
         {
         }
      };

      new JUnitCore().runMain(sys, className);
      out.close();
   }
}