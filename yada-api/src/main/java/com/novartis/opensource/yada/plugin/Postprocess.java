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
