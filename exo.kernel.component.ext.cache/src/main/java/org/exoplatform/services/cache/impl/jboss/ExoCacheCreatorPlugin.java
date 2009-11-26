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
package org.exoplatform.services.cache.impl.jboss;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

/**
 * This class allows us to define new creators
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * 20 juil. 2009  
 */
public class ExoCacheCreatorPlugin extends BaseComponentPlugin
{

   /**
    * The list of all the creators defined for this ComponentPlugin
    */
   private final List<ExoCacheCreator> creators;

   public ExoCacheCreatorPlugin(InitParams params)
   {
      creators = new ArrayList<ExoCacheCreator>();
      List<?> configs = params.getObjectParamValues(ExoCacheCreator.class);
      for (int i = 0; i < configs.size(); i++)
      {
         ExoCacheCreator config = (ExoCacheCreator)configs.get(i);
         creators.add(config);
      }
   }

   /**
    * Returns all the creators defined for this ComponentPlugin
    */
   public List<ExoCacheCreator> getCreators()
   {
      return creators;
   }
}
