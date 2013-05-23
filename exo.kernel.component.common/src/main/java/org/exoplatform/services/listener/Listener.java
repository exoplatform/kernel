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
package org.exoplatform.services.listener;

import org.exoplatform.container.component.BaseComponentPlugin;

/**
 * Created by The eXo Platform SAS</br>
 * This class is registered with the Listener service
 * and is invoked when an event with the same name is broadcasted.
 * You can have many listeners with the same name to listen to an
 * event.
 * @author <a href="mailto:nhudinhthuan@exoplatform.com">Nhu Dinh Thuan</a>
 * @LevelAPI Platform
 */
public abstract class Listener<S, D> extends BaseComponentPlugin
{

   /**
    * This method should be invoked when an event with the same name is
    * broadcasted
    * @param event the event instance
    */
   public abstract void onEvent(Event<S, D> event) throws Exception;

}
