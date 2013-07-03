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
package org.exoplatform.container.web;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.RootContainer;

import java.security.PrivilegedAction;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * This class is used to create and initialize all the portal containers that have been
 * registered previously
 * 
 * Created by The eXo Platform SAS
 * Author : Nicolas Filotto
 *          nicolas.filotto@exoplatform.com
 * 9 sept. 2009  
 */
public class PortalContainerCreator implements ServletContextListener
{

   /**
    * {@inheritDoc}
    */
   public void contextDestroyed(ServletContextEvent event)
   {
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            // Ensure that the root container is stopped properly since the shutdown hook
            // doesn't work in some cases for example with tomcat when we call the stop command
            RootContainer.getInstance().stop();
            return null;
         }
      });
   }

   /**
    * Initializes and creates all the portal container that have been registered previously
    */
   public void contextInitialized(ServletContextEvent event)
   {
      final RootContainer rootContainer = RootContainer.getInstance();
      SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
      {
         public Void run()
         {
            rootContainer.createPortalContainers();
            return null;
         }
      });
   }
}
