/*
 * Copyright (C) 2014 eXo Platform SAS.
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

import junit.framework.TestCase;

import java.util.Calendar;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestISO8601 extends TestCase
{
   public void testParseNFormat() throws Exception
   {
      String dateStr = "1997";
      Calendar date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + "-01-01T00:00:00.000Z", ISO8601.format(date));

      dateStr = "1997-07";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + "-01T00:00:00.000Z", ISO8601.format(date));

      dateStr = "1997-07-16";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + "T00:00:00.000Z", ISO8601.format(date));

      dateStr = "1997-07-16T19:20";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + ":00.000Z", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + ".000Z", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.45";
      date = ISO8601.parseEx(dateStr);
      assertEquals("1997-07-16T19:20:30.045Z", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.450";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + "Z", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.450+01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr, ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.450-01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr, ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.450+0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 2) + ":00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.450-0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 2) + ":00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.450+01";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + ":00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.450-01";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr + ":00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20+0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 5) + ":00.000+01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20-0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 5) + ":00.000-01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20+01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 6) + ":00.000+01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20-01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 6) + ":00.000-01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30+0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 5) + ".000+01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30-0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 5) + ".000-01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30+01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 6) + ".000+01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30-01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 6) + ".000-01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.045+0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 5) + "+01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.045-0100";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 5) + "-01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.45+01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 8) + "045+01:00", ISO8601.format(date));

      dateStr = "1997-07-16T19:20:30.45-01:00";
      date = ISO8601.parseEx(dateStr);
      assertEquals(dateStr.substring(0, dateStr.length() - 8) + "045-01:00", ISO8601.format(date));
   }

}
