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

/**
 * Created by The eXo Platform SAS Author : Nhu Dinh Thuan
 * nhudinhthuan@exoplatform.com Apr 6, 2007
 */
public class Event<S, D>
{

   protected String eventName;

   protected S source;

   protected D data;

   /**
    * Construct an Event object that contain the event name , the object that
    * broadcast the event and the data object that the source object is working
    * on
    * 
    * @param name The name of the event
    * @param source The object on which the Event initially occurred.
    * @param data the object that the source object is working on
    */
   public Event(String name, S source, D data)
   {
      this.eventName = name;
      this.source = source;
      this.data = data;
   }

   /**
    * @return The name of the event. Any Listener want to be invoked on the event
    *         has to have the same name
    */
   public String getEventName()
   {
      return eventName;
   }

   /**
    * 
    */
   public S getSource()
   {
      return source;
   }

   /**
    * @return The data object that the source object is working on
    */
   public D getData()
   {
      return data;
   }
}
