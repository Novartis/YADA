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
