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

import org.exoplatform.container.security.ContainerPermissions;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Jan 11, 2005
 * @version $Id: ExoContainerContext.java 6677 2006-07-03 10:49:59Z geaz $
 */
@SuppressWarnings("serial")
public final class ExoContainerContext implements java.io.Serializable
{

   private static ThreadLocal<WeakReference<ExoContainer>> currentContainer = new ThreadLocal<WeakReference<ExoContainer>>();

   private static volatile ExoContainer topContainer;

   private HashMap<String, Object> attributes = new HashMap<String, Object>();

   private final ExoContainer container;

   private String name;

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ExoContainerContext");

   public ExoContainerContext(ExoContainer container)
   {
      this.container = container;
   }

   public ExoContainerContext(ExoContainer container, String name)
   {
      this.container = container;
      this.name = name;
   }

   public ExoContainer getContainer()
   {
      return container;
   }

   /**
    * @return if the embedded container is a {@link PortalContainer}, it will return the name the
    * portal container otherwise it will return <code>null</code>
    */
   public String getPortalContainerName()
   {
      if (container instanceof PortalContainer)
      {
         return ((PortalContainer)container).getName();
      }
      return null;
   }

   /**
    * @return if the embedded container is a {@link PortalContainer}, it will return the name 
    * of the rest context related to the portal container otherwise it will return the default name
    */
   public String getRestContextName()
   {
      if (container instanceof PortalContainer)
      {
         return ((PortalContainer)container).getRestContextName();
      }
      return PortalContainer.DEFAULT_REST_CONTEXT_NAME;
   }

   /**
    * @return if the embedded container is a {@link PortalContainer}, it will return the name 
    * of the realm related to the portal container otherwise it will return the default name
    */
   public String getRealmName()
   {
      if (container instanceof PortalContainer)
      {
         return ((PortalContainer)container).getRealmName();
      }
      return PortalContainer.DEFAULT_REALM_NAME;
   }

   /**
    * @return if the embedded container is a {@link PortalContainer}, it will return the value 
    * of the setting related to the portal container otherwise it will return <code>null</code>
    */
   public Object getSetting(String settingName)
   {
      if (container instanceof PortalContainer)
      {
         return ((PortalContainer)container).getSetting(settingName);
      }
      return null;
   }
   
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);     
      this.name = name;
   }

   public static ExoContainer getTopContainer()
   {
      if (topContainer == null)
         topContainer = RootContainer.getInstance();
      return topContainer;
   }

   static void setTopContainer(ExoContainer cont)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);
      
      if (topContainer != null && cont != null && cont != topContainer)
      {
         throw new IllegalStateException("Two top level containers created, but must be only one.");
      }
      else if (cont == topContainer)
      {
         return;
      }
      LOG.info("Set the top container in its context");
      topContainer = cont;
   }

   public static ExoContainer getCurrentContainer()
   {
      WeakReference<ExoContainer> ref = currentContainer.get();
      ExoContainer container = ref == null ? null : ref.get();
      if (container == null)
         container = getTopContainer();
      return container;
   }

   public static ExoContainer getCurrentContainerIfPresent()
   {
      WeakReference<ExoContainer> ref = currentContainer.get();
      ExoContainer container = ref == null ? null : ref.get();
      if (container == null)
         return topContainer;
      return container;
   }

   public static void setCurrentContainer(ExoContainer instance)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);      
      currentContainer.set(instance == null ? null : new WeakReference<ExoContainer>(instance));
   }

   public static ExoContainer getContainerByName(String name)
   {
      ExoContainerContext containerContext = topContainer.getContext();
      String name1 = containerContext.getName();
      if (name1.equals(name))
         return topContainer;
      return (ExoContainer)topContainer.getComponentInstance(name);
   }

   public Set<String> getAttributeNames()
   {
      // Gives a safe copy
      return new HashSet<String>(attributes.keySet());
   }

   public Object getAttribute(String name)
   {
      return attributes.get(name);
   }

   public void setAttribute(String name, Object value)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
         security.checkPermission(ContainerPermissions.MANAGE_CONTAINER_PERMISSION);
      
      attributes.put(name, value);
   }
}
