/*
 * Copyright (C) 2012 eXo Platform SAS.
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

/**
 * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
 * @version $Id: ClassLoading.java 34360 2009-07-22 23:58:59Z tolusha $
 */
public class ClassLoading
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("org.exoplatform.commons.utils.ClassLoader");

   /**
    * Loads the class using the ClassLoader corresponding to the caller object first, 
    * if class not found we try with Thread's context ClassLoader (TCCL).
    * If the TCCL doesn't exist or the class still cannot be found, we use the 
    * System class loader.
    *
    * @param type FQN of class to load
    * @param callerObject the object from which we want to load the class
    * @return Loaded class
    * @throws ClassNotFoundException
    */
   public static Class<?> forName(String type, Object callerObject) throws ClassNotFoundException
   {
      return forName(type, callerObject.getClass());
   }

   /**
    * Loads the class using the ClassLoader corresponding to the caller class first, 
    * if class not found we try with Thread's context ClassLoader (TCCL).
    * If the TCCL doesn't exist or the class still cannot be found, we use the 
    * System class loader.
    *
    * @param type FQN of class to load
    * @param callerClass the class from which we want to load the class
    * @return Loaded class
    * @throws ClassNotFoundException
    */
   public static Class<?> forName(String type, Class<?> callerClass) throws ClassNotFoundException
   {
      try
      {
         // We first try with the local class loader
         return Class.forName(type, true, callerClass.getClassLoader());
      }
      catch (ClassNotFoundException e)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("The class " + type + " could not be found in the Class loader of " + callerClass);
         }
         // Then we try with the Thread Context Class loader
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         try
         {
            if (cl != null)
            {
               return Class.forName(type, true, cl);
            }
            else if (LOG.isTraceEnabled())
            {
               LOG.trace("No thread context Class loader could be found to load the class " + type);
            }
         }
         catch (ClassNotFoundException e1)
         {
            // ignore me
            if (LOG.isTraceEnabled())
            {
               LOG.trace("The class " + type + " could not be found in the thread context Class loader");
            }
            cl = null;
         }
         // Finally we test with the system class loader
         try
         {
            cl = ClassLoader.getSystemClassLoader();
         }
         catch (Exception e1)
         {
            // ignore me
            if (LOG.isTraceEnabled())
            {
               LOG.trace("The system Class loader could not be found to load the class " + type, e1);
            }
         }
         if (cl != null)
         {
            return Class.forName(type, true, cl);
         }
         else if (LOG.isTraceEnabled())
         {
            LOG.trace("The system Class loader could not be found to load the class " + type);
         }
         throw e;
      }
   }

   /**
    * Loads the class using the ClassLoader corresponding to the caller object first, 
    * if class not found we try with Thread's context ClassLoader (TCCL).
    * If the TCCL doesn't exist or the class still cannot be found, we use the 
    * System class loader.
    *
    * @param type FQN of class to load
    * @param callerObject the object from which we want to load the class
    * @return Loaded class
    * @throws ClassNotFoundException
    */
   public static Class<?> loadClass(String type, Object callerObject) throws ClassNotFoundException
   {
      return loadClass(type, callerObject.getClass());
   }

   /**
    * Loads the class using the ClassLoader corresponding to the caller class first, 
    * if class not found we try with Thread's context ClassLoader (TCCL).
    * If the TCCL doesn't exist or the class still cannot be found, we use the 
    * System class loader.
    *
    * @param type FQN of class to load
    * @param callerClass the class from which we want to load the class
    * @return Loaded class
    * @throws ClassNotFoundException
    */
   public static Class<?> loadClass(String type, Class<?> callerClass) throws ClassNotFoundException
   {
      ClassLoader localCl = callerClass.getClassLoader();
      try
      {
         return localCl.loadClass(type);
      }
      catch (ClassNotFoundException e)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("The class " + type + " could not be found in the Class loader of " + callerClass);
         }
         // Then we try with the Thread Context Class loader
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         try
         {
            if (cl != null)
            {
               return cl.loadClass(type);
            }
            else if (LOG.isTraceEnabled())
            {
               LOG.trace("No thread context Class loader could be found to load the class " + type);
            }
         }
         catch (ClassNotFoundException e1)
         {
            // ignore me
            if (LOG.isTraceEnabled())
            {
               LOG.trace("The class " + type + " could not be found in the thread context Class loader");
            }
            cl = null;
         }
         // Finally we test with the system class loader
         try
         {
            cl = ClassLoader.getSystemClassLoader();
         }
         catch (Exception e1)
         {
            // ignore me
            if (LOG.isTraceEnabled())
            {
               LOG.trace("The system Class loader could not be found to load the class " + type, e1);
            }
         }
         if (cl != null)
         {
            return cl.loadClass(type);
         }
         else if (LOG.isTraceEnabled())
         {
            LOG.trace("The system Class loader could not be found to load the class " + type);
         }
         throw e;
      }
   }

}
