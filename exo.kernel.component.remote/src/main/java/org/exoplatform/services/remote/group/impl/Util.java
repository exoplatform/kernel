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

import org.exoplatform.services.remote.group.MemberInfo;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.stack.IpAddress;

import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Mar 4, 2005
 * @version $Id: Util.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class Util
{
   static public Address findAddress(JChannel channel, MemberInfo info)
   {
      View view = channel.getView();
      List members = view.getMembers();
      String ip = info.getIpAddress();
      for (int i = 0; i < members.size(); i++)
      {
         Object member = members.get(i);
         if (member instanceof IpAddress)
         {
            IpAddress addr = (IpAddress)member;
            if (ip.equals(addr.getIpAddress().getHostAddress()))
               return addr;
         }
         else
         {
            if (info.getIpAddress().equals(member.toString()))
            {
               return (Address)member;
            }
         }
      }
      return null;
   }

   static public MemberInfo createMemberInfo(Object member)
   {
      MemberInfo info = new MemberInfo();
      if (member instanceof IpAddress)
      {
         IpAddress addr = (IpAddress)member;
         info.setHostName(addr.getIpAddress().getHostName());
         info.setIpAddress(addr.getIpAddress().getHostAddress());
         info.setPort(addr.getPort());
      }
      else
      {
         info.setHostName(member.toString());
         info.setIpAddress(member.toString());
      }
      return info;
   }
}
