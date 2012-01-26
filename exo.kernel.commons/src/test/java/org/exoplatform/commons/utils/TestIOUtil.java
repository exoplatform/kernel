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
package org.exoplatform.commons.utils;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestIOUtil extends TestCase
{

   public void testGetFileContentAsStringThrowsIllegalArgumentException() throws IOException
   {
      try
      {
         IOUtil.getFileContentAsString((File)null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         IOUtil.getFileContentAsString((String)null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         IOUtil.getFileContentAsString((File)null, "UTF-8");
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
      try
      {
         IOUtil.getFileContentAsString((String)null, "UTF-8");
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testStreamContentAsStringThrowsIllegalArgumentException() throws IOException
   {
      try
      {
         IOUtil.getStreamContentAsString(null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testStreamContentAsString() throws IOException
   {
      String s = IOUtil.getStreamContentAsString(new ByteArrayInputStream("a\u1000".getBytes("UTF-8")));
      assertEquals("a\u1000", s);
   }

   public void testStreamContentAsBytesThrowsIllegalArgumentException() throws IOException
   {
      try
      {
         IOUtil.getStreamContentAsBytes(null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
      }
   }

   public void testStreamContentAsBytes() throws IOException
   {
      byte[] bytes = IOUtil.getStreamContentAsBytes(new ByteArrayInputStream("a\u1000".getBytes("UTF-8")));
      assertEquals("a\u1000", new String(bytes, "UTF-8"));
   }

   public void testGetResourceAsString() throws IOException
   {
      ClassLoader newCL = TestIOUtil.class.getClassLoader();
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(newCL);
         String content = IOUtil.getResourceAsString("Simple.properties");
         assertEquals("resource_content", content);
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

   public void testGetResourceAsStringThrowsIllegalArgumentException() throws IOException
   {
      try
      {
         IOUtil.getResourceAsString(null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
         //
      }
   }

   public void testGetResourceAsStringNotFound() throws IOException
   {
      ClassLoader newCL = new ClassLoader()
      {
         @Override
         public URL getResource(String name)
         {
            return null;
         }
      };
      ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(newCL);
         IOUtil.getResourceAsString("whatever");
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
         //
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCL);
      }
   }

   public void testGetStreamContentAsBytesWithErrorDuringClose() throws IOException
   {
      final Error er = new Error();
      ClosableInputStream in = new ByteValueInputStream((byte)3)
      {
         @Override
         public void close() throws IOException
         {
            super.close();
            throw er;
         }
      };
      try
      {
         IOUtil.getStreamContentAsBytes(in);
         fail();
      }
      catch (Error e)
      {
         assertSame(e, er);
      }
      assertTrue(in.closed);
   }

   public void testGetStreamContentAsBytesWithIOExceptionDuringClose() throws IOException
   {
      ClosableInputStream in = new ByteValueInputStream((byte)3)
      {
         @Override
         public void close() throws IOException
         {
            super.close();
            throw new RuntimeException();
         }
      };
      byte[] bytes = IOUtil.getStreamContentAsBytes(in);
      assertNotNull(bytes);
      assertEquals(1, bytes.length);
      assertEquals(3, bytes[0]);
      assertTrue(in.closed);
   }

   public void testGetStreamContentAsBytesWithIOExceptionDuringRead() throws IOException
   {
      final IOException ex = new IOException();
      ClosableInputStream in = new ClosableInputStream()
      {
         public int read() throws IOException
         {
            throw ex;
         }
      };
      try
      {
         IOUtil.getStreamContentAsBytes(in);
      }
      catch (IOException e)
      {
         assertSame(ex, e);
      }
      assertTrue(in.closed);
   }

   public void testGetStreamContentAsBytesWithRuntimeExceptionDuringRead() throws IOException
   {
      final RuntimeException ex = new RuntimeException();
      ClosableInputStream in = new ClosableInputStream()
      {
         public int read() throws IOException
         {
            throw ex;
         }
      };
      try
      {
         IOUtil.getStreamContentAsBytes(in);
      }
      catch (RuntimeException e)
      {
         assertSame(ex, e);
      }
      assertTrue(in.closed);
   }

   public void testGetStreamContentAsBytesWithRuntimeExceptionDuringClose() throws IOException
   {
      final RuntimeException ex = new RuntimeException();
      ClosableInputStream in = new ClosableInputStream()
      {
         public int read() throws IOException
         {
            throw ex;
         }
      };
      try
      {
         IOUtil.getStreamContentAsBytes(in);
      }
      catch (RuntimeException e)
      {
         assertSame(ex, e);
      }
      assertTrue(in.closed);
   }

   public void testGetStreamContentAsBytesWithErrorDuringRead() throws IOException
   {
      final Error er = new Error();
      ClosableInputStream in = new ClosableInputStream()
      {
         public int read() throws IOException
         {
            throw er;
         }
      };
      try
      {
         IOUtil.getStreamContentAsBytes(in);
      }
      catch (Error e)
      {
         assertSame(er, e);
      }
      assertTrue(in.closed);
   }

   public void testGetStreamContentAsBytesCloseTheStreamAfterRead() throws IOException
   {
      ByteValueInputStream in = new ByteValueInputStream((byte)0);
      IOUtil.getStreamContentAsBytes(in);
      assertTrue(in.closed);
   }

   public void testGetStreamContentAsBytesWithEmptyStream()
   {
      try
      {
         byte[] bytes = IOUtil.getStreamContentAsBytes(new ByteArrayInputStream(new byte[0]));
         assertNotNull(bytes);
         assertEquals(0, bytes.length);
      }
      catch (IOException e)
      {
         fail();
      }
   }

   public void testGetStreamContentAsBytesThrowsIllegalArgumentException() throws IOException
   {
      try
      {
         IOUtil.getStreamContentAsBytes(null);
         fail("Was expecting an IllegalArgumentException");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   public void testGetStreamContentAsBytes() throws IOException
   {
      InputStream in = new ByteArrayInputStream("foo".getBytes("UTF8"));
      byte[] bytes = IOUtil.getStreamContentAsBytes(in);
      assertEquals("foo", new String(bytes, "UTF8"));
   }

   /**
    * This tests an rare case where the input stream provided is bugged. It may sound
    * stupid but my personal experience confirms it happens.
    */
   public void testGetStreamContentAsBytesWithCorruptedStream()
   {
      InputStream corruptedInputStream = new InputStream()
      {
         int state = 0;

         @Override
         public int read(byte[] b, int off, int len) throws IOException
         {
            if (len < 0)
            {
               throw new AssertionError("We assume that does not happen");
            }
            switch (state++)
            {
               case 0 :
                  b[0] = 4;
                  return 1;
               case 1 :
                  return 0;
               case 2 :
                  b[0] = 1;
                  return 1;
               case 3 :
                  return -1;
               default :
                  throw new AssertionError("This should not happen");
            }
         }

         @Override
         public int read() throws IOException
         {
            // Just because we know it will not be called by the method
            throw new UnsupportedOperationException();
         }
      };

      try
      {
         byte[] bytes = IOUtil.getStreamContentAsBytes(corruptedInputStream);
         assertNotNull(bytes);
         assertEquals(2, bytes.length);
         assertEquals(4, bytes[0]);
         assertEquals(1, bytes[1]);
      }
      catch (IOException e)
      {
         fail();
      }
   }

   private abstract static class ClosableInputStream extends InputStream
   {

      /** . */
      protected boolean closed = false;

      @Override
      public void close() throws IOException
      {
         super.close();
         closed = true;
      }
   }

   private static class ByteValueInputStream extends ClosableInputStream
   {

      /** . */
      private Byte value;

      private ByteValueInputStream(byte value)
      {
         this.value = value;
      }

      public int read() throws IOException
      {
         if (value != null)
         {
            byte tmp = value;
            value = null;
            return tmp;
         }
         else
         {
            return -1;
         }
      }
   }
}
