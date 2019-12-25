package org.exoplatform.container;

import java.util.ServiceLoader;
import java.util.Set;

/**
 * An interface used to define services that can be loaded using
 * {@link ServiceLoader} to inject profiles to eXo Kernel containers
 */
public interface ExoProfileExtension {

  /**
   * @return list of profiles to inject to containers
   */
  Set<String> getProfiles();

}
