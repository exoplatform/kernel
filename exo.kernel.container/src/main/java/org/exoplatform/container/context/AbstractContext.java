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

import org.exoplatform.container.component.ThreadContext;
import org.exoplatform.container.component.ThreadContextHolder;

import java.util.Set;
import java.util.concurrent.locks.Lock;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.spi.Contextual;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.PassivationCapable;

/**
 * This is the root class of all the implementations of an {@link AdvancedContext}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractContext<K> implements AdvancedContext<K>, ThreadContextHolder
{

   /**
    * The thread local in which we keep the current storage
    */
   private final ThreadLocal<CreationContextStorage> storage = new ThreadLocal<CreationContextStorage>();

   /**
    * {@inheritDoc}
    */
   public <T> T get(Contextual<T> contextual, CreationalContext<T> creationalContext)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      String id = getId(contextual);
      CreationContextStorage storage = getStorage();
      T result = getInstance(storage, id);
      if (result == null)
      {
         if (creationalContext == null)
            return null;
         if (isSharable())
         {
            Lock lock = getLock(id);
            try
            {
               lock.lock();
               result = getInstance(storage, id);
               if (result == null)
               {
                  result =
                     storage.setInstance(id,
                        new CreationContext<T>(contextual, creationalContext, contextual.create(creationalContext)));
               }
            }
            finally
            {
               lock.unlock();
            }
         }
         else
         {
            result =
               storage.setInstance(id,
                  new CreationContext<T>(contextual, creationalContext, contextual.create(creationalContext)));
         }
      }
      return result;
   }

   /**
    * {@inheritDoc}
    */
   public <T> T get(Contextual<T> contextual)
   {
      return get(contextual, null);
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings({"rawtypes", "unchecked"})
   public void destroy(Contextual contextual)
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      String id = getId(contextual);
      CreationContextStorage storage = getStorage();
      CreationContext creationContext = storage.getCreationContext(id);
      if (creationContext != null)
      {
         if (creationContext.getInstance() != null && creationContext.getCreationalContext() != null)
         {
            contextual.destroy(creationContext.getInstance(), creationContext.getCreationalContext());
         }
         storage.removeInstance(id);
      }
   }

   /**
    * {@inheritDoc}
    */
   public boolean isActive()
   {
      return storage.get() != null;
   }

   /**
    * {@inheritDoc}
    */
   public void activate(K key)
   {
      setStorage(createStorage(key));
   }

   /**
    * {@inheritDoc}
    */
   public void deactivate(K key)
   {
      setStorage(null);
   }

   /**
    * {@inheritDoc}
    */
   public void register(K key)
   {
   }

   /**
    * {@inheritDoc}
    */
   public void unregister(K key)
   {
      destroy(createStorage(key));
   }

   /**
    * Gives an id for the given {@link Contextual}
    * @param contextual the contextual object for which we want an id
    */
   protected <T> String getId(Contextual<T> contextual)
   {
      if (contextual instanceof PassivationCapable)
      {
         return ((PassivationCapable)contextual).getId();
      }
      throw new UnsupportedOperationException("Only contextuals that implement the "
         + "PassivationCapable interface are supported");
   }

   /**
    * Indicates whether the objects of the context that can shared or not. sharable 
    * @return <code>true</code> if the components are sharable, <code>false</code> otherwise.
    */
   protected abstract boolean isSharable();

   /**
    * In case the context is sharable, we will need a lock to synchronize the accesses 
    * @param id the id of the contextual for which we want a lock
    * @return a lock corresponding to the given contextual within the current context
    */
   protected abstract Lock getLock(String id);

   /**
    * Creates a {@link CreationContextStorage} instance from the given key
    * @param key the key to use to create the {@link CreationContextStorage}
    * @return the {@link CreationContextStorage} corresponding to the given key
    */
   protected abstract CreationContextStorage createStorage(K key);

   /**
    * Gives the storage to use to store and access to the CreationContext
    * @return the {@link CreationContextStorage} corresponding to the current context
    */
   protected CreationContextStorage getStorage()
   {
      return storage.get();
   }

   /**
    * Sets the current storage
    * @param storage the new current storage. Set it to <code>null</code> to remove the current storage
    * @throws IllegalStateException in case we try to set a storage and a storage has already been set
    */
   protected void setStorage(CreationContextStorage storage) throws IllegalStateException
   {
      if (storage == null)
      {
         this.storage.remove();
      }
      else
      {
         CreationContextStorage currentSorage = this.storage.get();
         if (currentSorage != null)
         {
            throw new IllegalStateException("A storage has already been defined "
               + "for the context whose corresponding annotation is " + getScope().getName());
         }
         this.storage.set(storage);
      }
   }

   /**
    * Destroys all the {@link CreationContext} that has been stored in the current storage
    */
   protected void destroy()
   {
      if (!isActive())
      {
         throw new ContextNotActiveException();
      }
      CreationContextStorage storage = getStorage();
      destroy(storage);
   }

   /**
    * Destroys all the {@link CreationContext} that has been stored in the given storage
    * @param storage the storage that we would like to cleanup
    */
   @SuppressWarnings("unchecked")
   protected void destroy(CreationContextStorage storage)
   {
      Set<String> ids = storage.getAllIds();
      if (ids != null)
      {
         for (String id : ids)
         {
            @SuppressWarnings("rawtypes")
            CreationContext creationContext = storage.getCreationContext(id);
            if (creationContext != null)
            {
               if (creationContext.getContextual() != null && creationContext.getInstance() != null
                  && creationContext.getCreationalContext() != null)
               {
                  creationContext.getContextual().destroy(creationContext.getInstance(),
                     creationContext.getCreationalContext());
               }
               storage.removeInstance(id);
            }
         }
      }
   }

   /**
    * Gives the instance stored with the given {@link Contextual} id.
    * @param storage the storage from which we will get the instance
    * @param id the id of the contextual for which we want the instance
    * @return the corresponding instance if it exists, <code>null</code> otherwise
    */
   protected <T> T getInstance(CreationContextStorage storage, String id)
   {
      CreationContext<T> creationContext = storage.getCreationContext(id);
      return creationContext == null ? null : creationContext.getInstance();
   }

   /**
    * {@inheritDoc}
    */
   public ThreadContext getThreadContext()
   {
      return new ThreadContext(storage);
   }
}
