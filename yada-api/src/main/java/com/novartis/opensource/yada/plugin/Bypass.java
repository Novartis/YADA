package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;

/**
 * The Bypass Plugin API is designed to enable processing of data and requests that almost completely 
 * circumvent (or override) the conventional YADA framework request/service process flow.
 * 
 * @author David Varon
 */
public interface Bypass {
	/**
	 * The public API for Bypass plugins
	 * @param yadaReq YADA request configuration
	 * @return String intended to be returned to the requestor, as content in a typical YADA response. 
	 * @throws YADAPluginException when the plugin execution fails
	 */
	public String engage(YADARequest yadaReq)
		throws YADAPluginException;
	
	/**
	 * The public API for QueryBypass
	 * @param yadaReq YADA request configuration
	 * @param yq the current {@link YADAQuery}
	 * @return a result object that will be integrated into the result set by the {@link com.novartis.opensource.yada.Service} object
	 * @throws YADAPluginException when plugin execution fails
	 */
	public YADAQueryResult engage(YADARequest yadaReq, YADAQuery yq)
	    throws YADAPluginException;
}
