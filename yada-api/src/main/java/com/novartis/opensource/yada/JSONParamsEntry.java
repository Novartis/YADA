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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.util.YADAUtils;

/**
 * An internally used data structure encapsulating data and params associated to a {@link YADAQuery} via {@code qname},
 * passed to the framework in a request.
 *  
 * @author David Varon
 * @since 4.0.0
 * @see JSONParams
 */
public class JSONParamsEntry { 
	
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(JSONParamsEntry.class);
	/**
	 * Internal structure to hold column names/data values passed in request config 
	 */
	private List<LinkedHashMap<String,String[]>> data;
	/**
	 * Request config in list form
	 */
	private List<YADAParam> params;
	/**
	 * Internal index of parameters mapped by name
	 */
	private Map<String,YADAParam> keys = new HashMap<>();
	/**
	 * Internal index of immutable parameters mapped by name 
	 */
	private Map<String,YADAParam> immutableKeys = new HashMap<>();
	
	/**
	 * The default constructor, sets the internal {@link #data} and {@link #params} structures.
	 */
	public JSONParamsEntry() {
		setData(new ArrayList<LinkedHashMap<String,String[]>>());
		setParams(new ArrayList<YADAParam>());
	}
	
	/**
	 * Mutator for variable
	 * @param data the data map to set or use as replacement
	 */
	private void setData(ArrayList<LinkedHashMap<String,String[]>> data)
	{
		this.data = data;
	}

	/**
	 * Mutator for variable, plus, if {@code params} size > 0, calls {@link #setKeys()} 
	 * to populate internal indices.
	 * @param params list of YADA request parameters
	 */
	private void setParams(ArrayList<YADAParam> params)
	{
		this.params = params;
		if(params.size() > 0) 
		{
			setKeys();
		}
	}
	
	/**
	 * Iterates over {@link #params} mapping parameter objects to parameter names
	 */
	private void setKeys() 
	{
		for(YADAParam param : this.params)
		{
			this.keys.put(param.getName(), param);
			if(param.getRule() != YADAParam.OVERRIDEABLE)
				this.immutableKeys.put(param.getName(), param);
		}
	}
	
	/**
	 * Creates an instance with populated {@link #data} and {@link #params} objects.
	 * @param jobj a JSONObject containing data and parameters
	 * @throws YADAQueryConfigurationException when {@code jobj} is malformed, or otherwise can't be converted into an entry 
	 */
	public JSONParamsEntry(JSONObject jobj) throws YADAQueryConfigurationException
	{
		this();
	
		try
		{
			for(String frag : YADAUtils.PARAM_FRAGS)
			{
				String key = YADARequest.getParamKeyVal("PS_"+frag);
				String val = YADARequest.getParamValueForKey(jobj,frag);
				if(val != null)
				{
					YADAParam param = new YADAParam(key,val,YADAParam.QUERY,YADAParam.OVERRIDEABLE);
					addParam(param);
				}
			}
			
			JSONArray  rows  = jobj.getJSONArray(YADARequest.JSON_KEY_DATA); // query DATA
			// multiple rows of data
			for (int j = 0; j < rows.length(); j++) 
			{
				JSONObject  row  = rows.getJSONObject(j); // row
				Iterator<?> iter = row.keys();
				LinkedHashMap<String,String[]> dataForRow = new LinkedHashMap<>();
				while (iter.hasNext()) 
				{
					String   column = ((String) iter.next()); 
					String   ucCol  = column.toUpperCase();  // DV20180615 case insensitivity (h/t to kildea)
					String[] value  = null; 
					
					// JSONArrays can be passed in as values
					JSONArray valIsArray;
					try
					{
						valIsArray = row.getJSONArray(column);
						value = new String[valIsArray.length()];
						for (int k=0;k<valIsArray.length();k++)
						{
							value[k]  = valIsArray.getString(k);
						}
						l.debug("JSONArray passed in is now ["+value+"]");
					}
					catch(JSONException e)
					{
						// value passed in was just a string
						value = new String[] {String.valueOf(row.get(column))};
					}
					dataForRow.put(ucCol, value);
				}
				// store the col/val hash in the arraylist for the qname
				addData(dataForRow); 
			}
		}
		catch(JSONException e)
		{
			String msg = "Unable to create JSONParamsEntry object from supplied parameters.";
			throw new YADAQueryConfigurationException(msg,e);
		}
	}
	
	/**
	 * Adds all the data passed in the param to the entry.
	 * @param dataToAdd the data to add to the entry
	 */
	public void addAllData(List<LinkedHashMap<String,String[]>> dataToAdd) {
		this.getData().addAll(dataToAdd);
	}
	
	/**
	 * Add {@code row} to the entry's data list.
	 * @param row the data row to add to the existing data structure in the entry.
	 */
	public void addData(LinkedHashMap<String,String[]> row) {
		this.getData().add(row);
	}
	
	/**
	 * Adds {@code row} to the entry's data list.
	 * @param row the data row to add to the existing data structure in the entry.
	 */
	public void addData(JSONObject row) {
		LinkedHashMap<String,String[]> d = new LinkedHashMap<>();
		String[] k = JSONObject.getNames(row); 
		for(int i=0;i<k.length;i++)
		{
			d.put(k[i], new String[] {String.valueOf(row.get(k[i]))});
		}
		this.addData(d);
	}
	
	/**
	 * Adds all the parameters passed to the method to the internal list. Checks for existing immutability. Resets {@link #keys} and {@link #immutableKeys} indices.
	 * @param paramsToAdd the parameters to add to the entry
	 */
	public void addAllParams(List<YADAParam> paramsToAdd) {
		for(YADAParam param : paramsToAdd)
		{
			if(!hasNonOverrideableParam(param.getName()))
			{
				addParam(param);
			}
		}
	}
	
	/**
	 * Adds a parameter to the internal list.  Checks for existing immutability. Resets {@link #keys} and {@link #immutableKeys} indices.
	 * @param param the parameter to add to the entry
	 */
	public void addParam(YADAParam param) {
		this.getParams().add(param);
		if(!hasNonOverrideableParam(param.getName()))
		{
			this.getParams().add(param);
			setKeys();
		}
	}
	
	/**
	 * Returns the internal {@link #data} structure
	 * @return the internal {@link #data} structure
	 */
	public List<LinkedHashMap<String,String[]>> getData() {
		return this.data;
	}
	
	/**
	 * Returns the internal {@link #params} structure
	 * @return the internal {@link #params} structure
	 */
	public List<YADAParam> getParams() {
		return this.params;
	}
	
	/**
	 * Returns a data structure corresponding to the data row at {@code index}.
	 * @param index the row to return
	 * @return data structure corresponding to the data row at {@code index}.
	 */
	public Map<String,String[]> getRow(int index) {
		return getData().get(index);
	}
	
	/**
	 * Returns the param containing the key
	 * @param key the name of the desired parameter
	 * @return {@link YADAParam} containing the {@code key}
	 */
	public YADAParam getParam(String key) {
		return this.keys.get(key);
	}
	
	
	/**
	 * Checks for a {@link YADAParam} with a name equal to {@code key}
	 * @param key the param name for which to check
	 * @return {@code true} if there is a param with the name equal to {@code key}
	 */
	@SuppressWarnings("unused")
	private boolean hasParam(String key) {
		return this.keys.containsKey(key);
	}
	
	/**
	 * @param key the name of the parameter to check for immutabilitiy
	 * @return {@code true} if there is a param with the name equal to {@code key} and a {@code rule} equal {@link YADAParam#NONOVERRIDEABLE}
	 */
	private boolean hasNonOverrideableParam(String key) {
		return this.immutableKeys.containsKey(key);
	}
}
