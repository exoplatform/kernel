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

import java.io.Serializable;
import java.util.List;

/**
 * This service provides mechanism to communicate with the other cluster nodes. This service will
 * be based of JGroups as an underlying Transport.
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public interface RPCService
{

   /**
    * The permission needed to access to any methods of the RPCService
    */
   public static final RuntimePermission ACCESS_RPC_SERVICE_PERMISSION = new RuntimePermission("accessRPCService");

   /**
    * Executes a command on all the cluster nodes. This method is equivalent to the other method of the
    * same type but with the default timeout. The command must be registered first otherwise an 
    * {@link RPCException} will be thrown.
    *
    * @param command The command to execute on each cluster node
    * @param synchronous if true, sets group request mode to {@link org.jgroups.blocks.GroupRequest#GET_ALL},
    *  and if false sets it to {@link org.jgroups.blocks.GroupRequest#GET_NONE}.
    * @param args an array of {@link Serializable} objects corresponding to parameters of the command 
    * to execute remotely
    * @return a list of responses from all the members of the cluster. If we met an exception on a given node, 
    * the RPCException will be the corresponding response of this particular node
    * @throws RPCException in the event of problems.
    * @throws SecurityException if the {@link SecurityManager} is installed and the call method
    * doesn't have the {@link RuntimePermission} <code>ACCESS_RPC_SERVICE_PERMISSION</code>
    */
   List<Object> executeCommandOnAllNodes(RemoteCommand command, boolean synchronous, Serializable... args)
      throws RPCException, SecurityException;

   /**
    * Executes a command synchronously on all the cluster nodes. The command must be registered first otherwise an 
    * {@link RPCException} will be thrown.
    *
    * @param command The command to execute on each cluster node
    * @param timeout a timeout after which to throw a replication exception.
    * @param args an array of {@link Serializable} objects corresponding to parameters of the command 
    * to execute remotely
    * @return a list of responses from all the members of the cluster. If we met an exception on a given node, 
    * the RPCException will be the corresponding response of this particular node
    * @throws RPCException in the event of problems.
    * @throws SecurityException if the {@link SecurityManager} is installed and the call method
    * doesn't have the {@link RuntimePermission} <code>ACCESS_RPC_SERVICE_PERMISSION</code>
    */
   List<Object> executeCommandOnAllNodes(RemoteCommand command, long timeout, Serializable... args)
      throws RPCException, SecurityException;

   /**
    * Executes a command on the coordinator only. This method is equivalent to the other method of the
    * same type but with the default timeout. The command must be registered first otherwise an 
    * {@link RPCException} will be thrown.
    *
    * @param command The command to execute on the coordinator node
    * @param synchronous if true, sets group request mode to {@link org.jgroups.blocks.GroupRequest#GET_ALL}, 
    * and if false sets it to {@link org.jgroups.blocks.GroupRequest#GET_NONE}.
    * @param args an array of {@link Serializable} objects corresponding to parameters of the command 
    * to execute remotely
    * @return the response of the coordinator.
    * @throws RPCException in the event of problems.
    * @throws SecurityException if the {@link SecurityManager} is installed and the call method
    * doesn't have the {@link RuntimePermission} <code>ACCESS_RPC_SERVICE_PERMISSION</code>
    */
   Object executeCommandOnCoordinator(RemoteCommand command, boolean synchronous, Serializable... args)
      throws RPCException, SecurityException;

   /**
    * Executes a command synchronously on the coordinator only. The command must be registered first otherwise an 
    * {@link RPCException} will be thrown.
    *
    * @param command The command to execute on the coordinator node
    * @param timeout a timeout after which to throw a replication exception.
    * @param args an array of {@link Serializable} objects corresponding to parameters of the command 
    * to execute remotely
    * @return the response of the coordinator.
    * @throws RPCException in the event of problems.
    * @throws SecurityException if the {@link SecurityManager} is installed and the call method
    * doesn't have the {@link RuntimePermission} <code>ACCESS_RPC_SERVICE_PERMISSION</code>
    */
   Object executeCommandOnCoordinator(RemoteCommand command, long timeout, Serializable... args) throws RPCException,
      SecurityException;

   /**
    * Register a new {@link RemoteCommand} instance, it will be mapped to its id. If a command with the
    * same Id has already been registered, a warning will be printed into the log file and the new
    * command will replace the old one.
    * @param command the instance of the {@link RemoteCommand} to register
    * @return the command itself if it could be registered null otherwise 
    * @throws SecurityException if the {@link SecurityManager} is installed and the call method
    * doesn't have the {@link RuntimePermission} <code>ACCESS_RPC_SERVICE_PERMISSION</code>
    */
   RemoteCommand registerCommand(RemoteCommand command) throws SecurityException;

   /**
    * Unregister a {@link RemoteCommand} instance, if the id is known or the instance itself is known
    * otherwise it will be ignored
    * @param command the command to unregister
    * @throws SecurityException if the {@link SecurityManager} is installed and the call method
    * doesn't have the {@link RuntimePermission} <code>ACCESS_RPC_SERVICE_PERMISSION</code>
    */
   void unregisterCommand(RemoteCommand command) throws SecurityException;
   
   /**
    * Indicates whether the local node is the coordinator of the cluster
    * @return <code>true</code> if the coordinator is the coordinator, <code>false</code> otherwise
    * throws RPCException in case the {@link RPCService} is in an illegal state
    */
   boolean isCoordinator() throws RPCException;   
}