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
package org.exoplatform.services.log;

import org.exoplatform.services.log.impl.SLF4JExoLogFactory;
import org.exoplatform.services.log.impl.SimpleExoLogFactory;

/**
 * 
 * The logger definition for exo platform.
 * <p>
 *
 * ExoLogger it's wrapping factory for different Log factories existing in runtime for eXo.
 * <br/>
 *  
 * The logger implements the {@link org.exoplatform.services.log.Log} interface
 * for backward compatibility purpose.
 * </p>
 * <p>
 * This class is also the way to obtain a reference to a logger through the
 * static methods {@link #getExoLogger(String)} and {@link #getExoLogger(Class)}
 * .
 * </p>
 * <p>
 * The factory methods delegates to an instance of
 * {@link org.exoplatform.services.log.ExoLogFactory} that is determined by
 * the following rules
 * <ul>
 * <li>A static instance is used and by default the static instance is assigned
 * with an instance of the class
 * {@link org.exoplatform.services.log.impl.SLF4JExoLogFactory}. It is
 * possible to change the instance at runtime by calling the static method
 * {@link #setFactory(ExoLogFactory)}.</li>
 * <li>If the static instance fails to deliver a logger at runtime due to a
 * {@link NoClassDefFoundError} then a factory instance of class
 * {@link org.exoplatform.services.log.impl.SimpleExoLogFactory} is used for
 * fail over.</li>
 * </ul>
 * </p>
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: ExoLogger.java 34394 2009-07-23 09:23:31Z dkatayev $
 */
public abstract class ExoLogger
{

   /**
    * The factory we use when we cannot load SLF4J (for instance when jibx maven
    * plugin is executed).
    */
   private static SimpleExoLogFactory failOverFactory = new SimpleExoLogFactory();

   /** SLF4J logger factory. */
   private static ExoLogFactory loggerFactory = new SLF4JExoLogFactory();

   /**
    * Configures the exo logger factory. This method can be called multiple times
    * to replace the current static instance.
    * 
    * @param factory ExoLogFactory, the new factory
    * @throws NullPointerException when the factory is null
    */
   public static void setFactory(ExoLogFactory factory) throws NullPointerException
   {
      if (factory == null)
      {
         throw new NullPointerException("Cannot set a null logger factory");
      }
      loggerFactory = factory;
   }

   /**
    * Use instead {@link #getExoLogger(String)}.
    * 
    * @param name String, the logger name
    * @return the logger
    */
   public static Log getLogger(String name)
   {
      return getExoLogger(name);
   }

   /**
    * Use instead {@link #getExoLogger(Class)}.
    * 
    * @param name Class, the logger name
    * @return the logger
    */
   public static Log getLogger(Class name)
   {
      return getExoLogger(name);
   }

   /**
    * Returns a specified logger.
    * 
    * @param name the logger name
    * @return the logger
    * @throws NullPointerException if the name is null
    */
   public static Log getExoLogger(String name) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      try
      {
         return loggerFactory.getExoLogger(name);
      }
      catch (NoClassDefFoundError e)
      {
         // We need to use the standard out print since we are actually 
         // configuring the logger
         System.err.println("Could not load logger class factory " + e.getMessage()
            + " will use fail over logger instead"); //NOSONAR
         return failOverFactory.getExoLogger(name);
      }
   }

   /**
    * Returns a specified logger.
    * 
    * @param name the logger name
    * @return the logger
    * @throws NullPointerException if the name is null
    */
   public static Log getExoLogger(Class name) throws NullPointerException
   {
      if (name == null)
      {
         throw new NullPointerException("No null name accepted");
      }
      try
      {
         return loggerFactory.getExoLogger(name);
      }
      catch (NoClassDefFoundError e)
      {
         // We need to use the standard out print since we are actually 
         // configuring the logger
         System.err.println("Could not load logger class factory " + e.getMessage()
            + " will use fail over logger instead"); //NOSONAR
         return failOverFactory.getExoLogger(name);
      }
   }
}
