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
package org.exoplatform.container.monitor.jvm;

import org.exoplatform.commons.utils.ExoProperties;
import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.PropertiesParam;
import org.picocontainer.Startable;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Nov 8, 2004
 * @version $Id: JVMRuntimeInfoImpl.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class JVMRuntimeInfoImpl implements JVMRuntimeInfo, Startable
{
   private RuntimeMXBean mxbean_;

   public JVMRuntimeInfoImpl(InitParams params) throws Exception
   {
      mxbean_ = ManagementFactory.getRuntimeMXBean();

      PropertiesParam param = params.getPropertiesParam("add.system.properties");
      if (param != null)
      {
         ExoProperties props = param.getProperties();
         Iterator i = props.entrySet().iterator();
         while (i.hasNext())
         {
            Map.Entry entry = (Map.Entry)i.next();
            PrivilegedSystemHelper.setProperty((String)entry.getKey(), (String)entry.getValue());
         }
      }
   }

   public String getName()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getName();
         }
      });
   }

   public String getSpecName()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getSpecName();
         }
      });
   }

   public String getSpecVendor()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getSpecVendor();
         }
      });
   }

   public String getSpecVersion()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getSpecVersion();
         }
      });
   }

   public String getManagementSpecVersion()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getManagementSpecVersion();
         }
      });
   }

   public String getVmName()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getVmName();
         }
      });
   }

   public String getVmVendor()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getVmVendor();
         }
      });
   }

   public String getVmVersion()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getVmVersion();
         }
      });
   }

   public List getInputArguments()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<List>()
      {
         public List run()
         {
            return mxbean_.getInputArguments();
         }
      });
   }

   public Map getSystemProperties()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<Map>()
      {
         public Map run()
         {
            return mxbean_.getSystemProperties();
         }
      });
   }

   public boolean getBootClassPathSupported()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<Boolean>()
      {
         public Boolean run()
         {
            return mxbean_.isBootClassPathSupported();
         }
      });
   }

   public String getBootClassPath()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getBootClassPath();
         }
      });
   }

   public String getClassPath()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getClassPath();
         }
      });
   }

   public String getLibraryPath()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<String>()
      {
         public String run()
         {
            return mxbean_.getLibraryPath();
         }
      });
   }

   public long getStartTime()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<Long>()
      {
         public Long run()
         {
            return mxbean_.getStartTime();
         }
      });
   }

   public long getUptime()
   {
      return SecurityHelper.doPrivilegedAction(new PrivilegedAction<Long>()
      {
         public Long run()
         {
            return mxbean_.getUptime();
         }
      });
   }

   public boolean isManagementSupported()
   {
      return true;
   }

   public String getSystemPropertiesAsText()
   {
      StringBuffer b = new StringBuffer();
      Iterator i = PrivilegedSystemHelper.getProperties().entrySet().iterator();
      while (i.hasNext())
      {
         Map.Entry entry = (Map.Entry)i.next();
         b.append(entry.getKey()).append("=").append(entry.getValue()).append("\n");
      }
      return b.toString();
   }

   public void start()
   {
   }

   public void stop()
   {
   }

   @Override
   public String toString()
   {
      StringBuilder b = new StringBuilder();
      b.append("Name: ").append(getName()).append("\n");
      b.append("Specification Name: ").append(getSpecName()).append("\n");
      b.append("Specification Vendor: ").append(getSpecVendor()).append("\n");
      b.append("Specification Version: ").append(getSpecVersion()).append("\n");
      b.append("Management Spec Version: ").append(getManagementSpecVersion()).append("\n\n");

      b.append("Virtual Machine Name: ").append(getVmName()).append("\n");
      b.append("Virtual Machine Vendor: ").append(getVmVendor()).append("\n");
      b.append("Virtual Machine Version: ").append(getVmVersion()).append("\n\n");

      b.append("Input Arguments: ").append(getInputArguments()).append("\n");
      b.append("System Properties: ").append(getSystemProperties()).append("\n\n");

      b.append("Boot Class Path Support: ").append(getBootClassPathSupported()).append("\n");
      b.append("Boot Class Path: ").append(getBootClassPath()).append("\n");
      b.append("Class Path: ").append(getClassPath()).append("\n");
      b.append("Library Path: ").append(getLibraryPath()).append("\n\n");

      b.append("Start Time: ").append(new Date(getStartTime())).append("\n");
      b.append("Up Time: ").append(getUptime() / (1000 * 60)).append("min\n");
      return b.toString();
   }
}
