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

import org.exoplatform.commons.utils.SecurityHelper;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 10, 2005
 * @version $Id: XMLObject.java 11659 2007-01-05 15:35:06Z geaz $
 */
public class XMLObject
{
   public static String CURRENT_VERSION = "1.0";

   static Map cacheFields_ = new HashMap();

   // private static String encoding = "UTF-8";

   private Map fields_ = new HashMap();

   private String type;

   public XMLObject()
   {
   }

   public XMLObject(Object obj) throws Exception
   {
      Class clazz = obj.getClass();
      Map fields = getFields(clazz);
      setType(obj.getClass().getName());
      Iterator i = fields.values().iterator();
      while (i.hasNext())
      {
         Field field = (Field)i.next();
         Object value = field.get(obj);
         addField(new XMLField(field.getName(), field.getType(), value));
      }
   }

   public String getType()
   {
      return type;
   }

   public void setType(String s)
   {
      type = s;
   }

   public XMLField getField(String name)
   {
      return (XMLField)fields_.get(name);
   }

   public void addField(Object o)
   {
      XMLField field = (XMLField)o;
      fields_.put(field.getName(), field);
   }

   public void addField(XMLField field)
   {
      fields_.put(field.getName(), field);
   }

   public Iterator getFieldIterator()
   {
      return fields_.values().iterator();
   }

   public Collection getFields()
   {
      return fields_.values();
   }

   public void setFields(Collection fields)
   {
      Iterator i = fields.iterator();
      while (i.hasNext())
      {
         XMLField field = (XMLField)i.next();
         fields_.put(field.getName(), field);
      }
   }

   public void setFields(Map fields)
   {
      fields_.putAll(fields);
   }

   public Object getFieldValue(String fieldName) throws Exception
   {
      XMLField field = (XMLField)fields_.get(fieldName);
      if (field != null)
         return field.getObjectValue();
      return null;
   }

   public void renameField(String oldName, String newName)
   {
      XMLField field = (XMLField)fields_.remove(oldName);
      field.setName(newName);
      fields_.put(newName, field);
   }

   public void removeField(String name)
   {
      fields_.remove(name);
   }

   public void addField(String name, Class fieldType, Object obj) throws Exception
   {
      addField(new XMLField(name, fieldType, obj));
   }

   @Override
   public String toString()
   {
      StringBuffer b = new StringBuffer();
      b.append("type: ").append(type).append("\n");
      Iterator i = fields_.values().iterator();
      while (i.hasNext())
      {
         XMLField field = (XMLField)i.next();
         b.append(field.toString()).append("\n");
      }
      return b.toString();
   }

   public Object toObject() throws Exception
   {
      Class clazz = Class.forName(type);
      Map fields = getFields(clazz);
      Object instance = clazz.newInstance();
      Iterator i = fields_.values().iterator();
      while (i.hasNext())
      {
         XMLField xmlfield = (XMLField)i.next();
         try
         {
            Object value = xmlfield.getObjectValue();
            Field field = (Field)fields.get(xmlfield.getName());
            field.set(instance, value);
         }
         catch (Exception ex)
         {
            System.err.println("ERROR: Cannot set  field: " + xmlfield.getName() + " of " + type);
            throw ex;
         }
      }
      return instance;
   }

   public String toXML() throws Exception
   {
      return toXML("UTF-8");
   }

   public String toXML(String encoding) throws Exception
   {
      return new String(toByteArray(encoding), encoding);
   }

   public byte[] toByteArray() throws Exception
   {
      return toByteArray("UTF-8");
   }

   public byte[] toByteArray(String encoding) throws Exception
   {
      IBindingFactory bfact = getBindingFactoryInPriviledgedMode(XMLObject.class);
      IMarshallingContext mctx = bfact.createMarshallingContext();
      mctx.setIndent(2);
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      mctx.marshalDocument(this, encoding, null, os);
      return os.toByteArray();
   }

   static public XMLObject getXMLObject(InputStream is) throws Exception
   {
      IBindingFactory bfact = getBindingFactoryInPriviledgedMode(XMLObject.class);
      IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
      XMLObject xmlobject = (XMLObject)uctx.unmarshalDocument(is, "UTF-8");
      return xmlobject;
   }

   static public Object getObject(InputStream is) throws Exception
   {
      return getXMLObject(is).toObject();
   }

   static Map getFields(Class clazz)
   {
      Map fields = (Map)cacheFields_.get(clazz);
      if (fields != null)
         return fields;
      synchronized (cacheFields_)
      {
         fields = new HashMap();
         findFields(fields, clazz);
         cacheFields_.put(clazz, fields);
      }
      return fields;
   }

   static void findFields(Map fields, Class clazz)
   {
      if (clazz.getName().startsWith("java.lang"))
         return;
      findFields(fields, clazz.getSuperclass());
      Field[] field = clazz.getDeclaredFields();
      for (int i = 0; i < field.length; i++)
      {
         int modifier = field[i].getModifiers();
         if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier))
            continue;
         
         final Field fld = field[i];

         SecurityHelper.doPrivilegedAction(new PrivilegedAction<Void>()
         {
            public Void run()
            {
               fld.setAccessible(true);
               return null;
            }
         });

         fields.put(field[i].getName(), field[i]);
      }
   }

   static protected IBindingFactory getBindingFactoryInPriviledgedMode(final Class clazz) throws JiBXException
   {
      try
      {
         return AccessController.doPrivileged(new PrivilegedExceptionAction<IBindingFactory>()
         {
            public IBindingFactory run() throws Exception
            {
               return BindingDirectory.getFactory(clazz);
            }
         });
      }
      catch (PrivilegedActionException pae)
      {
         Throwable cause = pae.getCause();
         if (cause instanceof JiBXException)
         {
            throw (JiBXException)cause;
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
   }
}
