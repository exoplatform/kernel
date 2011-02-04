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

   public void testGetMimeType()
   {
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("file.unknown-file-extension"));
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType("unknown-file-extension"));
      assertEquals(resolver.getDefaultMimeType(), resolver.getMimeType(""));

      // there are two MIMETypes for jpeg extension [image/jpeg, image/pjpeg], check the first one
      assertEquals("image/jpeg", resolver.getMimeType("image.jpeg"));
      assertEquals("image/jpeg", resolver.getMimeType("jpeg"));

      assertEquals("application/vnd.ms-outlook", resolver.getMimeType("my.msg"));
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
}
