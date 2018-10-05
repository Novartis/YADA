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
package com.novartis.opensource.yada.adaptor;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.Parser;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASQLException;

/**
 * Abstract subclass defining many methods and fields for JDBC functionality including 
 * the construction of SQL statements incorporating pagination, filtering, sorting, and 
 * total row counts.
 * <p>The code in this class is based on Oracle functionality. Other vendor JDBC Adaptor 
 * subclasses should extend this class and override it's methods, particalarly 
 * {@link #buildSelect(String, String, String, int, int, JSONObject)} 
 * and {@link #buildSelectCount(String, JSONObject)} </p>
 *
 * @author David Varon
 *
 */
public abstract class JDBCAdaptor extends Adaptor{

	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(JDBCAdaptor.class);
	/**
	 * Constant equal to: {@code d}
	 */
	protected static final char   DATE              = 'd';
	/**
	 * Constant equal to: {@code t}
	 * @since 5.1.0
	 */
	protected static final char   TIMESTAMP         = 't';
	/**
	 * Constant equal to: {@code i}
	 */
	protected static final char   INTEGER           = 'i';
	/**
	 * Constant equal to: {@code n}
	 */
	protected static final char   NUMBER            = 'n';
	/**
	 * Constant equal to: {@code v}
	 */
	protected static final char   VARCHAR           = 'v';
//	protected static final char   AUTH_USER         = 'u';
	/**
	 * Constant equal to: {@code D}
	 */
	protected static final char   OUTPARAM_DATE     = 'D';
	/**
	 * Constant equal to: {@code I}
	 */
	protected static final char   OUTPARAM_INTEGER  = 'I';
	/**
	 * Constant equal to: {@code N}
	 */
	protected static final char   OUTPARAM_NUMBER   = 'N';
	/**
	 * Constant equal to: {@code V}
	 */
	protected static final char   OUTPARAM_VARCHAR  = 'V';
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String NEWLINE           = "\n";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SPACE             = " ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String OPEN_PAREN        = "(";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String CLOSE_PAREN       = ")";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String OPEN_CURLY        = "{";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String CLOSE_CURLY       = "}";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String COMMA             = ",";
	/**
   * Constant equal to: {@value}
   * @since 8.4.0
   */
  protected static final String QUOTE             = "\"";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_NULL          = "NULL";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_NOT           = "NOT ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_IS            = "IS ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_IN            = "IN ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_SELECT        = "SELECT ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_LIKE          = "LIKE ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_ALL           = "*";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_SELECT_ALL    = SQL_SELECT + SQL_ALL + SPACE;
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_FROM          = "FROM ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_ORDER_BY      = "ORDER BY ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_CORE_ALIAS    = "yadacore";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_WRAPPER_ALIAS = "wrapper";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_WHERE         = "WHERE ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_AND           = "AND ";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String ROWNUM            = "ROWNUM";
	/**
	 * Constant equal to: {@value}
	 */
	public    static final String ROWNUM_ALIAS      = "yada_rnum";
	/**
	 * An alias for the sql COUNT(*) function, e.g., SELECT COUNT(*) COUNT FROM TAB
	 */
	public    static final String SQL_COUNT         = "COUNT";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String SQL_COUNT_ALL     = SQL_COUNT+OPEN_PAREN+SQL_ALL+CLOSE_PAREN+SPACE;
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String ORACLE_DATE_FMT   = "dd-MMM-yy";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String ORACLE_TIMESTAMP_FMT = "dd-MMM-yy hh:mm:ss";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String STANDARD_DATE_FMT = "yyyy-MM-dd";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String STANDARD_TIMESTAMP_FMT = "yyyy-MM-dd HH:mm:ss";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTERKEY_FIELD   = "field";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTERKEY_OP      = "op";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTERKEY_TYPE    = "type";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTERKEY_DATA    = "data";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTERKEY_RULES   = "rules";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTERKEY_GROUPS  = "groups";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTERKEY_GROUPOP = "groupOp";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_EQUAL         = "eq";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_NOTEQUAL      = "ne";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_LESSTHAN      = "lt";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_LESSEQUAL     = "le";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_GREATERTHAN   = "gt";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_GREATEREQUAL  = "ge";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_IN            = "in";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_NOTIN         = "ni";	
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_NULL          = "nu";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_NOTNULL       = "nn";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_BEGINSWITH    = "bw";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_NOTBEGINSWITH = "bn";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_ENDSWITH      = "ew";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_NOTENDSWITH   = "en";	
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_CONTAINS      = "cn";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String FILTER_NOTCONTAINS   = "nc";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String TYPE_NUMBER          = "number";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String TYPE_TEXT            = "text";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String TYPE_EXACTTEXT       = "etxt";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String RX_ALT               = "|";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String RX_CALLABLE          = "^call .+\\(.*\\)\\s*$";
	/**
	 * Constant equal to: {@value}
	 */
	protected static final String EMPTY                = "";
	/**
	 * A constant equal to: {@value}
	 */
	protected static final String LIMIT                = "LIMIT ";
	/**
	 * A constant equal to: {@value}
	 * @since 4.1.0
	 */
	protected static final String OFFSET               = "OFFSET";
	
	
	/**
	 * A convenient variable to hold an empty string by default.  This is potentially configurable in future versions.
	 */
	public    String            NULL_REPLACEMENT = EMPTY; // this will need to be a settable property, maybe NULL_REPLACEMENT_STRING
	
	
	/**
	 * Default constructor for adaptors, includes debug-level logging, but nothing else.
	 */
	public JDBCAdaptor() {
		l.debug("Initializing");
	}
	
	/**
	 * @since 4.0.0
	 * @param yadaReq YADA request configuration
	 */
	public JDBCAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
	
	/**
	 * Executes the statemetns stored in the query object.  Results are
	 * stored in a data structure inside a {@link YADAQueryResult} object
	 * contained by the query object.
	 * @since 4.0.0
	 * @param yq {@link YADAQuery} object containing code to be executed
	 * @throws YADAAdaptorExecutionException when the adaptor can't execute the statement or statements stored in the query 
	 */
	@Override
	public void execute(YADAQuery yq) throws YADAAdaptorExecutionException
	{	
		l.debug("Executing query ["+yq.getQname()+"]");
		boolean     count       = Boolean.parseBoolean(yq.getYADAQueryParamValue(YADARequest.PS_COUNT)[0]);
		boolean     countOnly   = Boolean.parseBoolean(yq.getYADAQueryParamValue(YADARequest.PS_COUNTONLY)[0]);
		int         countResult = -1;
		int         dataSize    = yq.getData().size() > 0 ? yq.getData().size() : 1; 
		for(int row=0;row<dataSize;row++)
		{
			yq.setResult();
			YADAQueryResult yqr = yq.getResult();
			yqr.setApp(yq.getApp());
			if(yq.getType().equals(Parser.CALL))
			{
				CallableStatement c = yq.getCstmt(row);
				for(int i=0;i<yq.getParamCount(row);i++)
				{
					int    position = i+1;
					char   dt       = yq.getDataTypes(row)[i];
					String val      = yq.getVals(row).get(i);
					try
					{
						setQueryParameter(c,position,dt,val);
					} 
					catch (YADASQLException e)
					{
						String msg = "There was an issue building the JDBC/SQL statement";
						throw new YADAAdaptorExecutionException(msg,e);
					}
				}
				
				boolean   hasData   = false;
				
				try
				{
					hasData = c.execute();
				} 
				catch (SQLException e)
				{
					String msg = "CallableStatement failed to execute";
					throw new YADAAdaptorExecutionException(msg,e);
				}
				ResultSet resultSet = null;
				if(hasData)
				{
					try
					{
						resultSet = c.getResultSet();
					} 
					catch (SQLException e)
					{
						String msg = "Unable to get ResultSet from CallableStatement";
						throw new YADAAdaptorExecutionException(msg,e);
					}
				}
				else
				{
					resultSet = new YADAResultSet();
				}
				yqr.addResult(row, resultSet);
				if(count)
				{
					try
					{
						while(resultSet.next())
						{
							countResult++;
						}
					} 
					catch (SQLException e)
					{
						String msg = "There was a problem iterating through the CallableStatement's ResultSet for row count.";
						throw new YADAAdaptorExecutionException(msg,e);
					}
					yqr.addCountResult(row, new Integer(countResult));
				}
			}
			else // SELECT, UPDATE, INSERT, DELETE
			{
				PreparedStatement p  = yq.getPstmt(row);
				for(int i=0;i<yq.getParamCount(row);i++)
				{
					int    position = i+1;
					char   dt       = yq.getDataTypes(row)[i];
					String val      = yq.getVals(row).get(i);
					try
					{
						setQueryParameter(p,position,dt,val);
					}
					catch(YADASQLException e)
					{
						String msg = "There was an issue building the JDBC/SQL statement";
						throw new YADAAdaptorExecutionException(msg,e);
					}
				}
				if(yq.getType().equals(Parser.SELECT))
				{	
					ResultSet resultSet = null;
					try
					{
						if(!countOnly)
						{
							resultSet = p.executeQuery();
							yqr.addResult(row, resultSet);
						}
					} 
					catch (SQLException e)
					{
						String msg = "PreparedStatement for data failed to execute.";
						throw new YADAAdaptorExecutionException(msg,e);
					}
					
					if(count || countOnly)
					{
						p = yq.getPstmtForCount(p);
						for(int i=0;i<yq.getParamCount(row);i++)
						{
							int    position = i+1;
							char   dt       = yq.getDataTypes(row)[i];
							String val      = yq.getVals(row).get(i);
							try
							{
								setQueryParameter(p,position,dt,val);
							}
							catch(YADASQLException e)
							{
								String msg = "There was an issue building the JDBC/SQL statement";
								throw new YADAAdaptorExecutionException(msg,e);
							}
						}
						try
						{
							resultSet = p.executeQuery();
						} 
						catch (SQLException e)
						{
							String msg = "PreparedStatement for row count failed to execute.";
							throw new YADAAdaptorExecutionException(msg,e);
						}
						try
						{
							while(resultSet.next()) 
							{
								countResult = resultSet.getInt(SQL_COUNT);
							}
						} 
						catch (SQLException e)
						{
							String msg = "There was a problem iterating over ResultSet for row count.";
							throw new YADAAdaptorExecutionException(msg,e);
						}
						yqr.addCountResult(row, new Integer(countResult));
					}
				}
				else // UPDATE, INSERT, DELETE
				{
					try
					{
						countResult = p.executeUpdate();
					} 
					catch (SQLException e)
					{
						String msg = "Prepared statement for update failed to execute";
						throw new YADAAdaptorExecutionException(msg,e);
					}
					yqr.addCountResult(row, new Integer(countResult));
				}
			}
		}
	}
	
	/**
	 * Wraps a function call in curly brackets
	 * @param core the SQL function call
	 * @return StringBuffer containing query text surrounded by curly braces
	 */
	public StringBuffer buildCall(String core)
	{
		StringBuffer sql = new StringBuffer();
		sql.append(OPEN_CURLY);
		sql.append(core);
		sql.append(CLOSE_CURLY);
		return sql;
	}
	
	/**
	 * Create a YADA SELECT statement from {@code core} wrapping it in two subqueries to account for pagination, sorting, and 
	 * filtering with a where clause. 
	 * @param core the code to wrap
	 * @param sortKey the column on which to sort
	 * @param sortOrder the sort order 'asc' or 'desc'
	 * @param firstRow the lower limiter of the result set
	 * @param pageSize the number of rows to return
	 * @param filters a JSON object containing the WHERE criteria 
	 * @return StringBuffer containing newly wrapped core sql, with pagination, filtering, and sorting, as desired
	 * @throws YADAAdaptorException when the query filters can't be converted into a WHERE clause
	 */
	public StringBuffer buildSelect(String core, String sortKey, String sortOrder, int firstRow, int pageSize, JSONObject filters) throws YADAAdaptorException 
	{
		StringBuffer sql = new StringBuffer(SQL_SELECT_ALL);
		sql.append(SQL_FROM);
		sql.append(OPEN_PAREN);
		sql.append(NEWLINE+SPACE+SPACE);
		sql.append(SQL_SELECT);
		sql.append(SQL_CORE_ALIAS+"."+SQL_ALL);
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
		sql.append(LIMIT + SPACE + pageSize + SPACE + OFFSET + SPACE + (firstRow-1)); 
		sql.append(NEWLINE);
		if (filters != null)
		{
			sql.append(SQL_WHERE);
			sql.append(NEWLINE+"    "+OPEN_PAREN+NEWLINE);
			sql.append(getQueryFilters(false));
			sql.append(NEWLINE+CLOSE_PAREN+NEWLINE);
		}
		sql.append(CLOSE_PAREN+SPACE+SQL_WRAPPER_ALIAS);
		sql.append(NEWLINE);
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
	public StringBuffer buildSelectCount(String core, JSONObject filters) throws YADAAdaptorException
	{
		StringBuffer sql = new StringBuffer(SQL_SELECT);
		sql.append(SQL_COUNT_ALL);
		sql.append(SQL_COUNT);
		sql.append(NEWLINE);
		sql.append(SQL_FROM);
		sql.append("  ("+NEWLINE);
		sql.append(core);
		sql.append(NEWLINE+"       ) ");
		sql.append(SQL_CORE_ALIAS);
		if (filters != null)
		{
			sql.append(NEWLINE+"  ");
			sql.append(SQL_WHERE);
			sql.append(getQueryFilters(false));
		}
		if (this.yadaReq.getViewLimit() > -1)
		{
			sql.append(NEWLINE);
			sql.append(LIMIT + this.yadaReq.getViewLimit()); /* THIS IS A MYSQL SPECIFIC LINE */
		}
		return sql;
	}
	
	/**
	 * Calls {@link #getQueryFilters(boolean, StringBuffer, JSONObject)} with {@code append} = {@code false} as
	 * well as a {@code null} {@link java.lang.StringBuffer} and filters from {@link #yadaReq}
	 * @param append currently ignored, always reset to {@code false}
	 * @return an SQL representation of the json filter string
	 * @throws YADAAdaptorException when the query filters can't be converted into a WHERE clause
	 */
	protected String getQueryFilters(boolean append) throws YADAAdaptorException
	{
		return getQueryFilters(false,null,this.yadaReq.getFilters());
	}
	
	/**
	 * Converts the filters stored in the local {@link #yadaReq} config as json into an SQL fragment for appendage 
	 * to a {@code WHERE} clause
	 * @since 4.0.0
	 * @param append currently ignored
	 * @param sql the current SQL query to which to append the WHERE clause
	 * @param filters a JSON object containing the WHERE criteria
	 * @return {@link String} SQL translation of {@link org.json.JSONObject#toString()} filters
	 * @throws YADAAdaptorException when the filters cannot be converted into a WHERE clause
	 */
	protected String getQueryFilters(boolean append, StringBuffer sql, JSONObject filters) throws YADAAdaptorException
	{
		JSONArray rules = null;
		JSONArray groups = null;
		StringBuffer lSql = sql;
		/*
		 * {"groupOp":"OR",
		 *  "rules":[{"field":"SAMPLE","op":"cn","data":"hly","type":"text"}],
		 *  "groups":[{"groupOp":"AND",
		 *               "rules":[{"field":"RPKM","op":"ge","data":"20"},
		 *                        {"field":"RPKM","op":"lt","data":"30"}],
		 *              "groups":[]},
		 *            {"groupOp":"AND",
		 *               "rules":[{"field":"RPKM","op":"ge","data":"100"},
		 *                        {"field":"RPKM","op":"lt","data":"1000"}],
		 *              "groups":[]}]}
		 *              
		 *              
		 * {"groupOp":"AND",
		 *    "rules":[],
		 *   "groups":[{"groupOp":"AND",
		 *                "rules":[{"field":"SAMPLE","op":"eq","data":""},
		 *                         {"field":"SAMPLE","op":"eq","data":""}],
		 *               "groups":[]},
		 *             {"groupOp":"AND",
		 *                "rules":[{"field":"SAMPLE","op":"eq","data":""},
		 *                         {"field":"SAMPLE","op":"eq","data":""}],
		 *               "groups":[]}]}
		*/
		if (null == lSql)
		{
			lSql = new StringBuffer();
		}
		try
		{
			String groupOp = filters.getString(FILTERKEY_GROUPOP);
			// init groups and rules
			try 
			{
				rules = filters.has(FILTERKEY_RULES) ? filters.getJSONArray(FILTERKEY_RULES) : null;
				groups = filters.has(FILTERKEY_GROUPS) ? filters.getJSONArray(FILTERKEY_GROUPS) : null;
			}
			catch (JSONException e)
			{
				String msg = "Could not extract filter rules and groups.";
				throw new YADAAdaptorException(msg,e);
			}
			if (null != rules && rules.length() > 0)
			{
				
				for (int i=0;i<rules.length();i++)
				{
					JSONObject obj = rules.getJSONObject(i);
					String field = obj.getString(FILTERKEY_FIELD);
					String op    = obj.getString(FILTERKEY_OP);
					
					// set type
					String type  = TYPE_TEXT;
					try
					{
						type  = obj.has(FILTERKEY_TYPE) ? obj.getString(FILTERKEY_TYPE) : type;
					}
					catch (JSONException e)
					{
						String msg = "Could not extract filter type.";
						throw new YADAAdaptorException(msg,e);
					}
					
					// set value
					String value = obj.getString(FILTERKEY_DATA);
					if (!TYPE_EXACTTEXT.equals(type))
					{
						value = value.toLowerCase();
					}
					
					// prepend group op if not 1st rule
					if (i > 0)
					{
						lSql.append(" " + groupOp + " ");
						
					}
					// nulls, not nulls
					if(op.matches(FILTER_NULL + RX_ALT + FILTER_NOTNULL))
					{
						lSql.append(SQL_CORE_ALIAS+"." + field + " ");
						lSql.append(SQL_IS);
						if (FILTER_NOTNULL.equals(op))  {  lSql.append(SQL_NOT); }
						lSql.append(SQL_NULL);
					}
					// numbers
					else
					{
						// strings:  eq, ne, cn, nc, bw, bn, ew, en, in, ni
						// numbers:  eq, ne, lt, le, gt, ge, in, ni
						if (TYPE_NUMBER.equals(type))
						{
							lSql.append(SQL_CORE_ALIAS+"." + field + " ");
							if(FILTER_NOTEQUAL.equals(op))  {  lSql.append("<> ");  }
							else if(op.matches(FILTER_LESSTHAN + RX_ALT + FILTER_LESSEQUAL))         
															{  lSql.append("< ");   }
							else if(op.matches(FILTER_GREATERTHAN + RX_ALT + FILTER_GREATEREQUAL))   
															{  lSql.append("> ");   }
							if(op.matches(FILTER_EQUAL + RX_ALT
										  +FILTER_LESSEQUAL + RX_ALT
										  +FILTER_GREATEREQUAL))     
															{  lSql.append("= ");   }
							if(FILTER_NOTIN.matches(op))    {  lSql.append(SQL_NOT); }
							if(op.matches(FILTER_IN + RX_ALT + FILTER_NOTIN))        
															{  lSql.append(SQL_IN+"("+value+") "); }
							else                            {  lSql.append(value);  }
						}
						// varchars
						else if (TYPE_TEXT.equals(type) || (TYPE_EXACTTEXT.equals(type)))
						{
							// case sensitive
							if (TYPE_EXACTTEXT.equals(type))
							{
								lSql.append(SQL_CORE_ALIAS+"." + field + " ");
							}
							// case insensitive
							else
							{
								lSql.append("LOWER("+SQL_CORE_ALIAS+"." + field + ") ");
							}
							if(FILTER_EQUAL.equals(op))          {  lSql.append(" = "); }
							else if(FILTER_NOTEQUAL.equals(op))  {  lSql.append(" <> ");}
							
							// not contains, not begins with, not ends with
							if(op.matches(FILTER_NOTCONTAINS + RX_ALT
									      +FILTER_NOTBEGINSWITH + RX_ALT
									      +FILTER_NOTENDSWITH + RX_ALT
									      +FILTER_NOTIN))        {  lSql.append(SQL_NOT);}
							if (op.matches(FILTER_CONTAINS + RX_ALT
									       +FILTER_BEGINSWITH + RX_ALT
									       +FILTER_ENDSWITH))    {  lSql.append(SQL_LIKE); }
							
							// equals, not equals, contains, 
							if (op.matches(FILTER_EQUAL + RX_ALT
									       +FILTER_NOTEQUAL + RX_ALT
									       +FILTER_CONTAINS + RX_ALT
									       +FILTER_NOTCONTAINS + RX_ALT
									       +FILTER_BEGINSWITH + RX_ALT
									       +FILTER_NOTBEGINSWITH + RX_ALT
									       +FILTER_ENDSWITH + RX_ALT
									       +FILTER_NOTENDSWITH)) {  lSql.append("'");     }
							
							// contains, ends with, not ends with
							if (op.matches(FILTER_CONTAINS + RX_ALT
									       +FILTER_ENDSWITH + RX_ALT
									       +FILTER_NOTENDSWITH)) {  lSql.append("%");   }
							if (op.matches(FILTER_EQUAL + RX_ALT
									       +FILTER_NOTEQUAL + RX_ALT
									       +FILTER_CONTAINS + RX_ALT
									       +FILTER_NOTCONTAINS + RX_ALT
									       +FILTER_BEGINSWITH + RX_ALT
									       +FILTER_NOTBEGINSWITH + RX_ALT
									       +FILTER_ENDSWITH + RX_ALT
									       +FILTER_NOTENDSWITH)) { lSql.append(value);  } 
							// in, ni
							else 
							{
								String[] split = value.split(",");
								lSql.append(SQL_IN+"(");
								for (int j=0;j<split.length;j++)
								{
									lSql.append("'"+split[j]+"'");
									if (j<split.length-1)
									{
										lSql.append(",");
									}
								}
								lSql.append(")");
							}
							// contains, begins with, not begins with
							if (op.matches(FILTER_CONTAINS + RX_ALT
									       +FILTER_BEGINSWITH + RX_ALT
									       +FILTER_NOTBEGINSWITH)) {  lSql.append("%"); }
							if (op.matches(FILTER_EQUAL + RX_ALT
								           +FILTER_NOTEQUAL + RX_ALT
								           +FILTER_CONTAINS + RX_ALT
								           +FILTER_NOTCONTAINS + RX_ALT
								           +FILTER_BEGINSWITH + RX_ALT
								           +FILTER_NOTBEGINSWITH + RX_ALT
								           +FILTER_ENDSWITH + RX_ALT
								           +FILTER_NOTENDSWITH))   {  lSql.append("'"); }
						} // end "text"
					} // end "non null ops"
				} // end rules iteration
			} // end is rules null
			if (null != groups && groups.length() > 0)
			{
				for(int i=0;i<groups.length();i++)
				{
					if (i > 0 || (rules != null && rules.length() > 0))
					{
						lSql.append("\n " + groupOp + NEWLINE);	
					}
					JSONObject filter = groups.getJSONObject(i);
					lSql.append("(");
					// recursive call to getQueryFilters to process nested groups
					this.getQueryFilters(false,lSql,filter);
					lSql.append(")");
				}
			}
		} // end try
		catch(JSONException e)
		{
			String msg = "Filter to SQL translation failed";
			throw new YADAAdaptorException(msg,e);
		}
		return lSql.toString();
	}

	/**
	 * Sets the parameter value based on the data type designated in the YADA Markup, mapped to the correct JDBC setter.
	 * @param pstmt the statement in which to set the parameter values
	 * @param index the current parameter
	 * @param type the data type of the parameter
	 * @param val the value to set
	 * @throws YADASQLException when a parameter cannot be set, for instance if the data type is wrong or unsupported
	 */
	protected void setQueryParameter(PreparedStatement pstmt, int index, char type, String val) throws YADASQLException
	{
		String idx = (index < 10) ? " "+String.valueOf(index) : String.valueOf(index);
		l.debug("Setting param [" + idx + "] of type [" + String.valueOf(type) + "] to: " + val);
		try
		{
			switch(type)
			{
			case TIMESTAMP: 
			  setTimestampParameter(pstmt,index,type,val);
        break;
			case DATE:
			  setDateParameter(pstmt,index,type,val);
				break;
			case INTEGER:
				setIntegerParameter(pstmt,index,type,val);
				break;
			case NUMBER:
			  setNumberParameter(pstmt,index,type,val);
				break;
			case OUTPARAM_DATE:
				((CallableStatement) pstmt).registerOutParameter(index,java.sql.Types.DATE);
				break;
			case OUTPARAM_INTEGER:
				((CallableStatement) pstmt).registerOutParameter(index,java.sql.Types.INTEGER);
				break;
			case OUTPARAM_NUMBER:
				((CallableStatement) pstmt).registerOutParameter(index,java.sql.Types.FLOAT);
				break;
			case OUTPARAM_VARCHAR:
				((CallableStatement) pstmt).registerOutParameter(index,java.sql.Types.VARCHAR);
				break;
			default: //VARCHAR2
				pstmt.setString(index, val);
				break;
			}
		}
		catch(SQLException e)
		{
			String msg = "Failed to set parameter.";
			throw new YADASQLException(msg,e);
		}
	}
	
	/**
   * Sets a {@code ?t} parameter value mapped to the correct {@link java.sql.Types#TIMESTAMP} JDBC setter.
   * @param pstmt the statement in which to set the parameter values
   * @param index the current parameter
   * @param type the data type of the parameter (retained here for logging)
   * @param val the value to set
   * @throws SQLException when a parameter cannot be set, for instance if the data type is wrong or unsupported
   * @since 5.1.0
   */
	protected void setTimestampParameter(PreparedStatement pstmt, int index, char type, String val) throws SQLException 
	{
	  if (EMPTY.equals(val) || val == null)
    {
      pstmt.setNull(index, java.sql.Types.TIMESTAMP);
    }
    else
    {
      SimpleDateFormat sdf     = new SimpleDateFormat(STANDARD_TIMESTAMP_FMT);
      ParsePosition    pp      = new ParsePosition(0);
      Date             dateVal = sdf.parse(val,pp);
      if (dateVal != null)
      {
        long t = dateVal.getTime();
        java.sql.Timestamp sqlDateVal = new java.sql.Timestamp(t);
        pstmt.setTimestamp(index, sqlDateVal);
      }
    }
	}
	
	/**
   * Sets a {@code ?d} parameter value mapped to the correct {@link java.sql.Types#DATE} JDBC setter.
   * @param pstmt the statement in which to set the parameter values
   * @param index the current parameter
   * @param type the data type of the parameter (retained here for logging)
   * @param val the value to set
   * @throws SQLException when a parameter cannot be set, for instance if the data type is wrong or unsupported
   * @since 5.1.0
   */
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
      if (dateVal != null)
      {
        long t = dateVal.getTime();
        java.sql.Date sqlDateVal = new java.sql.Date(t);
        pstmt.setDate(index, sqlDateVal);
      }
    }
	}
	
	/**
   * Sets a {@code ?i} parameter value mapped to the correct {@link java.sql.Types#INTEGER} JDBC setter.
   * @param pstmt the statement in which to set the parameter values
   * @param index the current parameter
   * @param type the data type of the parameter (retained here for logging)
   * @param val the value to set
   * @throws SQLException when a parameter cannot be set, for instance if the data type is wrong or unsupported
   * @since 5.1.0
   */
	protected void setIntegerParameter(PreparedStatement pstmt, int index, char type, String val) throws SQLException 
	{
	  try
    {
      int ival = Integer.parseInt(val);
      pstmt.setInt(index, ival);
    }
    catch(NumberFormatException e)
    {
      l.warn("Error: " + e.getMessage() + " caused by " + e.getClass());
      l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
      pstmt.setNull(index, java.sql.Types.INTEGER);
    }
    catch(NullPointerException e)
    {
      l.warn("Error: " + e.getMessage() + " caused by " + e.getClass());
      l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
      pstmt.setNull(index, java.sql.Types.INTEGER);
    }
	}
	
	/**
   * Sets a {@code ?n} parameter value mapped to the correct {@link java.sql.Types#FLOAT} JDBC setter.  
   * @param pstmt the statement in which to set the parameter values
   * @param index the current parameter
   * @param type the data type of the parameter (retained here for logging)
   * @param val the value to set
   * @throws SQLException when a parameter cannot be set, for instance if the data type is wrong or unsupported
   * @since 5.1.0
   */
	protected void setNumberParameter(PreparedStatement pstmt, int index, char type, String val) throws SQLException
	{
	  try
    {
      float fval = Float.parseFloat(val);
      pstmt.setFloat(index, fval);
    }
    catch(NumberFormatException | NullPointerException e)
    {
      l.warn("Error: " + e.getMessage() + " caused by " + e.getClass());
      l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
      pstmt.setNull(index, java.sql.Types.INTEGER);
    }
	}
}



