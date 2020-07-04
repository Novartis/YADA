package com.novartis.opensource.yada.plugin;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;

/**
 * @author jfinn
 * @since 8.7.6
 *
 */
public class SourceExchanger extends AbstractPreprocessor{
	
	/**
	 * Local logger handle
	 */
	private static final Logger LOG = Logger.getLogger(SourceExchanger.class);

	@Override	
	public YADARequest engage(YADARequest yadaReq) throws YADAPluginException
	{
		return yadaReq;
	}


	@Override
	public void engage(YADARequest yadaReq, YADAQuery yq) throws YADAPluginException
	{	
		// Grab the arguments, keep the first of any as the new source
		try {
			Collection<String> arg = yadaReq.getArgs();
			Iterator<String> arrg = arg.iterator();
			//this picks the first argument, if we want to grab the last argument in the list use while (arrg.hasNext())
			if ( arg != null && !arg.isEmpty()) { 
				yq.setApp(arrg.next());
			}
			LOG.debug(yq.getApp());
		} catch (NullPointerException e) {
			 String msg = "SourceExchanger plugin SOURCE is null. Put the name of the APP housing the SOURCE you want to use into the URL querystring -> ?q=[QUERY]&pl=SourceExchanger,[APP CONTAINING DESIRED SOURCE CONFIGURATION]";
		     throw new YADAPluginException(msg,e);			
		} catch (Exception e) {
			String msg = "Something happened in SourceExchanger.";	
	        throw new YADAPluginException(msg,e);					
		}
	}	
}
