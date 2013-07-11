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
package org.exoplatform.container.util;

import org.exoplatform.commons.utils.ClassLoading;
import org.exoplatform.commons.utils.PropertiesLoader;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.commons.utils.Tools;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.ComponentLifecyclePlugin;
import org.exoplatform.container.xml.ContainerLifecyclePlugin;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 28, 2004
 * @version $Id: ContainerUtil.java 9894 2006-10-31 02:52:41Z tuan08 $
 */
@SuppressWarnings("rawtypes")
public class ContainerUtil
{
   /** The logger. */
   private static final Log LOG = ExoLogger.getExoLogger(ContainerUtil.class);

   public static Constructor<?>[] getSortedConstructors(Class<?> clazz) throws NoClassDefFoundError
   {
      Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      for (int i = 0; i < constructors.length; i++)
      {
         Constructor<?> c = constructors[i];
         if (c.isAnnotationPresent(Inject.class))
         {
            // This constructor has the annotation Inject so we will use it
            return new Constructor<?>[]{c};
         }
      }
      constructors = clazz.getConstructors();
      for (int i = 0; i < constructors.length; i++)
      {
         Constructor<?> tmp = constructors[i];
         for (int j = i + 1; j < constructors.length; j++)
         {
            if (constructors[i].getParameterTypes().length < constructors[j].getParameterTypes().length)
            {
               constructors[i] = constructors[j];
               constructors[j] = tmp;
            }
         }
      }

      return constructors;
   }

   /**
    * Indicates whether or not the given Class has a constructor annotated with Inject
    */
   public static boolean hasInjectableConstructor(Class<?> clazz)
   {
      Constructor<?>[] constructors = clazz.getDeclaredConstructors();
      for (int i = 0; i < constructors.length; i++)
      {
         Constructor<?> c = constructors[i];
         if (c.isAnnotationPresent(Inject.class))
         {
            // There is a constructor annotated with Inject
            return true;
         }
      }
      return false;
   }

   /**
    * Indicates whether or not the given Class has only a public non argument constructor
    */
   public static boolean hasOnlyEmptyPublicConstructor(Class<?> clazz)
   {
      Constructor<?>[] constructors = clazz.getConstructors();
      // No constructor annotated with Inject but we have only a no-argument public constructor
      // In that case it is optional according to the JSR 330
      return (constructors.length == 1 && constructors[0].getParameterTypes().length == 0 && Modifier
         .isPublic(constructors[0].getModifiers()));
   }

   /**
    * Indicates whether or not this class or one of its super class has at least one 
    * Inject annotation on a field or a method
    */
   public static boolean hasFieldOrMethodWithInject(Class<?> clazz)
   {
      if (clazz == null || clazz.equals(Object.class))
      {
         return false;
      }
      Field[] fields = clazz.getDeclaredFields();
      for (int i = 0; i < fields.length; i++)
      {
         if (fields[i].isAnnotationPresent(Inject.class))
         {
            return true;
         }
      }
      Method[] methods = clazz.getDeclaredMethods();
      for (int i = 0; i < methods.length; i++)
      {
         if (methods[i].isAnnotationPresent(Inject.class))
         {
            return true;
         }
      }
      return hasFieldOrMethodWithInject(clazz.getSuperclass());
   }

   /**
    * Indicates whether or not the given Class is a singleton 
    */
   public static boolean isSingleton(Class<?> clazz)
   {
      boolean hasInjectableConstructor = hasInjectableConstructor(clazz);
      boolean hasOnlyEmptyPublicConstructor = hasOnlyEmptyPublicConstructor(clazz);
      if (!hasInjectableConstructor && !hasOnlyEmptyPublicConstructor)
      {
         // There is no constructor JSR 330 compliant so it is the old mode
         return true;
      }
      else if (hasInjectableConstructor)
      {
         // There is at least one constructor annotated with Inject so we know that we expect the new mode
         return clazz.isAnnotationPresent(Singleton.class);
      }
      // We have only one public non argument constructor which is compliant with both modes
      if (hasFieldOrMethodWithInject(clazz))
      {
         // There is at least one field or a method annotated with Inject so we expect the new mode
         return clazz.isAnnotationPresent(Singleton.class);
      }
      return true;
   }

   public static Collection<URL> getConfigurationURL(final String configuration) throws Exception
   {
      final ClassLoader cl = Thread.currentThread().getContextClassLoader();

      Collection c = SecurityHelper.doPrivilegedIOExceptionAction(new PrivilegedExceptionAction<Collection>()
      {
         public Collection run() throws IOException
         {
            return Collections.list(cl.getResources(configuration));
         }
      });

      Map<String, URL> map = new HashMap<String, URL>();
      Iterator i = c.iterator();
      String forbiddenSuffix = "WEB-INF/" + configuration;
      while (i.hasNext())
      {
         URL url = (URL)i.next();
         String key = url.toString();
         // The content of the WEB-INF folder is part of the CL in JBoss AS 6 so 
         // we have to get rid of it in order to prevent any boot issues
         if (key.endsWith(forbiddenSuffix))
         {
            continue;
         }
         // jboss bug, jboss has a very weird behavior. It copy all the jar files
         // and
         // deploy them to a temp dir and include both jars, the one in sar and tmp
         // dir,
         // in the class path. It cause the configuration run twice
         int index1 = key.lastIndexOf("exo-");
         int index2 = key.lastIndexOf("exo.");
         int index = index1 < index2 ? index2 : index1;
         if (index >= 0)
            key = key.substring(index);
         map.put(key, url);
      }

      i = map.values().iterator();
      // while(i.hasNext()) {
      // URL url = (URL) i.next() ;
      // System.out.println("==> Add " + url);
      // }
      return map.values();
   }

   public static void addContainerLifecyclePlugin(ExoContainer container, ConfigurationManager conf)
   {
      Iterator i = conf.getConfiguration().getContainerLifecyclePluginIterator();
      while (i.hasNext())
      {
         ContainerLifecyclePlugin plugin = (ContainerLifecyclePlugin)i.next();
         addContainerLifecyclePlugin(container, plugin);
      }
   }

   private static void addContainerLifecyclePlugin(ExoContainer container, ContainerLifecyclePlugin plugin)
   {
      try
      {
         Class<?> clazz = ClassLoading.forName(plugin.getType(), ContainerUtil.class);
         org.exoplatform.container.ContainerLifecyclePlugin cplugin =
            (org.exoplatform.container.ContainerLifecyclePlugin)container
               .createComponent(clazz, plugin.getInitParams());
         cplugin.setName(plugin.getName());
         cplugin.setDescription(plugin.getDescription());
         container.addContainerLifecylePlugin(cplugin);
      }
      catch (Exception ex)
      {
         LOG.error("Failed to instanciate plugin " + plugin.getType() + ": " + ex.getMessage(), ex);
      }
   }

   public static void addComponentLifecyclePlugin(ExoContainer container, ConfigurationManager conf)
   {
      Collection plugins = conf.getConfiguration().getComponentLifecyclePlugins();
      Iterator i = plugins.iterator();
      while (i.hasNext())
      {
         ComponentLifecyclePlugin plugin = (ComponentLifecyclePlugin)i.next();
         try
         {
            Class<?> classType = ClassLoading.loadClass(plugin.getType(), ContainerUtil.class);
            org.exoplatform.container.component.ComponentLifecyclePlugin instance =
               (org.exoplatform.container.component.ComponentLifecyclePlugin)classType.newInstance();
            container.addComponentLifecylePlugin(instance);
         }
         catch (Exception ex)
         {
            LOG.error("Failed to instanciate plugin " + plugin.getType() + ": " + ex.getMessage(), ex);
         }
      }
   }

   public static void addComponents(ExoContainer container, ConfigurationManager conf)
   {
      Collection components = conf.getComponents();
      if (components == null)
         return;
      Iterator i = components.iterator();
      while (i.hasNext())
      {
         Component component = (Component)i.next();
         String type = component.getType();
         String key = component.getKey();
         try
         {
            Class<?> classType = ClassLoading.loadClass(type, ContainerUtil.class);
            if (key == null)
            {
               if (component.isMultiInstance())
               {
                  throw new UnsupportedOperationException("Multi-instance isn't allowed anymore");
               }
               else
               {
                  container.registerComponentImplementation(classType);
               }
            }
            else
            {
               try
               {
                  Class<?> keyType = ClassLoading.loadClass(key, ContainerUtil.class);
                  if (component.isMultiInstance())
                  {
                     throw new UnsupportedOperationException("Multi-instance isn't allowed anymore");
                  }
                  else
                  {
                     container.registerComponentImplementation(keyType, classType);
                  }
               }
               catch (Exception ex)
               {
                  container.registerComponentImplementation(key, classType);
               }
            }
         }
         catch (ClassNotFoundException ex)
         {
            LOG.error("Cannot register the component corresponding to key = '" + key + "' and type = '" + type + "'",
               ex);
         }
      }
   }

   /**
    * Loads the properties file corresponding to the given url
    * @param url the url of the properties file
    * @return a {@link Map} of properties
    */
   public static Map<String, String> loadProperties(URL url)
   {
      return loadProperties(url, true);
   }

   /**
    * Loads the properties file corresponding to the given url
    * @param url the url of the properties file
    * @param resolveVariables indicates if the variables must be resolved
    * @return a {@link Map} of properties
    */
   public static Map<String, String> loadProperties(URL url, boolean resolveVariables)
   {
      LinkedHashMap<String, String> props = null;
      String path = null;
      InputStream in = null;
      try
      {
         //
         if (url != null)
         {
            path = url.getPath();
            in = url.openStream();
         }

         //
         if (in != null)
         {
            String fileName = url.getFile();
            if (Tools.endsWithIgnoreCase(path, ".properties"))
            {
               if (LOG.isDebugEnabled())
                  LOG.debug("Attempt to load property file " + path);
               props = PropertiesLoader.load(in);
            }
            else if (Tools.endsWithIgnoreCase(fileName, ".xml"))
            {
               if (LOG.isDebugEnabled())
                  LOG.debug("Attempt to load property file " + path + " with XML format");
               props = PropertiesLoader.loadFromXML(in);
            }
            else if (LOG.isDebugEnabled())
            {
               LOG.debug("Will not load property file" + path + " because its format is not recognized");
            }
            if (props != null && resolveVariables)
            {
               // Those properties are used for variables resolution
               final Map<String, Object> currentProps = new HashMap<String, Object>();
               for (Map.Entry<String, String> entry : props.entrySet())
               {
                  String propertyName = entry.getKey();
                  String propertyValue = entry.getValue();
                  propertyValue = Deserializer.resolveVariables(propertyValue, currentProps);
                  props.put(propertyName, propertyValue);
                  currentProps.put(propertyName, propertyValue);
               }
            }
         }
         else
         {
            LOG.error("Could not load property file " + path);
         }
      }
      catch (Exception e)
      {
         LOG.error("Cannot load property file " + path, e);
      }
      finally
      {
         if (in != null)
         {
            try
            {
               in.close();
            }
            catch (IOException ignore)
            {
               if (LOG.isTraceEnabled())
               {
                  LOG.trace("An exception occurred: " + ignore.getMessage());
               }
            }
         }
      }
      return props;
   }
}
