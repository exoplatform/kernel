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
/**
 * Jul 8, 2004, 3:47:17 PM
 * @author: F. MORON
 * @email: francois.moron@laposte.net
 * 
 * */
package org.exoplatform.container.client.http;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class ClientTypeMap
{
   final static public String XHTML_MIME_TYPE = "text/xhtml";

   final static public String XHTMLMP_MIME_TYPE = "application/vnd.wap.xhtml+xml";
   
   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ClientTypeMap");

   private ArrayList<HttpClientType> clientList_;

   private static volatile ClientTypeMap singleton_;

   private void loadClientsInfos()
   {
      try
      {
         // System.setProperty("javax.xml.xpath.XPathFactory",
         // "com.sun.org.apache.xpath.internal.jaxp.XPathFactoryImpl") ;
         // XPathFactory xpfactory = XPathFactory.newInstance() ;
         XPath xpath = XPathFactory.newInstance().newXPath();
         XPathExpression clientTypeExp = xpath.compile("/clients-type/client-type");
         XPathExpression nameExp = xpath.compile("name/text()");
         XPathExpression userAgentPatternExp = xpath.compile("userAgentPattern/text()");
         XPathExpression preferredMimeTypeExp = xpath.compile("preferredMimeType/text()");
         XPathExpression rendererExp = xpath.compile("renderer/text()");
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         java.net.URL url = cl.getResource("conf/portal/clients-type.xml");
         DocumentBuilderFactory finstance = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = finstance.newDocumentBuilder();
         Document document = builder.parse(url.openStream());
         NodeList nodes = (NodeList)clientTypeExp.evaluate(document, XPathConstants.NODESET);
         for (int i = 0; i < nodes.getLength(); i++)
         {
            Node node = nodes.item(i);
            String name = (String)nameExp.evaluate(node, XPathConstants.STRING);
            String userAgentPattern = (String)userAgentPatternExp.evaluate(node, XPathConstants.STRING);
            String preferredMimeType = (String)preferredMimeTypeExp.evaluate(node, XPathConstants.STRING);
            String renderer = (String)rendererExp.evaluate(node, XPathConstants.STRING);
            HttpClientType clientInfo;
            if (renderer != null && renderer.length() > 0)
            {
               clientInfo = new HttpClientType(name, userAgentPattern, preferredMimeType, renderer);
            }
            else
            {
               clientInfo = new HttpClientType(name, userAgentPattern, preferredMimeType);
            }
            addClientInfo(clientInfo);
         }
      }
      catch (Exception ex)
      {
         LOG.error(ex.getLocalizedMessage(), ex);
      }
   }

   public ClientTypeMap()
   {
      clientList_ = new ArrayList<HttpClientType>();
      loadClientsInfos();
   }

   protected void addClientInfo(HttpClientType clientInfo)
   {
      clientList_.add(clientInfo);
   }

   /*
    * @return ClientInfo according to userAgent parameter and first ClientInfo
    * (ie5) if not found or if userAgent is null
    */
   public HttpClientType findClient(String userAgent)
   {
      if (userAgent == null)
         return clientList_.get(0);
      if (userAgent.equals(""))
         return clientList_.get(0);
      HttpClientType client;
      for (int i = 0; i < clientList_.size(); i++)
      {
         client = clientList_.get(i);
         String userAgentPattern = client.getUserAgentPattern();
         if (userAgentPattern != null)
         {
            try
            {
               if (userAgent.matches(userAgentPattern))
                  return client;
            }
            catch (PatternSyntaxException e)
            {
               LOG.error(e.getLocalizedMessage(), e);
               return clientList_.get(0);
            }
         }
      }
      return clientList_.get(0);
   }

   public static ClientTypeMap getInstance()
   {
      if (singleton_ == null)
      {
         synchronized (ClientTypeMap.class)
         {
            if (singleton_ == null)
            {
               singleton_ = new ClientTypeMap();               
            }
         }
      }
      return singleton_;
   }
}
