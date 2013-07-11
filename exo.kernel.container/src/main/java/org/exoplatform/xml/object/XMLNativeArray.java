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
package org.exoplatform.xml.object;

/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Apr 11, 2005
 * @version $Id: XMLNativeArray.java 5799 2006-05-28 17:55:42Z geaz $
 */
public class XMLNativeArray
{
   private Object array;

   private String type;

   public XMLNativeArray()
   {
   }

   public XMLNativeArray(Object o) throws Exception
   {
      if (!isNativeArray(o))
      {
         throw new Exception(o.getClass().getName() + " is not a native array");
      }
      array = o;
      setType(o);
   }

   public String getType()
   {
      return type;
   }

   public void setType(String s)
   {
      type = s;
   }

   public void setType(Object o)
   {
      if (o instanceof int[])
         type = XMLBaseObject.INT;
      else if (o instanceof long[])
         type = XMLBaseObject.LONG;
      else if (o instanceof float[])
         type = XMLBaseObject.FLOAT;
      else if (o instanceof double[])
         type = XMLBaseObject.DOUBLE;
      else if (o instanceof boolean[])
         type = XMLBaseObject.BOOLEAN;
   }

   public Object getValue()
   {
      return array;
   }

   public String getArray()
   {
      if (type.equals(XMLBaseObject.INT))
         return encodeIntArray((int[])array);
      else if (type.equals(XMLBaseObject.LONG))
         return encodeLongArray((long[])array);
      else if (type.equals(XMLBaseObject.DOUBLE))
         return encodeDoubleArray((double[])array);
      else
         throw new RuntimeException("Unknown array type: " + type);
   }

   public void setArray(String text)
   {
      if (type.equals(XMLBaseObject.INT))
         array = decodeIntArray(text);
      else if (type.equals(XMLBaseObject.LONG))
         array = decodeLongArray(text);
      else if (type.equals(XMLBaseObject.DOUBLE))
         array = decodeDoubleArray(text);
   }

   public static int[] decodeIntArray(String text)
   {
      String temp[] = text.split(",");
      int[] iarray = new int[temp.length];
      for (int i = 0; i < temp.length; i++)
      {
         temp[i] = temp[i].trim();
         iarray[i] = Integer.parseInt(temp[i]);
      }
      return iarray;
   }

   public static String encodeIntArray(int[] array)
   {
      StringBuffer b = new StringBuffer();
      for (int i = 0; i < array.length; i++)
      {
         b.append(array[i]);
         if (i != array.length - 1)
            b.append(", ");
      }
      return b.toString();
   }

   public static long[] decodeLongArray(String text)
   {
      String temp[] = text.split(",");
      long[] array = new long[temp.length];
      for (int i = 0; i < temp.length; i++)
      {
         temp[i] = temp[i].trim();
         array[i] = Long.parseLong(temp[i]);
      }
      return array;
   }

   public static String encodeLongArray(long[] array)
   {
      StringBuffer b = new StringBuffer();
      for (int i = 0; i < array.length; i++)
      {
         b.append(array[i]);
         if (i != array.length - 1)
            b.append(", ");
      }
      return b.toString();
   }

   public static double[] decodeDoubleArray(String text)
   {
      String temp[] = text.split(",");
      double[] array = new double[temp.length];
      for (int i = 0; i < temp.length; i++)
      {
         temp[i] = temp[i].trim();
         array[i] = Double.parseDouble(temp[i]);
      }
      return array;
   }

   public static String encodeDoubleArray(double[] array)
   {
      StringBuffer b = new StringBuffer();
      for (int i = 0; i < array.length; i++)
      {
         b.append(array[i]);
         if (i != array.length - 1)
            b.append(", ");
      }
      return b.toString();
   }

   public static boolean isNativeArray(Object o)
   {
      if (o instanceof int[] || o instanceof long[] || o instanceof float[] || o instanceof double[]
         || o instanceof boolean[])
         return true;
      return false;
   }

   public static boolean isNativeArray(Class<?> clazz)
   {
      if (clazz.equals(int[].class) || clazz.equals(long[].class) || clazz.equals(float[].class)
         || clazz.equals(double[].class) || clazz.equals(boolean[].class))
         return true;
      return false;
   }
}
