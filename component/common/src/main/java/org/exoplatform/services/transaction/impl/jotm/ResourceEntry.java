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
package org.exoplatform.services.transaction.impl.jotm;

import org.exoplatform.services.transaction.ExoResource;
import org.objectweb.transaction.jta.ResourceManagerEvent;

import java.util.List;

import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAException;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ResourceEntry implements ResourceManagerEvent
{

   List<?> jotmResourceList;

   final ExoResource resource;

   public ResourceEntry(ExoResource resource)
   {
      this.resource = resource;
   }

   public void enlistConnection(Transaction transaction) throws SystemException
   {
      try
      {
         /*
               if (LOG.isDebugEnabled())
                 LOG.debug("Enlist connection. Session: " + getSessionInfo() + ", " + this
                     + ", transaction: " + transaction);
         */
         resource.enlistResource();
      }
      catch (IllegalStateException e)
      {
         throw new SystemException(e.getMessage());
      }
      catch (XAException e)
      {
         throw new SystemException(e.getMessage());
      }
   }
}
