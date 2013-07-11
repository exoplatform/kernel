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
package org.exoplatform.xml.object;

import org.exoplatform.commons.utils.ClassLoading;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 11, 2005
 * @version $Id: XMLCollection.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class XMLCollection
{

   private List<XMLValue> list_ = new ArrayList<XMLValue>();

   private String type_;

   public XMLCollection()
   {
   }

   public XMLCollection(Collection<?> list) throws Exception
   {
      Iterator<?> i = list.iterator();
      while (i.hasNext())
      {
         Object value = i.next();
         if (value != null)
         {
            list_.add(new XMLValue(null, value));
         }
      }
      type_ = list.getClass().getName();
   }

   public String getType()
   {
      return type_;
   }

   public void setType(String s)
   {
      type_ = s;
   }

   public Collection<Object> getCollection() throws Exception
   {
      Class<?> clazz = ClassLoading.forName(type_, this);
      @SuppressWarnings("unchecked")
      Collection<Object> collection = (Collection<Object>)clazz.newInstance();
      for (int i = 0; i < list_.size(); i++)
      {
         XMLValue value = list_.get(i);
         collection.add(value.getObjectValue());
      }
      return collection;
   }

   public Iterator<XMLValue> getIterator()
   {
      return list_.iterator();
   }

   public String toXML(String encoding) throws Exception
   {
      return new String(toByteArray(encoding), encoding);
   }

   public byte[] toByteArray(String encoding) throws Exception
   {
      IBindingFactory bfact = XMLObject.getBindingFactoryInPriviledgedMode(XMLObject.class);
      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      mctx.marshalDocument(this, encoding, null, os);
      return os.toByteArray();
   }

   public static XMLCollection getXMLCollection(InputStream is) throws Exception
   {
      IBindingFactory bfact = XMLObject.getBindingFactoryInPriviledgedMode(XMLObject.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      return (XMLCollection)uctx.unmarshalDocument(is, null);
   }

   public static Collection<Object> getCollection(InputStream is) throws Exception
   {
      return getXMLCollection(is).getCollection();
   }
}
