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
package org.exoplatform.container.jmx;

import junit.framework.TestCase;

import org.exoplatform.management.jmx.annotations.NameTemplate;
import org.exoplatform.management.jmx.annotations.Property;
import org.exoplatform.management.jmx.impl.ObjectNameBuilder;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a>
 * @version $Revision$
 */
public class TestNameTemplate extends TestCase
{

   public <T> void assertCannotBuild(T o, Class<T> clazz)
   {
      try
      {
         ObjectNameBuilder<T> builder = new ObjectNameBuilder<T>("foo", clazz);
         builder.build(o);
         fail();
      }
      catch (Exception ignore)
      {
      }
   }

   public <T> void assertSame(T o, Class<T> clazz, String value)
   {
      ObjectName expectedValue;
      try
      {
         expectedValue = new ObjectName(value);
      }
      catch (MalformedObjectNameException e)
      {
         throw new AssertionError(e);
      }
      ObjectNameBuilder<T> builder = new ObjectNameBuilder<T>("foo", clazz);
      assertEquals(expectedValue, builder.build(o));
   }

   public void testSame()
   {
      assertSame(new MBean1(), MBean1.class, "foo:a=b");
      assertSame(new MBean2(), MBean2.class, "foo:a=something");
      assertSame(new MBean3(), MBean3.class, "foo:foo=3");
   }

   @NameTemplate(@Property(key = "a", value = "b"))
   public static class MBean1
   {
   }

   @NameTemplate(@Property(key = "a", value = "{B}"))
   public static class MBean2
   {
      public String getB()
      {
         return "something";
      }
   }

   @NameTemplate(@Property(key = "foo", value = "{Foo}"))
   public static class MBean3
   {
      public Integer getFoo()
      {
         return 3;
      }
   }

   public void testCannotBuilder()
   {
      assertCannotBuild(new MBean4(), MBean4.class);
      assertCannotBuild(new MBean5(), MBean5.class);
      assertCannotBuild(new MBean6(), MBean6.class);
      assertCannotBuild(new MBean7(), MBean7.class);
      assertCannotBuild(new MBean8(), MBean8.class);
      assertCannotBuild(new MBean9(), MBean9.class);
      //    assertCannotBuild(new MBean10());
      assertCannotBuild(new MBean11(), MBean11.class);
      assertCannotBuild(new MBean12(), MBean12.class);
   }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean4
   {
   }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean5
   {
      public void getB()
      {
      }
   }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean6
   {
      public String getB(String s)
      {
         return "Foo";
      }
   }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean7
   {
      public String getB()
      {
         throw new RuntimeException();
      }
   }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean8
   {
      public String getB()
      {
         return null;
      }
   }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean9
   {
      public static String getB()
      {
         return "Foo";
      }
   }

   //  @NameTemplate("foo:a=={b}")
   //  public static class MBean10 {
   //    public String getB() {
   //      return "Foo";
   //    }
   //  }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean11
   {
      protected String getB()
      {
         return "Foo";
      }
   }

   @NameTemplate(@Property(key = "a", value = "{b}"))
   public static class MBean12
   {
      public String getB()
      {
         return "=";
      }
   }

   public void testNoAnnotation()
   {
      ObjectNameBuilder<MBean13> builder = new ObjectNameBuilder<MBean13>("foo", MBean13.class);
      assertEquals(null, builder.build(new MBean13()));
   }

   public static class MBean13
   {
   }

   public void testInheritence()
   {
      assertSame(new MBean14(), MBean14.class, "foo:a=b");
      assertSame(new MBean15(), MBean15.class, "foo:c=d");
      assertSame(new MBean16(), MBean16.class, "foo:e=f");
      assertSame(new MBean17(), MBean17.class, "foo:g=h");
      assertSame(new MBean18(), MBean18.class, "foo:g=h");
      assertSame(new MBean19(), MBean19.class, "foo:i=j");
   }

   @NameTemplate(@Property(key = "a", value = "b"))
   public static interface Interface1
   {
   }

   public static class MBean14 implements Interface1
   {
   }

   @NameTemplate(@Property(key = "c", value = "d"))
   public static class MBean15 implements Interface1
   {
   }

   @NameTemplate(@Property(key = "e", value = "f"))
   public static interface Interface2 extends Interface1
   {
   }

   public static class MBean16 implements Interface2
   {
   }

   @NameTemplate(@Property(key = "g", value = "h"))
   public static class MBean17
   {
   }

   public static class MBean18 extends MBean17
   {
   }

   @NameTemplate(@Property(key = "i", value = "j"))
   public static class MBean19 extends MBean17
   {
   }
}
