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
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
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

      private void log(String prefix, SAXParseException e)
      {
         System.err.println(prefix + " in document " + url + "  at (" + e.getLineNumber() + "," + e.getColumnNumber()
            + ") :" + e.getMessage());
      }

      public void warning(SAXParseException exception) throws SAXException
      {
         log("Warning", exception);
      }

      public void error(SAXParseException exception) throws SAXException
      {
         if (exception.getMessage().equals("cvc-elt.1: Cannot find the declaration of element 'configuration'."))
         {
            System.out
               .println("The document "
                  + url
                  + " does not contain a schema declaration, it should have an "
                  + "XML declaration similar to\n"
                  + "<configuration\n"
                  + "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                  + "   xsi:schemaLocation=\"http://www.exoplaform.org/xml/ns/kernel_1_0.xsd http://www.exoplaform.org/xml/ns/kernel_1_0.xsd\"\n"
                  + "   xmlns=\"http://www.exoplaform.org/xml/ns/kernel_1_0.xsd\">");
         }
         else
         {
            log("Error", exception);
         }
         valid = false;
      }

      public void fatalError(SAXParseException exception) throws SAXException
      {
         log("Fatal error", exception);
         valid = false;
      }
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
      SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      URL schemaURL = getClass().getResource("kernel-configuration_1_0.xsd");
      if (schemaURL != null)
      {
         try
         {
            Schema schema = factory.newSchema(schemaURL);
            Validator validator = schema.newValidator();
            Reporter reporter = new Reporter(url);
            validator.setErrorHandler(reporter);

            // Validate the document
            validator.validate(new StreamSource(url.openStream()));
            return reporter.valid;
         }
         catch (SAXException e)
         {
            System.err.print("Got a sax exception when doing XSD validation");
            e.printStackTrace(System.err);
            return false;
         }
      }
      else
      {
         return true;
      }
   }

   public Configuration unmarshall(URL url) throws Exception
   {

      /*
          byte[] bytes = new byte[256];
          InputStream in = url.openStream();
          ByteArrayOutputStream out = new ByteArrayOutputStream();
          for (int s = in.read(bytes);s != -1;s = in.read(bytes)) {
            out.write(bytes, 0, s);
          }
          String s = out.toString();
          System.out.println("s = " + s);
      */

      //
      boolean valid = isValid(url);
      if (!valid)
      {
         System.out.println("The configuration file " + url + " was not found valid according to its XSD");
      }

      // The buffer
      StringWriter buffer = new StringWriter();

      // Create a sax transformer result that will serialize the output
      SAXTransformerFactory tf = (SAXTransformerFactory)SAXTransformerFactory.newInstance();
      final TransformerHandler hd = tf.newTransformerHandler();
      hd.setResult(new StreamResult(buffer));
      Transformer serializer = tf.newTransformer();
      serializer.setOutputProperty(OutputKeys.ENCODING, "UTF8");
      serializer.setOutputProperty(OutputKeys.INDENT, "yes");

      // Perform
      InputSource source = new InputSource(url.openStream());
      SAXParserFactory spf = SAXParserFactory.newInstance();
      SAXParser saxParser = spf.newSAXParser();
      saxParser.parse(source, new NoKernelNamespaceSAXFilter(hd));

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
