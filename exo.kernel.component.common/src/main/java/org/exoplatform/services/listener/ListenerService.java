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

   private final Map<String, List<Listener>> listeners_;

   private static final Log log = ExoLogger.getLogger("exo.kernel.component.common.ListenerService");
   
   /**
    * Construct a listener service.
    */
   public ListenerService()
   {
      listeners_ = new HashMap<String, List<Listener>>();
   }

   /**
    * This method is used to register a {@link Listener} to the events of the same
    * name. It is similar to addListener(listener.getName(), listener)
    * 
    * @param listener the listener to notify any time an even of the same name is
    * triggered
    */
   public void addListener(Listener listener)
   {
      addListener(listener.getName(), listener);
   }

   /**
    * This method is used to register a new {@link Listener}. Any time an
    * event of the given event name has been triggered, the {@link Listener} will be
    * notified.
    * This method will:
    * <ol>
    * <li>Check if it exists a list of listeners that have been registered for the
    * given event name, create a new list if no list exists</li>
    * <li>Add the listener to the list</li>
    * </ol>
    * @param eventName The name of the event to listen to
    * @param listener The Listener to notify any time the event with the given
    * name is triggered
    */
   public void addListener(String eventName, Listener listener)
   {
      List<Listener> list = listeners_.get(eventName);
      if (list == null)
      {
         list = new ArrayList<Listener>();
         listeners_.put(eventName, list);
      }
      list.add(listener);
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
