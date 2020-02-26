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
package com.novartis.opensource.yada.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;
import java.net.HttpCookie;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;

/**
 * A java API enabling execution of pre-existing scripts, in pre-defined locations on the YADA server, as Postprocess plugins.
 * For more information see the <a href="../../../../../../../plugins.html">Plugin Guide</a>
 * @author David Varon
 *
 */
public class ScriptPostprocessor extends AbstractPostprocessor {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(ScriptPostprocessor.class);
	/**
	 * Constant equal to: {@value}
	 */
	private final static String INIT_RESULT	= "initResult_";	  
	
	/**
	 * Enables the execution of a script stored in the {@code yada.bin} directory.
	 * To execute a script postprocessor plugin, pass {@code postArgs}, or just {@code args}
	 * the first argument being the name of the script executable, and the rest of the arguments
	 * those, in order, to pass to it. If {@link YADARequest}{@code .getPostArgs()} is not null
	 * and {@link YADARequest#getPlugin()} is null, then the plugin will be set to 
	 * {@link YADARequest#SCRIPT_POSTPROCESSOR} automatically.
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public String engage(YADARequest yadaReq, String result) throws YADAPluginException 
	{
		// write the result to a tmp file so the script can easily access it
		// We don't want to wrestle with encoding, line feeds, spaces, etc by passing it as a string
		File tmpResult;
    try
    {
      tmpResult = File.createTempFile(INIT_RESULT, "");
    }
    catch (IOException e)
    {
      String msg = "Could not create temp file";
      throw new YADAPluginException(msg,e);
    }
		try(PrintWriter out = new PrintWriter(tmpResult)) 
		{
			out.write(result);
		} 
		catch (IOException e) 
		{
			String msg = "Could not write to temp file";
			throw new YADAPluginException(msg, e);
		}
		List<String> cmds = new ArrayList<>();
		// get args
		List<String> args = yadaReq.getPostArgs().size() == 0 ? yadaReq.getArgs() : yadaReq.getPostArgs();
		// add plugin
		try 
		{
		  // first arg to cmds is the executable script name passed in the postargs parameter
			cmds.add(Finder.getEnv("yada.bin")+args.remove(0));
		} 
		catch (YADAResourceException e)
		{
			String msg = "There was a problem locating the resource or variable identified by the supplied JNDI path (yada.bin) in the initial context.";
			throw new YADAPluginException(msg,e);
		}
		// add args
		cmds.addAll(args);
		// add results path as last argument to executable
		cmds.add(tmpResult.getAbsolutePath());
		// override the yadaReq.cookie parameter to contain the named cookie
		// values instead of the names alone, so that the values can be utilized
		// in the postprocessor script which does not have any access to the
		// HttpServletRequest object that its java counterparts do
		
		// Orig list of cookie names passed to yada request
		boolean      takeAllCookies = yadaReq.getCookies().get(0).equalsIgnoreCase("true");
		List<String> cookieStrings = new ArrayList<String>();
		
		// Orig cookie objects (name/value pairs) stored in http request
		Cookie[] cookies = yadaReq.getRequest().getCookies();		
		
		// iterate over cookie array and store
    if (cookies != null)
    {
      for (Cookie c : cookies)
      {
        if (takeAllCookies || yadaReq.getCookies().contains(c.getName()))
        {
        	HttpCookie hc = new HttpCookie(c.getName(), c.getValue());
        	hc.setComment(c.getComment());
        	hc.setDomain(c.getDomain());
        	hc.setMaxAge(c.getMaxAge());
        	hc.setPath(c.getPath());        	
        	hc.setSecure(c.getSecure());
        	hc.setVersion(c.getVersion());
        	cookieStrings.add(hc.toString());
        }
      }
    }    
    if(yadaReq.getParameterMap().containsKey(YADARequest.PS_COOKIES))
    {
    	yadaReq.getParameterMap().put(YADARequest.PS_COOKIES,cookieStrings.toArray(new String[] {}));
    }
    else
    {
    	yadaReq.getParameterMap().put(YADARequest.PL_COOKIES,cookieStrings.toArray(new String[] {}));
    }
		
		// add yadaReq json
		cmds.add(yadaReq.toString());
		l.debug("Executing script plugin: "+cmds);
		String scriptResult = "";
		String s            = null;
		try
		{
			ProcessBuilder builder = new ProcessBuilder(cmds);
			builder.redirectErrorStream(true);
			Process process = builder.start(); // send a security exception when the permission denied
			try(BufferedReader si = new BufferedReader(new InputStreamReader(process.getInputStream())))
			{
  			while ((s = si.readLine()) != null)
  			{
  				l.debug("  LINE: "+s);
  				scriptResult += s;
  			}
			}
			process.waitFor();
		}
		catch(IOException e)
		{
			String msg = "Failed to get input from InputStream.";
			throw new YADAPluginException(msg,e);
		}
		catch(InterruptedException e)
		{
			String msg = "The external process executing the script was interrupted.";
			throw new YADAPluginException(msg,e);
		}
		return scriptResult;
	}
	
}
