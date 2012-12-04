/*
 * Copyright (C) 2011 eXo Platform SAS.
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

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

/**
 * This implementation of {@link InitialContextFactory} is used to be able to share
 * all the objects that have been binded thanks to the {@link InitialContextInitializer}
 * which is required for example to be able to create and bind data sources dynamically
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class ExoContainerContextFactory implements InitialContextFactory
{

   /**
    * {@inheritDoc}
    */
   public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
   {
      return new ExoContainerCtx(environment);
   }

   @SuppressWarnings({"unchecked", "rawtypes"})
   private static class ExoContainerCtx extends SimpleContext
   {

      /**
       * The map containing all the bindings for all the containers defined
       */
      private static volatile Map<ExoContainer, AtomicReference<Map<String, Object>>> ALL_BINDINGS =
         new HashMap<ExoContainer, AtomicReference<Map<String, Object>>>();

      /**
       * The environment to use in case we cannot find the object
       */
      private final Hashtable env;

      /**
       * The current eXo container
       */
      private final ExoContainer container;

      /**
       * The nested context
       */
      private InitialContext ctx;

      /**
       * The reference to the bindings corresponding to this context;
       */
      private AtomicReference<Map<String, Object>> bindingsRef;
      
      public ExoContainerCtx(Hashtable<?, ?> env)
      {
         this.env = env == null ? null : (Hashtable)env.clone();
         this.container = ExoContainerContext.getCurrentContainerIfPresent();
         if (container != null)
         {
            AtomicReference<Map<String, Object>> ref = ALL_BINDINGS.get(container);
            if (ref == null)
            {
               synchronized (ExoContainerCtx.class)
               {
                  if (ref == null)
                  {
                     Map<ExoContainer, AtomicReference<Map<String, Object>>> tempAllBindings =
                        new HashMap<ExoContainer, AtomicReference<Map<String, Object>>>(ALL_BINDINGS);
                     tempAllBindings.put(container, ref =
                        new AtomicReference<Map<String, Object>>(new HashMap<String, Object>()));
                     ALL_BINDINGS = tempAllBindings;
                  }
               }
            }
            this.bindingsRef = ref;
         }
      }

      protected void bindRefValue(String name, Object value) throws NamingException
      {
         bind(name, value, false);
      }

      protected Map<String, Object> getBindings()
      {
         return bindingsRef.get();
      }

      protected void setBindings(Map<String, Object> bindings)
      {
         bindingsRef.set(bindings);
      }

      private InitialContext getContext() throws NamingException
      {
         if (ctx == null)
         {
            Hashtable env;
            if (this.env == null)
            {
               env = new Hashtable();
            }
            else
            {
               env = new Hashtable(this.env);
            }
            env.put(Context.INITIAL_CONTEXT_FACTORY, InitialContextInitializer.DEFAULT_INITIAL_CONTEXT_FACTORY);
            env.remove(InitialContextInitializer.class.getName());
            ctx = new InitialContext(env);
         }
         return ctx;
      }

      private boolean isInitialContextInitializerCall()
      {
         return container != null && env != null && env.containsKey(InitialContextInitializer.class.getName());
      }

      /**
       * {@inheritDoc}
       */
      public Object lookup(String name) throws NamingException
      {
         if (getBindings().containsKey(name) || isInitialContextInitializerCall())
         {
            return super.lookup(name);
         }
         return getContext().lookup(name);
      }

      /**
       * {@inheritDoc}
       */
      public Object lookup(Name name) throws NamingException
      {
         String sName = nameToString(name);
         if (getBindings().containsKey(sName) || isInitialContextInitializerCall())
         {
            return super.lookup(sName);
         }
         return getContext().lookup(name);
      }

      /**
       * {@inheritDoc}
       */
      public void bind(String name, Object value) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.bind(name, value);
            return;
         }
         getContext().bind(name, value);
      }

      /**
       * {@inheritDoc}
       */
      public void bind(Name name, Object value) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.bind(nameToString(name), value);
            return;
         }
         getContext().bind(name, value);
      }

      /**
       * {@inheritDoc}
       */
      public void rebind(String name, Object value) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.rebind(name, value);
            return;
         }
         getContext().rebind(name, value);
      }

      /**
       * {@inheritDoc}
       */
      public void rebind(Name name, Object value) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.rebind(nameToString(name), value);
            return;
         }
         getContext().rebind(name, value);
      }

      /**
       * {@inheritDoc}
       */
      public void unbind(String name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.unbind(name);
            return;
         }
         getContext().unbind(name);
      }

      /**
       * {@inheritDoc}
       */
      public void unbind(Name name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.unbind(nameToString(name));
            return;
         }
         getContext().unbind(name);
      }

      /**
       * {@inheritDoc}
       */
      public void rename(String name1, String name2) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.rename(name1, name2);
            return;
         }
         getContext().rename(name1, name2);
      }

      /**
       * {@inheritDoc}
       */
      public void rename(Name name1, Name name2) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.rename(nameToString(name1), nameToString(name2));
            return;
         }
         getContext().rename(name1, name2);
      }

      /**
       * {@inheritDoc}
       */
      public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.list(name);
         }
         return getContext().list(name);
      }

      /**
       * {@inheritDoc}
       */
      public NamingEnumeration<NameClassPair> list(String name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.list(name);
         }
         return getContext().list(name);
      }

      /**
       * {@inheritDoc}
       */
      public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.listBindings(name);
         }
         return getContext().listBindings(name);
      }

      /**
       * {@inheritDoc}
       */
      public NamingEnumeration<Binding> listBindings(String name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.listBindings(name);
         }
         return getContext().listBindings(name);
      }

      /**
       * {@inheritDoc}
       */
      public void destroySubcontext(Name name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.destroySubcontext(name);
            return;
         }
         getContext().destroySubcontext(name);
      }

      /**
       * {@inheritDoc}
       */
      public void destroySubcontext(String name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            super.destroySubcontext(name);
            return;
         }
         getContext().destroySubcontext(name);
      }

      /**
       * {@inheritDoc}
       */
      public Context createSubcontext(Name name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.createSubcontext(name);
         }
         return getContext().createSubcontext(name);
      }

      /**
       * {@inheritDoc}
       */
      public Context createSubcontext(String name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.createSubcontext(name);
         }
         return getContext().createSubcontext(name);
      }

      /**
       * {@inheritDoc}
       */
      public Object lookupLink(Name name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.lookupLink(name);
         }
         return getContext().lookupLink(name);
      }

      /**
       * {@inheritDoc}
       */
      public Object lookupLink(String name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.lookupLink(name);
         }
         return getContext().lookupLink(name);
      }

      /**
       * {@inheritDoc}
       */
      public NameParser getNameParser(Name name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.getNameParser(name);
         }
         return getContext().getNameParser(name);
      }

      /**
       * {@inheritDoc}
       */
      public NameParser getNameParser(String name) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.getNameParser(name);
         }
         return getContext().getNameParser(name);
      }

      /**
       * {@inheritDoc}
       */
      public Name composeName(Name name, Name prefix) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.composeName(name, prefix);
         }
         return getContext().composeName(name, prefix);
      }

      /**
       * {@inheritDoc}
       */
      public String composeName(String name, String prefix) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.composeName(name, prefix);
         }
         return getContext().composeName(name, prefix);
      }

      /**
       * {@inheritDoc}
       */
      public Object addToEnvironment(String propName, Object propVal) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.addToEnvironment(propName, propVal);
         }
         return getContext().addToEnvironment(propName, propVal);
      }

      /**
       * {@inheritDoc}
       */
      public Object removeFromEnvironment(String propName) throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.removeFromEnvironment(propName);
         }
         return getContext().removeFromEnvironment(propName);
      }

      /**
       * {@inheritDoc}
       */
      public Hashtable<?, ?> getEnvironment() throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            if (env == null)
            {
               // Must return non-null
               return new Hashtable(3, 0.75f);
            }
            else
            {
               return (Hashtable)env.clone();
            }
         }
         return getContext().getEnvironment();
      }

      protected Hashtable<?, ?> getInternalEnv()
      {
         return env;
      }

      protected Object getMutex()
      {
         return bindingsRef;
      }
      
      /**
       * {@inheritDoc}
       */
      public void close() throws NamingException
      {
         bindingsRef = null;
         if (env != null)
         {
            env.clear();
         }
         if (ctx != null)
         {
            ctx.close();
         }
      }

      /**
       * {@inheritDoc}
       */
      public String getNameInNamespace() throws NamingException
      {
         if (isInitialContextInitializerCall())
         {
            return super.getNameInNamespace();
         }
         return getContext().getNameInNamespace();
      }
   }
}
