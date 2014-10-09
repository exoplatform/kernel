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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.SessionScoped;
import javax.servlet.http.HttpSession;

/**
 * The context that represents the {@link SessionScoped}. The key used is the
 * id of the session.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class SessionContext extends SharedContext<HttpSession>
{
   /**
    * The id of the attribute in which it will store the objects
    */
   private static final String ATTRIBUTE_ID = SessionContext.class.getName();

   /**
    * {@inheritDoc}
    */
   public Class<? extends Annotation> getScope()
   {
      return SessionScoped.class;
   }

   /**
    * {@inheritDoc}
    */
   protected CreationContextStorage createStorage(HttpSession key)
   {
      return new SessionContextStorage(key);
   }

   private static final class SessionContextStorage implements CreationContextStorage
   {

      /**
       * The session in which we will store the content
       */
      private final HttpSession session;

      public SessionContextStorage(HttpSession session)
      {
         this.session = session;
      }

      /**
       * {@inheritDoc}
       */
      public String getId()
      {
         return session.getId();
      }

      /**
       * Gives the map of objects that we currently have in the session
       * @param create indicates whether it should be created if it doesn't exist yet
       */
      @SuppressWarnings("unchecked")
      private Map<String, Object> getObjects(boolean create)
      {
         Map<String, Object> map = (Map<String, Object>)session.getAttribute(ATTRIBUTE_ID);
         if (map == null && create)
         {
            synchronized (this)
            {
               map = (Map<String, Object>)session.getAttribute(ATTRIBUTE_ID);
               if (map == null)
               {
                  map = new ConcurrentHashMap<String, Object>();
                  session.setAttribute(ATTRIBUTE_ID, map);
               }
            }
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
            synchronized (this)
            {
               if (map.isEmpty())
               {
                  session.removeAttribute(ATTRIBUTE_ID);
               }
            }
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
