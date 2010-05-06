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
package org.exoplatform.container.configuration;

import org.exoplatform.test.BasicTestCase;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:nicolas.filotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Revision$
 */
public class TestXSD_1_2 extends BasicTestCase
{

   public void testValidation() throws Exception
   {
      ConfigurationUnmarshaller unmarshaller = new ConfigurationUnmarshaller();
      String baseDirPath = System.getProperty("basedir");
      File baseDir = new File(baseDirPath + "/src/test/resources/xsd_1_2");
      int count = 0;
      for (File f : baseDir.listFiles(new FileFilter()
      {
         public boolean accept(File pathname)
         {
            return pathname.getName().endsWith(".xml");
         }
      }))
      {
         count++;
         try
         {
            URL url = f.toURI().toURL();
            assertTrue("XML configuration file " + url + " is not valid", unmarshaller.isValid(url));
         }
         catch (MalformedURLException e)
         {
            fail("Was not expecting such exception " + e.getMessage());
         }
      }
      assertEquals(19, count);
   }
}