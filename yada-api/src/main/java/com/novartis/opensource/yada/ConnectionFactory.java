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
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPException;

import com.novartis.opensource.yada.util.QueryUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

import org.apache.log4j.Logger;

/**
 * Provides for the creation and disposal of JDBC and SOAP connection objects
 * configured using jndi.
 * 
 * @author David Varon
 * @since 1.0.0
 */
public class ConnectionFactory {
  /**
   * Local logger handle
   */
  private final static Logger l = Logger.getLogger(ConnectionFactory.class);
  
  /**
   * Constant equal to {@value}
   * @since 8.0.0
   */
  public  final static String YADA_APP       = "YADA";
  /**
   * Constant equal to {@value}
   * @since 8.0.0
   */
  private final static String YADA_DS_APP    = "APP";
  /**
   * Constant equal to {@value}
   * @since 8.0.0
   */
  private final static String YADA_DS_SOURCE = "SOURCE";
  /**
   * Constant equal to {@value}
   * @since 8.0.0
   */
  private final static String YADA_DS_CONF   = "CONF";
  /**
   * Constant equal to {@value}
   * @since 8.2.1
   */
  public final static String TYPE_JDBC      = "JDBC";
  /**
   * Constant equal to {@value}
   * @since 8.2.1
   */
  public final static String TYPE_URL       = "URL";
  
  
  /**
   * Constant equal to {@value}. Used for retrieving app configs.
   * @since 8.0.0
   */
  private final static String YADA_DS_SQL = "select "
                               + "a.app "+YADA_DS_APP+ ", "
                               + "a.source "+YADA_DS_SOURCE+ ", "
                               + "a.conf "+YADA_DS_CONF+ " "
                               + "from yada_query_conf a "
                               + "where a.app != '"+YADA_APP+"' ";
  /**
   * Constant equal to {@value}. Used for retrieving config for specific app.
   * @since 8.0.0
   */
  private final static String YADA_DS_WHERE = "and a.app = ?";               
  
  /**
   * Constant equal to {@value}. Used for retrieving config for specific YADA index.
   * @since 8.0.0
   */
  private final static String YADA_PROPERTIES_PATH = "YADA.properties.path";
  
  /**
   * Constant equal to {@value}. Used for retrieving config for specific YADA index.
   * @since 8.3.0
   */
  private final static String YADA_PROP_NAME = "N";
  
  /**
   * Constant equal to {@value}. Used for retrieving config for specific YADA index.
   * @since 8.3.0
   */
  private final static String YADA_PROP_VALUE = "V";
  
  /**
   * Constant equal to {@value}. Used for retrieving system properties
   * @since 8.3.0
   */
  private final static String YADA_SYS_PROP_SQL = "select " 
                              + "a.name "+YADA_PROP_NAME+", "
                              + "a.value "+YADA_PROP_VALUE+" "
                              + "from yada_prop a "
                              + "where lower(a.target) = 'system'";
  
  
  /**
   * Constant equal to {@value}. Default location for {@code YADA.properties} file, in {@code WEB-INF/classes}
   * @since 8.0.0
   */
  private final static String YADA_DEFAULT_PROP_PATH = "/YADA.properties";
  
  /**
   * Constant equal to {@value}. Enables comments in configs
   * @since 8.0.0
   */
  private final static String COMMENT = "#";
  
  
  /**
   * Map of {@link Connection} to JNDI {@link String} to facilitate better
   * logging
   */
  //private Map<Connection, String> connectionMap = new HashMap<>();
  
  /**
   * Map of {@link DataSource} objects to their names, as stored in the yada index.
   * @since 8.0.0
   */
  private Map<String, HikariDataSource> dataSourceMap = new HashMap<>();

  /**
   * Map of {@link String} urls to their names, as stored in the yada index.
   * @since 8.0.0
   */
  private Map<String, String> wsSourceMap = new HashMap<>();

  /**
   * The singleton instance of the class
   * @since 8.0.0
   */
  private static ConnectionFactory factory = null;
  
  static {
    try 
    {
      ConnectionFactory.getConnectionFactory().createDataSources();
      System.out.println("datasources created successfully");
    } 
    catch (Exception e) 
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
  
  /**
   * Private default constructor prohibits instantiation.
   * @since 8.0.0
   */
  private ConnectionFactory() {  }
  
  
  /**
   * Standard singleton lazy-initializer
   * @return the singleton instance of the class
   * @since 8.0.0
   */
  public synchronized static ConnectionFactory getConnectionFactory() {
    if(factory == null)
      factory = new ConnectionFactory();
    return factory;
  }

 
  /**
   * Creates a generic SOAP connection object to use for communication with an
   * endpoint.
   * 
   * @return a {@link SOAPConnection} object on which to submit a request
   * @throws YADAConnectionException when unable to obtain a connection
   */
  public SOAPConnection getSOAPConnection()
      throws YADAConnectionException {
    SOAPConnectionFactory factory;
    SOAPConnection connection = null;
    try {
      factory = SOAPConnectionFactory.newInstance();
      connection = factory.createConnection();
    } catch (UnsupportedOperationException e) {
      String msg = "There was a problem obtaining a SOAP ConnectionFactory instance.";
      throw new YADAConnectionException(msg, e);
    } catch (SOAPException e) {
      String msg = "There was a problem obtaining a SOAP Connection with the requested resource.";
      throw new YADAConnectionException(msg, e);
    }

    return connection;
  }
  
  /**
   * Pulls the YADA connection pool out of the {@link #dataSourceMap} and returns a connection.
   * If the datasource has not yet been created, it will be.
   * 
   * @return a {@link Connection} object from the {@link DataSource} connection pool
   * @throws YADAConnectionException when the YADAIndex data source cannot provide a connection
   * @since 8.0.0
   */
  private Connection getYADAConnection() throws YADAConnectionException 
  { 
    Connection yadaConn = null;
    HikariDataSource yadaDs = this.getDataSourceMap().get(YADA_APP);
    if(yadaDs == null)
    {
      String path = System.getProperty(YADA_PROPERTIES_PATH);
      if(path == null || "".equals(path))
        path = YADA_DEFAULT_PROP_PATH;
      
      HikariConfig config = new HikariConfig(path);
      yadaDs = new HikariDataSource(config);
      this.getDataSourceMap().put(YADA_APP, yadaDs);
      this.loadSystemProperties();
    }
    try 
    {
      yadaConn = yadaDs.getConnection();
    } 
    catch (SQLException e) 
    {
      String msg = "Could not retrieve connection from datasource.";
      throw new YADAConnectionException(msg, e);
    }
    return yadaConn;
  }
  
  /**
   * Loads all the properties from the YADA Index where {@code target = 'system'} (case insensitive)
   * @throws YADAConnectionException when the properties can not be retrieved from the YADA index
   * @since 8.3.0
   */
  private void loadSystemProperties() throws YADAConnectionException 
  {
    try(Connection yadaConn  = this.getYADAConnection(); 
        PreparedStatement pstmt = yadaConn.prepareStatement(YADA_SYS_PROP_SQL);)
    {
      try(ResultSet rs = pstmt.executeQuery();)
      {
        if(!rs.isBeforeFirst())
        {
          String msg = "There was an issue retrieving the property list";
          throw new YADAConnectionException(msg);
        }
        while (rs.next())
        {
          String key = rs.getString(YADA_PROP_NAME);
          String value = rs.getString(YADA_PROP_VALUE);
          System.setProperty(key, value);
        }
      }
      catch (SQLException e)
      {
        String msg = "The lookup query caused an error. This could be because the service is misconfigured.";
        throw new YADAConnectionException(msg,e);
      }
    } 
    catch (SQLException e)
    {
      String msg = "Unable to create or configure the PreparedStatement used to lookup the system properties in the YADA Index.  This could be a serious configuration issue.";
      throw new YADAConnectionException(msg,e);
    }
    
  }
  
  /**
   * Called from an initializer or static block, this method will retrieve the datasource configs from
   * the YADA index and store them in the {@link #dataSourceMap}.
   * @throws YADAConnectionException if the connection to YADA index is closed or otherwise problematic
   * @since 8.0.0 
   */
  public void createDataSources() throws YADAConnectionException 
  {
    getYADAConnection();
    Connection yadaConn  = this.getYADAConnection();
    PreparedStatement pstmt;
    Map<String,String> conf = new HashMap<>();
    ResultSet rs = null;
    try
    {
      pstmt = yadaConn.prepareStatement(YADA_DS_SQL);
    } 
    catch (SQLException e)
    {
      String msg = "Unable to create or configure the PreparedStatement used to lookup the datasource configs in the YADA Index.  This could be a serious configuration issue.";
      throw new YADAConnectionException(msg,e);
    }
    try
    {
      rs = pstmt.executeQuery();
      if(!rs.isBeforeFirst())
      {
        String msg = "There was an issue retrieving the app list";
        throw new YADAConnectionException(msg);
      }
      while (rs.next())
      {
        String confStr = rs.getString(YADA_DS_CONF);
        conf.put(YADA_DS_APP, rs.getString(YADA_DS_APP));
        conf.put(YADA_DS_SOURCE, rs.getString(YADA_DS_SOURCE));
        conf.put(YADA_DS_CONF, confStr);
        if(confStr != null)
        {
          if(confStr.matches(QueryUtils.RX_JDBC_CONF))
          {
            this.createJdbcDataSource(conf);
          }
          else
          {
            this.createWsDataSource(conf);
          }
        }
      }
    }
    catch (SQLException e)
    {
      String msg = "The lookup query caused an error. This could be because the service is misconfigured.";
      throw new YADAConnectionException(msg,e);
    }
    finally 
    {
      releaseResources(rs);
    }
  }

  /**
   * Stores the webservice url in the {@link #wsSourceMap}
   * @param conf the webservice configuration object to parse
   */
  public void createWsDataSource(Map<String,String> conf)
  {
    String app = conf.get(YADA_DS_APP);
    if(app != null && !"".equals(app))
    {
      if(this.getWsSourceMap().get(app) == null)
      {
        String url = conf.get(YADA_DS_CONF);
        if(url == null || "".equals(url))
          url = conf.get(YADA_DS_SOURCE);
        this.getWsSourceMap().put(app,url);
        l.debug(app+" : "+url);
      }
    }
  }
  
  /**
   * Returns the app type, either {@link #TYPE_JDBC} or {@link #TYPE_URL} to facilitate apdaptor loading
   * @param app the app code 
   * @return the app type
   * @since 8.2.1
   */
  public String getAppConnectionType(String app) {
    if(this.getDataSourceMap().get(app) != null)
      return TYPE_JDBC;
    else if(this.getWsSourceMap().get(app) != null)
      return TYPE_URL;
    return null;
  }
  
  /**
   * Creates and stores a datasource in the {@link #dataSourceMap}
   * @param conf {@link Map} containing datasource configs from database
   * @since 8.0.0
   */
  public void createJdbcDataSource(Map<String,String> conf) 
  {
    String app = conf.get(YADA_DS_APP);
    if(app != null && !"".equals(app))
    {
      if(this.getDataSourceMap().get(app) == null)
      {
        Properties props = new Properties();
        String propStr = conf.get(YADA_DS_CONF);
        String lines[] = propStr.split("\\r?\\n");
        for(String line : lines)
        {
          if(!line.startsWith(COMMENT))
          {
            String[] pair = line.split("=",2);
            props.put(pair[0], pair[1]);
          }
        }
        props.put("poolName","HikariPool-"+app);
        
        try
        {
          HikariConfig config = new HikariConfig(props);
          HikariDataSource datasource = new HikariDataSource(config);
          this.getDataSourceMap().put(app, datasource);
        }
        catch(Exception e)
        {
          String msg = "Could not create connection pool for "+app;
          l.warn(msg);
        }
      }
    }
  }
  
  /**
   * Close a connection pool
   * @param app the name of the datasource
   * @return the name of the connection pool that was closed
   * @since 8.4.0
   */
  public String closePool(String app)
  {
    HikariDataSource ds = this.getDataSourceMap().get(app); 
    String pool = ds.getPoolName();
    ds.close();
    return pool;
  }

  /**
   * Returns a JDBC connection from the datasource identified by {@code app}. <strong>Updated</strong>
   * in 8.0.0 to retrieve connections from {@link DataSource} objects stored in {@link #dataSourceMap}
   * rather than the JNDI context.
   * 
   * @param app the name of the datasource
   * @return {@link Connection} object to facilitate query execution
   * @throws YADAConnectionException when the JNDI {@code source} is unrecognized or the database to
   *           which it refers is unreachable
   */
  public Connection getConnection(String app) throws YADAConnectionException 
  {
    if(app.equals(YADA_APP))
    {
      return getYADAConnection();
    }
    
    Connection connection   = null;
    ResultSet  rs           = null;
    DataSource ds           = this.getDataSourceMap().get(app);
    Map<String,String> conf = new HashMap<>();
    
    try 
    {  
      if(ds == null)
      {
        
        PreparedStatement pstmt;
        try
        {
          pstmt = getYADAConnection().prepareStatement(YADA_DS_SQL + YADA_DS_WHERE);
          pstmt.setString(1,app);
        } 
        catch (SQLException e)
        {
          String msg = "Unable to create or configure the PreparedStatement used to lookup the requested app in the YADA Index.  This could be a serious configuration issue.";
          throw new YADAConnectionException(msg,e);
        }
        int row = 0;
        try
        {
          rs = pstmt.executeQuery();
          if(!rs.isBeforeFirst())
          {
            String msg = "The requested app ["+app+"] does not exist.";
            throw new YADAConnectionException(msg);
          }
          while (rs.next() && row == 0)
          {
            conf.put(YADA_DS_APP, rs.getString(YADA_DS_APP));
            conf.put(YADA_DS_SOURCE, rs.getString(YADA_DS_SOURCE));
            conf.put(YADA_DS_CONF, rs.getString(YADA_DS_CONF));
            row++;
          }
          this.createJdbcDataSource(conf);
          ds = this.getDataSourceMap().get(app);
        }
        catch (SQLException e)
        {
          String msg = "The lookup query caused an error. This could be because the app ("+app+") is misconfigured or doesn't exist in the YADA Index";
          throw new YADAConnectionException(msg,e);
        }
      }
      connection = ds.getConnection();
      l.debug("app: [" + app + "], product: ["
          + connection.getMetaData().getDatabaseProductName() + "], driver: ["
          + connection.getMetaData().getDriverName() + "]");
//    } catch (NamingException e) {
//      String msg = "There was a problem locating the resource identified by the supplied JNDI path ["
//          + Finder.getYADAJndi() + "] in the initial context.";
//      throw new YADAConnectionException(msg, e);
    } catch (SQLException e) {
      String msg = "There was a problem obtaining a JDBC Connection to ["
          + Finder.getYADAJndi()
          + "]. This could be caused by misconfiguration of the resource, recently changed credentials, or some other issue.";
      throw new YADAConnectionException(msg, e);
    }
    //this.connectionMap.put(connection, conf.get(YADA_DS_SOURCE));
    return connection;
  }

  
  
  /**
   * Retrieves the in-memory cache used to store requested {@link YADAQuery}
   * objects.
   * 
   * @param cacheManager
   *          the name of the cache manager, ({@code YADAIndexManager})
   * @param cache
   *          the name of the desired cache ({@code YADAIndex}
   * @return {@link Cache} object
   * @since 4.1.0
   */
  public Cache getCacheConnection(String cacheManager, String cache) {
    CacheManager manager = CacheManager.getCacheManager(cacheManager);
    if (manager == null)
      return null;
    return manager.getCache(cache);
  }

  /**
   * @return the dataSourceMap
   * @since 8.0.0
   */
  public Map<String, HikariDataSource> getDataSourceMap() {
    return this.dataSourceMap;
  }


  /**
   * @return the wsSourceMap
   * @since 8.0.0
   */
  public Map<String, String> getWsSourceMap() {
    return this.wsSourceMap;
  }

  /**
   * Retrieves the {@link Statement} from the {@link ResultSet}, closes the
   * {@code ResultSet}, and then cascades to close the
   * {@link java.sql.Statement} (which in turn will cascade to close its
   * {@link Connection}). IF the {@code Statement} can't be acquired, an attempt
   * is still made to close the {@code ResultSet}
   * 
   * @param rs the {@link ResultSet} recently iterated
   * @throws YADAConnectionException when {@code rs} can't be closed
   * @see com.novartis.opensource.yada.ConnectionFactory#releaseResources(Statement)
   */
  public static void releaseResources(ResultSet rs)
      throws YADAConnectionException {
    Statement stmt = null;
    if (rs != null) {
      try {
        stmt = rs.getStatement();
        rs.close();
      } catch (SQLException e) {
        String msg = "There was a problem closing the ResultSet.";
        l.warn(msg);
      }
    }
    if (stmt != null)
      releaseResources(stmt);
  }

  /**
   * Retrieves the {@link Connection} from the {@link java.sql.Statement}
   * parameter, closes the {@link Statement} and then cascades to close the
   * {@link Connection}. If the {@link Connection} can't be acquired, an attempt
   * is still made to close the {@link Statement}
   * 
   * @param stmt the {@link Statement} recently executed (
   *          {@link java.sql.PreparedStatement} or
   *          {@link java.sql.CallableStatement})
   * @throws YADAConnectionException when {@code stmt} can't be closed
   */
  public static void releaseResources(Statement stmt)
      throws YADAConnectionException {
    Connection conn = null;
    if (stmt != null) {
      try {
        conn = stmt.getConnection();
        stmt.close();
      } catch (SQLException e) {
        // String msg = "There was a problem closing the Statement.";
        // l.warn(msg);
      }
    }
    if (conn != null)
      releaseResources(conn);
  }

  /**
   * Returns the {@link Connection}, specified by the parameter, to the
   * connection pool.
   * 
   * @param conn The {@link Connection} intended to be returned to the pool.
   * @throws YADAConnectionException when {@code conn} can't be returned to the pool
   */
  public static void releaseResources(Connection conn)
      throws YADAConnectionException {
    if (conn != null) {
//      String source = getConnectionFactory().connectionMap.get(conn);
      try {
        conn.close();
      } catch (SQLException e) {
        String msg = "There was a problem closing the Connection. It may have already been closed.";
        throw new YADAConnectionException(msg, e);
      } finally {
        //getConnectionFactory().connectionMap.remove(conn);
      }
//      l.debug("Database connection to [" + source + "] closed successfully.");
    }
  }

  /**
   * Closes the {@link SOAPConnection}
   * 
   * @param conn the {@link SOAPConnection} object to close
   * @param source the url string pointing to the soap endpoint
   * @throws YADAConnectionException when the connection closing operation fails
   */
  public static void releaseResources(SOAPConnection conn, String source)
      throws YADAConnectionException {
    if (conn != null) {
      try 
      {
        conn.close();
      } 
      catch (SOAPException e) 
      {
        String msg = "There was a problem closing the SOAPConnection. It may have already been closed.";
        throw new YADAConnectionException(msg, e);
      } 
      l.debug("SOAPconnection to [" + source + "] closed successfully.");
    }
  }
}
