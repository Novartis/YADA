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
package com.novartis.opensource.yada.format;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.io.YADAIOException;

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
	 * @throws YADAResourceException when the path to {@code r.js} or {@code harmony.js} is corrupt
   * @throws YADAIOException when the path to {@code r.js} or {@code harmony.js} can't be read
   * @throws YADAConverterException when the Rhino javascript engine throws an error
	 */
	public RESTResultJSONConverter() throws YADAResourceException, YADAIOException, YADAConverterException {
	  // default constructor
//	  setHarmonizer(new Harmonizer());
	}
	
	/**
	 * Constructor with {@link YADAQueryResult}
	 * @param yqr the container for result processing artifacts
	 * @throws YADAResourceException when the path to {@code r.js} or {@code harmony.js} is corrupt
   * @throws YADAIOException when the path to {@code r.js} or {@code harmony.js} can't be read
   * @throws YADAConverterException when the Rhino javascript engine throws an error
	 */
	public RESTResultJSONConverter(YADAQueryResult yqr) throws YADAResourceException, YADAIOException, YADAConverterException {
	  this.setYADAQueryResult(yqr);
//	  setHarmonizer(new Harmonizer());
	}
	
	/**
	 * Wraps the result of the REST request in a json object
	 * @throws YADAConverterException 
	 * @see com.novartis.opensource.yada.format.AbstractConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object result) throws YADAConverterException 
	{
		JSONArray arrayResult = new JSONArray();
		String    harm        = "";
		Object[]  o           = new Object[2];
		o[0] = result;
		
		if(getHarmonyMap() != null)
		{
		  if(getHarmonizer() == null)
      {
		    try 
		    {
          setHarmonizer(new Harmonizer());
          o[1] = getHarmonyMap().toString();
          harm = getHarmonizer().call(HARMONIZE, o);
        } 
		    catch (YADAResourceException e) 
        {
		      throw new YADAConverterException(e.getMessage(),e);
        } 
		    catch (YADAIOException e) 
		    {
		      throw new YADAConverterException(e.getMessage(),e);
        }
      }
		}
		try
		{
		  
			arrayResult = new JSONArray(harm);
		} 
		catch (JSONException e)
		{
			try
			{
				arrayResult = new JSONArray();
				arrayResult.put(new JSONObject(harm));
			}
			catch(JSONException e1)
			{
				arrayResult.put(result);	
			}
		}
		return arrayResult;
	}
}
