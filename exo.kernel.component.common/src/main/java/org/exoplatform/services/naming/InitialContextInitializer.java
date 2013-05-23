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

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.xml.stream.XMLStreamException;

/**
 * Created by The eXo Platform SAS<br/> Initializer for
 * Context.INITIAL_CONTEXT_FACTORY
 * 
 * @author Gennady Azarenkov
 * @version $Id: InitialContextInitializer.java 9867 2006-10-30 08:01:12Z geaz $
 * @LevelAPI Experimental
 */
public class InitialContextInitializer
{

   static String DEFAULT_INITIAL_CONTEXT_FACTORY = PrivilegedSystemHelper.getProperty(Context.INITIAL_CONTEXT_FACTORY);
   
   public static final String PROPERTIES_DEFAULT = "default-properties";

   public static final String PROPERTIES_MANDATORY = "mandatory-properties";

   /**
    * This parameter is used to overload the default initial context factory in order to ensure that binded objects are shared
    */
   public static final String OVERLOAD_CONTEXT_FACTORY = "overload-context-factory";

   public static final String BINDINGS_STORE_PATH = "bindings-store-path";

   public static final String DEFAULT_BINDING_STORE_PATH = PrivilegedSystemHelper.getProperty("java.io.tmpdir")
      + File.separator + "bind-references.xml";

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.InitialContextInitializer");

   private List<BindReferencePlugin> bindReferencesPlugins;

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
            String existedProp = PrivilegedSystemHelper.getProperty(propName);
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
      bindReferencesPlugins = new ArrayList<BindReferencePlugin>();

      ValueParam bindingStorePathParam = params.getValueParam(BINDINGS_STORE_PATH);

      if (LOG.isDebugEnabled())
      {
         LOG.debug("The default initial context factory is " + DEFAULT_INITIAL_CONTEXT_FACTORY);         
      }
      ValueParam overloadContextFactoryParam = params.getValueParam(OVERLOAD_CONTEXT_FACTORY);
      if (overloadContextFactoryParam != null && overloadContextFactoryParam.getValue() != null
         && Boolean.valueOf(overloadContextFactoryParam.getValue()))
      {
         PrivilegedSystemHelper
            .setProperty(Context.INITIAL_CONTEXT_FACTORY, ExoContainerContextFactory.class.getName());

      }

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
      PrivilegedSystemHelper.setProperty(propName, propValue);
      if (propName.equals(Context.INITIAL_CONTEXT_FACTORY))
      {
         DEFAULT_INITIAL_CONTEXT_FACTORY = PrivilegedSystemHelper.getProperty(Context.INITIAL_CONTEXT_FACTORY);
      }
      LOG.info("Using mandatory system property: " + propName + " = " + PrivilegedSystemHelper.getProperty(propName));
   }

   // for out-of-container testing
   private InitialContextInitializer(String name, Reference reference) throws NamingException, FileNotFoundException,
      XMLStreamException
   {
      PrivilegedSystemHelper.setProperty(Context.INITIAL_CONTEXT_FACTORY, DEFAULT_INITIAL_CONTEXT_FACTORY);
      InitialContext initialContext = getInitialContext();
      initialContext.rebind(name, reference);

      // binder
      binder = new InitialContextBinder(this, DEFAULT_BINDING_STORE_PATH);
   }

   /**
    * Patch for case when bound objects are not shared i.e. there are some parts
    * of app using different copy of Context, for example per web app
    * InitialContext in Tomcat
    */
   @Deprecated
   public void recall()
   {
      for (BindReferencePlugin plugin : bindReferencesPlugins)
      {
         try
         {
            InitialContext ic = getInitialContext();
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
            InitialContext ic = getInitialContext();
            ic.rebind(brplugin.getBindName(), brplugin.getReference());
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
      return DEFAULT_INITIAL_CONTEXT_FACTORY;
   }

   /**
    * @return stored InitialContext
    */
   public InitialContext getInitialContext()
   {
      try
      {
         Hashtable<String, Object> env = new Hashtable<String, Object>();
         env.put(InitialContextInitializer.class.getName(), "true");
         return new InitialContext(env);
      }
      catch (NamingException e)
      {
         throw new RuntimeException("Cannot create the intial context", e);
      }
   }

   // for out-of-container testing
   public static void initialize(String name, Reference reference) throws NamingException, FileNotFoundException,
      XMLStreamException
   {
      new InitialContextInitializer(name, reference);
   }

   /**
    * Returns InitialContextBinder.
    */
   public InitialContextBinder getInitialContextBinder()
   {
      return binder;
   }
}
