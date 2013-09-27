/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container;

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This enumeration defines all the possible mode supported by the kernel.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public enum Mode {

   /**
    * Use this mode when you want to delegate several threads to the kernel to create, initialize and start components.
    */
   MULTI_THREADED,

   /**
    * Use this mode when you want to see the kernel automatically fixes dependency issues such as unexpected call to
    * getComponentInstanceOfType()
    */
   AUTO_SOLVE_DEP_ISSUES;

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.mt.Mode");

   /**
    * The name of the system parameter to indicate that we want to enable the <i>multi-threaded</i> mode of the kernel
    */
   public static final String MULTI_THREADED_PARAM_NAME = "org.exoplatform.container.mt.enabled";

   /**
    * The name of the system parameter to indicate that we want to enable the <i>auto solve dependency issues</i> mode
    * of the kernel
    */
   public static final String AUTO_SOLVE_DEP_ISSUES_PARAM_NAME = "org.exoplatform.container.as.enabled";

   private static volatile Set<Mode> MODES;

   static void setModes(Mode... modes)
   {
      Set<Mode> sModes;
      if (modes == null || modes.length == 0)
      {
         sModes = Collections.emptySet();
      }
      else
      {
         sModes = new HashSet<Mode>(Arrays.asList(modes));
      }
      synchronized (Mode.class)
      {
         MODES = Collections.unmodifiableSet(sModes);
      }
   }

   static void clearModes()
   {
      // Clear to enforce reloading the default configuration
      synchronized (Mode.class)
      {
         MODES = null;
      }
   }

   /**
    * Indicates whether or not the given mode has been activated
    */
   public static boolean hasMode(Mode mode)
   {
      return getModes().contains(mode);
   }

   private static Set<Mode> getModes()
   {
      Set<Mode> modes = MODES;
      if (modes == null)
      {
         synchronized (Mode.class)
         {
            modes = MODES;
            if (modes == null)
            {
               Set<Mode> sModes = new HashSet<Mode>();
               String sValue = PropertyManager.getProperty(MULTI_THREADED_PARAM_NAME);
               if (sValue == null || Boolean.valueOf(sValue))
               {
                  sModes.add(MULTI_THREADED);
                  if (LOG.isDebugEnabled())
                  {
                     LOG.debug("The 'multi-threaded' mode of the kernel has been enabled");
                  }
               }
               else if (LOG.isDebugEnabled())
               {
                  LOG.debug("The 'multi-threaded' mode of the kernel is disabled");
               }
               sValue = PropertyManager.getProperty(AUTO_SOLVE_DEP_ISSUES_PARAM_NAME);
               if (sValue == null || Boolean.valueOf(sValue))
               {
                  sModes.add(AUTO_SOLVE_DEP_ISSUES);
                  if (LOG.isDebugEnabled())
                  {
                     LOG.debug("The 'auto solve dependency issues' mode of the kernel has been enabled");
                  }
               }
               else if (LOG.isDebugEnabled())
               {
                  LOG.debug("The 'auto solve dependency issues' mode of the kernel is disabled");
               }
               modes = Collections.unmodifiableSet(sModes);
               MODES = modes;
            }
         }
      }
      return modes;
   }
}
