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
package org.exoplatform.commons.utils;

import junit.framework.TestCase;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by The eXo Platform SAS.
 *
 * Date: 3 02 2011
 * 
 * @author <a href="mailto:anatoliy.bazko@exoplatform.com.ua">Anatoliy Bazko</a>
 * @version $Id: TestMimeTypeResolver.java 34360 2010-11-11 11:11:11Z tolusha $
 */
public class TestMimeTypeResolver extends TestCase
{

   private MimeTypeResolver resolver = new MimeTypeResolver();

   /**
    * Here we're going to test MimeTypeResolver to obtain corresponding or 
    * at least most corresponding mime types for files with extensions.
    */
   public void testGetMimeTypeFromExtension()
   {
      // should return default mime type for unknown extension
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("file.unknown-file-extension"));

      // shoud return mime type based on last part separated by "." symbol
      // i. e. should return corresponding mime type for "unknown-file-extension"
      // but not for "pdf"
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("file.pdf.unknown-file-extension"));

      assertEquals("application/vnd.ms-outlook", resolver.getMimeType("my.msg"));
      assertEquals("application/msword", resolver.getMimeType("my.doc"));
      assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
         resolver.getMimeType("my.docx"));
      assertEquals("application/xls", resolver.getMimeType("my.xls"));
      assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", resolver.getMimeType("my.xlsx"));
      assertEquals("application/pdf", resolver.getMimeType("my.pdf"));
      assertEquals("image/jpeg", resolver.getMimeType("my.jpg"));
      assertEquals("application/vnd.oasis.opendocument.text", resolver.getMimeType("my.odt"));

   }

   /**
    * Here we're going to test MimeTypeResolver to obtain corresponding or 
    * at least most corresponding mime types for files without extensions.
    */
   public void testGetMimeTypeFromContent() throws IOException
   {
      // should return default mime type as file name has no extension
      // and file does not exist to read its content
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("unknown-file-extension"));

      // should return default mime type as file name has no extension
      // (though it has "." its extension is empty string == no extension)
      // and file does not exist to read its content
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("file."));

      InputStream is;

      is = TestMimeTypeResolver.class.getResourceAsStream("/testjpg");
      assertEquals("image/jpeg", resolver.getMimeType("testjpg", is));
      is.close();
      is = TestMimeTypeResolver.class.getResourceAsStream("/testpdf");
      assertEquals("application/pdf", resolver.getMimeType("testpdf", is));
      is.close();
      is = TestMimeTypeResolver.class.getResourceAsStream("/testdoc");
      assertEquals("application/msword", resolver.getMimeType("testdoc", is));
      is.close();
      is = TestMimeTypeResolver.class.getResourceAsStream("/testxml");
      assertEquals("text/xml", resolver.getMimeType("testxml", is));
      is.close();
      is = TestMimeTypeResolver.class.getResourceAsStream("/testxls");
      assertEquals("application/msword", resolver.getMimeType("testxls", is));
      is.close();

   }

   public void testGetExtension()
   {
      assertEquals("jpeg", resolver.getExtension("image/jpeg"));
      assertEquals("jpeg", resolver.getExtension("image/pjpeg"));

      assertEquals("", resolver.getExtension(""));
      assertEquals("", resolver.getExtension("unknown-mimetype"));
      assertEquals("", resolver.getExtension(resolver.getDefaultMimeType()));

      assertEquals("msg", resolver.getExtension("application/vnd.ms-outlook"));

      // there are two file extension for audio/midi MIMEType [mid, midi]
      // should be returned "midi" as MIMEType ends with "midi"
      assertEquals("midi", resolver.getExtension("audio/midi"));

      // there are two file extension for application/x-director MIMEType [dcr, dir]
      // should be returned "dcr" as first occurred
      assertEquals("dcr", resolver.getExtension("application/x-director"));
   }

   public void testGetMimeTypeFromExtensionInUpperCase()
   {
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("FILE.UNKNOWN-FILE-EXTENSION"));
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("FILE.PDF.UNKNOWN-FILE-EXTENSION"));
      assertEquals("application/vnd.ms-outlook", resolver.getMimeType("MY.MSG"));
      assertEquals("application/msword", resolver.getMimeType("MY.DOC"));
      assertEquals("application/vnd.openxmlformats-officedocument.wordprocessingml.document",
         resolver.getMimeType("MY.DOCX"));
      assertEquals("application/xls", resolver.getMimeType("MY.XLS"));
      assertEquals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", resolver.getMimeType("MY.XLSX"));
      assertEquals("application/pdf", resolver.getMimeType("MY.PDF"));
      assertEquals("image/jpeg", resolver.getMimeType("MY.JPG"));
      assertEquals("application/vnd.oasis.opendocument.text", resolver.getMimeType("MY.ODT"));

   }

}
