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
package org.exoplatform.services.naming;

import org.exoplatform.container.component.BaseComponentPlugin;
import org.exoplatform.container.configuration.ConfigurationException;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;

import java.util.Iterator;
import java.util.Map.Entry;

import javax.naming.Reference;
import javax.naming.StringRefAddr;

/**
 * Created by The eXo Platform SAS.<br/> InitialContextInitializer's Component
 * Plugin for binding reference to JNDI naming context
 * 
 * @author <a href="mailto:gennady.azarenkov@exoplatform.com">Gennady
 *         Azarenkov</a>
 * @version $Id: BindReferencePlugin.java 6853 2006-07-07 11:41:24Z geaz $
 */

public class BindReferencePlugin extends BaseComponentPlugin
{

   private String bindName;

   private Reference reference;

   /**
    * @param params Mandatory: bind-name (value param) - name of binding
    *          reference class-name (value param) - type of binding reference
    *          factory (value param) - object factory type Optional:
    *          ref-addresses (properties param)
    * @throws ConfigurationException
    */
   public BindReferencePlugin(InitParams params) throws ConfigurationException
   {

      ValueParam bnParam = params.getValueParam("bind-name");
      if (bnParam == null)
      {
         throw new ConfigurationException("No 'bind-name' parameter found");
      }
      ValueParam cnParam = params.getValueParam("class-name");
      if (cnParam == null)
      {
         throw new ConfigurationException("No 'class-name' parameter found");
      }
      ValueParam factoryParam = params.getValueParam("factory");
      if (factoryParam == null)
      {
         throw new ConfigurationException("No 'factory' parameter found");
      }
      ValueParam flParam = params.getValueParam("factory-location");
      String factoryLocation;
      if (flParam != null)
         factoryLocation = flParam.getValue();
      else
         factoryLocation = null;

      bindName = bnParam.getValue();
      reference = new Reference(cnParam.getValue(), factoryParam.getValue(), factoryLocation);

      PropertiesParam addrsParam = params.getPropertiesParam("ref-addresses");
      if (addrsParam != null)
      {
         for (Iterator it = addrsParam.getProperties().entrySet().iterator(); it.hasNext();)
         {
            Entry entry = (Entry)it.next();
            reference.add(new StringRefAddr((String)entry.getKey(), (String)entry.getValue()));
         }
      }
   }

   /**
    * @return reference bound
    */
   public Reference getReference()
   {
      return reference;
   }

   /**
    * @return name
    */
   public String getBindName()
   {
      return bindName;
   }
}
