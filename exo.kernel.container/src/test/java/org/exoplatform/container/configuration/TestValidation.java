/*
 * Copyright (C) 2012 eXo Platform SAS.
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

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.container.xml.ValuesParam;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestValidation
{
   public TestValidation(InitParams params)
   {
      PropertiesParam pp = params.getPropertiesParam("properties-param");
      ExoProperties ep = pp.getProperties();
      check(ep.containsKey("p1"));
      check(ep.containsKey("p2"));
      check(ep.containsKey("p3"));
      pp = params.getPropertiesParam("properties-param2");
      ep = pp.getProperties();
      check(ep.containsKey("p1"));
      check(ep.containsKey("p2"));
      check(ep.containsKey("p3"));
      MyBean bean = (MyBean)params.getObjectParam("object-param").getObject();
      check(bean.value.equals("value"));
      bean = (MyBean)params.getObjectParam("object-param2").getObject();
      check(bean.value.equals("value"));
      ValueParam vp = params.getValueParam("value-param");
      check(vp.getValue().equals("value"));
      vp = params.getValueParam("value-param2");
      check(vp.getValue().equals("value"));
      ValuesParam vps = params.getValuesParam("values-param");
      check(vps.getValues().get(0).equals("value1"));
      check(vps.getValues().get(1).equals("value2"));
      check(vps.getValues().get(2).equals("value3"));
      vps = params.getValuesParam("values-param2");
      check(vps.getValues().get(0).equals("value1"));
      check(vps.getValues().get(1).equals("value2"));
      check(vps.getValues().get(2).equals("value3"));
   }
   
   public void check(boolean ok)
   {
      if (!ok)
      {
         throw new IllegalArgumentException();         
      }
   }
   
   public static class MyBean
   {
      public String value;
   }
}
