
/**
 * 
 */
package com.novartis.opensource.yada.format;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
}
