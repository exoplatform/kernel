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
package org.exoplatform.services.cache;

/**
 * This class defines the main configuration properties of an {@link org.exoplatform.services.cache.ExoCache}
 * 
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Feb 20, 2005
 * @version $Id: ExoCacheConfig.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class ExoCacheConfig implements Cloneable
{
   /**
    * The name of the cache.
    */
   private String name;

   /**
    * The label of the cache.
    */
   private String label;

   /**
    * The maximum numbers of elements in cache.
    */
   private int maxSize;

   /**
    * The amount of time (in seconds) an element is not written or
    * read before it is evicted.
    */
   private long liveTime;

   /**
    * Indicates if the cache is distributed
    */
   private boolean distributed;

   /**
    * Indicates if the cache is replicated
    */
   private boolean replicated;

   /**
    * The full qualified name of the cache implementation to use
    */
   private String implementation;

   /**
    * Indicates if the log is enabled
    */
   private boolean logEnabled;

   /**
    * Indicates whether or not the replication of the values should be avoided
    */
   public boolean avoidValueReplication;

   public String getName()
   {
      return name;
   }

   public void setName(String s)
   {
      name = s;
   }

   public String getLabel()
   {
      return label;
   }

   public void setLabel(String s)
   {
      label = s;
   }

   public int getMaxSize()
   {
      return maxSize;
   }

   public void setMaxSize(int size)
   {
      maxSize = size;
   }

   public long getLiveTime()
   {
      return liveTime;
   }

   public void setLiveTime(long period)
   {
      liveTime = period;
   }

   public boolean isDistributed()
   {
      return distributed;
   }

   public void setDistributed(boolean b)
   {
      distributed = b;
   }

   public boolean isRepicated()
   {
      return replicated;
   }

   public void setReplicated(boolean b)
   {
      replicated = b;
   }

   public String getImplementation()
   {
      return implementation;
   }

   public void setImplementation(String alg)
   {
      implementation = alg;
   }

   public boolean isLogEnabled()
   {
      return logEnabled;
   }

   public void setLogEnabled(boolean enableLogging)
   {
      this.logEnabled = enableLogging;
   }
 
   /**
    * @return the avoidValueReplication
    */
   public boolean avoidValueReplication()
   {
      return avoidValueReplication;
   }

   /**
    * @param avoidValueReplication the avoidValueReplication to set
    */
   public void setAvoidValueReplication(boolean avoidValueReplication)
   {
      this.avoidValueReplication = avoidValueReplication;
   }

   /**
    * @see java.lang.Object#clone()
    */
   @Override
   public ExoCacheConfig clone() throws CloneNotSupportedException
   {
      return (ExoCacheConfig)super.clone();
   }
}
