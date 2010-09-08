package org.exoplatform.services.cache.impl.jboss;

import org.exoplatform.services.cache.ExoCacheConfig;
import org.jboss.cache.Cache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Region;
import org.jboss.cache.config.EvictionAlgorithmConfig;
import org.jboss.cache.config.EvictionRegionConfig;

import java.io.Serializable;

/**
 * This class is used to propose a set of common methods generally needed by {@link ExoCacheCreator}
 * implementations
 * 
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractExoCacheCreator implements ExoCacheCreator
{
   
   /**
    * Create a new region to the given cache.
    * @param config The ExoCacheConfig from which we get the name of the region.
    * @param cache the cache instance to which we want to add the new region
    * @param eac The Eviction Algorithm to use for the new region to create.
    * @return The root Fqn of the new created region
    */
   protected Fqn<String> addEvictionRegion(ExoCacheConfig config, Cache<Serializable, Object> cache,
      EvictionAlgorithmConfig eac)
   {
      Fqn<String> fqn = Fqn.fromElements(config.getName());
      // Create the region 
      Region region = cache.getRegion(fqn, true);
      // Set the eviction region config
      region.setEvictionRegionConfig(new EvictionRegionConfig(fqn, eac));
      return fqn;
   }

}
