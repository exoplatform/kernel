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
       * The prefix of all the attributes stored into the request
       */
      private static final String PREFIX = SessionContextStorage.class.getPackage().getName();

      /**
       * The session in which we will store the content
       */
      private final HttpSession session;

      public SessionContextStorage(HttpSession session)
      {
         this.session = session;
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
         return session.getId();
      }

      /**
       * {@inheritDoc}
       */
      public <T> T setInstance(String id, CreationContext<T> creationContext)
      {
         String fullId = getFullId(id);
         @SuppressWarnings("unchecked")
         CreationContext<T> currentValue = (CreationContext<T>)session.getAttribute(fullId);
         if (currentValue != null && currentValue.getInstance() != null)
         {
            return currentValue.getInstance();
         }
         session.setAttribute(fullId, creationContext);
         return creationContext.getInstance();
      }

      /**
       * {@inheritDoc}
       */
      @SuppressWarnings("unchecked")
      public <T> CreationContext<T> getCreationContext(String id)
      {
         String fullId = getFullId(id);
         return (CreationContext<T>)session.getAttribute(fullId);
      }

      /**
       * {@inheritDoc}
       */
      public void removeInstance(String id)
      {
         String fullId = getFullId(id);
         session.removeAttribute(fullId);
      }

      /**
       * {@inheritDoc}
       */
      public Set<String> getAllIds()
      {
         Enumeration<String> enumeration = session.getAttributeNames();
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
