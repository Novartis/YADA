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

/**
 * 
 */
package com.novartis.opensource.yada.format;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryResult;

/**
 * @since 0.4.0.0
 * @author David Varon
 *
 */
public class RESTResultJSONConverter extends AbstractConverter
{
	/**
	 * Local logger handle
	 */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(RESTResultJSONConverter.class);
	
	/**
	 * Default constructor
	 */
	public RESTResultJSONConverter() {
	  // default constructor
	}
	
	/**
	 * Constructor with {@link YADAQueryResult}
	 * @param yqr the container for result processing artifacts
	 */
	public RESTResultJSONConverter(YADAQueryResult yqr) {
	  this.setYADAQueryResult(yqr);
	}
	
	/**
	 * Wraps the result of the REST request in a json object
	 * @see com.novartis.opensource.yada.format.AbstractConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object result) 
	{
		//TODO implement harmony map solution for this
		JSONArray arrayResult = new JSONArray();
		
		try
		{
			arrayResult = new JSONArray((String)result);
		} 
		catch (JSONException e)
		{
			try
			{
				arrayResult = new JSONArray();
				arrayResult.put(new JSONObject((String)result));
			}
			catch(JSONException e1)
			{
				arrayResult.put(result);	
			}
		}
		return arrayResult;
	}
	
//	private Object harmonize(Object o) {
//	  JSONObject hm = (JSONObject)this.harmonyMap;
//	  for(String key : JSONObject.getNames(hm))
//    {
//      if(hasKey(hm,key))
//      {
//        
//      }
//    }
//	}
	
//	private boolean hasKey(JSONObject j, String key) {
//	  boolean hasKey = false;
//	  if(key.indexOf('.') > -1) 
//	  {
//	    String[] keys = key.split(".");
//	    Object j1 = j.optJSONObject(keys[0]);
//	    if(null == j1)
//	      j1 = j.optJSONArray(keys[0]);
//	    hasKey = hasKey(, keys[1] );
//	  }
//	  else
//	  {
//	    hasKey = j.has(key);
//	  }
//	  return hasKey;
//	}
}
