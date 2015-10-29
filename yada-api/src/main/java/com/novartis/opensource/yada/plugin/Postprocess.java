package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;

/**
 * The Postprocess Plugin API is intended to provide a means for transformation of query results
 * after retrieval from data sources, before returning to the client.
 * @author David Varon
 *
 */
public interface Postprocess {
	/**
	 * The public API for request result post-processing
	 * @param yadaReq YADA request configuration
	 * @param result the content to transform
	 * @return a {@link String} containing the transformed content
	 * @throws YADAPluginException when any error occurs during processing
	 */
	public String engage(YADARequest yadaReq, String result) 
		throws YADAPluginException;
	
	/**
	 * The public API for query result post-processing
	 * @param yq the query to post process	
	 * @throws YADAPluginException when plugin execution fails
	 */
	public void engage(YADAQuery yq)
	 	throws YADAPluginException;
}
