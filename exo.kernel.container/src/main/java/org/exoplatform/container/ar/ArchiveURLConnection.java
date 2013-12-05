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
package org.exoplatform.container.ar;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * This implementation of an {@link URLConnection} allows to get a content from an archive even if
 * the archive is not a zip file but simply a folder.
 *
 * <p>The syntax of a AR URL is:
 *
 * <pre>
 * ar:&lt;path&gt;(!/{entry}(/!{sub-entry}))
 * </pre>
 *
 * <p>for example:
 *
 * <p><code>
 * ar:/path/to/my/archive.zip<br>
 * </code>
 *
 * <p><code>
 * ar:/path/to/my/archive.zip!/path/to/my/file<br>
 * </code>
 *
 * <p><code>
 * ar:/path/to/my/archive.zip!/path/to/my/archive2.zip!/path/to/my/file<br>
 * </code>
 *
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
class ArchiveURLConnection extends URLConnection
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.ArchiveURLConnection");

   /**
    * In case it is simply a direct file access
    */
   private File directAccess;

   /**
    * The zip file corresponding to the root archive
    */
   private ZipFile zipFile;

   /**
    * The list containing all the name entries
    */
   private List<String> nameEntries;

   /**
    * @param url
    */
   ArchiveURLConnection(URL url)
   {
      super(url);
   }

   /**
    * @see java.net.URLConnection#connect()
    */
   @Override
   public void connect() throws IOException
   {
      this.connected = true;
      String file = url.getFile();
      int index = file.indexOf("!/");
      if (index == -1)
      {
         // It is a direct file access
         this.directAccess = new File(file);
         if (!directAccess.exists())
            throw new FileNotFoundException("Cannot find the file at " + file);
         if (directAccess.isDirectory())
            throw new IOException("A file was expected at " + file);
         return;
      }
      File f = new File(file.substring(0, index));
      if (!f.exists())
         throw new FileNotFoundException("Cannot find the file at " + f.getAbsolutePath());
      if (f.isDirectory())
         throw new IOException("A zip file was expected at " + f.getAbsolutePath());
      this.zipFile = new ZipFile(f);
      try
      {
         int fromIndex = index + 2;
         index = file.indexOf("!/", fromIndex);
         String nameEntry;
         if (index == -1)
            nameEntry = file.substring(fromIndex);
         else
            nameEntry = file.substring(fromIndex, index);

         ZipEntry entry = zipFile.getEntry(nameEntry);
         if (entry == null)
            throw new FileNotFoundException("Cannot find the file at "
               + file.substring(0, index == -1 ? file.length() : index));
         if (zipFile.getEntry(nameEntry + "/") != null)
            throw new IOException("A " + (index == -1 ? "" : "zip") + " file was expected at "
               + file.substring(0, index == -1 ? file.length() : index));
         nameEntries = new ArrayList<String>();
         nameEntries.add(nameEntry);
         if (index == -1)
         {
            // there is no more entries
            return;
         }
         nameEntry = file.substring(index + 2);
         // We add it without checking if it exists to avoid reading twice the ZipInputStream
         nameEntries.add(nameEntry);
      }
      catch (IOException e)
      {
         try
         {
            zipFile.close();
         }
         catch (IOException e2)
         {
            LOG.debug("Could not close the zip file");
         }
         throw e;
      }
      catch (RuntimeException e)
      {
         try
         {
            zipFile.close();
         }
         catch (IOException e2)
         {
            LOG.debug("Could not close the zip file");
         }
         throw e;
      }
   }

   /**
    * @see java.net.URLConnection#getContentLength()
    */
   @Override
   public int getContentLength()
   {
      if (connected)
      {
         if (directAccess != null)
            return (int)directAccess.length();
         if (zipFile != null && nameEntries != null && nameEntries.size() == 1)
         {
            ZipEntry entry = zipFile.getEntry(nameEntries.get(0));
            if (entry != null)
               return (int)entry.getSize();
         }
      }
      return -1;
   }

   /**
    * @see java.net.URLConnection#getInputStream()
    */
   @Override
   public InputStream getInputStream() throws IOException
   {
      if (!connected)
         connect();
      if (directAccess != null)
         return new FileInputStream(directAccess);
      if (zipFile == null)
         throw new IOException("no zip file defined");
      try
      {
         if (nameEntries == null || nameEntries.isEmpty())
            throw new IOException("no entry name specified");
         ZipEntry entry = zipFile.getEntry(nameEntries.get(0));
         if (entry == null)
            throw new FileNotFoundException("The entry " + nameEntries.get(0) + " could not be found");
         if (nameEntries.size() == 1)
         {
            return new ArchiveInputStream(zipFile.getInputStream(entry));
         }
         String nameEntry = nameEntries.get(1);
         ZipInputStream zis = new ZipInputStream(zipFile.getInputStream(entry));
         boolean closeZis = true;
         try
         {
            ZipEntry subZP;
            ZipEntry found = null;
            while ((subZP = zis.getNextEntry()) != null)
            {
               if (subZP.getName().equals(nameEntry) || subZP.getName().equals(nameEntry + "/"))
               {
                  found = subZP;
                  break;
               }
            }
            if (found == null)
               throw new FileNotFoundException("Cannot find the file at " + url.getFile());
            if (found.isDirectory())
               throw new IOException("A file was expected at " + url.getFile());
            closeZis = false;
            return new ArchiveInputStream(zis);
         }
         finally
         {
            if (closeZis)
            {
               try
               {
                  zis.close();
               }
               catch (IOException e)
               {
                  LOG.debug("Could not close the zip input stream");
               }
            }
         }
      }
      catch (IOException e)
      {
         try
         {
            zipFile.close();
         }
         catch (IOException e2)
         {
            LOG.debug("Could not close the zip file");
         }
         throw e;
      }
      catch (RuntimeException e)
      {
         try
         {
            zipFile.close();
         }
         catch (IOException e2)
         {
            LOG.debug("Could not close the zip file");
         }
         throw e;
      }
   }

   /**
    * This class is needed to properly close the {@link ZipFile} when we close the stream
    */
   private class ArchiveInputStream extends FilterInputStream
   {

      protected ArchiveInputStream(InputStream in)
      {
         super(in);
      }

      @Override
      public void close() throws IOException
      {
         try
         {
            super.close();
         }
         finally
         {
            try
            {
               zipFile.close();
            }
            catch (IOException e2)
            {
               LOG.debug("Could not close the zip file");
            }
         }
      }
   }
}
