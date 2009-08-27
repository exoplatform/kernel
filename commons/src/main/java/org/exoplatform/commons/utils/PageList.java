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
package org.exoplatform.commons.utils;

import org.exoplatform.commons.exception.ExoMessageException;

import java.util.Collections;
import java.util.List;

/**
 * Subclasses of this object should be replaced by the {@link org.exoplatform.commons.utils.LazyList subclass} and
 * an implementation of the {@link org.exoplatform.commons.utils.ListAccess} interface.
 *
 * @deprecated
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 21, 2004
 * @version $Id: PageList.java,v 1.2 2004/10/25 03:36:58 tuan08 Exp $
 */
abstract public class PageList<E>
{
   final static public PageList EMPTY_LIST = new ObjectPageList(Collections.emptyList(), 10);

   private int pageSize_;

   protected int available_ = 0;

   protected int availablePage_ = 1;

   protected int currentPage_ = -1;

   protected List<E> currentListPage_;

   public PageList(int pageSize)
   {
      pageSize_ = pageSize;
   }

   /**
    * Returns the page size.
    *
    * @return the page size
    */
   public int getPageSize()
   {
      return pageSize_;
   }

   /**
    * Updates the page size.
    *
    * @param pageSize the new page size value
    */
   public void setPageSize(int pageSize)
   {
      pageSize_ = pageSize;

      // A bit ugly but it force to refresh the state
      setAvailablePage(available_);
   }

   /**
    * Returns the current page index.
    *
    * @return the current page
    */
   public int getCurrentPage()
   {
      return currentPage_;
   }

   /**
    * Returns the number of available elements.
    *
    * @return the available elements
    */
   public int getAvailable()
   {
      return available_;
   }

   /**
    * Returns the number of available pages.
    *
    * @return the available pages
    */
   public int getAvailablePage()
   {
      return availablePage_;
   }

   public List<E> currentPage() throws Exception
   {
      if (currentListPage_ == null)
      {
         populateCurrentPage(currentPage_);
      }
      return currentListPage_;
   }

   abstract protected void populateCurrentPage(int page) throws Exception;

   /**
    * Updates the current page index and retrieves the element from that page.
    *
    * @param page the page index
    * @return the list of element of the page
    * @throws Exception an exception
    */
   public List<E> getPage(int page) throws Exception
   {
      checkAndSetPage(page);
      populateCurrentPage(page);
      return currentListPage_;
   }

   abstract public List<E> getAll() throws Exception;

   protected void checkAndSetPage(int page) throws Exception
   {
      if (page < 1 || page > availablePage_)
      {
         Object[] args = {Integer.toString(page), Integer.toString(availablePage_)};
         throw new ExoMessageException("PageList.page-out-of-range", args);
      }
      currentPage_ = page;
   }

   protected void setAvailablePage(int available)
   {
      available_ = available;
      if (available == 0)
      {
         availablePage_ = 1;
         currentPage_ = 1;
      }
      else
      {
         int pages = available / pageSize_;
         if (available % pageSize_ > 0)
            pages++;
         availablePage_ = pages;
         currentPage_ = 1;
      }
   }

   /**
    * Returns the from index.
    * 
    * @return the from index
    */
   public int getFrom()
   {
      return (currentPage_ - 1) * pageSize_;
   }

   /**
    * Returns the to index.
    *
    * @return the to index
    */
   public int getTo()
   {
      int to = currentPage_ * pageSize_;
      if (to > available_)
         to = available_;
      return to;
   }
}
