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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by The eXo Platform SAS Author : Nhu Dinh Thuan
 * nhudinhthuan@exoplatform.com Apr 6, 2007
 */
public class ListenerService
{

   private Map<String, List<Listener>> listeners_;

   private static Log log = ExoLogger.getLogger("kernel.ListenerService");

   /**
    * Construct a listener service.
    */
   public ListenerService()
   {
      listeners_ = new HashMap<String, List<Listener>>();
   }

   /**
    * This method is used to register a listener with the service. The method
    * should: 1. Check to see if there is a list of listener with the listener
    * name, create one if the listener list doesn't exit 2. Add the new listener
    * to the listener list
    * 
    * @param listener
    */
   public void addListener(Listener listener)
   {
      String name = listener.getName();
      List<Listener> list = listeners_.get(name);
      if (list == null)
      {
         list = new ArrayList<Listener>();
         listeners_.put(name, list);
      }
      list.add(listener);
   }

   /**
    * @deprecated use the Listener name as the event name
    * @param eventName
    * @param listener
    */
   public void addListener(String eventName, Listener listener)
   {
      listener.setName(eventName);
      addListener(listener);
   }

   /**
    * This method is used to broadcast an event. This method should: 1. Check if
    * there is a list of listener that listen to the event name. 2. If there is a
    * list of listener, create the event object with the given name , source and
    * data 3. For each listener in the listener list, invoke the method
    * onEvent(Event)
    * 
    * @param <S> The type of the source that broacast the event
    * @param <D> The type of the data that the source object is working on
    * @param name The name of the event
    * @param source The source object instance
    * @param data The data object instance
    * @throws Exception TODO: Should not delegate to the method broadcast(Event)
    */
   final public <S, D> void broadcast(String name, S source, D data) throws Exception
   {
      List<Listener> list = listeners_.get(name);
      if (list == null)
         return;
      for (Listener<S, D> listener : list)
      {
         if (log.isDebugEnabled())
         {
            log.debug("broadcasting event " + name + " on " + listener.getName());
         }
         listener.onEvent(new Event<S, D>(name, source, data));
      }
   }

   /**
    * This method is used when a developer want to implement his own event object
    * and broadcast the event. The method should: 1. Check if there is a list of
    * listener that listen to the event name. 2. If there is a list of the
    * listener, ror each listener in the listener list, invoke the method
    * onEvent(Event)
    * 
    * @param <T> The type of the event object, the type of the event object has
    *          to be extended from the Event type
    * @param event The event instance
    * @throws Exception
    */
   final public <T extends Event> void broadcast(T event) throws Exception
   {
      List<Listener> list = listeners_.get(event.getEventName());
      if (list == null)
         return;
      for (Listener listener : list)
         listener.onEvent(event);
   }
}
