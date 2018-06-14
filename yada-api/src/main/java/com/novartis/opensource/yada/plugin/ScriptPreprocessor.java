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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.JdbcFinder;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADAResourceException;


/**
 * A java API enabling execution of pre-existing scripts, in pre-defined locations on the YADA server, as Preprocess plugins.
 * For more information see <a href="../../../../../../../plugins.html">Plugin Guide</a>
 * @author David Varon
 *
 */
public class ScriptPreprocessor extends AbstractPreprocessor {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(ScriptPreprocessor.class);
	
	/**
	 * Enables the execution of a script stored in the {@code yada.bin} directory.
	 * To execute a script preprocessor plugin, pass {@code preArgs}, or just {@code args}
	 * the first argument being the name of the script executable, and the rest of the arguments
	 * those, in order, to pass to it. If {@link YADARequest}{@code .getPreArgs()} is not null
	 * and {@link YADARequest#getPlugin()} is null, then the plugin will be set to 
	 * {@link YADARequest#SCRIPT_PREPROCESSOR} automatically.  
	 * <p>
	 * The script must return a JSON object with name/value pairs corresponding
	 * to {@link YADARequest} parameters. These name/value pairs are then marshaled into
	 * {@code yadaReq}, which is subsequently returned by the method.
	 * </p>
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public YADARequest engage(YADARequest yadaReq) throws YADAPluginException 
	{
		List<String> cmds = new ArrayList<>();
		// get args
		List<String> args = yadaReq.getPreArgs().size() == 0 ? yadaReq.getArgs() : yadaReq.getPreArgs();
		// add plugin
		try 
		{
			cmds.add(JdbcFinder.getEnv("yada.bin")+args.remove(0));
		} 
		catch (YADAResourceException e)
		{
			String msg = "There was a problem locating the resource or variable identified by the supplied JNDI path (yada.bin) in the initial context.";
			throw new YADAPluginException(msg,e);
		}
		// add args
		cmds.addAll(args);
		for (String arg : args)
		{
			cmds.add(arg);
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
			Process process = builder.start();
			try(BufferedReader si = new BufferedReader(new InputStreamReader(process.getInputStream())))
			{
  			while ((s = si.readLine()) != null)
  			{
  				l.debug("  LINE: "+s);
  				scriptResult += s;
  			}
			}
			process.waitFor();
			JSONObject params = new JSONObject(scriptResult);
			for ( String param : JSONObject.getNames(params))
			{
				JSONArray ja     = (JSONArray) params.get(param);
				l.debug("JSON array "+ja.toString());
				// remove square brackets and leading/trailing quotes
				String    values = ja.toString().substring(2, ja.toString().length()-2);
				// remove "," between values and stuff in array
				String[]  value  = values.split("\",\"");
				l.debug("Value has " + value.length + " elements");
				yadaReq.invokeSetter(param, value);
			}
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
		catch(JSONException e)
		{
		  String msg  = "Parameter configuration failed. The script return value should conform to the syntax:\n";
		         msg += "  {\"key\":[\"value\"...],...}.\n";
		         msg += "  The array syntax for values complies to the return value of HTTPServletResponse.getParameterMap().toString()";
			throw new YADAPluginException(msg,e);
		}
		catch (YADARequestException e)
		{
			String msg = "Unable to set parameters.";
			throw new YADAPluginException(msg,e);
		}
		
		return yadaReq;
	}
}
