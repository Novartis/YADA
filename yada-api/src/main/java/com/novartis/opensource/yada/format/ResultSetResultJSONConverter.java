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

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.rowset.RowSetMetaDataImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.adaptor.JDBCAdaptor;

/**
 * @since 0.4.0.0
 * @author David Varon
 *
 */
public class ResultSetResultJSONConverter extends AbstractConverter {
	
  /**
   * Default constructor
   */
  public ResultSetResultJSONConverter() {
    // default constructor
  }
  
  /**
   * Constructor with {@link YADAQueryResult}
   * @param yqr the container for result processing artifacts
   */
  public ResultSetResultJSONConverter(YADAQueryResult yqr) {
    this.setYADAQueryResult(yqr);
  }
  
	/**
	 * Unpacks data in a {@code ResultSet} and stuffs it in a JSON object. 
	 * @see com.novartis.opensource.yada.format.AbstractConverter#convert(java.lang.Object)
	 */
	@Override
	public Object convert(Object result) throws YADAConverterException {
		JSONArray rows = null;
		
		try
		{
			rows = getJSONRows((ResultSet)result);
		} 
		catch (SQLException e)
		{
			String msg = "Unable to iterate over ResultSet";
			throw new YADAConverterException(msg,e);
		} 
		catch (JSONException e)
		{
			String msg = "Unable to create JSONArray from ResultSet";
			throw new YADAConverterException(msg,e);
		}
		return rows;
	}

	/**
	 * Converts data from a {@link java.sql.ResultSet} into a {@link JSONArray} containing
	 * one {@link JSONObject} per row
	 * @param rs the result set containing the data to convert to JSON
	 * @return a json array containing the data
	 * @throws SQLException when iteration or access to {@code rs} fails
	 */
	protected JSONArray getJSONRows(ResultSet rs) throws SQLException
	{
		JSONArray         rows = new JSONArray();
		ResultSetMetaData rsmd = rs.getMetaData();
		if (rsmd == null)
			rsmd = new RowSetMetaDataImpl();
		List<String> convertedResult = new ArrayList<>();
		while (rs.next())
		{
			JSONObject row = new JSONObject();
			String colValue;
			for (int i=1; i<=rsmd.getColumnCount(); i++)
			{
				String origColName = rsmd.getColumnName(i);
				if(!origColName.toLowerCase().equals(JDBCAdaptor.ROWNUM_ALIAS))
				{
				  boolean harmonize = isHarmonized(); 
				  boolean prune     = harmonize ?  ((JSONObject)this.harmonyMap).has(Harmonizer.PRUNE) && ((JSONObject)this.harmonyMap).getBoolean(Harmonizer.PRUNE) : false;
					String  col       = origColName;
					if(harmonize)
					{
					  if(((JSONObject)this.harmonyMap).has(origColName))
					  {
					    col = ((JSONObject)this.harmonyMap).getString(origColName); 
					  }
					  else if(prune)
					  {
					    col = "";
					  }
					} 
					
					//TODO handle empty result set more intelligently
					// OLD WAY adds headers to empty object when rs is empty
	        if(!"".equals(col))
	        {
  					if (null == rs.getString(origColName) || NULL.equals(rs.getString(origColName)))
  					{
  						colValue = NULL_REPLACEMENT; 
  					}
  					else
  					{
  						colValue = rs.getString(origColName);
  					}
  					row.put(col, colValue);
	        }
				}
				
			}
			rows.put(row);
			convertedResult.add(row.toString());
		}
		if(rows.length() > 0)
		{
  		for(String key : JSONObject.getNames(rows.getJSONObject(0)))
  		{
  		  getYADAQueryResult().addConvertedHeader(key);
  		}
  		getYADAQueryResult().getConvertedResults().add(convertedResult);
		}
		return rows;
	}
}
