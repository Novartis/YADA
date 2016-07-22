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

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADARequest;

/**
 * @author David Varon
 *
 */
public class TestPreprocessor extends AbstractPreprocessor
{
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(TestPreprocessor.class);

	/**
	 * Executes the query defined in {@code yadaReq} and analyzes the results.  If the
	 * result count is 0, a new request with an {@code INSERT} is query is created. If
	 * the result count is >0, a new request with a {@code DELETE} query is created.
	 * The new reqeust is then returned from the method.   
	 * @see com.novartis.opensource.yada.plugin.Preprocess#engage(com.novartis.opensource.yada.YADARequest)
	 */
	@Override
	public YADARequest engage(YADARequest yadaReq) throws YADAPluginException
	{
	  // store the original qname
		String      qname    = yadaReq.getQname();
		// create a new request object
		YADARequest lyadaReq = new YADARequest();

		// set the qname
		lyadaReq.setQname(new String[] { qname });
		// execute the new request with the original qname
		Service     svc       = new Service(lyadaReq);
		// store the result
		String      result    = svc.execute();
		
		l.debug(result);
		
		JSONObject res;
		
		try
		{
		  // eval the result as json
			res = new JSONObject(result);
			// if the result doesn't contain data, create another new request to insert it
			if(res.has("RESULTSET") && res.getJSONObject("RESULTSET").getInt("total") == 0)
			{
				lyadaReq = new YADARequest();
				lyadaReq.setQname(new String[] { "YADA test INSERT" });
				lyadaReq.setParams(new String[] { "A,10,7.5,26-SEP-2014" });
			}
			// if the result contains data, create a new request to delete it
			else if(res.has("RESULTSET") && res.getJSONObject("RESULTSET").getInt("total") > 0)
			{
				lyadaReq = new YADARequest();
				lyadaReq.setQname(new String[] { "YADA test DELETE" });
			}
			else
			// if the result is non-conforming, throw an exception
			{
				throw new YADAPluginException("Plugin failed.");
			}
		} 
		catch (JSONException e)
		{
		  throw new YADAPluginException("Unable to parse result.");
		}
		
    // plugins shouldn't stomp on other plugins
    lyadaReq.setPlugin(yadaReq.getPluginConfig());
		
    // return the new locally created request object
    return lyadaReq;
	}
}
