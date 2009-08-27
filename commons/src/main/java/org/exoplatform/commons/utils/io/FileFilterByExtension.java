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
package org.exoplatform.commons.utils.io;

import java.io.File;
import java.io.FileFilter;
import java.util.HashSet;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Sep 13, 2004
 * @version $Id: FileFilterByExtension.java,v 1.2 2004/09/16 03:03:19 tuan08 Exp
 *          $
 */
public class FileFilterByExtension implements FileFilter
{
   private boolean acceptDir_;

   private HashSet knownExt_;

   public FileFilterByExtension(String[] ext, boolean acceptDir)
   {
      acceptDir_ = acceptDir;
      knownExt_ = new HashSet();
      for (int i = 0; i < ext.length; i++)
      {
         knownExt_.add(ext[i].trim().toLowerCase());
      }
   }

   public boolean accept(File file)
   {
      if (file.isDirectory())
      {
         if (acceptDir_)
            return true;
         return false;
      }
      String temp = file.getName();
      int idx = temp.lastIndexOf(".");
      if (idx > 0)
         temp = temp.substring(idx + 1, temp.length());
      else
         return false;
      return knownExt_.contains(temp.toLowerCase());
   }
}
