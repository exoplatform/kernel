/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container.context;

import java.lang.annotation.Annotation;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.RequestScoped;
import javax.servlet.ServletRequest;

/**
 * The context that represents the request scope.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class RequestContext extends UnSharedContext<ServletRequest>
{

   /**
    * {@inheritDoc}
    */
   public Class<? extends Annotation> getScope()
   {
      return RequestScoped.class;
   }

   /**
    * {@inheritDoc}
    */
   protected CreationContextStorage createStorage(ServletRequest key)
   {
      return new RequestContextStorage(key);
   }

   private static final class RequestContextStorage implements CreationContextStorage
   {

      /**
       * The prefix of all the attributes stored into the request
       */
      private static final String PREFIX = RequestContextStorage.class.getPackage().getName();

      /**
       * The request in which we will store the content
       */
      private final ServletRequest request;

      public RequestContextStorage(ServletRequest request)
      {
         this.request = request;
      }

      private String getFullId(String id)
      {
         StringBuilder sb = new StringBuilder(PREFIX);
         sb.append('#');
         sb.append(id);
         return sb.toString();
      }

      /**
      * {@inheritDoc}
       */
      public String getId()
      {
         return "RequestContextStorage";
      }

      /**
       * {@inheritDoc}
       */
      public <T> T setInstance(String id, CreationContext<T> creationContext)
      {
         String fullId = getFullId(id);
         @SuppressWarnings("unchecked")
         CreationContext<T> currentValue = (CreationContext<T>)request.getAttribute(fullId);
         if (currentValue != null && currentValue.getInstance() != null)
         {
            return currentValue.getInstance();
         }
         request.setAttribute(fullId, creationContext);
         return creationContext.getInstance();
      }

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings("unchecked")
      public <T> CreationContext<T> getCreationContext(String id)
      {
         String fullId = getFullId(id);
         return (CreationContext<T>)request.getAttribute(fullId);
      }

      /**
       * {@inheritDoc}
       */
      public void removeInstance(String id)
      {
         String fullId = getFullId(id);
         request.removeAttribute(fullId);
      }

      /**
       * {@inheritDoc}
       */
      public Set<String> getAllIds()
      {
         Enumeration<String> enumeration = request.getAttributeNames();
         Set<String> ids = new HashSet<String>();
         while (enumeration.hasMoreElements())
         {
            String id = enumeration.nextElement();
            if (id.startsWith(PREFIX))
            {
               // Remove the prefix
               ids.add(id.substring(PREFIX.length() + 1));
            }
         }
         return ids;
      }
   }
}
