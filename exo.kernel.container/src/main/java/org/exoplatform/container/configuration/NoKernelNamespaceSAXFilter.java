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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashSet;
import java.util.Set;
import static org.exoplatform.container.configuration.Namespaces.*;

/**
 * Removes kernel namespace declaration from the document to not confuse the jibx thing.
 * It also filters the active profiles from the XML stream.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class NoKernelNamespaceSAXFilter extends DefaultHandler
{

   /** . */
   private static final Log log = ExoLogger.getExoLogger(NoKernelNamespaceSAXFilter.class);

   /** . */
   private static final String XSI_URI = "http://www.w3.org/2001/XMLSchema-instance";

   /** . */
   private ContentHandler contentHandler;

   /** . */
   private final Set<String> blackListedPrefixes;

   NoKernelNamespaceSAXFilter(ContentHandler contentHandler)
   {
      this.contentHandler = contentHandler;
      this.blackListedPrefixes = new HashSet<String>();
   }

   public void setDocumentLocator(Locator locator)
   {
      contentHandler.setDocumentLocator(locator);
   }

   public void startDocument() throws SAXException
   {
      contentHandler.startDocument();
   }

   public void endDocument() throws SAXException
   {
      contentHandler.endDocument();
   }

   public void startPrefixMapping(String prefix, String uri) throws SAXException
   {
      if (KERNEL_1_0_URI.equals(uri) || KERNEL_1_1_URI.equals(uri) || XSI_URI.equals(uri))
      {
         blackListedPrefixes.add(prefix);
         log.debug("Black listing prefix " + prefix + " with uri " + uri);
      }
      else
      {
         contentHandler.startPrefixMapping(prefix, uri);
         log.debug("Start prefix mapping " + prefix + " with uri " + uri);
      }
   }

   public void endPrefixMapping(String prefix) throws SAXException
   {
      if (!blackListedPrefixes.remove(prefix))
      {
         log.debug("Ending prefix mapping " + prefix);
         contentHandler.endPrefixMapping(prefix);
      }
      else
      {
         log.debug("Removed prefix mapping " + prefix + " from black list ");
      }
   }

   public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
   {
      Set<String> profiles = null;
      AttributesImpl noNSAtts = new AttributesImpl();
      for (int i = 0;i < atts.getLength();i++)
      {
         String attQName = atts.getQName(i);
         if ((attQName.equals("xmlns")) && blackListedPrefixes.contains(""))
         {
            // Skip
            log.debug("Skipping black listed xmlns attribute");
         }
         else if (attQName.startsWith("xmlns:") && blackListedPrefixes.contains(attQName.substring(6)))
         {
            // Skip
            log.debug("Skipping black listed " + attQName + " attribute");
         }
         else
         {
            String attURI = atts.getURI(i);
            String attLocalName = atts.getLocalName(i);
            String attType = atts.getType(i);
            String attValue = atts.getValue(i);

            //
            if (XSI_URI.equals(attURI))
            {
               // Skip
               log.debug("Skipping XSI " + attQName + " attribute");
               continue;
            }
            else if (KERNEL_1_0_URI.equals(attURI) || KERNEL_1_1_URI.equals(attURI))
            {
               log.debug("Requalifying prefixed attribute " + attQName + " attribute to " + localName);
               attURI = null;
               attQName = localName;
            }

            //
            noNSAtts.addAttribute(attURI, attLocalName, attQName, attType, attValue);
         }
      }

      //
      if (KERNEL_1_0_URI.equals(uri) || KERNEL_1_1_URI.equals(uri))
      {
         log.debug("Requalifying active profile " + qName + " start element to " + localName);
         qName = localName;
         uri = null;
      }

      //
      contentHandler.startElement(uri, localName, qName, noNSAtts);
   }

   public void endElement(String uri, String localName, String qName) throws SAXException
   {
      if (KERNEL_1_0_URI.equals(uri) || KERNEL_1_1_URI.equals(uri))
      {
         log.debug("Requalifying " + qName + " end element");
         qName = localName;
         uri = null;
      }

      //
      log.debug("Propagatting " + qName + " end element");
      contentHandler.endElement(uri, localName, qName);
   }

   public void characters(char[] ch, int start, int length) throws SAXException
   {
      contentHandler.characters(ch, start, length);
   }

   public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
   {
      contentHandler.ignorableWhitespace(ch, start, length);
   }

   public void processingInstruction(String target, String data) throws SAXException
   {
      contentHandler.processingInstruction(target, data);
   }

   public void skippedEntity(String name) throws SAXException
   {
      contentHandler.skippedEntity(name);
   }
}
