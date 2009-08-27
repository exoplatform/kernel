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
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ActionCatalog
{

   private Map<ActionMatcher, Action> commands;

   public ActionCatalog()
   {
      this.commands = new HashMap<ActionMatcher, Action>();
   }

   public Set<Action> getAllActions()
   {
      return new HashSet<Action>(commands.values());
   }

   public Map<ActionMatcher, Action> getAllEntries()
   {
      return commands;
   }

   public Set<Action> getActions(Condition conditions)
   {
      HashSet<Action> actions = new HashSet<Action>();
      for (Map.Entry<ActionMatcher, Action> entry : commands.entrySet())
      {
         if (entry.getKey().match(conditions))
            actions.add(entry.getValue());
      }
      return actions;
   }

   public Action getAction(Condition conditions, int index)
   {
      Iterator<Action> actions = getActions(conditions).iterator();
      for (int i = 0; actions.hasNext(); i++)
      {
         Action c = actions.next();
         if (i == index)
            return c;
      }
      return null;
   }

   public void addAction(ActionMatcher matcher, Action action)
   {
      commands.put(matcher, action);
   }

   public void clear()
   {
      commands.clear();
   }
}
