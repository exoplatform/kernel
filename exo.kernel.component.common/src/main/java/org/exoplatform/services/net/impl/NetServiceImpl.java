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
package org.exoplatform.services.net.impl;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.services.net.NetService;

import java.net.Socket;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;

/**
 * Created by The eXo Platform SAS Author : HoaPham phamvuxuanhoa@yahoo.com Jan
 * 10, 2006
 */
public class NetServiceImpl implements NetService
{

   public long ping(final String host, final int port) throws Exception
   {
      long startTime = 0;
      long endTime = 0;
      try
      {
         startTime = System.currentTimeMillis();
         Socket socket = SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Socket>()
         {
            public Socket run() throws Exception
            {
               return new Socket(host, port);
            }
         });
         endTime = System.currentTimeMillis();
      }
      catch (PrivilegedActionException e)
      {
         // e.printStackTrace() ;
         return -1;
      }
      return endTime - startTime;
   }
}
