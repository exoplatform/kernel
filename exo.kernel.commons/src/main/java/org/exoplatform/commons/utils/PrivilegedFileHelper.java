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
package org.exoplatform.commons.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: SecurityFileHelper.java 111 2010-11-11 11:11:11Z tolusha $
 *
 * Class helper need for perform privileged file operations.
 */
public class PrivilegedFileHelper
{

   /**
    * Create FileOutputStream in privileged mode.
    * 
    * @param file
    * @return
    * @throws FileNotFoundException
    */
   public static FileOutputStream fileOutputStream(final File file) throws FileNotFoundException
   {
      PrivilegedExceptionAction<FileOutputStream> action = new PrivilegedExceptionAction<FileOutputStream>()
      {
         public FileOutputStream run() throws Exception
         {
            return new FileOutputStream(file);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof FileNotFoundException)
         {
            throw (FileNotFoundException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create FileOutputStream in privileged mode.
    * 
    * @param name
    * @return
    * @throws FileNotFoundException
    */
   public static FileOutputStream fileOutputStream(final String name) throws FileNotFoundException
   {
      PrivilegedExceptionAction<FileOutputStream> action = new PrivilegedExceptionAction<FileOutputStream>()
      {
         public FileOutputStream run() throws Exception
         {
            return new FileOutputStream(name);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof FileNotFoundException)
         {
            throw (FileNotFoundException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create FileOutputStream in privileged mode.
    * 
    * @param file
    * @param append
    * @return
    * @throws FileNotFoundException
    */
   public static FileOutputStream fileOutputStream(final File file, final boolean append) throws FileNotFoundException
   {
      PrivilegedExceptionAction<FileOutputStream> action = new PrivilegedExceptionAction<FileOutputStream>()
      {
         public FileOutputStream run() throws Exception
         {
            return new FileOutputStream(file, append);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof FileNotFoundException)
         {
            throw (FileNotFoundException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create FileInputStream in privileged mode.
    * 
    * @param file
    * @return
    * @throws FileNotFoundException
    */
   public static FileInputStream fileInputStream(final File file) throws FileNotFoundException
   {
      PrivilegedExceptionAction<FileInputStream> action = new PrivilegedExceptionAction<FileInputStream>()
      {
         public FileInputStream run() throws Exception
         {
            return new FileInputStream(file);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof FileNotFoundException)
         {
            throw (FileNotFoundException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create FileInputStream in privileged mode.
    * 
    * @param name
    * @return
    * @throws FileNotFoundException
    */
   public static FileInputStream fileInputStream(final String name) throws FileNotFoundException
   {
      PrivilegedExceptionAction<FileInputStream> action = new PrivilegedExceptionAction<FileInputStream>()
      {
         public FileInputStream run() throws Exception
         {
            return new FileInputStream(name);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof FileNotFoundException)
         {
            throw (FileNotFoundException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create new file.
    * 
    * @param file
    * @return
    * @throws IOException
    */
   public static boolean createNewFile(final File file) throws IOException
   {
      PrivilegedExceptionAction<Boolean> action = new PrivilegedExceptionAction<Boolean>()
      {
         public Boolean run() throws Exception
         {
            return file.createNewFile();
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();

         if (cause instanceof IOException)
         {
            throw (IOException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create temporary file in privileged mode.
    * 
    * @param prefix
    * @param suffix
    * @param directory
    * @return
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public static File createTempFile(final String prefix, final String suffix, final File directory)
      throws IllegalArgumentException, IOException
   {
      PrivilegedExceptionAction<File> action = new PrivilegedExceptionAction<File>()
      {
         public File run() throws Exception
         {
            return File.createTempFile(prefix, suffix, directory);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IllegalArgumentException)
         {
            throw (IllegalArgumentException)cause;
         }
         else if (cause instanceof IOException)
         {
            throw (IOException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create teamporary file in privileged mode.
    * 
    * 
    * @param prefix
    * @param suffix
    * @return
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public static File createTempFile(final String prefix, final String suffix) throws IllegalArgumentException,
      IOException
   {
      PrivilegedExceptionAction<File> action = new PrivilegedExceptionAction<File>()
      {
         public File run() throws Exception
         {
            return File.createTempFile(prefix, suffix);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IllegalArgumentException)
         {
            throw (IllegalArgumentException)cause;
         }
         else if (cause instanceof IOException)
         {
            throw (IOException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Create RandomAccessFile in privileged mode.
    * 
    * @param file
    * @param mode
    * @return
    * @throws IllegalArgumentException
    * @throws IOException
    */
   public static RandomAccessFile randomAccessFile(final File file, final String mode) throws IllegalArgumentException,
      IOException
   {
      PrivilegedExceptionAction<RandomAccessFile> action = new PrivilegedExceptionAction<RandomAccessFile>()
      {
         public RandomAccessFile run() throws Exception
         {
            return new RandomAccessFile(file, mode);
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IllegalArgumentException)
         {
            throw (IllegalArgumentException)cause;
         }
         else if (cause instanceof FileNotFoundException)
         {
            throw (FileNotFoundException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Get file length in privileged mode.
    * 
    * @param file
    * @return
    */
   public static long length(final File file)
   {
      PrivilegedAction<Long> action = new PrivilegedAction<Long>()
      {
         public Long run()
         {
            return new Long(file.length());
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Requests in privileged mode that the file or directory denoted by this abstract 
    * pathname be deleted when the virtual machine terminates.
    * 
    * @param file
    */
   public static void deleteOnExit(final File file)
   {
      PrivilegedAction<Void> action = new PrivilegedAction<Void>()
      {
         public Void run()
         {
            file.deleteOnExit();
            return null;
         }
      };
      AccessController.doPrivileged(action);
   }

   /**
    * Get file absolute path in privileged mode.
    * 
    * @param file
    * @return
    */
   public static String getAbsolutePath(final File file)
   {
      PrivilegedAction<String> action = new PrivilegedAction<String>()
      {
         public String run()
         {
            return file.getAbsolutePath();
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Get file canonical path in privileged mode.
    * 
    * @param file
    * @return
    * @throws IOException
    */
   public static String getCanonicalPath(final File file) throws IOException
   {
      PrivilegedExceptionAction<String> action = new PrivilegedExceptionAction<String>()
      {
         public String run() throws Exception
         {
            return file.getCanonicalPath();
         }
      };
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IOException)
         {
            throw (IOException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
   }

   /**
    * Delete file in privileged mode.
    * 
    * @param file
    * @return
    */
   public static boolean delete(final File file)
   {
      PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return file.delete();
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Tests in privileged mode whether the file denoted by this abstract pathname is a
    * directory.
    * 
    * @param file
    * @return
    */
   public static boolean isDirectory(final File file)
   {
      PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return file.isDirectory();
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Tests in privileged mode whether the file or directory denoted by this abstract pathname
    * exists.
    *  
    * @param file
    * @return
    */
   public static boolean exists(final File file)
   {
      PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return file.exists();
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Creates the directory in privileged mode.
    * 
    * @param file
    * @return
    */
   public static boolean mkdirs(final File file)
   {
      PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return file.mkdirs();
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Rename File in privileged mode.
    * 
    * @param srcFile
    * @param dstfile
    * @return
    */
   public static boolean renameTo(final File srcFile, final File dstfile)
   {
      PrivilegedAction<Boolean> action = new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return new Boolean(srcFile.renameTo(dstfile));
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Get file's list in privileged mode.
    * 
    * @param file
    * @return
    */
   public static String[] list(final File file)
   {
      PrivilegedAction<String[]> action = new PrivilegedAction<String[]>()
      {
         public String[] run()
         {
            return file.list();
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Get file's list in privileged mode.
    * 
    * @param file
    * @return
    */
   public static String[] list(final File file, final FilenameFilter filter)
   {
      PrivilegedAction<String[]> action = new PrivilegedAction<String[]>()
      {
         public String[] run()
         {
            return file.list(filter);
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Get file's list in privileged mode.
    * 
    * @param file
    * @return
    */
   public static File[] listFiles(final File file)
   {
      PrivilegedAction<File[]> action = new PrivilegedAction<File[]>()
      {
         public File[] run()
         {
            return file.listFiles();
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Get file's list in privileged mode.
    * 
    * @param file
    * @return
    */
   public static File[] listFiles(final File file, final FilenameFilter filter)
   {
      PrivilegedAction<File[]> action = new PrivilegedAction<File[]>()
      {
         public File[] run()
         {
            return file.listFiles(filter);
         }
      };
      return AccessController.doPrivileged(action);
   }

   /**
    * Get file's list in privileged mode.
    * 
    * @param file
    * @return
    */
   public static File[] listFiles(final File file, final FileFilter filter)
   {
      PrivilegedAction<File[]> action = new PrivilegedAction<File[]>()
      {
         public File[] run()
         {
            return file.listFiles(filter);
         }
      };
      return AccessController.doPrivileged(action);
   }
}
