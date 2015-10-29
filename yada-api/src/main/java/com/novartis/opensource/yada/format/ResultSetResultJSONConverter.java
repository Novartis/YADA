/**
 * 
 */
package com.novartis.opensource.yada.format;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.rowset.RowSetMetaDataImpl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.adaptor.JDBCAdaptor;

/**
 * @since 0.4.0.0
 * @author David Varon
 *
 */
public class ResultSetResultJSONConverter extends AbstractConverter {
	
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
		while (rs.next())
		{
			JSONObject row = new JSONObject();
			String colValue;
			for (int i=1; i<=rsmd.getColumnCount(); i++)
			{
				String origColName = rsmd.getColumnName(i);
				if(!origColName.toLowerCase().equals(JDBCAdaptor.ROWNUM_ALIAS))
				{
					String col = isHarmonized() && ((JSONObject)this.harmonyMap).has(origColName) 
							? ((JSONObject)this.harmonyMap).getString(origColName) : origColName;
	
					//TODO handle empty result set more intelligently
					// OLD WAY adds headers to empty object when rs is empty
					
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
			rows.put(row);
		}
		return rows;
	}
}
