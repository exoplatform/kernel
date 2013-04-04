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
 * @LevelAPI Platform
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
   /**
    * Returns the cache name
    *
    * @return the cache name
    */
   public String getName()
   {
      return name;
   }
   /**
    * Sets the cache name
    *
    * @param s the cache name
    */
   public void setName(String s)
   {
      name = s;
   }
   /**
    * Returns the cache label
    *
    * @return the cache label
    */
   public String getLabel()
   {
      return label;
   }
   /**
    * Sets the cache label
    *
    * @param s the cache label
    */
   public void setLabel(String s)
   {
      label = s;
   }
   /**
    * Returns the maximum amount of entries allowed in the cache
    *
    * @return the max size of the cache.
    */
   public int getMaxSize()
   {
      return maxSize;
   }
   /**
    * Sets the maximum amount of entries allowed in the cache
    *
    * @param size the max size of the cache
    */
   public void setMaxSize(int size)
   {
      maxSize = size;
   }
   /**
    * Returns the time amount of entries allowed in the cache
    *
    * @return the live time
    */
   public long getLiveTime()
   {
      return liveTime;
   }
   /**
    * Sets the live time
    *
    * @param period the value of the live time
    */
   public void setLiveTime(long period)
   {
      liveTime = period;
   }
   /**
    * Indicates if the cache is distributed or not.
    *
    * @return flag that indicates if the cache is distributed or not.
    */
   public boolean isDistributed()
   {
      return distributed;
   }
   /**
    * Sets distributed state
    *
    * @param  b flag that indicates if the cache is distributed or not.
    */
   public void setDistributed(boolean b)
   {
      distributed = b;
   }
   /**
    * Indicates if the cache is replicated or not.
    *
    * @return flag that indicates if the cache is repicated or not.
    */
   public boolean isRepicated()
   {
      return replicated;
   }
   /**
    * Sets replicated state
    *
    * @param b flag that indicates if the cache is repicated or not.
    */
   public void setReplicated(boolean b)
   {
      replicated = b;
   }
   /**
    * Returns the full qualified name of the cache implementation to use.
    *
    * @return the full qualified name
    */
   public String getImplementation()
   {
      return implementation;
   }
   /**
    * Sets the full qualified name of the cache implementation to use.
    *
    * @param  alg the full qualified name of the cache implementation
    */
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
