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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
    * The id of the attribute in which it will store the objects
    */
   private static final String ATTRIBUTE_ID = RequestContext.class.getName();

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
       * The request in which we will store the content
       */
      private final ServletRequest request;

      public RequestContextStorage(ServletRequest request)
      {
         this.request = request;
      }

      /**
      * {@inheritDoc}
       */
      public String getId()
      {
         return "RequestContextStorage";
      }

      /**
       * Gives the map of objects that we currently have in the request
       * @param create indicates whether it should be created if it doesn't exist yet
       */
      private Map<String, Object> getObjects(boolean create)
      {
         @SuppressWarnings("unchecked")
         Map<String, Object> map = (Map<String, Object>)request.getAttribute(ATTRIBUTE_ID);
         if (map == null && create)
         {
            map = new HashMap<String, Object>();
            request.setAttribute(ATTRIBUTE_ID, map);
         }
         return map;
      }

      /**
       * {@inheritDoc}
       */
      public <T> T setInstance(String id, CreationContext<T> creationContext)
      {
         Map<String, Object> map = getObjects(true);
         @SuppressWarnings("unchecked")
         CreationContext<T> currentValue = (CreationContext<T>)map.get(id);
         if (currentValue != null && currentValue.getInstance() != null)
         {
            return currentValue.getInstance();
         }
         map.put(id, creationContext);
         return creationContext.getInstance();
      }

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings("unchecked")
      public <T> CreationContext<T> getCreationContext(String id)
      {
         Map<String, Object> map = getObjects(false);
         return map == null ? null : (CreationContext<T>)map.get(id);
      }

      /**
       * {@inheritDoc}
       */
      public void removeInstance(String id)
      {
         Map<String, Object> map = getObjects(false);
         if (map != null && map.remove(id) != null && map.isEmpty())
         {
            request.removeAttribute(ATTRIBUTE_ID);
         }
      }

      /**
       * {@inheritDoc}
       */
      public Set<String> getAllIds()
      {
         Map<String, Object> map = getObjects(false);
         return map == null ? Collections.<String>emptySet() : new HashSet<String>(map.keySet());
      }
   }
}
