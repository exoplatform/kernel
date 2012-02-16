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
package org.exoplatform.services.naming;

import org.exoplatform.services.jdbc.impl.CloseableDataSource;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InvalidNameException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.OperationNotSupportedException;
import javax.naming.Reference;
import javax.naming.Referenceable;
import javax.naming.spi.NamingManager;
import javax.sql.DataSource;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: SimpleContext.java 7117 2006-07-17 11:47:46Z peterit $
 */

public class SimpleContext implements Context
{

   /**
    * The logger
    */
   private static final Log LOG = ExoLogger.getLogger("exo.kernel.component.common.SimpleContext");

   private static final NameParser NAME_PARSER = new SimpleNameParser();
   
   private static volatile Map<String, Object> BINDINGS = new HashMap<String, Object>();

   public SimpleContext()
   {
   }

   protected Map<String, Object> getBindings()
   {
      return BINDINGS;
   }

   protected void setBindings(Map<String, Object> bindings)
   {
      BINDINGS = bindings;
   }

   /**
    * Converts a Name to a flat String.
    */
   protected String nameToString(Name name) throws NamingException
   {
      return name.toString();
   }

   /**
    * {@inheritDoc}
    */
   public Object lookup(Name name) throws NamingException
   {
      return lookup(nameToString(name));
   }

   /**
    * {@inheritDoc}
    */
   public Object lookup(String name) throws NamingException
   {
      if (name.isEmpty())
      {
         throw new InvalidNameException("Cannot bind empty name");
      }
      Object obj = getBindings().get(name);
      if (obj instanceof Reference)
      {
         synchronized (obj)
         {
            obj = getBindings().get(name);
            if (obj instanceof Reference)
            {
               try
               {
                  obj = NamingManager.getObjectInstance(obj, NAME_PARSER.parse(name), this, getInternalEnv());

                  if (obj instanceof DataSource)
                  {
                     obj = new CloseableDataSource((DataSource)obj);
                  }

                  // Re-bind with the object with its new value to be able to return the same ins
                  bindRefValue(name, obj);
               }
               catch (Exception e)
               {
                  LOG.error(e.getLocalizedMessage(), e);
                  NamingException ne = new NamingException("getObjectInstance failed");
                  ne.setRootCause(e);
                  throw ne;
               }
            }
         }
      }
      else if (obj == null)
      {
         throw new NameNotFoundException("No object has been binded with the name '" + name + "'");
      }
      return obj;
   }

   /**
    * {@inheritDoc}
    */
   public void bind(Name name, Object value) throws NamingException
   {
      bind(nameToString(name), value);
   }

   /**
    * {@inheritDoc}
    */
   public void bind(String name, Object value) throws NamingException
   {
      bind(name, value, true);
   }

   protected void bind(String name, Object value, boolean checkIfExists) throws NamingException
   {
      if (name.isEmpty())
      {
         throw new InvalidNameException("Cannot bind empty name");
      }
      // Call getStateToBind for using any state factories
      value = NamingManager.getStateToBind(value, NAME_PARSER.parse(name), this, getInternalEnv());

      if (value instanceof Context)
      {
         throw new OperationNotSupportedException("Context not supported");
      }
      else if (value instanceof LinkRef)
      {
         throw new OperationNotSupportedException("LinkRef not supported");
      }
      else if (value instanceof Referenceable)
      {
         value = ((Referenceable)value).getReference();
      }
      synchronized (getMutex())
      {
         Map<String, Object> tmpObjects = new HashMap<String, Object>(getBindings());
         if (checkIfExists && tmpObjects.containsKey(name))
         {
            throw new NameAlreadyBoundException("An object has already been binded with the name '" + name + "'");
         }
         tmpObjects.put(name, value);
         setBindings(tmpObjects);
      }
   }

   /**
    * {@inheritDoc}
    */
   protected void bindRefValue(String name, Object value) throws NamingException
   {
   }

   /**
    * {@inheritDoc}
    */
   public void rebind(Name name, Object value) throws NamingException
   {
      rebind(nameToString(name), value);
   }

   /**
    * {@inheritDoc}
    */
   public void rebind(String name, Object value) throws NamingException
   {
      bind(name, value, false);
   }

   /**
    * {@inheritDoc}
    */
   public void unbind(Name name) throws NamingException
   {
      unbind(nameToString(name));
   }

   /**
    * {@inheritDoc}
    */
   public void unbind(String name) throws NamingException
   {
      if (name.isEmpty())
      {
         throw new InvalidNameException("Cannot bind empty name");
      }
      synchronized (getMutex())
      {
         Map<String, Object> tmpObjects = new HashMap<String, Object>(getBindings());
         if (tmpObjects.remove(name) == null)
         {
            throw new NameNotFoundException("No object has been binded with the name '" + name + "'");
         }
         setBindings(tmpObjects);
      }
   }

   /**
    * {@inheritDoc}
    */
   public void rename(Name name1, Name name2) throws NamingException
   {
      rename(nameToString(name1), nameToString(name2));
   }

   /**
    * {@inheritDoc}
    */
   public void rename(String name1, String name2) throws NamingException
   {
      if (name1.isEmpty() || name2.isEmpty())
      {
         throw new InvalidNameException("Cannot bind empty name");
      }
      Object value;
      synchronized (getMutex())
      {
         Map<String, Object> tmpObjects = new HashMap<String, Object>(getBindings());
         if (tmpObjects.containsKey(name2))
         {
            throw new NameAlreadyBoundException("An object has already been binded with the name '" + name2 + "'");
         }
         else if ((value = tmpObjects.remove(name1)) == null)
         {
            throw new NameNotFoundException("No object has been binded with the name '" + name1 + "'");
         }
         tmpObjects.put(name2, value);
         setBindings(tmpObjects);
      }
   }

   /**
    * {@inheritDoc}
    */
   public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public NamingEnumeration<NameClassPair> list(String name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public NamingEnumeration<Binding> listBindings(String name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public void destroySubcontext(Name name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public void destroySubcontext(String name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public Context createSubcontext(Name name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public Context createSubcontext(String name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public Object lookupLink(Name name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public Object lookupLink(String name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public NameParser getNameParser(Name name) throws NamingException
   {
      return getNameParser(nameToString(name));
   }

   /**
    * {@inheritDoc}
    */
   public NameParser getNameParser(String name) throws NamingException
   {
      return NAME_PARSER;
   }

   /**
    * {@inheritDoc}
    */
   public Name composeName(Name nam1, Name name2) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public String composeName(String name1, String name2) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public Object addToEnvironment(String name1, Object name2) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   public Object removeFromEnvironment(String name) throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("rawtypes")
   public Hashtable<?, ?> getEnvironment() throws NamingException
   {
      return new Hashtable(3, 0.75f);
   }

   protected Hashtable<?, ?> getInternalEnv()
   {
      return null;
   }
   
   protected Object getMutex()
   {
      return SimpleContext.class;
   }
   
   /**
    * {@inheritDoc}
    */
   public void close() throws NamingException
   {
   }

   /**
    * {@inheritDoc}
    */
   public String getNameInNamespace() throws NamingException
   {
      throw new OperationNotSupportedException("Not supported");
   }

   private static class SimpleNameParser implements NameParser
   {
      /**
       * {@inheritDoc}
       */
      public Name parse(String name) throws NamingException
      {
         return new CompositeName(name);
      }  
   }
}