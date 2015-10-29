package com.novartis.opensource.yada;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;

/**
 * Provides for the creation and disposal of JDBC and SOAP connection 
 * objects configured using jndi.
 * 
 * @author David Varon
 * @since 0.1.0.0
 */
public class ConnectionFactory
{
	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(ConnectionFactory.class);
	
	/**
	 * Map of {@link Connection} to JNDI {@link String} to facilitate better logging
	 */
	private static Map<Connection,String> connectionMap = new HashMap<>();
	
	/**
	 * Creates a generic SOAP connection object to use for communication with an endpoint.
	 * 
	 * @return a {@link SOAPConnection} object on which to submit a request
	 * @throws YADAConnectionException when unable to obtain a connection 
	 */
	public static SOAPConnection getSOAPConnection() throws YADAConnectionException
	{
		SOAPConnectionFactory factory;
		SOAPConnection        connection = null;
		try 
		{
			factory    = SOAPConnectionFactory.newInstance();
			connection = factory.createConnection();
		} 
		catch (UnsupportedOperationException e) 
		{
			String msg = "There was a problem obtaining a SOAP ConnectionFactory instance.";
			throw new YADAConnectionException(msg,e);
		} 
		catch (SOAPException e) 
		{
			String msg = "There was a problem obtaining a SOAP Connection with the requested resource.";
			throw new YADAConnectionException(msg,e);
		}
		
		return connection;
	}
	
	/**
	 * Returns a JDBC connection from the datasource identified by {@code source}.
	 * 
	 * @param source the JNDI name of the datasource 
	 * @return {@link Connection} object to facilitate query execution
	 * @throws YADAConnectionException when the JNDI {@code source} is unrecognized or the database to which it refers is unreachable
	 */
	public static Connection getConnection(String source) throws YADAConnectionException
	{
		Connection connection = null;
		try
		{
			Context ctx;
			ctx = new InitialContext();
			DataSource ds  = (DataSource)ctx.lookup(source);
			connection = ds.getConnection();
			l.debug("source: ["+source+"], product: ["+connection.getMetaData().getDatabaseProductName()+"], driver: ["+connection.getMetaData().getDriverName()+"]");
		}
		catch (NamingException e)
		{
			String msg = "There was a problem locating the resource identified by the supplied JNDI path ["+ source +"] in the initial context."; 
			throw new YADAConnectionException(msg,e);
		} 
		catch (SQLException e) 
		{	
			String msg = "There was a problem obtaining a JDBC Connection to ["+source+"]. This could be caused by misconfiguration of the resource, recently changed credentials, or some other issue.";
			throw new YADAConnectionException(msg,e);
		}
		connectionMap.put(connection,source);
		return connection;
	}
	
	/**
	 * Retrievs the in-memory cache used to store requested {@link YADAQuery} objects.
	 * @param cacheManager the name of the cache manager, ({@code YADAIndexManager})
	 * @param cache the name of the desired cache ({@code YADAIndex}
	 * @return {@link Cache} object
	 * @since 0.4.1.0
	 */
	public static Cache getCacheConnection(String cacheManager, String cache) {
		CacheManager manager = CacheManager.getCacheManager(cacheManager);
		if(manager == null)
		  return null;
		return manager.getCache(cache);
	}
	
	/**
	 * Retrieves the {@link Statement} from the {@link ResultSet},
	 * closes the {@code ResultSet}, and then cascades to close the {@link java.sql.Statement} 
	 * (which in turn will cascade to close its {@link Connection}).  IF the 
	 * {@code Statement} can't be acquired, an attempt is still made to close the {@code ResultSet}
	 * @param rs the {@link ResultSet} recently iterated
	 * @throws YADAConnectionException when {@code rs} can't be closed 
	 * @see com.novartis.opensource.yada.ConnectionFactory#releaseResources(Statement)
	 */
	public static void releaseResources(ResultSet rs) throws YADAConnectionException
	{
		Statement  stmt = null;
		if (rs != null)
		{
			try 
			{
				stmt = rs.getStatement();
				rs.close();
			} 
			catch (SQLException e) 
			{
				String msg = "There was a problem closing the ResultSet.";
				l.warn(msg);
			}
		}
		if (stmt != null)
			releaseResources(stmt);
	}
	
	/**
	 * Retrieves the {@link Connection} from the {@link java.sql.Statement} parameter,
	 * closes the {@link Statement} and then cascades to close the {@link Connection}.  
	 * If the {@link Connection} can't be acquired, an attempt is still made to close the {@link Statement}
	 * 
	 * @param stmt the {@link Statement} recently executed ({@link java.sql.PreparedStatement} or {@link java.sql.CallableStatement})
	 * @throws YADAConnectionException when {@code stmt} can't be closed
	 */
	public static void releaseResources(Statement stmt) throws YADAConnectionException
	{
		Connection conn = null;
		if (stmt != null)
		{
			try
			{
				conn = stmt.getConnection();
				stmt.close();
			} 
			catch (SQLException e)
			{
				//String msg = "There was a problem closing the Statement.";
				//l.warn(msg);
			}
		}
		if(conn != null)
			releaseResources(conn);
	}
	
	/**
	 * Returns the {@link Connection}, specified by the parameter, to the connection pool.
	 * 
	 * @param conn - The {@link Connection} intended to be returned to the pool.
	 * @throws YADAConnectionException when {@code conn} can't be returned to the pool
	 */
	public static void releaseResources(Connection conn) throws YADAConnectionException
	{
		if (conn != null)
		{
			String source = connectionMap.get(conn);
			try
			{
				conn.close();
			} 
			catch (SQLException e)
			{
				String msg = "There was a problem closing the Connection. It may have already been closed.";
				throw new YADAConnectionException(msg, e);
			}
			finally
			{
				connectionMap.remove(conn);
			}
			l.debug("Database connection to ["+source+"] closed successfully.");
		}
	}
	
	/**
	 * Closes the {@link SOAPConnection} 
	 * @param conn the {@link SOAPConnection} object to close
	 * @param source the url string pointing to the soap endpoint
	 * @throws YADAConnectionException when the connection closing operation fails
	 */
	public static void releaseResources(SOAPConnection conn,String source) throws YADAConnectionException
	{
		if (conn != null)
		{
			try
			{
				conn.close();
			} 
			catch (SOAPException e)
			{
				String msg = "There was a problem closing the SOAPConnection. It may have already been closed.";
				throw new YADAConnectionException(msg, e);
			}
			finally
			{
				connectionMap.remove(conn);
			}
			l.debug("SOAPconnection to ["+source+"] closed successfully.");
		}
	}
}
