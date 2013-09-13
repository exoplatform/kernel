/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container.weld;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValuesParam;
import org.jboss.weld.environment.se.Weld;

import java.util.List;

import javax.enterprise.inject.spi.Extension;

/**
 * This provider simply returns no {@link Extension} to be added to {@link Weld}. It also
 * relies on a configurable lists of prefixes of classes to be included or excluded from
 * the scope of {@link Weld}.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class BasicWeldContainerHelper implements WeldContainerHelper
{

   /**
    * The name of the values parameter that will contain the prefixes of the
    * classes to be included into the scope of Weld
    */
   private static final String INCLUDE_PARAM_NAME = "include";

   /**
    * The name of the values parameter that will contain the prefixes of the
    * classes to be excluded from the scope of Weld
    */
   private static final String EXCLUDE_PARAM_NAME = "exclude";

   /**
    * An array of String containing the prefixes of classes to be included into the scope of {@link Weld}
    */
   private final String[] includes;

   /**
    * An array of String containing the prefixes of classes to be excluded from the scope of {@link Weld}
    */
   private final String[] excludes;

   /**
    * The default constructor
    * @param p the initial parameters
    */
   public BasicWeldContainerHelper(InitParams p)
   {
      ValuesParam params = p == null ? null : p.getValuesParam(INCLUDE_PARAM_NAME);
      if (params != null && params.getValues().size() > 0)
      {
         includes = new String[params.getValues().size()];
         int i = 0;
         for (String value : params.getValues())
         {
            includes[i++] = value;
         }
      }
      else
      {
         includes = null;
      }
      params = p == null ? null : p.getValuesParam(EXCLUDE_PARAM_NAME);
      if (params != null && params.getValues().size() > 0)
      {
         excludes = new String[params.getValues().size()];
         int i = 0;
         for (String value : params.getValues())
         {
            excludes[i++] = value;
         }
      }
      else
      {
         excludes = null;
      }
      if (includes == null && excludes == null)
      {
         throw new IllegalArgumentException("The values parameter " + INCLUDE_PARAM_NAME + " and " + EXCLUDE_PARAM_NAME
            + " cannot be both empty, please set at least " + "one value to one of them.");
      }
   }

   /**
    * Simply returns <code>null</code>
    * {@inheritDoc}
    */
   public List<Extension> getExtensions()
   {
      return null;
   }

   /**
    * {@inheritDoc}
    */
   public boolean isIncluded(Class<?> clazz)
   {
      String name = clazz.getName();
      boolean result;
      if (includes != null && includes.length > 0)
      {
         // We included at least one class prefix
         result = false;
         for (int i = 0, length = includes.length; i < length; i++)
         {
            if (name.startsWith(includes[i]))
            {
               result = true;
               break;
            }
         }
      }
      else
      {
         // we did not define any class prefix
         result = true;
      }
      if (excludes != null)
      {
         for (int i = 0, length = excludes.length; i < length; i++)
         {
            if (name.startsWith(excludes[i]))
            {
               result = false;
               break;
            }
         }
      }
      return result;
   }
}
