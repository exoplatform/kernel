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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

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
}
