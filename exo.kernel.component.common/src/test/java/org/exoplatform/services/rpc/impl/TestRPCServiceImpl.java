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
package org.exoplatform.services.rpc.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.rpc.RPCException;
import org.exoplatform.services.rpc.RemoteCommand;
import org.exoplatform.services.rpc.SingleMethodCallCommand;
import org.exoplatform.services.rpc.TopologyChangeEvent;
import org.exoplatform.services.rpc.TopologyChangeListener;
import org.exoplatform.services.rpc.impl.RPCServiceImpl.MemberHasLeftException;
import org.exoplatform.test.BasicTestCase;
import org.jgroups.Address;

/**
 * This is the unit test class for the service {@link RPCServiceImpl}
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestRPCServiceImpl extends BasicTestCase
{
   private PortalContainer container;
   private ConfigurationManager configManager;
   
   public void setUp() throws Exception
   {
      container = PortalContainer.getInstance();
      configManager = (ConfigurationManager)container.getComponentInstanceOfType(ConfigurationManager.class);
   }
   
   public void testParameters()
   {
      InitParams params = null;
      try
      {
         new RPCServiceImpl(container.getContext(), params, configManager);
         fail("We expect a IllegalArgumentException since the jgroups config cannot be found");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      params = new InitParams();
      try
      {
         new RPCServiceImpl(container.getContext(), params, configManager);
         fail("We expect a IllegalArgumentException since the jgroups config cannot be found");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      ValueParam paramConf = new ValueParam();
      paramConf.setName(RPCServiceImpl.PARAM_JGROUPS_CONFIG);
      params.addParameter(paramConf);
      try
      {
         new RPCServiceImpl(container.getContext(), params, configManager);
         fail("We expect a IllegalArgumentException since the jgroups config cannot be found");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      paramConf.setValue("fakePath");
      try
      {
         new RPCServiceImpl(container.getContext(), params, configManager);
         fail("We expect a IllegalArgumentException since the jgroups config cannot be found");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      paramConf.setValue("jar:/conf/portal/udp.xml");
      RPCServiceImpl service = null;
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         assertEquals(RPCServiceImpl.DEFAULT_TIMEOUT, service.getDefaultTimeout());
         assertEquals(RPCServiceImpl.DEFAULT_RETRY_TIMEOUT, service.getRetryTimeout());
         assertEquals(true, service.isAllowFailover());
         assertEquals(RPCServiceImpl.CLUSTER_NAME + "-" + container.getContext().getName(), service.getClusterName());
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }
      ValueParam paramTimeout = new ValueParam();
      paramTimeout.setName(RPCServiceImpl.PARAM_DEFAULT_TIMEOUT);
      paramTimeout.setValue("fakeValue");
      params.addParameter(paramTimeout);
      try
      {
         new RPCServiceImpl(container.getContext(), params, configManager);
         fail("We expect a NumberFormatException since the timeout is not properly set");
      }
      catch (NumberFormatException e)
      {
         // OK
      }
      paramTimeout.setValue("60");
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         assertEquals(60, service.getDefaultTimeout());
         assertEquals(RPCServiceImpl.DEFAULT_RETRY_TIMEOUT, service.getRetryTimeout());
         assertEquals(true, service.isAllowFailover());
         assertEquals(RPCServiceImpl.CLUSTER_NAME + "-" + container.getContext().getName(), service.getClusterName());
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }
      ValueParam paramRetryTimeout = new ValueParam();
      paramRetryTimeout.setName(RPCServiceImpl.PARAM_RETRY_TIMEOUT);
      paramRetryTimeout.setValue("fakeValue");
      params.addParameter(paramRetryTimeout);
      try
      {
         new RPCServiceImpl(container.getContext(), params, configManager);
         fail("We expect a NumberFormatException since the retry timeout is not properly set");
      }
      catch (NumberFormatException e)
      {
         // OK
      }      
      paramRetryTimeout.setValue("60");
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         assertEquals(60, service.getDefaultTimeout());
         assertEquals(60, service.getRetryTimeout());
         assertEquals(true, service.isAllowFailover());
         assertEquals(RPCServiceImpl.CLUSTER_NAME + "-" + container.getContext().getName(), service.getClusterName());
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }
      ValueParam paramAllowFailover = new ValueParam();
      paramAllowFailover.setName(RPCServiceImpl.PARAM_ALLOW_FAILOVER);
      paramAllowFailover.setValue("fakeValue");
      params.addParameter(paramAllowFailover);
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         assertEquals(60, service.getDefaultTimeout());
         assertEquals(60, service.getRetryTimeout());
         assertEquals(false, service.isAllowFailover());
         assertEquals(RPCServiceImpl.CLUSTER_NAME + "-" + container.getContext().getName(), service.getClusterName());
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }
      paramAllowFailover.setValue("TRUE");
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         assertEquals(60, service.getDefaultTimeout());
         assertEquals(60, service.getRetryTimeout());
         assertEquals(true, service.isAllowFailover());
         assertEquals(RPCServiceImpl.CLUSTER_NAME + "-" + container.getContext().getName(), service.getClusterName());
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }
      
      ValueParam paramClusterName = new ValueParam();      
      paramClusterName.setName(RPCServiceImpl.PARAM_CLUSTER_NAME);
      paramClusterName.setValue("MyName");
      params.addParameter(paramClusterName);
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         assertEquals(60, service.getDefaultTimeout());
         assertEquals(paramClusterName.getValue() + "-" + container.getContext().getName(), service.getClusterName());
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }
   }
   
   public void testStates() throws Exception
   {
      InitParams params = new InitParams();
      ValueParam paramConf = new ValueParam();
      paramConf.setName(RPCServiceImpl.PARAM_JGROUPS_CONFIG);
      paramConf.setValue("jar:/conf/portal/udp.xml");      
      params.addParameter(paramConf);
      RPCServiceImpl service = null;
      RemoteCommand foo  = new RemoteCommand()
      {
         
         public String getId()
         {
            return "foo";
         }
         
         public String execute(Serializable[] args) throws Throwable
         {
            return null;
         }
      };
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         
         service.registerCommand(foo);
         try
         {
            service.executeCommandOnAllNodes(foo, true);
            fail("We expect a RPCException since the current state is not the expected one");
         }
         catch (RPCException e)
         {
            // OK
         }
         try
         {
            service.executeCommandOnAllNodes(foo, 10);
            fail("We expect a RPCException since the current state is not the expected one");
         }
         catch (RPCException e)
         {
            // OK
         }
         try
         {
            service.executeCommandOnCoordinator(foo, true);
            fail("We expect a RPCException since the current state is not the expected one");
         }
         catch (RPCException e)
         {
            // OK
         }
         try
         {
            service.executeCommandOnCoordinator(foo, 10);
            fail("We expect a RPCException since the current state is not the expected one");
         }
         catch (RPCException e)
         {
            // OK
         }
         try
         {
            service.isCoordinator();
            fail("We expect a RPCException since the current state is not the expected one");
         }
         catch (RPCException e)
         {
            // OK
         }
         service.start();
         assertEquals(true, service.isCoordinator());
         service.executeCommandOnAllNodes(foo, true);
         service.executeCommandOnAllNodes(foo, 10);
         service.executeCommandOnCoordinator(foo, true);
         service.executeCommandOnCoordinator(foo, 10);
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }
      try
      {
         service.executeCommandOnAllNodes(foo, true);
         fail("We expect a RPCException since the current state is not the expected one");
      }
      catch (RPCException e)
      {
         // OK
      }
      try
      {
         service.executeCommandOnAllNodes(foo, 10);
         fail("We expect a RPCException since the current state is not the expected one");
      }
      catch (RPCException e)
      {
         // OK
      }
      try
      {
         service.executeCommandOnCoordinator(foo, true);
         fail("We expect a RPCException since the current state is not the expected one");
      }
      catch (RPCException e)
      {
         // OK
      }
      try
      {
         service.executeCommandOnCoordinator(foo, 10);
         fail("We expect a RPCException since the current state is not the expected one");
      }
      catch (RPCException e)
      {
         // OK
      }      
   }
   
   public void testCommands() throws Exception
   {
      InitParams params = new InitParams();
      ValueParam paramConf = new ValueParam();
      paramConf.setName(RPCServiceImpl.PARAM_JGROUPS_CONFIG);
      paramConf.setValue("jar:/conf/portal/udp.xml");      
      params.addParameter(paramConf);
      RPCServiceImpl service = null;
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         RemoteCommand fake = new RemoteCommand()
         {
            
            public String getId()
            {
               return "fake";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return null;
            }
         };
         RemoteCommand fake2 = new RemoteCommand()
         {
            
            public String getId()
            {
               return "fake2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return null;
            }
         };
         RemoteCommand fake2_Unregistered = new RemoteCommand()
         {
            
            public String getId()
            {
               return "fake2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return null;
            }
         };         
         service.registerCommand(fake2);
         RemoteCommand Exception = new RemoteCommand()
         {
            
            public String getId()
            {
               return "Exception";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               throw new Exception("MyException");
            }
         };
         service.registerCommand(Exception);
         RemoteCommand Error = new RemoteCommand()
         {
            
            public String getId()
            {
               return "Error";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               throw new Error("MyError");
            }
         } ;
         service.registerCommand(Error);
         RemoteCommand StringValue = new RemoteCommand()
         {
            
            public String getId()
            {
               return "StringValue";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "OK";
            }
         };         
         service.registerCommand(StringValue);
         RemoteCommand NullValue = new RemoteCommand()
         {
            
            public String getId()
            {
               return "NullValue";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return null;
            }
         };         
         service.registerCommand(NullValue);
         RemoteCommand LongTask = new RemoteCommand()
         {
            
            public String getId()
            {
               return "LongTask";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               Thread.sleep(2000);
               return null;
            }
         };         
         service.registerCommand(LongTask);         
         service.start();
         try
         {
            service.executeCommandOnAllNodes(fake, true);
            fail("We expect a RPCException since the command is unknown");
         }
         catch (RPCException e)
         {
            // OK
         }
         try
         {
            service.executeCommandOnCoordinator(fake, true);
            fail("We expect a RPCException since the command is unknown");
         }
         catch (RPCException e)
         {
            // OK
         }
         try
         {
            service.executeCommandOnAllNodes(fake2_Unregistered, true);
            fail("We expect a RPCException since the command is unknown");
         }
         catch (RPCException e)
         {
            // OK
         }
         try
         {
            service.executeCommandOnCoordinator(fake2_Unregistered, true);
            fail("We expect a RPCException since the command is unknown");
         }
         catch (RPCException e)
         {
            // OK
         }         
         List<Object> result;
         result = service.executeCommandOnAllNodes(Exception, true);
         assertTrue(result != null && result.size() == 1);
         assertTrue("We expect a RPCException since one node could not execute the command", result.get(0) instanceof RPCException);
         try
         {
            service.executeCommandOnCoordinator(Exception, true);
            fail("We expect a RPCException since one node could not execute the command");
         }
         catch (RPCException e)
         {
            // OK
         }
         result = service.executeCommandOnAllNodes(Error, true);
         assertTrue(result != null && result.size() == 1);
         assertTrue("We expect a RPCException since one node could not execute the command", result.get(0) instanceof RPCException);
         try
         {
            service.executeCommandOnCoordinator(Error, true);
            fail("We expect a RPCException since one node could not execute the command");
         }
         catch (RPCException e)
         {
            // OK
         }
         result = service.executeCommandOnAllNodes(LongTask, true);
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertNull(result.get(0));
         Object o = service.executeCommandOnCoordinator(LongTask, true);
         assertNull(o);
         result = service.executeCommandOnAllNodes(LongTask, 1000);
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertTrue("We expect an RPCException due to a Replication Timeout", result.get(0) instanceof RPCException);
         try
         {
            service.executeCommandOnCoordinator(LongTask, 1000);
            fail("We expect an RPCException due to a Replication Timeout");
         }
         catch (RPCException e)
         {
            // OK
         }
         result = service.executeCommandOnAllNodes(LongTask, false);
         assertNotNull(result);
         assertTrue(result.isEmpty());
         assertNull(service.executeCommandOnCoordinator(LongTask, false));
         
         result = service.executeCommandOnAllNodes(StringValue, true);
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertEquals("OK", result.get(0));
         o = service.executeCommandOnCoordinator(StringValue, true);
         assertEquals("OK", o);
         result = service.executeCommandOnAllNodes(NullValue, true);
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertNull(result.get(0));
         o = service.executeCommandOnCoordinator(NullValue, true);
         assertNull(o);
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }        
   }

   public void testSeveralNodes() throws Exception
   {
      InitParams params = new InitParams();
      ValueParam paramConf = new ValueParam();
      paramConf.setName(RPCServiceImpl.PARAM_JGROUPS_CONFIG);
      paramConf.setValue("jar:/conf/portal/udp.xml");      
      params.addParameter(paramConf);
      RPCServiceImpl service1 = null, service2 = null;      
      try
      {
         service1 = new RPCServiceImpl(container.getContext(), params, configManager);
         service2 = new RPCServiceImpl(container.getContext(), params, configManager);
         RemoteCommand CmdUnknownOnNode2 = new RemoteCommand()
         {
            
            public String getId()
            {
               return "CmdUnknownOnNode2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "OK";
            }
         };
         service1.registerCommand(CmdUnknownOnNode2);
         RemoteCommand ExceptionOnNode2 = new RemoteCommand()
         {
            
            public String getId()
            {
               return "ExceptionOnNode2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "OK";
            }
         };         
         service1.registerCommand(ExceptionOnNode2);
         RemoteCommand ErrorOnNode2 = new RemoteCommand()
         {
            
            public String getId()
            {
               return "ErrorOnNode2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "OK";
            }
         };           
         service1.registerCommand(ErrorOnNode2);
        
         RemoteCommand LongTaskOnNode2 = new RemoteCommand()
         {
            
            public String getId()
            {
               return "LongTaskOnNode2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "OK";
            }
         };           
         service1.registerCommand(LongTaskOnNode2); 
         service1.registerCommand(new RemoteCommand()
         {
            
            public String getId()
            {
               return "LongTask";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               Thread.sleep(3000);
               return "OldCoordinator";
            }
         }); 
         service1.registerCommand(new RemoteCommand()
         {
            
            public String getId()
            {
               return "OK";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "OK";
            }
         });         
         service2.registerCommand(new RemoteCommand()
         {
            
            public String getId()
            {
               return "ExceptionOnNode2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               throw new Exception("MyException");
            }
         });
         service2.registerCommand(new RemoteCommand()
         {
            
            public String getId()
            {
               return "ErrorOnNode2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               throw new Error("MyError");
            }
         });
         service2.registerCommand(new RemoteCommand()
         {
            
            public String getId()
            {
               return "LongTaskOnNode2";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               Thread.sleep(2000);
               return null;
            }
         });
         RemoteCommand OK = new RemoteCommand()
         {
            
            public String getId()
            {
               return "OK";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "OK";
            }
         };
         service2.registerCommand(OK);
         final RemoteCommand LongTask = new RemoteCommand()
         {
            
            public String getId()
            {
               return "LongTask";
            }
            
            public String execute(Serializable[] args) throws Throwable
            {
               return "NewCoordinator";
            }
         };
         service2.registerCommand(LongTask);
         MyListener listener1 = new MyListener();
         service1.registerTopologyChangeListener(listener1);
         MyListener listener2 = new MyListener();
         service2.registerTopologyChangeListener(listener2);
         assertFalse(listener1.coordinatorHasChanged);
         assertFalse(listener1.isCoordinator);
         assertEquals(0, listener1.count);
         assertFalse(listener2.coordinatorHasChanged);
         assertFalse(listener2.isCoordinator);
         assertEquals(0, listener2.count);
         service1.start();
         assertFalse(listener1.coordinatorHasChanged);
         assertTrue(listener1.isCoordinator);
         assertEquals(1, listener1.count);
         assertFalse(listener2.coordinatorHasChanged);
         assertFalse(listener2.isCoordinator);
         assertEquals(0, listener2.count);
         service2.start();
         assertFalse(listener1.coordinatorHasChanged);
         assertTrue(listener1.isCoordinator);
         assertEquals(2, listener1.count);
         assertFalse(listener2.coordinatorHasChanged);
         assertFalse(listener2.isCoordinator);
         assertEquals(1, listener2.count);
         assertEquals(true, service1.isCoordinator());
         assertEquals(false, service2.isCoordinator());
         List<Object> result;
         Object o;
         result = service1.executeCommandOnAllNodes(CmdUnknownOnNode2, true);
         assertTrue(result != null && result.size() == 2);
         assertEquals("OK", result.get(0));
         assertTrue("We expect a RPCException since the command is unknown on node 2", result.get(1) instanceof RPCException);
         o = service1.executeCommandOnCoordinator(CmdUnknownOnNode2, true);
         assertEquals("OK", o);

         result = service1.executeCommandOnAllNodes(ExceptionOnNode2, true);
         assertTrue(result != null && result.size() == 2);
         assertEquals("OK", result.get(0));
         assertTrue("We expect a RPCException since the command fails on node 2", result.get(1) instanceof RPCException);
         o = service1.executeCommandOnCoordinator(ExceptionOnNode2, true);
         assertEquals("OK", o);

         result = service1.executeCommandOnAllNodes(ErrorOnNode2, true);
         assertTrue(result != null && result.size() == 2);
         assertEquals("OK", result.get(0));
         assertTrue("We expect a RPCException since the command fails on node 2", result.get(1) instanceof RPCException);         
         o = service1.executeCommandOnCoordinator(ErrorOnNode2, true);
         assertEquals("OK", o);
         
         result = service1.executeCommandOnAllNodes(LongTaskOnNode2, 1000);
         assertNotNull(result);
         assertTrue(result.size() == 2);
         assertEquals("OK", result.get(0));
         assertTrue("We expect an RPCException due to a Replication Timeout", result.get(1) instanceof RPCException);
         o = service1.executeCommandOnCoordinator(LongTaskOnNode2, 1000);
         assertEquals("OK", o);
         
         Vector<Address> allMembers = service1.members;
         Vector<Address> coordinatorOnly = new Vector<Address>(1);
         coordinatorOnly.add(service1.coordinator);
         
         final RPCServiceImpl service = service2;
         final AtomicReference<Throwable> error = new AtomicReference<Throwable>();
         final CountDownLatch doneSignal = new CountDownLatch(1);
         Thread t = new Thread()
         {
            @Override
            public void run()
            {
               try
               {
                  Object o = service.executeCommandOnCoordinator(LongTask, true);
                  assertEquals("NewCoordinator", o);
               }
               catch (Throwable e)
               {
                  error.set(e);
               }
               finally
               {
                  doneSignal.countDown();
               }
            }           
         };
         t.start();
         service1.stop();
         listener2.waitTopologyChange();
         assertFalse(listener1.coordinatorHasChanged);
         assertTrue(listener1.isCoordinator);
         assertEquals(2, listener1.count);
         assertTrue(listener2.coordinatorHasChanged);
         assertTrue(listener2.isCoordinator);
         assertEquals(2, listener2.count);         
         doneSignal.await();
         assertNull(error.get() != null ? error.get().getMessage() : "", error.get());
         result = service2.excecuteCommand(allMembers, OK, true, 0);
         assertNotNull(result);
         assertTrue(result.size() == 2);
         assertTrue("We expect an RPCException due to a member that has left", result.get(0) instanceof MemberHasLeftException);
         assertEquals("OK", result.get(1));
         result = service2.excecuteCommand(coordinatorOnly, OK, true, 0);
         assertNotNull(result);
         assertTrue(result.size() == 1);
         assertTrue("We expect an RPCException due to a member that has left", result.get(0) instanceof MemberHasLeftException);
         try
         {
            service1.isCoordinator();
            fail("We expect a RPCException since the current state is not the expected one");
         }
         catch (RPCException e)
         {
            // OK
         }
         assertEquals(true, service2.isCoordinator());         
      }
      finally
      {
         if (service1 != null)
         {
            service1.stop();            
         }
         if (service2 != null)
         {
            service2.stop();            
         }
      }        
   }
   
   public void testSingleMethodCallCommand() throws Exception
   {
      try
      {
         new SingleMethodCallCommand(null, null);
         fail("we expect an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      MyService myService = new MyService();
      try
      {
         new SingleMethodCallCommand(myService, null);
         fail("we expect an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      try
      {
         new SingleMethodCallCommand(myService, "foo");
         fail("we expect an NoSuchMethodException");
      }
      catch (NoSuchMethodException e)
      {
         // OK
      }
      try
      {
         new SingleMethodCallCommand(myService, "getPrivateName");
         fail("we expect an IllegalArgumentException since only the public methods are allowed");
      }
      catch (IllegalArgumentException e)
      {
         // OK
      }
      InitParams params = new InitParams();
      ValueParam paramConf = new ValueParam();
      paramConf.setName(RPCServiceImpl.PARAM_JGROUPS_CONFIG);
      paramConf.setValue("jar:/conf/portal/udp.xml");      
      params.addParameter(paramConf);
      RPCServiceImpl service = null;
      try
      {
         service = new RPCServiceImpl(container.getContext(), params, configManager);
         RemoteCommand getName = service.registerCommand(new SingleMethodCallCommand(myService, "getName"));
         RemoteCommand add = service.registerCommand(new SingleMethodCallCommand(myService, "add", int.class));
         RemoteCommand evaluate1 = service.registerCommand(new SingleMethodCallCommand(myService, "evaluate", int[].class));
         RemoteCommand evaluate2 = service.registerCommand(new SingleMethodCallCommand(myService, "evaluate", List.class));
         RemoteCommand total1 = service.registerCommand(new SingleMethodCallCommand(myService, "total", int.class));
         RemoteCommand total2 = service.registerCommand(new SingleMethodCallCommand(myService, "total", int.class, int.class));
         RemoteCommand total3 = service.registerCommand(new SingleMethodCallCommand(myService, "total", int[].class));
         RemoteCommand total4 = service.registerCommand(new SingleMethodCallCommand(myService, "total", String.class, long.class, int[].class));
         RemoteCommand testTypes1 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", String[].class));
         RemoteCommand testTypes2 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", int[].class));
         RemoteCommand testTypes3 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", long[].class));
         RemoteCommand testTypes4 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", byte[].class));
         RemoteCommand testTypes5 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", short[].class));
         RemoteCommand testTypes6 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", char[].class));
         RemoteCommand testTypes7 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", double[].class));
         RemoteCommand testTypes8 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", float[].class));
         RemoteCommand testTypes9 = service.registerCommand(new SingleMethodCallCommand(myService, "testTypes", boolean[].class));

         service.start();
         List<Object> result;
         
         assertEquals("name", service.executeCommandOnCoordinator(getName, true));
         result = service.executeCommandOnAllNodes(getName, true);
         assertTrue(result != null && result.size() == 1);
         assertEquals("name", result.get(0));
         
         assertEquals(10, service.executeCommandOnCoordinator(add, true, 10));
         result = service.executeCommandOnAllNodes(add, true, 10);
         assertTrue(result != null && result.size() == 1);
         assertEquals(20, result.get(0));
         
         assertEquals(100, service.executeCommandOnCoordinator(evaluate1, true, new int[]{10, 10, 10, 30, 40}));
         result = service.executeCommandOnAllNodes(evaluate1, true, new int[]{10, 10, 10, 30, 40});
         assertTrue(result != null && result.size() == 1);
         assertEquals(100, result.get(0));
         
         List<Integer> values = new ArrayList<Integer>();
         values.add(10);
         values.add(10);
         values.add(10);
         values.add(30);
         values.add(40);
         assertEquals(100, service.executeCommandOnCoordinator(evaluate2, true, (Serializable)values));
         result = service.executeCommandOnAllNodes(evaluate2, true, (Serializable)values);
         assertTrue(result != null && result.size() == 1);
         assertEquals(100, result.get(0));
         
         assertEquals(10, service.executeCommandOnCoordinator(total1, true, 10));
         result = service.executeCommandOnAllNodes(total1, true, 10);
         assertTrue(result != null && result.size() == 1);
         assertEquals(10, result.get(0));
         
         assertEquals(20, service.executeCommandOnCoordinator(total2, true, 10, 10));
         result = service.executeCommandOnAllNodes(total2, true, 10, 10);
         assertTrue(result != null && result.size() == 1);
         assertEquals(20, result.get(0));
         
         assertEquals(100, service.executeCommandOnCoordinator(total3, true, new int[]{10, 10, 10, 30, 40}));
         result = service.executeCommandOnAllNodes(total3, true, new int[]{10, 10, 10, 30, 40});
         assertTrue(result != null && result.size() == 1);
         assertEquals(100, result.get(0));
         
         assertEquals(100, service.executeCommandOnCoordinator(total4, true, "foo", 50, new int[]{10, 10, 10, 30, 40}));
         result = service.executeCommandOnAllNodes(total4, true, "foo", 50, new int[]{10, 10, 10, 30, 40});
         assertTrue(result != null && result.size() == 1);
         assertEquals(100, result.get(0));
         
         assertEquals(0, service.executeCommandOnCoordinator(total4, true, "foo", 50, null));
         result = service.executeCommandOnAllNodes(total4, true, "foo", 50, null);
         assertTrue(result != null && result.size() == 1);
         assertEquals(0, result.get(0));
         
         try
         {
            service.executeCommandOnCoordinator(total4, true, "foo", 50);
            fail("We expect a RPCException since the list of arguments mismatch with what is expected");
         }
         catch (RPCException e)
         {
            // OK
         }         
         result = service.executeCommandOnAllNodes(total4, true, "foo", 50);
         assertTrue(result != null && result.size() == 1);
         assertTrue("We expect a RPCException since the list of arguments mismatch with what is expected", result.get(0) instanceof RPCException);
         
         assertEquals("foo", service.executeCommandOnCoordinator(testTypes1, true, (Serializable)new String[]{"foo"}));
         result = service.executeCommandOnAllNodes(testTypes1, true, (Serializable)new String[]{"foo"});
         assertTrue(result != null && result.size() == 1);
         assertEquals("foo", result.get(0));
         
         assertEquals(10, service.executeCommandOnCoordinator(testTypes2, true, new int[]{10}));
         result = service.executeCommandOnAllNodes(testTypes2, true, new int[]{10});
         assertTrue(result != null && result.size() == 1);
         assertEquals(10, result.get(0));
         
         assertEquals(11L, service.executeCommandOnCoordinator(testTypes3, true, new long[]{10}));
         result = service.executeCommandOnAllNodes(testTypes3, true, new long[]{10});
         assertTrue(result != null && result.size() == 1);
         assertEquals(11L, result.get(0));
         
         assertEquals((byte)12, service.executeCommandOnCoordinator(testTypes4, true, new byte[]{10}));
         result = service.executeCommandOnAllNodes(testTypes4, true, new byte[]{10});
         assertTrue(result != null && result.size() == 1);
         assertEquals((byte)12, result.get(0));
         
         assertEquals((short)13, service.executeCommandOnCoordinator(testTypes5, true, new short[]{10}));
         result = service.executeCommandOnAllNodes(testTypes5, true, new short[]{10});
         assertTrue(result != null && result.size() == 1);
         assertEquals((short)13, result.get(0));
         
         assertEquals('a', service.executeCommandOnCoordinator(testTypes6, true, new char[]{'a'}));
         result = service.executeCommandOnAllNodes(testTypes6, true, new char[]{'a'});
         assertTrue(result != null && result.size() == 1);
         assertEquals('a', result.get(0));
         
         assertEquals(10.5, service.executeCommandOnCoordinator(testTypes7, true, new double[]{10}));
         result = service.executeCommandOnAllNodes(testTypes7, true, new double[]{10});
         assertTrue(result != null && result.size() == 1);
         assertEquals(10.5, result.get(0));
         
         assertEquals((float)11.5, service.executeCommandOnCoordinator(testTypes8, true, new float[]{10}));
         result = service.executeCommandOnAllNodes(testTypes8, true, new float[]{10});
         assertTrue(result != null && result.size() == 1);
         assertEquals((float)11.5, result.get(0));
         
         assertEquals(true, service.executeCommandOnCoordinator(testTypes9, true, new boolean[]{true}));
         result = service.executeCommandOnAllNodes(testTypes9, true, new boolean[]{true});
         assertTrue(result != null && result.size() == 1);
         assertEquals(true, result.get(0));
         
      }
      finally
      {
         if (service != null)
         {
            service.stop();            
         }
      }         
   }
   
   public static class MyService
   {
      private int value = 0;
      
      public int add(int i)
      {
         return value += i;
      }
      
      @SuppressWarnings("unused")
      private String getPrivateName()
      {
         return "name";
      }
      
      public String getName()
      {
         return "name";
      }
      
      public int total(int i)
      {
         return i;
      }
      
      public int total(int i1, int i2)
      {
         return i1 + i2;
      }
      
      public int total(int... values)
      {
         int total = 0;
         for (int i : values)
         {
            total += i;
         }
         return total;
      }
      
      public int total(String s, long l, int... values)
      {
         int total = 0;
         if (values != null)
         {
            for (int i : values)
            {
               total += i;
            }            
         }
         return total;
      }
      
      public int evaluate(int[] values)
      {
         int total = 0;
         for (int i : values)
         {
            total += i;
         }
         return total;         
      }
      
      public int evaluate(List<Integer> values)
      {
         int total = 0;
         for (int i : values)
         {
            total += i;
         }
         return total;         
      }
      
      public String testTypes(String... values)
      {
         return values[0];
      }
      
      public boolean testTypes(boolean... values)
      {
         return values[0];
      }
      
      public char testTypes(char... values)
      {
         return values[0];
      }
      
      public double testTypes(double... values)
      {
         return values[0] + 0.5;
      }
      
      public float testTypes(float... values)
      {
         return (float)(values[0] + 1.5);
      }
      
      public int testTypes(int... values)
      {
         return values[0];
      }
      
      public long testTypes(long... values)
      {
         return values[0] + 1;
      }
      
      public byte testTypes(byte... values)
      {
         return (byte)(values[0] + 2);
      }
      
      public short testTypes(short... values)
      {
         return (short)(values[0] + 3);
      }
   }
   
   private static class MyListener implements TopologyChangeListener
   {

      private boolean coordinatorHasChanged;
      private boolean isCoordinator;
      private int count;

      private CountDownLatch lock;
      
      /**
       * @see org.exoplatform.services.rpc.TopologyChangeListener#onChange(org.exoplatform.services.rpc.TopologyChangeEvent)
       */
      public void onChange(TopologyChangeEvent event)
      {
         this.coordinatorHasChanged = event.isCoordinatorHasChanged();
         this.isCoordinator = event.isCoordinator();
         count++;
         
         if (lock != null)
         {
            lock.countDown();
         }
      }

      public void waitTopologyChange() throws InterruptedException
      {
         lock = new CountDownLatch(1);
         lock.await();
      }
   }
}
