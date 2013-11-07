/*
 * Copyright (C) 2010 eXo Platform SAS.
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
package org.exoplatform.container.util;

import junit.framework.TestCase;

import org.exoplatform.container.configuration.ConfigurationManagerImpl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: TestTemplateHelper.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class TestTemplateConfigurationHelper extends TestCase
{

   public void testFilters()
   {
      // create helper with predefined include and exclude patterns
      TemplateConfigurationHelper helper =
         new TemplateConfigurationHelper(new String[]{"^foo-.*", "^jgroups-configuration"},
            new String[]{"^foo-configuration"}, new ConfigurationManagerImpl());
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("foo-configuration", "");
      parameters.put("foo-cache.loader", "");
      parameters.put("foo-clustername", "");
      parameters.put("max-volatile-size", "");
      Map<String, String> preparedParameters = helper.prepareParameters(parameters);
      assertEquals(2, preparedParameters.size());
      // "foo-configuration" and "max-volatile-size" should be excluded
      assertFalse(preparedParameters.containsKey("${foo-configuration}"));
      assertFalse(preparedParameters.containsKey("${max-volatile-size}"));
      assertTrue(preparedParameters.containsKey("${foo-cache.loader}"));
      assertTrue(preparedParameters.containsKey("${foo-clustername}"));
   }

   public void testFilters2()
   {
      // create helper with predefined include and exclude patterns
      TemplateConfigurationHelper helper =
         new TemplateConfigurationHelper(new String[]{"^foo-.*", "^jgroups-configuration"},
            new String[]{"^foo-configuration"}, new ConfigurationManagerImpl());
      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("jgroups-configuration", "");
      parameters.put("foo-cache.loader", "");
      parameters.put("foo-clustername", "");
      parameters.put("max-volatile-size", "");
      Map<String, String> preparedParameters = helper.prepareParameters(parameters);
      assertEquals(3, preparedParameters.size());
      // "foo-configuration" and "max-volatile-size" should be excluded
      assertFalse(preparedParameters.containsKey("${max-volatile-size}"));
      assertTrue(preparedParameters.containsKey("${foo-cache.loader}"));
      assertTrue(preparedParameters.containsKey("${foo-clustername}"));
   }

   public void testTemplating() throws IOException
   {
      TemplateConfigurationHelper helper =
         new TemplateConfigurationHelper(new String[]{"^foo-.*", "^jgroups-configuration"},
            new String[]{"^foo-configuration"}, new ConfigurationManagerImpl());
      String template = "configuration in any format, containing ${foo-template-variable} and many others";
      String expectedConfig = "configuration in any format, containing pretty good parameter and many others";

      InputStream templateStream = new ByteArrayInputStream(template.getBytes());

      Map<String, String> parameters = new HashMap<String, String>();
      parameters.put("foo-template-variable", "pretty good parameter");

      InputStream configStream = helper.fillTemplate(templateStream, parameters);
      String config = Utils.readStream(configStream);
      assertTrue(expectedConfig.equals(config));
   }
}
