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

import java.net.HttpCookie;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.soap.SOAPConnection;

import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.Statement;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.adaptor.Adaptor;
import com.novartis.opensource.yada.adaptor.FileSystemAdaptor;
import com.novartis.opensource.yada.adaptor.JDBCAdaptor;
import com.novartis.opensource.yada.util.QueryUtils;

/**
 * The processing unit for a YADA query, containing the following, where applicable:
 * <ul>
 * 	<li>Stored code</li>
 *  <li>Parsed code</li>
 *  <li>List of {@link YADAParam}s</li>
 *  <li>Lists of {@link java.sql.PreparedStatement}s for query and result count</li>
 *  <li>List of {@link java.sql.CallableStatement}s</li>
 *  <li>List of {@link java.net.URL}s</li>
 *  <li>List of cookie names, for REST queries, where specified</li>
 *  <li>Array of parameterized columns</li>
 *  <li>Array of columns referenced by SQL {@code IN} clauses</li>
 *  <li>SQL transactional info</li>
 *  <li>{@link java.sql.Connection}</li>
 *  <li>{@link javax.xml.soap.SOAPConnection}</li>
 *  <li>List of list of values to be processed</li>
 *  <li>List of {@link YADAQueryResult}s</li>
 *  <li>{@link JDBCAdaptor} class and object</li>
 * </ul>
 * @author David Varon
 * @since 4.0.0
 */
public class YADAQuery {

	
  /**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(YADAQuery.class);
	/**
	 * Constant equal to {@value}
	 */
	private static final String PROP_PROTECTED = "protected";
	/**
	 * Cached-status indicator
	 */
	private boolean isCached = false;
	/**
	 * The app to which the query is mapped
	 * @since 7.1.0
	 */
	private String     app;
	/**
	 * The query name 
	 */
	private String     qname;
	/**
	 * The query source mapped to the app to which the query is mapped
	 */
	private String     source;
	/**
	 * The code mapped to the qname, including YADA markup
	 */
	private String     coreCode;
	/**
	 * The code, stripped of YADA markup
	 */
	private String     conformedCode;
	/**
	 * The parsed object representation of the SQL query
	 * @since 7.0.0
	 */
	private Statement  statement;
	/**
	 * The version of the framework set in the index for the app
	 */
	private String     version;
	/**
	 * A list of the parameters
	 */
	private List<YADAParam>              					yqParams = new ArrayList<>();
	/**
	 * A list of properties
	 * @since 7.1.0
	 */
	private Set<YADAProperty>                     properties = new HashSet<>();
	/**
	 * The cookies
	 * @since 5.1.0
	 */
	private List<HttpCookie>                      cookies  = new ArrayList<>();
	/**
	 * The headers
	 * @since 8.5.0
	 */
	private JSONObject                            httpHeaders = new JSONObject();
	/**
	 * A list containing maps, one for each query execution, of data key/value pairs, supporting arrays of values
	 */
	private List<LinkedHashMap<String,String[]>>  data     = new ArrayList<>();
	/**
	 * A list of columns referenced by the query that correspond to values in the {@link #data} maps
	 */
	private String[]   parameterizedColumns;
	/**
	 * The columns mapped to YADA markup
	 */
	private List<Column> parameterizedColumnList;
	/**
	 * A list of columns referenced by the query occur in SQL {@code IN} clauses
	 */
	private String[]   ins;
	/**
	 * The list of columns in sql {@code IN} expressions
	 */
	private List<Column> inList;
	/**
	 * A list of all columns referenced by the query
	 * @deprecated as of 7.1.0 
	 */
	@Deprecated
	private String[]   columns;
	/**
   * A list of all columns referenced by the query
   * @since 7.1.0 
   */
	private List<Column> columnList;
	/**
	 * The db connection to be used to execute the query if applicable
	 */
	private Connection connection;
	/**
	 * The SOAP connection to be used to execute the query, if applicable
	 */
	private SOAPConnection soapConnection;
	/**
	 * The rollback point for a transactional jdbc query
	 */
	private Savepoint  savepoint;
	/**
	 * The YADA jDBCAdaptor class to instantiate for query construction and execution 
	 */
	private Class<Adaptor>  adaptorClass;
	/**
	 * The YADA jDBCAdaptor object to instantiate for query construction and execution
	 */
	private Adaptor         adaptor;
	/**
	 * The nature of the query, either {@link Parser#SOAP}, 
	 * {@link QueryUtils#READ}, 
	 * {@link QueryUtils#WRITE}, 
	 * {@link QueryUtils#APPEND}, 
	 * {@link Parser#REST}, 
	 * {@link Parser#SELECT}, 
	 * {@link Parser#INSERT}, 
	 * {@link Parser#UPDATE}, 
	 * or {@link Parser#DELETE}
	 * @see FileSystemAdaptor
	 * @see Parser
	 */
	private String          type;
	/**
	 * The connection string qualifier, either {@link Parser#JDBC}, {@link Parser#SOAP}, {@link Parser#REST}, or {@link Parser#FILE}
	 */
	private String          protocol;
	/**
	 * Container for indexed raw, converted, and count results corresponding to indices in {@link #data}
	 */
	private YADAQueryResult yadaQueryResult;
	/**
	 * The number of query parameters in the code, corresponding to indices in {@link #data}
	 */
	private List<Integer>           paramCount    = new ArrayList<>();
	/**
	 * The data types of query parameters in the code, corresponding to indices in {@link #data}.
	 * Values can be {@link JDBCAdaptor}{@code .DATE}, {@link JDBCAdaptor}{@code .INTEGER}, {@link JDBCAdaptor}{@code .VARCHAR}, or {@link JDBCAdaptor}{@code .NUMBER}
	 * @see JDBCAdaptor
	 */
  private List<char[]>            dataTypes     = new ArrayList<>();
	/**
	 * The values for query parameters in the code, corresponding to indices in {@link #data}
	 */
	private List<List<String>>      vals          = new ArrayList<>();
	/**
	 * The executable jdbc objects for SQL queries, corresponding to indices in {@link #data}
	 */
	private List<PreparedStatement> pstmt         = new ArrayList<>();
	/**
	 * The executable jdbc objects for count queries, corresponding to indices in {@link #data}
	 */
	private HashMap<PreparedStatement,PreparedStatement> pstmtForCount = new HashMap<>();
	/**
	 * The executable jdbc objects for SQL function calls, corresponding to indices in {@link #data}
	 */
	private List<CallableStatement> cstmt         = new ArrayList<>();
	/**
	 * The urls for REST requests, corresponding to indices in {@link #data}
	 */
	private List<String>            url           = new ArrayList<>();
	/**
	 * The query strings for SOAP requests, corresponding to indices in {@link #data}
	 */
	private List<String>            soap          = new ArrayList<>();
	/**
	 * The YADA request parameter names
	 */
	private Map<String,List<YADAParam>>   keys          = new HashMap<>();
	/**
	 * The non-overridable YADA request parameter keys
	 */
	private Map<String,List<YADAParam>>   immutableKeys = new HashMap<>();
	/**
   * The container of field names to support merged result sets
   * @since 6.1.0
   */
  private JSONObject globalHarmonyMap;
  /**
   * A map of {@link Column} objects to {@link InExpression} objects
   * @since 7.1.0
   */
  private Map<Column, InExpression> inExpressionMap;
	/**
	 * Placeholder for the access count, to facilitate vendor-agnosticism
	 * @since 8.1.1
	 * @see "https://github.com/Novartis/YADA/issues/24"
	 */
	private int accessCount;
	/**
	 * Default constructor
	 */
	public YADAQuery() {}
	
	/**
	 * Cloning constructor
	 * @param yq the cached {@link YADAQuery} to clone
	 * @since 4.1.0
	 */
	public YADAQuery(YADAQuery yq) {
		this.setVersion(new String(yq.getVersion()));
		this.setCoreCode(new String(yq.getYADACode()));
		String legacySource = yq.getSource() == null ? "" : yq.getSource();
		this.setSource(legacySource);
		this.setQname(new String(yq.getQname()));
		this.setApp(new String(yq.getApp()));
		for (YADAParam cachedParam : yq.getYADAQueryParams())
		{
			YADAParam param = new YADAParam();
			if(cachedParam.isDefault())
			{
			  param.setId(cachedParam.getId());
  			param.setName(cachedParam.getName());
  			param.setValue(cachedParam.getValue());
  			param.setTarget(cachedParam.getTarget());
  			param.setRule(cachedParam.getRule());
  			param.setDefault(cachedParam.isDefault());
  			this.addParam(param);
			}
		}
		this.setProperties(yq.getProperties());
	}
	
	/**
	 * Standard mutator for variable
	 * @param qname the name of the query encapsulated by this object
	 */
	public void setQname(String qname) { this.qname = qname; }
	/**
	 * Standard mutator for variable
	 * @param source the jndi string or url for the source connection associated to this query's YADA app  
	 */
	public void setSource(String source) { this.source = source; }
	/**
	 * Standard mutator for variable
	 * @param version the YADA version of this query's YADA app
	 */
	public void setVersion(String version) { this.version = version; }
	/**
	 * Standard mutator for variable
	 * @param coreSql the content of the query
	 */
	public void setCoreCode(String coreSql) { this.coreCode = coreSql; }
	/**
	 * Standard mutator for variable
	 * @param yqr the result object stored in the query
	 */
	public void setResult(YADAQueryResult yqr) 
	{ 
	  this.yadaQueryResult = yqr;
	  if(yqr.getGlobalHarmonyMap() == null && getGlobalHarmonyMap() != null)
	    yqr.setGlobalHarmonyMap(getGlobalHarmonyMap());
	}
	
	/**
	 * Checks if instance variable {@link #yadaQueryResult} is {@code null}, and if so, creates a 
	 * new {@link YADAQueryResult} with parameters
	 */
	public void setResult() 
	{
		if(getResult() == null)
		{
		  YADAQueryResult yqr = new YADAQueryResult(getYADAQueryParams());
		  yqr.setGlobalHarmonyMap(getGlobalHarmonyMap());
			setResult(yqr);
		}
	}
	
	/**
	 * Standard mutator for variable.  The value should equal one of the following:
	 * <ul>
	 * 	<li>{@link Parser#JDBC}</li>
	 *  <li>{@link Parser#SOAP}</li>
	 *  <li>{@link Parser#REST}</li>
	 * </ul>
	 * @param protocol the protocol for query execution
	 */
	public void setProtocol(String protocol) { this.protocol = protocol; }
	
	/**
	 * Standard mutator for variable
	 * @param pstmt the list of jdbc api objects for sql query execution
	 */
	public void setPstmt(List<PreparedStatement> pstmt) { this.pstmt = pstmt; }
	/**
	 * Adds the parameter to the internal {@link java.util.List} of {@link java.sql.PreparedStatement}s
	 * @param pstmtToAdd the statement to add to the internal list
	 */
	public void addPstmt(PreparedStatement pstmtToAdd) { this.pstmt.add(pstmtToAdd); }
	/**
	 * Replaces an existing {@link PreparedStatement} with {@code pstmtToAdd} or appends
	 * if {@code row} is out of range; also removes 
	 * the {@link PreparedStatement} entry from {@link #pstmtForCount} if it exists.
	 * @param pstmtToAdd the {@link PreparedStatement} to add to the list
	 * @param row the position in {@link #pstmt} to store {@code pstmtToAdd}
	 * @since 7.0.0
	 */
	public void addPstmt(PreparedStatement pstmtToAdd, int row) {
	  // remove count query
	  if(this.getPstmt().size() > row && this.getPstmtForCount().containsKey(this.getPstmt().get(row)))
	    this.getPstmtForCount().remove(getPstmt().get(row));
	  
	  // append or replace
	  if(this.getPstmt().size() > row)
	    this.getPstmt().set(row,pstmtToAdd);
	  else
	    this.getPstmt().add(pstmtToAdd);
	}
	/**
	 * Standard mutator for variable
	 * @param cstmt the list of jdbc api objects for sql query execution
	 */
	public void setCstmt(List<CallableStatement> cstmt) { this.cstmt = cstmt; }
	/**
	 * Adds the parameter to the internal {@link java.util.List} of {@link java.sql.CallableStatement}s
	 * @param cstmtToAdd the statement to add to the internal list
	 */
	public void addCstmt(CallableStatement cstmtToAdd) { this.cstmt.add(cstmtToAdd); }
	
	/**
	 * Standard mutator for variable
	 * @param url the list of url objects as strings for REST query execution
	 */
	public void setUrl(List<String> url) { this.url = url; }
	/**
	 * Adds {@code urlToAdd} to the existing list
	 * @param urlToAdd the url to add to the internal list
	 */
	public void addUrl(String urlToAdd) { this.url.add(urlToAdd); }
	
	/**
	 * Standard mutator for variable
	 * @param soap the list of SOAP objects as strings for SOAP query execution
	 */
	public void setSoap(List<String> soap) { this.soap = soap; }
	/**
	 * Adds {@code soapToAdd} to the existing list of SOAP queries.
	 * @param soapToAdd the soap query ot add to the internal list
	 */
	public void addSoap(String soapToAdd) { this.soap.add(soapToAdd); }
	
	/**
	 * Standard mutator for variable
	 * @param pstmtForCount the HashMap of count statements mapped to their data query counterparts
	 */
	public void setPstmtForCount(HashMap<PreparedStatement,PreparedStatement> pstmtForCount) { this.pstmtForCount = pstmtForCount; }
	/**
	 * Adds {@code value}, a new {@link java.sql.PreparedStatement} for execution of a count query to the internal hash,
	 * keyed off {@code key}, the existing {@link java.sql.PreparedStatement} containing the requested query
	 * @param key data sql
	 * @param value count sql
	 */
	public void addPstmtForCount(PreparedStatement key, PreparedStatement value) { this.pstmtForCount.put(key, value); }
	
	/**
	 * Standard mutator for variable
	 * @param paramCount list of number of parameters for each data row
	 */
	public void setParamCount(List<Integer> paramCount) { this.paramCount = paramCount; }
	
	/**
	 * Stores the number of yada parameters contained in the query string.  The param count can vary 
	 * if a query contains a function with a dynamic argument list, such as an SQL {@code IN} clause.
	 * Consequently, param counts are indexed to the data-map list.
	 *  
	 * @param row the index of the data map in the list
	 * @param paramCountToAdd number of params for row
	 */
	public void addParamCount(int row, int paramCountToAdd) { 
		if(this.paramCount.isEmpty() || row >= this.paramCount.size())
			this.paramCount.add(row, new Integer(paramCountToAdd));
		else
			this.paramCount.set(row, new Integer(paramCountToAdd));
	}
	
	/**
	 * Standard mutator for variable
	 * @param dataTypes list of arrays of data types
	 */
	public void setDataTypes(List<char[]> dataTypes) { this.dataTypes = dataTypes; } 
	
	/**
	 * Stores the number of yada parameter data types referenced in the query string.  The data type list can vary 
	 * if a query contains a function with a dynamic argument list, such as an SQL {@code IN} clause.
	 * Consequently, data types are stored in arrays, and indexed to the data-map list.
	 * 
	 * @param row the index of the data map in the list
	 * @param dataTypesToAdd array of data types
	 */
	public void addDataTypes(int row, char[] dataTypesToAdd) { 
		if(this.dataTypes.isEmpty() || row >= this.dataTypes.size())
			this.dataTypes.add(row,dataTypesToAdd);
		else
			this.dataTypes.set(row,dataTypesToAdd);
	}
	
	/**
	 * Standard mutator for variable
	 * @param vals list of lists of values
	 */
	public void setVals(List<List<String>> vals) { this.vals = vals; }
	
	/**
	 * Stores the values to be applied to the query parameters. The value count can vary 
	 * if a query contains a function with a dynamic argument list, such as an SQL {@code IN} clause.
	 * Consequently, values are stored in lists, and indexed to the data-map list.
	 * 
	 * @param row the index of the data map in the list
	 * @param valsToAdd list of values
	 */
	public void addVals(int row, List<String> valsToAdd) { 
		if(this.vals.isEmpty() || row >= this.vals.size())
			this.vals.add(row,valsToAdd);
		else
			this.vals.set(row,valsToAdd);
	}
		
	/**
	 * Standard mutator for variable
	 * @param yqParams parameter objects list
	 */
	public void setYADAQueryParams(List<YADAParam> yqParams) 
	{
		this.yqParams = yqParams;
		if(this.yqParams.size() > 0) 
		{
			setKeys();
		}
	}
	
	/**
	 * Parses the {@code yadaReq} object to internalize and index the current request parameters
	 * @param yadaReq YADA request configuration
	 * @throws YADARequestException if the request is malformed
	 */
	public void setYADAQueryParams(YADARequest yadaReq) throws YADARequestException 
	{
		this.yqParams = yadaReq.getAllParams();
		if(this.yqParams.size() > 0) 
		{
			setKeys();
		}
	}
	
	/**
	 * a {@link List} of {@link YADAParam} objects with {@link YADAParam#getName()} = {@code key}.
	 * 
	 * @param key the param name
	 * @return a {@link List} of {@link YADAParam} objects with {@link YADAParam#getName()} = {@code key}
	 * @since 7.1.4
	 */
	public List<YADAParam> getYADAQueryParamsForKey(String key) {
	  List<YADAParam> paramList = new ArrayList<>(1000);
	  for(YADAParam yp : this.yqParams)
	  {
	    if(yp.getName().equals(key))
	      paramList.add(yp);
	  }
	  return paramList;
	}
	
	/**
	 * Returns an array containing the value associated to param with name {@code key}
	 * @param key name of parameter whose value is sought
	 * @return an array containing the value associated to param with name {@code key} 
	 */
	public String[] getYADAQueryParamValue(String key) {	
		boolean hasVal = false;
		if(getParam(key) != null)
		{
	    String[] values = new String[1000];
	    for(YADAParam param : getParam(key))
	    {
	      values[param.getId()] = param.getValue();
	      hasVal = true;
	    	//TODO 'id' is used as "rank" or order of execution.  This is bad.  
      	// Need a sequence field in the model, or a ui gadget (dnd) to change the 
      	// order (it can only be done in the db directly now). If the same 'id'
      	// field is reused, it should be renamed to 'seq','ord','rank' etc.      	
	    }
	    if(hasVal)
	      return Arrays.stream(values).filter(Objects::nonNull).toArray(String[]::new);	    
		}
		return null;		
	}
	
	/**
   * Returns an array containing the value associated to param with name {@code key}
   * where target {@link YADAQuery#getQname} of {@code this} 
   * @param key name of parameter whose value is sought
   * @return an array containing the value associated to param with name {@code key} 
   */
  public String[] getYADAQueryParamValuesForTarget(String key) {
  	boolean hasVal = false;
    if(getParam(key) != null)
    {
      String[] values = new String[1000];
      for(YADAParam param : getParam(key))
      {
        if(param.getTarget().equals(this.getQname()))
        {
        	values[param.getId()] = param.getValue();
        	hasVal = true;
        	//TODO 'id' is used as "rank" or order of execution.  This is bad.  
        	// Need a sequence field in the model, or a ui gadget (dnd) to change the 
        	// order (it can only be done in the db directly now). If the same 'id'
        	// field is reused, it should be renamed to 'seq','ord','rank' etc.
        	
        }
      }
      if(hasVal)
	      return Arrays.stream(values).filter(Objects::nonNull).toArray(String[]::new);	    
    }
    return null;    
  }
  
  /**
   * Returns an array containing the value associated to param with name {@code key}
   * where target is arbitrary, usually a qname or plugin name 
   * @param key name of parameter whose value is sought
   * @param target name of target for param value, usually a qname or plugin name
   * @return an array containing the value associated to param with name {@code key} and target {@code target}
   * @since 8.5.0
   */
  public String[] getYADAQueryParamValuesForTarget(String key, String target) {
  	boolean hasVal = false;
    if(getParam(key) != null)
    {
      String[] values = new String[1000];
      for(YADAParam param : getParam(key))
      {
        if(param.getTarget().equals(target))
        {
        	//TODO 'id' is used as "rank" or order of execution.  This is bad.  
        	// Need a sequence field in the model, or a ui gadget (dnd) to change the 
        	// order (it can only be done in the db directly now). If the same 'id'
        	// field is reused, it should be renamed to 'seq','ord','rank' etc.        	
        	values[param.getId()] = param.getValue();
        	hasVal = true;
        }
          
      }
      if(hasVal)
	      return Arrays.stream(values).filter(Objects::nonNull).toArray(String[]::new);	    
    }
    return null;    
  }
	
	/**
	 * Tests value of parameter name
	 * @param key the name of the param
	 * @return {@code true} if {@code key} = {@link YADARequest#PL_PLUGIN} or {@link YADARequest#PS_PLUGIN}
	 * @since 8.3.0
	 */
	private boolean isPluginParam(String key) {
	  if(key.equals(YADARequest.PL_PLUGIN) || key.equals(YADARequest.PS_PLUGIN))
	    return true;
	  return false;
	}
	
	/**
	 * Tests value of parameter name
	 * @param key the name of the param
	 * @return {@code true} if {@code key} = {@link YADARequest#PS_ARGLIST}
	 * @since 8.5.0
	 */
	private boolean isArgumentParam(String key) {
	  if(key.equals(YADARequest.PS_ARGLIST))
	    return true;
	  return false;
	}
	
	/**
	 * Returns the {@link YADAParam} with name = {@code key}
	 * @param key the name of the desired param
	 * @return the param object
	 */
	public List<YADAParam> getParam(String key)
	{
		return this.keys.get(key);
	}
	
	/**
	 * Retrievs a list of columns stored in the first indexed data object.  The
	 * list will either contain the column names passed in 
	 * {@link YADARequest#PS_JSONPARAMS} or those created on the fly in standard 
	 * params scenarios, i.e., {@link QueryUtils#YADA_COLUMN} + {@code int}
	 * @return list of columns stored in the first indexed data object
	 */
	public String[] getColumnNameArray() 
	{
		Set<String> keySet = getData().get(0).keySet();
		return keySet.toArray(new String[keySet.size()]);
	}
	
	/**
	 * Iterates over {@link #yqParams} list to populate indices of {@link #keys} 
	 * and {@link #immutableKeys}, for faster access to parameter values
	 */
	private void setKeys() 
	{
		for(YADAParam param : this.yqParams)
		{
		  String key = param.getName();
		  if(hasOverridableParam(key))
		  {
		    this.keys.get(key).add(param);
	      if(param.getRule() != 0)
	        this.immutableKeys.get(key).add(param);
		  }
		  else
		  {
		    List<YADAParam> list = new ArrayList<>();
		    this.keys.put(key,list);
		    this.immutableKeys.put(key,list);
		    this.keys.get(key).add(param);
	      if(param.getRule() != 0)
	        this.immutableKeys.get(key).add(param);
		  }
		}
	}
	
	/**
	 * Adds a single {@link YADAParam} to the {@link #keys} and {@link #immutableKeys} indices. 
	 * @param param the parameter to add to each index, as needed
	 */
	private void setKey(YADAParam param)
	{
		String key = param.getName();
		List<YADAParam> list = null;
		if(!hasParam(key))
    {
		  list = new ArrayList<>();
		  list.add(param);
      this.keys.put(key,list);
      if(param.getRule() != YADAParam.OVERRIDEABLE)
      {
        if(!hasNonOverridableParam(key))
        {
          list = new ArrayList<>();
          list.add(param);
          this.immutableKeys.put(key,list);
        }
      }
    }
		else if(isPluginParam(key)) // if it's a plugin, just add it straight away
		{
		  this.keys.get(key).add(param);
      if(param.getRule() != YADAParam.OVERRIDEABLE)
        this.immutableKeys.get(key).add(param);
    }
		else if(isArgumentParam(key))
		{
			this.keys.get(key).add(param);
			if(param.getRule() != YADAParam.OVERRIDEABLE)
				this.immutableKeys.get(key).add(param);
		}
		else
		{
		  // if the param is not a plugin param, and it's 
		  // overrideable, add it. If it is, itself, nonoverridable,
		  // make a note of that too.
	    if(!hasNonOverridableParam(key)) // this confirms no immutables (non-overrides)
	    {
		    this.keys.get(key).add(param);
	      if(param.getRule() != YADAParam.OVERRIDEABLE)
	        this.immutableKeys.get(key).add(param);
	    }
		}
	}
	
	/**
	 * Removes values from {@link #immutableKeys} and {@link #keys}. 
	 * @since 4.1.0
	 */
	public void clearKeys() 
	{
		this.keys.clear();
		this.immutableKeys.clear();
	}
	
	/**
	 * Creates a new query-level, overrideable {@link YADAParam} with a name of {@code key} and a value of {@code val} and adds it to the list.
	 * @param key name of parameter
	 * @param val value of parameter
	 */
	public void addParam(String key,String val) {
		YADAParam param = new YADAParam(key,val,this.getQname(), YADAParam.OVERRIDEABLE);
		addParam(param);
	}
	
	/**
	 * Adds {@code param} to the internal list.  Allows multiple plugins.
	 * @param param parameter object
	 */
	public void addParam(YADAParam param)
	{
		String key = param.getName();
		if(isPluginParam(key) || isArgumentParam(key))
		{
		  List<YADAParam> lp = getYADAQueryParams();
      String  value  = param.getValue();
      String  target = param.getTarget();
      int     rule   = param.getRule();
      boolean add    = true;
      for(YADAParam p : lp)
      {
        if(value.equals(p.getValue()) && target.equals(p.getTarget()) && rule == p.getRule())
        {
          add = false;
          break;
        }
      }
      if(add)
      {
        getYADAQueryParams().add(param);
        setKey(param);
      }
		}
		else
    {
		  
	    if(hasNonOverridableParam(key)) 
      {
        l.debug("An attempt was made to override a non-overridable default parameter. The original contract was honored.");
      }
      else if(hasParam(key))
      {
        replaceParam(param);
        setKey(param);  
      }
      else
      {
        getYADAQueryParams().add(param);
        setKey(param);
      }
    }
	}
	
	/**
	 * Adds the property to the {@link Set} of {@link #properties} 
	 * @param prop the {@link YADAProperty} object to add to this {@link YADAQuery} object
	 * @since 7.1.0
	 */
	public void addProperty(YADAProperty prop)
	{
	  this.getProperties().add(prop);
	}
	
	/**
	 * Adds the cookie with {@code name} to the {@link #cookies} List
	 * @param name the name of the cookie.
	 * @param val the value of the cookie with {@code name}
	 * @since 5.1.0
	 */
	public void addCookie(String name,String val) {
    HttpCookie cookie = new HttpCookie(name,val);
    addCookie(cookie);
  }
  
	/**
   * Adds the cookie with {@code name} to the {@link #cookies} List
   * @param cookie the cookie to store
   * @since 5.1.0
   */
  public void addCookie(HttpCookie cookie) {
    this.cookies.add(cookie);
  }
  
  /**
   * Adds the HTTP header name and value to the {@link #httpHeaders} {@link JSONObject} 
   * @param name the header name
   * @param value the header value
   * @since 8.5.0
   */
  public void addHttpHeader(String name, String value) {  	
  	this.httpHeaders.put(name, value);
  }
  
	/**
	 * Stores the request-level (global) parameters in the {@link YADAQuery} object for easier downstream utilization. 
	 * This method is distinct from {@link #addYADAQueryParams(List)} in that it will not replace a parameter value
	 * already associated to the query even if it's overrideable
	 * @param params list of parameter objects
	 * @param index the position of the query in the request when using {@link JSONParams}, otherwise {@code 0}
	 * @since 6.1.0
	 */
	public void addRequestParams(List<YADAParam> params, int index) {
		for(YADAParam param : params)
		{
			String key = param.getName();
			if(key.equals(YADARequest.PS_HARMONYMAP) || key.equals(YADARequest.PL_HARMONYMAP))
			{
			  JSONArray  hMap = new JSONArray(param.getValue());
			  int idx = hMap.length() > 1 ? index : 0;
			  param.setValue(hMap.getJSONObject(idx).toString());
			}
			if(!hasParam(key) || hasOverridableParam(key)) //TODO do we need this check, maybe this is bad?  Maybe we always want to replace?
			{
				addParam(param);
			}
		}
	}
	
	/**
	 * The zero-index-only implementation of this method, for requests with "standard params" (i.e., not {@link JSONParams}
	 * @param params list of parameter objects
	 */
	public void addRequestParams(List<YADAParam> params) {
    addRequestParams(params,0);
  }
	

	/**
	 * Stores the query-level parameters in the {@link YADAQuery} object for easier downstream utilization.  Care is taken 
	 * to ensure non-overrideable default parameters are not mutated, nor duplicate parameters are added. 
	 * @param params list of parameter objects
	 */
	public void addYADAQueryParams(List<YADAParam> params)
	{
		for(YADAParam param : params)
		{
			addParam(param);
		}
	}
	
	/**
	 * Replaces the value of {@link YADAParam} with name {@code key} with {@code value}, but only if the existing
	 * {@link YADAParam} is overridable.  If the param doesn't exist, it is added. If the param is not
	 * overrideable, this method exits silently.
	 * @param key name of parameter for which to replace value
	 * @param value new value for parameter
	 * @see #replaceParam(YADAParam)
	 */
	public void replaceParam(String key, String value) 
	{
		YADAParam param = new YADAParam(key, value, YADAParam.QUERY, YADAParam.OVERRIDEABLE);
		if(!hasParam(key))
			replaceParam(param);
		else
			addParam(param);
	}
	
	/**
	 * Replaces the value of {@link YADAParam} with values encapsulated by {@code param}, but only if the existing
	 * {@link YADAParam} is overridable. If the param doesn't exist, it is added.  If the param is not overrideable, 
	 * this method exits silently.
	 * @param param parameter object containing replacement values
	 */
	public void replaceParam(YADAParam param) {
		if(hasOverridableParam(param.getName()))
		{
			Iterator<YADAParam> iter = getYADAQueryParams().iterator();
			while(iter.hasNext()) 
			{
				YADAParam storedParam = iter.next();
				if(storedParam.getName().equals(param.getName()))
				{
					storedParam.setValue(param.getValue());
					storedParam.setTarget(param.getTarget());
					storedParam.setRule(param.getRule());
				}
			}
		}
		else if(!hasParam(param.getName()))
		{
			addParam(param);
		}
	}
	
	/**
	 * @param key parameter name to check
	 * @return {@code true} if the query contains the param with name equal to {@code key}, otherwise false
	 */
	public boolean hasParam(String key) {
		return this.keys.containsKey(key);
	}
	
	/**
	 * @param key parameter name to check
	 * @return {@code true} if the parameter has at least one value
	 */
	public boolean hasParamValue(String key) {
		if(getYADAQueryParamValue(key) != null 
				&& getYADAQueryParamValue(key).length > 0)
			return true;
		return false;
	}
	
	/**
   * Returns {@code true} if {@link #cookies} contains a cookie name, otherwise {@code false}
   * @return {@code true} if {@link #cookies} contains a cookie name, otherwise {@code false}
   * @since 5.1.0
   */
  public boolean hasCookies() {
    if(null == this.getCookies() || this.getCookies().size() == 0)
    {
      return false;
    }
    return true;
  }
	
	/**
	 * @since 4.0.0
	 * @param key parameter name to check
	 * @return boolean true if stored parameter is overridable by url params (rule=1), otherwise false
	 */
	public boolean hasOverridableParam(String key) {
		return !hasNonOverridableParam(key) && this.keys.containsKey(key);
	}
	
	/**
	 * @since 4.0.0
	 * @param key paremeter name to check
	 * @return boolean true if stored parameter is not overridable by url params (rule=0), otherwise false
	 */
	public boolean hasNonOverridableParam(String key) {
		return this.immutableKeys.containsKey(key);
	}
		
	/**
	 * Standard mutator for variable
	 * @param data the list of data maps
	 */
	public void setData(List<LinkedHashMap<String,String[]>> data) 
	{
		this.data = data;
	}
	
	/**
	 * Calls {@link #setData(List)}, replacing any existing data in the query object.
	 * @param allData the data to add to the query
	 */
	public void addAllData(List<LinkedHashMap<String,String[]>> allData)
	{
		setData(allData);
	}
	
	/**
	 * Adds a single row of data to the existing list.
	 * @param dataToAdd the map of data to add to the list
	 */
	public void addData(LinkedHashMap<String,String[]> dataToAdd) 
	{
		getData().add(dataToAdd);
	}
	
	/**
	 * Standard mutator for variable
	 * @param conn the JDBC connection to use to execute the query
	 */
	public void setConnection(Connection conn) { this.connection = conn; }
	/**
	 * Standard mutator for variable
	 * @param conn the SOAP connection to use to execute the query
	 */
	public void setSOAPConnection(SOAPConnection conn) { this.soapConnection = conn; }
	/**
	 * Standard mutatar for variable.
   * {@code type} should equal one of the following:
   * <ul>
   * 	<li>{@link Parser#CALL}</li>
   *  <li>{@link Parser#SELECT}</li>
   *  <li>{@link Parser#UPDATE}</li>
   *  <li>{@link Parser#INSERT}</li>
   *  <li>{@link Parser#DELETE}</li>
   *  <li>{@link Parser#SOAP}</li>
   *  <li>{@link Parser#REST}</li>
   *  <li>{@link QueryUtils#READ}</li>
   *  <li>{@link QueryUtils#WRITE}</li>
   *  <li>{@link QueryUtils#APPEND}</li>
   * </ul>
	 * @param type the query type
	 * @see QueryManager
	 * @see FileSystemAdaptor
	 */
	public void setType(String type) { this.type = type; }
	
	/**
	 * @throws YADAConnectionException when the connection can't be opened
	 * @since 4.0.0
	 */
	public void setConnection() throws YADAConnectionException 
	{
		this.setConnection(this.getApp());
	}
	
	/**
	 * Set a transactional connection for the source
	 * 
	 * @since 4.0.0
	 * @param app the app name stored in the query
	 * @throws YADAConnectionException when the connection can't be opened
	 */
	public void setConnection(String app) throws YADAConnectionException 
	{
		if(this.getProtocol().equals(Parser.SOAP))
		{
			this.setSOAPConnection(ConnectionFactory.getConnectionFactory().getSOAPConnection());
		}
		else
		{
			Connection c = ConnectionFactory.getConnectionFactory().getConnection(app);
			this.setConnection(c);
			try
			{
				if(!c.getAutoCommit())
				{
					try
					{
						this.setSavepoint(c.setSavepoint());
					}
					catch(SQLException e)
					{
						String msg = "This JDBC driver does not support savepoints.";
						l.warn(msg);
					}
				} 
			}
			catch (SQLException e)
			{
				String msg = "Unable to configure connection for transaction.";
				throw new YADAConnectionException(msg,e);
			}
		}
	}
	
	/**
	 * Set a transactional or non-transactional connection for the source
	 * @since 4.0.0
	 * @deprecated since 8.6.1
	 * @param app the app name stored in the query
	 * @param transactions set to {@code true} to execute multiple queries as a single transaction.
	 * @throws YADAConnectionException when the connection can't be opened
	 */
	@Deprecated
	public void setConnection(String app, boolean transactions) throws YADAConnectionException 
	{
		if(this.getProtocol().equals(Parser.SOAP))
		{
			this.setSOAPConnection(ConnectionFactory.getConnectionFactory().getSOAPConnection());
		}
		else
		{
			this.setConnection(ConnectionFactory.getConnectionFactory().getConnection(app));
			if(transactions)
			{
				try
				{
				  Connection c = (Connection)this.getConnection();
					c.setAutoCommit(false);
					try
					{
						this.setSavepoint(c.setSavepoint());
					}
					catch(SQLException e)
					{
						String msg = "This JDBC driver does not support savepoints.";
						l.warn(msg);
					}
				} 
				catch (SQLException e)
				{
					String msg = "Unable to configure connection for transaction.";
					throw new YADAConnectionException(msg,e);
				}
			}
		}
	}
	
	/**
	 * Closes {@link CallableStatement}s {@link PreparedStatement}s and Connections
	 * in order to avoid connection and memory leaks in {@link com.novartis.opensource.yada.plugin.Preprocess} scenarios
	 * @throws YADAConnectionException when a resource can't be closed
	 * @since 8.6.1
	 */
	public void clearResources() throws YADAConnectionException
	{
		this.clearCsmts();
		this.clearPsmts();
		this.clearConnection();
	}
	
	/**
	 * Checks for the any {@link CallableStatement}s used by the queries and renders it null.  This is 
	 * to facilitate long term storage of the query in the cache
	 * @throws YADAConnectionException when a resource can't be closed
	 * @since 8.6.1
	 */
	public void clearCsmts() throws YADAConnectionException 
	{
		if(this.getCstmt().size() > 0)
		{
			for(int i = 0; i < this.getCstmt().size(); i++)
			{
				ConnectionFactory.releaseResources(this.getCstmt(i));
			}
			this.setCstmt(new ArrayList<CallableStatement>());
		}
	}
	
	/**
	 * Checks for the any {@link PreparedStatement}s used by the queries and renders it null.  This is 
	 * to facilitate long term storage of the query in the cache
	 * @throws YADAConnectionException when a resource can't be closed
	 * @since 8.6.1
	 */
	public void clearPsmts() throws YADAConnectionException 
	{
		if(this.getPstmtForCount().size() > 0)
		{
			for(PreparedStatement pstmtKey : this.getPstmtForCount().keySet())
			{
				ConnectionFactory.releaseResources(this.getPstmtForCount(pstmtKey));
			}
			this.setPstmtForCount(new HashMap<PreparedStatement,PreparedStatement>());
		}
		
		if(this.getPstmt().size() > 0)
		{
			for(int i = 0; i < this.getPstmt().size(); i++)
			{
				ConnectionFactory.releaseResources(this.getPstmt(i));
			}
			this.setPstmt(new ArrayList<PreparedStatement>());
			
		}
	}
	
	/**
	 * Checks for the type of Connection used by the queries, closes and nullifies them.  This is 
	 * to avoid connection leaks in {@link com.novartis.opensource.yada.plugin.Preprocess} plugin scenarios, as well as 
	 * facilitate long term storage of the query in the cache.
	 * @throws YADAConnectionException when a resource can't be closed
	 * @since 4.1.0
	 */
	public void clearConnection() throws YADAConnectionException 
	{
		if(this.connection != null)
		{
			ConnectionFactory.releaseResources(this.connection);
			this.connection = null;
		}
		else if(this.soapConnection != null)
			this.soapConnection = null;
		else
			this.url.clear();
	}
	
	/**
	 * Sets {@code this.savepoint} to {@code null}.
	 * @since 4.1.0
	 */
	public void clearSavepoint()
	{
		this.savepoint = null;
	}
	
	/**
	 * Standard mutator for variable.  Called when transactional processing is required.
	 * @param savepoint the transactional rollback point.
	 * @see #setConnection(String, boolean)
	 */
	public void setSavepoint(Savepoint savepoint) {
		this.savepoint = savepoint;
	}
	/**
	 * Standard accessor for variable.  Set while setting connection.
	 * @return the savepoint set for this query
	 * @see #setConnection(String, boolean)
	 */
	public Savepoint getSavepoint() { return this.savepoint; }
	/**
	 * Standard accessor for variable.
	 * @return the {@code qname} for this query
	 */
	public String   getQname() { return this.qname; }
	/**
	 * Standard accessor for variable.
	 * @return the JNDI source associated to the query
	 */
	public String   getSource() { return this.source; }
	/**
	 * Standard accessor for variable.
	 * @return the YADA version to which the query is associated, usually an empty string.
	 */
	public String   getVersion() { return this.version; }
	/**
	 * Standard accessor for variable.
	 * @return a {@link String} containing the query code with YADA markup: {@code v}, {@code d}, {@code i}, and {@code n} 
	 */
	public String   getYADACode() { return this.coreCode; }
	/**
	 * Standard accessor for variable
	 * @return the 
	 */
	public String   getType() { return this.type; }
	/**
	 * Standard accessor for variable
	 * @return the protocol: {@link Parser#SOAP}, {@link Parser#REST}, or {@link Parser#JDBC}
	 */
	public String   getProtocol() { return this.protocol; }
	/**
	 * @return the {@link YADAQueryResult} object associated to this {@link YADAQuery}
	 */
	public YADAQueryResult getResult() { return this.yadaQueryResult; }
	/**
	 * Standard accessor for variable
	 * @return the list of {@link HttpCookie} objects stored in the query
	 * @since 5.1.0
	 */
	public List<HttpCookie> getCookies() { return this.cookies; }
	/**
	 * Standard accessor for variable
	 * @return the list of {@code name/value} pair arrays stored in the query
	 * @since 8.5.0
	 */
	public JSONObject getHttpHeaders() { return this.httpHeaders; }
	
	/**
	 * @return the appropriate connection object, based on {@link #getProtocol()}
	 */
	public Object         getConnection() { 
		if(this.getProtocol().equals(Parser.SOAP))
			return this.soapConnection;
		return this.connection; 
	}
	
	/**
	 * Standard accessor for variable
	 * @return the {@link java.lang.Class} of the jDBCAdaptor
	 */
	public Class<Adaptor>          getAdaptorClass() { return this.adaptorClass; }
	
	/**
	 * @param row the list index
	 * @return the count of parameters corresponding to index {@code row} in the internal list
	 */
	public int                     getParamCount(int row) 
	{ 
		return getParamCount().size() == 0 ? 0 : getParamCount().get(row).intValue(); 
	}
	/**
	 * Standard accessor for variable
	 * @return the jdbc parameter count
	 */
	public List<Integer>           getParamCount() { return this.paramCount; }
	
	/**
	 * @param row the list index
	 * @return the list of data values at index {@code row} in the internal list
	 */
	public List<String>            getVals(int row) { return getVals().get(row); }
	/**
	 * Standard accessor for variable
	 * @return the list of {@link String} objects for values
	 */
	public List<List<String>>      getVals() { return this.vals; }
	
	/**
	 * @param row the list index
	 * @return the {@link java.sql.PreparedStatement} at index {@code row} in the internal list
	 */
	public PreparedStatement       getPstmt(int row) { return getPstmt().get(row); }
	/**
	 * Standard accessor for variable
	 * @return the list of {@link java.sql.PreparedStatement} objects
	 */
	public List<PreparedStatement> getPstmt() { return this.pstmt; }
	
	/**
	 * @param row the list index
	 * @return the {@link java.sql.CallableStatement} at index {@code row} in the internal list
	 */
	public CallableStatement       getCstmt(int row) { return getCstmt().get(row); }
	/**
	 * Standard accessor for variable
	 * @return the list of {@link java.sql.CallableStatement} objects
	 */
	public List<CallableStatement> getCstmt() { return this.cstmt; }
	
	/**
	 * @param row the list index
	 * @return the url, as a {@link String}, from index {@code row} in the internal list
	 */
	public String                  getUrl(int row) { return getUrl().get(row); }
	
	/**
	 * Standard accessor for variable
	 * @return the list of {@link java.net.URL} objects
	 */
	public List<String>            getUrl() { return this.url; }
	
	/**
	 * @param row the list index
	 * @return the soap query, as a {@link String}, from index {@code row} in the internal list
	 */
	public String                  getSoap(int row) { return getSoap().get(row); }
	/**
	 * Standard accessor for variable
	 * @return the list of soap queries
	 */
	public List<String>            getSoap() { return this.soap; }
	
	/**
	 * Standard accessor for variable
	 * @return the list of {@link java.sql.PreparedStatement}s for count queries
	 */
	public Map<PreparedStatement,PreparedStatement> getPstmtForCount() { return this.pstmtForCount; }
	/**
	 * @param pstmtKey the data statement object
	 * @return the {@link java.sql.PreparedStatement} associated to {@code pstmtKey} in the hash
	 */
	public PreparedStatement                        getPstmtForCount(PreparedStatement pstmtKey) { return getPstmtForCount().get(pstmtKey); }
	
	/**
	 * @param row the list index
	 * @return the array of data types at index {@code row} in the internal list
	 */
	public char[] 				   			 getDataTypes(int row) { return getDataTypes().get(row); }
	/**
	 * Standard accessor for variable
	 * @return the list of data types
	 */
	public List<char[]>            getDataTypes() { return this.dataTypes; }
		
	/**
	 * Standard accessor for variable
	 * @return the list of parameter objects
	 */
	public List<YADAParam> getYADAQueryParams() { return this.yqParams; }
	/**
	 * Standard accessor for variable
	 * @return the list of data objects
	 */
	public List<LinkedHashMap<String,String[]>> getData() { return this.data; }
	
	/**
	 * @param row the list index
	 * @return the map of data at index {@code row}
	 */
	public LinkedHashMap<String,String[]> getDataRow(int row) {
		return this.getData().get(row);
	}
	
	/**
	 * Deletes the data map at index {@code row} from the internal list
	 * @param row the list index
	 */
	public void removeDataRow(int row) {
		this.getData().remove(row);
	}

	/**
	 * Standard mutator for variable
	 * @param adaptorClass class name of adaptor
	 */
	public void setAdaptorClass(Class<Adaptor> adaptorClass)
	{
		this.adaptorClass = adaptorClass;
	}

	/**
	 * Standard mutator for variable
	 * @param adaptor instance of {@code adaptorClass}
	 */
	public void setAdaptor(Adaptor adaptor) {
		this.adaptor = adaptor;
	}
	
	/**
	 * Standard accessar for variable
	 * @return the source jDBCAdaptor object
	 */
	public Adaptor getAdaptor() {
		return this.adaptor;
	}
	
	/**
	 * Standard accessor for variable
	 * @return the inExpressionMap
	 * @since 7.1.0
	 */
	public Map<Column,InExpression> getInExpressionMap() {
	  return this.inExpressionMap;
	}

	/**
   * @return the globalHarmonyMap
   * @since 6.1.0
   */
  public JSONObject getGlobalHarmonyMap() {
    return this.globalHarmonyMap;
  }

  /**
   * @param globalHarmonyMap the globalHarmonyMap to set
   * @since 6.1.0
   */
  public void setGlobalHarmonyMap(JSONObject globalHarmonyMap) {
    this.globalHarmonyMap = globalHarmonyMap;
  }
  
  /**
   * Adds an entry with a unique key to the {@link #globalHarmonyMap}
   * @param key the original column name
   * @param value the mapped, replacement column name
   * @since 6.1.0
   */
  public void addHarmonyMapEntry(String key, String value) {
    try
    {
      if(getGlobalHarmonyMap() == null)
        setGlobalHarmonyMap(new JSONObject());
      getGlobalHarmonyMap().putOnce(key, value);
    }
    catch(JSONException e)
    {
      String msg = "Key ["+key+"] already exists in global harmony map.";
      l.warn(msg);
    }
  }

  /**
	 * Standard accessor for variable.
	 * @return the conformedCode
	 */
	public String getConformedCode()
	{
		return this.conformedCode;
	}

	/**
	 * Standard mutator for variable. Store the stripped code in its object. 
	 * @param conformedCode the conformedCode to set
	 */
	public void setConformedCode(String conformedCode)
	{
		this.conformedCode = conformedCode;
	}

	/**
	 * Returns the cached status
	 * @return the isCached
	 * @since 4.1.0
	 */
	public boolean isCached()
	{
		return this.isCached;
	}
	
	/**
	 * Interrogates the {@link #properties} {@link Set} to look for security properties
	 * @return {@code true} if there is a {@link YADAProperty} with a {@code name = "protected"}
	 * @since 7.1.0
	 */
	public boolean isProtected()
	{
	  if(this.getProperties().size() > 0)
	  {
	    for(YADAProperty prop : this.getProperties())
	    {
	      if(prop.getName().equals(PROP_PROTECTED) && Boolean.parseBoolean(prop.getValue()))
	        return true;
	    }
	  }
	  return false;
	}

	/**
	 * Sets the cached status
	 * @param isCached the isCached to set
	 * @since 4.1.0
	 */
	public void setCached(boolean isCached)
	{
		this.isCached = isCached;
	}

  /**
   * The object representation of the SQL query.
   * @return the statement
   * @since 7.0.0
   */
  public Statement getStatement() {
    return this.statement;
  }

  /**
   * The object representation of the SQL query.
   * @param statement the statement to set
   * @since 7.0.0
   */
  public void setStatement(Statement statement) {
    this.statement = statement;
  }
  
  /**
   * Standard accessor for variable
   * @return the list of columns linked to JDBC parameters
   */
  public String[] getParameterizedColumns() { return this.parameterizedColumns; }
  /**
   * Standard accessor for variable
   * @return the list of columns referenced by {@code in} clauses
   */
  public String[] getIns() { return this.ins; }
  /**
   * Standard accessor for variable
   * @return the list of columns referenced by the SQL query
   * @deprecated as of 7.1.0
   */
  @Deprecated
  public String[] getColumns() { return this.columns; }
  /**
   * Standard mutator for variable
   * @param parameterizedColumns list of columns with JDBC parameters
   */
  public void setParameterizedColumns(String[] parameterizedColumns) { this.parameterizedColumns = parameterizedColumns; }
  /**
   * Standard mutator for variable
   * @param ins list of columns in {@code IN} clauses
   */
  public void setIns(String[] ins) { this.ins = ins; }
  /**
   * Standard mutator for variable
   * @param columns list of columns in the SQL query
   * @deprecated as of 7.1.0
   */
  @Deprecated
  public void setColumns(String[] columns) { this.columns = columns; }
  
  /**
   * Standard accessor for variable
   * @return the list of columns linked to JDBC parameters
   */
  public List<Column> getParameterizedColumnList() { return this.parameterizedColumnList; }
  /**
   * Standard accessor for variable
   * @return the list of columns referenced by {@code in} clauses
   */
  public List<Column> getInList() { return this.inList; }
  /**
   * Standard accessor for variable
   * @return the list of columns referenced by the SQL query
   */
  public List<Column> getColumnList() { return this.columnList; }

  /**
   * Standard mutator for variable
   * @param columnList list of columns in the SQL query
   */
  public void setColumnList(List<Column> columnList) {
    this.columnList = columnList;
  }
  
  /**
   * Standard mutator for variable
   * @param inList list of columns in {@code IN} clauses
   */
  public void setInList(List<Column> inList) {
    this.inList = inList;
  }
  
  /**
   * Standard mutator for variable
   * @param parameterizedColumnList list of columns with JDBC parameters
   */
  public void setParameterizedColumnList(List<Column> parameterizedColumnList) {
    this.parameterizedColumnList = parameterizedColumnList;
  }

  /**
   * Standard accessor for variable
   * @param inExpressionMap column to expression map for processing IN clauses
   */
  public void setInExpressionMap(Map<Column, InExpression> inExpressionMap) {
    this.inExpressionMap = inExpressionMap;
  }

  /**
   * Standard accessor for variable
   * @return the app
   * @since 7.1.0
   */
  public String getApp() {
    return this.app;
  }

  /**
   * Standard mutator for variable
   * @param app the app to set
   * @since 7.1.0
   */
  public void setApp(String app) {
    this.app = app;
  }

  /**
   * Standard accessor for variable
   * @return the properties
   * @since 7.1.0
   */
  public Set<YADAProperty> getProperties() {
    return this.properties;
  }

  /**
   * Standard mutator for variable
   * @param properties the properties to set
   * @since 7.1.0
   */
  public void setProperties(Set<YADAProperty> properties) {
    this.properties = properties;
  }

  /**
   * Standard accessor for var
   * @return the accessCount
   * @since 8.1.1
   */
  public int getAccessCount() {
    return this.accessCount;
  }

  /**
   * Standard mutator for var
   * @param accessCount the accessCount to set
   * @since 8.1.1
   */
  public void setAccessCount(int accessCount) {
    this.accessCount = accessCount;
  }
  
  /**
   * @since 9.0.0
   */
  @Override
  public String toString() {
  	JSONObject jo = new JSONObject();
  	jo.append("qname", getQname());
  	JSONArray data = new JSONArray();
  	for (int i=0;i<getData().size();i++)
  	{
  		JSONObject djo = new JSONObject();
  		LinkedHashMap<String,String[]> row = getDataRow(i);
  		for(Map.Entry<String,String[]> entry : row.entrySet())
  		{
  			String key = entry.getKey();
  			String[] value = entry.getValue();
  			djo.put(key, new JSONArray(value));
  		}
  	}
  	jo.append("DATA", data);
  	return jo.toString();
  }
}
