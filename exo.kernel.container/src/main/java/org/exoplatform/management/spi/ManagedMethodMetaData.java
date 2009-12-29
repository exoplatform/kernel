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
package org.exoplatform.management.spi;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Meta data that describes a managed method.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class ManagedMethodMetaData extends ManagedMetaData
{

   /** . */
   private final Method method;

   /** . */
   private final Map<Integer, ManagedMethodParameterMetaData> parameters;

   /**
    * Build a new instance.
    *
    * @param method the method
    * @throws NullPointerException if the method is null
    */
   public ManagedMethodMetaData(Method method) throws NullPointerException
   {
      if (method == null)
      {
         throw new NullPointerException();
      }

      //
      this.method = method;
      this.parameters = new HashMap<Integer, ManagedMethodParameterMetaData>();
   }

   public Method getMethod()
   {
      return method;
   }

   public void addParameter(ManagedMethodParameterMetaData parameter)
   {
      if (parameter == null)
      {
         throw new NullPointerException("No null parameter accepted");
      }
      parameters.put(parameter.getIndex(), parameter);
   }

   public Collection<ManagedMethodParameterMetaData> getParameters()
   {
      return Collections.unmodifiableCollection(parameters.values());
   }
}
