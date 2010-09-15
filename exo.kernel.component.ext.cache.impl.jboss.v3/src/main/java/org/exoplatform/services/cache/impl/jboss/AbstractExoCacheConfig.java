package org.exoplatform.services.cache.impl.jboss;

import org.exoplatform.services.cache.ExoCacheConfig;

/**
 * The parent class of all the existing {@link ExoCacheConfig} available for the JBossCache
 * implementation of the eXo Cache
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public abstract class AbstractExoCacheConfig extends ExoCacheConfig
{

   /**
    * Indicate whether the JBossCache instance used for this cache can be shared
    */
   public Boolean allowShareableCache;

   public Boolean getAllowShareableCache()
   {
      return allowShareableCache;
   }

   public void setAllowShareableCache(Boolean allowShareableCache)
   {
      this.allowShareableCache = allowShareableCache;
   }
}
