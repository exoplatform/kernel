/*
 * Copyright (C) 2010 eXo Platform SAS.
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

import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import java.util.List;
import java.util.Map;

import java.util.Map.Entry;

import javax.naming.NamingException;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import javax.sql.DataSource;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author <a href="anatoliy.bazko@exoplatform.org">Anatoliy Bazko</a>
 * @version $Id: InitialContextBinder.java 111 2010-11-11 11:11:11Z tolusha $
 */
public class InitialContextBinder
{

   public static final String BIND_REFERENCES_ELEMENT = "bind-references";

   public static final String REFERENCE_ELEMENT = "reference";

   public static final String PROPERTY_ELEMENT = "property";

   public static final String REFADDR_ELEMENT = "ref-addr";

   public static final String BIND_NAME_ATTR = "bind-name";

   public static final String CLASS_NAME_ATTR = "class-name";

   public static final String FACTORY_ATTR = "factory-name";

   public static final String FACTORY_LOCATION_ATTR = "factory-location";

   /**
    * Initial context binder. 
    */
   protected final InitialContextInitializer initialContextInitializer;

   protected final String bindReferencesStorage;

   protected final Reference reference;

   protected Map<String, Reference> bindReferences;

   /**
    * InitialContextBinder constructor.
    * 
    * @param initialContextInitializer
    *          initial context initializer
    * @param initParams
    *          initialization parameters
    * 
    * @throws ConfigurationException
    * @throws FileNotFoundException
    * @throws XMLStreamException
    * @throws NamingException
    */
   public InitialContextBinder(InitialContextInitializer initialContextInitializer, InitParams initParams)
      throws ConfigurationException, FileNotFoundException, XMLStreamException, NamingException
   {
      this.initialContextInitializer = initialContextInitializer;

      ValueParam cnParam = initParams.getValueParam("class-name");
      if (cnParam == null)
      {
         throw new ConfigurationException("class-name parameter expected");
      }

      ValueParam factoryParam = initParams.getValueParam("factory");
      if (factoryParam == null)
      {
         throw new ConfigurationException("factory parameter expected");
      }

      ValueParam flParam = initParams.getValueParam("factory-location");
      String factoryLocation = flParam != null ? flParam.getValue() : null;

      this.reference = new Reference(cnParam.getValue(), factoryParam.getValue(), factoryLocation);

      PropertiesParam addrsParam = initParams.getPropertiesParam("ref-addresses");
      if (addrsParam != null)
      {
         for (Entry entry : addrsParam.getProperties().entrySet())
         {
            reference.add(new StringRefAddr((String)entry.getKey(), (String)entry.getValue()));
         }
      }

      this.bindReferencesStorage = System.getProperty("java.io.tmpdir") + File.separator + "datasources.xml";
      this.bindReferences = new HashMap<String, Reference>();
      if (new File(bindReferencesStorage).exists())
      {
         this.bindReferences.putAll(doImport());
         for (Entry<String, Reference> entry : bindReferences.entrySet())
         {
            bind(entry.getKey(), entry.getValue());
         }
      }
   }

   /**
    * Bind reference and return datasource.
    * 
    * @param bindName
    *          bind name
    * @return  datasource
    * @throws NamingException
    * @throws FileNotFoundException
    * @throws XMLStreamException
    */
   public DataSource bind(String bindName) throws NamingException, FileNotFoundException, XMLStreamException
   {
      bind(bindName, reference);

      bindReferences.put(bindName, reference);
      doExport();

      return (DataSource)initialContextInitializer.getInitialContext().lookup(bindName);

   }

   private void bind(String bindName, Reference reference) throws NamingException
   {
      initialContextInitializer.getInitialContext().bind(bindName, reference);
   }

   /**
    * Export into xml.
    * 
    * @throws XMLStreamException
    * @throws FileNotFoundException
    */
   protected void doExport() throws XMLStreamException, FileNotFoundException
   {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      XMLStreamWriter writer =
         outputFactory.createXMLStreamWriter(new FileOutputStream(bindReferencesStorage), "UTF-8");

      writer.writeStartDocument("UTF-8", "1.0");
      writer.writeStartElement(BIND_REFERENCES_ELEMENT);

      for (Entry<String, Reference> entry : bindReferences.entrySet())
      {
         String bindName = entry.getKey();
         Reference reference = entry.getValue();

         writer.writeStartElement(REFERENCE_ELEMENT);
         writer.writeAttribute(BIND_NAME_ATTR, bindName);
         if (reference.getClassName() != null)
         {
            writer.writeAttribute(CLASS_NAME_ATTR, reference.getClassName());
         }
         if (reference.getFactoryClassName() != null)
         {
            writer.writeAttribute(FACTORY_ATTR, reference.getFactoryClassName());
         }
         if (reference.getFactoryClassLocation() != null)
         {
            writer.writeAttribute(FACTORY_LOCATION_ATTR, reference.getFactoryClassLocation());
         }

         writer.writeStartElement(REFADDR_ELEMENT);
         for (int i = 0; i < reference.size(); i++)
         {
            writer.writeStartElement(PROPERTY_ELEMENT);
            writer.writeAttribute(reference.get(i).getType(), (String)reference.get(i).getContent());
            writer.writeEndElement();
         }
         writer.writeEndElement();
         writer.writeEndElement();
      }

      writer.writeEndElement();
      writer.writeEndDocument();
   }

   /**
    * Import from xml. 
    * 
    * @return
    * @throws XMLStreamException 
    * @throws FileNotFoundException 
    */
   protected Map<String, Reference> doImport() throws FileNotFoundException, XMLStreamException
   {
      Map<String, Reference> references = new HashMap<String, Reference>();

      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLEventReader reader = factory.createXMLEventReader(new FileInputStream(bindReferencesStorage), "UTF-8");

      while (reader.hasNext())
      {
         XMLEvent event = reader.nextEvent();
         switch (event.getEventType())
         {
            case XMLStreamConstants.START_ELEMENT :
               StartElement startElement = event.asStartElement();
               Map<String, String> attr = parseAttributes(startElement);

               String localName = startElement.getName().getLocalPart();
               if (localName.equals(REFERENCE_ELEMENT))
               {
                  String bindName = attr.get(BIND_NAME_ATTR);
                  String className = attr.get(CLASS_NAME_ATTR);
                  String factoryName = attr.get(FACTORY_ATTR);
                  String factoryLocation = attr.get(FACTORY_LOCATION_ATTR);

                  Reference reference = new Reference(className, factoryName, factoryLocation);
                  for (RefAddr refAddr : importRefAddr(reader))
                  {
                     reference.add(refAddr);
                  }

                  references.put(bindName, reference);
               }
               break;
            case XMLStreamConstants.END_ELEMENT :
               break;
            default :
               break;
         }
      }

      return references;
   }

   private List<RefAddr> importRefAddr(XMLEventReader reader) throws XMLStreamException
   {
      List<RefAddr> refAddrs = new ArrayList<RefAddr>();

      outer : while (reader.hasNext())
      {
         XMLEvent event = reader.nextEvent();
         switch (event.getEventType())
         {
            case XMLStreamConstants.START_ELEMENT :
               StartElement startElement = event.asStartElement();

               Map<String, String> attr = parseAttributes(startElement);

               String localName = startElement.getName().getLocalPart();
               if (localName.equals(PROPERTY_ELEMENT))
               {
                  for (Entry<String, String> entry : attr.entrySet())
                  {
                     refAddrs.add(new StringRefAddr(entry.getKey(), entry.getValue()));
                  }
               }
               break;
            case XMLStreamConstants.END_ELEMENT :
               EndElement endElement = event.asEndElement();

               localName = endElement.getName().getLocalPart();
               if (localName.equals(REFADDR_ELEMENT))
               {
                  break outer;
               }
               break;
            default :
               break;
         }
      }

      return refAddrs;
   }

   private Map<String, String> parseAttributes(StartElement startElement)
   {
      Map<String, String> attr = new HashMap<String, String>();

      Iterator attributes = startElement.getAttributes();
      while (attributes.hasNext())
      {
         Attribute attribute = (Attribute)attributes.next();
         attr.put(attribute.getName().getLocalPart(), attribute.getValue());
      }

      return attr;
   }
}
