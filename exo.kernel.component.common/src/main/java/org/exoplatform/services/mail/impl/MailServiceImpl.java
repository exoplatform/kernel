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
package org.exoplatform.services.mail.impl;

import org.exoplatform.commons.utils.PrivilegedSystemHelper;
import org.exoplatform.commons.utils.SecurityHelper;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.mail.Attachment;
import org.exoplatform.services.mail.MailService;
import org.exoplatform.services.mail.Message;

import java.io.InputStream;
import java.security.PrivilegedAction;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;

import javax.activation.DataHandler;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;

/**
 * Basically this is {@link MailService} implementation build on top of javax.mail package.
 * You may define the behaviour of the service via {@link InitParams}, which can
 * be set in configuration file of the {@link ExoContainer}.
 * <p>
 * Note: To be able to send mails you must provide active SMTP server and
 * mention it in service configuration. 
 * <p>
 * Created by The eXo Platform SAS Author : Phung Hai Nam phunghainam@gmail.com
 * Dec 23, 2005
 */
public class MailServiceImpl implements MailService
{
   /**
    * String mapping of configuration parameter to define maximal number
    * of threads for asynchronous mail message sending
    */
   static final String MAX_THREAD_NUMBER = "mail.max.thread.number";

   private Session mailSession_;

   private Properties props_;

   /**
    * Provides thread pool routines for asynchronous mail message sending
    */
   private ExecutorService executorService;

   /**
    * Track current threads number used for asynchronouys mail send
    * to set explicit thread names.
    */
   private static volatile int mailServiceThreadCounter = 0;

   public MailServiceImpl(InitParams params, final ExoContainerContext ctx) throws Exception
   {
      props_ = new Properties(PrivilegedSystemHelper.getProperties());
      props_.putAll(params.getPropertiesParam("config").getProperties());
      if ("true".equals(props_.getProperty("mail.smtp.auth")))
      {
         String username = props_.getProperty("mail.smtp.auth.username");
         String password = props_.getProperty("mail.smtp.auth.password");
         final ExoAuthenticator auth = new ExoAuthenticator(username, password);
         mailSession_ = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Session>()
         {
            public Session run()
            {
               return Session.getInstance(props_, auth);
            }
         });
      }
      else
      {
         mailSession_ = SecurityHelper.doPrivilegedAction(new PrivilegedAction<Session>()
         {
            public Session run()
            {
               return Session.getInstance(props_, null);
            }
         });
      }
      int threadNumber =
         props_.getProperty(MAX_THREAD_NUMBER) != null ? Integer.valueOf(props_.getProperty(MAX_THREAD_NUMBER))
            : Runtime.getRuntime().availableProcessors();

      executorService = Executors.newFixedThreadPool(threadNumber, new ThreadFactory()
      {
         public Thread newThread(Runnable arg0)
         {
            return new Thread(arg0, ctx.getName() + "-MailServiceThread-" + mailServiceThreadCounter++);
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   public Session getMailSession()
   {
      return mailSession_;
   }

   /**
    * {@inheritDoc}
    */
   public String getOutgoingMailServer()
   {
      return props_.getProperty("mail.smtp.host");
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(String from, String to, String subject, String body) throws Exception
   {
      Message message = new Message();
      message.setFrom(from);
      message.setTo(to);
      message.setSubject(subject);
      message.setBody(body);
      sendMessage(message);
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(Message message) throws Exception
   {
      MimeMessage mimeMessage = new MimeMessage(getMailSession());
      String FROM = message.getFrom();
      String TO = message.getTo();
      String CC = message.getCC();
      String BCC = message.getBCC();
      String subject = message.getSubject();
      String mimeType = message.getMimeType();
      String body = message.getBody();
      List<Attachment> attachment = message.getAttachment();
      // set From to the message
      if (FROM != null && !FROM.equals(""))
      {
         InternetAddress sentFrom = new InternetAddress(FROM);
         mimeMessage.setFrom(sentFrom);
      }
      // set To to the message
      InternetAddress[] sendTo = new InternetAddress[getArrs(TO).length];
      for (int i = 0; i < getArrs(TO).length; i++)
      {
         sendTo[i] = new InternetAddress(getArrs(TO)[i]);
      }
      mimeMessage.setRecipients(javax.mail.Message.RecipientType.TO, sendTo);
      // set CC to the message
      if ((getArrs(CC) != null) && (getArrs(CC).length > 0))
      {
         InternetAddress[] copyTo = new InternetAddress[getArrs(CC).length];
         for (int i = 0; i < getArrs(CC).length; i++)
         {
            copyTo[i] = new InternetAddress(getArrs(CC)[i]);
         }
         mimeMessage.setRecipients(javax.mail.Message.RecipientType.CC, copyTo);
      }
      // set BCC to the message
      if ((getArrs(BCC) != null) && (getArrs(BCC).length > 0))
      {
         InternetAddress[] bccTo = new InternetAddress[getArrs(BCC).length];
         for (int i = 0; i < getArrs(BCC).length; i++)
         {
            bccTo[i] = new InternetAddress(getArrs(BCC)[i]);
         }
         mimeMessage.setRecipients(javax.mail.Message.RecipientType.BCC, bccTo);
      }
      // set Subject to the message
      mimeMessage.setSubject(subject);
      mimeMessage.setSubject(message.getSubject(), "UTF-8");
      mimeMessage.setSentDate(new Date());

      MimeMultipart multipPartRoot = new MimeMultipart("mixed");

      MimeMultipart multipPartContent = new MimeMultipart("alternative");

      if (attachment != null && attachment.size() != 0)
      {
         MimeBodyPart contentPartRoot = new MimeBodyPart();
         if (mimeType != null && mimeType.indexOf("text/plain") > -1)
            contentPartRoot.setContent(body, "text/plain; charset=utf-8");
         else
            contentPartRoot.setContent(body, "text/html; charset=utf-8");
         MimeBodyPart mimeBodyPart1 = new MimeBodyPart();
         mimeBodyPart1.setContent(body, mimeType);
         multipPartContent.addBodyPart(mimeBodyPart1);
         multipPartRoot.addBodyPart(contentPartRoot);
         for (Attachment att : attachment)
         {
            InputStream is = att.getInputStream();
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            ByteArrayDataSource byteArrayDataSource = new ByteArrayDataSource(is, att.getMimeType());
            mimeBodyPart.setDataHandler(new DataHandler(byteArrayDataSource));

            mimeBodyPart.setDisposition(Part.ATTACHMENT);
            if (att.getName() != null)
               mimeBodyPart.setFileName(MimeUtility.encodeText(att.getName(), "utf-8", null));
            multipPartRoot.addBodyPart(mimeBodyPart);
         }
         mimeMessage.setContent(multipPartRoot);
      }
      else
      {
         if (mimeType != null && mimeType.indexOf("text/plain") > -1)
            mimeMessage.setContent(body, "text/plain; charset=utf-8");
         else
            mimeMessage.setContent(body, "text/html; charset=utf-8");
      }
      sendMessage(mimeMessage);
   }

   /**
    * {@inheritDoc}
    */
   public void sendMessage(MimeMessage message) throws Exception
   {
      Transport.send(message);
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Future<Boolean> sendMessageInFuture(final String from, final String to, final String subject,
      final String body)
   {
      return executorService.submit(new Callable<Boolean>()
      {
         @Override
         public Boolean call() throws Exception
         {
            sendMessage(from, to, subject, body);
            return true;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Future<Boolean> sendMessageInFuture(final Message message)
   {
      return executorService.submit(new Callable<Boolean>()
      {
         @Override
         public Boolean call() throws Exception
         {
            sendMessage(message);
            return true;
         }
      });
   }

   /**
    * {@inheritDoc}
    */
   @Override
   public Future<Boolean> sendMessageInFuture(final MimeMessage message)
   {
      return executorService.submit(new Callable<Boolean>()
      {
         @Override
         public Boolean call() throws Exception
         {
            sendMessage(message);
            return true;
         }
      });
   }

   protected String[] getArrs(String toArray)
   {
      if (toArray != null && !toArray.equals(""))
      {
         return toArray.split(",");
      }
      return null;
   }

}
