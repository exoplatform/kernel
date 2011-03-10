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

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.sql.SQLException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Helps running code in privileged 
 * 
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: SecurityHelper.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class SecurityHelper
{

   /**
    * Launches action in privileged mode. Can throw only IO exception.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedIOExceptionAction(PrivilegedExceptionAction<E> action) throws IOException
   {
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
    * Launches action in privileged mode. Can throw only NamingException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedNamingExceptionAction(PrivilegedExceptionAction<E> action) throws NamingException
   {
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof NamingException)
         {
            throw (NamingException)cause;
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
    * Launches action in privileged mode. Can throw only SQL exception.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedSQLExceptionAction(PrivilegedExceptionAction<E> action) throws SQLException
   {
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof SQLException)
         {
            throw (SQLException)cause;
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
    * Launches action in privileged mode. Can throw only ParserConfigurationException, SAXException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedParserConfigurationOrSAXExceptionAction(PrivilegedExceptionAction<E> action)
      throws ParserConfigurationException, SAXException
   {
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof ParserConfigurationException)
         {
            throw (ParserConfigurationException)cause;
         }
         else if (cause instanceof SAXException)
         {
            throw (SAXException)cause;
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
    * Launches action in privileged mode. Can throw only ParserConfigurationException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedParserConfigurationAction(PrivilegedExceptionAction<E> action)
      throws ParserConfigurationException
   {
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof ParserConfigurationException)
         {
            throw (ParserConfigurationException)cause;
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
    * Launches action in privileged mode. Can throw only SAXException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedSAXExceptionAction(PrivilegedExceptionAction<E> action) throws SAXException
   {
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof SAXException)
         {
            throw (SAXException)cause;
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
    * Launches action in privileged mode. Can throw only SAXException.
    * 
    * @param <E>
    * @param action
    * @return
    * @throws IOException
    */
   public static <E> E doPrivilegedMalformedURLExceptionAction(PrivilegedExceptionAction<E> action)
      throws MalformedURLException
   {
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof MalformedURLException)
         {
            throw (MalformedURLException)cause;
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
    * Launches action in privileged mode. Can throw only runtime exceptions.
    * 
    * @param <E>
    * @param action
    * @return
    */
   public static <E> E doPrivilegedAction(PrivilegedAction<E> action)
   {
      return AccessController.doPrivileged(action);
   }
   
   /**
    * Launches action in privileged mode. 
    * 
    * @param <E>
    * @param action
    * @return
    */
   public static <E> E doPrivilegedExceptionAction(PrivilegedExceptionAction<E> action)
      throws PrivilegedActionException
   {
      return AccessController.doPrivileged(action);
   }
   
}
