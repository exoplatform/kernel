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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by The eXo Platform SAS Author : Peter Nedonosko
 * peter.nedonosko@exoplatform.com.ua 05.07.2007 ISO 8601 standard Year: YYYY
 * (eg 1997) Year and month: YYYY-MM (eg 1997-07) Complete date: YYYY-MM-DD (eg
 * 1997-07-16) Complete date plus hours and minutes: YYYY-MM-DDThh:mmTZD (eg
 * 1997-07-16T19:20+01:00) Complete date plus hours, minutes and seconds:
 * YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00) Complete date plus
 * hours, minutes, seconds and a decimal fraction of a second
 * YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00) where: YYYY =
 * four-digit year MM = two-digit month (01=January, etc.) DD = two-digit day of
 * month (01 through 31) hh = two digits of hour (00 through 23) (am/pm NOT
 * allowed) mm = two digits of minute (00 through 59) ss = two digits of second
 * (00 through 59) s = one or more digits representing a decimal fraction of a
 * second TZD = time zone designator (Z or +hh:mm or -hh:mm) a RFC 822 time zone
 * is also accepted: For formatting, the RFC 822 4-digit time zone format is
 * used: RFC822TimeZone: Sign TwoDigitHours Minutes TwoDigitHours: Digit Digit
 * like -8000
 *
 * @author <a href="mailto:peter.nedonosko@exoplatform.com.ua">Peter
 *         Nedonosko</a>
 * @version $Id: ISO8601.java 34394 2009-07-23 09:23:31Z dkatayev $
 */

public class ISO8601
{

   /**
    * ISO 8601 time zone designator
    */
   protected static final String TZD = "TZD";

   /**
    * Year: YYYY (eg 1997)
    */
   public static final String YEAR_FORMAT = "yyyy";

   /**
    * Year and month: YYYY-MM (eg 1997-07)
    */
   public static final String YEARMONTH_FORMAT = "yyyy-MM";

   /**
    * Complete date: YYYY-MM-DD (eg 1997-07-16)
    */
   public static final String COMPLETE_DATE_FORMAT = "yyyy-MM-dd";

   /**
    * NON ISO STANDARD. Simple date plus hours and minutes, without time zone:
    * YYYY-MM-DDThh:mm (eg 1997-07-16T19:20)
    */
   public static final String SIMPLE_DATEHOURSMINUTES_FORMAT = "yyyy-MM-dd'T'HH:mm";

   /**
    * NON ISO STANDARD. Complete date plus hours and minutes, with time zone by
    * RFC822: YYYY-MM-DDThh:mmZ (eg 1997-07-16T19:20+0100)
    */
   public static final String COMPLETE_DATEHOURSMINUTESZRFC822_FORMAT = "yyyy-MM-dd'T'HH:mmZ";

   /**
    * Complete date plus hours and minutes: YYYY-MM-DDThh:mmTZD (eg
    * 1997-07-16T19:20+01:00)
    */
   public static final String COMPLETE_DATEHOURSMINUTESZ_FORMAT = "yyyy-MM-dd'T'HH:mm" + TZD;

   /**
    * NON ISO STANDARD. Simple date plus hours, minutes and seconds, without
    * time zone: YYYY-MM-DDThh:mm:ss (eg 1997-07-16T19:20:30)
    */
   public static final String SIMPLE_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

   /**
    * NON ISO STANDARD. Complete date plus hours, minutes and seconds, with
    * time zone by RFC822: YYYY-MM-DDThh:mm:ssZ (eg 1997-07-16T19:20:30+0100)
    */
   public static final String COMPLETE_DATETIMEZRFC822_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";

   /**
    * Complete date plus hours, minutes and seconds: YYYY-MM-DDThh:mm:ssTZD (eg
    * 1997-07-16T19:20:30+01:00)
    */
   public static final String COMPLETE_DATETIMEZ_FORMAT = "yyyy-MM-dd'T'HH:mm:ss" + TZD;

   /**
    * NON ISO STANDARD. Simple date plus hours, minutes, seconds and a decimal
    * fraction of a second, without time zone YYYY-MM-DDThh:mm:ss.s (eg
    * 1997-07-16T19:20:30.45)
    */
   public static final String SIMPLE_DATETIMEMS_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss.SS";

   /**
    * NON ISO STANDARD. Simple date plus hours, minutes, seconds and a decimal
    * fraction of a second, without time zone YYYY-MM-DDThh:mm:ss.s (eg
    * 1997-07-16T19:20:30.450)
    */
   public static final String SIMPLE_DATETIMEMS_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

   /**
    * Complete date plus hours, minutes, seconds and a decimal fraction of a
    * second, with time zone by RFC822 YYYY-MM-DDThh:mm:ss.sZ (eg
    * 1997-07-16T19:20:30.45+0100)
    */
   public static final String COMPLETE_DATETIMEMSZRFC822_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss.SSZ";

   /**
    * Complete date plus hours, minutes, seconds and a decimal fraction of a
    * second, with time zone by RFC822 YYYY-MM-DDThh:mm:ss.sZ (eg
    * 1997-07-16T19:20:30.450+0100)
    */
   public static final String COMPLETE_DATETIMEMSZRFC822_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

   /**
    * Complete date plus hours, minutes, seconds and a decimal fraction of a
    * second YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.450+01:00)
    */
   public static final String COMPLETE_DATETIMEMSZ_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS" + TZD;

   /**
    * Complete date plus hours, minutes, seconds and a decimal fraction of a
    * second YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)
    */
   public static final String COMPLETE_DATETIMEMSZ_FORMAT_1 = "yyyy-MM-dd'T'HH:mm:ss.SS" + TZD;

   /**
    * Possible formats list. ISO 8601, RFC822 + simple formats in order of
    * priority of parse
    */
   public static final String[] FORMATS =
      {COMPLETE_DATETIMEMSZ_FORMAT, COMPLETE_DATETIMEMSZ_FORMAT_1, COMPLETE_DATETIMEMSZRFC822_FORMAT, COMPLETE_DATETIMEMSZRFC822_FORMAT_1, SIMPLE_DATETIMEMS_FORMAT,
         SIMPLE_DATETIMEMS_FORMAT_1, COMPLETE_DATETIMEZ_FORMAT, COMPLETE_DATETIMEZRFC822_FORMAT, SIMPLE_DATETIME_FORMAT,
         COMPLETE_DATEHOURSMINUTESZ_FORMAT, COMPLETE_DATEHOURSMINUTESZRFC822_FORMAT, SIMPLE_DATEHOURSMINUTES_FORMAT,
         COMPLETE_DATE_FORMAT, YEARMONTH_FORMAT, YEAR_FORMAT};

   public static final String[] LEGACY_FORMATS =
           {COMPLETE_DATETIMEMSZ_FORMAT, COMPLETE_DATETIMEMSZRFC822_FORMAT, SIMPLE_DATETIMEMS_FORMAT, COMPLETE_DATETIMEZ_FORMAT,
                   COMPLETE_DATETIMEZRFC822_FORMAT, SIMPLE_DATETIME_FORMAT, COMPLETE_DATEHOURSMINUTESZ_FORMAT, COMPLETE_DATEHOURSMINUTESZRFC822_FORMAT,
                   SIMPLE_DATEHOURSMINUTES_FORMAT, COMPLETE_DATE_FORMAT, YEARMONTH_FORMAT, YEAR_FORMAT};

   /**
    * Unknown Time Zone ID
    */
   private final static String UNKNOWN_TIME_ZONE = "Unknown";

   protected static class ISODateFormat
   {

      private final DateTimeFormatter formater;

      private final String format;

      private final boolean isoTZ;

      ISODateFormat(String format)
      {
         this.isoTZ = format.endsWith(TZD);
         this.format = this.isoTZ ? format.substring(0, format.length() - TZD.length()) + "Z" : format;
         this.formater = this.format.equals(YEARMONTH_FORMAT) || this.format.equals(YEAR_FORMAT) ? DateTimeFormatter.ofPattern(COMPLETE_DATE_FORMAT, Locale.US)
                 :  DateTimeFormatter.ofPattern(this.format, Locale.US);
      }

      public Calendar parse(String dateString) throws ParseException, NumberFormatException
      {
         Instant instant = null;
         TimeZone timeZone = null;
         if (dateString.length() >= 16)
         {
            if (isoTZ)
            {
               // need fix TZ from ISO 8601 (+01:00) to RFC822 (+0100)
               if (dateString.endsWith("Z"))
               {
                  dateString = dateString.substring(0, dateString.length() - 1) + "+0000";
               }
               else
               {
                  int tzsindex = dateString.length() - 6;
                  char tzsign = dateString.charAt(tzsindex); // sixth char from the end
                  if (tzsign == '+' || tzsign == '-')
                  {
                     dateString = dateString.substring(0, tzsindex) + dateString.substring(tzsindex).replaceAll(":", "");
                  }
               }
            }
            int index = dateString.lastIndexOf('-');
            if (index >= 16 || (index = dateString.lastIndexOf('+')) >= 16)
            {
               String timeZoneStr = dateString.substring(index);
               if(timeZoneStr.length()==3)
               {
                  timeZoneStr = timeZoneStr.concat("00");
                  dateString = dateString.concat("00");
               }
               timeZone = TimeZone.getTimeZone("GMT" + timeZoneStr);
               formater.withZone(timeZone.toZoneId());
            }
         }
         Calendar isoCalendar = Calendar.getInstance();
         boolean isNegativeDate = false;
         if(dateString.startsWith("-"))
         {
            isNegativeDate = true;
            dateString = dateString.substring(1, dateString.length());
         }
         if(dateString.length()==4 && ISO8601.YEAR_FORMAT.equals(format))
         {
            LocalDate localDate= LocalDate.parse(dateString+"-01-01", formater);
            if (isNegativeDate)
            {
               localDate.withYear(-localDate.getYear());
            }
            instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
         }
         else if(dateString.length()==7 && ISO8601.YEARMONTH_FORMAT.equals(format))
         {
            LocalDate localDate= LocalDate.parse(dateString+"-01", formater);
            if (isNegativeDate)
            {
               localDate.withYear(-localDate.getYear());
            }
            instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
         }
         else if(ISO8601.COMPLETE_DATE_FORMAT.equals(format))
         {
            LocalDate localDate= LocalDate.parse(dateString, formater);
            if (isNegativeDate)
            {
               localDate.withYear(-localDate.getYear());
            }
            instant = localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
         }
         else if(timeZone != null)
         {
            ZonedDateTime zonedDateTime =ZonedDateTime.parse(dateString,formater);
            if (isNegativeDate)
            {
               zonedDateTime.withYear(-zonedDateTime.getYear());
            }
            instant = zonedDateTime.toInstant();
         }
         else
         {
            instant = LocalDateTime.parse(dateString, formater).atZone(ZoneId.systemDefault()).toInstant();
         }
         isoCalendar.setTime(Date.from(instant));
         if (timeZone != null)
            isoCalendar.setTimeZone(timeZone);

         return isoCalendar;
      }

      public String format(Calendar source)
      {
         if (isoTZ)
         {
            TimeZone timeZone = source.getTimeZone();
            ZoneId zoneId = (timeZone != null && !UNKNOWN_TIME_ZONE.equalsIgnoreCase(timeZone.getID())) ? source.getTimeZone().toZoneId() :
                    ZoneId.systemDefault();
            String formatedDate = ZonedDateTime.ofInstant(source.getTime().toInstant(), zoneId).format(formater);

            if (formatedDate.endsWith("0000"))
            {
               return formatedDate.substring(0, formatedDate.length() - 5) + "Z"; // GMT
               // (
               // UTC
               // )
            }
            else
            {
               int dindex = formatedDate.length() - 2;
               return formatedDate.substring(0, dindex) + ":" + formatedDate.substring(dindex); // GMT
               // offset
            }

         }
         else
            return LocalDateTime.ofInstant(source.toInstant(), ZoneId.systemDefault()).format(formater);
      }
   }

   /**
    * Format date using format: complete date plus hours, minutes, seconds and a
    * decimal fraction of a second.
    *
    * @param date
    * @return
    */
   public static String format(Calendar date)
   {
      //Try to parse date text with new date format
      try
      {
         return new ISODateFormat(COMPLETE_DATETIMEMSZ_FORMAT).format(date);
      }
      //Try to parse date text with old date format
      catch (Exception e)
      {
         return new LegacyISODateFormat(COMPLETE_DATETIMEMSZ_FORMAT).format(date);
      }
   }

   /**
    * Parse string using possible formats list.
    * 
    * @param dateString - date string
    * @return - calendar or null if dateString is inparseable text
    */
   public static Calendar parse(String dateString)
   {
      try
      {
         return parseEx(dateString);
      }
      catch (ParseException e)
      {
         return null;
      }
   }

   /**
    * Parse string using possible formats list.
    *
    * @param dateString - date string
    * @return - calendar
    * @throws ParseException
    * @throws NumberFormatException
    */
   public static Calendar parseEx(String dateString) throws ParseException, NumberFormatException
   {
      //Try to parse date text with new date format
      try
      {
         return parse(dateString, FORMATS);
      }
      //Try to parse date text with old date format
      catch (Exception e)
      {
         return parse(dateString, LEGACY_FORMATS, true);
      }
   }

   /**
    * Parse string using given formats list.
    *
    * @param dateString
    * @param formats
    * @return
    * @throws ParseException
    * @throws NumberFormatException
    */
   public static Calendar parse(String dateString, String[] formats) throws ParseException, NumberFormatException
   {
      return parse(dateString, formats, false);
   }

   /**
    * Parse string using given formats list.
    * 
    * @param dateString
    * @param formats
    * @param isLegacyParser
    * @return
    * @throws ParseException
    * @throws NumberFormatException
    */
   private static Calendar parse(String dateString, String[] formats, boolean isLegacyParser) throws ParseException
   {
      StringBuilder problems = new StringBuilder();

      int errorIndex = 0;
      for (String format : formats)
      {
         try
         {
            Calendar isoDate = isLegacyParser ? new LegacyISODateFormat(format).parse(dateString) : new ISODateFormat(format).parse(dateString);
            return isoDate; // done
         }
         catch (DateTimeParseException e)
         {
            if (errorIndex == 0)
               errorIndex = e.getErrorIndex();

            appendError(problems, format, e.getErrorIndex(), e.getMessage());
         }
         catch (ParseException e)
         {
            if (errorIndex == 0)
               errorIndex = e.getErrorOffset();

            appendError(problems, format, e.getErrorOffset(), e.getMessage());
         }
         catch (NumberFormatException e)
         {
            errorIndex = 0;

            problems.append(format);
            problems.append(" - ");
            problems.append(e.getMessage());
            problems.append(" \n");
         }
      }

      throw new ParseException("Can not parse " + dateString + " as Date. " + problems.toString(), errorIndex);
   }

   /**
    * Old Date format implementation 
    */
   private static class LegacyISODateFormat
   {

      private final SimpleDateFormat formater;

      private final String format;

      private final boolean isoTZ;

      LegacyISODateFormat(String format)
      {
         this.isoTZ = format.endsWith(TZD);
         this.format = this.isoTZ ? format.substring(0, format.length() - TZD.length()) + "Z" : format;
         this.formater = new SimpleDateFormat(this.format, Locale.US);
      }

      public Calendar parse(String dateString) throws ParseException, NumberFormatException
      {
         Date isoDate = null;
         TimeZone timeZone = null;
         if (dateString.length() >= 16)
         {
            if (isoTZ)
            {
               // need fix TZ from ISO 8601 (+01:00) to RFC822 (+0100)
               if (dateString.endsWith("Z"))
               {
                  dateString = dateString.substring(0, dateString.length() - 1) + "+0000";
               }
               else
               {
                  int tzsindex = dateString.length() - 6;
                  char tzsign = dateString.charAt(tzsindex); // sixth char from the end
                  if (tzsign == '+' || tzsign == '-')
                  {
                     dateString = dateString.substring(0, tzsindex) + dateString.substring(tzsindex).replaceAll(":", "");
                  }
               }
            }
            int index = dateString.lastIndexOf('-');
            if (index >= 16 || (index = dateString.lastIndexOf('+')) >= 16)
            {
               String timeZoneStr = dateString.substring(index);
               timeZone = TimeZone.getTimeZone("GMT" + timeZoneStr);
               formater.setTimeZone(timeZone);
            }
         }
         isoDate = formater.parse(dateString);

         Calendar isoCalendar = Calendar.getInstance();
         isoCalendar.setTime(isoDate);
         if (timeZone != null)
            isoCalendar.setTimeZone(timeZone);

         return isoCalendar;
      }

      public String format(Calendar source)
      {
         if (isoTZ)
         {
            formater.setTimeZone(source.getTimeZone());
            String formatedDate = formater.format(source.getTime());

            if (formatedDate.endsWith("0000"))
            {
               return formatedDate.substring(0, formatedDate.length() - 5) + "Z"; // GMT
               // (
               // UTC
               // )
            }
            else
            {
               int dindex = formatedDate.length() - 2;
               return formatedDate.substring(0, dindex) + ":" + formatedDate.substring(dindex); // GMT
               // offset
            }

         }
         else
            return formater.format(source);
      }
   }

   private static void appendError(StringBuilder problems, String format, int index, String message)
   {
      problems.append(format);
      problems.append(" - ");
      problems.append(message);
      problems.append(", error index ");
      problems.append(index);
      problems.append(" \n");
   }
}
