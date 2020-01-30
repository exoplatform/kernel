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
package org.exoplatform.container.configuration;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.Configuration;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;

/**
 * The {@link ConfigurationManager} is the component allowing to access to the configuration of a given
 * eXo container. We have one instance of {@link ConfigurationManager} per eXo container.
 * All the url provided in String format can use the next prefixes:
 * <ul>
 * <li><i>war:</i> try to find the file using the Servlet Context of your portal.war or any web applications defined
 *  as PortalContainerConfigOwner, so for example in case of the portal.war if the URL is 
 *  war:/conf/common/portlet-container-configuration.xml it will try to get the file from 
 *  portal.war/WEB-INF/conf/common/portlet-container-configuration.xml.</li>
 * <li><i>jar or classpath:</i> you can use this prefix to find a file that is accessible using the ClassLoader. 
 * For example jar:/conf/my-file.xml will be understood as try to find conf/my-file.xml from 
 * the ClassLoader.</li>
 * <li><i>file:</i> this prefix will indicate the configuration manager that it needs to interpret the URL as 
 * an absolute path. For example file:///path/to/my/file.xml will be understood as an absolute path.</li>
 * <li><i>Without prefixes:</i> it will be understood as a relative path from the parent directory of the 
 * last processed configuration file. For example, if the configuration manager is processing the 
 * file corresponding to the URL file:///path/to/my/configuration.xml and in this file you import 
 * dir/to/foo.xml, the configuration manager will try to get the file from file:///path/to/my/dir/to/foo.xml. 
 * Please note that it works also for other prefixes</li>
 * </ul>
 * @author: Tuan Nguyen
 * @version: $Id: ConfigurationManager.java 5799 2006-05-28 17:55:42Z geaz $
 */
public interface ConfigurationManager
{
   /**
    * The name of the system property that indicates whether the logger of the configuration
    * must be in debug more or not.
    */
   public static final String LOG_DEBUG_PROPERTY = "org.exoplatform.container.configuration.debug";

   /**
    * Constant that indicates whether the logger of the configuration
    * must be in debug more or not.
    */
   public static final boolean LOG_DEBUG = PrivilegedSystemHelper.getProperty(LOG_DEBUG_PROPERTY) != null;

   /**
    * Gives the entire configuration
    */
   Configuration getConfiguration();

   /**
    * Gives the component configuration of a given service
    * @param service the FQN of the service for which we want the configuration
    */
   Component getComponent(String service);

   /**
    * Gives the component configuration of a given service
    * @param clazz the {@link Class} of the service for which we want the configuration
    */
   Component getComponent(Class<?> clazz);

   /**
    * Gives the configuration of all the registered components
    */
   Collection<Component> getComponents();

   /**
    * Adds a new location of a configuration file
    * @param url the url of the configuration to add, that we want to resolve
    * @throws Exception if the configuration could not be found
    * or the url in String format could not be resolved
    */
   void addConfiguration(String url) throws Exception;

   /**
    * Adds a collection {@link URL} corresponding to the location of the
    * configuration files to add 
    * @param urls the URLs of configuration files to add
    */
   void addConfiguration(Collection<URL> urls);

   /**
    * Adds a new location of a configuration file
    * @param url the url of the configuration to add
    */
   void addConfiguration(URL url);

   /**
    * Gives the {@link URL} of the resource file corresponding to the url provided in
    * String format
    * @param url the url to resolve
    * @param defaultURL The default URL to use in case the parameter <code>url</code> is null.
    * @return The {@link URL} representing the String url to resolve
    * @throws Exception if the String url could not be resolved
    */
   URL getResource(String url, String defaultURL) throws Exception;

   /**
    * Gives the {@link URL} of the resource file corresponding to the url provided in
    * String format
    * @param url the url to resolve
    * @return The {@link URL} representing the String url to resolve
    * @throws Exception if the String url could not be resolved
    */
   URL getResource(String url) throws Exception;

   /**
    * Gives the {@link InputStream} of the resource file corresponding to the url provided in
    * String format
    * @param url the url to resolve
    * @param defaultURL The default URL to use in case the parameter <code>url</code> is null.
    * @return The {@link InputStream} of the resource file
    * @throws Exception if the String url could not be resolved
    */
   InputStream getInputStream(String url, String defaultURL) throws Exception;

   /**
    * Gives the {@link InputStream} of the resource file corresponding to the url provided in
    * String format
    * @param url the url to resolve
    * @return The {@link InputStream} of the resource file
    * @throws Exception if the String url could not be resolved
    */
   InputStream getInputStream(String url) throws Exception;

   /**
    * This method is equivalent to {@link #getResource(String)}
    */
   URL getURL(String uri) throws Exception;

   /**
    * @return a {@link Set} of profiles used to parse configuration entries
    */
   default Set<String> getProfiles() {
     return ExoContainer.getProfilesFromProperty();
   }
}
