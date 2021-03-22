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
package com.novartis.opensource.yada;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A {@link LinkedHashMap} implementation for storing values passed to the YADA {@link Service}, by way of {@link YADARequest} 
 * in the url parameters or http POST content.  Naturally, {@code JSONParams} or {@code j} parameters map directly, but in fact, standard parameters
 * ({@code qname} or {@code q}, and {@code params} or {@code p}) are ultimately mapped to these objects as well. 
 * 
 * <p>This map contains YADA query names as keys, and {@link JSONParamsEntry} objects as values. Thus the structure is like the following:</p>
 * <p><code>query : { JSONParamsEntry }</code></p>
 * <p>which in turns parses out to:</p>
 * <p><code>query : { DATA: [ array of data ], PARAMS: [ array of params ] }</code></p>
 * 
 * @since 4.0.0               
 * @author David Varon
 * @see JSONParamsEntry
 */
public class JSONParams extends LinkedHashMap<String, JSONParamsEntry> {
  /**
	 * Constant equal to: {@value}
	 */
	private static final long serialVersionUID = -7677815049178407711L;
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(JSONParams.class);
	
	/** 
	 * Default constructor, provides no additional functionality.
	 * @since 4.0.0
	 */
	public JSONParams() {	}
	
	/**
	 * Takes a json string as an argument and converts it into a {@link JSONParams} object.
	 * The {@link String} must be a {@link String} representation of a {@link JSONArray}
	 * and must conform to the <a href="../../../../../../jsonparams.html">JSONParams specification</a>, e.g.:
	 * 
	 * <code>[{"qname":"name of query","DATA":[{"col_1":"val_1",..."col_n":"val_n"},{...},...]},...]</code> 
	 * 
	 * @since 4.0.0
	 * @param jp the json string to convert to an object
	 * @throws YADAQueryConfigurationException when the object can't be created, probably due to a malformed, or non-compliant json string.
	 */
	public JSONParams(String jp) throws YADAQueryConfigurationException 
	{
		try 
		{
			convertJSONObjects(new JSONArray(jp));
		} 
		catch (JSONException e) 
		{
			String msg = "Unable to create JSONParams object from supplied parameters.";
			throw new YADAQueryConfigurationException(msg, e);
		}
	}
	
	/**
	 * <p>Takes a {@link String}[] array as an argument and converts it into a {@link JSONParams} object.</p>
	 * 
	 * <p>The {@code jp} array can contain a representation of a {@link JSONArray}, in which case only the first index of the {@code jp} param will be processed.</p>
	 * 
	 * <p>Alternatively, if {@code jp} contains {@link String} representations of {@link JSONObject}s, 
	 * a new {@link JSONArray} will be instantiated and populated with the objects.</p>
	 * 
	 * <p>Currently, only JSON syntax checking is performed, 
	 * but no YADA syntax validation against the 
	 * <a href="https://github.com/Novartis/YADA/blob/master/src/site/markdown/jsonparams.md">JSONParams specification</a> 
	 * is performed.</p> 
	 * 
	 * This is the constructor called by {@link Service#handleRequest(javax.servlet.http.HttpServletRequest)} when encountering a JSONParams url parameter.
	 * @since 4.0.0
	 * @param jp an array containing json strings 
	 * @throws YADAQueryConfigurationException when {@code jp} is not convertable into a {@link JSONParams} object 
	 * @see YADARequest#setJsonParams(String[])
	 * @see Service#handleRequest(javax.servlet.http.HttpServletRequest)
	 */
  
	public JSONParams(String[] jp) throws YADAQueryConfigurationException 
	{
		JSONObject jo;
		JSONArray  ja = new JSONArray();
		for(String j : jp)
		{
			try 	
			{
				ja = new JSONArray(j);
				break;
			} 
			catch (JSONException e) 
			{
				try
				{
					jo = new JSONObject(j);
					ja.put(jo);
				}
				catch (JSONException e1)
				{
					String msg = "Unable to create JSONParams object from supplied parameters.";
					throw new YADAQueryConfigurationException(msg, e1);
				}
			}
		}
		convertJSONObjects(ja);
	}
	
	/**
	 * Takes a {@link JSONArray} object as an argument and converts it into a {@link JSONParams} object.
	 * The {@link JSONArray} must conform to the <a href="../../../../../../jsonparams.html">JSONParams specification</a>.
	 * @param ja a json string conforming to the <a href="../../../../../../jsonparams.html">JSONParams specification</a>
	 * @throws YADAQueryConfigurationException when {@code ja} is not convertable into a {@link JSONParams} object
	 */
	public JSONParams(JSONArray ja) throws YADAQueryConfigurationException
	{
		convertJSONObjects(ja);
	}
	
	/**
	 * Convenience constructor for creating new object from existing.
	 * @param qname the query name to add
	 * @param entry the data and params object to add
	 */
	public JSONParams(String qname, JSONParamsEntry entry) 
	{
		put(qname,entry);
	}
	
	/**
	 * Takes input that originated as a json array of json objects, builds {@link JSONParamsEntry} objects 
	 * for each of them, and adds them to itself, mapping to the appropriate query names
	 * @param ja an array of json objects conforming to the <a href="../../../../../../jsonparams.html">JSONParams specification</a>
	 * @throws YADAQueryConfigurationException when {@code ja} is not convertable into a {@link JSONParams} object
	 */
	private void convertJSONObjects(JSONArray ja) throws YADAQueryConfigurationException
	{
		try
		{
			for (int i = 0; i < ja.length(); i++) // multiple queries
			{
				// object with the query and data
				JSONObject jobj     = ja.getJSONObject(i);
				String     qnameKey = YADARequest.getParamKeyVal("PL_QNAME");
				String     qKey     = YADARequest.getParamKeyVal("PS_QNAME");
				String     qname    = jobj.has(qnameKey) 
				                      ? jobj.getString(qnameKey) 
				                      : jobj.getString(qKey);				
				JSONParamsEntry entry = new JSONParamsEntry(jobj);
				put(qname,entry);
			}
			l.debug(this.toString());
		}
		catch (JSONException e) 
		{
			String msg = "Unable to create JSONParams object from supplied parameters.";
			throw new YADAQueryConfigurationException(msg, e);
		}
	}
	
	/**
	 * Prints the value of the {@link JSONParams} object as a {@link LinkedHashMap}.
	 * @since 4.0.0
	 */
	@Override
	public String toString() {
		String sSet = "Set [JsonParams]";
		String sVal = "["+super.toString()+"]";
		return String.format("%25s to %s", sSet, sVal);
	}
	
	/**
	 * Adds a new entry to the internal {@link LinkedHashMap} with the {@code qname} 
	 * as a key and an empty {@link JSONParamsEntry} as a value.
	 * @since 4.0.0
	 * @param qname the new key to add to the map
	 */
	public void put(String qname) {
		if(!this.containsKey(qname))
			this.put(qname, new JSONParamsEntry());
	}
	
	/**
	 * Adds {@code data}, pre-conformed into a {@link LinkedHashMap}&lt;{@link String},{@link String}[]&gt;, to the {@link JSONParamsEntry} in the map, associated to {@code qname}. 
	 * If {@code qname} is not yet in the map, it is added first.
	 * <p>Calls {@link JSONParamsEntry#addData(LinkedHashMap)} to perform the {@link LinkedHashMap} to {@link JSONParamsEntry} conversion.</p>
	 * 
	 * @since 4.0.0
	 * @param qname the query name to add to the map
	 * @param data the data to add to the entry associated to the query in the map 
	 */
	public void addData(String qname, LinkedHashMap<String,String[]> data) {
		if(!hasQuery(qname))
			this.put(qname);
		this.get(qname).addData(data);
	}
	
	/**
	 * Adds {@code data} to the {@link JSONParamsEntry} in the map, associated to {@code qname}. 
	 * If {@code qname} is not yet in the map, it is added first.
	 * <p>Calls {@link JSONParamsEntry#addData(JSONObject)} to perform the {@link LinkedHashMap} to {@link JSONParamsEntry} conversion.</p>
	 * 
	 * @since 4.0.0
	 * @param qname the query name to add to the map
	 * @param data the data to add to the entry associated to the query in the map 
	 */
	public void addData(String qname, JSONObject data) {
		if(!hasQuery(qname))
			this.put(qname);
		this.get(qname).addData(data);
	}
	
	/**
	 * Returns a boolean value corresponding to the String parameter passed to the method.
	 * 
	 * @since 4.0.0
	 * @param count the value of the {@code count} YADAParam
	 * @return {@code true} if {@code count} = {@code "true"} (the default,) or {@code false} if {@code "false"}
	 */
	public boolean getCountParam(String count) {
		if (count == null) 
			return true;
		return Boolean.parseBoolean(count); 
	}
	
	/**
	 * Just calls {@link #clear()}
	 * @since PROVISIONAL
	 */
	public void resetAllData() {
		//TODO implement this
		this.clear();
	}
	
	/**
	 * Returns true if this {@link JSONParams} object contains a key matching {@code query}, otherwise false
	 * @param query the name of the query
	 * @return {@code true} if this {@link JSONParams} object contains a key matching {@code query}, otherwise {@code false}
	 */
	public boolean hasQuery(String query) {
		if(this.containsKey(query))
			return true;
		return false;
	}
	
	/**
	 * Convenience method for appending a single data row to the internal {@link JSONParamsEntry} mapped to the {@code query}.
	 * If the {@code query} doesn't exist, it will be added via {@link JSONParams#addData(String, LinkedHashMap)}
	 * 
	 * @param query the name of the query to which to attach the data
	 * @param row a {@link JSONObject} containing the data to add
	 * @throws JSONException if {@code row} cannot be parsed
	 */
  public void addRowForYADAQuery(String query, JSONObject row) throws JSONException {
		this.addData(query, row);
	}
	
	/**
	 * Convenience method for appending multiple rows to the internal {@link JSONParamsEntry} mapped to the {@code query}.
	 * If the {@code query} doesn't exist, it will be added via {@link JSONParams#addData(String, LinkedHashMap)}
	 * Uses {@link JSONParams#addRowForYADAQuery(String, JSONObject)} for convenience, to avoid dealing with value object casting
	 * @param query the name of the query to which to associate the datas
	 * @param fields list of field names to corresponding to {@code values}
	 * @param values list of values to associate to {@code fields}
	 * @throws JSONException if the internal JSONObject can not be created or populated.
	 */
	public void addRowsForYADAQuery(String query, List<String> fields, List<String[]> values) throws JSONException  {
		for (String[] vals : values)
		{
			JSONObject row = new JSONObject();
			for (int i=0; i<vals.length; i++)
			{
				row.put(fields.get(i),vals[i]);
			}
			addRowForYADAQuery(query, row);
		}
	}
	
	/**
	 * Convenience method for appending multiple rows to the internal {@link JSONParamsEntry} mapped to the {@code query}.
	 * If the {@code query} doesn't exist, it will be added via {@link JSONParams#addData(String, LinkedHashMap)}
	 * Uses {@link JSONParams#addRowForYADAQuery(String, JSONObject)}
	 * @param query the name of the query to which to associate the data in {@code rows}
	 * @param rows the data
	 * @throws JSONException if the internal JSONObject cannot be created or populated
	 */
	public void addRowsForYADAQuery(String query, List<JSONObject> rows) throws JSONException {
		for(JSONObject row : rows)
		{
			this.addRowForYADAQuery(query, row);
		}
	}
	
	/**
	 * Convenience method for appending multiple rows to the internal {@link JSONParamsEntry} mapped to the {@code query}.
	 * If the {@code query} doesn't exist, it will be added via {@link JSONParams#addData(String, LinkedHashMap)}
	 * Uses {@link JSONParams#addRowForYADAQuery(String, JSONObject)}
	 * @param query the name of the query to which to associate the data in {@code rows}
	 * @param rows the data
	 * @throws JSONException if the internal JSONObject cannot be created or populated
	 */
	public void addRowsForYADAQuery(String query, JSONArray rows) throws JSONException {
		for (int i = 0; i < rows.length(); i++)
		{
			JSONObject row = rows.getJSONObject(i);
			this.addRowForYADAQuery(query, row);
		}
	}
		
	/**
	 * Convenience method for adding a query to the intenal map, and data to the internal {@link JSONParamsEntry}.
	 * Uses {@link JSONParams#addYADAQueryWithData(String, JSONArray)}
	 * @param query the name of the query to add
	 * @param rows the data to associate to the query
	 * @throws JSONException if the internal JSONObject cannot be created or populated
	 */
	public void addYADAQueryWithData(String query, List<JSONObject> rows) throws JSONException {
		JSONArray a = new JSONArray();
		for(JSONObject j : rows)
		{
			a.put(j);
		}
		addYADAQueryWithData(query, a);
	}
	
	/**
	 * Convenience method for adding a query to the intenal map, and data to the internal {@link JSONParamsEntry}.
	 * @param query the name of the query to add
	 * @param rows the data to associate to the query
	 * @throws JSONException if the internal JSONObject cannot be created or populated
	 */
	public void addYADAQueryWithData(String query, JSONArray rows) throws JSONException {
		this.put(query);
		this.addRowsForYADAQuery(query, rows);
	}
	
	/**
	 * Convenience method for adding just a query to the internal map along with an empty {@link JSONParamsEntry}
	 * @param query the name of the query to add to the map
	 */
	public void addYADAQuery(String query) {
		this.put(query);
	}
	
	/**
	 * Convenience method to facilitate getting the list of qnames from this parameter.
	 * @return a {@link String}[] of qnames
	 * @since 4.1.0
	 */
	public String[] getKeys() {
		return this.keySet().toArray(new String[this.size()]);
	}
}
