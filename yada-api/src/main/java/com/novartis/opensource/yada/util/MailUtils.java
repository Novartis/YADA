/**
 * Copyright 2016 Novartis Institutes for BioMedical Research Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.novartis.opensource.yada.util;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.plugin.EmailBypassPlugin;

/**
 * A utility class for creation and transmission of email messages.
 * @author David Varon
 * @see EmailBypassPlugin
 */
public class MailUtils {

	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(MailUtils.class);
	
	/**
	 * Retrieves the current mail session object from the application context
	 * @return the current mail session object from the application context
	 */
	public Session getSession() {
		l.debug("Creating session");
		Context initCtx;
		Context envCtx;
		Session session = null;
		try {
			initCtx = new InitialContext();
			envCtx = (Context) initCtx.lookup("java:comp/env");
			session = (Session) envCtx.lookup("mail/Session");
		} catch (NamingException e) {
			e.printStackTrace();
		}
		return session;
	}
	
	/**
	 * <p>
	 * <code>
	 * int result = MailUtils.sendMessage(new MailUtils().getSession(), from, to, subject, content);
	 * </code>
	 * </p>
	 * <p>
	 * Returns 0 for exceptions, 1 for success.
	 * </p>
	 * @param session the mail session obtained from the {@code InitialContext}
	 * @param from the sender
	 * @param to the recipient
	 * @param cc more recipients
	 * @param subject the subject line of the message
	 * @param content the message body
	 * @return int 0 for exceptions, 1 if message sent successfully
	 */
	public static int sendMessage(Session session, String from, String to, String cc, String subject, String content) {
		l.debug("Creating message...");
		Message message = null;
		try {
			message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));
			InternetAddress toList[] = InternetAddress.parse(to);
			message.setRecipients(Message.RecipientType.TO, toList);
			InternetAddress ccList[] = InternetAddress.parse(cc);
			message.setRecipients(Message.RecipientType.CC, ccList);
			message.setSubject(subject);
			message.setContent(content, "text/plain");
			l.debug("Sending message...");
			Transport.send(message);
		} catch (AddressException e) {
			e.printStackTrace();
			return 0;
		} catch (MessagingException e) {
			e.printStackTrace();
			return 0;
		}
		return 1;
	}	
}
