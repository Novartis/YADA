package com.novartis.opensource.yada.plugin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;

/**
 * A java API enabling execution of pre-existing scripts, in pre-defined locations on the YADA server, as Bypass plugins. 
 * For more information see the <a href="../../../../../../../guide.html">Users' Guide</a>
 * @author David Varon
 *
 */
public class ScriptBypass extends AbstractBypass {
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(ScriptBypass.class);
	
	/**
	 * Enables the execution of a script stored in the {@code YADA_BIN} directory.
	 * To execute a script bypass plugin, pass {@code bypassargs}, or just {@code args}
	 * the first argument being the name of the script executable, and the rest of the arguments
	 * those, in order, to pass to it. If {@link YADARequest#getBypassArgs()} is not null
	 * and {@link YADARequest#getPlugin()} is null, then the plugin will be set to 
	 * {@link YADARequest#SCRIPT_BYPASS} automatically.
	 * <p>
	 * The script should return a String intended to be returned to the requesting client.
	 * </p>
	 * @see com.novartis.opensource.yada.plugin.Bypass#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public String engage(YADARequest yadaReq) throws YADAPluginException
	{
		List<String> cmds = new ArrayList<>();
		// add args
		List<String> args = yadaReq.getBypassArgs().size() == 0 ? yadaReq.getArgs() : yadaReq.getBypassArgs();
		// add plugin
		try 
		{
			cmds.add(Finder.getEnv("yada_bin")+args.remove(0));
		} 
		catch (YADAResourceException e)
		{
			String msg = "There was a problem locating the resource or variable identified by the supplied JNDI path (yada_bin) in the initial context.";
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
