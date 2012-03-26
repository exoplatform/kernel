/*
 * Copyright (C) 2011 eXo Platform SAS.
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
package org.exoplatform.services.ispn;

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.util.TemplateConfigurationHelper;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.transaction.TransactionService;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.global.GlobalConfigurationBuilder;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.Parser;
import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.transaction.lookup.TransactionManagerLookup;
import org.picocontainer.Startable;

import java.security.PrivilegedExceptionAction;
import java.util.Map;
import java.util.Map.Entry;

import javax.transaction.TransactionManager;

/**
 * This class is used to allow to use infinispan in distribution mode with
 * the ability to launch infinispan instances in standalone mode, in other
 * words outside an application server. To make it possible we will need to share
 * the same cache instance whatever the related {@link ExoContainer} because
 * to be able to launch ispn instances in standalone mode we need to have a static
 * configuration file.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class DistributedCacheManager implements Startable
{
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger //NOSONAR
      .getLogger("exo.kernel.component.ext.cache.impl.infinispan.v5.DistributedCacheManager");//NOSONAR

   /**
    * The parameter name corresponding to the infinispan configuration
    */
   private static final String CONFIG_FILE_PARAMETER_NAME = "infinispan-configuration";

   /**
    * The parameter name corresponding to the parameters to inject 
    * into the infinispan configuration file
    */
   private static final String PARAMS_PARAMETER_NAME = "parameters";

   /**
    * The infinispan cache manager
    */
   protected final EmbeddedCacheManager manager;

   /**
    * Default constructor
    */
   public DistributedCacheManager(String configurationFile, Map<String, String> parameters,
      ConfigurationManager configManager)
   {
      this.manager = init(configurationFile, parameters, configManager, null);
   }

   /**
    * Default constructor
    */
   public DistributedCacheManager(InitParams params, ConfigurationManager configManager)
   {
      this(params, configManager, null);
   }

   /**
    * Default constructor
    */
   public DistributedCacheManager(InitParams params, ConfigurationManager configManager, TransactionService ts)
   {
      ValueParam vp;
      final String result;
      if (params != null && (vp = params.getValueParam(CONFIG_FILE_PARAMETER_NAME)) != null
         && (result = vp.getValue()) != null && !result.isEmpty())
      {
         PropertiesParam pp = params.getPropertiesParam(PARAMS_PARAMETER_NAME);
         this.manager =
            init(result, pp == null ? null : pp.getProperties(), configManager,
               ts == null ? null : ts.getTransactionManager());
      }
      else
      {
         throw new IllegalArgumentException("The parameter '" + CONFIG_FILE_PARAMETER_NAME + "' must be set");
      }
   }

   /**
    * Initializes and created the CacheManager
    * @param configurationFile the path of the configuration file
    * @param parameters the parameters to inject into the configuration file
    * @param configManager the configuration manager used to get the configuration file
    * @param tm the transaction manager
    * @return the CacheManager initialized
    */
   private EmbeddedCacheManager init(final String configurationFile, final Map<String, String> parameters,
      final ConfigurationManager configManager, final TransactionManager tm)
   {
      try
      {
         if (configurationFile == null || configurationFile.isEmpty())
         {
            throw new IllegalArgumentException("The parameter 'configurationFile' must be set");
         }
         if (LOG.isDebugEnabled())
         {
            LOG.debug("The configuration file of the DistributedCacheManager will be loaded from " + configurationFile);
         }
         final TemplateConfigurationHelper helper =
            new TemplateConfigurationHelper(new String[]{"^.*"}, new String[]{}, configManager);
         if (LOG.isDebugEnabled() && parameters != null && !parameters.isEmpty())
         {
            LOG.debug("The parameters to use while processing the configuration file are " + parameters);
         }
         return SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<EmbeddedCacheManager>()
         {
            public EmbeddedCacheManager run() throws Exception
            {
               Parser parser = new Parser(Thread.currentThread().getContextClassLoader());
               // Load the configuration
               ConfigurationBuilderHolder holder = parser.parse(helper.fillTemplate(configurationFile, parameters));
               GlobalConfigurationBuilder configBuilder = holder.getGlobalConfigurationBuilder();
               Utils.loadJGroupsConfig(configManager, configBuilder.build(), configBuilder);
               // Create the CacheManager from the new configuration
               EmbeddedCacheManager manager =
                  new DefaultCacheManager(configBuilder.build(), holder.getDefaultConfigurationBuilder().build());
               TransactionManagerLookup tml = new TransactionManagerLookup()
               {
                  public TransactionManager getTransactionManager() throws Exception
                  {
                     return tm;
                  }
               };
               for (Entry<String, ConfigurationBuilder> entry : holder.getNamedConfigurationBuilders().entrySet())
               {
                  ConfigurationBuilder b = entry.getValue();
                  if (tm != null)
                  {
                     b.transaction().transactionManagerLookup(tml);
                  }
                  manager.defineConfiguration(entry.getKey(), b.build());
                  manager.getCache(entry.getKey());
               }
               return manager;
            }
         });
      }
      catch (Exception e)//NOSONAR
      {
         throw new IllegalStateException(
            "Could not initialize the cache manager corresponding to the configuration file " + configurationFile, e);
      }
   }

   /**
    * Gives the cache corresponding to the given name if it doesn't exist
    * a {@link NullPointerException} will be thrown
    */
   public <K, V> Cache<K, V> getCache(String cacheName)
   {
      Cache<K, V> cache = manager.getCache(cacheName, false);
      if (cache == null)
      {
         throw new IllegalArgumentException("The expected cache named '" + cacheName
            + "' has not been defined in the configuration of infinispan as named cache.");
      }
      return cache;
   }

   /**
    * @see org.picocontainer.Startable#start()
    */
   @Override
   public void start()
   {
   }

   /**
    * @see org.picocontainer.Startable#stop()
    */
   @Override
   public void stop()
   {
      manager.stop();
   }
}
