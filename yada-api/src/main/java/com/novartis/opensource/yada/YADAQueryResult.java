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
package com.novartis.opensource.yada;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

/**
 * A container for storing indexed query results, their converted forms, and row counts. 
 * @since 4.0.0
 * @author David Varon
 *
 */
public class YADAQueryResult {
	
	/**
	 * The query name of the query from which this result is generated and to which this result is attached.
	 */
	private String       qname;
	/**
	 * The app to which the {@code qname} is mapped
	 * @since 8.6.1
	 */
	private String       app;
	/**
	 * The list of query results, in raw form, i.e. {@link java.sql.ResultSet}, {@link String}
	 */
	private List<Object> results = new ArrayList<>();
	/**
	 * The list of converted query results.  These objects are to be returned to the requestor. 
	 */
	private List<Object> convertedResults = new ArrayList<>();
	/**
   * The converted (mapped) header, i.e., list of column names. This list is the same for the entire result.
   * These objects are to be returned to the requestor. 
   * @since 6.1.0
   */
  private List<String> convertedHeader = new ArrayList<>();
	/**
	 * The list of results of count queries.  These results typically end up as values corresponding to {@code total} keys in 
	 * {@code RESULTSET} objects inside YADA json.
	 */
	private List<Object> countResults = new ArrayList<>();
	/**
	 * The list of configuration parameters
	 */
	private List<YADAParam> parameters = new ArrayList<>();
	/**
	 * The number of rows processed across all iterations of the {@link YADAQuery}
	 */
	private int totalResultCount = 0;
	/**
   * The container of field names to support merged result sets
   * @since 6.1.0
   */
  private JSONObject globalHarmonyMap;
	/**
	 * Default no-arg constructor.
	 */
	public YADAQueryResult() {}
	
	/**
	 * Constructor accepting list of parameters
	 * @param params the request config parameters 
	 */	
	public YADAQueryResult(List<YADAParam> params) {
		setParameters(params);
	}
	/**
	 * Standard mutator for variable
	 * @param qname the name of the query
	 */
	public void setQname(String qname) { this.qname = qname; }
	/**
	 * Standard accessor for variable
	 * @return the query name
	 */
	public String getQname() { return this.qname; }
	/**
	 * Standard accessor for variable
	 * @return the app
	 * @since 8.6.1
	 */
	public String getApp() {
		return app;
	}
	/**
	 * Standard mutator for variable
	 * @param app the app to set
	 * @since 8.6.1
	 */
	public void setApp(String app) {
		this.app = app;
	}
	/**
	 * Standard mutator for variable
	 * @param results list for raw results
	 */
	public void setResult(List<Object> results) { this.results = results; }
	/**
	 * Adds the raw result to the list at index {@code row}
	 * @param row the result list index at which to add the result
	 * @param result the raw result to add to the list
	 */
	public void addResult(int row, Object result) { getResults().add(row,result); }
	/**
	 * Standard accessor for variable
	 * @return the list of raw results
	 */
	public List<Object> getResults() { return this.results; }
	/**
	 * Returns the raw result from the list at index {@code row}
	 * @param row the list index of the desired result
	 * @return the raw result at index {@code row}
	 */
	public Object getResult(int row) { return getResults().get(row); }

	/**
   * Standard mutator for variable
   * @param convertedResults the converted result list
   */
  public void setConvertedResults(List<Object> convertedResults) { this.convertedResults = convertedResults; }
  /**
   * Standard mutator for variable
   * @param convertedHeader the converted header list
   * @since 6.1.0
   */
  public void setConvertedHeader(List<String> convertedHeader) { this.convertedHeader = convertedHeader; }
	/**
	 * Adds a converted (formatted) result to the list at index {@code row}
	 * @param row the list index at which to put the converted result 
	 * @param convertedResult the converted result to add to the list
	 */
	public void addConvertedResult(int row, Object convertedResult) { getConvertedResults().add(row,convertedResult); }
	/**
   * Adds a converted (mapped) header to the list. The header is the same for all rows.
	 * @param convertedResult the converted (mapped) header to add to the list
   * @since 6.1.0
   */
  public void addConvertedHeader(String convertedResult) { getConvertedHeader().add(convertedResult); }
	/**
	 * Standard accessor for variable
	 * @return the list of converted results
	 */
	public List<Object> getConvertedResults() { return this.convertedResults; }
	/**
   * Standard accessor for variable
   * @return the list of converted headers
   * @since 6.1.0
   */
  public List<String> getConvertedHeader() { return this.convertedHeader; }
	/**
	 * Returns the converted (formatted) result at index {@code row}
	 * @param row the index of the desired result
	 * @return the converted (formatted) result at index {@code row}
	 */
	public Object getConvertedResult(int row) { return getConvertedResults().get(row); }
	
	/**
	 * Standard mutator for variable
	 * @param countResults the list of "count" results
	 */
	public void setCountResult(List<Object> countResults) { this.countResults = countResults; }
	/**
	 * Add the {@code countResult} to index at {@code row}
	 * @param row the list index at which to add the "count" result 
	 * @param countResult the result to add to the list
	 */
	public void addCountResult(int row, Object countResult) 
	{ 
		getCountResults().add(row,countResult);
		setTotalResultCount(((Integer)countResult).intValue());
	}
	/**
	 * Standard accessor for variable
	 * @return the list of count results
	 */
	public List<Object> getCountResults() { return this.countResults; }
	/**
	 * Returns the number of rows in the result at index {@code row}
	 * @param row the list index of the desired result
	 * @return the number of results corresponding to the data set in the query indexed at {@code row}
	 */
	public Object getCountResult(int row) { return getCountResults().get(row); }

	/**
	 * Returns the total number of rows transacted by the {@link YADAQuery}
	 * @return the totalResultCount
	 */
	public int getTotalResultCount()
	{
		return this.totalResultCount;
	}

	/**
	 * Adds the value of the {@code totalResultCount} argument to instance variable {@link #totalResultCount}
	 * @param totalResultCount the number of rows processed
	 */
	public void setTotalResultCount(int totalResultCount)
	{
		this.totalResultCount += totalResultCount;
	}

	/**
	 * Standard mutator for variable
	 * @param parameters the list of request config parameters
	 */
	public void setParameters(List<YADAParam> parameters) { this.parameters = parameters; }
	/**
	 * Standard accessor for variable.
	 * @return the list of parameters
	 */
	public List<YADAParam> getParameters() { return this.parameters; }
	/**
	 * Returns the value of the requested parameter with name = {@code key}
	 * @param key the name of the desired parameter
	 * @return the parameter value for {@code key}
	 */
	public String getYADAQueryParamValue(String key) 
	{ 
		String value = null;
		if(key != null && !"".equals(key))
		{
			for (Iterator<YADAParam> iterator = getParameters().iterator(); iterator.hasNext();)
			{
				YADAParam param = iterator.next();
				if(param.getName().equals(key))
				{
					value = param.getValue();
				}
			}
		}
		return value;
	}
	
	/**
	 * Convenience method to facilite result formatting.
	 * @return {@code true} if this container contains only a single result, or only a single count result
	 */
	public boolean hasSingularResult() {
		if(this.getResults().size() == 1
				|| (this.getResults().size() == 0
				    && this.getCountResults().size() == 1))
			return true;
		return false;
	}
	
	/**
	 * Convenience method to facilitate result formatting.
	 * @since 4.0.0
	 * @return {@code true} if format is JSON, XML; otherwise {@code false}
	 */
	public boolean isFormatStructured()
	{
		String format = this.getYADAQueryParamValue(YADARequest.PS_FORMAT);
		// 2015-12-10 removed HTML from "structured" because it actually returns tabular data which must
		// be processed as delimited.
		if(format.equals(YADARequest.FORMAT_JSON) || format.equals(YADARequest.FORMAT_XML)) 
			return true;
		return false;
	}

  /**
   * Enables composite header in harmonized delimited results
   * @return the globalHarmonyMap
   * @since 6.1.0
   */
  public JSONObject getGlobalHarmonyMap() {
    return this.globalHarmonyMap;
  }

  /**
   * Enables composite header in harmonized delimited results
   * @param globalHarmonyMap the globalHarmonyMap to set
   * @since 6.1.0
   */
  public void setGlobalHarmonyMap(JSONObject globalHarmonyMap) {
    this.globalHarmonyMap = globalHarmonyMap;
  }
  
  /**
   * Returns {@code true} if any of the following have length &gt; 0:
   * <ul>
   *  <li>{@link YADARequest#PL_JOIN}</li>
   *  <li>{@link YADARequest#PS_JOIN}</li>
   *  <li>{@link YADARequest#PL_LEFTJOIN}</li>
   *  <li>{@link YADARequest#PS_LEFTJOIN}</li>
   *  </ul>
   * @return {@code true} if a join parameter is set on the {@link YADARequest}
   */
  public boolean hasJoin() 
  {
    boolean val = false;
    String join = this.getYADAQueryParamValue(YADARequest.PS_JOIN);
    if(join != null && !join.equals("") && join.length() > 0)
      val = true;
    else
    {
      join = this.getYADAQueryParamValue(YADARequest.PS_LEFTJOIN);
      if(join != null && !join.equals("") && join.length() > 0)
        val = true;
    }
    return val;
  }
  
  /**
   * Returns {@code true} if any of the following have length &gt; 0:
   * <ul>
   *  <li>{@link YADARequest#PL_LEFTJOIN}</li>
   *  <li>{@link YADARequest#PS_LEFTJOIN}</li>
   *  </ul>
   * @return {@code true} if a leftjoin parameter is set on the {@link YADARequest}
   */
  public boolean hasOuterJoin() 
  {
    boolean val = false;
    String join = this.getYADAQueryParamValue(YADARequest.PS_LEFTJOIN);
    if(join != null && !join.equals("") && join.length() > 0)
        val = true;
    return val;
  }
}
