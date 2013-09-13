/*
 * Copyright (C) 2013 eXo Platform SAS.
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
package org.exoplatform.container;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.atinject.tck.Tck;
import org.atinject.tck.auto.Car;
import org.atinject.tck.auto.Convertible;
import org.atinject.tck.auto.Drivers;
import org.atinject.tck.auto.DriversSeat;
import org.atinject.tck.auto.Engine;
import org.atinject.tck.auto.FuelTank;
import org.atinject.tck.auto.Seat;
import org.atinject.tck.auto.Tire;
import org.atinject.tck.auto.V8Engine;
import org.atinject.tck.auto.accessories.Cupholder;
import org.atinject.tck.auto.accessories.SpareTire;

import java.net.URL;

/**
 * @author <a href="mailto:nfilotto@exoplatform.com">Nicolas Filotto</a>
 * @version $Id$
 *
 */
public class TestTCK_JSR330
{
   public static Test suite()
   {
      URL url = TestTCK_JSR330.class.getResource("empty-config.xml");
      RootContainer container = new ContainerBuilder().withRoot(url).build();
      container.registerComponentImplementation(Car.class, Convertible.class);
      container.registerComponentImplementation(Seat.class);
      container.registerComponentImplementation(Drivers.class, DriversSeat.class);
      container.registerComponentImplementation(Engine.class, V8Engine.class);
      container.registerComponentImplementation(Cupholder.class);
      container.registerComponentImplementation(Tire.class);
      container.registerComponentImplementation(FuelTank.class);
      container.registerComponentImplementation("spare", SpareTire.class);
      Test t1 = Tck.testsFor(container.getComponentInstanceOfType(Car.class), false, true);
      url = TestTCK_JSR330.class.getResource("tck-config.xml");
      container = new ContainerBuilder().withRoot(url).build();
      Test t2 = Tck.testsFor(container.getComponentInstanceOfType(Car.class), false, true);
      TestSuite suite = new TestSuite("tck");
      suite.addTest(t1);
      suite.addTest(t2);
      return suite;
   }
}
