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
package org.exoplatform.services.command.action;

import java.util.HashMap;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ActionService extends HashMap<String, ActionCatalog>
{

   // public void addPlugin(ComponentPlugin plugin) {
   // if (plugin instanceof AddActionCatalogPlugin) {
   // AddActionCatalogPlugin cplugin = (AddActionCatalogPlugin) plugin;
   // CatalogConfiguration conf = cplugin.getCatalogConfiguration();
   // ActionCatalog catalog = new ActionCatalog();
   // for(ActionConfiguration ac:(List <ActionConfiguration>)conf.getActions()) {
   // try {
   // ActionMatcher matcher =
   // (ActionMatcher)Class.forName(ac.getMatcherFQCN()).newInstance();
   // Action action = (Action)Class.forName(ac.getActionFQCN()).newInstance();
   // catalog.addAction(matcher, action);
   // } catch (Exception e) {
   // e.printStackTrace();
   // }
   // }
   // put(conf.getName(), catalog);
   // }
   // }

}
