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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.mail.Attachment;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;
import org.exoplatform.services.net.NetService;
import org.exoplatform.test.BasicTestCase;

import java.io.ByteArrayInputStream;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * Created by The eXo Platform SAS Author : Phung Hai Nam phunghainam@gmail.com
 * Dec 23, 2005
 */
public class TestMailService extends BasicTestCase
{
   static private String EMAIL = "<exo@PC01>";

   static private int MAIL_PORT = 25;

   private MailService service_;

   private NetService nservice_;

   public TestMailService(String name)
   {
      super(name);
   }

   public void setUp() throws Exception
   {
      if (service_ == null)
      {
         PortalContainer pcontainer = PortalContainer.getInstance();
         service_ = (MailService)pcontainer.getComponentInstanceOfType(MailService.class);
         nservice_ = (NetService)pcontainer.getComponentInstanceOfType(NetService.class);
      }
   }

   public void testSendMineMessage() throws Exception
   {
      if (!pingMailServer())
         return;
      Properties props = new Properties(System.getProperties());
      Session session = Session.getDefaultInstance(props, null);
      session.setDebug(true);
      MimeMessage message = new MimeMessage(session);
      message.setFrom(new InternetAddress("exo@PC01"));
      message.setRecipients(javax.mail.Message.RecipientType.TO, EMAIL);
      message.setSubject("Test Java Mail Service");
      message.setContent("This is the contents of the Java Mail !", "text/plain");
      Flags flags = new Flags();
      flags.add(Flags.Flag.RECENT);
      message.setFlags(flags, true);
      service_.sendMessage(message);
   }

   public void testSendMessage() throws Exception
   {
      if (!pingMailServer())
         return;
      ByteArrayInputStream is = new ByteArrayInputStream("===> Attachement text".getBytes());
      String TO = "<exo@PC01>";
      String FROM = TO;
      String CC = "<exo@PC01>, <exo@PC01>";
      String BCC = "<exo@PC01>, <exo@PC01>";
      String subject = "Subject test";
      String contents = "This is test sendMessage method !";
      String mimeType = "text/html";
      Message message = new Message();
      message.setFrom(FROM);
      message.setTo(TO);
      message.setCC(CC);
      message.setBCC(BCC);
      message.setSubject(subject);
      message.setBody(contents);
      message.setMimeType(mimeType);
      Attachment attachment = new Attachment();
      attachment.setInputStream(is);
      attachment.setMimeType("text/plain");
      message.addAttachment(attachment);
      service_.sendMessage(message);
   }

   public void testSendMessageWithInfo() throws Exception
   {
      if (!pingMailServer())
         return;
      String TO = "<exo@PC01>";
      String FROM = TO;
      String subject = "Subject test";
      String contents = "This is test sendMessage method with 4 parameter !";
      service_.sendMessage(FROM, TO, subject, contents);
   }

   private boolean pingMailServer() throws Exception
   {
      String mailServer = service_.getOutgoingMailServer();
      if (nservice_.ping(mailServer, MAIL_PORT) < 0)
      {
         System.out.println("======>MailServer:" + mailServer + " and on port:" + MAIL_PORT + " is not connected");
         return false;
      }
      return true;
   }
}
