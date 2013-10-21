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
package org.exoplatform.container.util;

import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;

/**
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 */
public class TestContainerUtil extends TestCase
{

   public void testJBossAS6CL() throws Exception
   {
      ClassLoader oldCLoader = Thread.currentThread().getContextClassLoader();
      try
      {
         Thread.currentThread().setContextClassLoader(new JBossAS6MockClassLoader());
         Collection<URL> urls = ContainerUtil.getConfigurationURL("conf/configuration.xml");
         assertNotNull(urls);
         assertEquals(3, urls.size());
      }
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCLoader);
      }
   }

   private static class JBossAS6MockClassLoader extends ClassLoader
   {

      @Override
      public Enumeration<URL> getResources(String name) throws IOException
      {
         List<URL> urls = new ArrayList<URL>();
         urls.add(new URL("file:///GateIn-JBoss6/server/default/deploy/gatein.ear/lib/exo.portal.webui.portal.jar/conf/configuration.xml"));
         urls.add(new URL("file:///GateIn-JBoss6/server/default/deploy/gatein.ear/lib/exo.core.component.xml-processing/conf/configuration.xml"));
         urls.add(new URL("file:///GateIn-JBoss6/server/default/deploy/gatein-sample-extension.ear/sample-ext.war/WEB-INF/conf/configuration.xml"));
         urls.add(new URL("file:///GateIn-JBoss6/server/default/deploy/gatein.ear/lib/exo.kernel.commons.jar/conf/configuration.xml"));
         return Collections.enumeration(urls);
      }
      
   }

   public void testGetServletContextName()
   {
      final AtomicReference<String> scn = new AtomicReference<String>("myContextName");
      final AtomicReference<String> scp = new AtomicReference<String>("/myContextPath");
      ServletContext context = (ServletContext)Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class<?>[]{ServletContext.class}, new InvocationHandler()
      {
         
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
         {
            if ("getServletContextName".equals(method.getName()))
               return scn.get();
            else if ("getContextPath".equals(method.getName()))
               return scp.get();
            return null;
         }
      });
      assertEquals("myContextName", ContainerUtil.getServletContextName(context));
      scn.set(null);
      assertEquals("myContextPath", ContainerUtil.getServletContextName(context));
      scp.set("");
      assertEquals("", ContainerUtil.getServletContextName(context));
      scp.set("/a/b");
      assertEquals("a", ContainerUtil.getServletContextName(context));
      scp.set("/a2/b/");
      assertEquals("a2", ContainerUtil.getServletContextName(context));
      scp.set("a3/b");
      assertEquals("a3", ContainerUtil.getServletContextName(context));
      scp.set("a4/b/");
      assertEquals("a4", ContainerUtil.getServletContextName(context));
      scp.set(null);
      assertNull(ContainerUtil.getServletContextName(context));
   }
}
