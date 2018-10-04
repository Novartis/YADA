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
package com.novartis.opensource.yada.format;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * <p>
 * The default response for the framework, embedding results in a standard YADA JSON object and returning the object as a {@link String}
 * </p>
 * <p>
 * Multi-query responses take the following form:
 * <code>
 * {
 * 	RESULTSETS: [
 * 		RESULTSET: {
 * 	    ROWS: [
 * 				{COL1.1:VAL1.1,...COL1.n:VAL1.n},
 * 				...
 * 				{COLn.1:VALn.1,...COLn.n:VALs.n}
 *      ],
 *    	qname: "...",
 *      total: "...",
 *      records: "...",
 *      page: "..."
 *    },
 *    RESULTSET: {
 *      ROWS: [
 *      	{COL1.1:VAL1.1,...COL1.n:VAL1.n},
 * 				...
 * 				{COLn.1:VALn.1,...COLn.n:VALs.n}
 *      ],
 *      qname: "...",
 *      total: "...",
 *      records: "...",
 *      page: "..."
 *    }
 * ]
 * }
 * </code>
 * </p>
 * <p>
 * Single-query responses take the following form:
 * </p>
 * <code>
 * {
 * 	RESULTSET: {
 *      ROWS: [
 *      	{COL1.1:VAL1.1,...COL1.n:VAL1.n},
 * 				...
 * 				{COLn.1:VALn.1,...COLn.n:VALs.n}
 *      ],
 *      qname: "...",
 *      total: "...",
 *      records: "...",
 *      page: "..."s
 *    }
 * }
 * </code>
 * @author David Varon
 *
 */
public class JSONResponse extends AbstractResponse {
	/**
   * Local logger handle
   */
	@SuppressWarnings("unused")
	private static Logger l = Logger.getLogger(JSONResponse.class);
	/**
	 * The result to be returned by this class's {@link #toString()} method
	 */
	private JSONObject jsonResponse;
	
	/**
	 * Default constructor, which creates the internal data structure for the response.
	 */
	public JSONResponse()
	{
		this.jsonResponse = new JSONObject();
	}
	
	/**
	 * Iterates over the results, appending each set as a JSONObject in the {@code ROWS} array.
	 * @throws YADAQueryConfigurationException when the {@link Response} spec in the request is malformed
	 * @see com.novartis.opensource.yada.format.AbstractResponse#compose(com.novartis.opensource.yada.YADAQueryResult[])
	 */
	@Override
	public Response compose(YADAQueryResult[] yqrs) throws YADAResponseException, YADAConverterException, YADAQueryConfigurationException
	{
		setYADAQueryResults(yqrs);
		create();
		for(YADAQueryResult yqr : yqrs)
		{
			setYADAQueryResult(yqr);
			// iterate over results
			// at this point 
			if(yqr != null)
			{
				if(yqr.getResults() != null && yqr.getResults().size() > 0)
				{
					for(Object result : yqr.getResults())
					{
						if(result != null)
							this.append(result); // should be a ResultSet
					}
				}
				else if(yqr.getCountResults() != null && yqr.getCountResults().size() > 0)
				{
					for(Object result : yqr.getCountResults())
					{
						if(result != null)
							this.append((Integer)result); // should be an Integer
					}
				}
			}
		}
    //TODO there will be memory issues with multiple users and large result sets
		//TODO implement a caching strategy for the format package
		//TODO implement a server-level caching/queuing strategy to handle multi-user, large-request scenarios
		
		// process converted headers into unique ordered Set
    
		boolean    join      = this.yqr.hasJoin();
    boolean    outer     = this.yqr.hasOuterJoin();
    if(join)
    {
      JSONArray qname = new JSONArray();
      JSONArray resultSets = this.jsonResponse.getJSONArray(RESULTSETS);
      int total = 0;
      for(int i=0;i<resultSets.length();i++)
      {
        JSONObject j = resultSets.getJSONObject(i); 
        qname.put(j.getJSONObject(RESULTSET).getString(QNAME));
      }
      
      this.jsonResponse = new JSONObject();
      this.jsonResponse.put(RESULTSET, new JSONObject());
      try 
      {
        this.jsonResponse.put(VERSION, YADAUtils.getVersion());
      } 
      catch (JSONException e) 
      {
        //TODO exception handling
      } 
      catch (YADAResourceException e) 
      {
        //TODO exception handling
      }
      this.jsonResponse.put(QNAME, qname);
      Joiner    joiner = new Joiner(getYadaQueryResults());
      JSONArray joins  = (JSONArray) joiner.join();
      this.jsonResponse.getJSONObject(RESULTSET).put(ROWS,joins);
      this.jsonResponse.put(TOTAL, joins.length());
    }
		
		return this;
	}
	
	
	
	/**
	 * Creates the root objects in the response.
	 * @see com.novartis.opensource.yada.format.AbstractResponse#create()
	 */
	@Override
	public Response create() throws YADAResponseException {
		try 
		{
			if(hasMultipleResults())
				this.jsonResponse.put(RESULTSETS,new JSONArray());
			else
				this.jsonResponse.put(RESULTSET,new JSONObject());
			this.jsonResponse.put(VERSION, YADAUtils.getVersion());
		} 
		catch (JSONException e) 
		{
		  String msg = "There was problem creating the JSON reponse object with the 'root key'.";
			throw new YADAResponseException(msg);
		}
		catch (YADAResourceException e)
		{
		  String msg = "There was a problem obtaining the version information from JNDI.";
		  throw new YADAResponseException(msg,e);
		}
		return this;
	}
	
	/**
	 * Used for update, insert, and delete results, where output is simply the count of rows processed,
	 * this method sets the value {@code TOTAL} key in each {@code RESULTSET} in the response object.  
	 * @see com.novartis.opensource.yada.format.AbstractResponse#append(java.lang.Integer)
	 */
	@Override
	public Response append(Integer i) throws YADAResponseException {
		Response r = this;
		try 
		{
			JSONObject resultSet = null;
			if(hasMultipleResults())
			{
				JSONObject resultSetWrapper = new JSONObject();
				resultSet = new JSONObject();	
				((JSONArray)this.jsonResponse.get(RESULTSETS)).put(resultSetWrapper);
				resultSetWrapper.put(RESULTSET, resultSet);
			}
			else
			{
				resultSet = this.jsonResponse.getJSONObject(RESULTSET); 
			}
			resultSet.put(QNAME, this.yqr.getYADAQueryParamValue(YADARequest.getParamValueForKey(this.yqr.getParameters(),YADAUtils.PARAM_FRAG_QNAME)));
			resultSet.put(APP, this.yqr.getApp());
			resultSet.put(TOTAL, this.yqr.getCountResult(0));
		} 
		catch (JSONException e) 
		{
			String msg = "There was problem appending a JSONObject to the reponse.";
			throw new YADAResponseException(msg,e);
		} 
		catch (YADAQueryConfigurationException e)
		{
			String msg = "There was problem obtaining the query name from the query object.";
			throw new YADAResponseException(msg,e);
		} 
		return r;
	}

	/**
	 * Passes {@code o} to the {@link Converter} and appends it to the internal JSON response object.
	 * @throws YADAQueryConfigurationException when the {@link Response} spec in the request is malformed
	 * @see com.novartis.opensource.yada.format.AbstractResponse#append(java.lang.Object)
	 */
	@Override
	public Response append(Object o) throws YADAResponseException, YADAConverterException, YADAQueryConfigurationException {
		
		Response r = this;
		try 
		{
		  JSONObject resultSet = null;
		  Converter  converter = getConverter(this.yqr);
		  boolean    count     = Boolean.parseBoolean(this.yqr.getYADAQueryParamValue(YADARequest.PS_COUNT));
		  if(getHarmonyMap() != null)
				converter.setHarmonyMap(getHarmonyMap());
			JSONArray rows = (JSONArray)converter.convert(o);
			
			
			// object prep
			if(hasMultipleResults()) // multiple queries, no harmonyMap
			{
				JSONObject resultSetWrapper = new JSONObject();
				resultSet = new JSONObject();
				((JSONArray)this.jsonResponse.get(RESULTSETS)).put(resultSetWrapper);
				resultSetWrapper.put(RESULTSET, resultSet);
			}
			else // single query, or harmonyMap
			{
				resultSet = this.jsonResponse.getJSONObject(RESULTSET); 
			}
			
			// data and stats prep
			if(resultSet.has(ROWS)) // appending rows for harmonized queries
			{
				//TODO consider Harmonizer and/or Paginator class
				// handle data
				// put the new data into the existing ROWS array
				JSONArray existing = resultSet.getJSONArray(ROWS);
				for(int i=0;i<rows.length();i++)
				{
					existing.put(rows.get(i));
				}
				
				// handle pagination 				
				/*TODO the algorithm here needs work.  
				 * 
				 * The issue here is related to interlacing 
				 * multple result sets in a harmonyMap with pagination case
				 * e.g., two results with > pagesize records.  consider:
				 * resultSet 1 = 700, first 20 returned
				 *   resultSet 2 = 400, first 20 returned
				 * What is on page 1, length 20, or change length to 50?
				 *    Do resultSet 1 records by default always appear first?
				 *    Does the algo return pagesize/number of queries from each resultSet?
				 *    Should pagesize be divided by number of queries earlier in the process if 
				 *      harmonyMap is provided, i.e., pagesize = pagesize/number of queries
				 *      -this would be wierd if there were pagesize=20, and 10 queries, 2 rows per
				 *      query would be returned.  
				 *    Should a pagination handler be added, with options and a default? **probably this
				 * 		
				 * All these scenarios get wierder when page > 1 is involved, and sorting, filtering, etc.
				 * Some default behavior must be identified and implemented, then options can be whatever.
				 */
				int pagesize = Integer.parseInt(this.yqr.getYADAQueryParamValue(YADARequest.PS_PAGESIZE));
				if(pagesize < existing.length())
				{
					rows = new JSONArray();
					for(int i=0;i<pagesize;i++)
					{
						rows.put(existing.get(i));
					}
				}

				// handle stats
				resultSet.put(RECORDS, existing.length());
				resultSet.put(QNAME, this.yqr.getQname());
				resultSet.put(APP, this.yqr.getApp());
				if(count)
				{
					resultSet.put(TOTAL, resultSet.getInt(TOTAL) + ((Integer)this.yqr.getCountResult(0)).intValue());
					resultSet.put(PAGE,this.yqr.getYADAQueryParamValue(YADARequest.PS_PAGESTART));
				}
			}
			else // adding rows for single query
			{
				resultSet.put(ROWS,rows);
				resultSet.put(RECORDS, rows.length());
				resultSet.put(QNAME, this.yqr.getYADAQueryParamValue(YADARequest.PS_QNAME));
				resultSet.put(APP, this.yqr.getApp());
				if(count)
				{
					resultSet.put(TOTAL,this.yqr.getCountResult(0));
					resultSet.put(PAGE,this.yqr.getYADAQueryParamValue(YADARequest.PS_PAGESTART));
				}
			}
		} 
		catch (JSONException e) 
		{
			String msg = "There was problem appending a JSONObject to the reponse.";
			throw new YADAResponseException(msg,e);
		} 
		catch (YADARequestException e)
		{
			String msg = "There was problem creating the Converter.";
			throw new YADAResponseException(msg,e);
		} 
		return r;
	}
	
	/**
	 * Outputs the internal json object as a string (calls {@link JSONObject#toString()}
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.jsonResponse.toString();
	}

	/**
	 * Calls {@link JSONObject#toString(int)} (but doesn't seem to be working as of this writing)
	 * @see com.novartis.opensource.yada.format.AbstractResponse#toString(boolean)
	 */
	@Override
	public String toString(boolean prettyPrint) throws YADAResponseException {
		if(prettyPrint)
		{
			try 
			{
				return this.jsonResponse.toString(2);
			} 
			catch (JSONException e) 
			{
				throw new YADAResponseException("There was problem formatting the JSONObject as a string.");
			}
		}
		return this.toString();
	}
}
