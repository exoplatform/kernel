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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;

/**
 * @author: Tuan Nguyen
 * @version: $Id: IOUtil.java,v 1.4 2004/09/14 02:41:19 tuan08 Exp $
 * @since: 0.0
 * @email: tuan08@yahoo.com
 */
public class IOUtil
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.commons.IOUtil");

   /** The buffer size for reading input streams. */
   private static final int DEFAULT_BUFFER_SIZE = 256;

   /**
    * Returns the content of the specified file as a string using the <code>UTF-8</code> charset.
    *
    * @param file the file
    * @return the content
    * @throws IOException any io exception
    * @throws IllegalArgumentException if any argument is null
    */
   static public String getFileContentAsString(File file) throws IOException, IllegalArgumentException
   {
      return getFileContentAsString(file, "UTF-8");
   }

   /**
    * Returns the content of the specified file as a string using the specified charset.
    *
    * @param file the file
    * @param charset the charset
    * @return the content
    * @throws IOException any io exception
    * @throws IllegalArgumentException if any argument is null
    */
   static public String getFileContentAsString(File file, String charset) throws IOException, IllegalArgumentException
   {
      if (file == null)
      {
         throw new IllegalArgumentException("No null file accepted");
      }
      FileInputStream is = PrivilegedFileHelper.fileInputStream(file);
      return new String(getStreamContentAsBytes(is), charset);
   }

   /**
    * Returns the content of the specified file as a string using the specified charset.
    *
    * @param fileName the file name
    * @param charset the charset
    * @return the content
    * @throws IOException any io exception
    * @throws IllegalArgumentException if any argument is null
    */
   static public String getFileContentAsString(String fileName, String charset) throws IOException,
      IllegalArgumentException
   {
      if (fileName == null)
      {
         throw new IllegalArgumentException("No null file name accepted");
      }
      return getFileContentAsString(new File(fileName), charset);
   }

   /**
    * Returns the content of the specified file as a string using the <code>UTF-8<code> charset.
    *
    * @param fileName the file name
    * @return the content
    * @throws IOException any io exception
    * @throws IllegalArgumentException if any argument is null
    */
   static public String getFileContentAsString(String fileName) throws IOException, IllegalArgumentException
   {
      return getFileContentAsString(fileName, "UTF-8");
   }

   /**
    * Returns the content of the specified stream as a string using the <code>UTF-8</code> charset.
    *
    * @param is the stream
    * @return the content
    * @throws IOException any io exception
    * @throws IllegalArgumentException if the specified stream is null
    */
   static public String getStreamContentAsString(InputStream is) throws IOException, IllegalArgumentException
   {
      byte buf[] = getStreamContentAsBytes(is);
      return new String(buf, "UTF-8");
   }

   /**
    * Returns the content of the file specified by its name as a byte array.
    *
    * @param fileName the file name
    * @return the content
    * @throws IOException any io exception
    * @throws IllegalArgumentException if the specified file name is null
    */
   static public byte[] getFileContentAsBytes(String fileName) throws IOException, IllegalArgumentException
   {
      if (fileName == null)
      {
         throw new IllegalArgumentException("No null file name accepted");
      }
      FileInputStream is = PrivilegedFileHelper.fileInputStream(fileName);
      return getStreamContentAsBytes(is);
   }

   /**
    * Reads a stream until its end and returns its content as a byte array. The provided
    * stream will be closed by this method. Any runtime exception thrown when the stream is closed will
    * be ignored and not rethrown. 
    *
    * @param is the input stream
    * @return the data read from the input stream its end
    * @throws IOException if any IOException occurs during a read
    * @throws IllegalArgumentException if the provided input stream is null
    */
   static public byte[] getStreamContentAsBytes(InputStream is) throws IOException, IllegalArgumentException
   {
      if (is == null)
      {
         throw new IllegalArgumentException("No null input stream accepted");
      }
      try
      {
         ByteArrayOutputStream output = new ByteArrayOutputStream();
         byte[] data = new byte[DEFAULT_BUFFER_SIZE];
         int available;
         while ((available = is.read(data)) > -1)
         {
            output.write(data, 0, available);
         }
         return output.toByteArray();
      }
      finally
      {
         if (is != null)
         {
            try
            {
               is.close();
            }
            catch (IOException ignore)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("An exception occurred: " + ignore.getMessage());
               }
            }
            catch (RuntimeException ignore)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("An exception occurred: " + ignore.getMessage());
               }
            }
         }
      }
   }

   /**
    * Get a resource from the thread context classloader and returns its content as a string. The resource
    * is obtained by calling the method {@link java.lang.ClassLoader#getResource(String)} on the context classloader
    * associated with the current thread of execution. The charset used for encoding the resource as a string is
    * <code>UTF-8</code>.
    *
    * @param resource the resource name
    * @return the resource content
    * @throws IllegalArgumentException if the specified argument is null or the loaded resource does not exist
    * @throws IOException thrown by accessing the resource
    */
   static public String getResourceAsString(String resource) throws IOException
   {
      byte[] bytes = getResourceAsBytes(resource);
      return new String(bytes, "UTF-8");
   }

   /**
    * Get a resource from the thread context classloader and returns its content as a byte array. The resource
    * is obtained by calling the method {@link java.lang.ClassLoader#getResource(String)} on the context classloader
    * associated with the current thread of execution.
    *
    * @param resource the resource name
    * @return the resource content
    * @throws IllegalArgumentException if the specified argument is null or the loaded resource does not exist
    * @throws IOException thrown by accessing the resource
    */
   static public byte[] getResourceAsBytes(String resource) throws IOException
   {
      if (resource == null)
      {
         throw new IllegalArgumentException("Cannot provide null resource values");
      }
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      URL url = cl.getResource(resource);
      if (url == null)
      {
         throw new IllegalArgumentException("The resource " + resource
            + " was not found in the thread context classloader");
      }
      InputStream is = url.openStream();
      return getStreamContentAsBytes(is);
   }

   // Deprecated stuf ***************************************************************************************************

   @Deprecated
   static public byte[] serialize(Object obj) throws Exception
   {
      // long start = System.currentTimeMillis() ;
      ByteArrayOutputStream bytes = new ByteArrayOutputStream();
      ObjectOutputStream out = new ObjectOutputStream(bytes);
      out.writeObject(obj);
      out.close();
      byte[] ret = bytes.toByteArray();
      // long end = System.currentTimeMillis() ;
      return ret;
   }

   @Deprecated
   static public Object deserialize(byte[] bytes) throws Exception
   {
      if (bytes == null)
         return null;
      // long start = System.currentTimeMillis() ;
      ByteArrayInputStream is = new ByteArrayInputStream(bytes);
      ObjectInputStream in = new ObjectInputStream(is);
      Object obj = in.readObject();
      in.close();
      return obj;
   }

   /**
    * Use {@link #getFileContentAsString(File)} instead.
    */
   @Deprecated
   static public String getFileContenntAsString(File file) throws Exception
   {
      return getFileContentAsString(file);
   }

   /**
    * Use {@link #getFileContentAsString(File,String)} instead.
    */
   @Deprecated
   static public String getFileContenntAsString(File file, String encoding) throws Exception
   {
      return getFileContentAsString(file, encoding);
   }

   /**
    * Use {@link #getFileContentAsString(String,String)} instead.
    */
   @Deprecated
   static public String getFileContenntAsString(String fileName, String encoding) throws Exception
   {
      return getFileContentAsString(fileName, encoding);
   }

   /**
    * Use {@link #getFileContentAsString(String)} instead.
    */
   @Deprecated
   static public String getFileContenntAsString(String fileName) throws Exception
   {
      return getFileContentAsString(fileName);
   }

}
