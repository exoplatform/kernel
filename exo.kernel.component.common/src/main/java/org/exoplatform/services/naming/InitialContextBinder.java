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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.NameAlreadyBoundException;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
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
 * Class is responsible for binding references at runtime, persists on file and automatically binds after restart.
 * 
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

   /**
    * Absolute file path to references's storage.
    */
   protected final String bindingsStorePath;

   /**
    * All current binded references.
    */
   protected Map<String, Reference> bindings;

   /**
    * InitialContextBinder constructor.
    * 
    * @param initialContextInitializer
    *          initial context initializer
    * @param initParams
    *          initialization parameters
    * @throws FileNotFoundException 
    * @throws XMLStreamException
    * @throws NamingException
    */
   InitialContextBinder(InitialContextInitializer initialContextInitializer, String bindingsStorePath)
      throws FileNotFoundException, XMLStreamException, NamingException
   {
      this.initialContextInitializer = initialContextInitializer;
      this.bindings = new ConcurrentHashMap<String, Reference>();
      this.bindingsStorePath = bindingsStorePath;

      if (new File(bindingsStorePath).exists())
      {
         Map<String, Reference> importedRefs = readBindings();
         for (Entry<String, Reference> entry : importedRefs.entrySet())
         {
            bind(entry.getKey(), entry.getValue());
         }
      }
   }

   /**
    * InitialContextBinder constructor.
    */
   InitialContextBinder(InitialContextInitializer initialContextInitializer) throws FileNotFoundException,
      XMLStreamException, NamingException
   {
      this(initialContextInitializer, InitialContextInitializer.DEFAULT_BINDING_STORE_PATH);
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
      Reference reference = new Reference(className, factory, factoryLocation);
      for (Map.Entry<String, String> entry : refAddr.entrySet())
      {
         reference.add(new StringRefAddr(entry.getKey(), entry.getValue()));
      }

      bind(bindName, reference);

      saveBindings();
   }

   private void bind(String bindName, Reference reference) throws NamingException
   {
      try
      {
         initialContextInitializer.getInitialContext().bind(bindName, reference);
      }
      catch (NameAlreadyBoundException e)
      {
         initialContextInitializer.getInitialContext().rebind(bindName, reference);
      }
      bindings.put(bindName, reference);
   }

   /**
    * Export references into xml-file.
    * 
    * @throws XMLStreamException 
    *          if any exception occurs during export
    * @throws FileNotFoundException
    *          if can't open output stream from file
    */
   protected synchronized void saveBindings() throws FileNotFoundException, XMLStreamException
   {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      XMLStreamWriter writer = outputFactory.createXMLStreamWriter(new FileOutputStream(bindingsStorePath), "UTF-8");

      writer.writeStartDocument("UTF-8", "1.0");
      writer.writeStartElement(BIND_REFERENCES_ELEMENT);

      for (Entry<String, Reference> entry : bindings.entrySet())
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
    * Import references from xml-file. 
    * 
    * @return map with bind name - references
    * 
    * @throws XMLStreamException
    *          if errors occurs during import 
    * @throws FileNotFoundException 
    *          if can't open input stream from file
    */
   protected Map<String, Reference> readBindings() throws FileNotFoundException, XMLStreamException
   {
      Stack<RefEntity> stack = new Stack<RefEntity>();

      Map<String, Reference> importedRefs = new HashMap<String, Reference>();

      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLEventReader reader = factory.createXMLEventReader(new FileInputStream(bindingsStorePath), "UTF-8");

      while (reader.hasNext())
      {
         XMLEvent event = reader.nextEvent();
         switch (event.getEventType())
         {
            case XMLStreamConstants.START_ELEMENT :
               StartElement startElement = event.asStartElement();

               Map<String, String> attr = new HashMap<String, String>();

               Iterator attributes = startElement.getAttributes();
               while (attributes.hasNext())
               {
                  Attribute attribute = (Attribute)attributes.next();
                  attr.put(attribute.getName().getLocalPart(), attribute.getValue());
               }

               String localName = startElement.getName().getLocalPart();
               if (localName.equals(REFERENCE_ELEMENT))
               {
                  String bindName = attr.get(BIND_NAME_ATTR);
                  String className = attr.get(CLASS_NAME_ATTR);
                  String factoryName = attr.get(FACTORY_ATTR);
                  String factoryLocation = attr.get(FACTORY_LOCATION_ATTR);

                  Reference reference = new Reference(className, factoryName, factoryLocation);
                  stack.push(new RefEntity(bindName, reference));
               }
               else if (localName.equals(PROPERTY_ELEMENT))
               {
                  RefEntity refEntity = stack.pop();
                  Reference reference = refEntity.getValue();

                  for (Entry<String, String> entry : attr.entrySet())
                  {
                     reference.add(new StringRefAddr(entry.getKey(), entry.getValue()));
                  }

                  refEntity.setValue(reference);
                  stack.push(refEntity);
               }

               break;
            case XMLStreamConstants.END_ELEMENT :
               EndElement endElement = event.asEndElement();
               localName = endElement.getName().getLocalPart();

               if (localName.equals(REFERENCE_ELEMENT))
               {
                  RefEntity refEntity = stack.pop();
                  importedRefs.put(refEntity.getKey(), refEntity.getValue());
               }
               break;
            default :
               break;
         }
      }

      return importedRefs;
   }

   /**
    * Class implements Map.Entry interface and used to push/pop entity in stack. 
    */
   class RefEntity implements Map.Entry
   {

      /**
       * Entry key.
       */
      private final String key;

      /**
       * Entry value.
       */
      private Reference value;

      /**
       * RefEntity constructor.
       * 
       * @param key
       *          entry key
       * @param value
       *          entry value       
       */
      public RefEntity(String key, Reference value)
      {
         this.key = key;
         this.value = value;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public String getKey()
      {
         return key;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Reference getValue()
      {
         return value;
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public Reference setValue(Object value)
      {
         Reference oldValue = this.value;
         this.value = (Reference)value;

         return oldValue;
      }
   }
}
