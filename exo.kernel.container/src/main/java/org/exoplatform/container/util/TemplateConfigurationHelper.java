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

import org.exoplatform.commons.utils.PrivilegedFileHelper;
import org.exoplatform.container.configuration.ConfigurationManager;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

/**
 * Builds configuration from template using map of template-variables <--> value.
 * Class provides extra functionality for filtering parameters by pattern, excluding 
 * unnecessary parameters. 
 * 
 * @author <a href="mailto:nikolazius@gmail.com">Nikolay Zamosenchuk</a>
 * @version $Id: TemplateConfigurationHelper.java 34360 2009-07-22 23:58:59Z nzamosenchuk $
 *
 */
public class TemplateConfigurationHelper
{

   private static final Log LOG = ExoLogger.getLogger("exo.kernel.container.TemplateConfigurationHelper");

   // list with include-patterns
   private List<Pattern> includes = new ArrayList<Pattern>();

   // list with exclude-patterns
   private List<Pattern> excludes = new ArrayList<Pattern>();

   private ConfigurationManager cfm;

   /**
    * Creates instance of template configuration helper with given lists of filtering 
    * patterns. Parameter will be included only if it matches any include-pattern and
    * doesn't match any exclude-pattern. I.e. You can include "extended-*" and exclude
    * "extended-type". Please refer to Java regexp documentation. Filtering for this
    * example, should be defined as following:
    * include: "^extended-.*"
    * exclude: "^extended-type"
    * 
    * @param includes Array with string representation of include reg-exp patterns
    * @param excludes Array with string representation of exclude reg-exp patterns
    * @param ConfigurationManager instance for looking up resources
    */
   public TemplateConfigurationHelper(String[] includes, String[] excludes, ConfigurationManager cfm)
   {
      super();
      this.cfm = cfm;
      // compile include patterns
      for (String regex : includes)
      {
         this.includes.add(Pattern.compile(regex));
      }
      // compile exclude patterns
      for (String regex : excludes)
      {
         this.excludes.add(Pattern.compile(regex));
      }
   }

   /**
    * Reads configuration file from a stream and replaces all the occurrences of template-variables 
    * (like : "${parameter.name}") with values provided in the map. 
    * 
    * @param inputStream
    * @param parameters
    * @return
    * @throws IOException
    */
   public InputStream fillTemplate(InputStream inputStream, Map<String, String> parameters) throws IOException
   {
      if (inputStream == null || parameters == null || parameters.size() == 0)
      {
         return inputStream;
      }
      // parameters filtering
      Map<String, String> preparedParams = prepareParameters(parameters);
      // read stream
      String configuration = Utils.readStream(inputStream);
      for (Entry<String, String> entry : preparedParams.entrySet())
      {
         configuration = configuration.replace(entry.getKey(), entry.getValue());
      }
      // create new stream
      InputStream configurationStream = new ByteArrayInputStream(configuration.getBytes());
      return configurationStream;
   }

   /**
    * Reads configuration file from a stream and replaces all the occurrences of template-variables 
    * (like : "${parameter.name}") with values provided in the map. 
    * 
    * @param filename
    * @param parameters
    * @return
    * @throws IOException
    */
   public InputStream fillTemplate(String filename, Map<String, String> parameters) throws IOException
   {
      InputStream inputStream = getInputStream(cfm, filename);
      // inputStream still remains null, so file was not opened
      if (inputStream == null)
      {
         throw new IOException("Can't find or open file:" + filename);
      }
      return fillTemplate(inputStream, parameters);
   }
   
   /**
    * Tries first to get the file content using the configuration manager, if it cannot
    * be found it will then try to get it from the context class loader of the current thread,
    * if it cannot be found it will try to get it from the class loader of the current class and
    * finally it still cannot be found it will try to use the file name as a file path.
    * @param cfm the configuration manager from which we want to try to find the file content
    * @param filename the name of the file to found
    * @return the {@link InputStream} corresponding to the file content if it can be found
    * <code>null</code> otherwise
    */
   public static InputStream getInputStream(ConfigurationManager cfm, String filename)
   {
      InputStream inputStream = null;
      // try to get using configuration manager
      try
      {
         inputStream = cfm.getInputStream(filename);
      }
      catch (Exception e)
      {
         if (LOG.isTraceEnabled())
         {
            LOG.trace("An exception occurred: " + e.getMessage());
         }
      }

      // try to get resource by class loader
      if (inputStream == null)
      {
         ClassLoader cl = Thread.currentThread().getContextClassLoader();
         inputStream = cl == null ? null : cl.getResourceAsStream(filename);
      }

      // check system class loader
      if (inputStream == null)
      {
         inputStream = TemplateConfigurationHelper.class.getClassLoader().getResourceAsStream(filename);
      }

      // try to get as file stream
      if (inputStream == null)
      {
         try
         {
            inputStream = PrivilegedFileHelper.fileInputStream(filename);
         }
         catch (IOException e)
         {
            if (LOG.isTraceEnabled())
            {
               LOG.trace("An exception occurred: " + e.getMessage());
            }
         }
      }
      return inputStream;
   }
   
   /**
    * Checks if String mathes to any pattern from the list
    * 
    * @param patterns
    * @param parameter
    * @return
    */
   private boolean matches(List<Pattern> patterns, String parameter)
   {
      for (Pattern pattern : patterns)
      {
         if (pattern.matcher(parameter).matches())
         {
            // string matched
            return true;
         }
      }
      return false;
   }

   /**
    * Filters the map of parameters, leaving only those than matches filtering regular expressions.
    * Also adds "${}" to the parameter key: <br>
    * I.e. such map provided on input:
    * 
    * "foo-cache.loader":"org.exoplatform"
    * "foo-configuration":"/conf/test.xml"
    * "max-volatile-size":"100Kb"
    * 
    * the output will be like:
    * 
    * "${foo-cache.loader}":"org.exoplatform"
    * 
    * Other will be ignored (depending on includes/excludes lists provided in constructor).
    * 
    * @param parameters
    * @return
    */
   protected Map<String, String> prepareParameters(Map<String, String> parameters)
   {
      Map<String, String> map = new HashMap<String, String>();
      for (Entry<String, String> entry : parameters.entrySet())
      {
         if (matches(includes, entry.getKey()) && !matches(excludes, entry.getKey()))
         {
            map.put("${" + entry.getKey() + "}", entry.getValue());
         }
      }
      return map;
   }
}
