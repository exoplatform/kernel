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
package org.exoplatform.services.mail.test;

import com.dumbster.smtp.SimpleSmtpServer;
import com.dumbster.smtp.SmtpMessage;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.mail.Attachment;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.net.NetService;
import org.exoplatform.test.BasicTestCase;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by The eXo Platform SAS Author : Phung Hai Nam phunghainam@gmail.com
 * Dec 23, 2005
 */
public class TestMailService extends BasicTestCase
{
   /**
    * Test email subject used throughout the test cases
    */
   static private String MAIL_SUBJECT = "eXo Test Subject";
   
   /**
    * Test email contents used throughout the test cases
    */
   static private String MAIL_CONTENTS = "eXo Mail Service Test Content";

   /**
    * Test email attachment used throughout the test cases
    */
   static private String ATTACHMENT = "eXo Mail Service Test Attachment";

   /**
    * Mime-type corresponding to plain text documents
    */
   static private String TEXT_PLAIN = "text/plain";

   /**
    * Mime-type corresponding to html documents
    */
   static private String TEXT_HTML = "text/html";

   /**
    * Time we assume to be enough to wait for server to receive sent emails
    */
   static private long ONE_SECOND = 1000l;

   /**
    * SMTP server prot, used for mail session 
    * and {@link SimpleSmtpServer} configuration
    * Though the default port is 25, to run server 
    * with default port you must have root privilegies
    */
   static private int SMTP_PORT = 2525;

   /**
    * Thread number for asynchronous message send tests
    */
   static private int THREAD_NUMBER = 4;
   
   /**
    * SMTP mail server instance, to emulate basic SMTP mail server functions
    */
   protected SimpleSmtpServer mailServer;

   private MailService service;

   private NetService netService;

   public TestMailService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      if (service == null)
      {
         PortalContainer pcontainer = PortalContainer.getInstance();
         service = (MailService)pcontainer.getComponentInstanceOfType(MailService.class);
         netService = (NetService)pcontainer.getComponentInstanceOfType(NetService.class);
      }
      // starting dummy SMTP Server
      if (mailServer == null)
      {
         mailServer = SimpleSmtpServer.start(SMTP_PORT);
      }
   }

   public void tearDown() throws Exception
   {
      mailServer.stop();
   }

   public void testSendMimeMessage() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      MimeMessage message = new MimeMessage(service.getMailSession());
      message.setFrom(new InternetAddress(generateRandomEmailSender()));
      message.setRecipients(javax.mail.Message.RecipientType.TO, generateRandomEmailRecipient());
      message.setSubject(MAIL_SUBJECT);
      message.setContent(MAIL_CONTENTS, TEXT_PLAIN);
      Flags flags = new Flags();
      flags.add(Flags.Flag.RECENT);
      message.setFlags(flags, true);

      cleanEmailMessages();
      assertEquals("SMTP server should be now empty", 0, mailServer.getReceivedEmailSize());
      assertFalse(isEmailMessageSent(MAIL_SUBJECT));
      service.sendMessage(message);
      Thread.sleep(ONE_SECOND);
      assertEquals("SMTP server should have one message", 1, mailServer.getReceivedEmailSize());
      assertTrue(isEmailMessageSent(MAIL_SUBJECT));
   }

   public void testSendMessage() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      Message message = new Message();
      message.setFrom(generateRandomEmailSender());
      message.setTo(generateRandomEmailRecipient());
      message.setCC(generateRandomEmailRecipient() + "," + generateRandomEmailRecipient());
      message.setBCC(generateRandomEmailRecipient() + "," + generateRandomEmailRecipient());
      message.setSubject(MAIL_SUBJECT);
      message.setBody(MAIL_CONTENTS);
      message.setMimeType(TEXT_HTML);
      Attachment attachment = new Attachment();
      attachment.setInputStream(new ByteArrayInputStream(ATTACHMENT.getBytes()));
      attachment.setMimeType(TEXT_PLAIN);
      message.addAttachment(attachment);

      cleanEmailMessages();
      assertEquals("SMTP server should be now empty", 0, mailServer.getReceivedEmailSize());
      assertFalse(isEmailMessageSent(MAIL_SUBJECT));
      service.sendMessage(message);
      Thread.sleep(ONE_SECOND);
      assertEquals("SMTP server should have one message", 1, mailServer.getReceivedEmailSize());
      assertTrue(isEmailMessageSent(MAIL_SUBJECT));
   }

   public void testSendSimplMessage() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      cleanEmailMessages();
      assertEquals("SMTP server should be now empty", 0, mailServer.getReceivedEmailSize());
      assertFalse(isEmailMessageSent(MAIL_SUBJECT));
      service.sendMessage(generateRandomEmailSender(), generateRandomEmailRecipient(), MAIL_SUBJECT, MAIL_CONTENTS);
      Thread.sleep(ONE_SECOND);
      assertEquals("SMTP server should have one message", 1, mailServer.getReceivedEmailSize());
      assertTrue(isEmailMessageSent(MAIL_SUBJECT));
   }

   /**
    * Here we test asynchronous email sending explicitly defined by sender, 
    * recipient, subject and content parameters.
    * We check if we can get real cause of exception, if that occurs during
    * message sending process. 
    */
   public void testSendSimpleMessageAsynchExceptionCause() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      Future<Boolean> future =
         service.sendMessageAsynch("!@#$%^&*()", generateRandomEmailSender(), MAIL_SUBJECT, MAIL_CONTENTS);

      try
      {
         future.get();
         fail();
      }
      catch (ExecutionException ee)
      {
         assertEquals("We tried to send mail with malformed sender field,"
            + " so we expect an AdressException to be the real cause of ExecutionException", ee.getCause().getClass(),
            AddressException.class);
      }
   }

   /**
    * Here we test asynchronous email sending explicitly defined by sender, 
    * recipient, subject and content parameters.
    * We check concurrent execution of {@link FutureTask}
    */
   public void testSendSimpleMessageAsynch() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      @SuppressWarnings("unchecked")
      Future<Boolean>[] futures = new Future[THREAD_NUMBER];

      cleanEmailMessages();

      assertEquals("SMTP server should be now empty", 0, mailServer.getReceivedEmailSize());
      for (int i = 0; i < THREAD_NUMBER; i++)
      {
         assertFalse(isEmailMessageSent(MAIL_SUBJECT + i));
         futures[i] =
            service.sendMessageAsynch(generateRandomEmailSender(), generateRandomEmailRecipient(), MAIL_SUBJECT + i,
               MAIL_CONTENTS + i);
      }

      for (int i = 0; i < THREAD_NUMBER; i++)
      {
         assertTrue(futures[i].get());
         assertTrue(isEmailMessageSent(MAIL_SUBJECT + i));
      }
      //we assume that one thread sends one email
      assertEquals("SMTP server should have" + THREAD_NUMBER + " message (asynchronously sent)", THREAD_NUMBER,
         mailServer.getReceivedEmailSize());
   }


   /**
    * Here we test asynchronous email sending of {@link MimeMessage}.
    * We check if we can get real cause of exception, if that occurs during
    * message sending process. 
    */
   public void testSendMimeMessageAsynchExceptionCause() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      Flags flags = new Flags();
      flags.add(Flags.Flag.RECENT);

      Properties props = new Properties(System.getProperties());
      props.put("mail.smtp.port", SMTP_PORT + 1);
      Session session = Session.getDefaultInstance(props, null);

      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress(generateRandomEmailSender()));
      message.setRecipients(javax.mail.Message.RecipientType.TO, generateRandomEmailRecipient());
      message.setSubject(MAIL_SUBJECT);
      message.setContent(MAIL_CONTENTS, TEXT_PLAIN);
      message.setFlags(flags, true);

      Future<Boolean> future = service.sendMessageAsynch(message);

      try
      {
         future.get();
         fail();
      }
      catch (ExecutionException ee)
      {
         assertEquals("We tried to send mail with malformed SMTP port (" + SMTP_PORT + 1
            + "), so we expect a MessagingException to be the real cause of ExecutionException", ee.getCause()
            .getClass(), MessagingException.class);
      }
   }

   /**
    * Here we test asynchronous email sending of {@link MimeMessage}.
    * We check concurrent execution of {@link FutureTask}
    */
   public void testSendMimeMessageAsynch() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      @SuppressWarnings("unchecked")
      Future<Boolean>[] futures = new Future[THREAD_NUMBER];
      MimeMessage message;
      Flags flags = new Flags(Flags.Flag.RECENT);
      Session session = service.getMailSession();

      cleanEmailMessages();

      assertEquals("SMTP server should be now empty", 0, mailServer.getReceivedEmailSize());

      for (int i = 0; i < THREAD_NUMBER; i++)
      {
         assertFalse(isEmailMessageSent(MAIL_SUBJECT + i));

         message = new MimeMessage(session);
         message.setFrom(new InternetAddress(generateRandomEmailSender()));
         message.setRecipients(javax.mail.Message.RecipientType.TO, generateRandomEmailRecipient());
         message.setSubject(MAIL_SUBJECT + i);
         message.setContent(MAIL_CONTENTS + i, TEXT_PLAIN);
         message.setFlags(flags, true);

         futures[i] = service.sendMessageAsynch(message);
      }

      for (int i = 0; i < THREAD_NUMBER; i++)
      {
         assertTrue(futures[i].get());
         assertTrue(isEmailMessageSent(MAIL_SUBJECT + i));
      }
      //we assume that one thread sends one email
      assertEquals("SMTP server should have" + THREAD_NUMBER + " message (asynchronously sent)", THREAD_NUMBER,
         mailServer.getReceivedEmailSize());
   }

   /**
    * Here we test asynchronous email sending of {@link Message}.
    * We check if we can get real cause of exception, if that occurs during
    * message sending process. 
    */
   public void testSendMessageAsynchExceptionCause() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      Attachment attachment = new Attachment();
      attachment.setInputStream(new ByteArrayInputStream(ATTACHMENT.getBytes()));
      attachment.setMimeType(TEXT_PLAIN);

      Message message = new Message();
      message.setFrom("!@#$%^&*()");
      message.setTo(generateRandomEmailRecipient());
      message.setCC(generateRandomEmailRecipient() + "," + generateRandomEmailRecipient());
      message.setBCC(generateRandomEmailRecipient() + "," + generateRandomEmailRecipient());
      message.setSubject(MAIL_SUBJECT);
      message.setBody(MAIL_CONTENTS);
      message.setMimeType(TEXT_HTML);
      message.addAttachment(attachment);

      Future<Boolean> future = service.sendMessageAsynch(message);

      try
      {
         future.get();
         fail();
      }
      catch (ExecutionException ee)
      {
         assertEquals("We tried to send mail with malformed sender field,"
            + " so we expect an AdressException to be the real cause of ExecutionException", ee.getCause().getClass(),
            AddressException.class);
      }
   }

   /**
    * Here we test asynchronous email sending of {@link Message}.
    * We check concurrent execution of {@link FutureTask}
    */
   public void testSendMessageInFuture() throws Exception
   {
      if (!pingMailServer())
      {
         fail();
      }

      Message message;

      Attachment attachment = new Attachment();
      attachment.setInputStream(new ByteArrayInputStream(ATTACHMENT.getBytes()));
      attachment.setMimeType(TEXT_PLAIN);

      @SuppressWarnings("unchecked")
      Future<Boolean>[] futures = new Future[THREAD_NUMBER];

      cleanEmailMessages();

      assertEquals("SMTP server should be now empty", 0, mailServer.getReceivedEmailSize());

      for (int i = 0; i < THREAD_NUMBER; i++)
      {
         assertFalse(isEmailMessageSent(MAIL_SUBJECT + i));

         message = new Message();
         message.setFrom(generateRandomEmailSender());
         message.setTo(generateRandomEmailRecipient());
         message.setCC(generateRandomEmailRecipient() + "," + generateRandomEmailRecipient());
         message.setBCC(generateRandomEmailRecipient() + "," + generateRandomEmailRecipient());
         message.setSubject(MAIL_SUBJECT + i);
         message.setBody(MAIL_CONTENTS + i);
         message.setMimeType(TEXT_HTML);
         message.addAttachment(attachment);

         futures[i] = service.sendMessageAsynch(message);
         assertFalse(futures[i].isDone());
      }

      for (int i = 0; i < THREAD_NUMBER; i++)
      {
         assertTrue(futures[i].get());
         assertTrue(isEmailMessageSent(MAIL_SUBJECT + i));
      }
      //we assume that one thread sends one email
      assertEquals("SMTP server should have" + THREAD_NUMBER + " message (asynchronously sent)", THREAD_NUMBER,
         mailServer.getReceivedEmailSize());
   }

   private boolean pingMailServer() throws Exception
   {
      String mailServerName = service.getOutgoingMailServer();
      if (netService.ping(mailServerName, SMTP_PORT) < 0)
      {
         System.out.println("======>MailServer:" + mailServerName + " and on port:" + SMTP_PORT + " is not connected");
         return false;
      }
      return true;
   }

   /**
    * Utility method to clean mail server. 
    * Removes all stored messages one by one.
    */
   private void cleanEmailMessages()
   {
      if (mailServer.getReceivedEmailSize() > 0)
      {
         @SuppressWarnings("unchecked")
         Iterator<SmtpMessage> it = mailServer.getReceivedEmail();
         while (it.hasNext())
         {
            it.next();
            it.remove();
         }
      }
   }

   /**
    * Utility method to check if you really sent message
    * to dummy mail server. Basically it simply checks if
    * there is an email with defined 'subject' header.
    * @param subject 
    * @return
    */
   private boolean isEmailMessageSent(String subject)
   {
      if (mailServer.getReceivedEmailSize() > 0)
      {
         @SuppressWarnings("unchecked")
         Iterator<SmtpMessage> it = mailServer.getReceivedEmail();
         SmtpMessage message;
         while (it.hasNext())
         {
            message = it.next();
            if (message.getHeaderValue("Subject") != null && message.getHeaderValue("Subject").equals(subject))
            {
               return true;
            }
         }
      }
      return false;
   }

   /**
    * Utility method to generate random email sender String
    * @return {@link String} random email sender
    */
   private String generateRandomEmailSender()
   {
      return "<exo-sender" + System.currentTimeMillis() + "@localhost>";
   }

   /**
    * Utility method to generate random email recipient String
    * @return {@link String} random email recipient
    */
   private String generateRandomEmailRecipient()
   {
      return "<exo-recipient" + System.currentTimeMillis() + "@localhost>";
   }
}
