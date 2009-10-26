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
package org.exoplatform.commons.xml;

import org.xmlpull.mxp1.MXParserCachingStrings;
import org.xmlpull.v1.XmlPullParser;

/**
 * Jul 8, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ExoXPPParser.java,v 1.4 2004/10/20 20:58:27 tuan08 Exp $
 */
public class ExoXPPParser extends MXParserCachingStrings
{
   private String[] nodeAttributeName_ = new String[20];

   private String[] nodeAttributeValue_ = new String[20];

   private int nodeAttributeCount_;

   public boolean node(String name) throws Exception
   {
      if (this.eventType == XmlPullParser.END_DOCUMENT)
         return false;
      while (this.eventType != XmlPullParser.START_TAG)
      {
         next();
         if (this.eventType == XmlPullParser.END_DOCUMENT)
            return false;
      }
      if (getName().equals(name))
      {
         copyAttributes();
         next();
         return true;
      }
      return false;
   }

   public String nodeContent(String name) throws Exception
   {
      if (node(name))
         return getContent();
      return null;
   }

   public void endNode(String name) throws Exception
   {
      while (!(this.eventType == XmlPullParser.END_TAG && name.equals(getName())))
      {
         nextTag();
      }
   }

   public void mandatoryNode(String name) throws Exception
   {
      if (this.eventType == XmlPullParser.END_DOCUMENT)
      {
         throw new Exception("expect tag name " + name + ", but end of document");
      }
      while (this.eventType != XmlPullParser.START_TAG)
      {
         next();
         if (this.eventType == XmlPullParser.END_DOCUMENT)
         {
            throw new Exception("expect tag name " + name + ", but end of document");
         }
      }
      if (!getName().equals(name))
      {
         throw new Exception("expect tag name " + name + ", but find " + getName());
      }
      copyAttributes();
      next();
   }

   public String mandatoryNodeContent(String name) throws Exception
   {
      mandatoryNode(name);
      return getContent();
   }

   public String getContent() throws Exception
   {
      if (this.eventType != TEXT)
      {
         return null; // throw new Exception("Not a text node, name : " +
         // getName()) ;
      }
      return this.getText();
   }

   public String getNodeAttributeValue(String name)
   {
      for (int i = 0; i < nodeAttributeCount_; i++)
      {
         if (name.equals(nodeAttributeName_[i]))
            return nodeAttributeValue_[i];
      }
      return null;
   }

   private void copyAttributes()
   {
      for (int i = 0; i < this.attributeCount; i++)
      {
         nodeAttributeName_[i] = this.attributeName[i];
         nodeAttributeValue_[i] = this.attributeValue[i];
      }
      nodeAttributeCount_ = this.attributeCount;
   }

   static public ExoXPPParser getInstance() throws Exception
   {
      return new ExoXPPParser();
   }
}
