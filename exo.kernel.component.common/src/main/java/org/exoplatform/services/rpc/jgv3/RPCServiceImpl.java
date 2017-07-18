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
package org.exoplatform.services.rpc.jgv3;

import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.rpc.impl.AbstractRPCService;
import org.jgroups.*;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.ResponseMode;
import org.jgroups.stack.IpAddress;
import org.jgroups.util.RspList;

import java.util.List;

/**
 * This class is the implementation of the {@link AbstractRPCService} for JGroups 3.
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public class RPCServiceImpl extends AbstractRPCService
{

   /**
    * {@inheritDoc}
    */
   public RPCServiceImpl(ExoContainerContext ctx, InitParams params, ConfigurationManager configManager)
   {
      super(ctx, params, configManager);
   }

   /**
    * {@inheritDoc}
    */
   protected Address getLocalAddress()
   {
      return channel.getAddress();
   }

   /**
    * {@inheritDoc}
    */
   public String getHostAddress()
   {
      String address = null;
      org.jgroups.Address jgAddress = channel.getAddress() ;
      if (!(jgAddress instanceof IpAddress))
      {
         // this is the only way of getting physical address.
         jgAddress = (org.jgroups.Address)channel.down(new Event(Event.GET_PHYSICAL_ADDRESS, jgAddress));
      }
      if (jgAddress instanceof IpAddress)
      {
         address = ((IpAddress)jgAddress).getIpAddress().getHostAddress();
      }
      else
      {
         LOG.error("Unsupported Address object : " + jgAddress.getClass().getName());
      }
      return address;
   }
   
   /**
    * {@inheritDoc}
    */
   protected RspList<Object> castMessage(List<Address> dests, Message msg, boolean synchronous, long timeout) throws Exception
   {
      return dispatcher.castMessage(dests, msg, new RequestOptions(synchronous ? ResponseMode.GET_ALL
         : ResponseMode.GET_NONE, timeout));
   }
   
   /**
    * {@inheritDoc}
    */
   protected Channel createChannel() throws Exception
   {
      return new JChannel(configurator);
   }

   /**
    * {@inheritDoc}
    */
   public void unblock()
   {
   }

   /**
    * {@inheritDoc}
    */
   protected List<Address> getMembers(View view)
   {
      return view.getMembers();
   }

   /**
    * {@inheritDoc}
    */
   protected void setObject(Message m, Object o)
   {
      m.setObject(o);
   }
}
