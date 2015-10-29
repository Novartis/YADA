	/**
 * 
 */
package com.novartis.opensource.yada.format;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.sql.rowset.RowSetMetaDataImpl;

import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.adaptor.JDBCAdaptor;

/**
 * @author David Varon
 * @since 0.4.0.0
 */
public class ResultSetResultDelimitedConverter extends AbstractConverter {
	
	/**
	 * Local handle for column separator, defaults to {@link YADARequest#DEFAULT_DELIMITER} 
	 */
	private String     colsep;
	/**
	 * Local handle for record separator, defaults to {@link YADARequest#DEFAULT_ROW_DELIMITER}
	 */
	private String     recsep;
	
	/**
	 * Creates a delimited result object using {@code newColSep} as a column separator and {@code newRecSep} as 
	 * a row separator. 
	 * @see com.novartis.opensource.yada.format.AbstractConverter#convert(java.lang.Object, java.lang.String, java.lang.String)
	 */
	@Override
	public Object convert(Object o, String newColSep, String newRecSep) throws YADAConverterException {
		
		StringBuffer result  = new StringBuffer();
		this.colsep = newColSep;
		this.recsep = newRecSep;
		
		try
		{
			result.append(getDelimitedRows((ResultSet)o));
		} 
		catch (SQLException e)
		{
			String msg = "Unable to iterate over ResultSet";
			throw new YADAConverterException(msg,e);
		} 
		catch (JSONException e)
		{
			String msg = "Unable to read Harmony Map";
			throw new YADAConverterException(msg,e);
		}
		return result;
	}
	
	/**
	 * Converts columns of data in a {@link java.sql.ResultSet} to a {@link java.lang.StringBuffer}
	 * containing tabular data delimited accordingly per request parameters 
	 * @param rs the result set to convert
	 * @return the reformatted, delimited data
	 * @throws SQLException when {@link ResultSet} or {@link ResultSetMetaData} iteration fails
	 */
	protected StringBuffer getDelimitedRows(ResultSet rs) throws SQLException 
	{
		StringBuffer      result    = new StringBuffer();		
		ResultSetMetaData rsmd      = rs.getMetaData();
		if (rsmd == null) //TODO What happens to headers when rsmd is null, or resultSet is empty?
			rsmd = new RowSetMetaDataImpl();
		int colCount = rsmd.getColumnCount();
		boolean hasYadaRnum = rsmd.getColumnName(colCount).toLowerCase().equals(JDBCAdaptor.ROWNUM_ALIAS);
		// handle headers 
		//TODO How to suppress headers?
		for (int j=1; j <= colCount; j++)
		{
			String colName = rsmd.getColumnName(j);
			if(!hasYadaRnum || !colName.toLowerCase().equals(JDBCAdaptor.ROWNUM_ALIAS))
			{
				String col = isHarmonized() ? ((JSONObject)this.harmonyMap).getString(colName) : colName;
				result.append(col);
				if ((hasYadaRnum && j < colCount - 1) || (!hasYadaRnum && j < colCount)) 
				{
					result.append(this.colsep);
				}
			}
		}
		result.append(this.recsep);
		
		while (rs.next())
		{
			String colValue;
			for (int j=1; j <= colCount; j++)
			{
				String colName = rsmd.getColumnName(j);
				if(!hasYadaRnum || !colName.toLowerCase().equals(JDBCAdaptor.ROWNUM_ALIAS))
				{
					String col = isHarmonized() ? ((JSONObject)this.harmonyMap).getString(colName) : colName;
					if (null == rs.getString(col) || "null".equals(rs.getString(col)))
					{
						colValue = NULL_REPLACEMENT;
					}
					else
					{
						colValue = "\""+rs.getString(col)+"\"";
					}
					result.append(colValue);
					if ((hasYadaRnum && j < colCount - 1) || (!hasYadaRnum && j < colCount))
					{
						result.append(this.colsep);
					}
				}
			}
			result.append(this.recsep);
		}
		return result;
	}
}
