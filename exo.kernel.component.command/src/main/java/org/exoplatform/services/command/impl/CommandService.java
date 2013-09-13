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
package org.exoplatform.services.command.impl;

import org.apache.commons.chain.Catalog;
import org.apache.commons.chain.CatalogFactory;
import org.apache.commons.chain.config.ConfigParser;
import org.apache.commons.chain.impl.CatalogFactoryBase;
import org.apache.commons.digester.Digester;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.component.ComponentPlugin;
import org.exoplatform.container.spi.DefinitionByType;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Iterator;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: CommandService.java 12832 2007-02-15 12:41:32Z geaz $
 */
@DefinitionByType
public class CommandService
{

   // protected Catalog catalog;

   protected CatalogFactory catalogFactory;

   protected Digester digester;

   public CommandService()
   {
      this.catalogFactory = CatalogFactoryBase.getInstance();

      final ConfigParser parser = new ConfigParser();
      this.digester = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Digester>()
      {
         public Digester run()
         {
            return parser.getDigester();
         }
      });
   }

   public void addPlugin(ComponentPlugin plugin)
   {
      // no needs to do anything as CatalogFactory is initialized in plugin

      // if (plugin instanceof CommonsXMLConfigurationPlugin) {
      // CommonsXMLConfigurationPlugin cplugin = (CommonsXMLConfigurationPlugin)
      // plugin;
      // can just reinitialize it every time as have single instance
      // catalog = cplugin.getCatalog();
      // Iterator names = cplugin.getCatalogNames();
      // while(names.hasNext()) {
      // String name = (String)names.next();
      // catalogs.put(name, cplugin.getCatalog(name));
      // }
      // }
   }

   /**
    * puts catalog (add or update) using XML input stream
    * 
    * @param xml
    * @throws IOException
    * @throws SAXException
    */
   public void putCatalog(final InputStream xml) throws IOException, SAXException
   {
      // ConfigParser parser = new ConfigParser();
      // Prepare our Digester instance
      // Digester digester = parser.getDigester();
      digester.clear();

      try
      {
         SecurityHelper.doPrivilegedExceptionAction(new PrivilegedExceptionAction<Void>()
         {
            public Void run() throws Exception
            {
               digester.parse(xml);
               return null;
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof IOException)
         {
            throw (IOException)cause;
         }
         else if (cause instanceof SAXException)
         {
            throw (SAXException)cause;
         }
         else if (cause instanceof RuntimeException)
         {
            throw (RuntimeException)cause;
         }
         else
         {
            throw new RuntimeException(cause);
         }
      }

      // parser.getDigester().parse(xml);
   }

   /**
    * @return default catalog
    */
   public Catalog getCatalog()
   {
      Catalog catalog = catalogFactory.getCatalog();
      return catalog;
   }

   /**
    * @param name
    * @return named catalog
    */
   public Catalog getCatalog(String name)
   {
      Catalog catalog = catalogFactory.getCatalog(name);
      return catalog;

   }

   /**
    * @return iterator of catalog names. default catalog is not listed here!
    */
   public Iterator getCatalogNames()
   {
      return catalogFactory.getNames();
   }

}
