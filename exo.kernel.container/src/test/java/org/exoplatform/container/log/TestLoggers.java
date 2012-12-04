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
package org.exoplatform.container.log;

import junit.framework.TestCase;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.log.LogConfigurationInitializer;

import java.util.Properties;

/**
 * Created by The eXo Platform SAS
 * 
 * @author <a href="work.visor.ck@gmail.com">Dmytro Katayev</a> Jun 24, 2009
 */
public class TestLoggers extends TestCase
{

   private final String logger = "org.slf4j.Logger";

   public void _testExoLogPerformance() throws Exception
   {

      Log log = ExoLogger.getLogger(TestLoggers.class);

      String confClass = "org.exoplatform.services.log.impl.Log4JConfigurator";

      Properties props = new Properties();

      props.put("log4j.rootLogger", "INFO, stdout, file");

      props.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
      props.put("log4j.appender.stdout.threshold", "DEBUG");

      props.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
      props.put("log4j.appender.stdout.layout.ConversionPattern",
         "%d{dd.MM.yyyy HH:mm:ss} *%-5p* [%t] %c{1}: %m (%F, line %L) %n");

      props.put("log4j.appender.file", "org.apache.log4j.FileAppender");
      props.put("log4j.appender.file.File", "target/l4j_info.log");

      props.put("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
      props.put("log4j.appender.file.layout.ConversionPattern",
         "%d{dd.MM.yyyy HH:mm:ss} *%-5p* [%t] %c{1}: %m (%F, line %L) %n");

      new LogConfigurationInitializer(logger, confClass, props);

      log.info("Performance test.");
   }

   public void _testLog4j() throws Exception
   {

      Log log = ExoLogger.getLogger(TestLoggers.class);

      String confClass = "org.exoplatform.services.log.impl.Log4JConfigurator";

      Properties props = new Properties();

      props.put("log4j.rootLogger", "DEBUG, stdout, file");

      props.put("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender");
      props.put("log4j.appender.stdout.threshold", "DEBUG");

      props.put("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout");
      props.put("log4j.appender.stdout.layout.ConversionPattern",
         "%d{dd.MM.yyyy HH:mm:ss} *%-5p* [%t] %c{1}: %m (%F, line %L) %n");

      props.put("log4j.appender.file", "org.apache.log4j.FileAppender");
      props.put("log4j.appender.file.File", "target/l4j_info.log");

      props.put("log4j.appender.file.layout", "org.apache.log4j.PatternLayout");
      props.put("log4j.appender.file.layout.ConversionPattern",
         "%d{dd.MM.yyyy HH:mm:ss} *%-5p* [%t] %c{1}: %m (%F, line %L) %n");

      LogConfigurationInitializer initializer = new LogConfigurationInitializer(logger, confClass, props);
      log.info("LOG4J Tests");
      logOut(log);

      initializer.setProperty("log4j.rootLogger", "INFO, stdout, file");
      initializer.setProperty("log4j.appender.file.File", "target/l4j_debg.log");

      logOut(log);

   }

   public void testLog4jContainer() throws Exception
   {

      PortalContainer.getInstance();
      Log log = ExoLogger.getLogger(TestLoggers.class);

      log.info("Log4j Container Tests");
      log.info("Log4j Container {}", "Tests");
      log.info("Log4j Conta{} Te{}", "iner", "sts");
      log.info("Log4j Container Tests", 1, 2, 3);
      logOut(log);

   }

   /**
    * To launch this test: 1. remove Log4jConfogurator from
    * org.exoplatform.services.log. 2. remove log4j dependency from
    * exo.kernel.commons. 3. replace slf4j-log4j12 with slf4j-jcl in
    * exo.kernel.commons dependencies.
    */
   public void _testJCLLog() throws Exception
   {

      String confClass = "org.exoplatform.services.log.impl.Jdk14Configurator";

      Properties props = new Properties();

      props.put("handlers", "java.util.logging.ConsoleHandler,java.util.logging.FileHandler");
      props.put(".level", "INFO");
      props.put("java.util.logging.ConsoleHandler.level", "ALL");
      props.put("java.util.logging.FileHandler.pattern", "./target/java%u.log");
      props.put("java.util.logging.FileHandler.formatter", "java.util.logging.SimpleFormatter");

      new LogConfigurationInitializer(logger, confClass, props);
      Log log = ExoLogger.getLogger(TestLoggers.class);

      log.info("JCL Tests");
      logOut(log);

   }

   /**
    * To launch this test: 1. remove Log4jConfogurator from
    * org.exoplatform.services.log. 2. remove log4j dependency from
    * exo.kernel.commons. 3. replace slf4j-log4j12 with slf4j-jcl in
    * exo.kernel.commons dependencies. 4. Comment Log4J logger configuration and
    * uncomment JDK14 logger configuration in conf.portal/test-configuration.xml.
    */
   public void _testJCLContainer() throws Exception
   {

      PortalContainer.getInstance();
      Log log = ExoLogger.getLogger(TestLoggers.class);

      log.info("JCL Container Tests");
      logOut(log);

   }

   private void logOut(Log log)
   {
      log.debug(log.getClass().getName() + ": \tDEBUG");
      log.error(log.getClass().getName() + ": \tERROR");
      log.info(log.getClass().getName() + ": \tINFO");
      log.trace(log.getClass().getName() + ": \tTRACE");
      log.warn(log.getClass().getName() + ": \tWARNING\n");
   }

}
