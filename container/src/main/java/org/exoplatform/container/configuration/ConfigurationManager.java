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

import org.exoplatform.container.xml.Component;
import org.exoplatform.container.xml.Configuration;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;

/**
 * Jul 19, 2004
 * 
 * @author: Tuan Nguyen
 * @email: tuan08@users.sourceforge.net
 * @version: $Id: ConfigurationManager.java 5799 2006-05-28 17:55:42Z geaz $
 */
public interface ConfigurationManager
{
   public Configuration getConfiguration();

   public Component getComponent(String service);

   public Component getComponent(Class clazz) throws Exception;

   public Collection getComponents();

   public void addConfiguration(String url) throws Exception;

   public void addConfiguration(Collection urls) throws Exception;

   public void addConfiguration(URL url) throws Exception;

   public URL getResource(String url, String defaultURL) throws Exception;

   public URL getResource(String url) throws Exception;

   public InputStream getInputStream(String url, String defaultURL) throws Exception;

   public InputStream getInputStream(String url) throws Exception;

   public boolean isDefault(String value);

   public URL getURL(String uri) throws Exception;
}
