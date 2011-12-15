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
package org.exoplatform.services.ispn;

import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.Mapper;

/**
 * The main class of all the mappers.
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public abstract class AbstractMapper<KIn, VIn, KOut, VOut> implements Mapper<KIn, VIn, KOut, VOut>
{

   /**
    * The serial version UID
    */
   private static final long serialVersionUID = 7118530772747505976L;

   /**
    * @see org.infinispan.distexec.mapreduce.Mapper#map(java.lang.Object, java.lang.Object, org.infinispan.distexec.mapreduce.Collector)
    */
   @Override
   public void map(KIn key, VIn value, Collector<KOut, VOut> collector)
   {
      if (isValid(key))
      {
         _map(key, value, collector);
      }
   }

   /**
    * This method is in fact an internal mapping
    * 
    * @see org.infinispan.distexec.mapreduce.Mapper#map(java.lang.Object, java.lang.Object, org.infinispan.distexec.mapreduce.Collector)
    */
   protected abstract void _map(KIn key, VIn value, Collector<KOut, VOut> collector);

   /**
    * Indicates if the given key matches with the current context, indeed as the cache instances are
    * shared it is needed to check each key to know if it is part of the targeted scope or not.
    * 
    * @param key the key to check
    * @return <code>true</code> if the key matches with the scope, <code>false</code> otherwise.
    */
   protected abstract boolean isValid(KIn key);

}
