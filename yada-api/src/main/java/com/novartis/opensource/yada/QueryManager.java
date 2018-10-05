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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.xml.soap.SOAPConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.adaptor.FileSystemAdaptor;
import com.novartis.opensource.yada.adaptor.JDBCAdaptor;
import com.novartis.opensource.yada.adaptor.RESTAdaptor;
import com.novartis.opensource.yada.adaptor.SOAPAdaptor;
import com.novartis.opensource.yada.adaptor.YADAAdaptorException;
import com.novartis.opensource.yada.util.QueryUtils;
import com.novartis.opensource.yada.util.YADAUtils;

/**
 * QueryManager is a workhorse class that performs essential query preparation
 * tasks prior to execution. These tasks include retrieving {@link YADAQuery}
 * objects using {@link Finder#getQuery(String)}, and creating global and
 * query-based data structures for storing and mapping source connections,
 * statements, and results.
 * 
 * @since 4.0.0
 * @author David Varon
 * @see Service#execute()
 * @see Service#handleRequest(javax.servlet.http.HttpServletRequest)
 */
public class QueryManager
{
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(QueryManager.class);
	/**
	 * Local handle to configuration object
	 */
	private YADARequest yadaReq;
	/**
	 * Local handle to configuration object
	 */
	private JSONParams jsonParams;
	/**
	 * Index of queries passed in request
	 */
	private YADAQuery[] queries;
	/**
	 * Utility object
	 */
	private QueryUtils qutils = new QueryUtils();
	/**
	 * Map of sources (JNDI strings) to JDBC or SOAP connections
	 */
	private HashMap<String,Object> connectionMap = new HashMap<>();
	/**
	 * Map of SOAP query names to url strings
	 */
	private HashMap<String,String> soapMap = new HashMap<>();
	/**
	 * Map of REST query names to url strings
	 */
	private HashMap<String,String> urlMap = new HashMap<>();
	/**
	 * List of sources for which to defer commit execution, usu due to unsupported {@link ResultSet#HOLD_CURSORS_OVER_COMMIT}
	 */
	private List<String> deferredCommits = new ArrayList<>();
	/**
	 * Set of sources for which to commit execution is required&mdash;effectively any JDBC source in a {@link YADAQuery} with
	 *  an {@code INSERT}, {@code UPDATE}, or {@code DELETE} statement 
	 */
	private Set<String> requiredCommits = new HashSet<>();
	/**
	 * Constant equal to: {@value}
	 */
	@SuppressWarnings("unused")
	private final static String UNDEFINED = "undefined";
	/**
	 * Constant equal to: {@value}
	 */
	@SuppressWarnings("unused")
	private final static String NULLSTRING = "null";

	/**
	 * Constructor stores config object and JSONParams if necessary, then calls
	 * effectively bootstraps query management process by calling
	 * {@link #processQueries()}
	 * 
	 * @since 4.0.0
	 * @param yadaReq
	 *          YADA request configuration
	 * @throws YADAUnsupportedAdaptorException when the adaptor can't be instantiated, or when it can't be found
	 * @throws YADAFinderException when there is an issue retrieving a query from the YADA index
	 * @throws YADAConnectionException when there is an issue opening a connection to a source
	 *           referenced by a query
	 * @throws YADAResourceException when a query's source attribute can't be found in the application
	 *           context, or there is another problem with the context
	 * @throws YADAQueryConfigurationException if request does not contain either a {@code qname} or {@code q},
	 *           or a {@code JSONParams} or {@code j} parameter
	 * @throws YADAAdaptorException when a query cannot be built by the adaptor
	 * @throws YADARequestException when filters are included in the request config, but can't be
	 *           converted into a JSONObject
	 * @throws YADAParserException when a query code cannot be parsed successfully
	 */
	public QueryManager(YADARequest yadaReq) throws YADAQueryConfigurationException, YADAResourceException, YADAConnectionException, YADAFinderException, YADAUnsupportedAdaptorException, YADARequestException, YADAAdaptorException, YADAParserException
	{
	  processRequest(yadaReq);
	}
	
	
	/**
	 * Default constructor
	 * @since 7.1.0
	 */
	public QueryManager() {
	  
	}

	/**
   * A broker method which calls {@link #endowQuery(String)} (for standard param requests)
   * or {@link #endowQueries(JSONParams)} for json params requests, followed by setting
   * harmony maps, and 
   * {@link #prepQueriesForExecution()} in succession
   * 
   * @param yadaReq the {@link YADARequest} to process
   * @throws YADAFinderException when there is an issue retrieving a query from the YADA index
   * @throws YADAConnectionException when there is an issue opening a connection to a source
   *           referenced by a query
   * @throws YADAQueryConfigurationException if request does not contain either a {@code qname} or {@code q},
   *           or a {@code JSONParams} or {@code j} parameter
   * @throws YADAUnsupportedAdaptorException when the adaptor can't be instantiated, or when it can't be found
   * @throws YADAResourceException when a query's source attribute can't be found in the application
   *           context, or there is another problem with the context
   * @throws YADAAdaptorException when a query cannot be built by the adaptor
   * @throws YADARequestException when filters are included in the request config, but can't be
   *           converted into a JSONObject
   * @throws YADAParserException when query code cannot be parsed successfully
   * @since 7.1.0
   */
	private void processRequest(YADARequest yadaReq) throws YADAQueryConfigurationException, YADAConnectionException, YADAFinderException, YADAResourceException, YADAUnsupportedAdaptorException, YADARequestException, YADAAdaptorException, YADAParserException
	{
	  setYADAReq(yadaReq);
	  if (YADAUtils.hasJSONParams(getYADAReq()))
    {
      setJsonParams(yadaReq.getJsonParams());
      setQueries(endowQueries(getJsonParams()));
    }
	  else if (YADAUtils.hasQname(getYADAReq()))
    {
      setQueries(new YADAQuery[] {endowQuery(getYADAReq().getQname())});
    } 
    else
    {
      String msg = "Your request must contain a 'qname', 'q', 'JSONParams', or 'j' parameter.";
      throw new YADARequestException(msg);
    }
    setGlobalHarmonyMaps();
    setQueryHarmonyMaps();
    prepQueriesForExecution();
	}
	
	/**
	 * A broker method which calls {@link #endowQuery(String)} (for standard param requests)
	 * or {@link #endowQueries(JSONParams)} for json params requests, followed by
	 * {@link #prepQueriesForExecution()} in succession
	 * 
	 * @throws YADAFinderException when there is an issue retrieving a query from the YADA index
	 * @throws YADAConnectionException when there is an issue opening a connection to a source
	 *           referenced by a query
	 * @throws YADAQueryConfigurationException if request does not contain either a {@code qname} or {@code q},
	 *           or a {@code JSONParams} or {@code j} parameter
	 * @throws YADAUnsupportedAdaptorException when the adaptor can't be instantiated, or when it can't be found
	 * @throws YADAResourceException when a query's source attribute can't be found in the application
	 *           context, or there is another problem with the context
	 * @throws YADAAdaptorException when a query cannot be built by the adaptor
	 * @throws YADARequestException when filters are included in the request config, but can't be
	 *           converted into a JSONObject
	 * @throws YADAParserException when query code cannot be parsed successfully
	 * @since 4.0.0
	 * @deprecated as of 7.1.0
	 */
	@SuppressWarnings("unused")
  @Deprecated
	private void processQueries() throws YADAQueryConfigurationException, YADAConnectionException, YADAFinderException, YADAResourceException, YADAUnsupportedAdaptorException, YADARequestException, YADAAdaptorException, YADAParserException
	{
		if (YADAUtils.hasJSONParams(this.yadaReq)) //TODO this is a redundant call, see the constructor
		{
			setQueries(endowQueries(getJsonParams()));
		} 
		else if (YADAUtils.hasQname(this.yadaReq))
		{
			setQueries(new YADAQuery[] {endowQuery(this.yadaReq.getQname())});
		} 
		else
		{
			String msg = "Your request must contain a 'qname', 'q', 'JSONParams', or 'j' parameter.";
			throw new YADARequestException(msg);
		}
		setGlobalHarmonyMaps();
		setQueryHarmonyMaps();
		prepQueriesForExecution();
	}
	
	/**
	 * Create a composite harmony map for all queries based on either {@link YADARequest#getHarmonyMap()} 
	 * or the harmony maps stored in each query via param {@link YADAQuery#getParam(String)} 
	 * named {@link YADARequest#PS_HARMONYMAP}.  This object, consistent in each request, enables inclusion
	 * of both mapped and unmapped columns in a single result, specifically necessary for 
	 * {@link YADARequest#FORMAT_CSV} and other delimited formats.
	 * @since 6.1.0
	 */
	private void setGlobalHarmonyMaps() 
	{
	  JSONArray  reqHm            = this.yadaReq.getHarmonyMap();
	  JSONObject globalHarmonyMap = new JSONObject();
	  if(null == reqHm)
	  {
	    for(YADAQuery yq : getQueries())
	    {
//	      YADAParam p = yq.getParam(YADARequest.PS_HARMONYMAP).get(0);
//	      if(p != null)
//	      {
	      if(yq.hasParam(YADARequest.PS_HARMONYMAP) && yq.getParam(YADARequest.PS_HARMONYMAP).size() > 0)
        {
          YADAParam p = yq.getParam(YADARequest.PS_HARMONYMAP).get(0);
          JSONObject j = new JSONObject(p.getValue());
	        if(j.length() > 0)
	        {
	          globalHarmonyMap = populateGlobalHarmonyMap(globalHarmonyMap, j);
	        }
	      }
	    }
	    for(YADAQuery yq : getQueries())
	      yq.setGlobalHarmonyMap(globalHarmonyMap);
	  }
	  else
	  {
	    for(int i=0;i<reqHm.length();i++)
	    {
        JSONObject j = reqHm.getJSONObject(i);
        if(j.length() > 0)
        {
          globalHarmonyMap = populateGlobalHarmonyMap(globalHarmonyMap, j);
	      }
      }
	    for(YADAQuery yq : getQueries())
	      yq.setGlobalHarmonyMap(globalHarmonyMap);
	  }  
	}
	
	/**
	 * Populates a {@link JSONObject} with the unique set of keys, i.e., original column or field names 
	 * passed into the request in {@link JSONParams} or in the {@link YADARequest#PS_HARMONYMAP} parameter.
	 * 
	 * @param global The {@link JSONObject} to populate
	 * @param local The {@link JSONObject} containing the key/value pairs to transfer to {@code global}
	 * @return the {@code global} {@link JSONObject} containing the unique set of keys (and values)
	 * @since 6.1.0
	 */
	private JSONObject populateGlobalHarmonyMap(JSONObject global, JSONObject local)
	{
	  for(String key : JSONObject.getNames(local))
    {
      try
      {
        global.putOnce(key, local.get(key));
      }
      catch(JSONException e)
      {
        String msg = "Key ["+key+"] already exists in global harmony map.";
        l.warn(msg);
      }
    }
	  return global;
	}
	
	/**
	 * Checks for a {@code harmonyMap} or {@code h} spec in the {@link #yadaReq}. If {@code null}, 
	 * iterates over the queries in the request, identifying those that have embedded {@code h} specs
	 * and those that do not. If any queries contain specs, those that do not are provided with empty maps.
	 * 
	 * @throws YADARequestException when the resulting harmonyMap is non-compliant
	 * @since 6.1.0
	 */
	private void setQueryHarmonyMaps() throws YADARequestException 
	{
  	if(this.yadaReq.getHarmonyMap() == null)
  	{
  	  ArrayList<JSONObject> hasMap = new ArrayList<>();
  	  ArrayList<YADAQuery> noMap  = new ArrayList<>();
  	  for(YADAQuery yq : this.queries)
  	  {
  	    if(yq.hasParam(YADARequest.PS_HARMONYMAP) && yq.getParam(YADARequest.PS_HARMONYMAP).size() > 0)
  	    {
  	      YADAParam p = yq.getParam(YADARequest.PS_HARMONYMAP).get(0);
    	    hasMap.add(new JSONObject(p.getValue()));
  	    }
  	    else
  	    {
  	      noMap.add(yq);
  	    }
  	  }
  	  if(hasMap.size() > 0)
  	  {
  	    for(YADAQuery yq : noMap)
  	    {
  	      YADAParam param = new YADAParam(YADARequest.PS_HARMONYMAP,"{}",YADAParam.QUERY,YADAParam.OVERRIDEABLE);
  	      yq.addParam(param);
  	    }
  	  }
  	}
	}

	/**
	 * Adds the JDBC or SOAP connection object for {@code yq} to the internal
	 * index. If the connection is not yet in the index, it is created from the
	 * source stored in the {@code yq} object.  If the protocol value in the query
	 * is not SOAP or JDBC, the method exits silently
	 * 
	 * @param yq the query object containing the source string for setting the
	 *          connection
	 * @throws YADAConnectionException when the connection to the source in the query cannot be set
	 */
	private void storeConnection(YADAQuery yq) throws YADAConnectionException
	{
		String app = yq.getApp();
		String protocol = yq.getProtocol();
		try 
		{
			if (!this.connectionMap.containsKey(app)
				  || (protocol.equals(Parser.JDBC) && ((Connection)this.connectionMap.get(app)).isClosed()))
			{
				yq.setConnection();
				this.connectionMap.put(app, yq.getConnection());
			} 
			else
			{
				if (protocol.equals(Parser.JDBC))
					yq.setConnection((Connection)this.connectionMap.get(app));
				else if (protocol.equals(Parser.SOAP))
					yq.setSOAPConnection((SOAPConnection)this.connectionMap.get(app));
			}
		} 
		catch (SQLException e) 
		{
			String msg = "Unable to close connection";
			throw new YADAConnectionException(msg, e);
		}
	}

	/**
	 * Executes a query-level commit on the connection stored in the YADAQuery
	 * referenced by the parameter. If the connection object returned by the query
	 * is not a JDBC connection, but, for instance, a SOAPConnection, the error
	 * will be caught and handled gracefully.
	 * 
	 * @param yq
	 *          the query containing the statements to commit
	 * @throws YADAConnectionException when the commit fails
	 */
	public void commit(YADAQuery yq) throws YADAConnectionException
	{
		try
		{
			if(this.requiredCommits.contains(yq.getApp()))
			{
				Connection connection = (Connection)yq.getConnection();
				if(connection.getHoldability() == ResultSet.HOLD_CURSORS_OVER_COMMIT)
				{
					connection.commit();
					int    count = yq.getResult().getTotalResultCount();
					String rows  = count == 1 ? "row" : "rows"; 
					String msg   = "\n------------------------------------------------------------\n";
					msg         += "   Commit successful on connection to ["+yq.getApp()+"] ("+count+" "+rows+")\n";
					msg         += "------------------------------------------------------------\n";
					l.debug(msg);
				}
				else
				{
					deferCommit(yq.getApp());
				}
			}
		} 
		catch (SQLException e)
		{
			String msg = "Unable to commit transaction on ["+yq.getApp()+"].";
			throw new YADAConnectionException(msg, e);
		} 
		catch (ClassCastException e)
		{
			l.info("Connection to ["+yq.getApp()+"] is not a JDBC connection (it's probably SOAP.) No commit was attempted.");
		}
	}

	/**
	 * Executes a commit on all connections created during processing of the
	 * current request.
	 * 
	 * @throws YADAConnectionException when the commit fails
	 */
	public void commit() throws YADAConnectionException
	{
		if (this.connectionMap != null && this.connectionMap.keySet().size() > 0)
		{
			//TODO			int    totalCount = 0;
			String source     = "";
			for(Iterator<String> iterator = this.requiredCommits.iterator(); iterator.hasNext();)
			{
				try
				{
					source = iterator.next();
					Connection connection = (Connection)this.connectionMap.get(source);
					if(connection.getHoldability() == ResultSet.HOLD_CURSORS_OVER_COMMIT)
					{
						connection.commit();
						String msg = "\n------------------------------------------------------------\n";
						msg += "   Commit successful on ["+source+"].\n";
						msg += "------------------------------------------------------------\n";
						l.info(msg);
					}
					else
					{
						deferCommit(source);
					}
				}
				catch (SQLException e)
				{
					String msg = "Unable to commit transaction on ["+source+"].";
					throw new YADAConnectionException(msg, e);
				} 
				catch (ClassCastException e)
				{
					l.info("Connection to ["+source+"] is not a JDBC connection (it's probably SOAP.)  No commit was attempted.");
				} 
			}
 		}
	}
	
	/**
	 * Adds {@code source} to the internal {@code #deferredCommits} list for execution of commit on the connection after
	 * results are parsed.
	 * @param app the name of the YADA app mapped to the datasource config
	 * @since 4.2.0
	 */
	private void deferCommit(String app)
	{
		this.deferredCommits.add(app);
		String msg = "Commit deferred on ["+app+"]. This is done, most likely, because the JDBC driver for this source does not support holdability.";
		l.info(msg);
	}

	/**
	 * This method attempts (and usually suceeds) to close all JDBC resources
	 * opened during processing of the current request, including
	 * {@link ResultSet}s {@link java.sql.PreparedStatement}s and
	 * {@link java.sql.CallableStatement}s using the utility methods in
	 * {@link com.novartis.opensource.yada.ConnectionFactory}
	 * 
	 * @throws YADAConnectionException when there is a problem closing any of the resources
	 * @see ConnectionFactory#releaseResources(ResultSet)
	 * @see ConnectionFactory#releaseResources(java.sql.Statement)
	 */
	public void releaseResources() throws YADAConnectionException
	{
		for (String app : this.deferredCommits)
		{
			try
			{
				Connection connection = (Connection)this.connectionMap.get(app);
				connection.commit();
				String msg = "\n------------------------------------------------------------\n";
				msg += "   Commit successful on ["+app+"].\n";
				msg += "------------------------------------------------------------\n";
				l.info(msg);
			}
			catch (SQLException e)
			{
				String msg = "\n------------------------------------------------------------\n";
				msg += "   Unable to commit transaction on ["+app+"].\n";
				msg += "------------------------------------------------------------\n";
				l.error(msg); //TODO should there be a rollback message here?
			} 
		}
		for (YADAQuery yq : this.getQueries())
		{
			YADAQueryResult yqr = yq.getResult();
			if (yqr != null && yqr.getResults() != null)
			{
				for (Object result : yqr.getResults())
				{
					if (result instanceof ResultSet)
					{
						l.debug("Closing ResultSet");
						ConnectionFactory.releaseResources((ResultSet)result);
					}
				}
			}
			if (yq.getPstmt() != null && yq.getPstmt().size() > 0)
			{
				for (PreparedStatement p : yq.getPstmt())
				{
					l.debug("Closing PreparedStatement");
					ConnectionFactory.releaseResources(p);
					l.debug("PreparedStatement removed from map.");
				}
			}
			if (yq.getPstmtForCount() != null && yq.getPstmtForCount().values()
																							.size() > 0)
			{
				for (PreparedStatement p : yq.getPstmtForCount().values())
				{
					l.debug("Closing PreparedStatement for count query");
					ConnectionFactory.releaseResources(p);
				}
			}
			if (yq.getCstmt() != null && yq.getCstmt().size() > 0)
			{
				for (CallableStatement c : yq.getCstmt())
				{
					l.debug("Closing CallableStatement");
					ConnectionFactory.releaseResources(c);
				}
			}
			if(yq.getConnection() != null)
			{
				l.debug("Closing Connection");
				ConnectionFactory.releaseResources((Connection)yq.getConnection());
			}
		}
		this.connectionMap.clear();
		l.debug("QueryManager connection map has been cleared.");
	}

	/**
	 * Adds the SQL function statement to the internal index
	 * 
	 * @param yq
	 *          the query object
	 * @param code
	 *          the raw code
	 */
	private void storeCallableStatement(YADAQuery yq, String code)
	{
			yq.addCstmt(this.qutils.getCallableStatement(	code,
																										(Connection)yq.getConnection()));
	}

	/**
	 * Adds the JDBC statement to the internal index
	 * 
	 * @param yq
	 *          the query containing the {@code code} and the index to which to
	 *          add the statement
	 * @param code
	 *          the SQL to map to the statement
	 * @throws YADAConnectionException when the statement is not yet in the map, and the connection
	 *           deliver it
	 */
	@SuppressWarnings("unused")
  private void storePreparedStatement(YADAQuery yq, String code) throws YADAConnectionException
	{
			PreparedStatement p = this.qutils.getPreparedStatement(code,(Connection)yq.getConnection());
			yq.addPstmt(p);
	}
	
	/**
   * Adds the JDBC statement to the internal index at the specified position
   * @param row the index of {@link YADAQuery}{@code .pstmt} at which to store the {@link PreparedStatement}
   * @param yq the query containing the {@code code} and the index to which to
   *          add the statement
   * @param code the SQL to map to the statement
   * @throws YADAConnectionException when the statement is not yet in the map, and the connection
   *           deliver it
   * @since 7.0.0
   */
  private void storePreparedStatement(YADAQuery yq, String code, int row) throws YADAConnectionException
  {
      PreparedStatement p = this.qutils.getPreparedStatement(code,(Connection)yq.getConnection());
      yq.addPstmt(p,row);
  }

	/**
	 * Adds the JDBC statement to the internal index
	 * 
	 * @param yq the query containing the {@code code} and the index to which to
	 *          add the statement
	 * @param p the statement for the data query, serving as a key for the count
	 *          statement in the map
	 * @param code the SQL to map to the statement
	 * @throws YADAConnectionException when the statement is not yet in the map, and the connection
	 *           deliver it
	 */
	private void storePreparedStatementForCount(YADAQuery yq,
																							PreparedStatement p, String code) throws YADAConnectionException
	{
		// TODO this won't work with CountOnly
			PreparedStatement pc = this.qutils.getPreparedStatement(code,
																															(Connection)yq.getConnection());
			yq.addPstmtForCount(p, pc);
	}

	/**
	 * Adds the SOAP message to the internal index
	 * 
	 * @param yq
	 *          the query containing the code and index
	 * @param code
	 *          the soap message
	 */
	private void storeSoapMessage(YADAQuery yq, String code)
	{
		if (this.soapMap.containsKey(code))
		{
			yq.addSoap(this.soapMap.get(code));
		} else
		{
			yq.addSoap(this.qutils.getSoap(code));
		}
	}

	/**
	 * Adds the REST url string to the internal index
	 * 
	 * @param yq
	 *          the query containing the code and index
	 * @param code
	 *          the rest url string
	 */
	private void storeRestQuery(YADAQuery yq, String code)
	{
		if (this.urlMap.containsKey(code))
		{
			yq.addUrl(this.urlMap.get(code));
		} else
		{
			yq.addUrl(code);
		}
	}
	
	/**
	 * Prepares the query for execution by retrieving the wrapped query code, amending
	 * it if needed, as prescribed by request parameters, and links the query statement
	 * with it's database connection.
	 * @param yq the {@link YADAQuery} to prep
	 * 
   * @since 7.0.0
   * @throws YADAResourceException when a query's source attribute can't be found in the application
   *           context, or there is another problem with the context
   * @throws YADAUnsupportedAdaptorException when there is no adaptor available for the source or protocol or
   *           the intended adaptor can't be instantiated
   * @throws YADAConnectionException when a connection to the source can't be opened
   * @throws YADARequestException when filters are included in the request config, but can't be
   *           converted into a JSONObject
   * @throws YADAAdaptorException when the query cannot be built by the adaptor
   * @throws YADAParserException when the query code cannot be parsed successfully
   */
	void prepQueryForExecution(YADAQuery yq) throws YADAResourceException, YADAUnsupportedAdaptorException, YADAConnectionException, YADARequestException, YADAAdaptorException, YADAParserException
	{

    String conformedCode = yq.getConformedCode();
    String wrappedCode   = "";
    int    dataSize      = yq.getData().size() > 0 ? yq.getData().size() : 1;
    if(this.qutils.requiresConnection(yq))
    { 
      storeConnection(yq);
    }
    
    this.qutils.processStatement(yq);
    
    if(yq.getProtocol().equals(Parser.JDBC))
    {
      if(yq.getType().equals(Parser.CALL))
      {
        for (int row = 0; row < dataSize; row++)
        {
          wrappedCode = ((JDBCAdaptor)yq.getAdaptor()).buildCall(conformedCode).toString();
          String msg  = "\n------------------------------------------------------------";
                 msg += "\n   Callable statement to execute:";
                 msg += "\n------------------------------------------------------------\n";
                 msg += wrappedCode.toString() + "\n";
          l.debug(msg);
          storeCallableStatement(yq, wrappedCode);
        }
        this.requiredCommits.add(yq.getApp());
      }
      else
      {
        boolean    count     = Boolean.parseBoolean(yq.getYADAQueryParamValue(YADARequest.PS_COUNT)[0]);
        boolean    countOnly = Boolean.parseBoolean(yq.getYADAQueryParamValue(YADARequest.PS_COUNTONLY)[0]);
        int        pageStart = Integer.parseInt(yq.getYADAQueryParamValue(YADARequest.PS_PAGESTART)[0]);
        int        pageSize  = Integer.parseInt(yq.getYADAQueryParamValue(YADARequest.PS_PAGESIZE)[0]);
        if (pageSize == -1)
          pageSize = YADAUtils.ONE_BILLION;
        int        firstRow  = 1 + (pageStart * pageSize) - pageSize;
        String     sortOrder = yq.getYADAQueryParamValue(YADARequest.PS_SORTORDER)[0];
        String     sortKey   = "";
        JSONObject filters   = null;
        
        if (yq.hasParamValue(YADARequest.PS_SORTKEY))
        {
          sortKey = yq.getYADAQueryParamValue(YADARequest.PS_SORTKEY)[0];
        }
        
        try
        {
          if (yq.hasParamValue(YADARequest.PS_FILTERS))
          {
            filters = new JSONObject(yq.getYADAQueryParamValue(YADARequest.PS_FILTERS)[0]);
          }
        } 
        catch (JSONException e)
        {
          String msg = "Error while getting filters from parameters.";
          throw new YADARequestException(msg, e);
        }
        
        for (int row = 0; row < dataSize; row++)
        {
          if(yq.getInList() != null && yq.getInList().size() > 0)
          {
            conformedCode = this.qutils.getConformedCode(this.qutils.processInList(yq,row));
          }
          
          if(yq.getType().equals(Parser.SELECT))
          {
            wrappedCode = ((JDBCAdaptor)yq.getAdaptor()).buildSelect( conformedCode,
                                                                      sortKey,
                                                                      sortOrder,
                                                                      firstRow,
                                                                      pageSize,
                                                                      filters).toString();
            String msg  = "\n------------------------------------------------------------";
                   msg += "\n   SELECT statement to execute:";
                   msg += "\n------------------------------------------------------------\n";
                   msg += wrappedCode.toString() + "\n";
            l.debug(msg);
          }
          else // INSERT, UPDATE, DELETE
          {
            wrappedCode = conformedCode;
            this.requiredCommits.add(yq.getApp());
            String msg  = "\n------------------------------------------------------------";
                   msg += "\n   INSERT/UPDATE/DELETE statement to execute:";
                   msg += "\n------------------------------------------------------------\n";
                   msg += wrappedCode.toString() + "\n";
            l.debug(msg);
          }
          
          storePreparedStatement(yq, wrappedCode, row);
          if (yq.getType().equals(Parser.SELECT) && (count || countOnly))
          {
            wrappedCode = ((JDBCAdaptor)yq.getAdaptor()).buildSelectCount(conformedCode,filters).toString();
            String msg  = "\n------------------------------------------------------------";
                   msg += "\n   SELECT COUNT statement to execute:";
                   msg += "\n------------------------------------------------------------\n";
                   msg += wrappedCode.toString() + "\n";
            l.debug(msg);
            storePreparedStatementForCount(yq, yq.getPstmt(row), wrappedCode);
          }
          
          if(yq.getStatement() != null)
            this.qutils.setPositionalParameterValues(yq, row);
          else
            this.qutils.setValsInPosition(yq, row);
        } // end data loop
      } // end if callable
    } // end if JDBC
    else if(yq.getProtocol().equals(Parser.SOAP)
        || yq.getProtocol().equals(Parser.REST)
        || yq.getProtocol().equals(Parser.FILE))
    {
      if(yq.getType().equals(Parser.SOAP))
      {
        // TODO this may have introduced a bug (10-SEP-16)
        yq.setSource(yq.getSource().replace(SOAPAdaptor.PROTOCOL_SOAP,SOAPAdaptor.PROTOCOL_HTTP));
        for (int row = 0; row < dataSize; row++)
        {
          wrappedCode = ((SOAPAdaptor)yq.getAdaptor()).build(yq);
          storeSoapMessage(yq, wrappedCode);
          this.qutils.setValsInPosition(yq, row);
        }
      }
      else if(yq.getType().equals(Parser.REST))
      {
        for (int row = 0; row < dataSize; row++)
        {
          wrappedCode = ((RESTAdaptor)yq.getAdaptor()).build(yq);
          storeRestQuery(yq, wrappedCode);
          this.qutils.setValsInPosition(yq, row);
        }
      }
      else // filesystem
      {
        for (int row = 0; row < dataSize; row++)
        {
          wrappedCode = ((FileSystemAdaptor)yq.getAdaptor()).build(yq);
          storeRestQuery(yq, wrappedCode);
          this.qutils.setValsInPosition(yq, row);
        }
      }
    }
    else
    {
      String msg = "The query you are attempting to execute requires ";
      msg += "a protocol or class that is not supported.  This ";
      msg += "could be the result of a configuration issue.";
      throw new YADAUnsupportedAdaptorException(msg);
    } // end protocols
	}

	/**
	 * @since 4.0.0
	 * @throws YADAResourceException when a query's source attribute can't be found in the application
	 *           context, or there is another problem with the context
	 * @throws YADAUnsupportedAdaptorException when there is no adaptor available for the source or protocol or
	 *           the intended adaptor can't be instantiated
	 * @throws YADAConnectionException when a connection to the source can't be opened
	 * @throws YADARequestException when filters are included in the request config, but can't be
	 *           converted into a JSONObject
	 * @throws YADAAdaptorException when the query cannot be built by the adaptor
	 * @throws YADAParserException when the query code cannot be parsed successfully
	 */
	private void prepQueriesForExecution() throws YADAResourceException, YADAUnsupportedAdaptorException, YADAConnectionException, YADARequestException, YADAAdaptorException, YADAParserException
	{
		for (YADAQuery yq : this.getQueries())
		{
		  prepQueryForExecution(yq);
		} // query loop
	} 

	/**
	 * Populates the data and parameter storage in the query object, using values passed in request object
	 * @since 4.0.0
	 * @param jSONParams
	 *          the request param containing the query information to process
	 * @return array of {@link YADAQuery} objects corresponding to JSONParams entries
	 * @throws YADAFinderException when a query in {@code jsonParams} can't be found in the YADA index
	 * @throws YADAConnectionException when a connection to the YADA index can't be established
	 * @throws YADAQueryConfigurationException when the YADA request is malformed
 	 * @throws YADAUnsupportedAdaptorException when the adaptor attached to the query object can't be found or instantiated
	 * @throws YADAResourceException when the query {@code q} can't be found in the index
 
	 */
	YADAQuery[] endowQueries(JSONParams jSONParams) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException, YADAResourceException, YADAUnsupportedAdaptorException
	{
		Iterator<String> jpIter = jSONParams.keySet().iterator();
		int index = 0;
		YADAQuery[] yqs = new YADAQuery[jSONParams.size()];
		while (jpIter.hasNext())
		{
			String qname = jpIter.next();
			
			YADAQuery yq = new Finder().getQuery(qname,this.getUpdateStats());
			yqs[index++] = endowQuery(yq,jSONParams.get(qname));
		}
		return yqs;
	}
	
	/**
	 * Populates the data and parameter storage in the {@code yq} object, using values passed {@code entry}
	 * @param yq the {@link YADAQuery} to augment
	 * @param entry the {@link JSONParamsEntry} containing the values to store in the query
	 * @return the augmented version of {@code yq}
	 * @since 4.2.0
	 * @throws YADAQueryConfigurationException when the YADA request is malformed
 	 * @throws YADAUnsupportedAdaptorException when the adaptor attached to the query object can't be found or instantiated
	 * @throws YADAResourceException when the query {@code q} can't be found in the index
	 * @throws YADAConnectionException when a connection pool or string cannot be established
 
	 */
	YADAQuery endowQuery(YADAQuery yq, JSONParamsEntry entry) throws YADAQueryConfigurationException, YADAResourceException, YADAUnsupportedAdaptorException, YADAConnectionException 
	{
		yq.addAllData(entry.getData());
		yq.addYADAQueryParams(entry.getParams());
		yq.setParameterizedColumns(yq.getColumnNameArray());
		return endowQuery(yq);
	}

	/**
	 * Populates the data and parameter storage in the query object, using values passed in request object.
	 * When the request has a qname and params, it is for a singular query.
	 * 
	 * @param q the name of the query to process
	 * @return a {@link YADAQuery} object retrieved from the YADA index corresponding to {@code q}
	 * 
	 * @throws YADAFinderException when the query {@code q} can't be found in the YADA index
	 * @throws YADAConnectionException when a connection to the YADA index can't be established
	 * @throws YADAQueryConfigurationException whenthe YADA request is malformed
	 * @throws YADAUnsupportedAdaptorException when the adaptor attached to the query object can't be found or instantiated
	 * @throws YADAResourceException when the query {@code q} can't be found in the index
	 * @since 4.0.0
	 */
	YADAQuery endowQuery(String q) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException, YADAResourceException, YADAUnsupportedAdaptorException
	{
		YADAQuery yq = new Finder().getQuery(q,this.getUpdateStats());
		LinkedHashMap<String,String[]> data = new LinkedHashMap<>();
    String[][] params = this.yadaReq.getParams();
    if (params != null)
    {
      for (int i = 0; i < params.length; i++)
      {
        data.put(QueryUtils.YADA_COLUMN + String.valueOf(i + 1), params[i]);
      }
      yq.addData(data);
      yq.setParameterizedColumns(yq.getColumnNameArray());
    }
    yq.addParam(YADARequest.PS_QNAME, yq.getQname());
		endowQuery(yq);
		return yq;
	}

	/**
	 * Populates the data and parameter storage in the query object, using values passed in request object
	 * @since 4.0.0
	 * @param yq the query object to be processed
	 * @return {@code yq}, now endowed with metadata
	 * @throws YADAQueryConfigurationException when the YADA request is malformed
	 * @throws YADAUnsupportedAdaptorException when the adaptor attached to the query object can't be found or instantiated
	 * @throws YADAResourceException when the query {@code q} can't be found in the index
	 * @throws YADAConnectionException when a connection pool or string cannot be established
	 */
	YADAQuery endowQuery(YADAQuery yq) throws YADAQueryConfigurationException, YADAResourceException, YADAUnsupportedAdaptorException, YADAConnectionException 
	{
	  int index = 0;
	  if (getJsonParams() != null)
	    index = ArrayUtils.indexOf(getJsonParams().getKeys(), yq.getQname());
		yq.addRequestParams(this.yadaReq.getRequestParamsForQueries(),index);
		yq.setAdaptorClass(this.qutils.getAdaptorClass(yq.getApp()));
		if(RESTAdaptor.class.equals(yq.getAdaptorClass()))
		{
			if(this.yadaReq.hasCookies())
			{
	      for(String cookieName : this.yadaReq.getCookies())
	      {
	        for(Cookie cookie : this.yadaReq.getRequest().getCookies())
	        {
	          if(cookie.getName().equals(cookieName))
	          {
	            yq.addCookie(cookieName, Base64.encodeBase64String(Base64.decodeBase64(cookie.getValue().getBytes())));
	          }
	        }
	      }
			}
			if(this.yadaReq.hasHttpHeaders())
			{
				JSONObject httpHeaders = this.yadaReq.getHttpHeaders(); 
				@SuppressWarnings("unchecked")
				Iterator<String> keys = httpHeaders.keys();
				while(keys.hasNext())
				{
					String name = keys.next(); 
					yq.addHttpHeader(name, httpHeaders.getString(name));
				}
			}
    }
		
		//TODO handle missing params exceptions here, throw YADARequestException
		//TODO review instances where YADAQueryConfigurationException is thrown
		this.qutils.setProtocol(yq);
		yq.setAdaptor(this.qutils.getAdaptor(yq.getAdaptorClass(), this.yadaReq));
		yq.setConformedCode(this.qutils.getConformedCode(yq.getYADACode()));
		for (int row = 0; row < yq.getData().size(); row++)
		{
		  // TODO perhaps move this functionality to the deparsing step? 
		  yq.addDataTypes(row, this.qutils.getDataTypes(yq.getYADACode()));
		  int paramCount = yq.getDataTypes().get(0).length;
		  yq.addParamCount(row, paramCount);
		}
		return yq;
	}

	/**
	 * Accessor for array of {@link YADAQuery} objects.
	 * 
	 * @since 4.0.0
	 * @return array of YADAQuery objects
	 */
	public YADAQuery[] getQueries()
	{
		return this.queries;
	}

	/**
	 * Accessor for {@link YADAQuery} at index.
	 * 
	 * @param index
	 *          the position of the desired query in the internal array
	 * @return YADAQuery at the desired index
	 */
	public YADAQuery getQuery(int index)
	{
		return this.getQueries()[index];
	}
	
	/**
	 * Returns the value of {@link YADARequest#PS_UPDATE_STATS} 
	 * @return {@code true} by default, or {@code false} if set deliberately in the request
	 */
	private boolean getUpdateStats() {
		return this.yadaReq.getUpdateStats();
	}
	
	/**
	 * Standard accessor.
	 * @return the local {@link YADARequest}
	 */
	private YADARequest getYADAReq() {
	  return this.yadaReq;
	}

	/**
	 * Standard mutator for variable
	 * 
	 * @param yadaReq
	 *          YADA request configuration
	 */
	private void setYADAReq(YADARequest yadaReq)
	{
		this.yadaReq = yadaReq;
	}

	/**
	 * Standard accessor for variable
	 * 
	 * @return the jsonParams object
	 */
	private JSONParams getJsonParams()
	{
		return this.jsonParams;
	}

	/**
	 * Standard mutator for variable
	 * 
	 * @param jsonParams
	 *          the config object to set
	 */
	private void setJsonParams(JSONParams jsonParams)
	{
		this.jsonParams = jsonParams;
	}

	/**
	 * @param queries the queries to set
	 */
	private void setQueries(YADAQuery[] queries)
	{
		this.queries = queries;
	}
}
