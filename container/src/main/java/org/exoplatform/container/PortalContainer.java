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
package org.exoplatform.container;

import org.exoplatform.container.jmx.MX4JComponentAdapterFactory;
import org.exoplatform.container.xml.PortalContainerInfo;
import org.exoplatform.management.annotations.Managed;
import org.exoplatform.management.annotations.ManagedDescription;
import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.NamingContext;
import org.exoplatform.management.jmx.annotations.Property;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoException;
import org.picocontainer.defaults.DuplicateComponentKeyRegistrationException;

import java.util.List;

import javax.servlet.ServletContext;

/**
 * Created by The eXo Platform SAS Author : Mestrallet Benjamin
 * benjmestrallet@users.sourceforge.net Date: Jul 31, 2003 Time: 12:15:28 AM
 */
@Managed
@NamingContext(@Property(key = "portal", value = "{Name}"))
@NameTemplate(@Property(key = "container", value = "portal"))
public class PortalContainer extends ExoContainer implements SessionManagerContainer
{

   /**
    * The default name of the portal container
    */
   private static final String DEFAULT_PORTAL_CONTAINER_NAME = "portal";

   private static ThreadLocal currentContainer_ = new ThreadLocal();

   private boolean started_ = false;

   private PortalContainerInfo pinfo_;

   private SessionManager smanager_;

   private final String name;

   public PortalContainer(PicoContainer parent, ServletContext portalContext)
   {
      super(new MX4JComponentAdapterFactory(), parent);
      registerComponentInstance(ServletContext.class, portalContext);
      context.setName(portalContext.getServletContextName());
      pinfo_ = new PortalContainerInfo(portalContext);
      registerComponentInstance(PortalContainerInfo.class, pinfo_);
      this.name = portalContext.getServletContextName();
   }

   @Managed
   @ManagedDescription("The portal container name")
   public String getName()
   {
      return name;
   }

   public SessionContainer createSessionContainer(String id, String owner)
   {
      SessionContainer scontainer = getSessionManager().getSessionContainer(id);
      if (scontainer != null)
         getSessionManager().removeSessionContainer(id);
      scontainer = new SessionContainer(id, owner);
      scontainer.setPortalName(pinfo_.getContainerName());
      getSessionManager().addSessionContainer(scontainer);
      SessionContainer.setInstance(scontainer);
      return scontainer;
   }

   public void removeSessionContainer(String sessionID)
   {
      getSessionManager().removeSessionContainer(sessionID);
   }

   public List<SessionContainer> getLiveSessions()
   {
      return getSessionManager().getLiveSessions();
   }

   public SessionManager getSessionManager()
   {
      if (smanager_ == null)
         smanager_ = (SessionManager)this.getComponentInstanceOfType(SessionManager.class);
      return smanager_;
   }

   public PortalContainerInfo getPortalContainerInfo()
   {
      return pinfo_;
   }

   public static PortalContainer getInstance()
   {
      PortalContainer container = (PortalContainer)currentContainer_.get();
      if (container == null)
      {
         container = RootContainer.getInstance().getPortalContainer(DEFAULT_PORTAL_CONTAINER_NAME);
         currentContainer_.set(container);
      }
      return container;
   }

   @Managed
   public boolean isStarted()
   {
      return started_;
   }

   public void start()
   {
      super.start();
      started_ = true;
   }

   public void stop()
   {
      super.stop();
      started_ = false;
   }

   synchronized public ComponentAdapter getComponentAdapterOfType(Class componentType)
   {
      return super.getComponentAdapterOfType(componentType);
   }

   synchronized public List getComponentAdaptersOfType(Class componentType)
   {
      return super.getComponentAdaptersOfType(componentType);
   }

   synchronized public ComponentAdapter unregisterComponent(Object componentKey)
   {
      return super.unregisterComponent(componentKey);
   }

   synchronized public ComponentAdapter registerComponent(ComponentAdapter componentAdapter)
      throws DuplicateComponentKeyRegistrationException
   {
      return super.registerComponent(componentAdapter);
   }

   synchronized public List getComponentInstancesOfType(Class componentType) throws PicoException
   {
      return super.getComponentInstancesOfType(componentType);
   }

   public static void setInstance(PortalContainer instance)
   {
      currentContainer_.set(instance);
      ExoContainerContext.setCurrentContainer(instance);
   }

   public static Object getComponent(Class key)
   {
      PortalContainer pcontainer = (PortalContainer)currentContainer_.get();
      return pcontainer.getComponentInstanceOfType(key);
   }
}
