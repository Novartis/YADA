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

//import javax.naming.InitialContext;
//import javax.naming.NameClassPair;
//import javax.naming.NamingEnumeration;
//import javax.naming.NamingException;


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
	 * @deprecated since 8.0.0
	 */
	@SuppressWarnings("unused")
  @Deprecated
	private final static String YADA_VERSION     = "VER";
	/**
   * Constant equal to: {@value}
   */
  private final static String YADA_SOURCE      = "S";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_CONF        = "C";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String YADA_QUERY       = "Q";
	/**
   * Constant equal to: {@value}
   */
  private final static String YADA_APP         = "A";
  /**
   * Constant equal to: {@value}
   */
  private final static String YADA_PARAMID     = "I";
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
  private final static String YADA_PROPTARGET   = "PROPT";
  /**
   * Constant equal to: {@value}
   */
  private final static String YADA_PROPNAME   = "PROPN";
  /**
   * Constant equal to: {@value}
   */
  private final static String YADA_PROPVALUE   = "PROPV";
  /**
   * Constant equal to: {@value}
   */
  private final static String YADA_ACCESS_COUNT   = "AC";
	/**
	 * Constant equal to: {@value}
	 */
	private final static String SQL_STATS      = "update yada_query set access_count=(select b.access_count+1 from yada_query b where b.qname = ?), last_access=? where qname = ?";
	/**
   * Constant equal to: {@value}
   */
	private final static String SQL_STATS_WCOUNT = "update yada_query set access_count = ?,last_access = ? where qname = ?";
	/**
	 * Constant equal to: {@code "select "
                                + "a.query "       +YADA_QUERY+", "
                                + "b.app "         +YADA_APP+", "
                                + "b.source "      +YADA_SOURCE+", "
                                + "b.conf "        +YADA_CONF+", "
                                + "c.id "          +YADA_PARAMID+", "
                                + "c.target "      +YADA_PARAMTARGET+", " 
                                + "c.name "        +YADA_PARAMNAME+", "
                                + "c.value "       +YADA_PARAMVAL+", "
                                + "c.rule "        +YADA_PARAMRULE+", "
                                + "d.target "      +YADA_PROPTARGET+", "
                                + "d.name "        +YADA_PROPNAME+", "
                                + "d.value "       +YADA_PROPVALUE+", "
                                + "a.access_count "+YADA_ACCESS_COUNT+" "
                                + "from yada_query a "
                                + "join yada_query_conf b on (a.app = b.app) "
                                + "left join yada_param c on (a.app = c.target or a.qname = c.target) "
                                + "left join yada_prop d on (a.app = d.target or a.qname = d.target or c.target||'-'||c.id = d.target) "
                                + "where b.active = 1 "
                                + "and a.qname = ? "
                                + "order by c.target"}
	 */
	private final static String YADA_PKG_SQL   = "select "
																							+ "a.query "       +YADA_QUERY+", "
																							+ "b.app "         +YADA_APP+", "
																							+ "b.source "      +YADA_SOURCE+", "
																							+ "b.conf "        +YADA_CONF+", "
																							+ "c.id "          +YADA_PARAMID+", "
																							+ "c.target "      +YADA_PARAMTARGET+", " 
																							+ "c.name "        +YADA_PARAMNAME+", "
																							+ "c.value "       +YADA_PARAMVAL+", "
																							+ "c.rule "        +YADA_PARAMRULE+", "
																							+ "d.target "      +YADA_PROPTARGET+", "
																							+ "d.name "        +YADA_PROPNAME+", "
																							+ "d.value "       +YADA_PROPVALUE+", "
																							+ "a.access_count "+YADA_ACCESS_COUNT+" "
																							+ "from yada_query a "
																							+ "join yada_query_conf b on (a.app = b.app) "
																							+ "left join yada_param c on (a.app = c.target or a.qname = c.target) "
																							+ "left join yada_prop d on (a.app = d.target or a.qname = d.target or c.target||'-'||c.id = d.target) "
																							+ "where b.active = 1 "
																							+ "and a.qname = ? "
																							+ "order by c.target";
	
	
	/**
	 * Constant equal to: {@value}
	 */
	public final static String YADA_CACHE     = "YADAIndex";
	/**
	 * Constant equal to: {@value}
	 */
	public final static String YADA_CACHE_MGR     = "YADAIndexManager";
	
	
	/**
	 * Retrieves the system property loaded at startup.  This method was refactored in version 8.3.0  
	 *  
	 * @param property a string registered in the application context, or system property
	 * @return {@link String} value of the system property
	 * @throws YADAResourceException when the property is null or empty
	 */
	public static String getEnv(String property) throws YADAResourceException
	{
		String result = System.getProperty(property);
		if(result == null || "".equals(result))
		{
			String msg = "The property [" + property + "] was not found.";
			throw new YADAResourceException(msg);
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
	 * @throws YADAQueryConfigurationException when default parameters are mis-configured
	 * @see #getQuery(String)
	 */
	public YADAQuery getQueryFromIndex(String q) throws YADAConnectionException, YADAFinderException, YADAQueryConfigurationException
	{
	  ResultSet rs = null;
		YADAQuery yq = new YADAQuery();
		try
		{
			
			
			
			try(Connection        conn  = com.novartis.opensource.yada.ConnectionFactory.getConnectionFactory().getConnection(com.novartis.opensource.yada.ConnectionFactory.YADA_APP);
			    PreparedStatement pstmt = conn.prepareStatement(YADA_PKG_SQL);)
			{
				pstmt.setString(1,q);
				int row = 0;
	      
	      try
	      {
	        rs = pstmt.executeQuery();
	        if(!rs.isBeforeFirst())
	        {
	          String msg = "The requested query ["+q+"] does not exist.";
	          throw new YADAFinderException(msg);
	        }
	        while (rs.next())
	        {
	          if (row == 0)
	          {
	            yq.setVersion("");
	            yq.setCoreCode(rs.getString(YADA_QUERY));
	            yq.setSource(rs.getString(YADA_SOURCE));
	            yq.setQname(q);
	            yq.setApp(rs.getString(YADA_APP));
	            yq.setAccessCount(rs.getInt(YADA_ACCESS_COUNT));
	          }
	          setDefaultParam(yq,rs);
	          setProperty(yq,rs);
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
			catch (SQLException e)
			{
				String msg = "Unable to create or configure the PreparedStatement used to lookup the requested query in the YADA Index.  This could be a serious configuration issue.";
				throw new YADAConnectionException(msg,e);
			}
			
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
		Cache     yadaIndex = ConnectionFactory.getConnectionFactory().getCacheConnection(YADA_CACHE_MGR,YADA_CACHE);
		
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
  	
		  yq.setAccessCount(yq.getAccessCount() + 1);
  		final int accessCount = yq.getAccessCount();
  		if(updateStats)
  		{
  			// log usage of query 
  			(new Thread() {
  				
  				@Override
  				public void run() {
  					try
  					{
  						updateQueryStatistics(qname,accessCount);
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
		}
				
		return yq;
	}
	
	/**
	 * Wraps default parameter data stored in the YADAIndex into a {@link YADAParam} object and adds it to {@code yq}.
	 * @param yq the {@link YADAQuery} to which to add the parameter defined in {@code rs} 
	 * @param rs the data containing the parameter, as retrieved from the YADA Index
	 * @throws YADAFinderException when {@code rs} can't be processed
	 * @throws YADAQueryConfigurationException when default parameters are malformed
	 */
	
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
				String id     = rs.getString(YADA_PARAMID) != null ? rs.getString(YADA_PARAMID) : String.valueOf(0);
				YADAParam param = new YADAParam();
				param.setId(Integer.parseInt(id));
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
	 * 
	 * @param yq The {@link YADAQuery} to which to attach {@link YADAProperty} objects
	 * @param rs The {@link ResultSet} from which to retrieve the values
	 * @throws YADAFinderException if a property target can't be retrieveed
	 */
	private void setProperty(YADAQuery yq, ResultSet rs) throws YADAFinderException
	{
	  String target;
    try 
    {
      target = rs.getString(YADA_PROPTARGET);
      if(target != null && !"".equals(target))
      {
        String name  = rs.getString(YADA_PROPNAME);
        String value = rs.getString(YADA_PROPVALUE);
        YADAProperty prop = new YADAProperty(target,name,value);
        yq.addProperty(prop);
      }
    } 
    catch (SQLException e) 
    {
      String msg = "Unable to set properties.";
      throw new YADAFinderException(msg,e);
    }
	}
	
	/**
   * A utility method called in a separate thread by {@link Finder#getQuery(String)} to increment a query-access counter in the YADA Index.
   * 
   * @param qname the name of the query just requested
	 * @param accessCount the number of times a query has been access, derived from the cache  
   * @throws YADAConnectionException when the YADA Index can't be accessed
   * @throws YADAFinderException when {@code qname} can't be found in the YADA Index.
   * @see Finder#getQuery(String)
   * @since 8.1.1
   */
  void updateQueryStatistics(String qname, int accessCount) throws YADAConnectionException, YADAFinderException
  {

    l.debug("Updating Query Stats for ["+qname+"]");
    PreparedStatement pstmt = null;
    String querySql    = SQL_STATS_WCOUNT;
    try
    {
      Connection conn = ConnectionFactory.getConnectionFactory().getConnection(ConnectionFactory.YADA_APP);
      
      try
      {
        pstmt = conn.prepareStatement(querySql);
        pstmt.setInt(1,accessCount);
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
        String msg = "The update query caused an error. This could be because the query name ("+qname+") was mistyped or doesn't exist in the YADA Index";
        throw new YADAFinderException(msg,e);
      }
      
    }
    finally
    {
      ConnectionFactory.releaseResources(pstmt);
    }
  }
	
	/**
	 * A utility method called in a separate thread by {@link Finder#getQuery(String)} to increment a query-access counter in the YADA Index.
	 * 
	 * @param qname the name of the query just requested
	 * @throws YADAConnectionException when the YADA Index can't be accessed
	 * @throws YADAFinderException when {@code qname} can't be found in the YADA Index.
	 * @see Finder#getQuery(String)
	 * @deprecated since 8.1.1
	 */
  @Deprecated
	void updateQueryStatistics(String qname) throws YADAConnectionException, YADAFinderException
	{

		l.debug("Updating Query Stats for ["+qname+"]");
		PreparedStatement pstmt = null;
		String querySql    = SQL_STATS;
		try
		{
			Connection conn = ConnectionFactory.getConnectionFactory().getConnection(ConnectionFactory.YADA_APP);
			
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
				String msg = "The update query caused an error. This could be because the query name ("+qname+") was mistyped or doesn't exist in the YADA Index";
				throw new YADAFinderException(msg,e);
			}
			
		}
		finally
		{
			ConnectionFactory.releaseResources(pstmt);
		}
	}
}
