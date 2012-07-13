/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.listener;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.ThreadContext;
import org.exoplatform.container.component.ThreadContextHolder;

/**
 * Created by The eXo Platform SAS.
 * 
 * <br/>Date: 
 *
 * @author <a href="karpenko.sergiy@gmail.com">Karpenko Sergiy</a> 
 * @version $Id: TestAsynchronousListener.java 111 2008-11-11 11:11:11Z serg $
 */
public class TestAsynchronousListener extends TestCase
{
   private ListenerService service_;

   public void setUp() throws Exception
   {
      PortalContainer manager = PortalContainer.getInstance();
      service_ = (ListenerService)manager.getComponentInstanceOfType(ListenerService.class);
      TestHolder.tl.set("-suffix");
   }
   
   protected void tearDown() throws Exception
   {
      TestHolder.tl.remove();
   }
   
   public void testParentAsynchListener() throws Exception
   {
      final String listenerName = "test_parent_asynch";
      final String baseString = "Value not changed";
      final String resultString = "Value become changed";

      assertNotNull(service_);
      Listener<Object, StrValue> listener = new ExtendedAsynchListener();
      listener.setName(listenerName);
      listener.setDescription("Asynchronous listener");

      service_.addListener(listener);

      StrValue testValue = new StrValue(baseString);

      synchronized (testValue)
      {
         service_.broadcast(listenerName, new Object(), testValue);
      
         // if asynch enabled value must be changed later so it's same exact after listener 
         // broadcasting
         assertEquals(baseString, testValue.getValue());
         testValue.wait();
         assertEquals(resultString + "-suffix", testValue.getValue());
      }
      assertEquals("-suffix", TestHolder.tl.get());
   }
   
   public void testAsynchronousListener() throws Exception
   {
      final String listenerName = "test_asynch";
      final String baseString = "Value not changed";
      final String resultString = "Value become changed";

      assertNotNull(service_);
      Listener<Object, StrValue> listener = new AsynchListener();
      listener.setName(listenerName);
      listener.setDescription("Asynchronous listener");

      service_.addListener(listener);

      StrValue testValue = new StrValue(baseString);
      TestHolder.tl.set("-suffix");
      synchronized (testValue)
      {
         service_.broadcast(listenerName, new Object(), testValue);

         // if asynch enabled value must be changed later so it's same exact after listener 
         // broadcasting
         assertEquals(baseString, testValue.getValue());
         testValue.wait();
         assertEquals(resultString + "-suffix", testValue.getValue());
      }
      assertEquals("-suffix", TestHolder.tl.get());
   }

   public void testSynchronousListener() throws Exception
   {
      final String listenerName = "test_synch";
      final String baseString = "Value not changed";
      final String resultString = "Value become changed";

      assertNotNull(service_);
      Listener<Object, StrValue> listener = new SynchListener();
      listener.setName(listenerName);
      listener.setDescription("Synchronous listener");

      service_.addListener(listener);

      StrValue testValue = new StrValue(baseString);

      TestHolder.tl.set("-suffix");
      service_.broadcast(listenerName, null, testValue);

      // if Synch enabled - broadcast must wait until all events will be processed, 
      // so value must be changed
      assertFalse(baseString.equals(testValue.getValue()));
      assertEquals(resultString + "-suffix", testValue.getValue());
      assertEquals("-suffix", TestHolder.tl.get());
   }

   public void testSynchronousExeption() throws Exception
   {
      try
      {
         final String listenerName = "test_synch_exeption";

         assertNotNull(service_);
         Listener<Object, StrValue> listener = new SynchListenerWithException();
         listener.setName(listenerName);
         listener.setDescription("Synchronous listener with exception");

         service_.addListener(listener);

         StrValue testValue = new StrValue("no matter");

         service_.broadcast(listenerName, null, testValue);
         // exception must be ignored
      }
      catch (Exception e)
      {
         fail("Exception must be ignored.");
      }
   }

   public void testAsynchronousExeption() throws Exception
   {
      try
      {
         final String listenerName = "test_asynch_exeption";

         assertNotNull(service_);
         Listener<Object, StrValue> listener = new AsynchListenerWithException();
         listener.setName(listenerName);
         listener.setDescription("Asynchronous listener with exception");

         service_.addListener(listener);

         StrValue testValue = new StrValue("no matter");

         service_.broadcast(listenerName, null, testValue);
         // exception must be ignored

         Thread.sleep(1000);
      }
      catch (Exception e)
      {
         fail("Exception must be ignored.");
      }
   }

   class StrValue
   {
      private String val;

      public StrValue(String value)
      {
         val = value;
      }

      public void setValue(String value)
      {
         val = value + TestHolder.tl.get();
      }

      public String getValue()
      {
         return val;
      }
   }

   @Asynchronous
   class AsynchListener extends Listener<Object, StrValue>
   {
      @Override
      public void onEvent(Event<Object, StrValue> event) throws Exception
      {
         StrValue value = event.getData();
         //wait
         synchronized (value)
         {
            //change test value
            value.setValue("Value become changed");
            value.notifyAll();
         }
      }
   }

   class SynchListener extends Listener<Object, StrValue>
   {
      @Override
      public void onEvent(Event<Object, StrValue> event) throws Exception
      {
         //wait
         Thread.sleep(1000);
         //change test value
         event.getData().setValue("Value become changed");
      }
   }

   class ExtendedAsynchListener extends AsynchListener
   {
      // do nothing. This class exist only for check, does ListenerService process 
      // extended Asynchronous listeners as asynchronous
   }

   @Asynchronous
   class AsynchListenerWithException extends Listener<Object, StrValue>
   {
      @Override
      public void onEvent(Event<Object, StrValue> event) throws Exception
      {
         //wait
         Thread.sleep(1000);

         throw new Exception("This is test exception");
      }
   }

   class SynchListenerWithException extends Listener<Object, StrValue>
   {
      @Override
      public void onEvent(Event<Object, StrValue> event) throws Exception
      {
         //wait
         Thread.sleep(1000);

         throw new Exception("This is test exception");
      }
   }
   
   public static class TestHolder implements ThreadContextHolder
   {
      public static ThreadLocal<String> tl = new ThreadLocal<String>();
      public ThreadContext getThreadContext()
      {
         return new ThreadContext(tl);
      }
   }
}
