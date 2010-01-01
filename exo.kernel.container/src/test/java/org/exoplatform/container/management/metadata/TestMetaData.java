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

package org.exoplatform.container.management.metadata;

import junit.framework.TestCase;
import org.exoplatform.container.management.MetaDataBuilder;
import org.exoplatform.management.annotations.ImpactType;
import org.exoplatform.management.spi.ManagedMethodMetaData;
import org.exoplatform.management.spi.ManagedTypeMetaData;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestMetaData extends TestCase
{

   private Map<String, ManagedMethodMetaData> getMethodMap(ManagedTypeMetaData type)
   {
      Map<String, ManagedMethodMetaData> methodMap = new HashMap<String, ManagedMethodMetaData>();
      for (ManagedMethodMetaData method : type.getMethods())
      {
         methodMap.put(method.getName(), method);
      }
      return methodMap;
   }

   public void testImpact()
   {
      MetaDataBuilder builder = new MetaDataBuilder(Foo.class);
      ManagedTypeMetaData type = builder.build();
      Map<String, ManagedMethodMetaData> methodMap = getMethodMap(type);
      ManagedMethodMetaData read = methodMap.get("read");
      assertEquals(ImpactType.READ, read.getImpact());
      ManagedMethodMetaData write = methodMap.get("write");
      assertEquals(ImpactType.WRITE, write.getImpact());
      ManagedMethodMetaData idempotentWrite = methodMap.get("idempotentWrite");
      assertEquals(ImpactType.IDEMPOTENT_WRITE, idempotentWrite.getImpact());
   }

   public void testMethodNameOverride()
   {
      MetaDataBuilder builder = new MetaDataBuilder(Bar.class);
      try
      {
         builder.build();
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
   }
}
