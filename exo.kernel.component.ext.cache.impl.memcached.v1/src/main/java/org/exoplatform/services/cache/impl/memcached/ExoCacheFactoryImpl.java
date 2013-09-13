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
package org.exoplatform.services.cache.impl.memcached;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ObjectParameter;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.cache.ExoCache;
import org.exoplatform.services.cache.ExoCacheConfig;
import org.exoplatform.services.cache.ExoCacheFactory;
import org.exoplatform.services.cache.ExoCacheInitException;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.picocontainer.Startable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.PrivilegedExceptionAction;
import java.util.List;

/**
 * This class is the Memcached implementation of the {@link org.exoplatform.services.cache.ExoCacheFactory}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ExoCacheFactoryImpl implements ExoCacheFactory, Startable
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger
      .getLogger("exo.kernel.component.ext.cache.impl.memcached.v1.ExoCacheFactoryImpl");

   /**
    * The parameter key that defines the {@link ConnectionFactoryCreator}
    */
   public static final String CONNECTION_FACTORY_CREATOR = "connection.factory.creator";

   /**
    * The parameter key that defines the memcache locations
    */
   public static final String MEMCACHED_LOCATIONS = "memcached.locations";

   /**
    * The parameter key that defines the default expiration timeout
    */
   public static final String DEFAULT_EXPIRATION_TIMEOUT = "default.expiration.timeout";

   /**
    * The default expiration timeout, set to 15 minutes
    */
   public static final long DEFAULT_EXPIRATION_TIMEOUT_VALUE = 15 * 60 * 1000L;

   /**
    * The current {@link ExoContainerContext}
    */
   private final ExoContainerContext ctx;

   /**
    * The memcached client
    */
   private final MemcachedClient cache;

   /**
    * The cache creator
    */
   private final MCExoCacheCreator cacheCreator;

   /**
    * The default constructor
    */
   public ExoCacheFactoryImpl(ExoContainerContext ctx, final InitParams params) throws IOException
   {
      this.ctx = ctx;
      ValueParam locations;
      if (params == null || (locations = params.getValueParam(MEMCACHED_LOCATIONS)) == null
         || locations.getValue() == null || locations.getValue().isEmpty())
      {
         throw new IllegalArgumentException("The parameter '" + MEMCACHED_LOCATIONS + "' cannot be null or empty");
      }
      final List<InetSocketAddress> isaLocations = AddrUtil.getAddresses(locations.getValue());
      this.cache = SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<MemcachedClient>()
      {
         public MemcachedClient run() throws IOException
         {
            ObjectParameter op = params.getObjectParam(CONNECTION_FACTORY_CREATOR);
            if (op == null || op.getObject() == null)
            {
               LOG.debug("No connection factory creator has been defined, "
                  + "so we will use the BinaryConnectionFactory by default");
               return new MemcachedClient(new BinaryConnectionFactory(), isaLocations);
            }
            else if (!(op.getObject() instanceof ConnectionFactoryCreator))
            {
               throw new IllegalArgumentException("The parameter '" + CONNECTION_FACTORY_CREATOR
                  + "' must refer to a ConnectionFactoryCreator.");
            }
            else
            {
               return new MemcachedClient(((ConnectionFactoryCreator)op.getObject()).create(), isaLocations);
            }
         }
      });

      ValueParam vp = params.getValueParam(DEFAULT_EXPIRATION_TIMEOUT);
      if (vp == null || vp.getValue() == null || vp.getValue().isEmpty())
      {
         LOG.debug("No default expiration timeout has been defined");
         this.cacheCreator = new MCExoCacheCreator(DEFAULT_EXPIRATION_TIMEOUT_VALUE);
      }
      else
      {
         this.cacheCreator = new MCExoCacheCreator(Long.parseLong(vp.getValue()));
      }
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   public ExoCache createCache(ExoCacheConfig config) throws ExoCacheInitException
   {
      return cacheCreator.create(ctx, config, cache);
   }

   /**
    * {@inheritDoc}
    */
   public void start()
   {
   }

   /**
    * {@inheritDoc}
    */
   public void stop()
   {
      cache.shutdown();
   }
}
