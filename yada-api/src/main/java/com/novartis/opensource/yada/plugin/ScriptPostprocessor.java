package com.novartis.opensource.yada.plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
	 * Enables the execution of a script stored in the {@code YADA_BIN} directory.
	 * To execute a script postprocessor plugin, pass {@code preArgs}, or just {@code args}
	 * the first argument being the name of the script executable, and the rest of the arguments
	 * those, in order, to pass to it. If {@link YADARequest#getPostArgs()} is not null
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
		// add results path
		cmds.add(tmpResult.getAbsolutePath());
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
