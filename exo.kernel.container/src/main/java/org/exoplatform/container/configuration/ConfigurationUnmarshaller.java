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

import org.exoplatform.container.xml.Configuration;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collections;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

/**
 * Unmarshall a configuration.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ConfigurationUnmarshaller
{

   private static final Log log = ExoLogger.getLogger(ConfigurationUnmarshaller.class);

   private class Reporter implements ErrorHandler
   {

      private final URL url;

      private boolean valid;

      private Reporter(URL url)
      {
         this.url = url;
         this.valid = true;
      }

      public void warning(SAXParseException exception) throws SAXException
      {
         log.warn(exception.getMessage(), exception);
      }

      public void error(SAXParseException exception) throws SAXException
      {
         if (exception.getMessage().equals("cvc-elt.1: Cannot find the declaration of element 'configuration'."))
         {
            log.info("The document "
                  + url
                  + " does not contain a schema declaration, it should have an "
                  + "XML declaration similar to\n"
                  + "<configuration\n"
                  + "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                  + "   xsi:schemaLocation=\"http://www.exoplaform.org/xml/ns/kernel_1_1.xsd http://www.exoplaform.org/xml/ns/kernel_1_1.xsd\"\n"
                  + "   xmlns=\"http://www.exoplaform.org/xml/ns/kernel_1_1.xsd\">");
         }
         else
         {
            log.error("In document " + url + "  at (" + exception.getLineNumber() + "," + exception.getColumnNumber()
                     + ") :" + exception.getMessage());
         }
         valid = false;
      }

      public void fatalError(SAXParseException exception) throws SAXException
      {
         log.fatal("In document " + url + "  at (" + exception.getLineNumber() + "," + exception.getColumnNumber()
            + ") :" + exception.getMessage());
         valid = false;
      }
   }

   /** . */
   private final Set<String> profiles;

   public ConfigurationUnmarshaller(Set<String> profiles)
   {
      this.profiles = profiles;
   }

   public ConfigurationUnmarshaller()
   {
      this.profiles = Collections.emptySet();
   }

   /**
    * Returns true if the configuration file is valid according to its schema declaration. If the file
    * does not have any schema declaration, the file will be reported as valid.
    *
    * @param url the url of the configuration to validate
    * @return true if the configuration file is valid
    * @throws IOException any IOException thrown by using the provided URL
    * @throws NullPointerException if the provided URL is null
    */
   public boolean isValid(URL url) throws NullPointerException, IOException
   {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      String[] schemas = {
         Namespaces.KERNEL_1_0_URI,
         Namespaces.KERNEL_1_1_URI
      };
      factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
      factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", schemas);
      factory.setNamespaceAware(true);
      factory.setValidating(true);

      try
      {
         DocumentBuilder builder = factory.newDocumentBuilder();
         Reporter reporter = new Reporter(url);
         builder.setErrorHandler(reporter);
         builder.setEntityResolver(Namespaces.resolver);
         builder.parse(url.openStream());
         return reporter.valid;
      }
      catch (ParserConfigurationException e)
      {
         log.error("Got a parser configuration exception when doing XSD validation");
         return false;
      }
      catch (SAXException e)
      {
         log.error("Got a sax exception when doing XSD validation");
         return false;
      }
   }

   public Configuration unmarshall(URL url) throws Exception
   {
      boolean valid = isValid(url);
      if (!valid)
      {
         log.info("The configuration file " + url + " was not found valid according to its XSD");
      }

      //
      DocumentBuilderFactory factory = null;
      try
      {
         // With Java 6, it's safer to precise the builder factory class name as it may result:
         // java.lang.AbstractMethodError: org.apache.xerces.dom.DeferredDocumentImpl.getXmlStandalone()Z
	      // at com.sun.org.apache.xalan.internal.xsltc.trax.DOM2TO.setDocumentInfo(Unknown Source) 
         Method dbfniMethod = DocumentBuilderFactory.class.getMethod("newInstance", String.class, ClassLoader.class);
         factory = (DocumentBuilderFactory)dbfniMethod.invoke(null, "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl", Thread.currentThread().getContextClassLoader());
      }
      catch (InvocationTargetException e)
      {
         Throwable cause = e.getCause();
         if (cause instanceof FactoryConfigurationError)
         {
            // do nothing and let try to instantiate later
            log.debug("Was not able to find document builder factory class in Java > 5, will use default", cause);
         }
         else
         {
            // Rethrow
            throw e;
         }
      }
      catch (NoSuchMethodException e)
      {
         // Java < 6
      }

      //
      if (factory == null)
      {
         factory = DocumentBuilderFactory.newInstance();
      }

      //
      factory.setNamespaceAware(true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(url.openStream());

      // Filter DOM
      ProfileDOMFilter filter = new ProfileDOMFilter(profiles);
      filter.process(doc.getDocumentElement());

      // SAX event stream -> String
      StringWriter buffer = new StringWriter();
      SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
      TransformerHandler hd = tf.newTransformerHandler();
      StreamResult result = new StreamResult(buffer);
      hd.setResult(result);
      Transformer serializer = tf.newTransformer();
      serializer.setOutputProperty(OutputKeys.ENCODING, "UTF8");
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");

      // Transform -> SAX event stream
      SAXResult saxResult = new SAXResult(new NoKernelNamespaceSAXFilter(hd));

      // DOM -> Transform
      serializer.transform(new DOMSource(doc), saxResult);

      // Reuse the parsed document
      String document = buffer.toString();

      // Debug
      log.debug("About to parse configuration file " + document);

      //
      IBindingFactory bfact = BindingDirectory.getFactory(Configuration.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      return (Configuration)uctx.unmarshalDocument(new StringReader(document), null);
   }
}
