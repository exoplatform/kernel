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
package org.exoplatform.services.cache;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by The eXo Platform SAS Author : Tuan Nguyen
 * tuan08@users.sourceforge.net Jan 6, 2006
 *  @LevelAPI Platform
 */
public class ExoCacheConfigPlugin extends BaseComponentPlugin
{
   private List<ExoCacheConfig> configs_;

   public ExoCacheConfigPlugin(InitParams params)
   {
      configs_ = new ArrayList<ExoCacheConfig>();
      List configs = params.getObjectParamValues(ExoCacheConfig.class);
      for (int i = 0; i < configs.size(); i++)
      {
         ExoCacheConfig config = (ExoCacheConfig)configs.get(i);
         configs_.add(config);
      }
   }
   /**
    * @return the list of ExoCacheConfig
    */
   public List<ExoCacheConfig> getConfigs()
   {
      return configs_;
   }
}
