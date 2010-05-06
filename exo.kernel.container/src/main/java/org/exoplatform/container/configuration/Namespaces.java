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

import org.xml.sax.EntityResolver;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class Namespaces
{

   /** . */
   public static final String KERNEL_1_0_URI = "http://www.exoplaform.org/xml/ns/kernel_1_0.xsd";

   /** . */
   public static final String KERNEL_1_1_URI = "http://www.exoplaform.org/xml/ns/kernel_1_1.xsd";

   /** . */
   public static final String KERNEL_1_2_URI = "http://www.exoplaform.org/xml/ns/kernel_1_2.xsd";

   /**
    * All the namespaces related to the kernel
    */
   public static final Set<String> KERNEL_NAMESPACES_SET;

   static
   {
      Set<String> tmp = new LinkedHashSet<String>();
      tmp.add(KERNEL_1_0_URI);
      tmp.add(KERNEL_1_1_URI);
      tmp.add(KERNEL_1_2_URI);
      KERNEL_NAMESPACES_SET = Collections.unmodifiableSet(tmp);
   }

   /** . */
   static final EntityResolver resolver;

   static
   {
      Map<String, String> resourceMap = new HashMap<String, String>();
      resourceMap.put(KERNEL_1_0_URI, "org/exoplatform/container/configuration/kernel-configuration_1_0.xsd");
      resourceMap.put(KERNEL_1_1_URI, "org/exoplatform/container/configuration/kernel-configuration_1_1.xsd");
      resourceMap.put(KERNEL_1_2_URI, "org/exoplatform/container/configuration/kernel-configuration_1_2.xsd");
      resolver = new EntityResolverImpl(Namespaces.class.getClassLoader(), resourceMap);
   }

   /**
    * @return A safe copy of the list of kernel namespaces
    */
   public static String[] getKernelNamespaces()
   {
      return new String[]{KERNEL_1_0_URI, KERNEL_1_1_URI, KERNEL_1_2_URI};
   }
   
   /**
    * Indicates whether the given uri is a kernel namespace or not
    * @param uri the uri to check
    * @return <code>true</code> if it is a kernel namespace, <code>false</code> otherwise.
    */
   public static boolean isKernelNamespace(String uri)
   {
      return KERNEL_NAMESPACES_SET.contains(uri);
   }
}
