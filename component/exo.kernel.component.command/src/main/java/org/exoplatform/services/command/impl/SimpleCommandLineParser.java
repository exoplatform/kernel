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
package org.exoplatform.services.command.impl;

import org.apache.commons.chain.Context;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class SimpleCommandLineParser implements CommandLineParser
{

   protected final String parametersPropertyName;

   public SimpleCommandLineParser(String parametersPropertyName)
   {
      this.parametersPropertyName = parametersPropertyName;
   }

   /*
    * (non-Javadoc)
    * @see
    * org.exoplatform.services.command.impl.CommandLineParser#parse(java.lang
    * .String, org.apache.commons.chain.Context)
    */
   public String parse(String commandLine, Context context)
   {

      context.remove(parametersPropertyName);

      // TODO make regexp parser
      // the rules:
      // first word is command name (should be returned)
      // else are parameters of command (should be put into Context under name ==
      // parametersPropertyName as array of Strings)
      // mind <space> contained string parameters - should be quoted (" or ')
      // /////////////////////

      StringTokenizer parser = new StringTokenizer(commandLine);
      String commandName = null;
      List<String> params = new ArrayList<String>();

      while (parser.hasMoreTokens())
      {
         String str = parser.nextToken();
         if (commandName == null)
            commandName = str;
         else
            params.add(str);
      }
      // //////////////////////
      context.put(parametersPropertyName, params);
      return commandName;
   }

   /**
    * @return parameters Property Name
    */
   public String getParametersPropertyName()
   {
      return parametersPropertyName;
   }

}
