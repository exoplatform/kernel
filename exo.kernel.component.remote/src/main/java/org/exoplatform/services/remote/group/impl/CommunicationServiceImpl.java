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
package org.exoplatform.services.remote.group.impl;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.remote.group.CommunicationService;
import org.exoplatform.services.remote.group.CommunicationServiceMonitor;
import org.exoplatform.services.remote.group.MemberInfo;
import org.exoplatform.services.remote.group.Message;
import org.exoplatform.services.remote.group.MessageHandler;
import org.exoplatform.services.remote.group.PingResult;
import org.exoplatform.services.remote.group.Response;
import org.exoplatform.services.remote.group.ResultHandler;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MessageDispatcher;
import org.jgroups.util.Rsp;
import org.jgroups.util.RspList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Feb 21, 2005
 * @version $Id: CommunicationServiceImpl.java 5799 2006-05-28 17:55:42Z geaz $
 */
@Deprecated
public class CommunicationServiceImpl implements CommunicationService
{
   private String properties_;

   private JChannel channel_;

   private MessageDispatcher mdispatcher_;

   private RequestHandlerImpl requestHandler_;

   private CommunicationChannelListener channelListener_ = new CommunicationChannelListener();

   private Log LOG = ExoLogger.getLogger("exo.kernel.component.remote.CommunicationServiceImpl");

   public CommunicationServiceImpl(InitParams confParams) throws Exception
   {
      ValuesParam param = confParams.getValuesParam("jgroups.channel.properties");
      List values = param.getValues();
      StringBuffer b = new StringBuffer();
      for (int i = 0; i < values.size(); i++)
      {
         b.append((String)values.get(i));
         if (i != values.size() - 1)
         {
            b.append(":");
         }
      }
      properties_ = b.toString();
      configure(properties_);
   }

   public void configure(String props) throws Exception
   {
      channel_ = new JChannel(props);
      requestHandler_ = new RequestHandlerImpl();
      mdispatcher_ = new MessageDispatcher(channel_, null, null, requestHandler_);
      channel_.setChannelListener(channelListener_);
      channel_.connect("Portal");
   }

   synchronized public void addPlugin(ComponentPlugin plugin)
   {
      MessageHandler handler = (MessageHandler)plugin;
      handler.init(this);
      requestHandler_.registerMessageHandler(handler);
   }

   public ComponentPlugin removePlugin(String name)
   {
      throw new RuntimeException("this method is not supported");
   }

   public Collection getPlugins()
   {
      throw new RuntimeException("");
   }

   public Message createMessage(String targetHandler)
   {
      MessageImpl m = new MessageImpl();
      m.setTargetHandler(targetHandler);
      return m;
   }

   synchronized public Object send(MemberInfo member, Message message) throws Exception
   {
      Address dest = Util.findAddress(channel_, member);
      if (dest == null)
         throw new Exception("Cannot find the member: " + member.getIpAddress());
      org.jgroups.Message jmessage = new org.jgroups.Message(dest, null, message);
      Object result = mdispatcher_.sendMessage(jmessage, GroupRequest.GET_ALL, 0);
      return result;
   }

   synchronized public void broadcast(Message message, boolean include) throws Exception
   {
      Vector members = null;
      if (!include)
         members = getMembersExcludeMe();
      org.jgroups.Message jmessage = new org.jgroups.Message(null, null, message);
      mdispatcher_.castMessage(members, jmessage, GroupRequest.GET_NONE, 0);
   }

   synchronized public void broadcast(Message message, ResultHandler handler, boolean include) throws Exception
   {
      Vector members = null;
      if (!include)
         members = getMembersExcludeMe();
      org.jgroups.Message jmessage = new org.jgroups.Message(null, null, message);
      RspList res = mdispatcher_.castMessage(members, jmessage, GroupRequest.GET_ALL, 0);
      Response response = new Response();
      List list = res.getResults();
      for (int i = 0; i < list.size(); i++)
      {
         Rsp rsp = (Rsp)list.get(i);
         response.populate(rsp);
         handler.handleResponse(response);
      }
   }

   synchronized public PingResult ping(MemberInfo member, String message) throws Exception
   {
      Address dest = Util.findAddress(channel_, member);
      if (dest == null)
         throw new Exception("Cannot find the member: " + member.getIpAddress());
      Message pingMesg = createMessage(PingMessageHandler.IDENTIFIER);
      pingMesg.setMessage(message);
      org.jgroups.Message jmessage = new org.jgroups.Message(dest, null, pingMesg);
      Object result = mdispatcher_.sendMessage(jmessage, GroupRequest.GET_ALL, 0);
      PingResult p = new PingResult(member, (String)result);
      return p;
   }

   synchronized public List pingAll(String message) throws Exception
   {
      Message pingMesg = createMessage(PingMessageHandler.IDENTIFIER);
      pingMesg.setMessage(message);
      org.jgroups.Message jmessage = new org.jgroups.Message(null, null, pingMesg);
      RspList res = mdispatcher_.castMessage(null, jmessage, GroupRequest.GET_ALL, 0);
      List results = new ArrayList();
      for (int i = 0; i < res.size(); i++)
      {
         Rsp rsp = (Rsp)res.elementAt(i);
         PingResult result = new PingResult(Util.createMemberInfo(rsp.getSender()), (String)rsp.getValue());
         results.add(result);
      }
      return results;
   }

   public CommunicationServiceMonitor getCommunicationServiceMonitor(MemberInfo info) throws Exception
   {
      if (info == null)
      {
         info = Util.createMemberInfo(channel_.getView().getCreator());
         CommunicationServiceMonitor monitor =
            new CommunicationServiceMonitor(requestHandler_.getMessageHandlerMonitors(), info);
         return monitor;
      }
      Message message = createMessage(GetCommunicationMonitorHandler.IDENTIFIER);
      CommunicationServiceMonitor monitor = (CommunicationServiceMonitor)send(info, message);
      return monitor;
   }

   public List getMembersInfo()
   {
      List list = new ArrayList();
      View view = channel_.getView();
      Vector members = view.getMembers();
      for (int i = 0; i < members.size(); i++)
      {
         Object member = members.get(i);
         list.add(Util.createMemberInfo(member));
      }
      return list;
   }

   public Vector getMembersExcludeMe()
   {
      View view = channel_.getView();
      Address addr = view.getCreator();
      Vector members = view.getMembers();
      for (int i = 0; i < members.size(); i++)
      {
         Object member = members.get(i);
         if (member.equals(addr))
         {
            members.remove(i);
            return members;
         }

      }
      return members;
   }

   public String getInfo()
   {
      StringBuffer b = new StringBuffer();
      List members = getMembersInfo();
      for (int i = 0; i < members.size(); i++)
      {
         MemberInfo addr = (MemberInfo)members.get(i);
         b.append("Name: " + addr.getHostName()).append("; Address " + addr.getIpAddress()).append(
            "; Port " + addr.getPort()).append("\n");
      }
      return b.toString();
   }
}
