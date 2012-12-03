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
package org.exoplatform.commons;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;

public class Environment
{

   public static final int UNKNOWN = 0;

   public static final int STAND_ALONE = 1;

   public static final int TOMCAT_PLATFORM = 2;

   public static final int JBOSS_PLATFORM = 3;

   public static final int JETTY_PLATFORM = 4;

   public static final int WEBSHPERE_PLATFORM = 5;

   public static final int WEBLOGIC_PLATFORM = 6;

   private static volatile Environment singleton_;

   private int platform_;

   private Environment()
   {
      String catalinaHome = PrivilegedSystemHelper.getProperty("catalina.home");
      String jbossHome = PrivilegedSystemHelper.getProperty("jboss.home.dir");
      String jettyHome = PrivilegedSystemHelper.getProperty("jetty.home");
      String websphereHome = PrivilegedSystemHelper.getProperty("was.install.root");
      String weblogicHome = PrivilegedSystemHelper.getProperty("weblogic.Name");
      String standAlone = PrivilegedSystemHelper.getProperty("maven.exoplatform.dir");
      if (jbossHome != null)
      {
         platform_ = JBOSS_PLATFORM;
      }
      else if (catalinaHome != null)
      {
         platform_ = TOMCAT_PLATFORM;
      }
      else if (jettyHome != null)
      {
         platform_ = JETTY_PLATFORM;
      }
      else if (websphereHome != null)
      {
         platform_ = WEBSHPERE_PLATFORM;
      }
      else if (weblogicHome != null)
      {
         platform_ = WEBLOGIC_PLATFORM;
      }
      else if (standAlone != null)
      {
         platform_ = STAND_ALONE;
      }
      else
      {
         platform_ = UNKNOWN;
      }
   }

   public int getPlatform()
   {
      return platform_;
   }

   static public Environment getInstance()
   {
      if (singleton_ == null)
      {
         synchronized (Environment.class)
         {
            singleton_ = new Environment();
         }
      }
      return singleton_;
   }
}