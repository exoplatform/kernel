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

import org.exoplatform.services.log.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
class LogOutputStream extends OutputStream
{
   private Log log;

   private ByteArrayOutputStream buf = new ByteArrayOutputStream();

   private boolean eol;

   LogOutputStream(Log log)
   {
      this.log = log;
   }

   @Override
   public void write(int b) throws IOException
   {
      if (b == '\n' || b == '\r')
      {
         eol = true;
         return;
      }
      flush();
      if (b != -1)
      {
         buf.write(b);
      }
   }

   public void flush()
   {
      if (eol)
      {
         eol = false;
         log.info(new String(buf.toByteArray()));
         buf.reset();
      }
   }

   public void close()
   {
      flush();
   }
}
