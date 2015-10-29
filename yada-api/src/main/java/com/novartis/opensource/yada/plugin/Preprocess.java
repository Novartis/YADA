package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;

/**
 * The Preprocess Plugin API is intended to provide a means for transformation of parameters sent in the 
 * request, before execution of it's queries.
 * @author David Varon
 *
 */
public interface Preprocess {

	/**
	 * The Request Preprocessor is for manipulation of request parameters prior to query execution
	 * @param yadaReq YADA request configuration
	 * @return a new and presumably modified {@link YADARequest} object
	 * @throws YADAPluginException when any error occurs during processing
	 */
	public YADARequest engage(YADARequest yadaReq)
		throws YADAPluginException;
	
	/**
	 * The Query Preprocessor is for manipulation of the request parameters or execution of filtering or security 
	 * on the specified query
	 * @param yadaReq the current {@link YADARequest}
	 * @param yq the current {@link YADAQuery}
	 * @throws YADAPluginException when any error occurs during processing
	 */
	public void engage(YADARequest yadaReq, YADAQuery yq) 
	  throws YADAPluginException;
}
