package com.novartis.opensource.yada.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.config.CacheConfiguration;

import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAConnectionException;

/**
 * Test class responsible for validating {@link ConnectionFactory} methods, and also for setup of JNDI context necessary for testing.
 * @author David Varon
 * @since 0.4.0.0
 */
public class ConnectionFactoryTest {

	/**
	 * Instance variable to hold the connection object
	 */
	private Connection connection;

	/**
	 * Container of properties, mainly passwords, which shouldn't be hardcoded. 
	 */
	private static Properties props = new Properties();
	
	/**
	 * Test prep method which creates and populates a local JNDI context to facilitate testing independently of Tomcat.
	 * @param properties the path to the properties file, expected to be set in the TestNG xml config file.
	 */
	@Parameters({"properties"})
	@BeforeSuite(alwaysRun = true)
	public void init(String properties) {
		try 
		{
			// load properties
			setProps(properties);
			BasicDataSource ds = new BasicDataSource();
			try
			{
				Class<?> dsClass = Class.forName(ds.getClass().getName());
				Set<Method> methods  = new HashSet<>();
				Set<String> idxProps = new HashSet<>();
 				// setters
				for(Method method : dsClass.getMethods())
				{
					if(method.getName().startsWith("set"))
						methods.add(method);
				}
 				// YADA.index properties
 				for(String prop : getProps().keySet().toArray(new String[getProps().size()]))
 				{
 					if(prop.startsWith("YADA.index"))
 					{
 						idxProps.add(prop.split(Pattern.quote("."))[2]);
 					}
 				}
 				// call setters with properties
				for(String prop : idxProps.toArray(new String[idxProps.size()]))
				{
					for(Method method : methods.toArray(new Method[methods.size()]))
					{
		    			if(method.getName().toLowerCase().endsWith(prop.toLowerCase()))
		    			{
		    				method.setAccessible(true);
		    				String type = method.getParameterTypes()[0].getName();
		    				String val  = (String)getProps().get("YADA.index."+prop);
		    				if(type.endsWith("String"))
		    				{
		    					method.invoke(ds, new Object[]{val});
		    				}
		    				else if(type.endsWith("Boolean"))
		    				{
		    					method.invoke(ds, new Object[]{Boolean.valueOf(val)});
		    				}
		    				else if(type.endsWith("Integer"))
		    				{
		    					method.invoke(ds, new Object[]{Integer.valueOf(val)});
		    				}
		    			}
			    	}
		    	}
			} 
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
			} 
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
			} 
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			} 
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
			}
			            
      InitialContext ic = new InitialContext();
      try
      {
	      ic.createSubcontext("java:comp");
	      ic.createSubcontext("java:comp/env");
	      ic.createSubcontext("java:comp/env/jdbc");
	      ic.createSubcontext("java:comp/env/adaptor");
	      ic.createSubcontext("java:comp/env/io");
	      ic.bind("java:comp/env/jdbc/yada",ds);
	      ic.bind("java:comp/env/adaptor/"+getProps().get("YADA.index.driverClassName"), getProps().get("YADA.index.adaptor"));
	      ic.bind("java:comp/env/yada_bin", getProps().get("YADA.bin"));
	      ic.bind("java:comp/env/io/in", getProps().get("YADA.io.in"));
	      ic.bind("java:comp/env/io/out", getProps().get("YADA.io.out"));
	      ic.bind("java:comp/env/yada_version", getProps().get("YADA.version"));
      }
      catch(NamingException e)
      {
      	e.printStackTrace();
      }
      // Init cache
      String file = "ehcache.xml";
      try
      {
	      URL    url  = getClass().getClassLoader().getResource(file);
	      if (url != null)
	      {
	    	  CacheManager.create(url);
	      }
	      else
	      {
	        CacheManager YADAIndexManager = CacheManager.create();
	        Cache YADAIndex = new Cache(new CacheConfiguration("YADAIndex",0).eternal(true));
	        YADAIndexManager.addCache(YADAIndex);
	      }
      } 
      catch(CacheException e)
      {
    	  CacheManager YADAIndexManager = CacheManager.create(getClass().getClassLoader().getResourceAsStream("/"+file));
    	  Cache YADAIndex = new Cache(new CacheConfiguration("YADAIndex",0).eternal(true));
    	  YADAIndexManager.addCache(YADAIndex);
      }
		}    
		catch (Exception e)
		{
			//e.printStackTrace();
			
		}
	}
	
  /**
   * Tests {@link ConnectionFactory#getConnection(String)} by attempting to connect to the YADA Index.
   * @throws YADAConnectionException when the connection can't be opened
   */
  @Test (groups = {"core"})
  public void getConnection() throws YADAConnectionException {
    this.connection = ConnectionFactory.getConnection("java:comp/env/jdbc/yada");
  }
  
  /**
   * Tests exception handling in {@link ConnectionFactory#getConnection(String)} by attempting to connect to a non-existent JNDI string.
   * The test is successful if a {@link YADAConnectionException} is thrown.
   * @throws YADAConnectionException when the connection can't be opened
   */
  @Test (groups = {"core"}, expectedExceptions=YADAConnectionException.class)
  public void getUnknownConnectionFail() throws YADAConnectionException 
  {
  	ConnectionFactory.getConnection("jdbc/yomama");
  }

  /**
   * Tests {@link ConnectionFactory#getSOAPConnection()} by opening a generic {@link javax.xml.soap.SOAPConnection}
   * The test is successful if no exception is thrown.
   * @throws YADAConnectionException when the connection can't be opened
   */
	@Test (groups = {"core"})
  public void getSOAPConnection() throws YADAConnectionException {
    //soapConnection = 
    ConnectionFactory.getSOAPConnection();
  }
  
  /**
   * Tests YADA clean-up method {@link ConnectionFactory#releaseResources(Connection)}.
   * The test is successful if no exception is thrown.
   * @throws YADAConnectionException when the connection can't be opened
   */
  //@AfterSuite (groups = {"core", "json","standard","api","jsp","options","plugins"})
	@AfterSuite(alwaysRun = true)
  public void releaseResources() throws YADAConnectionException 
  {
  	ConnectionFactory.releaseResources(this.connection);
  }

	/**
	 * @return the props
	 */
	public static Properties getProps()
	{
		return props;
	}

	/**
	 * For loading properties
	 * @param props the props to set
	 */
	public void setProps(Properties props)
	{
		ConnectionFactoryTest.props = props;
	}
  
	/**
	 * For loading properties from disk
	 * @param properties the properties file
	 * @throws IOException when the file can't be loaded
	 */
	@Parameters({"properties"})
	@BeforeSuite(alwaysRun = true)
	public static void setProps(String properties) throws IOException 
	{		
		if(!"".equals(properties))
		{
			try(InputStream fis = ConnectionFactoryTest.class.getResourceAsStream(properties)) 
			{
			  props.load(fis);
			}
		}
	}
}
