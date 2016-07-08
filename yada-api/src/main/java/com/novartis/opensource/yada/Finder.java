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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
/**
 * Provides for retrieval YADA query code and metadata from the YADA Index as encapsulated {@link YADAQuery} objects.
 * @author David Varon
 * @since 4.0.0
 */
public class Finder 
{
	/**
	 * Local logger handle
	 */
	static Logger l = Logger.getLogger(Finder.class);
	/**
	 * Hardcoded to the stardard value: {@value}. Used to identify mapped sources in the YADA Index.
	 */
	public  final static String JNDI_PREFIX      = "java:comp/env";  
	/**
	 * Currently hardcoded to {@value}, this may be configurable in a future version. Used to access the YADA Index.
	 */
	public  final static String YADA_INDEX       = "/jdbc/yada"; //TODO Make yada jndi string a configurable property
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_VERSION     = "v";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_SOURCE      = "s";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_QUERY       = "q";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_QNAME       = "qn";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_URLPARAMS   = "p";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_PARAMTARGET = "t";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_PARAMNAME   = "n";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_PARAMVAL    = "v";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String JSON_PARAMRULE   = "r";
	/**
	 * A constant equal to: {@value}
	 */
	public  final static String NOT_APPLICABLE   = "na";
	
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_VERSION     = "VER";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_SOURCE      = "S";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_QUERY       = "Q";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_PARAMTARGET = "T";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_PARAMNAME   = "N";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_PARAMVAL    = "VAL";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_PARAMRULE   = "R";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String SQL_STATS      = "update yada_query set access_count=(select b.access_count+1 from yada_query b where b.qname = ?), last_access=? where qname = ?";
	/**
	 * Constant equal to: {@code select a.sql "+YADA_QUERY+", b.source "+YADA_SOURCE+", nvl(b.version,'na') "+YADA_VERSION+", nvl(c.target,'na') "+YADA_PARAMTARGET+", nvl(c.name,'na') "+YADA_PARAMNAME+", nvl(c.value,'na') "+YADA_PARAMVAL+", c.rule "+YADA_PARAMRULE+" from yada_query a join yada_query_conf b on a.app = b.app left join yada_params c on (a.app = c.target or a.name = c.target) where a.name = ? order by c.target}
	 */
	private final static String YADA_PKG_SQL   = "select "
																							+ "a.query "+YADA_QUERY+", "
																							+ "b.source "+YADA_SOURCE+", "
																							+ "b.version "+YADA_VERSION+", "
																							+ "c.target "+YADA_PARAMTARGET+", " 
																							+ "c.name "+YADA_PARAMNAME+", "
																							+ "c.value "+YADA_PARAMVAL+", "
																							+ "c.rule "+YADA_PARAMRULE+" "
																							+ "from yada_query a "
																							+ "join yada_query_conf b on a.app = b.app "
																							+ "left join yada_params c on (a.app = c.target or a.qname = c.target) "
																							+ "where a.qname = ? order by c.target";
	/**
	 * Constant equal to: {@value}
	 */
	public final static String YADA_CACHE     = "YADAIndex";
	/**
	 * Constant equal to: {@value}
	 */
	public final static String YADA_CACHE_MGR     = "YADAIndexManager";
	
	
	/**
	 * Retreives the environment variables mapped to the application context using JNDI, for example, {@code "io/in"}, or {@code "yada_bin"}.  
	 * If {@code jndiPath} is not found in the application context, an ottempt is made to find a system property with the same name. 
	 * @param jndiPath a string registered in the application context, or system property
	 * @return {@link String} value of variable mapped to JNDI parameter string
	 * @throws YADAResourceException when the JNDI string is not found in the application context or system properties
	 */
	public static String getEnv(String jndiPath) throws YADAResourceException
	{
		InitialContext ictx;
		String result = "";
		try 
		{
			ictx = new javax.naming.InitialContext();
			javax.naming.Context env = (javax.naming.Context)ictx.lookup(JNDI_PREFIX);
			try
			{
				NamingEnumeration<NameClassPair> list = ictx.list(JNDI_PREFIX);
				l.debug("JNDI Environment:");
				while (list.hasMore())
				{
					NameClassPair nc = list.next();
					l.debug("    ["+nc+"]");
				}
			}
			catch (Exception e)
			{
				l.debug("An exception was thrown while attempting to list contexts. The context implementation might not support it.  This is a non-fatal error");
			}
			result = (String) env.lookup(jndiPath);
		} 
		catch (NamingException e) 
		{
			
			String msg = "There was a problem locating the resource or variable identified by the supplied JNDI path ("+jndiPath+") in the initial context...trying system properties.";
			l.warn(msg);
			result = System.getProperty(jndiPath);
			if(result == null)
			{
				msg = "The property [" + jndiPath + "] was not found.";
				throw new YADAResourceException(msg,e);
			}
		} 
		return result;
	}

	/**
	 * Convenient wrapper to return a {@link String} equal to {@link #JNDI_PREFIX} + {@link #YADA_INDEX}
	 * @return {@link String} equal to {@link #JNDI_PREFIX} + {@link #YADA_INDEX}
	 */
	public static String getYADAJndi() 
	{
		return JNDI_PREFIX + YADA_INDEX;
	}
	
	/**
	 * Retrieves the query {@code q} from the distributed index (i.e., database).  Typically the query is 
	 * then added to the in-memory cache. 
	 * @param q the query name to retrieve
	 * @return the {@link YADAQuery} retrieved from the distributed index
	 * @throws YADAFinderException if the query {@code q} can't be found in the YADA index.  Check your spelling and case.
	 * @throws YADAConnectionException if an error occurs when attempting to query the YADA Index.  This could be the result of a configuration issue.
	 * @throws YADAQueryConfigurationException 
	 * @see #getQuery(String)
	 */
	public YADAQuery getQueryFromIndex(String q) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException
	{
		ResultSet rs = null;
		YADAQuery yq = new YADAQuery();
		try
		{
			
			Connection        conn  = ConnectionFactory.getConnection(getYADAJndi());
			PreparedStatement pstmt;
			try
			{
				pstmt = conn.prepareStatement(YADA_PKG_SQL);
				pstmt.setString(1,q);
			} 
			catch (SQLException e)
			{
				String msg = "Unable to create or configure the PreparedStatement used to lookup the requested query in the YADA Index.  This could be a serious configuration issue.";
				throw new YADAConnectionException(msg,e);
			}
			int row = 0;
			
			try
			{
				rs = pstmt.executeQuery();
				if(!rs.isBeforeFirst())
				{
					String msg = "The request query ["+q+"] does not exist.";
					throw new YADAFinderException(msg);
				}
				while (rs.next())
				{
					if (row == 0)
					{
						String v = rs.getString(YADA_VERSION);
						yq.setVersion(v == null ||  v.equals(NOT_APPLICABLE) ? "" : v);
						yq.setCoreCode(rs.getString(YADA_QUERY));
						yq.setSource(rs.getString(YADA_SOURCE));
						yq.setQname(q);
					}
					setDefaultParam(yq,rs);
					row++;
				}
			} 
			catch (SQLException e)
			{
				String msg = "The lookup query caused an error. This could be because the query name ("+q+") was mistyped or doesn't exist in the YADA Index";
				throw new YADAFinderException(msg,e);
			}
			l.debug("Query package: "+ yq.toString());
		}
		finally
		{
			ConnectionFactory.releaseResources(rs);
		}
		return yq;
	}
	
	/**
	 * Overloaded version of {@link #getQuery(String, boolean)} always passing {@code true} in the second {@code updateStats} arg.
	 * @since 5.0.0
	 * @param q the stored canonical name of the desired {@link YADAQuery} 
	 * @return {@link YADAQuery} object encapsulating the query code and parameters
	 * @throws YADAFinderException if the query {@code q} can't be found in the YADA index.  Check your spelling and case.
	 * @throws YADAConnectionException if an error occurs when attempting to query the YADA Index.  This could be the result of a configuration issue.
	 * @throws YADAQueryConfigurationException if default parameters cannot be set
	 */
	public YADAQuery getQuery(String q) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException {
		return this.getQuery(q, true);
	}
	
	/**
	 * A cornerstone to the entire YADA framework, this method takes the name of a stored 
	 * query as an argument and returns a {@link YADAQuery} object containing the query code and default parameters,
	 * as well as data structures and methods to facilitate it's execution.
	 * <p>
	 * There is now an <a href="http://www.ehcache.org">EhCache</a> implementation in which all queries are stored upon retrieval.
	 * When a query is subsequently requested, it is found in the cache, cloned, and the clone put to use.
	 * </p>
	 *
	 * 
	 * @since 4.0.0
	 * @param q the stored canonical name of the desired {@link YADAQuery} 
	 * @param updateStats set to {@code true} to execute parallel operation to query update access count and date, {@code false} to suppress the operation
	 * @return {@link YADAQuery} object encapsulating the query code and parameters
	 * @throws YADAFinderException if the query {@code q} can't be found in the YADA index.  Check your spelling and case.
	 * @throws YADAConnectionException if an error occurs when attempting to query the YADA Index.  This could be the result of a configuration issue. 
	 * @throws YADAQueryConfigurationException if default parameters cannot be set
	 * @see YADAQuery 
	 *  
	 */
	public YADAQuery getQuery(String q, boolean updateStats) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException
	{
		final String qname = q;
		YADAQuery yq        = null; 
		Element   cachedYq  = null;
		Cache     yadaIndex = ConnectionFactory.getCacheConnection(YADA_CACHE_MGR,YADA_CACHE);
		
		if(yadaIndex != null)
		{  
		  cachedYq = yadaIndex.get(qname);
  		if(cachedYq != null)
  		{
  			yq = new YADAQuery((YADAQuery)cachedYq.getObjectValue());
  			l.debug("YADAQuery ["+qname+"] retrieved from cache");
  		}
  		else
  		{
  			yq = getQueryFromIndex(q);
  			// update cache
  			Element yqElement = new Element(qname,yq);
  			yadaIndex.put(yqElement);
  			yq.setCached(true);
  			l.debug("YADAQuery ["+qname+"] stored in cache.");
  		}
		}
		
		if(updateStats)
		{
			// log usage of query 
			(new Thread() {
				
				@Override
				public void run() {
					try
					{
						updateQueryStatistics(qname);
					} 
					catch (YADAConnectionException e)
					{
						l.error(e.getMessage(),e);
					} 
					catch (YADAFinderException e)
					{
						l.error(e.getMessage(),e);
					} 
					finally
					{
						//TODO what happens in finally block?
					}
				}
			}).start();
		}
				
		return yq;
	}
	
	/**
	 * Wraps default parameter data stored in the YADAIndex into a {@link YADAParam} object and adds it to {@code yq}.
	 * @param yq the {@link YADAQuery} to which to add the parameter defined in {@code rs} 
	 * @param rs the data containing the parameter, as retrieved from the YADA Index
	 * @throws YADAFinderException when {@code rs} can't be processed
	 */
	@SuppressWarnings("static-method")
	private void setDefaultParam(YADAQuery yq, ResultSet rs) throws YADAFinderException, YADAQueryConfigurationException
	{
		//TODO should this return null, rather than default 'na' values?
		try
		{
			String name = rs.getString(YADA_PARAMNAME);
			if(!( name == null || name.equals(NOT_APPLICABLE)))
			{	
				String target = rs.getString(YADA_PARAMTARGET) != null ? rs.getString(YADA_PARAMTARGET) : NOT_APPLICABLE;
				String value  = rs.getString(YADA_PARAMVAL) != null ? rs.getString(YADA_PARAMVAL) : NOT_APPLICABLE;
				YADAParam param = new YADAParam();
				param.setName(name);
				param.setTarget(target);
				param.setValue(value);
				param.setRule(rs.getInt(YADA_PARAMRULE));
				param.setDefault(true);
				yq.addParam(param);
			}
		}
		catch(SQLException e)
		{
			String msg = "Unable to set default params.";
			throw new YADAQueryConfigurationException(msg,e);
		}
	}
	
	/**
	 * A utility method called in a separate thread by {@link Finder#getQuery(String)} to increment a query-access counter in the YADA Index.
	 * 
	 * @param qname the name of the query just requested
	 * @throws YADAConnectionException when the YADA Index can't be accessed
	 * @throws YADAFinderException when {@code qname} can't be found in the YADA Index.
	 * @see Finder#getQuery(String)
	 */
	void updateQueryStatistics(String qname) throws YADAConnectionException, YADAFinderException
	{

		l.debug("Updating Query Stats for ["+qname+"]");
		PreparedStatement pstmt = null;
		String querySql    = SQL_STATS;
		try
		{
			Connection conn = ConnectionFactory.getConnection(JNDI_PREFIX + YADA_INDEX);
			
			try
			{
				pstmt = conn.prepareStatement(querySql);
				pstmt.setString(1,qname);
				long t = new Date().getTime();
        java.sql.Timestamp sqlDateVal = new java.sql.Timestamp(t);
				pstmt.setTimestamp(2, sqlDateVal);
				pstmt.setString(3,qname);
				
			} 
			catch (SQLException e)
			{
				String msg = "Unable to create or configure the PreparedStatement used to update the access statistics for the requested query in the YADA Index.  This could be a serious configuration issue.";
				throw new YADAConnectionException(msg,e);
			}
			
			try
			{
				pstmt.executeUpdate();
				if(!conn.getAutoCommit())
				  conn.commit();
			}
			catch (SQLException e)
			{
				String msg = "The lookup query caused an error. This could be because the query name ("+qname+") was mistyped or doesn't exist in the YADA Index";
				throw new YADAFinderException(msg,e);
			}
			
		}
		finally
		{
			ConnectionFactory.releaseResources(pstmt);
		}
	}
}
