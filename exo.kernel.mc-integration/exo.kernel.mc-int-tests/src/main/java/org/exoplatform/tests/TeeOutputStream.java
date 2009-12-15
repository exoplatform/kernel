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

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:mstrukel@redhat.com">Marko Strukelj</a>
 */
class TeeOutputStream extends OutputStream
{
   private OutputStream out;
   private OutputStream out2;

   TeeOutputStream(OutputStream out, OutputStream out2)
   {
      this.out = out;
      this.out2 = out2;
   }

   @Override
   public void write(byte[] buf, int off, int len) throws IOException
   {
      out.write(buf, off, len);
      out2.write(buf, off, len);
   }

   @Override
   public void write(byte[] buf) throws IOException
   {
      write(buf, 0, buf.length);
   }

   @Override
   public void write(int b) throws IOException
   {
      out.write(b);
      out2.write(b);
   }

   @Override
   public void flush() throws IOException
   {
      out.flush();
      out2.flush();
   }

   @Override
   public void close() throws IOException
   {
      out.close();
      out2.close();
   }
}
