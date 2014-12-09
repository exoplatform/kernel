/*
 * Copyright (C) 2014 eXo Platform SAS.
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
package org.exoplatform.services.cache.concurrent;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rpc.RPCService;
import org.exoplatform.services.rpc.RemoteCommand;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * <p>This implementation of {@link ExoCache} will behave exactly the same way as {@link ConcurrentFIFOExoCache}
 * except in case of a cache change, indeed the modifications will be first applied locally
 * then it will be replicated over the cluster asynchronously to limit the performance impact
 * on the local cluster node.</p>
 * <p>This class can be used as a drop-in replacement for {@link ConcurrentFIFOExoCache} in a cluster environment 
 * as long as we know that modifications like remove, clearCache, removeCachedObjects, put or putMap happen rarely. 
 * In other words, it should be used for caches that rarely change.</p>
 * 
 * <b>This limitation is due to the fact that the mechanism used for the replication
 * has not been designed to support heavy load so it must be used with a lot of caution.</b>
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class SimpleReplicatedExoCache<K extends Serializable, V extends Serializable> extends ConcurrentFIFOExoCache<K, V>
{
   /**
    * Logger.
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.cache.SimpleReplicatedExoCache");

   /**
    * Component used to execute commands over the cluster.
    */
   private final RPCService rpcService;

   /**
    * The generic command used to replicate changes over the cluster
    */
   private RemoteCommand command;

   /**
    * Id used to avoid launching twice the same command on the same node
    */
   private final String id;

   /**
    * The name of the current context
    */
   private final String ctxName;

   public SimpleReplicatedExoCache()
   {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      this.rpcService = container.getComponentInstanceOfType(RPCService.class);
      if (rpcService == null)
         throw new IllegalArgumentException("The RPCService is required for this type of cache, please configure it first");
      this.ctxName = container.getContext().getName();
      this.id = UUID.randomUUID().toString();
   }

   SimpleReplicatedExoCache(ExoContainerContext ctx, RPCService rpcService)
   {
      if (rpcService == null)
         throw new IllegalArgumentException("The RPCService is required for this type of cache, please configure it first");
      this.rpcService = rpcService;
      this.ctxName = ctx.getName();
      this.id = UUID.randomUUID().toString();
   }

   @Override
   public void setName(String s)
   {
      super.setName(s);
      if (command == null)
      {
         command = rpcService.registerCommand(new RemoteCommand()
         {
            private final String commandId = SimpleReplicatedExoCache.class.getName() + "-" + getName() + "-"
               + ctxName;

            public String getId()
            {
               return commandId;
            }

            @SuppressWarnings("unchecked")
            public Serializable execute(Serializable[] args) throws Throwable
            {
               if (!id.equals(args[0]))
               {
                  if ("c".equals(args[1]))
                  {
                     try
                     {
                        clearCacheOnly();
                     }
                     catch (Exception e)
                     {
                        LOG.warn("Could not clear the cache on other cluster nodes", e);
                     }
                  }
                  else if ("r".equals(args[1]))
                  {
                     try
                     {
                        removeOnly((Serializable)args[2]);
                     }
                     catch (Exception e)
                     {
                        LOG.warn("Could not remove the entry " + args[2] + " on other cluster nodes", e);
                     }
                  }
                  else if ("p".equals(args[1]))
                  {
                     try
                     {
                        putIfNeeded((K)args[2], (V)args[3]);
                     }
                     catch (Exception e)
                     {
                        LOG.warn("Could not put the entry " + args[2] + " on other cluster nodes", e);
                     }
                  }
                  else if ("m".equals(args[1]))
                  {
                     try
                     {
                        Map<? extends K, ? extends V> objs = (Map<? extends K, ? extends V>)args[2];
                        for (Entry<? extends K, ? extends V> entry : objs.entrySet())
                        {
                           putIfNeeded(entry.getKey(), entry.getValue());
                        }
                     }
                     catch (Exception e)
                     {
                        LOG.warn("Could not put entries on other cluster nodes", e);
                     }
                  }
               }
               return true;
            }
         });
      }
   }

   /**
    * Removes the entry without replication
    * @param name the key of the entry to remove
    */
   void removeOnly(Serializable name)
   {
      super.remove(name);
   }

   /**
    * Clears the cache without replication
    */
   void clearCacheOnly()
   {
      super.clearCache();
   }

   /**
    * Puts the entry without replication only if the current cache
    * doesn't have already the exact same entry with the same key and value
    * @param name the key of the entry to put
    * @param obj the value of the entry to put
    */
   void putIfNeeded(K name, V obj)
   {
      V currrentValue = get(name);
      if (currrentValue == null || !currrentValue.equals(obj))
         super.put(name, obj);
   }

   @Override
   public V remove(Serializable name)
   {
      V v = super.remove(name);
      if (v != null)
      {
         try
         {
            rpcService.executeCommandOnAllNodes(command, false, id, "r", name);
         }
         catch (Exception e)
         {
            LOG.warn("Could not remove the entry " + name + " on other cluster nodes", e);
         }
      }
      return v;
   }

   @Override
   public void clearCache()
   {
      super.clearCache();
      try
      {
         rpcService.executeCommandOnAllNodes(command, false, id, "c");
      }
      catch (Exception e)
      {
         LOG.warn("Could not clear the cache on other cluster nodes", e);
      }
   }

   @Override
   public void put(K name, V obj)
   {
      super.put(name, obj);
      try
      {
         rpcService.executeCommandOnAllNodes(command, false, id, "p", name, obj);
      }
      catch (Exception e)
      {
         LOG.warn("Could not put the entry " + name + " on other cluster nodes", e);
      }
   }

   
   @Override
   public void putMap(Map<? extends K, ? extends V> objs)
   {
      super.putMap(objs);
      try
      {
         rpcService.executeCommandOnAllNodes(command, false, id, "m", new HashMap<K, V>(objs));
      }
      catch (Exception e)
      {
         LOG.warn("Could not put entries on other cluster nodes", e);
      }
   }

   @Override
   protected void finalize() throws Throwable
   {
      try
      {
         if (command != null)
         {
            rpcService.unregisterCommand(command);
         }
      }
      finally
      {
         super.finalize();
      }
   }
}
