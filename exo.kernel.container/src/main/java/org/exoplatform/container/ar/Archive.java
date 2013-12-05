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

import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.Deserializer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedExceptionAction;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Defines an archive with all its properties
 * 
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class Archive
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.Archive");

   /**
    * The specific handler for the archive {@link URL} 
    */
   private static final ArchiveURLStreamHandler HANDLER = new ArchiveURLStreamHandler();

   /**
    * The protocol used to define an URL that defines a resource into an archive
    */
   public static final String PROTOCOL = "ar";

   /**
    * The most common description of a Web Application ARchive
    */
   public static final Archive WAR = new Archive("war", false, true, null);

   /**
    * The most common description of a Enterprise Application ARchive
    */
   public static final Archive EAR = new Archive("ear", false, true, Collections.singleton(WAR));

   /**
    * The type of the archive
    */
   private final String type;

   /**
    * Indicates whether the archive is replaceable with a directory without extension
    */
   private final boolean useDirWoExt;

   /**
    * Indicates whether the archive can be a directory
    */
   private final boolean allowsDir;

   /**
    * The archives that can be included in the current archive
    */
   private final Set<Archive> subArchives;

   /**
    * The default constructor
    */
   public Archive(String type, boolean useDirWoExt, boolean allowsDir, Set<Archive> subArchives)
   {
      this.type = type;
      this.useDirWoExt = useDirWoExt;
      this.allowsDir = allowsDir;
      this.subArchives = subArchives;
   }

   /**
    * @return the type of the archive
    */
   public String getType()
   {
      return type;
   }

   /**
    * Indicates whether the archive is replaceable with a directory without extension
    */
   public boolean isUseDirWoExt()
   {
      return useDirWoExt;
   }

   /**
    * Indicates whether the archive can be a directory
    */
   public boolean isAllowsDir()
   {
      return allowsDir;
   }

   /**
    * @return the archives that can be included in the current archive
    */
   public Set<Archive> getSubArchives()
   {
      return subArchives;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((type == null) ? 0 : type.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Archive other = (Archive)obj;
      if (type == null)
      {
         if (other.type != null)
            return false;
      }
      else if (!type.equals(other.type))
         return false;
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString()
   {
      return "Archive [type=" + type + ", useDirWoExt=" + useDirWoExt + ", allowsDir=" + allowsDir + ", subArchives="
         + subArchives + "]";
   }

   /**
    * Gives a Collection of URL corresponding to the configuration files that could be found under the given directories
    * inside archives with the given suffixes
    * @param appDeployDirectories the list of directory to scan
    * @param appDeployArchives the list of archives to scan
    * @param configuration the relative path to the configuration file
    * @return the URL of the configuration files that could be found
    * @throws IOException If we cannot access to the content of the archives for some reasons
    */
   public static Collection<URL> getConfigurationURL(List<String> appDeployDirectories,
      Set<Archive> appDeployArchives, String configuration) throws IOException
   {
      if (configuration == null || configuration.isEmpty())
         throw new IllegalArgumentException("The path to the configuration cannot be empty");
      if (appDeployDirectories == null || appDeployDirectories.isEmpty() || appDeployArchives == null || appDeployArchives.isEmpty())
         return Collections.emptyList();
      Collection<URL> result = new LinkedHashSet<URL>();
      for (String directory : appDeployDirectories)
      {
         File dir = new File(directory);
         if (!dir.exists())
         {
            LOG.debug("The directory {} doesn't exist", directory);
            continue;
         }
         result.addAll(getConfigurationURL(dir, appDeployArchives, configuration, true));
      }
      return result;
   }

   /**
    * Gives a Collection of URL corresponding to the configuration files that could be found under the given directory
    * inside archives with the given suffixes
    * @param dir the directory to scan
    * @param appDeployArchives the list of archives to scan
    * @param configuration the relative path to the configuration file
    * @param enableRecursion enable the recursion
    * @return the URL of the configuration files that could be found
    * @throws IOException If we cannot access to the content of the archives for some reasons
    */
   private static Collection<URL> getConfigurationURL(File dir, final Set<Archive> appDeployArchives,
      String configuration, final boolean enableRecursion) throws IOException
   {
      final Map<String, Archive> types = new HashMap<String, Archive>();
      for (Archive archive : appDeployArchives)
      {
         types.put(archive.getType(), archive);
      }
      Collection<URL> result = new LinkedHashSet<URL>();
      File[] files = dir.listFiles(new FilenameFilter()
      {
         public boolean accept(File dir, String name)
         {
            Archive extension = null;
            int index = name.lastIndexOf('.');
            if (index != -1)
            {
               String ext = name.substring(index + 1).toLowerCase();
               extension = types.get(ext);
            }
            File f = new File(dir, name);
            if (f.isDirectory())
            {
               if (extension == null)
               {
                  if (!enableRecursion)
                  {
                     return false;
                  }
                  for (Archive archive : appDeployArchives)
                  {
                     if (archive.isUseDirWoExt())
                     {
                        return true;
                     }
                  }
               }
               else if (extension.isAllowsDir())
               {
                  return true;
               }
            }
            else if (extension != null)
            {
               if (!extension.isUseDirWoExt())
               {
                  return true;
               }
               File file = new File(dir, name.substring(0, name.length() - extension.getType().length() - 1));
               return !file.exists() || !file.isDirectory();
            }
            return false;
         }
      });
      Arrays.sort(files);
      for (File file : files)
      {
         if (file.isDirectory())
         {
            File f = new File(file, configuration);
            if (f.exists() && f.isFile())
            {
               result.add(parse(f.getAbsolutePath()));
            }
            else
            {
               int index = file.getName().lastIndexOf('.');
               Archive extension = null;
               if (index == -1)
               {
                  if (!enableRecursion)
                  {
                     continue;
                  }
                  for (Archive archive : appDeployArchives)
                  {
                     if (archive.isUseDirWoExt())
                     {
                        extension = archive;
                        break;
                     }
                  }
               }
               else
               {
                  String ext = file.getName().substring(index + 1).toLowerCase();
                  extension = types.get(ext);
               }
               if (extension == null || extension.getSubArchives() == null || extension.getSubArchives().isEmpty())
               {
                  continue;
               }
               result.addAll(getConfigurationURL(file, extension.getSubArchives(), configuration, false));
            }
         }
         else
         {
            ZipFile f = new ZipFile(file);
            try
            {
               ZipEntry entry = f.getEntry(configuration);
               if (entry != null && f.getEntry(configuration + "/") == null)
               {
                  result.add(parse(file.getAbsolutePath() + "!/" + configuration));
               }
               else
               {
                  int indexExt = file.getName().lastIndexOf('.');
                  if (indexExt == -1)
                     throw new IllegalStateException("Cannot find the extension of the file " + file.getAbsolutePath());
                  String extFile = file.getName().substring(indexExt + 1).toLowerCase();
                  if (extFile == null)
                     throw new IllegalStateException("Cannot find the extension of the file " + file.getAbsolutePath());
                  Archive extension = types.get(extFile);
                  if (extension == null)
                     throw new IllegalStateException("Cannot find the archive corresponding to the file "
                        + file.getAbsolutePath());
                  if (extension.getSubArchives() == null || extension.getSubArchives().isEmpty())
                     continue;
                  final Map<String, Archive> subTypes = new HashMap<String, Archive>();
                  for (Archive archive : extension.getSubArchives())
                  {
                     subTypes.put(archive.getType(), archive);
                  }
                  Enumeration<? extends ZipEntry> entries = f.entries();
                  Map<String, Archive> names = new TreeMap<String, Archive>();
                  while (entries.hasMoreElements())
                  {
                     ZipEntry ze = entries.nextElement();
                     String name = ze.getName();
                     int index = name.indexOf('/');
                     if (index > 0)
                     {
                        if (index < name.length() - 1)
                        {
                           // skip sub directories and files
                           continue;
                        }
                        name = name.substring(0, index);
                     }
                     index = name.lastIndexOf('.');
                     if (index == -1)
                     {
                        // We skip files and directories without extension
                     }
                     String ext = name.substring(index + 1).toLowerCase();
                     if (subTypes.containsKey(ext))
                     {
                        names.put(ze.getName(), subTypes.get(ext));
                     }
                  }
                  for (String name : names.keySet())
                  {
                     Archive a = names.get(name);
                     ZipEntry ze = f.getEntry(name);
                     if (ze.isDirectory())
                     {
                        if (!a.isAllowsDir())
                        {
                           continue;
                        }
                        ze = f.getEntry(name + configuration);
                        if (ze != null && f.getEntry(name + configuration + "/") == null)
                        {
                           result.add(parse(file.getAbsolutePath() + "!/" + name + configuration));
                        }
                     }
                     else
                     {
                        ZipInputStream zis = new ZipInputStream(f.getInputStream(ze));
                        try
                        {
                           ZipEntry subZP;
                           while ((subZP = zis.getNextEntry()) != null)
                           {
                              if (!subZP.isDirectory() && subZP.getName().equals(configuration))
                              {
                                 result.add(parse(file.getAbsolutePath() + "!/" + name + "!/" + configuration));
                                 break;
                              }
                           }
                        }
                        finally
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
               }
            }
            finally
            {
               try
               {
                  f.close();
               }
               catch (IOException e)
               {
                  LOG.debug("Could not close the zip file");
               }
            }
         }
      }
      return result;
   }

   /**
    * Creates an archive URL from a String representation of that URL
    * @param url the String representation
    * @return the corresponding {@link URL}
    * @throws MalformedURLException If the URL is incorrect
    */
   public static URL createArchiveURL(String url) throws MalformedURLException
   {
      url = Deserializer.resolveVariables(url);
      // we ensure that we don't have windows path separator in the url
      url = url.replace('\\', '/');
      final String sUrl = url;
      return SecurityHelper.doPrivilegedMalformedURLExceptionAction(new PrivilegedExceptionAction<URL>()
      {
         public URL run() throws Exception
         {
            return new URL(null, sUrl, HANDLER);
         }
      });
   }

   /**
    * Indicates whether or not the provided URL is an archive URL
    * @param url the String representation of the URL to check
    * @return <code>true</code> if it is an archive URL, false otherwise
    */
   public static boolean isArchiveURL(String url)
   {
      return url.startsWith("ar:");
   }

   /**
    * Converts a string to an archive {@link URL}
    * @param path2Convert the path to convert into an archive {@link URL}
    * @return an archive {@link URL}
    * @throws MalformedURLException if the {@link URL} could not be created
    */
   static URL parse(String path2Convert) throws MalformedURLException
   {
      if (File.separatorChar != '/')
         path2Convert = path2Convert.replace(File.separatorChar, '/');
      if (!path2Convert.startsWith("/"))
         path2Convert = "/" + path2Convert;
      return new URL(Archive.PROTOCOL, null, -1, path2Convert, Archive.HANDLER);
   }
}
