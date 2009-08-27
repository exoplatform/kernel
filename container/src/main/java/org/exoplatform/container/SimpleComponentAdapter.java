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
package org.exoplatform.container;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoVisitor;

/**
 * @author James Strachan
 * @author Mauro Talevi
 * @author Jeppe Cramon
 * @author Benjamin Mestrallet
 * @version $Revision: 1.5 $
 */
public class SimpleComponentAdapter implements ComponentAdapter
{

   private Object instance_;

   private Object key_;

   private Class implementation_;

   public SimpleComponentAdapter(Object key, Class implementation)
   {
      key_ = key;
      implementation_ = implementation;
   }

   public Object getComponentInstance(PicoContainer container)
   {
      if (instance_ != null)
         return instance_;
      ExoContainer exocontainer = (ExoContainer)container;
      try
      {
         synchronized (container)
         {
            instance_ = exocontainer.createComponent(getComponentImplementation());
         }
      }
      catch (Exception ex)
      {
         throw new RuntimeException("Cannot instantiate component " + getComponentImplementation(), ex);
      }
      return instance_;
   }

   public void verify(PicoContainer container)
   {
   }

   public Object getComponentKey()
   {
      return key_;
   }

   public Class getComponentImplementation()
   {
      return implementation_;
   }

   public void accept(PicoVisitor visitor)
   {
      visitor.visitComponentAdapter(this);
   }
}
