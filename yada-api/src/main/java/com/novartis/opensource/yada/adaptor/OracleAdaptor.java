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
package com.novartis.opensource.yada.adaptor;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.novartis.opensource.yada.YADARequest;

/**
 * JDBCAdaptor class for execution of Oracle SQL queries.  This is a subclass of {@link JDBCAdaptor} but only 
 * overrides the constructors.  {@link JDBCAdaptor}, thought abstract, was initially created as an
 * Oracle adaptor, and by and large, contains generic code.
 * 
 * @author David Varon
 *
 */
public class OracleAdaptor extends JDBCAdaptor {
	/**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(OracleAdaptor.class);
	/**
	 * Default constructor
	 */
	public OracleAdaptor() {
		super();
		l.debug("Initializing");
	}
	
	/**
	 * Preferred "YADARequest" constructor
	 * @param yadaReq YADA request configuration
	 */
	public OracleAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
	
	/**
	 * Create a YADA SELECT statement from {@code core} wrapping it in two subqueries to account for pagination, sorting, and 
	 * filtering with a where clause. 
	 * @param core the code to wrap
	 * @param sortKey the column on which to sort
	 * @param sortOrder deprecated:  the sort order 'asc' or 'desc', include sort direction in sort key 
	 * @param firstRow the lower limiter of the result set
	 * @param pageSize the number of rows to return
	 * @param filters a JSON object containing the WHERE criteria 
	 * @return StringBuffer containing newly wrapped core sql, with pagination, filtering, and sorting, as desired
	 * @throws YADAAdaptorException when the query filters can't be converted into a WHERE clause
	 * @see JDBCAdaptor
	 */
	@Override
	public StringBuffer buildSelect(String core, String sortKey, String sortOrder, int firstRow, int pageSize, JSONObject filters) throws YADAAdaptorException 
	{
		StringBuffer sql = new StringBuffer(SQL_SELECT_ALL);
		sql.append(SQL_FROM);
		sql.append(OPEN_PAREN);
		sql.append(NEWLINE+SPACE+SPACE);
		sql.append(SQL_SELECT);
		sql.append(SQL_CORE_ALIAS+"."+SQL_ALL);
		sql.append(COMMA+SPACE);
		sql.append(ROWNUM+SPACE+ROWNUM_ALIAS); /*  THIS IS AN ORACLE SPECIFIC LINE */
		sql.append(NEWLINE+SPACE+SPACE);
		sql.append(SQL_FROM);
		sql.append(SPACE+OPEN_PAREN+NEWLINE);
		sql.append(core);
		if (null != sortKey && !sortKey.equals(EMPTY))
		{
			if (core.toUpperCase().indexOf(SQL_ORDER_BY) == -1)
			{
				sql.append(NEWLINE);
				sql.append(SQL_ORDER_BY);
			}
			else
			{
				sql.append(COMMA+SPACE);
			}
			sql.append(sortKey);
			if (null != sortOrder && !sortOrder.equals(EMPTY))
			{
				 sql.append(SPACE + sortOrder);
			}
		}
		sql.append(NEWLINE+"       "+CLOSE_PAREN+SPACE);
		sql.append(SQL_CORE_ALIAS);
		sql.append(NEWLINE+SPACE+SPACE);
		sql.append(SQL_WHERE);
		sql.append(ROWNUM+SPACE+"< " + String.valueOf(firstRow+pageSize)); /*  THIS IS AN ORACLE SPECIFIC LINE */
		if (filters != null)
		{
			sql.append(NEWLINE+"    ");
			sql.append(SQL_AND);
			sql.append(NEWLINE+"    "+OPEN_PAREN+NEWLINE);
			sql.append(getQueryFilters(false));
			sql.append(NEWLINE+CLOSE_PAREN+NEWLINE);
		}
		sql.append(CLOSE_PAREN+SPACE+SQL_WRAPPER_ALIAS);
		sql.append(NEWLINE);
		sql.append(SQL_WHERE);
		sql.append(ROWNUM_ALIAS+SPACE+">="+SPACE+firstRow); /*  THIS IS AN ORACLE SPECIFIC LINE */
		return sql;
	}
	
	/**
	 * Returns a StringBuffer with core sql + filters, wrapped in an outer sql COUNT(*) query.  
	 * This is typically used in pagination scenarios to return the total number of records 
	 * returnable by a query when only a subset are requested by the application.
	 * 
	 * @param core the SQL to wrap in an outer count(*) query
	 * @param filters a JSON object containing the WHERE criteria
	 * @return StringBuffer of wrapped core sql including filters
	 * @throws YADAAdaptorException when the query filters can't be converted into a WHERE clause
	 */
	@Override
	public StringBuffer buildSelectCount(String core, JSONObject filters) throws YADAAdaptorException
	{
		boolean hasFilter = false;
		StringBuffer sql = new StringBuffer(SQL_SELECT);
		sql.append(SQL_COUNT_ALL);
		sql.append(SQL_COUNT);
		sql.append(NEWLINE);
		sql.append(SQL_FROM);
		sql.append(SPACE+SPACE+OPEN_PAREN+NEWLINE);
		sql.append(core);
		sql.append(NEWLINE+"       "+CLOSE_PAREN+SPACE);
		sql.append(SQL_CORE_ALIAS);
		if (filters != null)
		{
			sql.append(NEWLINE+SPACE+SPACE);
			sql.append(SQL_WHERE);
			sql.append(getQueryFilters(false));
			hasFilter = true;
		}
		if (this.yadaReq.getViewLimit() > -1)
		{
			if (hasFilter) 
			{ 
				sql.append(NEWLINE+SPACE+SPACE);
				sql.append(SQL_AND); 
			} 
			else 
			{ 
				sql.append(NEWLINE);
				sql.append(SQL_WHERE); 
			}
			
			sql.append("ROWNUM <=" + this.yadaReq.getViewLimit()); /*  THIS IS AN ORACLE SPECIFIC LINE */
		}
		return sql;
	}
	
	/**
   * Enables checking for {@link JDBCAdaptor#ORACLE_TIMESTAMP_FMT} if {@code val} does not conform to {@link JDBCAdaptor#STANDARD_TIMESTAMP_FMT}
   * @since 5.1.1
   */
  @Override
  protected void setTimestampParameter(PreparedStatement pstmt, int index, char type, String val) throws SQLException 
  {
    if (EMPTY.equals(val) || val == null)
    {
      pstmt.setNull(index, java.sql.Types.DATE);
    }
    else
    {
      SimpleDateFormat sdf     = new SimpleDateFormat(STANDARD_TIMESTAMP_FMT);
      ParsePosition    pp      = new ParsePosition(0);
      Date             dateVal = sdf.parse(val,pp);
      if(dateVal == null)
      {
        sdf     = new SimpleDateFormat(ORACLE_TIMESTAMP_FMT);
        pp      = new ParsePosition(0);
        dateVal = sdf.parse(val,pp);
      }
      if (dateVal != null)
      {
        long t = dateVal.getTime();
        java.sql.Timestamp sqlDateVal = new java.sql.Timestamp(t);
        pstmt.setTimestamp(index, sqlDateVal);
      }
    }
  }
	
	/**
	 * Enables checking for {@link JDBCAdaptor#ORACLE_DATE_FMT} if {@code val} does not conform to {@link JDBCAdaptor#STANDARD_DATE_FMT}
	 * @since 5.1.1
	 */
	@Override
	protected void setDateParameter(PreparedStatement pstmt, int index, char type, String val) throws SQLException 
  {
    if (EMPTY.equals(val) || val == null)
    {
      pstmt.setNull(index, java.sql.Types.DATE);
    }
    else
    {
      SimpleDateFormat sdf     = new SimpleDateFormat(STANDARD_DATE_FMT);
      ParsePosition    pp      = new ParsePosition(0);
      Date             dateVal = sdf.parse(val,pp);
      if(dateVal == null)
      {
        sdf     = new SimpleDateFormat(ORACLE_DATE_FMT);
        pp      = new ParsePosition(0);
        dateVal = sdf.parse(val,pp);
      }
      if (dateVal != null)
      {
        long t = dateVal.getTime();
        java.sql.Date sqlDateVal = new java.sql.Date(t);
        pstmt.setDate(index, sqlDateVal);
      }
    }
  }
}
