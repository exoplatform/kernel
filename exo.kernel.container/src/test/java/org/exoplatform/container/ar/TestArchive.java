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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestArchive
{

   @Test
   public void testGetConfigurationURL() throws Exception
   {
      try
      {
         Archive.getConfigurationURL(null, null, null);
         fail("An IllegalArgumentException was expected");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
      String configuration = "foo/config.xml";
      URL urlArchives = getClass().getResource(".");
      String path = new File(urlArchives.toURI()).getAbsolutePath();
      Archive zip = new Archive("zip", true, true, Collections.singleton(new Archive("zip", true, true, null)));
      assertTrue(Archive.getConfigurationURL(null, null, configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(null, Collections.<Archive> emptySet(), configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(null, Collections.singleton(zip), configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(Collections.<String> emptyList(), null, configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(Collections.<String> emptyList(), Collections.<Archive> emptySet(),
         configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(Collections.<String> emptyList(), Collections.singleton(zip),
         configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(null, null, configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(null, Collections.<Archive> emptySet(), configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(Collections.singletonList(path), null, configuration).isEmpty());
      assertTrue(Archive.getConfigurationURL(Collections.singletonList(path), Collections.<Archive> emptySet(),
         configuration).isEmpty());
      Collection<URL> result =
         Archive.getConfigurationURL(Collections.singletonList(path), Collections.singleton(zip), configuration);
      String[] aResult1 =
         {"ar-with-dir-and-ar-with-config-dir.zip!/ar-without-dir.zip!/foo/config.xml",
            "ar-with-dir-and-ar-with-config-dir.zip!/dir-with-ext.zip/foo/config.xml",
            "ar-with-dir-and-ar-with-config-file.zip!/foo/config.xml",
            "ar-with-dir-and-ar.zip!/ar-without-dir.zip!/foo/config.xml",
            "ar-with-dir-and-ar.zip!/dir-with-ext.zip/foo/config.xml", "ar-without-dir.zip!/foo/config.xml",
            "dir-with-dir-and-ar-with-config-dir.zip/ar-without-dir.zip!/foo/config.xml",
            "dir-with-dir-and-ar-with-config-dir.zip/dir-with-ext.zip/foo/config.xml",
            "dir-with-dir-and-ar-with-config-file.zip/foo/config.xml",
            "dir-with-dir-and-ar.zip/ar-without-dir.zip!/foo/config.xml",
            "dir-with-dir-and-ar.zip/dir-with-ext.zip/foo/config.xml", "dir-with-ext.zip/foo/config.xml",
            "dir-without-ext/foo/config.xml", "dir-without-ext-with-ar/foo/config.xml"};
      assertEquals(aResult1.length, result.size());
      Iterator<URL> it = result.iterator();
      for (int i = 0; it.hasNext(); i++)
      {
         String p = it.next().toString();
         assertTrue(p + " doesn't end with " + aResult1[i], p.endsWith(aResult1[i]));
      }

      zip = new Archive("zip", false, true, Collections.singleton(new Archive("zip", false, true, null)));
      result = Archive.getConfigurationURL(Collections.singletonList(path), Collections.singleton(zip), configuration);
      String[] aResult2 =
         {"ar-with-dir-and-ar-with-config-dir.zip!/ar-without-dir.zip!/foo/config.xml",
            "ar-with-dir-and-ar-with-config-dir.zip!/dir-with-ext.zip/foo/config.xml",
            "ar-with-dir-and-ar-with-config-file.zip!/foo/config.xml",
            "ar-with-dir-and-ar.zip!/ar-without-dir.zip!/foo/config.xml",
            "ar-with-dir-and-ar.zip!/dir-with-ext.zip/foo/config.xml", "ar-without-dir.zip!/foo/config.xml",
            "dir-with-dir-and-ar-with-config-dir.zip/ar-without-dir.zip!/foo/config.xml",
            "dir-with-dir-and-ar-with-config-dir.zip/dir-with-ext.zip/foo/config.xml",
            "dir-with-dir-and-ar-with-config-file.zip/foo/config.xml",
            "dir-with-dir-and-ar.zip/ar-without-dir.zip!/foo/config.xml",
            "dir-with-dir-and-ar.zip/dir-with-ext.zip/foo/config.xml", "dir-with-ext.zip/foo/config.xml",
            "dir-without-ext-with-ar.zip!/foo/config.xml"};
      assertEquals(aResult2.length, result.size());
      it = result.iterator();
      for (int i = 0; it.hasNext(); i++)
      {
         String p = it.next().toString();
         assertTrue(p + " doesn't end with " + aResult2[i], p.endsWith(aResult2[i]));
      }

      zip = new Archive("zip", false, false, Collections.singleton(new Archive("zip", false, false, null)));
      result = Archive.getConfigurationURL(Collections.singletonList(path), Collections.singleton(zip), configuration);
      String[] aResult3 =
         {"ar-with-dir-and-ar-with-config-dir.zip!/ar-without-dir.zip!/foo/config.xml",
            "ar-with-dir-and-ar-with-config-file.zip!/foo/config.xml",
            "ar-with-dir-and-ar.zip!/ar-without-dir.zip!/foo/config.xml", "ar-without-dir.zip!/foo/config.xml",
            "dir-without-ext-with-ar.zip!/foo/config.xml"};
      assertEquals(aResult3.length, result.size());
      it = result.iterator();
      for (int i = 0; it.hasNext(); i++)
      {
         String p = it.next().toString();
         assertTrue(p + " doesn't end with " + aResult3[i], p.endsWith(aResult3[i]));
      }

      zip = new Archive("zip", false, false, null);
      result = Archive.getConfigurationURL(Collections.singletonList(path), Collections.singleton(zip), configuration);
      String[] aResult4 =
         {"ar-with-dir-and-ar-with-config-file.zip!/foo/config.xml", "ar-without-dir.zip!/foo/config.xml",
            "dir-without-ext-with-ar.zip!/foo/config.xml"};
      assertEquals(aResult4.length, result.size());
      it = result.iterator();
      for (int i = 0; it.hasNext(); i++)
      {
         String p = it.next().toString();
         assertTrue(p + " doesn't end with " + aResult4[i], p.endsWith(aResult4[i]));
      }

      zip = new Archive("zip", true, false, Collections.singleton(new Archive("zip", true, false, null)));
      result = Archive.getConfigurationURL(Collections.singletonList(path), Collections.singleton(zip), configuration);
      String[] aResult5 =
         {"ar-with-dir-and-ar-with-config-dir.zip!/ar-without-dir.zip!/foo/config.xml",
            "ar-with-dir-and-ar-with-config-file.zip!/foo/config.xml",
            "ar-with-dir-and-ar.zip!/ar-without-dir.zip!/foo/config.xml", "ar-without-dir.zip!/foo/config.xml",
            "dir-without-ext/foo/config.xml", "dir-without-ext-with-ar/foo/config.xml"};
      assertEquals(aResult5.length, result.size());
      it = result.iterator();
      for (int i = 0; it.hasNext(); i++)
      {
         String p = it.next().toString();
         assertTrue(p + " doesn't end with " + aResult5[i], p.endsWith(aResult5[i]));
      }

      zip = new Archive("zip", true, false, null);
      result = Archive.getConfigurationURL(Collections.singletonList(path), Collections.singleton(zip), configuration);
      String[] aResult6 =
         {"ar-with-dir-and-ar-with-config-file.zip!/foo/config.xml", "ar-without-dir.zip!/foo/config.xml",
            "dir-without-ext/foo/config.xml", "dir-without-ext-with-ar/foo/config.xml"};
      assertEquals(aResult6.length, result.size());
      it = result.iterator();
      for (int i = 0; it.hasNext(); i++)
      {
         String p = it.next().toString();
         assertTrue(p + " doesn't end with " + aResult6[i], p.endsWith(aResult6[i]));
      }
   }

   @Test
   public void testURL() throws Exception
   {
      URL url = new URL("file", null, "/");
      ArchiveURLStreamHandler handler = new ArchiveURLStreamHandler();
      try
      {
         handler.openConnection(url);
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = new URL(Archive.PROTOCOL, "localhost", -1, "/", handler);
      try
      {
         url.openConnection();
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = new URL(Archive.PROTOCOL, "", -1, "/", handler);
      try
      {
         url.openConnection();
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = Archive.parse("//");
      try
      {
         url.openConnection();
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = Archive.parse("/foo!/foo2!/foo3!/foo4");
      try
      {
         url.openConnection();
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = Archive.parse("/foo!/");
      try
      {
         url.openConnection();
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = Archive.parse("/foo/");
      try
      {
         url.openConnection();
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = new URL(Archive.PROTOCOL, null, -1, "foo", handler);
      try
      {
         url.openConnection();
         fail("A MalformedURLException was expected");
      }
      catch (MalformedURLException e)
      {
         // expected
      }
      url = Archive.parse("foo");
      url.openConnection();
      url = new URL(Archive.PROTOCOL, null, -1, "/foo", handler);
      url.openConnection();
      url = Archive.parse("/foo");
      url.openConnection();
   }

   @Test
   public void testConnect() throws Exception
   {
      URL url = Archive.parse("/foo");
      URLConnection connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A FileNotFoundException was expected");
      }
      catch (FileNotFoundException e)
      {
         // expected
      }
      URL urlArchives = getClass().getResource(".");
      String root = new File(urlArchives.toURI()).getAbsolutePath();
      url = Archive.parse(root);
      connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A IOException was expected");
      }
      catch (IOException e)
      {
         // expected
      }
      String path = root + "/ar-with-dir-and-ar-with-config-file.zip";
      url = Archive.parse(path);
      connection = url.openConnection();
      connection.connect();

      path = root + "/foo.zip!/foo/config.xml";
      url = Archive.parse(path);
      connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A FileNotFoundException was expected");
      }
      catch (FileNotFoundException e)
      {
         // expected
      }

      path = root + "/dir-with-dir-and-ar-with-config-file.zip!/foo/config.xml";
      url = Archive.parse(path);
      connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A IOException was expected");
      }
      catch (IOException e)
      {
         // expected
      }

      path = root + "/ar-with-dir-and-ar-with-config-file.zip!/foo/foo.xml";
      url = Archive.parse(path);
      connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A FileNotFoundException was expected");
      }
      catch (FileNotFoundException e)
      {
         // expected
      }

      path = root + "/ar-with-dir-and-ar-with-config-file.zip!/foo";
      url = Archive.parse(path);
      connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A IOException was expected");
      }
      catch (IOException e)
      {
         // expected
      }

      path = root + "/ar-with-dir-and-ar-with-config-dir.zip!/foo.zip!/foo/config.xml";
      url = Archive.parse(path);
      connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A FileNotFoundException was expected");
      }
      catch (FileNotFoundException e)
      {
         // expected
      }

      path = root + "/ar-with-dir-and-ar-with-config-dir.zip!/dir-with-ext.zip!/foo/config.xml";
      url = Archive.parse(path);
      connection = url.openConnection();
      try
      {
         connection.connect();
         fail("A IOException was expected");
      }
      catch (IOException e)
      {
         // expected
      }
   }

   @Test
   public void testGetStream() throws Exception
   {
      URL urlArchives = getClass().getResource(".");
      String root = new File(urlArchives.toURI()).getAbsolutePath();
      String path = root + "/ar-with-dir-and-ar-with-config-dir.zip!/ar-without-dir.zip!/foo/foo.xml";
      URL url = Archive.parse(path);
      try
      {
         getContent(url);
         fail("A FileNotFoundException was expected");
      }
      catch (FileNotFoundException e)
      {
         // expected
      }

      path = root + "/ar-with-dir-and-ar-with-config-dir.zip!/ar-without-dir.zip!/foo";
      url = Archive.parse(path);
      try
      {
         getContent(url);
         fail("A IOException was expected");
      }
      catch (IOException e)
      {
         // expected
      }

      path = root + "/dir-without-ext/foo/config.xml";
      url = Archive.parse(path);
      String contentRef = getContent(url);
      assertNotNull(contentRef);
      assertFalse(contentRef.isEmpty());

      path = root + "/ar-without-dir.zip!/foo/config.xml";
      url = Archive.parse(path);
      String content = getContent(url);
      assertNotNull(content);
      assertFalse(content.isEmpty());
      assertEquals(contentRef, content);

      path = root + "/ar-with-dir-and-ar-with-config-dir.zip!/ar-without-dir.zip!/foo/config.xml";
      url = Archive.parse(path);
      content = getContent(url);
      assertNotNull(content);
      assertFalse(content.isEmpty());
      assertEquals(contentRef, content);
   }

   private static String getContent(URL url) throws Exception
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      InputStream is = null;
      try
      {
         is = url.openStream();
         byte[] buffer = new byte[2048];
         int n;
         while ((n = is.read(buffer, 0, 2048)) > -1)
         {
            baos.write(buffer, 0, n);
         }
      }
      finally
      {
         if (is != null)
            is.close();
      }
      return new String(baos.toByteArray(), "ISO-8859-1");
   }
}
