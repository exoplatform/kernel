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
package org.exoplatform.services.threadpool.impl;

/**
 * $Id: DefaultThreadFactory.java 5332 2006-04-29 18:32:44Z geaz $
 *
 * The contents of this file are subject to the ClickBlocks Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.clickblocks.org
 * 
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied, including, but not limited to, the implied warranties of
 * merchantability, fitness for a particular purpose and
 * non-infringement. See the License for the specific language
 * governing rights and limitations under the License.
 * 
 * ClickBlocks, the ClickBlocks logo and combinations thereof are
 * trademarks of ClickBlocks, LLC in the United States and other
 * countries.
 * 
 * The Initial Developer of the Original Code is ClickBlocks, LLC.
 * Portions created by ClickBlocks, LLC are Copyright (C) 2000.  
 * All Rights Reserved.
 *
 * Contributor(s): Mark Grand
 */

/**
 * This is a default thread factory that creates vanilla threads.
 */
public class DefaultThreadFactory implements ThreadFactoryIF
{
   /**
    * Return a Thread that runs the given Runnable object.
    */
   public Thread createThread(Runnable r)
   {
      return new Thread(r);
   } // createThread(Runnable)
} // class DefaultThreadFactory
