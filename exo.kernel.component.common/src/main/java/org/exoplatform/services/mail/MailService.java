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
package org.exoplatform.services.mail;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * Interface provides basic operations for sending mail messages and mail service 
 * configuration data retrieval. Defines synchronous and asynchronous methods
 * responsible for sending mail message. They can receive parameters of different types
 * to create a mail send. You can pass {@link Message}, {@link MimeMessage}
 * or specify mail message data explicitly via String parameters.
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 13, 2004
 * @version $Id: MailService.java 5332 2006-04-29 18:32:44Z geaz $
 */
public interface MailService
{
   /**
    * Provides {@link Session} instance, which is to be used throughout {@link MailService} methods
    * @return {@link Session}
    */
   public Session getMailSession();

   /**
    * Provides outgoing server information, which is basically its hostname or ip address.
    * This server is used as transceiver for mail messages. {@link MailService} should send message 
    * to the server first and than server will resend messages to the receivers.
    * @return
    */
   public String getOutgoingMailServer();

   /**
    * Sends mail message based on passed {@link String} parameters.
    * @param from - {@link String} identificator of mail sender. For example 'test.sender@test.test'
    * @param to - {@link String} identificator of mail receiver. For example 'test.receiver@test.test'
    * @param subject - {@link String} subject of mail message
    * @param body - {@link String} contents of mail message
    * @throws Exception is thrown if something's gone wrong during mail send procedure
    */
   public void sendMessage(String from, String to, String subject, String body) throws Exception;

   /**
    * Sends mail message based on {@link Message} instance
    * @param message - {@link Message} provides mail message related data (e.g. subject, content etc.)
    * @throws Exception is thrown if something's gone wrong during mail send procedure
    */
   public void sendMessage(Message message) throws Exception;

   /**
    * Sends mail message based on {@link MimeMessage} instance
    * @param message - {@link MimeMessage} provides mail message related data (e.g. subject, content etc.)
    * @throws Exception is thrown if something's gone wrong during mail send procedure
    */
   public void sendMessage(MimeMessage message) throws Exception;

   /**
    * Asynchronous variant of {@link MailService#sendMessage(String, String, String, String)}. 
    * Returns {@link Future} object, which allows to track mail sending result. Calling {@link Future#get()}
    * for this object returns {@link Boolean#TRUE} if mail is sent successfully,
    * throws {@link ExecutionException} if some exception occured during mail sending. 
    * Calling {@link ExecutionException#getCause()} for the thrown exception object provides the exception,
    * which indeed occured during sending mail. 
    * @param from - {@link String} identificator of mail sender. For example 'test.sender@test.test'
    * @param to - {@link String} identificator of mail receiver. For example 'test.receiver@test.test'
    * @param subject - {@link String} subject of mail message
    * @param body - {@link String} contents of mail message
    * @return {@link Future} object to watch the result of asynchronous calculation
    */
   public Future<Boolean> sendMessageInFuture(String from, String to, String subject, String body);

   /**
    * Asynchronous variant of {@link MailService#sendMessage(Message)}. 
    * Returns {@link Future} object, which allows to track mail sending result. Calling {@link Future#get()}
    * for this object returns {@link Boolean#TRUE} if mail is sent successfully,
    * throws {@link ExecutionException} if some exception occured during mail sending. 
    * Calling {@link ExecutionException#getCause()} for the thrown exception object provides the exception,
    * which indeed occured during sending mail. 
    * @param message - {@link Message} provides mail message related data (e.g. subject, content etc.)
    * @return {@link Future} object to watch the result of asynchronous calculation
    */
   public Future<Boolean> sendMessageInFuture(Message message);

   /**
    * Asynchronous variant of {@link MailService#sendMessage(MimeMessage)}. 
    * Returns {@link Future} object, which allows to track mail sending result. Calling {@link Future#get()}
    * for this object returns {@link Boolean#TRUE} if mail is sent successfully,
    * throws {@link ExecutionException} if some exception occured during mail sending. 
    * Calling {@link ExecutionException#getCause()} for the thrown exception object provides the exception,
    * which indeed occured during sending mail. 
    * @param message - {@link MimeMessage} provides mail message related data (e.g. subject, content etc.)
    * @return {@link Future} object to watch the result of asynchronous calculation
    */
   public Future<Boolean> sendMessageInFuture(MimeMessage message);
}
