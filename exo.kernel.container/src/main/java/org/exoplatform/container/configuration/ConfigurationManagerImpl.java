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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ar.Archive;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.Configuration;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
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

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ConfigurationManagerImpl");

   protected Configuration configurations_;

   private ServletContext scontext_;

   private ClassLoader scontextClassLoader_;

   private String contextPath;

   private boolean validateSchema = true;
   
   private boolean logEnabled = true;

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

   public ConfigurationManagerImpl(Set<String> profiles, boolean logEnabled)
   {
      this(profiles);
      this.logEnabled = logEnabled;
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

   public void addConfiguration(Collection<URL> urls)
   {
      Iterator<URL> i = urls.iterator();
      while (i.hasNext())
      {
         URL url = i.next();
         addConfiguration(url);
      }
   }

   public void addConfiguration(URL url)
   {
      addConfiguration(scontext_, url);
   }

   private void addConfiguration(ServletContext context, URL url)
   {
      if (url == null)
         return;
      if (logEnabled && LOG_DEBUG)
         LOG.info("Add configuration " + url);
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
         importConf(unmarshaller, conf);
      }
      catch (Exception ex)
      {
         LOG.error("Cannot process the configuration " + currentURL.get(), ex);
      }
      finally
      {
         currentURL.set(null);
      }
   }

   /**
    * Recursively import the configuration files
    * 
    * @param unmarshaller the unmarshaller used to unmarshall the configuration file to import
    * @param conf the configuration in which we get the list of files to import
    * @throws Exception if an exception occurs while loading the files to import
    */
   private void importConf(ConfigurationUnmarshaller unmarshaller, Configuration conf) throws Exception
   {
      importConf(unmarshaller, conf, 1);
   }
   
   /**
    * Recursively import the configuration files
    * 
    * @param unmarshaller the unmarshaller used to unmarshall the configuration file to import
    * @param conf the configuration in which we get the list of files to import
    * @param depth used to log properly the URL of the file to import
    * @throws Exception if an exception occurs while loading the files to import
    */
   private void importConf(ConfigurationUnmarshaller unmarshaller, Configuration conf, int depth) throws Exception
   {
      List<String> urls = conf.getImports();
      if (urls != null)
      {
         StringBuilder prefix = new StringBuilder(depth);
         for (int i = 0; i < depth; i++)
         {
            prefix.append('\t');
         }
         for (int i = 0; i < urls.size(); i++)
         {
            String uri = (String)urls.get(i);
            URL urlObject = getURL(uri);
            if (urlObject != null)
            {
               if (logEnabled && LOG_DEBUG)
                  LOG.info(prefix + "import " + urlObject);
               // Set the URL of imported file
               currentURL.set(urlObject);
               conf = unmarshaller.unmarshall(urlObject);
               configurations_.mergeConfiguration(conf);
               importConf(unmarshaller, conf, depth + 1);
            }
            else
            {
               LOG.warn("Couldn't process the URL for " + uri + " configuration file ignored ");
            }
         }
      }
   }

   public void processRemoveConfiguration()
   {
      if (configurations_ == null)
         return;
      List<String> list = configurations_.getRemoveConfiguration();
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

   public Component getComponent(Class<?> clazz)
   {
      return configurations_.getComponent(clazz.getName());
   }

   public Collection<Component> getComponents()
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
      if (url == null)
         url = defaultURL;
      return getResource(url);
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
      final URL url = getURL(uri);
      if (url == null)
      {
         throw new IOException("Resource (" + uri
            + ") could not be found or the invoker doesn't have adequate privileges to get the resource");
      }

      return SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<InputStream>()
      {
         public InputStream run() throws Exception
         {
            return url.openStream();
         }
      });
   }

   public URL getURL(String url) throws Exception
   {
      return getURL(scontext_, url);
   }

   private URL getURL(final ServletContext context, String url) throws Exception
   {
      if (url == null)
      {
         return null;
      }
      else if (url.startsWith("jar:"))
      {
         String path = removePrefix("jar:", url);
         if (path.startsWith("/"))
         {
            path = path.substring(1);
         }
         final ClassLoader cl = Thread.currentThread().getContextClassLoader();
         final String finalPath = path;
         return SecurityHelper.doPrivilegedAction(new PrivilegedAction<URL>()
         {
            public URL run()
            {
               return cl.getResource(finalPath);
            }
         });
      }
      else if (url.startsWith("classpath:"))
      {
         String path = removePrefix("classpath:", url);
         if (path.startsWith("/"))
         {
            path = path.substring(1);
         }
         final ClassLoader cl = Thread.currentThread().getContextClassLoader();
         final String finalPath = path;
         return SecurityHelper.doPrivilegedAction(new PrivilegedAction<URL>()
         {
            public URL run()
            {
               return cl.getResource(finalPath);
            }
         });
      }
      else if (url.startsWith("war:"))
      {
         String path = removePrefix("war:", url);
         if (context != null)
         {
            final String fPath = path;
            return SecurityHelper.doPrivilegedMalformedURLExceptionAction(new PrivilegedExceptionAction<URL>()
            {
               public URL run() throws Exception
               {
                  return context.getResource(WAR_CONF_LOCATION + fPath);
               }
            });
         }
         if (scontextClassLoader_ != null)
         {
            if (path.startsWith("/"))
            {
               // The ClassLoader doesn't support the first "/"
               path = path.substring(1);
            }
            final String fPath = path;
            return SecurityHelper.doPrivilegedAction(new PrivilegedAction<URL>()
            {
               public URL run()
               {
                  return scontextClassLoader_.getResource(fPath);
               }
            });
         }
         throw new Exception("unsupport war uri in this configuration service");
      }
      else if (url.startsWith("file:"))
      {
         url = resolveFileURL(url);
         return new URL(url);
      }
      else if (Archive.isArchiveURL(url))
      {
         return Archive.createArchiveURL(url);
      }
      else if (url.indexOf(":") < 0 && contextPath != null)
      {
         if (Archive.isArchiveURL(contextPath))
            return Archive.createArchiveURL(contextPath + url.replace('\\', '/'));
         return new URL(contextPath + url.replace('\\', '/'));
      }
      return null;
   }

   /**
    * This methods is used to convert the given into a valid url, it will:
    * <ol>
    * <li>Resolve variables in the path if they exist</li>
    * <li>Replace windows path separators with proper separators</li>
    * <li>Ensure that the path start with file:///</li>
    * </ol>
    * , then it will 
    * @param url the url to resolve
    * @return the resolved url
    */
   private String resolveFileURL(String url)
   {
      url = Deserializer.resolveVariables(url);
      // we ensure that we don't have windows path separator in the url
      url = url.replace('\\', '/');
      if (!url.startsWith("file:///"))
      {
         // The url is invalid, so we will fix it
         // it happens when we use a path of type file://${path}, under
         // linux or mac os the path will start with a '/' so the url
         // will be correct but under windows we will have something
         // like C:\ so the first '/' is missing
         if (url.startsWith("file://"))
         {
            // The url is of type file://, so one '/' is missing
            url = "file:///" + url.substring(7);
         }
         else if (url.startsWith("file:/"))
         {
            // The url is of type file:/, so two '/' are missing
            url = "file:///" + url.substring(6);
         }
         else
         {
            // The url is of type file:, so three '/' are missing
            url = "file:///" + url.substring(5);
         }
      }
      return url;
   }

   protected String removePrefix(String prefix, String url)
   {
      return url.substring(prefix.length(), url.length());
   }
}
