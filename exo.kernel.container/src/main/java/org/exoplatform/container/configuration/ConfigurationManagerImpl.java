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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ConfigurationServiceImpl.java,v 1.8 2004/10/30 02:29:51 tuan08
 *           Exp $
 */
public class ConfigurationManagerImpl implements ConfigurationManager
{
   final static public String WAR_CONF_LOCATION = "/WEB-INF";

   final static public String LOG_DEBUG_PROPERTY = "org.exoplatform.container.configuration.debug";

   final static public boolean LOG_DEBUG = System.getProperty(LOG_DEBUG_PROPERTY) != null;

   private static final String EXO_CONTAINER_PROP_NAME = "container.name.suffix";

   private static final Log log = ExoLogger.getLogger(ConfigurationManagerImpl.class);

   protected Configuration configurations_;

   private ServletContext scontext_;

   private ClassLoader scontextClassLoader_;

   private String contextPath = null;

   private boolean validateSchema = true;

   /** . */
   private final Set<String> profiles;

   /** The URL of the current document being unmarshalled. */
   private static final ThreadLocal<URL> currentURL = new ThreadLocal<URL>();

   /**
    * Returns the URL of the current document being unmarshalled or null.
    * @return the URL
    */
   public static URL getCurrentURL()
   {
      return currentURL.get();
   }

   public ConfigurationManagerImpl()
   {
      this.profiles = Collections.emptySet();
   }

   public ConfigurationManagerImpl(Set<String> profiles)
   {
      this.profiles = profiles;
   }

   public ConfigurationManagerImpl(ServletContext context, Set<String> profiles)
   {
      scontext_ = context;
      this.profiles = profiles;
   }

   public ConfigurationManagerImpl(ClassLoader loader, Set<String> profiles)
   {
      scontextClassLoader_ = loader;
      this.profiles = profiles;
   }

   public Configuration getConfiguration()
   {
      return configurations_;
   }

   public void addConfiguration(ServletContext context, String url) throws Exception
   {
      if (url == null)
         return;
      addConfiguration(context, getURL(context, url));
   }

   public void addConfiguration(String url) throws Exception
   {
      if (url == null)
         return;
      addConfiguration(getURL(url));
   }

   public void addConfiguration(Collection urls) throws Exception
   {
      Iterator i = urls.iterator();
      while (i.hasNext())
      {
         URL url = (URL)i.next();
         addConfiguration(url);
      }
   }

   public void addConfiguration(URL url) throws Exception
   {
      addConfiguration(scontext_, url);
   }

   private void addConfiguration(ServletContext context, URL url) throws Exception
   {
      if (LOG_DEBUG)
         log.info("Add configuration " + url);
      if (url == null)
         return;
      try
      {
         contextPath = (new File(url.toString())).getParent() + "/";
         contextPath = contextPath.replaceAll("\\\\", "/");
      }
      catch (Exception e)
      {
         contextPath = null;
      }

      // Just to prevent some nasty bug to happen
      if (currentURL.get() != null)
      {
         throw new IllegalStateException("Would not expect that");
      }
      else
      {
         currentURL.set(url);
      }

      //
      try
      {
         ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller(profiles);
         Configuration conf = unmarshaller.unmarshall(url);

         if (configurations_ == null)
            configurations_ = conf;
         else
            configurations_.mergeConfiguration(conf);
         List urls = conf.getImports();
         if (urls != null)
         {
            for (int i = 0; i < urls.size(); i++)
            {
               String uri = (String)urls.get(i);
               URL urlObject = getURL(uri);
               if (urlObject != null)
               {
                  conf = unmarshaller.unmarshall(urlObject);
                  configurations_.mergeConfiguration(conf);
                  if (LOG_DEBUG)
                     log.info("\timport " + urlObject);
               }
               else
               {
                  log.warn("Couldn't process the URL for " + uri + " configuration file ignored ");
               }
            }
         }
      }
      catch (Exception ex)
      {
         log.error("Cannot process the configuration " + url, ex);
      }
      finally
      {
         currentURL.set(null);
      }
   }

   public void processRemoveConfiguration()
   {
      if (configurations_ == null)
         return;
      List list = configurations_.getRemoveConfiguration();
      if (list != null)
      {
         for (int i = 0; i < list.size(); i++)
         {
            String type = (String)list.get(i);
            configurations_.removeConfiguration(type);
         }
      }
   }

   public Component getComponent(String service)
   {
      return configurations_.getComponent(service);
   }

   public Component getComponent(Class clazz) throws Exception
   {
      return configurations_.getComponent(clazz.getName());
   }

   public Collection getComponents()
   {
      if (configurations_ == null)
         return null;
      return configurations_.getComponents();
   }

   public boolean isValidateSchema()
   {
      return validateSchema;
   }

   public void setValidateSchema(boolean validateSchema)
   {
      this.validateSchema = validateSchema;
   }

   public URL getResource(String url, String defaultURL) throws Exception
   {
      return null;
   }

   public URL getResource(String uri) throws Exception
   {
      return getURL(uri);
   }

   public InputStream getInputStream(String url, String defaultURL) throws Exception
   {
      if (url == null)
         url = defaultURL;
      return getInputStream(url);
   }

   public InputStream getInputStream(String uri) throws Exception
   {
      URL url = getURL(uri);
      if (url == null)
      {
         throw new IOException("Resource (" + uri
            + ") could not be found or the invoker doesn't have adequate privileges to get the resource");
      }
      return url.openStream();
   }

   public URL getURL(String url) throws Exception
   {
      return getURL(scontext_, url);
   }

   private URL getURL(ServletContext context, String url) throws Exception
   {
      if (url.startsWith("jar:"))
      {
         String path = removePrefix("jar:/", url);
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         return cl.getResource(path);
      }
      else if (url.startsWith("classpath:"))
      {
         String path = removePrefix("classpath:/", url);
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         return cl.getResource(path);
      }
      else if (url.startsWith("war:"))
      {
         String path = removePrefix("war:", url);
         if (context != null)
         {
            return context.getResource(WAR_CONF_LOCATION + path);
         }
         if (scontextClassLoader_ != null)
         {
            return scontextClassLoader_.getResource(path);
         }
         throw new Exception("unsupport war uri in this configuration service");
      }
      else if (url.startsWith("file:"))
      {
         url = resolveSystemProperties(url);
         return new URL(url);
      }
      else if (url.indexOf(":") < 0 && contextPath != null)
      {
         return new URL(contextPath + url);
      }
      return null;
   }

   /**
    *
    * @param input the input
    * @return the resolved input
    */
   public static String resolveSystemProperties(String input)
   {
      final int NORMAL = 0;
      final int SEEN_DOLLAR = 1;
      final int IN_BRACKET = 2;
      if (input == null)
         return input;
      char[] chars = input.toCharArray();
      StringBuffer buffer = new StringBuffer();
      boolean properties = false;
      int state = NORMAL;
      int start = 0;
      for (int i = 0; i < chars.length; ++i)
      {
         char c = chars[i];
         if (c == '$' && state != IN_BRACKET)
            state = SEEN_DOLLAR;
         else if (c == '{' && state == SEEN_DOLLAR)
         {
            buffer.append(input.substring(start, i - 1));
            state = IN_BRACKET;
            start = i - 1;
         }
         else if (state == SEEN_DOLLAR)
            state = NORMAL;
         else if (c == '}' && state == IN_BRACKET)
         {
            if (start + 2 == i)
            {
               buffer.append("${}");
            }
            else
            {
               String value = null;
               String key = input.substring(start + 2, i);
               if (key.equals(EXO_CONTAINER_PROP_NAME))
               {
                  // The requested key is the name of current container
                  ExoContainer container = ExoContainerContext.getCurrentContainerIfPresent();
                  if (container instanceof PortalContainer)
                  {
                     // The current container is a portal container
                     RootContainer rootContainer = (RootContainer)ExoContainerContext.getTopContainer();
                     value = rootContainer.isPortalContainerConfigAware() ? "_" + container.getContext().getName() : "";
                  }
               }
               else
               {
                  value = System.getProperty(key);
               }
               if (value != null)
               {
                  properties = true;
                  buffer.append(value);
               }
            }
            start = i + 1;
            state = NORMAL;
         }
      }
      if (properties == false)
         return input;
      if (start != chars.length)
         buffer.append(input.substring(start, chars.length));
      return buffer.toString();

   }

   public boolean isDefault(String value)
   {
      return value == null || value.length() == 0 || "default".equals(value);
   }

   protected String removePrefix(String prefix, String url)
   {
      return url.substring(prefix.length(), url.length());
   }
}
