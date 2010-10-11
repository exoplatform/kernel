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
package org.exoplatform.services.rpc;

/**
 * The event triggered anytime the cluster has changed of topology
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TopologyChangeEvent
{

   /**
    * Indicates whether the current node is the coordinator after this topology change
    */
   private final boolean coordinator;
   
   /**
    * Indicates whether the coordinator has changed
    */
   private final boolean coordinatorHasChanged;

   /**
    * Default constructor
    * @param coordinatorHasChanged this parameter is set to <code>true</code> if the 
    * coordinator has changed, <code>false</code> otherwise
    * @param coordinator this parameter is set to <code>true</code> if the current node
    * is the coordinator, <code>false</code> otherwise
    */
   public TopologyChangeEvent(boolean coordinatorHasChanged, boolean coordinator)
   {
      this.coordinator = coordinator;
      this.coordinatorHasChanged = coordinatorHasChanged;
   }

   /**
    * Indicates whether the current node is the coordinator or not after the topology change
    * @return <code>true</code> if the current node is the coordinator, <code>false</code> otherwise.
    */
   public boolean isCoordinator()
   {
      return coordinator;
   }

   /**
    * Indicates whether the coordinator has changed after the topology change
    * @return <code>true</code> if the coordinator has changed, <code>false</code> otherwise.
    */
   public boolean isCoordinatorHasChanged()
   {
      return coordinatorHasChanged;
   }
}
