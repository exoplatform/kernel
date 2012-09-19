/*
 * Copyright (C) 2012 eXo Platform SAS.
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
package org.exoplatform.services.scheduler;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.xml.InitParams;
import org.quartz.JobListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * The root class allowing to assign a given job listener to one or several job identified by their 
 * {@link org.exoplatform.services.scheduler.JobKey}
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AddJobListenerComponentPlugin extends BaseComponentPlugin implements JobListener
{
   private List<JobKey> keys;
   
   public AddJobListenerComponentPlugin()
   {
   }
   
   public AddJobListenerComponentPlugin(InitParams params)
   {
      if (params != null)
      {
         keys = new ArrayList<JobKey>();
         for (Iterator<JobKey> it = params.getObjectParamIterator(); it.hasNext(); )
         {
            JobKey key = it.next();
            keys.add(key);
         }
      }
   }

   /**
    * @return the keys
    */
   public List<JobKey> getKeys()
   {
      if (keys == null || keys.isEmpty())
      {
         return Collections.singletonList(new JobKey(getName(), null));
      }
      return keys;
   }
}
