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

import org.exoplatform.commons.utils.PropertyManager;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rpc.RPCException;
import org.exoplatform.services.rpc.RPCService;
import org.exoplatform.services.rpc.RemoteCommand;
import org.exoplatform.services.rpc.TopologyChangeEvent;
import org.exoplatform.services.rpc.TopologyChangeListener;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.conf.ConfiguratorFactory;
import org.jgroups.conf.ProtocolStackConfigurator;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;
import org.picocontainer.Startable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.net.URL;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * This class is a basic implementation of the {@link RPCService}, it is mainly based on the
 * {@link MessageDispatcher} of JGroups. This implementation is not designed to give 
 * the best possible performances, it only aims to give a way to communicate with other nodes.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public abstract class AbstractRPCService implements RPCService, Startable, RequestHandler, MembershipListener
{

   /**
    * Connection logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.RPCServiceImpl");
   
   /**
    * The name of the parameter for the location of the JGroups configuration.
    */
   protected static final String PARAM_JGROUPS_CONFIG = "jgroups-configuration";

   /**
    * The name of the parameter for the name of the cluster.
    */
   protected static final String PARAM_CLUSTER_NAME = "jgroups-cluster-name";

   /**
    * The name of the parameter for the default timeout
    */
   protected static final String PARAM_DEFAULT_TIMEOUT = "jgroups-default-timeout";

   /**
    * The name of the parameter to allow the failover
    */
   protected static final String PARAM_ALLOW_FAILOVER = "allow-failover";

   /**
    * The name of the parameter for the retry timeout
    */
   protected static final String PARAM_RETRY_TIMEOUT = "retry-timeout";
   
   /**
    * The value of the default timeout
    */
   protected static final int DEFAULT_TIMEOUT = 0;
   
   /**
    * The value of the default retry timeout
    */
   protected static final int DEFAULT_RETRY_TIMEOUT = 20000;

   /**
    * The default value of the cluster name
    */
   protected static final String CLUSTER_NAME = "RPCService-Cluster";
   
   /**
    * The configurator used to create the JGroups Channel
    */
   protected final ProtocolStackConfigurator configurator;

   /**
    * The lock used to synchronize all the threads waiting for a topology change.
    */
   private final Object topologyChangeLock = new Object();
   
   /**
    * The name of the cluster
    */
   private final String clusterName;

   /**
    * The JGroups Channel used to communicate with other nodes
    */
   protected Channel channel;

   /**
    * The current list of all the members of the cluster
    */
   protected volatile List<Address> members;

   /**
    * The address of the current coordinator
    */
   protected volatile Address coordinator;

   /**
    * Indicates whether the current node is the coordinator of the cluster or not
    */
   protected volatile boolean isCoordinator;
   
   /**
    * The default value of the timeout
    */
   private long defaultTimeout = DEFAULT_TIMEOUT;

   /**
    * The value of the retry timeout
    */
   private long retryTimeout = DEFAULT_RETRY_TIMEOUT;
   
   /**
    * Indicates whether the failover capabilities are enabled
    */
   private boolean allowFailover = true;
   
   /**
    * The dispatcher used to launch the command of the cluster nodes
    */
   protected MessageDispatcher dispatcher;

   /**
    * The signal that indicates that the service is started, it will be used
    * to make the application wait until the service is fully started to
    * ensure that all the commands have been registered before handling
    * incoming messages.
    */
   private final CountDownLatch startSignal = new CountDownLatch(1);
   
   /**
    * All the registered {@link TopologyChangeListener}
    */
   private final List<TopologyChangeListener> listeners = new CopyOnWriteArrayList<TopologyChangeListener>();

   /**
    * Current State of the {@link RPCServiceImpl}
    */
   private volatile State state;

   /**
    * All the commands that have been registered
    */
   private volatile Map<String, RemoteCommand> commands =
      Collections.unmodifiableMap(new HashMap<String, RemoteCommand>());

   /**
    * The public constructor
    * @param ctx the {@link ExoContainerContext} from which we will extract the corresponding
    * {@link ExoContainer}
    * @param params the list of initial parameters
    * @param configManager the configuration manager used to get the configuration
    * of JGroups
    */
   public AbstractRPCService(ExoContainerContext ctx, InitParams params, ConfigurationManager configManager)
   {
      if (params == null)
      {
         throw new IllegalArgumentException("The RPCServiceImpl requires some parameters");
      }
      final URL properties = getProperties(params, configManager);
      if (LOG.isInfoEnabled())
      {
         LOG.info("The JGroups configuration used for the RPCServiceImpl will be loaded from " + properties);
      }

      try
      {
         this.configurator = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<ProtocolStackConfigurator>()
         {
            public ProtocolStackConfigurator run() throws Exception
            {
               return ConfiguratorFactory.getStackConfigurator(properties);
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         throw new RuntimeException("Cannot load the JGroups configuration from " + properties, pae.getCause());
      }

      this.clusterName = getClusterName(ctx, params);
      if (LOG.isDebugEnabled())
      {
         LOG.debug("The cluster name of the RPCServiceImpl has been set to " + clusterName);
      }
      String sTimeout = getValueParam(params, PARAM_DEFAULT_TIMEOUT);
      if (sTimeout != null)
      {
         defaultTimeout = Integer.parseInt(sTimeout);
         if (LOG.isDebugEnabled())
         {
            LOG.debug("The default timeout of the RPCServiceImpl has been set to " + defaultTimeout);
         }
      }
      String sAllowFailover = getValueParam(params, PARAM_ALLOW_FAILOVER);
      if (sAllowFailover != null)
      {
         allowFailover = Boolean.valueOf(sAllowFailover);
         if (LOG.isDebugEnabled())
         {
            LOG.debug("The parameter '" + PARAM_ALLOW_FAILOVER + "' of the RPCServiceImpl has been set to " + allowFailover);
         }
      }
      sTimeout = getValueParam(params, PARAM_RETRY_TIMEOUT);
      if (sTimeout != null)
      {
         retryTimeout = Integer.parseInt(sTimeout);
         if (LOG.isDebugEnabled())
         {
            LOG.debug("The retry timeout of the RPCServiceImpl has been set to " + retryTimeout);
         }
      }
      this.state = State.INITIALIZED;
   }

   /**
    * {@inheritDoc}
    */
   public List<Object> executeCommandOnAllNodes(RemoteCommand command, boolean synchronous, Serializable... args)
      throws RPCException
   {
      return executeCommandOnAllNodesMain(command, synchronous, defaultTimeout, args);
   }

   /**
    * {@inheritDoc}
    */
   public List<Object> executeCommandOnAllNodes(RemoteCommand command, long timeout, Serializable... args)
      throws RPCException
   {
      return executeCommandOnAllNodesMain(command, true, timeout, args);
   }

   /**
    * Executes a command on all the cluster nodes. This method is equivalent to the other method of the
    * same type but with the default timeout. The command must be registered first otherwise an 
    * {@link RPCException} will be thrown.
    *
    * @param command The command to execute on each cluster node
    * @param synchronous if true, sets group request mode to {@link org.jgroups.blocks.GroupRequest#GET_ALL},
    *  and if false sets it to {@link org.jgroups.blocks.GroupRequest#GET_NONE}.
    * @param timeout a timeout after which to throw a replication exception.
    * @param args an array of {@link Serializable} objects corresponding to parameters of the command 
    * to execute remotely
    * @return a list of responses from all the members of the cluster. If we met an exception on a given node, 
    * the RPCException will be the corresponding response of this particular node
    * @throws RPCException in the event of problems.
    */
   protected List<Object> executeCommandOnAllNodesMain(RemoteCommand command, boolean synchronous, long timeout,
      Serializable... args) throws RPCException
   {
      return excecuteCommand(members, command, synchronous, timeout, args);
   }

   /**
    * {@inheritDoc}
    */
   public Object executeCommandOnCoordinator(RemoteCommand command, boolean synchronous, Serializable... args)
      throws RPCException
   {
      return executeCommandOnCoordinatorMain(command, synchronous, defaultTimeout, args);
   }

   /**
    * {@inheritDoc}
    */
   public Object executeCommandOnCoordinator(RemoteCommand command, long timeout, Serializable... args)
      throws RPCException
   {
      return executeCommandOnCoordinatorMain(command, true, timeout, args);
   }

   /**
    * Executes a command on the coordinator only. This method is equivalent to the other method of the
    * same type but with the default timeout. The command must be registered first otherwise an 
    * {@link RPCException} will be thrown.
    *
    * @param command The command to execute on the coordinator node
    * @param synchronous if true, sets group request mode to {@link org.jgroups.blocks.GroupRequest#GET_ALL}, 
    * and if false sets it to {@link org.jgroups.blocks.GroupRequest#GET_NONE}.
    * @param timeout a timeout after which to throw a replication exception.
    * @param args an array of {@link Serializable} objects corresponding to parameters of the command 
    * to execute remotely
    * @return the response of the coordinator.
    * @throws RPCException in the event of problems.
    */
   protected Object executeCommandOnCoordinatorMain(RemoteCommand command, boolean synchronous, long timeout,
      Serializable... args) throws RPCException
   {
      Address coordinator = this.coordinator;
      Vector<Address> v = new Vector<Address>(1);
      v.add(coordinator);
      List<Object> lResults = excecuteCommand(v, command, synchronous, timeout, args);
      Object result = lResults == null || lResults.size() == 0 ? null : lResults.get(0);
      if (allowFailover && result instanceof MemberHasLeftException)
      {
         // The failover capabilities have been enabled and the coordinator seems to have left
         if (coordinator.equals(this.coordinator))
         {
            synchronized(topologyChangeLock)
            {
               if (coordinator.equals(this.coordinator))
               {
                  if (LOG.isTraceEnabled())
                     LOG.trace("The coordinator did not change yet, we will relaunch the command after " 
                              + retryTimeout + " ms or once a topology change has been detected");                  
                  try
                  {
                     topologyChangeLock.wait(retryTimeout);
                  }
                  catch (InterruptedException e)
                  {
                     Thread.currentThread().interrupt();
                  }                  
               }
            }
         }
         if (LOG.isTraceEnabled())
            LOG.trace("The coordinator has changed, we will automatically retry with the new coordinator");                  
         return executeCommandOnCoordinator(command, synchronous, timeout, args);
      }
      else if (result instanceof RPCException)
      {
         throw (RPCException)result;
      }
      return result;
   }

   /**
    * Execute the command on all the nodes corresponding to the list of destinations.
    * @param dests the list of members on which the command needs to be executed
    * @param command the command to execute
    * @param synchronous if true, sets group request mode to {@link org.jgroups.blocks.GroupRequest#GET_ALL}, and if false sets 
    * it to {@link org.jgroups.blocks.GroupRequest#GET_NONE}.
    * @param timeout a timeout after which to throw a replication exception.
    * @param args the list of parameters
    * @return a list of responses from all the targeted members of the cluster.
    * @throws RPCException in the event of problems.
    */
   protected List<Object> excecuteCommand(final List<Address> dests, RemoteCommand command,
      final boolean synchronous, final long timeout, Serializable... args) throws RPCException
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(RPCService.ACCESS_RPC_SERVICE_PERMISSION);
      }
      if (state != State.STARTED)
      {
         throw new RPCException(
            "Cannot execute any commands if the service is not started, the current state of the service is " + state);
      }
      final String commandId = command.getId();
      if (commands.get(commandId) != command)
      {
         throw new RPCException("Command " + commandId + " unknown, please register your command first");
      }
      final Message msg = new Message();
      setObject(msg, new MessageBody(dests.size() == 1 && dests != members ? dests.get(0) : null, commandId, args)); //NOSONAR
      RspList rsps = SecurityHelper.doPrivilegedAction(new PrivilegedAction<RspList>()
      {
         public RspList run()
         {
            try
            {
               return castMessage(dests, msg, synchronous, timeout);
            }
            catch (Exception e)
            {
               LOG.error("Could not cast the message corresponding to the command " + commandId + ".", e);
            }
            return null;
         }
      });

      if (LOG.isTraceEnabled())
         LOG.trace("responses: " + rsps);
      if (rsps == null)
         throw new RPCException("Could not get the responses for command " + commandId + ".");
      if (!synchronous)
         return Collections.emptyList();// async case
      if (LOG.isTraceEnabled())
      {
         LOG.trace("(" + getLocalAddress() + "): responses for command " + commandId + ":\n" + rsps);
      }
      List<Object> retval = new ArrayList<Object>(rsps.size());
      for (Address dest : dests)
      {
         Rsp rsp = rsps.get(dest);
         if (rsp == null || (rsp.wasSuspected() && !rsp.wasReceived()))
         {
            // The corresponding member has left
            retval.add(new MemberHasLeftException("No response for the member " + dest
               + ", this member has probably left the cluster."));
         }
         else if (!rsp.wasReceived())
         {
            retval.add(new RPCException("Replication timeout for " + rsp.getSender() + ", rsp=" + rsp));
         }
         else
         {
            Object value = rsp.getValue();
            if (value instanceof RPCException)
            {
               // if we have any application-level exceptions make sure we throw them!!
               if (LOG.isTraceEnabled())
                  LOG.trace("Recieved exception'" + value + "' from " + rsp.getSender(), (RPCException)value);
            }
            retval.add(value);
         }
      }
      return retval;
   }

   /**
    * {@inheritDoc}
    */
   public Object handle(Message msg)
   {
      String commandId = null;
      try
      {
         // Ensure that the service is fully started before trying to execute any command
         startSignal.await();
         MessageBody body = (MessageBody)msg.getObject();
         commandId = body.getCommandId();
         if (!body.accept(getLocalAddress()))
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("Command : " + commandId + " needs to be executed on the coordinator " +
                     "only and the local node is not the coordinator, the command will be ignored");
            }
            return null;
         }
         RemoteCommand command = getCommand(commandId);
         if (command == null)
         {
            return new RPCException("Command " + commandId + " unkown, please register your command first");
         }
         Object execResult = command.execute(body.getArgs());
         if (LOG.isTraceEnabled())
         {
            LOG.trace("Command : " + commandId + " executed, result is: " + execResult);
         }
         return execResult;
      }
      catch (Throwable x)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("Problems invoking command.", x);
         }
         return new RPCException("Cannot execute the command " + (commandId == null ? "" : commandId), x);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void block()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void suspect(Address suspectedMbr)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void viewAccepted(View view)
   {
      boolean coordinatorHasChanged;
      synchronized (topologyChangeLock)
      {
         this.members = getMembers(view);
         Address currentCoordinator = coordinator;
         this.coordinator = members != null && members.size() > 0 ? members.get(0) : null;
         this.isCoordinator = coordinator != null && coordinator.equals(getLocalAddress());
         coordinatorHasChanged = currentCoordinator != null && !currentCoordinator.equals(coordinator);
         // Release all the nodes
         topologyChangeLock.notifyAll();
      }
      onTopologyChange(coordinatorHasChanged);
   }

   /**
    * Called anytime the topology has changed, this method will notify all the listeners
    * currently registered
    * @param coordinatorHasChanged this parameter is set to <code>true</code> if the 
    * coordinator has changed, <code>false</code> otherwise
    */
   private void onTopologyChange(boolean coordinatorHasChanged)
   {
      TopologyChangeEvent event = new TopologyChangeEvent(coordinatorHasChanged, isCoordinator);
      for (TopologyChangeListener listener : listeners)
      {
         try
         {
            listener.onChange(event);
         }
         catch (Exception e)
         {
            LOG.warn("An error occurs with the listener of type " + listener.getClass(), e);
         }
      }
   }

   /**
    * {@inheritDoc}
    */
   public synchronized RemoteCommand registerCommand(RemoteCommand command)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(RPCService.ACCESS_RPC_SERVICE_PERMISSION);
      }
      if (command != null)
      {
         String commandId = command.getId();
         if (commandId == null)
         {
            throw new IllegalArgumentException("The command Id cannot be null");
         }
         Map<String, RemoteCommand> tmpCommands = new HashMap<String, RemoteCommand>(this.commands);
         RemoteCommand oldCommand = tmpCommands.put(commandId, command);
         if (oldCommand != null && PropertyManager.isDevelopping())
         {
            LOG.warn("A command has already been registered with the id " + commandId
               + ", this command will be replaced with the new one");
         }
         this.commands = Collections.unmodifiableMap(tmpCommands);
         return command;
      }
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public synchronized void unregisterCommand(RemoteCommand command)
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(RPCService.ACCESS_RPC_SERVICE_PERMISSION);
      }
      if (command != null)
      {
         String commandId = command.getId();
         if (commandId == null)
         {
            throw new IllegalArgumentException("The command Id cannot be null");
         }
         if (commands.get(commandId) != command)
         {
            // We prevent to remove any command that has not been registered, thus we expect that
            // the registered instance is exactly the same instance as the one that we want to
            // unregister
            if (PropertyManager.isDevelopping())
            {
               LOG.warn("Cannot unregister an unknown RemoteCommand, either the command id " + commandId
                  + " is unknown or the instance of RemoteCommand to unregister is unknown");
            }
            return;
         }
         Map<String, RemoteCommand> tmpCommands = new HashMap<String, RemoteCommand>(this.commands);
         tmpCommands.remove(commandId);
         this.commands = Collections.unmodifiableMap(tmpCommands);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isCoordinator() throws RPCException
   {
      if (state != State.STARTED)
      {
         throw new RPCException("Cannot know whether the local node is a coordinator or not if " +
                  "the service is not started, the current state of the service is " + state);
      }
      return isCoordinator;
   }

   /**
    * {@inheritDoc}
    */
   public void registerTopologyChangeListener(TopologyChangeListener listener) throws SecurityException
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(RPCService.ACCESS_RPC_SERVICE_PERMISSION);
      }
      if (listener == null)
      {
         return;
      }
      listeners.add(listener);   
   }

   /**
    * {@inheritDoc}
    */
   public void unregisterTopologyChangeListener(TopologyChangeListener listener) throws SecurityException
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(RPCService.ACCESS_RPC_SERVICE_PERMISSION);
      }
      if (listener == null)
      {
         return;
      }
      listeners.remove(listener);
   }

   /**
    * Gives the {@link RemoteCommand} corresponding to the given id
    * @param commandId the command id of the command to retrieve
    * @return the corresponding {@link RemoteCommand}
    */
   protected RemoteCommand getCommand(String commandId)
   {
      return commands.get(commandId);
   }

   /**
    * {@inheritDoc}
    */
   public void start()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(RPCService.ACCESS_RPC_SERVICE_PERMISSION);
      }

      try
      {
         SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Void>()
         {
            public Void run() throws Exception
            {
               channel = createChannel();
               dispatcher = new MessageDispatcher(channel, null, AbstractRPCService.this, AbstractRPCService.this);
               channel.connect(clusterName);
               return null;
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         throw new RuntimeException("Cannot initialize the Channel needed for the RPCServiceImpl", pae.getCause());
      }
      finally
      {
         this.state = State.STARTED;
         startSignal.countDown();
      }
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
      SecurityManager security = System.getSecurityManager();
      if (security != null)
      {
         security.checkPermission(RPCService.ACCESS_RPC_SERVICE_PERMISSION);
      }

      this.state = State.STOPPED;
      this.isCoordinator = false;
      if (channel != null && channel.isOpen())
      {
         if (LOG.isInfoEnabled())
            LOG.info("Disconnecting and closing the Channel");
         SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
         {
            public Void run()
            {
               channel.disconnect();
               channel.close();
               return null;
            }
         });
         channel = null;
      }
      if (dispatcher != null)
      {
         dispatcher.stop();
         dispatcher = null;
      }
   }

   /**
    * Gives the value of the default timeout
    * @return the default timeout
    */
   protected long getDefaultTimeout()
   {
      return defaultTimeout;
   }

   /**
    * Gives the name of the cluster
    * @return the name of the cluster
    */
   protected String getClusterName()
   {
      return clusterName;
   }
   
   /**
    * Gives the value of the retry timeout
    * @return the value of the retry timeout
    */
   protected long getRetryTimeout()
   {
      return retryTimeout;
   }

   /**
    * Indicates whether the failover capabilities are enabled or not
    * @return <code>true</code> if the failover capabilities are allowed, <code>false</code>
    * otherwise
    */
   protected boolean isAllowFailover()
   {
      return allowFailover;
   }

   /**
    * Returns the channel's own address. The result of calling this method on an unconnected
    * channel is implementation defined (may return null). Calling this method on a closed
    * channel returns null. Successor to {@link #getAddress()}. Addresses can be used as destination
    * in the <code>send()</code> operation.
    * @return The channel's address (opaque) or null if it cannot be found
    */
   protected abstract Address getLocalAddress();
   
   /**
    * Cast a message to all the given members
    * @param dests The members to which the message is to be sent.
    * @param msg The message to be sent to the members.
    * @param synchronous Indicates whether the message must be sent in synchronous or asynchronous mode.
    * @param timeout If 0: wait forever. Otherwise, wait for responses or timeout time.
    * @return A list of responses. Each response is an <code>Object</code> and associated to its sender.
    * @throws Exception if any error occur while casting the message
    */
   protected abstract RspList castMessage(List<Address> dests, Message msg, boolean synchronous, long timeout) throws Exception;
   
   /**
    * Create a channel
    * @return An initialized channel
    * @throws Exception if any error occur while creating the channel
    */
   protected abstract Channel createChannel() throws Exception;
   
   /**
    * Returns a reference to the List of members (ordered)
    * Do NOT change this list, hence your will invalidate the view
    * Make a copy if you have to modify it.
    *
    * @return a reference to the ordered list of members in this view
    */
   protected abstract List<Address> getMembers(View view);
   
   /**
    * Takes an object and uses Java serialization to generate the byte[] buffer which
    * is set in the message.
    */
   protected abstract void setObject(Message m, Object o);
   
   /**
    * Gives the value of the {@link ValueParam} corresponding to the given key
    * @param params the list of initial parameters from which we want to extract the {@link ValueParam}
    * @param parameterKey the name of the {@link ValueParam} that we are looking for
    * @return the value if it exists, null otherwise
    */
   private static String getValueParam(InitParams params, String parameterKey)
   {
      try
      {
         return params.getValueParam(parameterKey).getValue().trim();
      }
      catch (NullPointerException e)
      {
         return null;
      }
   }

   /**
    * Gives the {@link URL} corresponding to the location of the JGroups configuration
    * @param params the initial parameters from which we extract the parameter 
    * <code>PARAM_JGROUPS_CONFIG</code> 
    * @param configManager the configuration manager used to get the {@link URL} corresponding
    * to the path given in the configuration of the RPCServiceImpl
    * @return The {@link URL} corresponding to the location of the JGroups configuration,
    * it will throw {@link RuntimeException} otherwise since it is a mandatory configuration.
    */
   private static URL getProperties(InitParams params, ConfigurationManager configManager)
   {
      String configPath = getValueParam(params, PARAM_JGROUPS_CONFIG);
      if (configPath == null)
      {
         throw new IllegalArgumentException("The parameter '" + PARAM_JGROUPS_CONFIG
            + "' of RPCServiceImpl is mandatory");
      }
      URL properties;
      try
      {
         properties = configManager.getResource(configPath);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Cannot find the JGroups configuration at " + configPath, e);
      }
      if (properties == null)
      {
         throw new IllegalArgumentException("Cannot find the JGroups configuration at " + configPath);
      }
      return properties;
   }

   /**
    * Gives the name of the cluster that will be able to support several portal containers
    * since the name will be post fixed with "-${container-name}"
    * @param ctx the context from which we extract the name of the container
    * @param params the list of initial parameters from which we get the value of the parameter
    * <code>PARAM_CLUSTER_NAME</code> if it exists otherwise the value will be "RPCService-Cluster"
    */
   private static String getClusterName(ExoContainerContext ctx, InitParams params)
   {
      String clusterName = getValueParam(params, PARAM_CLUSTER_NAME);
      if (clusterName == null)
      {
         clusterName = CLUSTER_NAME;
      }
      return clusterName += "-" + ctx.getName();
   }

   /**
    * This intern class will be used to 
    */
   public static class MessageBody implements Externalizable
   {
      /**
       * The Id of the command to execute
       */
      private String commandId;

      /**
       * The list of parameters
       */
      private Serializable[] args;
      
      /**
       * The hash code of the expected destination
       */
      private int destination;

      public MessageBody()
      {
      }

      /**
       * @param dest The destination of the message
       * @param commandId the id of the command to execute
       * @param args the arguments to use
       */
      public MessageBody(Address dest, String commandId, Serializable[] args)
      {
         this.commandId = commandId;
         this.args = args;
         this.destination = dest == null ? 0 : dest.hashCode();
      }

      public String getCommandId()
      {
         return commandId;
      }

      public Serializable[] getArgs()
      {
         return args;
      }      

      /**
       * Indicates whether or not the given message body accepts the given address
       * @param address the address to check
       * @return <code>true</code> if the message is for everybody or if the given address is the expected address,
       * <code>false</code> otherwise
       */
      public boolean accept(Address address)
      {
         return destination == 0 || destination == address.hashCode();
      }

      /**
       * {@inheritDoc}
       */
      public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
      {
         boolean unicast = in.readBoolean();
         if (unicast)
         {
            this.destination = in.readInt();            
         }
         this.commandId = in.readUTF();
         int size = in.readInt();
         if (size == -1)
         {
            this.args = null;
         }
         else
         {
            this.args = new Serializable[size];
            for (int i = 0; i < size; i++)
            {
               args[i] = (Serializable)in.readObject();
            }
         }
      }

      /**
       * {@inheritDoc}
       */
      public void writeExternal(ObjectOutput out) throws IOException
      {
         boolean unicast = destination != 0;
         out.writeBoolean(unicast);
         if (unicast)
         {
            out.writeInt(destination);            
         }         
         out.writeUTF(commandId);
         if (args == null)
         {
            out.writeInt(-1);
         }
         else
         {
            out.writeInt(args.length);
            for (int i = 0; i < args.length; i++)
            {
               out.writeObject(args[i]);
            }
         }
      }
   }

   /**
    * All the potential states of the {@link RPCServiceImpl}
    */
   public enum State
   {
      INITIALIZED, STARTED, STOPPED
   }

   public static class MemberHasLeftException extends RPCException
   {

      /**
       * The serial version UID
       */
      private static final long serialVersionUID = 3558158913564367637L;

      public MemberHasLeftException(String message)
      {
         super(message);
      }
   }
}
