/**
 * Copyright 2015 Novartis Institutes for BioMedical Research Inc.
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
package com.novartis.opensource.yada.plugin;

import javax.mail.Session;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.util.MailUtils;

/**
 * Provides a convenient platform for sending emails from YADA client apps, particularly 
 * @author David Varon
 * @since 0.3.0.0
 */
public class EmailBypassPlugin extends AbstractBypass {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(EmailBypassPlugin.class);
	
	/**
	 * Extracts parameters from {@link YADARequest#getMail()} spec and invokes {@link MailUtils#sendMessage(Session, String, String, String, String, String)}
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public String engage(YADARequest yadaReq) throws YADAPluginException {
		String   result  = String.valueOf(0);
		try 
		{
			JSONObject json    = new JSONObject(yadaReq.getMail());
			String     from    = json.getString("from");
			String     to      = json.getString("to");
			String     cc      = json.getString("cc");
			String     subject = json.getString("subject");
			String 	   content = json.getString("content");
			
			Session session = new MailUtils().getSession();
			l.debug("Sending mail...");
			l.debug("  From ["+from+"]");
			l.debug("  To   ["+to+"]");
			l.debug("  CC   ["+cc+"]");
			l.debug("  Subj ["+subject+"]");
			result = String.valueOf(MailUtils.sendMessage(session, from, to, cc, subject, content));
		} 
		catch (JSONException e) 
		{
			e.printStackTrace();
		}
		 
		
		
		return result;
	}

}
