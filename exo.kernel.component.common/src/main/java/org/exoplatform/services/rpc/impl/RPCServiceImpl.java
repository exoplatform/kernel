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
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.Message;
import org.jgroups.View;
import org.jgroups.blocks.GroupRequest;
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
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.CountDownLatch;

/**
 * This class is a basic implementation of the {@link RPCService}, it is mainly based on the
 * {@link MessageDispatcher}. This implementation is not designed to give the best possible
 * performances, it only aims to give a way to communicate with other nodes.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public class RPCServiceImpl implements RPCService, Startable, RequestHandler, MembershipListener
{

   /**
    * Connection logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.RPCServiceImpl");

   /**
    * The name of the parameter for the location of the JGroups configuration.
    */
   public static final String PARAM_JGROUPS_CONFIG = "jgroups-configuration";

   /**
    * The name of the parameter for the name of the cluster.
    */
   public static final String PARAM_CLUSTER_NAME = "jgroups-cluster-name";

   /**
    * The name of the parameter for the default timeout
    */
   public static final String PARAM_DEFAULT_TIMEOUT = "jgroups-default-timeout";

   /**
    * The value of the default timeout
    */
   public static final int DEFAULT_TIMEOUT = 0;

   /**
    * The default value of the cluster name
    */
   public static final String CLUSTER_NAME = "RPCService-Cluster";

   /**
    * The configurator used to create the JGroups Channel
    */
   private final ProtocolStackConfigurator configurator;

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
   protected volatile Vector<Address> members;

   /**
    * The address of the current coordinator
    */
   protected volatile Address coordinator;

   /**
    * The default value of the timeout
    */
   private long defaultTimeout = DEFAULT_TIMEOUT;

   /**
    * The dispatcher used to launch the command of the cluster nodes
    */
   private MessageDispatcher dispatcher;

   /**
    * The signal that indicates that the service is started, it will be used
    * to make the application wait until the service is fully started to
    * ensure that all the commands have been registered before handling
    * incoming messages.
    */
   private final CountDownLatch startSignal = new CountDownLatch(1);

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
   public RPCServiceImpl(ExoContainerContext ctx, InitParams params, ConfigurationManager configManager)
   {
      if (params == null)
      {
         throw new IllegalArgumentException("The RPCServiceImpl requires some parameters");
      }
      URL properties = getProperties(params, configManager);
      if (LOG.isInfoEnabled())
      {
         LOG.info("The JGroups configuration used for the RPCServiceImpl will be loaded from " + properties);
      }
      try
      {
         this.configurator = ConfiguratorFactory.getStackConfigurator(properties);
      }
      catch (ChannelException e)
      {
         throw new RuntimeException("Cannot load the JGroups configuration from " + properties, e);
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
      if (result instanceof MemberHasLeftException)
      {
         if (coordinator.equals(this.coordinator))
         {
            throw new RPCException("The coordinator did not change, we faced an unexpected situation",
               (MemberHasLeftException)result);
         }
         else
         {
            // The coordinator has changed, we will automatically retry with the new coordinator
            return executeCommandOnCoordinator(command, synchronous, timeout, args);
         }
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
   protected List<Object> excecuteCommand(final Vector<Address> dests, RemoteCommand command,
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
      String commandId = command.getId();
      if (commands.get(commandId) != command)
      {
         throw new RPCException("Command " + commandId + " unknown, please register your command first");
      }
      final Message msg = new Message();
      msg.setObject(new MessageBody(commandId, args));
      RspList rsps = AccessController.doPrivileged(new PrivilegedAction<RspList>()
      {
         public RspList run()
         {
            return dispatcher.castMessage(dests, msg, synchronous ? GroupRequest.GET_ALL : GroupRequest.GET_NONE,
               timeout);
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
         LOG.trace("(" + channel.getLocalAddress() + "): responses for command " + commandId + ":\n" + rsps);
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
         RemoteCommand command = getCommand(commandId = body.getCommandId());
         if (command == null)
         {
            return new RPCException("Command " + commandId + " unkown, please register your command first");
         }
         Object execResult = command.execute(body.getArgs());
         if (LOG.isTraceEnabled())
            LOG.trace("Command : " + commandId + " executed, result is: " + execResult);
         return execResult;
      }
      catch (Throwable x)
      {
         if (LOG.isTraceEnabled())
            LOG.trace("Problems invoking command.", x);
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
      this.members = view.getMembers();
      this.coordinator = members != null && members.size() > 0 ? members.get(0) : null;
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
         this.channel = new JChannel(configurator);
         channel.setOpt(Channel.AUTO_RECONNECT, true);
         this.dispatcher = new MessageDispatcher(channel, null, this, this);
         doPriviledgedExceptionAction(new PrivilegedExceptionAction<Void>()
         {
            public Void run() throws Exception
            {
               channel.connect(clusterName);
               return null;
            }
         });
      }
      catch (ChannelException e)
      {
         throw new RuntimeException("Cannot initialize the Channel needed for the RPCServiceImpl", e);
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
      if (channel != null && channel.isOpen())
      {
         if (LOG.isInfoEnabled())
            LOG.info("Disconnecting and closing the Channel");
         AccessController.doPrivileged(new PrivilegedAction<Void>()
         {
            public Void run()
            {
               channel.disconnect();
               return null;
            }
         });
         channel.close();
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
   public long getDefaultTimeout()
   {
      return defaultTimeout;
   }

   /**
    * Gives the name of the cluster
    * @return the name of the cluster
    */
   public String getClusterName()
   {
      return clusterName;
   }

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
    * Execute a privilege action
    */
   private static <E> E doPriviledgedExceptionAction(PrivilegedExceptionAction<E> action) throws ChannelException
   {
      try
      {
         return AccessController.doPrivileged(action);
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof ChannelException)
         {
            throw (ChannelException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }
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
       * The list of parameters;
       */
      private Serializable[] args;

      public MessageBody()
      {
      }

      public MessageBody(String commandId, Serializable[] args)
      {
         this.commandId = commandId;
         this.args = args;
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
       * {@inheritDoc}
       */
      public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
      {
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
   public enum State {
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
