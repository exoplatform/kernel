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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by The eXo Platform SAS Author : Thuannd nhudinhthuan@yahoo.com Feb
 * 6, 2006
 */
public class ZipUtil
{

   private final int BUFFER = 2048;

   private byte data[] = new byte[BUFFER];

   public void addToArchive(File input, File output, boolean containParent) throws Exception
   {
      ByteArrayOutputStream byteOutput = addToArchive(input, containParent);
      output = createFile(output, false);
      byteOutput.writeTo(new FileOutputStream(output));
      byteOutput.close();
   }

   public ByteArrayOutputStream addToArchive(File input, boolean containParent) throws Exception
   {
      ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
      addToArchive(input, byteOutput, containParent).close();
      return byteOutput;
   }

   public ZipOutputStream addToArchive(File input, OutputStream output, boolean containParent) throws Exception
   {
      String path = input.getAbsolutePath();
      ZipOutputStream zipOutput = new ZipOutputStream(output);
      BufferedInputStream bufInput = null;

      List<File> list = listFile(input);
      if (input.isDirectory())
         list.remove(input);
      if (list == null || list.size() < 1)
         return zipOutput;
      for (File f : list)
      {
         String filePath = f.getAbsolutePath();
         if (filePath.startsWith(path))
         {
            if (containParent && input.isDirectory())
               filePath = input.getName() + File.separator + filePath.substring(path.length() + 1);
            else if (input.isDirectory())
               filePath = filePath.substring(path.length() + 1);
            else
               filePath = input.getName();
         }
         if (f.isFile())
         {
            FileInputStream fileInput = new FileInputStream(f);
            bufInput = new BufferedInputStream(fileInput, BUFFER);
         }
         else
            filePath += "/";
         addToArchive(zipOutput, bufInput, filePath);
         if (bufInput != null)
            bufInput.close();
      }
      return zipOutput;
   }

   public ByteArrayOutputStream addToArchive(InputStream input, String entryName) throws Exception
   {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      ZipOutputStream zipOutput = new ZipOutputStream(output);
      addToArchive(zipOutput, input, entryName);
      zipOutput.close();
      return output;
   }

   public ZipOutputStream addToArchive(ZipOutputStream zipOutput, InputStream input, String entryName) throws Exception
   {
      ZipEntry entry = new ZipEntry(entryName);
      zipOutput.putNextEntry(entry);
      if (input != null)
      {
         int count;
         while ((count = input.read(data, 0, BUFFER)) != -1)
            zipOutput.write(data, 0, count);
      }
      zipOutput.closeEntry();
      return zipOutput;
   }

   public ZipOutputStream addToArchive(ZipOutputStream zipOutput, byte[] d, String entryName) throws Exception
   {
      ZipEntry entry = new ZipEntry(entryName);
      zipOutput.putNextEntry(entry);
      if (d != null && d.length > 0)
         zipOutput.write(d);
      zipOutput.closeEntry();
      return zipOutput;
   }

   public void extractFromArchive(File input, String output) throws Exception
   {
      BufferedOutputStream dest = null;
      FileInputStream fileInput = new FileInputStream(input);
      ZipInputStream zipInput = new ZipInputStream(new BufferedInputStream(fileInput));
      ZipEntry entry;
      int count;
      FileOutputStream fileOuput = null;
      while ((entry = zipInput.getNextEntry()) != null)
      {
         if (entry.isDirectory())
            createFile(new File(output + entry.getName()), true);
         else
         {
            if (output != null)
               fileOuput = new FileOutputStream(createFile(new File(output + entry.getName()), false));
            else
               fileOuput = new FileOutputStream(createFile(new File(entry.getName()), false));
            dest = new BufferedOutputStream(fileOuput, BUFFER);
            while ((count = zipInput.read(data, 0, BUFFER)) != -1)
               dest.write(data, 0, count);
            dest.close();
         }
      }
      zipInput.close();
   }

   private File createFile(File file, boolean folder) throws Exception
   {
      if (file.getParentFile() != null)
         createFile(file.getParentFile(), true);
      if (file.exists())
         return file;
      if (file.isDirectory() || folder)
         file.mkdir();
      else
         file.createNewFile();
      return file;
   }

   private List<File> listFile(File dir)
   {
      final List<File> list = new ArrayList<File>();
      if (dir.isFile())
      {
         list.add(dir);
         return list;
      }
      dir.listFiles(new FileFilter()
      {
         public boolean accept(File f)
         {
            if (f.isDirectory())
               list.addAll(listFile(f));
            list.add(f);
            return true;
         }
      });
      return list;
   }
}
