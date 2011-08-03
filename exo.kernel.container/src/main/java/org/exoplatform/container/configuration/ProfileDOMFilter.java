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

import org.exoplatform.commons.utils.Tools;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Filters a kernel DOM according to a list of active profiles.
 *
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
class ProfileDOMFilter
{

   /** . */
   private static final String PROFILE_ATTRIBUTE = "profiles";

   /** . */
   private static final Set<String> kernelURIs = Namespaces.KERNEL_NAMESPACES_SET;

   /** . */
   private static final Set<String> kernelWithProfileURIs;
   static
   {
      // All the kernel namespaces but KERNEL_1_0_URI
      Set<String> tmp = new LinkedHashSet<String>(Namespaces.KERNEL_NAMESPACES_SET);
      tmp.remove(Namespaces.KERNEL_1_0_URI);
      tmp.remove(Namespaces.KERNEL_1_0_URI_OLD);
      kernelWithProfileURIs = Collections.unmodifiableSet(tmp);
   }

   /** . */
   private final Set<String> activeProfiles;

   public ProfileDOMFilter(Set<String> activeProfiles)
   {
      this.activeProfiles = activeProfiles;
   }

   public void process(Element elt)
   {
      if (kernelURIs.contains(elt.getNamespaceURI()))
      {
         // Check if element must be removed
         NamedNodeMap attrs = elt.getAttributes();
         if (attrs != null)
         {
            Set<String> profiles = null;
            List<Attr> toRemove = null;
            for (int i = 0;i < attrs.getLength();i++)
            {
               Attr attr = (Attr)attrs.item(i);
               String attrURI = attr.getNamespaceURI();
               if ((attrURI == null || kernelWithProfileURIs.contains(attrURI)) && PROFILE_ATTRIBUTE.equals(attr.getLocalName()))
               {
                  if (profiles == null)
                  {
                     profiles = Tools.parseCommaList(attr.getValue());
                  }
                  else
                  {
                     profiles.addAll(Tools.parseCommaList(attr.getValue()));
                  }

                  //
                  if (toRemove == null)
                  {
                     toRemove = new ArrayList<Attr>();
                  }
                  toRemove.add(attr);
               }
            }

            // Check if must we delete this node
            if (profiles != null && Collections.disjoint(activeProfiles, profiles))
            {
               Node parent = elt.getParentNode();

               // Remove it
               parent.removeChild(elt);

               // No more processing
               return;
            }

            // Remove profile attributes
            if (toRemove != null)
            {
               for (Attr attr : toRemove)
               {
                  elt.removeAttributeNode(attr);
               }
            }
         }
      }

      // Make a copy as children may be removed and result
      // in a concurrent modification
      NodeList children = elt.getChildNodes();
      ArrayList<Element> childElts = new ArrayList<Element>();
      for (int i = 0;i < children.getLength();i++)
      {
         Node child = children.item(i);
         if (child.getNodeType() == Node.ELEMENT_NODE)
         {
            childElts.add((Element)child);
         }
      }

      // Process recursively
      for (Element childElt : childElts)
      {
         process(childElt);
      }
   }
}
