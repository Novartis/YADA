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
/**
 * 
 */
package com.novartis.opensource.yada.plugin;

import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASecurityException;

/**
 * @author David Varon
 * @since 0.4.2
 */
public abstract class AbstractPostprocessor implements Postprocess, Authorization
{
	/**
	 * Null implementation
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public void engage(YADAQuery yq) throws YADAPluginException { /* nothing to do */ }
	
	/**
	 * Null implementation
	 * @throws YADAPluginException when there is a processing error
	 * @see com.novartis.opensource.yada.plugin.Postprocess#engage(com.novartis.opensource.yada.YADARequest, java.lang.String)
	 */
	@Override
	public String engage(YADARequest yadaReq, String result) throws YADAPluginException { return null; }

	/**
	 * Authorization of query use for given context {@link Authorization#authorize()}
	 * @since 8.7.6
	 */
	@Override
	public void authorize() throws YADASecurityException
	  {
	    // nothing to do			  
	  }

	/**
	 * Authorization of general use for given context {@link Authorization#authorize()}
	 * Not implemented in preprocessor
	 * @return 
	 * @since 8.7.6
	 */
	@Override
	public void authorize(String payload) throws YADASecurityException 
	  {
		// nothing to do
	  }


	
}
