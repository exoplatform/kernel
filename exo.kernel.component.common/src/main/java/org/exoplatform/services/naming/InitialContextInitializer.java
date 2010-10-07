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
package org.exoplatform.services.naming;

import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.Property;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.xml.stream.XMLStreamException;

/**
 * Created by The eXo Platform SAS.<br/> Initializer for
 * Context.INITIAL_CONTEXT_FACTORY
 * 
 * @author Gennady Azarenkov
 * @version $Id: InitialContextInitializer.java 9867 2006-10-30 08:01:12Z geaz $
 */
public class InitialContextInitializer
{

   final public static String PROPERTIES_DEFAULT = "default-properties";

   final public static String PROPERTIES_MANDATORY = "mandatory-properties";

   final public static String BINDINGS_STORE_PATH = "bindings-store-path";

   final public static String DEFAULT_BINDING_STORE_PATH = System.getProperty("java.io.tmpdir") + File.separator
      + "bind-references.xml";

   private static Log LOG = ExoLogger.getLogger("exo.kernel.component.common.InitialContextInitializer");

   private List<BindReferencePlugin> bindReferencesPlugins;

   private String defaultContextFactory;

   private final InitialContext initialContext;

   private final InitialContextBinder binder;

   /**
    * @param params
    * @throws NamingException
    * @throws ConfigurationException if no context factory initialized and no
    *           context-factory nor default-context-factory configured
    * @throws XMLStreamException if error of serialized bindings read
    * @throws FileNotFoundException if cannot open file with serialized bindings
    */
   public InitialContextInitializer(InitParams params) throws NamingException, ConfigurationException,
      FileNotFoundException, XMLStreamException
   {
      for (Iterator propsParams = params.getPropertiesParamIterator(); propsParams.hasNext();)
      {
         PropertiesParam propParam = (PropertiesParam)propsParams.next();
         boolean isDefault = propParam.getName().equals(PROPERTIES_DEFAULT);
         boolean isMandatory = propParam.getName().equals(PROPERTIES_MANDATORY);
         for (Iterator props = propParam.getPropertyIterator(); props.hasNext();)
         {
            Property prop = (Property)props.next();
            String propName = prop.getName();
            String propValue = prop.getValue();
            String existedProp = System.getProperty(propName);
            if (isMandatory)
            {
               setSystemProperty(propName, propValue, propParam.getName());
            }
            else if (isDefault)
            {
               if (existedProp == null)
               {
                  setSystemProperty(propName, propValue, propParam.getName());
               }
               else
               {
                  LOG.info("Using default system property: " + propName + " = " + existedProp);
               }
            }
         }
      }
      initialContext = new InitialContext();
      bindReferencesPlugins = new ArrayList<BindReferencePlugin>();

      ValueParam bindingStorePathParam = params.getValueParam(BINDINGS_STORE_PATH);

      // binder
      if (bindingStorePathParam == null)
      {
         binder = new InitialContextBinder(this, DEFAULT_BINDING_STORE_PATH);
      }
      else
      {
         binder = new InitialContextBinder(this, bindingStorePathParam.getValue());
      }

   }

   private void setSystemProperty(String propName, String propValue, String propParamName)
   {
      System.setProperty(propName, propValue);
      if (propName.equals(Context.INITIAL_CONTEXT_FACTORY))
      {
         defaultContextFactory = propValue;
      }
      LOG.info("Using mandatory system property: " + propName + " = " + System.getProperty(propName));
   }

   // for out-of-container testing
   private InitialContextInitializer(String name, Reference reference) throws NamingException, FileNotFoundException,
      XMLStreamException
   {
      if (System.getProperty(Context.INITIAL_CONTEXT_FACTORY) == null)
      {
         System.setProperty(Context.INITIAL_CONTEXT_FACTORY, defaultContextFactory);
      }
      initialContext = new InitialContext();
      initialContext.rebind(name, reference);

      // binder
      binder = new InitialContextBinder(this, DEFAULT_BINDING_STORE_PATH);
   }

   /**
    * Patch for case when bound objects are not shared i.e. there are some parts
    * of app using different copy of Context, for example per web app
    * InitialContext in Tomcat
    */
   public void recall()
   {
      for (BindReferencePlugin plugin : bindReferencesPlugins)
      {
         try
         {
            InitialContext ic = new InitialContext();
            ic.bind(plugin.getBindName(), plugin.getReference());
            LOG.info("Reference bound (by recall()): " + plugin.getBindName());
         }
         catch (NameAlreadyBoundException e)
         {
            LOG.debug("Name already bound: " + plugin.getBindName());
         }
         catch (NamingException e)
         {
            LOG.error("Could not bind: " + plugin.getBindName(), e);
         }
      }
   }

   public void addPlugin(ComponentPlugin plugin)
   {
      if (plugin instanceof BindReferencePlugin)
      {
         BindReferencePlugin brplugin = (BindReferencePlugin)plugin;
         try
         {
            // initialContext = new InitialContext();
            initialContext.rebind(brplugin.getBindName(), brplugin.getReference());
            LOG.info("Reference bound: " + brplugin.getBindName());
            bindReferencesPlugins.add((BindReferencePlugin)plugin);
         }
         catch (NamingException e)
         {
            LOG.error("Could not bind: " + brplugin.getBindName(), e);
         }
      }
   }

   public ComponentPlugin removePlugin(String name)
   {
      return null;
   }

   public Collection getPlugins()
   {
      return bindReferencesPlugins;
   }

   /**
    * @return defaultContextFactory name
    */
   public String getDefaultContextFactory()
   {
      return defaultContextFactory;
   }

   /**
    * @return stored InitialContext
    */
   public synchronized InitialContext getInitialContext()
   {
      return initialContext;
   }

   // for out-of-container testing
   public static void initialize(String name, Reference reference) throws NamingException, FileNotFoundException,
      XMLStreamException
   {
      new InitialContextInitializer(name, reference);
   }

   /**
    * Constructs references from params, binds in initial contexts and persists list of all binded
    * references into file.
    * 
    * @param bindName
    *          bind name
    * @param className
    *          class name
    * @param factory
    *          factory name
    * @param factoryLocation
    *          factory location
    * @param refAddr
    *          map of references's properties
    * 
    * @throws NamingException
    *          if error occurs due to binding
    * @throws XMLStreamException 
    * @throws FileNotFoundException
    */
   public void bind(String bindName, String className, String factory, String factoryLocation,
      Map<String, String> refAddr) throws NamingException, FileNotFoundException, XMLStreamException
   {
      if (LOG.isDebugEnabled())
      {
         StringBuilder refAddrString = new StringBuilder();
         refAddrString.append('{');
         Set<Map.Entry<String, String>> refs = refAddr.entrySet();
         int i = 1;
         for (Map.Entry<String, String> ent : refs)
         {
            refAddrString.append(ent.getKey());
            refAddrString.append('=');
            refAddrString.append(ent.getValue());
            if (i < refs.size())
            {
               refAddrString.append(' ');
            }
            i++;
         }
         refAddrString.append('}');
         LOG.debug("Bind: " + bindName + " class-name:" + className + " factory:" + factory + " factoryLocation:"
            + factoryLocation + " refAddr:" + refAddrString);
      }

      binder.bind(bindName, className, factory, factoryLocation, refAddr);
   }
}
