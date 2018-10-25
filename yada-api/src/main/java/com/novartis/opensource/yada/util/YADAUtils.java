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
package com.novartis.opensource.yada.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

import org.json.JSONException;
import org.json.JSONObject;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.JSONParams;
import com.novartis.opensource.yada.QueryManager;
import com.novartis.opensource.yada.Service;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAExecutionException;
import com.novartis.opensource.yada.YADAFinderException;
import com.novartis.opensource.yada.YADAParserException;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADARequestException;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.YADASQLException;
import com.novartis.opensource.yada.YADASecurityException;
import com.novartis.opensource.yada.YADAUnsupportedAdaptorException;
import com.novartis.opensource.yada.adaptor.YADAAdaptorException;
import com.novartis.opensource.yada.adaptor.YADAAdaptorExecutionException;

/**
 * Provider of convenience methods and "one-liners" for use primarily in plugins, but also
 * used in the guts of the framework.
 * @author David Varon
 *
 */
public class YADAUtils {
	
	
  /**
   * A constant equal to: {@value}, the JNDI key for the framework version value
   */
  public final static String   YADA_VERSION             = "yada-api-version";
  /**
	 * A constant equal to: {@value}
	 */
	public final static String   Q_NEXTVAL	              = "YADA select nextval";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   RSQ_SEQUENCE             = "RSQ_SEQ";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   VAL	                    = "VAL";
	/**
	 * A constant equal to: {@value}
	 */
	public final static int      ONE_BILLION              = 1000000000;
	/**
	 * A constant equal to the value of {@code System.getProperty("java.io.tmpdir")}
	 */
	public final static String   TMP                      = System.getProperty("java.io.tmpdir");
	/**
	 * A constant equal to: {@value}
	 */
	public final static boolean  COUNT                    = true;
	/**
	 * A constant equal to the one-index array: <code>{"false"}</code> 
	 */
	public final static String[] NOCOUNT                  = new String[] {"false"};
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   UNDEFINED                = "undefined";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   NULLSTRING               = "null";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_COUNT         = "COUNT";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_PAGESIZE      = "PAGESIZE";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_PAGESTART     = "PAGESTART";
	/**
	 * A constant equal to: {@value}
	 */
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_FILTERS       = "FILTERS";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_SORTKEY       = "SORTKEY";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_SORTORDER     = "SORTORDER";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_FORMAT        = "FORMAT";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_DELIMITER      = "DELIMITER";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_ROW_DELIMITER = "ROW_DELIMITER";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_QNAME         = "QNAME";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_HARMONYMAP    = "HARMONYMAP";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_PROTOCOL      = "PROTOCOL";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_METHOD        = "METHOD";
	/**
	 * A constant equal to: {@value}
	 */
	public final static String   PARAM_FRAG_UPDATE_STATS  = "UPDATE_STATS";
	/**
   * A constant equal to: {@value}
   * @since 6.2.0
   */
  public final static String   PARAM_FRAG_JOIN          = "JOIN";
  /**
   * A constant equal to: {@value}
   * @since 6.2.0
   */
  public final static String   PARAM_FRAG_LEFTJOIN      = "LEFTJOIN";
	/**
	 * A contsant {@link String}[] array containing the param name fragment constants of all request-level parameters: 
	 * <p>
	 * {@link #PARAM_FRAG_COUNT}, {@link #PARAM_FRAG_FILTERS}, {@link #PARAM_FRAG_PAGESIZE}, {@link #PARAM_FRAG_PAGESTART},
	 * {@link #PARAM_FRAG_SORTKEY}, {@link #PARAM_FRAG_SORTORDER}, {@link #PARAM_FRAG_METHOD}, {@link #PARAM_FRAG_FORMAT},
	 * {@link #PARAM_FRAG_DELIMITER}, {@link #PARAM_FRAG_ROW_DELIMITER}, {@link #PARAM_FRAG_QNAME}, {@link #PARAM_FRAG_HARMONYMAP},
	 * {@link #PARAM_FRAG_PROTOCOL}, {@link #PARAM_FRAG_UPDATE_STATS}, {@link #PARAM_FRAG_JOIN}, {@link #PARAM_FRAG_LEFTJOIN}
	 * </p>
	 */
	public final static String[] PARAM_FRAGS = new String[] {
		PARAM_FRAG_COUNT,
		PARAM_FRAG_FILTERS,
		PARAM_FRAG_PAGESIZE,
		PARAM_FRAG_PAGESTART,
		PARAM_FRAG_SORTKEY,
		PARAM_FRAG_SORTORDER,
		
		PARAM_FRAG_METHOD,
		PARAM_FRAG_FORMAT,
		PARAM_FRAG_DELIMITER,
		PARAM_FRAG_ROW_DELIMITER,
		PARAM_FRAG_QNAME,
		PARAM_FRAG_HARMONYMAP,
		PARAM_FRAG_PROTOCOL,
		
		PARAM_FRAG_UPDATE_STATS,
		PARAM_FRAG_JOIN,
		PARAM_FRAG_LEFTJOIN
	};
	
	/**
	 * Returns the in-use version of the YADA framework.
	 * 
	 * @return this YADA framework version derived from JNDI {@code yada.version} variable 
	 * @throws YADAResourceException when the JNDI path cannot be found or read
	 * @since 5.1.0
	 */
	public static String getVersion() throws YADAResourceException 
	{		
		String   version     = "-1";
		Class<?> clazz       = YADAUtils.class; 
		String   mfPath      = "META-INF/MANIFEST.MF";
		String   jarFilePath =  clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
		Manifest mf          = null;      
		try(JarFile jar = new JarFile(jarFilePath))
		{
		  mf = jar.getManifest();			
		} 
		catch (IOException e) 
		{			
			try(FileInputStream file = new FileInputStream(jarFilePath+"META-INF/MANIFEST.MF"))
			{
				mf = new Manifest(file);
			} 
			catch (IOException e1) 
			{
				String msg = "Unable to retrieve manifest from jar or path";
				throw new YADAResourceException(msg, e1);
			} 
		}
		Attributes attr = mf.getMainAttributes();
		version = attr.getValue(YADA_VERSION);
		return version;
	}
	
	/**
	 * A wrapper function which calls the {@link #Q_NEXTVAL} query, which executes an Oracle {@code nextval} function on {@code seq}.
	 * @param seq the oracle sequence name from which to obtain the value
	 * @return {@code int} containing the result of {@code nextval}
	 * @throws YADAExecutionException when the function call fails
	 */
	public static int getNextVal(String seq) throws YADAExecutionException  
	{
		int       retval = -1;
			YADARequest yadaReq = new YADARequest();
			yadaReq.setQname(new String[]{Q_NEXTVAL});
			yadaReq.setParams(new String[] {seq});
			yadaReq.setCount(new String[] {String.valueOf(NOCOUNT)});
			Service service = new Service(yadaReq);
			try
			{
				retval = new JSONObject(service.execute())
							.getJSONObject("RESULTSET")
							.getJSONArray("ROWS")
							.getJSONObject(0)
							.getInt("VAL");
			} 
			catch (JSONException e)
			{
				String msg = "Unable to parse result";
				throw new YADAExecutionException(msg,e);
			}
		return retval;
	}
	
	/**
	 * One-liner execution of a sql statement, returning an SQL {@link java.sql.ResultSet}.
	 * <strong>Note: This method opens a db connection but DOES NOT CLOSE IT. 
	 * Use the static method {@link ConnectionFactory#releaseResources(ResultSet)} to close it from 
	 * the calling method</strong>
	 * @param sql the query to execute
	 * @param params the data values to map to query columns
	 * @return a {@link java.sql.ResultSet} object containing the result of the query
	 * @throws YADAConnectionException when the datasource is inaccessible
	 * @throws YADASQLException when the JDBC configuration or execution fails
	 */
	public static ResultSet executePreparedStatement(String sql, Object[] params) throws YADAConnectionException, YADASQLException 
	{
		ResultSet rs = null;
		try
		{
			Connection        c = ConnectionFactory.getConnectionFactory().getConnection(ConnectionFactory.YADA_APP);
			PreparedStatement p = c.prepareStatement(sql);
			for(int i=1;i<=params.length;i++) 
			{
				Object param = params[i-1];
				if(param instanceof String)
				{
					p.setString(i, (String)param);
				}
				else if(param instanceof Date)
				{
					p.setDate(i, (Date)param);
				}
				else if(param instanceof Integer)
				{
					p.setInt(i, ((Integer)param).intValue());
				}
				else if(param instanceof Float)
				{
					p.setFloat(i, ((Float)param).floatValue());
				}
			}
			rs = p.executeQuery();
		} 
		catch (SQLException e)
		{
			throw new YADASQLException(e.getMessage(),e);
		}
		return rs;
	}
	
	/**
	 * One-liner execution of a jdbc-parameter-less sql statement, returning an SQL {@link java.sql.ResultSet}.
	 * <strong>Note: This method opens a db connection but DOES NOT CLOSE IT. 
	 * Use the static method {@link ConnectionFactory#releaseResources(ResultSet)} to close it from 
	 * the calling method</strong>
	 * @param sql the query to execute
	 * @return a {@link java.sql.ResultSet} object containing the result of the query
	 * @throws YADAConnectionException when the datasource is inaccessible
	 * @throws YADASQLException when the JDBC configuration or execution fails
	 */
	public static ResultSet executePreparedStatement(String sql) throws YADAConnectionException, YADASQLException
	{
		ResultSet rs = null;
		try
		{
			Connection        c = ConnectionFactory.getConnectionFactory().getConnection(ConnectionFactory.YADA_APP);
			PreparedStatement p = c.prepareStatement(sql);
			rs = p.executeQuery();
		} 
		catch (SQLException e)
		{
			throw new YADASQLException(e.getMessage(),e);
		}
		return rs;
	}
	
	/**
	 * One-liner execution of an SQL function.
	 * @param sql the query to execute
	 * @return the result of the function
	 * @throws YADAConnectionException when the datasource is inaccessible
	 * @throws YADASQLException when the JDBC configuration or execution fails
	 */
	public static int executeCallableStatement(String sql) throws YADAConnectionException, YADASQLException
	{
		CallableStatement c      = null;
		int               result = -1;
		try 
		{
			c = ConnectionFactory.getConnectionFactory().getConnection(ConnectionFactory.YADA_APP).prepareCall(sql);
			result = c.executeUpdate();
		} 
		catch (SQLException e) 
		{
			throw new YADASQLException(e.getMessage(),e);
		} 
		finally 
		{
			if (c != null)
			{
				ConnectionFactory.releaseResources(c);
			}
		}
		return result;
	}
	
	/**
	 * One-liner execution of a single YADA "get" (as opposed to "update" or "upload").
	 * Sets {@code count} = {@code false}
	 * @param qname the name of the query to execute
	 * @param params the params to map to the query columns
	 * @return the result of the query
	 */
	public static String executeYADAGet(String[] qname, String[] params) 
	{
		YADARequest yadaReq = new YADARequest();
		yadaReq.setCount(NOCOUNT);
		yadaReq.setQname(qname);
		if(params.length > 0)
		  yadaReq.setParams(params);
		Service service = new Service(yadaReq);
		return service.execute();
	}
	
	/**
	 * A convenience/utility method to execute a single "standard" yada query (i.e., one with {@code qname} and {@code params}.  
	 * JSONParams-based requests with multiple queries are not supported by this method.
	 * 
	 * @param yadaReq YADA request configuration
	 * @return Object result of query execution
	 * @throws YADAExecutionException if multiple query executions are attempted, or other YADA exceptions are thrown internally
	 * @throws YADASecurityException if there is authentication or authorization error in preparation for or during execution
	 */
	public static Object executeYADAQuery(YADARequest yadaReq) throws YADAExecutionException, YADASecurityException
	{
		Object result = null;
		try
		{
			QueryManager qmgr = new QueryManager(yadaReq);
			
			if(qmgr.getQueries().length > 1)
				throw new YADAExecutionException("This method supports only a single query");
			
			YADAQuery yq = qmgr.getQuery(0);
			yq.getAdaptor().execute(yq);
			result = yq.getResult().getResult(0);
		} 
		catch (YADAQueryConfigurationException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADAResourceException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADAConnectionException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADAFinderException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADAUnsupportedAdaptorException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADARequestException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADAAdaptorException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADAAdaptorExecutionException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		} 
		catch (YADAParserException e)
		{
			throw new YADAExecutionException(e.getMessage(),e);
		}
		return result;
	}
	
	/**
	 * Executes the queries defined in {@code jp} and returns the result.
	 * @param jp the configuration object
	 * @return String result of statement execution
	 *
	 */
	public static String executeYADAGetWithJSONParams(com.novartis.opensource.yada.JSONParams jp) 
	{
		YADARequest yadaReq = new YADARequest();
		yadaReq.setCount(NOCOUNT);
		yadaReq.setJsonParams(jp);
		Service service = new Service();
		service.setYADARequest(yadaReq);
		return service.execute();
	}
	
	/**
   * Executes the queries defined in {@code jp} and returns the result.
   * @param jp the configuration object
   * @return String result of statement execution
   *
   */
  public static String executeYADAGetWithJSONParamsNoStats(com.novartis.opensource.yada.JSONParams jp) 
  {
    YADARequest yadaReq = new YADARequest();
    yadaReq.setCount(NOCOUNT);
    yadaReq.setUpdateStats(NOCOUNT);
    yadaReq.setJsonParams(jp);
    Service service = new Service();
    service.setYADARequest(yadaReq);
    return service.execute();
  }
  
  
	
	
	/**
	 * One-liner execution of a YADA "update" query.  Sets {@code response} to {@link com.novartis.opensource.yada.format.CountResponse}, 
	 * returning only the {@code int} result, as a {@link String}, of caurse.
	 * @param jp the configuration object
	 * @return the result of the query
	 */
	public static String executeYADAUpdateWithJSONParams(com.novartis.opensource.yada.JSONParams jp)
	{
		YADARequest yadaReq = new YADARequest();
		yadaReq.setJsonParams(jp);
		yadaReq.setResponse(new String[] {"com.novartis.opensource.yada.format.CountResponse"});
		Service service = new Service();
		service.setYADARequest(yadaReq);
		return service.execute();
	}
	
	/**
	 * Returns {@code true} if {@code yadaReq} contains a {@code JSONParams} variable that is non-null and has
	 * a size &gt; 0.  This method does not validate the value, and could return {@code true} even if the 
	 * json string is malformed or non-compliant.
	 * @param yadaReq YADA request configuration
	 * @return boolean status of the parameter
	 */
	public static boolean hasJSONParams(YADARequest yadaReq) 
	{
	  JSONParams JSONParams = yadaReq.getJsonParams();
		if (JSONParams != null && JSONParams.size() > 0)
			return true;
		return false;
	}
	
	/**
	 * Uses java reflection to determine if {@code yadaReq} contains a plugin of {@code type}
	 * @param type the plugin scope
	 * @param yadaReq YADA request configuration
	 * @return {@code true} if the current config references a loadable plugin class
	 */
	private static boolean hasPlugin(String type, YADARequest yadaReq)
	{
	  //TODO use this method to short-circuit to plugin processing
		String[] plugin = yadaReq.getPlugin();
		if(plugin == null || plugin.length == 0)
		{
			return false;
		}
		Class<?> pluginClass = null;
		Class<?> pluginInterface = null;
		try
		{
			pluginClass = plugin[0].indexOf(YADARequest.PLUGIN_PKG) > -1 
					? Class.forName(plugin[0]) 
					: Class.forName(YADARequest.PLUGIN_PKG + "." + plugin[0]);
			pluginInterface = Class.forName(YADARequest.PLUGIN_PKG+"."+type);
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}
		
		if (pluginClass != null && pluginInterface != null && pluginInterface.isAssignableFrom(pluginClass)) // this checks plugin type
		{
			return true;
		}
		return false;
	}
	
	/**
	 * Returns {@code true} if a {@link YADARequest#BYPASS} plugin was registered in the request
	 * @param yadaReq YADA request configuration
	 * @return {@code true} if a {@link YADARequest#BYPASS} plugin was registered in the request
	 */
	public static boolean hasBypassPlugin(YADARequest yadaReq)
	{
		return hasPlugin(YADARequest.BYPASS, yadaReq);
	}
	
	/**
	 * Returns {@code true} if a {@link YADARequest#PREPROCESS} plugin was registered in the request
	 * @param yadaReq YADA request configuration
	 * @return {@code true} if a {@link YADARequest#PREPROCESS} plugin was registered in the request
	 */
	public static boolean hasPreprocessPlugin(YADARequest yadaReq)
	{
		return hasPlugin(YADARequest.PREPROCESS, yadaReq);
	}
	/**
	 * Returns {@code true} if a {@link YADARequest#POSTPROCESS} plugin was registered in the request
	 * @param yadaReq YADA request configuration
	 * @return {@code true} if a {@link YADARequest#POSTPROCESS} plugin was registered in the request
	 */
	public static boolean hasPostprocessPlugin(YADARequest yadaReq)
	{
		return hasPlugin(YADARequest.POSTPROCESS, yadaReq);
	}
	
	/**
	 * Returns {@code true} if the {@code qname} parameter was set in the request, or if it was not set, but a {@code BYPASS} or {@code PREPROCESS} plugin was.
	 * @param yadaReq YADA request configuration
	 * @return {@code true} if the {@code qname} parameter was set in the request, or if it was not set, but a {@code BYPASS} or {@code PREPROCESS} plugin was.
	 */
	public static boolean hasQname(YADARequest yadaReq) 
	{
		String qname = yadaReq.getQname();
		if ("".equals(qname)	
		    || UNDEFINED.equals(qname)
		    || NULLSTRING.equals(qname)
		    || null == qname
		    // qname is 'YADA dummy' and there are NO bypass or preproc plugins in the request
		    // (which means return 'true' if there ARE plugins:)
		    || (YADARequest.DEFAULT_QNAME.equals(qname) 
		    		&& !hasBypassPlugin(yadaReq) 
		    		&& !hasPreprocessPlugin(yadaReq)))
			return false;
		return true;
	}
	
	/**
	 * Returns {@code true} if {@link #hasJSONParams(YADARequest)} is {@code true} and {@link #hasQname(YADARequest)} is {@code false}.
	 * @param yadaReq YADA request configuration
	 * @return {@code true} if {@link #hasJSONParams(YADARequest)} is {@code true} and {@link #hasQname(YADARequest)} is {@code false}.
	 */
	public static boolean useJSONParams(YADARequest yadaReq)
	{
		if (hasJSONParams(yadaReq) && !hasQname(yadaReq))
		{
			return true;
		}
		return false;
	}
}
