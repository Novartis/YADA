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

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.YADAMarkupParameter;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ItemsList;
import net.sf.jsqlparser.expression.operators.relational.MultiExpressionList;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.ValuesList;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.Parser;
import com.novartis.opensource.yada.QueryManager;
import com.novartis.opensource.yada.YADAConnectionException;
import com.novartis.opensource.yada.YADAParserException;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.YADAUnsupportedAdaptorException;
import com.novartis.opensource.yada.adaptor.Adaptor;
import com.novartis.opensource.yada.adaptor.FileSystemAdaptor;
import com.novartis.opensource.yada.adaptor.JDBCAdaptor;
import com.novartis.opensource.yada.adaptor.RESTAdaptor;
import com.novartis.opensource.yada.adaptor.SOAPAdaptor;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Utilities for query string manipulation and accounting.
 * 
 * @since 4.0.0
 * @author David Varon
 * 
 */
public class QueryUtils
{

	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(QueryUtils.class);
	/**
	 * A constant equal to: {@value}
	 * @since 9.1.0
	 */
	public static final String KEY_ADAPTOR = "adaptor";
	/**
   * A constant equal to: {@value}
   * @since 9.1.0
   */
  public static final String KEY_EXTENSION = "extn";
	/**
	 * A constant equal to: {@code com.novartis.opensource.yada.adaptor.JDBCAdaptor}
	 */
	public static final String JDBC_ADAPTOR_CLASS_NAME = JDBCAdaptor.class.getName();
	/**
	 * A constant equal to the class {@code com.novartis.opensource.yada.adaptor.JDBCAdaptor}
	 */
	public static final Class<JDBCAdaptor> JDBC_ADAPTOR_CLASS = JDBCAdaptor.class;
	/**
	 * A constant equal to: {@code com.novartis.opensource.yada.adaptor.SOAPAdaptor}
	 */
	public static final String SOAP_ADAPTOR_CLASS_NAME = SOAPAdaptor.class.getName();
	/**
	 * A constant equal to the class {@code com.novartis.opensource.yada.adaptor.SOAPAdaptor}
	 */
	public static final Class<SOAPAdaptor> SOAP_ADAPTOR_CLASS = SOAPAdaptor.class;
	/**
	 * A constant equal to: {@code com.novartis.opensource.yada.adaptor.RESTAdaptor}
	 */
	public static final String REST_ADAPTOR_CLASS_NAME = RESTAdaptor.class.getName();
	/**
	 * A constant equal to the class {@code com.novartis.opensource.yada.adaptor.RESTAdaptor}
	 */
	public static final Class<RESTAdaptor> REST_ADAPTOR_CLASS = RESTAdaptor.class;
	/**
	 * A constant equal to: {@code com.novartis.opensource.yada.adaptor.FileSystemAdaptor}
	 */
	public static final String FILESYSTEM_ADAPTOR_CLASS_NAME = FileSystemAdaptor.class.getName();
	/**
	 * A constant equal to the class {@code com.novartis.opensource.yada.adaptor.FileSystemAdaptor}
	 */
	public static final Class<FileSystemAdaptor> FILESYSTEM_ADAPTOR_CLASS = FileSystemAdaptor.class;
	/**
	 * A constant equal to: {@value}
	 */
	public static final String RX_FILE = "^file:.+$";
	/**
	 * A constant equal to: {@value}
	 * @since 8.0.0
	 */
	public static final String RX_JDBC_JNDI = "^java:.+/jdbc/.+$";
	/**
   * A constant equal to: {@value}. {@code (?s)} means "dot matches newline for rest of regex.
   * @since 8.0.0
   */
  public static final String RX_JDBC_CONF = "(?s).*jdbcUrl=jdbc:.+";
	/**
   * A constant equal to: {@value}
   */
  public static final String RX_JDBC = "^jdbc:.+$";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String RX_SOAP = "^soaps?:.+$";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String RX_CALLABLE = "^call .+\\(.*\\)\\s*$";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String RX_SELECT = "^SELECT.*";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String RX_INSERT = "^INSERT.*";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String RX_UPDATE = "^UPDATE.*";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String RX_DELETE = "^DELETE.*";
	/**
	 * A constant equal to: {@code ^([^&lt;]+)((&lt;&lcub;1,2&rcub;)((?!rm|mkdir).+))*$ } 
	 */
	public final static String RX_FILE_URI = "^([^<]+)((<{1,2})((?!rm|mkdir).+))?$";
	/**
   * A constant equal to: {@code ^[^<]+<mkdir$ }
   */
  public final static String RX_FILE_MKDIR = "^[^<]+<mkdir$";
  /**
   * A constant equal to: {@code ^[^<]+<rm$ }
   * @since 9.0.3
   */
  public final static String RX_FILE_RM = "^[^<]+<rm$";
  /**
   * A constant equal to: {@code \\(?([^)]+)\\)?,?}
   * @since 9.3.6
   */
  public final static Pattern RX_VALUES_PARAM_STRING = Pattern.compile("(\\([^)]+\\),?)");
  /**
   * A constant equal to: {@code \\(?([^)]+)\\)?,?}
   * @since 9.3.6
   */
  public final static Pattern RX_VALUES_PARAM_SINGLE = Pattern.compile("\\(?([^),]+)\\)?,?");
  /**
   * A constant equal to: {@code \\(?([^)]+)\\)?,?}
   * @since 9.3.6
   */
  public final static Pattern RX_VALUES_PARAM_LEFT = Pattern.compile("\\(?([^),]+),");
  /**
   * A constant equal to: {@code \\(?([^)]+)\\)?,?}
   * @since 9.3.6
   */
  public final static Pattern RX_VALUES_PARAM_RIGHT = Pattern.compile("([^(,]+)\\)");
	/**
	 * A constant equal to: {@value}
	 * @since 9.2.0
	 */
	public static final String READ = "";
	/**
	 * A constant equal to: {@code &lt;}
	 */
	public static final String WRITE = "<";
	/**
	 * A constant equal to: {@code &lt;&lt;}
	 */
	public static final String APPEND = "<<";
	/**
   * A constant equal to: {@code RM}
   * @since 9.0.3
   */
  public static final String RM = "RM";
  /**
   * A constant equal to: {@code MKDIR}
   * @since 9.0.3
   */
  public static final String MKDIR = "MKDIR";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String YADA_COLUMN = "YADA_";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String NEWLINE = "\n";
	/**
	 * A constant equal to: {@value}
	 */
	public static final char DATE = 'd';
	/**
	 * A constant equal to: {@value}
	 */
	public static final char INTEGER = 'i';
	/**
	 * A constant equal to: {@value}
	 */
	public static final char NUMBER = 'n';
	/**
	 * A constant equal to: {@value}
	 */
	public static final char VARCHAR = 'v';
	/**
	 * A constant equal to: {@value}
	 */
	public static final char OUTPARAM_DATE = 'D';
	/**
	 * A constant equal to: {@value}
	 */
	public static final char OUTPARAM_INTEGER = 'I';
	/**
	 * A constant equal to: {@value}
	 */
	public static final char OUTPARAM_NUMBER = 'N';
	/**
	 * A constant equal to: {@value}
	 */
	public static final char OUTPARAM_VARCHAR = 'V';
	/**
	 * A constant equal to: {@value}
	 */
	public static final String ORACLE_DATE_FMT = "dd-MMM-yy";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String STANDARD_DATE_FMT = "yyyy-MM-dd";
	/**
	 * A constant equal to: {@value}
	 */
	public static final String QUOTE = "\"";

	/**
	 * Retrieves the adaptor class from the application context given the
	 * parameter values.
	 * 
	 * @param source the JNDI string or url mapped to the query's app in the YADA index
	 * @param version the version of the framework, for selection of the proper adaptor
	 * @return the {@link Class} of the appropriate adaptor
	 * @throws YADAResourceException when {@code source} can't be found, or there is an issue with the
	 *           application context
	 * @throws YADAUnsupportedAdaptorException when the adaptor class mapped to {@code source} can't be found
	 * @deprecated since 8.0.0           
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
  public Class<Adaptor> getAdaptorClass(String source, String version) throws YADAResourceException, YADAUnsupportedAdaptorException
	{
		String driverName = "";
		String className = REST_ADAPTOR_CLASS_NAME;
		l.debug("JNDI source is [" + source + "]");
		if (source.matches(RX_JDBC_JNDI))
		{
			Context ctx;
			try
			{
				ctx = new InitialContext();
			} 
			catch (NamingException e)
			{
				String msg = "Could not create context.";
				throw new YADAResourceException(msg, e);
			}
			DataSource ds;
			try
			{
				ds = (DataSource)ctx.lookup(source);
			} 
			catch (NamingException e)
			{
				String msg = "Could not find data source at " + source;
				throw new YADAResourceException(msg, e);
			}
			
			//TODO add integration tests for multiple containers after breaking tomcat dbcp dependency
			//     http://docs.oracle.com/javase/1.5.0/docs/api/java/sql/DriverManager.html?is-external=true
			driverName = ((HikariDataSource)ds).getDriverClassName();
			l.debug("JDBC driver is [" + driverName + "]");
			className = Finder.getEnv("adaptor/" + driverName + version);
		} 
		else if (source.matches(RX_SOAP))
		{
			className = SOAP_ADAPTOR_CLASS_NAME;
		} 
		else if (source.matches(RX_FILE))
		{
			className = FILESYSTEM_ADAPTOR_CLASS_NAME;
		}
		l.debug("JDBCAdaptor class is [" + className + "]");

		Class<Adaptor> adaptorClass;
		try
		{
			adaptorClass = (Class<Adaptor>)Class.forName(className);
		} 
		catch (ClassNotFoundException e)
		{
			String msg = "Could not find appropriate adaptor class";
			throw new YADAUnsupportedAdaptorException(msg, e);
		} 
		catch (NoClassDefFoundError e)
		{
			String msg = "Could not find appropriate adaptor class";
			throw new YADAUnsupportedAdaptorException(msg, e);
		}
		return adaptorClass;
	}

	/**
	 * 
	 * @param app the query's APP code
	 * @return Class of type Adaptor mapped to the {@code source}
	 * @throws YADAResourceException when {@code source} can't be found, or there is an issue with the application context
	 * @throws YADAUnsupportedAdaptorException when the adaptor class mapped to {@code source} can't be found
	 * @throws YADAConnectionException if a new datasource connection pool cannot be established or stored
	 */
	@SuppressWarnings("unchecked")
	public Class<Adaptor> getAdaptorClass(String app) throws YADAResourceException, YADAUnsupportedAdaptorException, YADAConnectionException
	{
	  String driverName = "";
    String className = REST_ADAPTOR_CLASS_NAME;
    ConnectionFactory factory = ConnectionFactory.getConnectionFactory(); 
    String type = factory.getAppConnectionType(app);
    if(type == null)
    {
      factory.createDataSources();
      type = factory.getAppConnectionType(app);
    }
    
    if(type.equals(ConnectionFactory.TYPE_JDBC))
    {
      HikariDataSource ds = factory.getDataSourceMap().get(app);
      driverName = ds.getDriverClassName();
      className = Finder.getEnv("adaptor/" + driverName);
    }
    else if(type.equals(ConnectionFactory.TYPE_URL))
    {
      Properties props = ConnectionFactory.getConnectionFactory().getWsSourceMap().get(app);    
      String url  = props.getProperty(ConnectionFactory.YADA_CONF_SOURCE); 
      if (url.matches(RX_SOAP))
      {
        className = SOAP_ADAPTOR_CLASS_NAME;
      } 
      else if (url.matches(RX_FILE))
      {
        // interrogate url and or configuration object to obtain adaptor class or file extn
        if(props.containsKey(KEY_ADAPTOR))
        {
          className = props.getProperty(KEY_ADAPTOR);
        }
        else if(props.containsKey(KEY_EXTENSION))
        {
          
          className = Finder.getEnv(props.getProperty(KEY_EXTENSION.toLowerCase()));
        }
        else
        {
          className = FILESYSTEM_ADAPTOR_CLASS_NAME;
        }
      }
      // else default is REST, set up top
    }
    
    l.debug("JDBCAdaptor class is [" + className + "]");
    
    Class<Adaptor> adaptorClass;
    try
    {
      adaptorClass = (Class<Adaptor>)Class.forName(className);
    } 
    catch (ClassNotFoundException e)
    {
      String msg = "Could not find appropriate adaptor class";
      throw new YADAUnsupportedAdaptorException(msg, e);
    } 
    catch (NoClassDefFoundError e)
    {
      String msg = "Compiled adaptor class could not be loaded";
      throw new YADAUnsupportedAdaptorException(msg, e);
    }
    return adaptorClass;
	}

	/**
	 * 
	 * @param query the query object to process
	 * @return Class of type JDBCAdaptor mapped to the source string stored in the YADAQuery object
	 * @throws YADAResourceException when the source stored in query can't be found, or there is an issue with the application context
	 * @throws YADAUnsupportedAdaptorException when the adaptor class mapped to the source stored in the query can't be found
	 */
	public Class<Adaptor> getAdaptorClass(YADAQuery query) throws YADAResourceException, YADAUnsupportedAdaptorException
	{
		return getAdaptorClass(query.getSource(), query.getVersion());
	}

	/**
	 * Returns an instance of {@code adaptorClass} using the "YADARequest"
	 * constructor.
	 * 
	 * @param adaptorClass Class of type JDBCAdaptor mapped to the source string stored in
	 *          the YADAQuery object
	 * @param yadaReq request config
	 * @return an adaptor instance of the provided class, with including the
	 *         service parameters
	 * @throws YADAUnsupportedAdaptorException when {@code adaptorClass} cannot be instantiated
	 */
	public Adaptor getAdaptor(Class<Adaptor> adaptorClass, YADARequest yadaReq) throws YADAUnsupportedAdaptorException
	{
		try
		{
			return adaptorClass.getConstructor(YADARequest.class)
													.newInstance(yadaReq);
		} catch (InstantiationException e)
		{
			String msg = "Error instanting adaptor for class " + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		} catch (IllegalAccessException e)
		{
			String msg = "Error instanting adaptor for class " + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		} 
		catch (IllegalArgumentException e)
		{
			String msg = "Error instanting adaptor for class " + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		} 
		catch (SecurityException e)
		{
			String msg = "Error instanting adaptor for class " + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		} 
		catch (InvocationTargetException e)
		{
			String msg = "Error instanting adaptor for class " + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		} 
		catch (NoSuchMethodException e)
		{
			String msg = "Error instanting adaptor for class  s" + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		}

	}

	/**
	 * Returns an instance of {@code adaptorClass} using the no-arg constructor.
	 * 
	 * @param adaptorClass the class name of the YADA adaptor associated to the query
	 * @return an instance of the provided class
	 * @throws YADAUnsupportedAdaptorException when the {@code adaptorClass} cannot be instantiated
	 */
	public Adaptor getAdaptor(Class<Adaptor> adaptorClass) throws YADAUnsupportedAdaptorException
	{
		try
		{
			return adaptorClass.newInstance();
		} catch (InstantiationException e)
		{
			String msg = "Error instantiating adaptor for class" + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		} catch (IllegalAccessException e)
		{
			String msg = "Error instantiating adaptor for class" + adaptorClass.getName();
			throw new YADAUnsupportedAdaptorException(msg, e);
		}
	}

	/**
	 * Calls {@link #getAdaptorClass(String)} to get the class, then
	 * {@link #getAdaptor(Class)} to get an instance, using the no-arg
	 * constructor.
	 * @param app the YADA app assigned to the datasource
	 * @return an instance of the adaptor class mapped to {@code app}
	 * @throws YADAResourceException when {@code source} is not mapped to an adaptor
	 * @throws YADAUnsupportedAdaptorException when the adaptor class cannot be instantiated
	 * @throws YADAConnectionException when the connection pool or string cannot be established
	 */
	public Adaptor getAdaptor(String app) throws YADAResourceException, YADAUnsupportedAdaptorException, YADAConnectionException
	{
		Class<Adaptor> execClass = getAdaptorClass(app);
		return getAdaptor(execClass);
	}

	/**
	 * Returns {@code true} if the adaptor class is {@link #SOAP_ADAPTOR_CLASS}
	 * 
	 * @param adaptorClass
	 *          the class name of the YADA adaptor associated to the query
	 * @return {@code true} if the adaptor class is {@link #SOAP_ADAPTOR_CLASS}
	 */
	public boolean isSoap(Class<Adaptor> adaptorClass)
	{
		return SOAP_ADAPTOR_CLASS.isAssignableFrom(adaptorClass);
	}

	/**
	 * Returns {@code true} if the adaptor class is {@link #REST_ADAPTOR_CLASS}
	 * 
	 * @param adaptorClass
	 *          the class name of the YADA adaptor associated to the query
	 * @return {@code true} if the adaptor class is {@link #REST_ADAPTOR_CLASS}
	 */
	public boolean isRest(Class<Adaptor> adaptorClass)
	{
		return REST_ADAPTOR_CLASS.isAssignableFrom(adaptorClass);
	}

	/**
	 * Returns {@code true} if the adaptor class is
	 * {@link #FILESYSTEM_ADAPTOR_CLASS}
	 * 
	 * @param adaptorClass
	 *          the class name of the YADA adaptor associated to the query
	 * @return {@code true} if the adaptor class is
	 *         {@link #FILESYSTEM_ADAPTOR_CLASS}
	 */
	public boolean isFileSystem(Class<Adaptor> adaptorClass)
	{
		return FILESYSTEM_ADAPTOR_CLASS.isAssignableFrom(adaptorClass);
	}
	/**
	 * @since 4.0.0
	 * @param adaptorClass
	 *          the class name of the YADA adaptor associated to the query
	 * @return boolean true if the Class of the JDBCAdaptor param is a
	 *         JDBCAdaptor, otherwise false
	 */
	public boolean isJdbc(Class<Adaptor> adaptorClass)
	{
		return JDBC_ADAPTOR_CLASS.isAssignableFrom(adaptorClass);
	}

	/**
	 * Utility wrapper method to manage {@link Parser} instantiation and parsing.
	 * 
	 * @param yq
	 *          The query object containing the SQL to parse
	 * @throws YADAParserException when the parser fails
	 * @see com.novartis.opensource.yada.QueryManager
	 */

	private void processJDBCStatement(YADAQuery yq) throws YADAParserException
	{
	  // new method
		Parser parser = new Parser();
		
		parser.parseDeparse(yq.getYADACode());
		
		yq.setStatement(parser.getStatement());
		yq.setType(parser.getStatementType());
    yq.setColumnList(parser.getColumnList());
    yq.setInList(parser.getInColumnList());
    yq.setValuesList(parser.getValuesList());
    yq.setValuesColumns(parser.getValuesColumns());
    yq.setParameterizedColumnList(parser.getJdbcColumnList());
    yq.setInExpressionMap(parser.getInExpressionMap());
	}

	/**
	 * Initiates the parse/deparse process for a statement, and 
	 * recovers gracefully if {@link CCJSqlParserManager#parse(java.io.Reader)} throws a
	 * {@link JSQLParserException} in which case it will use a regular expression to infer 
	 * the query type.
	 * 
	 * @param yq the query object containing the code to parse
	 * @throws YADAUnsupportedAdaptorException when the adaptor can't be found or instantiated
	 */

	public void processStatement(YADAQuery yq) throws YADAUnsupportedAdaptorException
	{
		String         code         = getConformedCode(yq.getYADACode());
		Class<Adaptor> adaptorClass = yq.getAdaptorClass();
		if (isJdbc(yq.getAdaptorClass()))
		{
			try
			{
			  // Attempts to parse the JDBC statement
				processJDBCStatement(yq); 
			} 
			catch (YADAParserException e)
			{
				l.warn("Attempting to qualify previously unparsable statement");
				if (isCallable(code))
					yq.setType(Parser.CALL);
				else if (isSelect(code))
					yq.setType(Parser.SELECT);
				else if (isUpdate(code))
					yq.setType(Parser.UPDATE);
				else if (isInsert(code))
					yq.setType(Parser.INSERT);
				else if (isDelete(code))
					yq.setType(Parser.DELETE);
			}
		} 
		else if (isSoap(adaptorClass))
		{
			yq.setType(Parser.SOAP);
		} 
		else if (isRest(adaptorClass))
		{
			yq.setType(Parser.REST);
		} 
		else if (isFileSystem(adaptorClass))
		{
			if (isRead(code))
				yq.setType(READ);
			else if (isWrite(code))
				yq.setType(WRITE);
			else if (isAppend(code))
        yq.setType(APPEND);
			else if (isRm(code))
        yq.setType(RM);
			else if (isMkdir(code))
        yq.setType(MKDIR);
		} 
		else
		{
			String msg = "The query you are attempting to execute requires a protocol or class that is not supported.  This could be a configuration issue.";
			throw new YADAUnsupportedAdaptorException(msg);
		}
	}

	/**
	 * Interrogates {@code yq} for the adaptor class and sets the protocol
	 * attribute accordingly.
	 * 
	 * @param yq the query object in which to set the protocol attribute
	 * @throws YADAUnsupportedAdaptorException when the adaptor class cannot be found
	 */
	public void setProtocol(YADAQuery yq) throws YADAUnsupportedAdaptorException
	{
		Class<Adaptor> adaptorClass = yq.getAdaptorClass();
		if (isJdbc(adaptorClass))
		{
			yq.setProtocol(Parser.JDBC);
		}
		else if (isSoap(adaptorClass))
		{
			yq.setProtocol(Parser.SOAP);
		} 
		else if (isRest(adaptorClass))
		{
			yq.setProtocol(Parser.REST);
		} 
		else if (isFileSystem(adaptorClass))
		{
			yq.setProtocol(Parser.FILE);
		}
		else
		{
			String msg = "The query you are attempting to execute requires a protocol or class that is not supported.  This could be a configuration issue.";
			throw new YADAUnsupportedAdaptorException(msg);
		}
		yq.addParam(YADARequest.PS_PROTOCOL, yq.getProtocol());
	}

	/**
	 * Returns {@code true} if the query content matches an SQL callable statement
	 * syntax (see {@link #RX_CALLABLE}.
	 * 
	 * @param coreSql
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches an SQL callable statement
	 *         syntax
	 */
	public boolean isCallable(String coreSql)
	{
		Matcher matcher = Pattern.compile(RX_CALLABLE, Pattern.CASE_INSENSITIVE).matcher(coreSql);
		return matcher.matches();
	}

	/**
	 * Returns {@code true} if the query content matches an SQL SELECT statement
	 * syntax (see {@link #RX_SELECT}.
	 * 
	 * @param code
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches an SQL SELECT statement
	 *         syntax
	 */
	public boolean isSelect(String code)
	{
		Matcher matcher = Pattern.compile(RX_SELECT,
																			Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
															.matcher(code);
		return matcher.matches();
	}
	/**
	 * Returns {@code true} if the query content matches an SQL UPDATE statement
	 * syntax (see {@link #RX_UPDATE}.
	 * 
	 * @param code
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches an SQL UPDATe statement
	 *         syntax
	 */
	public boolean isUpdate(String code)
	{
		Matcher matcher = Pattern.compile(RX_UPDATE,
																			Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
															.matcher(code);
		return matcher.matches();
	}
	/**
	 * Returns {@code true} if the query type matches {@link Parser#UPDATE}.
	 * 
	 * @param yq
	 *          the query object to check
	 * @return {@code true} if the query type is {@link Parser#UPDATE}. s
	 */
	public boolean isUpdate(YADAQuery yq)
	{
		return yq.getType().equals(Parser.UPDATE);
	}
	/**
	 * Returns {@code true} if the query content matches an SQL INSERT statement
	 * syntax (see {@link #RX_INSERT}.
	 * 
	 * @param code
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches an SQL INSERT statement
	 *         syntax
	 */
	public boolean isInsert(String code)
	{
		Matcher matcher = Pattern.compile(RX_INSERT,
																			Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
															.matcher(code);
		return matcher.matches();
	}
	/**
	 * Returns {@code true} if the query type matches {@link Parser#INSERT}.
	 * 
	 * @param yq
	 *          the query object to check
	 * @return {@code true} if the query type is {@link Parser#INSERT}.
	 */
	public boolean isInsert(YADAQuery yq)
	{
		return yq.getType().equals(Parser.INSERT);
	}
	/**
	 * Returns {@code true} if the query content matches an SQL DELETE statement
	 * syntax (see {@link #RX_DELETE}.
	 * 
	 * @param code
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches an SQL DELETE statement
	 *         syntax
	 */
	public boolean isDelete(String code)
	{
		Matcher matcher = Pattern.compile(RX_DELETE,
																			Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
															.matcher(code);
		return matcher.matches();
	}
	/**
	 * Returns {@code true} if the query type matches {@link Parser#DELETE}.
	 * 
	 * @param yq
	 *          the query object to check
	 * @return {@code true} if the query type is {@link Parser#DELETE}.
	 */
	public boolean isDelete(YADAQuery yq)
	{
		return yq.getType().equals(Parser.DELETE);
	}

	/**
	 * Returns {@code true} if the query content matches an SQL DELETE statement
	 * syntax (see {@link #RX_DELETE}.
	 * 
	 * @param code
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches an SQL DELETE statement
	 *         syntax
	 * @since PROVISIONAL
	 */
	public boolean isRead(String code)
	{
		Matcher m1 = Pattern.compile(RX_FILE_URI).matcher(code);
		if (m1.matches())
		{
			if (m1.groupCount() > 1 && m1.group(3) != null)
			{
				return false;
			}
			return true;
		}
		return false;
	}
	/**
	 * Returns {@code true} if the query content matches an the {@link #RX_FILE_URI} regex
	 * 
	 * @param code
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches
	 *         
	 */
	public boolean isWrite(String code)
	{
		Matcher m1 = Pattern.compile(RX_FILE_URI).matcher(code);
		if (m1.matches())
		{
			if (m1.groupCount() > 1 && m1.group(3) != null && m1.group(3)
																													.equals(WRITE))
			{
				return true;
			}
			return false;
		}
		return false;
	}
	
	/**
   * Returns {@code true} if the query content matches an the {@link #RX_FILE_RM} regex
   * 
   * @param code
   *          stored code (with YADA markup)
   * @return {@code true} if the query content matches
   * @since 9.0.3        
   */
  public boolean isRm(String code)
  {
    Matcher m1 = Pattern.compile(RX_FILE_RM).matcher(code);
    return m1.matches();
  }
  
  /**
   * Returns {@code true} if the query content matches an the {@link #RX_FILE_MKDIR} regex
   * 
   * @param code
   *          stored code (with YADA markup)
   * @return {@code true} if the query content matches
   * @since 9.0.3        
   */
  public boolean isMkdir(String code)
  {
    Matcher m1 = Pattern.compile(RX_FILE_MKDIR).matcher(code);
    return m1.matches();
  }

	/**
	 * Test if the query requires a stored connection
	 * 
	 * @param yq
	 *          the query to evaluate
	 * @return {@code true} if the query's protocol is {@link Parser#JDBC} or
	 *         {@link Parser#SOAP}
	 */
	public boolean requiresConnection(YADAQuery yq)
	{
		return (yq.getProtocol().equals(Parser.JDBC) // TODO this is now redundant to YADAQuery.getType -- perhaps this could be refactored 
		    || yq.getProtocol().equals(Parser.SOAP));
	}

	/**
	 * Returns {@code true} if the {@link YADARequest#PS_COMMITQUERY} parameter is
	 * not {@code null}, has a length &gt; 0, equals {@code true}, and the query type
	 * is equal to {@link Parser#INSERT}, {@link Parser#UPDATE}, or
	 * {@link Parser#DELETE}
	 * 
	 * @param yq
	 *          the query to commit
	 * @return {@code true} if the {@link YADARequest#PS_COMMITQUERY} parameter is
	 *         not {@code null}, has a length &gt; 0, equals {@code true}, and the
	 *         query type is equal to {@link Parser#INSERT}, {@link Parser#UPDATE}
	 *         , or {@link Parser#DELETE}
	 * @since 4.1.0
	 */
	public boolean isCommitQuery(YADAQuery yq)
	{
		return yq.getYADAQueryParamValue(YADARequest.PS_COMMITQUERY) != null 
				&& yq.getYADAQueryParamValue(YADARequest.PS_COMMITQUERY).length > 0 
				&& Boolean.parseBoolean(yq.getYADAQueryParamValue(YADARequest.PS_COMMITQUERY)[0]) 
				&& (this.isInsert(yq) || this.isUpdate(yq) || this.isDelete(yq));
	}

	/**
	 * Returns {@code true} if the query content matches an SQL DELETE statement
	 * syntax (see {@link #RX_DELETE}.
	 * 
	 * @param code
	 *          stored code (with YADA markup)
	 * @return {@code true} if the query content matches an SQL DELETE statement
	 *         syntax
	 * @since PROVISIONAL
	 */
	public boolean isAppend(String code)
	{
		Matcher m1 = Pattern.compile(RX_FILE_URI).matcher(code);
		if (m1.matches())
		{
			if (m1.groupCount() > 1 && m1.group(3) != null && m1.group(3)
																													.equals(APPEND))
			{
				return true;
			}
			return false;
		}
		return false;
	}

	/**
	 * Returns the soap query that is passed as an argument. This method does
	 * nothing.
	 * 
	 * @param xmlStr
	 *          the soap query
	 * @return the soap query
	 */
	public String getSoap(String xmlStr)
	{
		return xmlStr;
	}

	/**
	 * Returns a {@link URL} built from the provided {@code urlStr}
	 * 
	 * @param urlStr
	 *          the url string to convert to an object
	 * @return a {@link URL} object
	 */
	public URL getUrl(String urlStr)
	{
		URL url = null;
		try
		{
			url = new URL(urlStr);
		} 
		catch (MalformedURLException e)
		{
			e.printStackTrace();
			l.error(e.getMessage());
		}
		return url;
	}

	/**
	 * Creates and returns a {@link java.sql.CallableStatement} from the
	 * {@code sql} on the {@code conn}.
	 * <p>
	 * <strong>NOTE:</strong> not sure why this method isn't throwing an
	 * exception.
	 * </p>
	 * 
	 * @param sql
	 *          conformed code (without YADA markup)
	 * @param conn
	 *          connection object derived from YADA query's source attribute
	 * @return the desired statement object
	 */
	public CallableStatement getCallableStatement(String sql, Connection conn)
	{
		CallableStatement c = null;
		try
		{
			c = conn.prepareCall(sql);
		} 
		catch (SQLException e)
		{
			l.error(e.getMessage());
		}
		return c;
	}

	/**
	 * Creates and returns a {@link java.sql.PreparedStatement} from {@code sql}
	 * on the {@code conn}
	 * 
	 * @param sql conformed code (without YADA markup)
	 * @param conn connection object derived from YADA query's source attribute
	 * @return the desired statement object
	 * @throws YADAConnectionException when the connection cannot deliver the statement
	 */
	public PreparedStatement getPreparedStatement(String sql, Connection conn) throws YADAConnectionException
	{
		PreparedStatement pstmt = null;
		try
		{
			pstmt = conn.prepareStatement(sql);
		} 
		catch (SQLException e)
		{
			String msg = "Unable to create or configure the PreparedStatementfor the requested query in the YADA Index.";
			throw new YADAConnectionException(msg, e);
		}
		return pstmt;
	}

	/**
	 * Removes all YADA data type symbols from source code.
	 * 
	 * @param code
	 *          stored code with YADA markup
	 * @return the transformed code
	 */
	public String getConformedCode(String code)
	{
		String c = code.replaceAll("\\?[nvdti]", "?");
		return c;
	}

	/**
	 * Parses the source code, generating a {@code char} array of data types in
	 * order of occurrence.
	 * 
	 * @param sql
	 *          stored code with YADA markup
	 * @return a {@code char} array of data types
	 */
	public char[] getDataTypes(String sql)
	{
		int count = sql.split("\\?(?=[vindt])").length - 1;
		char[] dataTypes = new char[count];
		int idx = 0;
		for (int i = 0; i < count; i++)
		{
			idx = sql.indexOf("?", idx) + 1;
			if(String.valueOf(sql.charAt(idx)).matches("[vindt]"))
			{
  			dataTypes[i] = sql.charAt(idx);
  			l.debug("data type of param [" + String.valueOf(i + 1) + "] = " + dataTypes[i]);
			}
		}
		return dataTypes;
	}
	
	/**
	 * This method creates a list for positional-indexed storage of data values,
   * then iterates over the list of jdbc-parameterized columns, extracting the
   * corresponding values from the data map. It then stores the value in the
   * list at it's proper positional index. Finally, the indexed value list is
   * added to the list of value lists in the query {@code yq}, at position {@code row}.
   * 
   * This method uses the value returned by {@link YADAQuery#getParameterizedColumnList()} 
   * instead of {@link YADAQuery#getParameterizedColumns()}, and 
   * supercedes {@link #setValsInPosition(YADAQuery, int)} 
   * 
	 * @param yq the query containing the data to process
   * @param row the index of the data list
   * @since 7.1.0
	 */
	public void setPositionalParameterValues(YADAQuery yq, int row) 
	{
	  // the indexed positional value list for the current "row"
	  List<String> valsInPosition = new ArrayList<>();
	  
	  // any subtables in values claused previously processed in the loop below
	  List<String> processedValuesAliases = new ArrayList<>();
	  
	  // only process if there's data
    if (yq.getData().size() > 0)
    {
      // get the columns corresponding to jdbc parameters
      List<Column> paramColumns = yq.getParameterizedColumnList();
      
      // get the data for the current "row" 
      Map<String,String[]> data = yq.getDataRow(row);
      
      // iterate over parameterized columns in the query
//      for (int paramColIndex = 0; paramColIndex < paramColumms.size(); paramColIndex++)
      int paramColIndex = 0;
      for(Column column : paramColumns)
      {
//        String paramColName = paramColumns.get(paramColIndex).getColumnName();

        String paramColName = column.getColumnName();
        // get table name  
        Table  table = column.getTable();
        String tabName = "";        
        if(table.getAlias() != null 
            && !table.getAlias().getName().contentEquals(""))
        {
          tabName = table.getAlias().getName().trim();
        }
        else if(table.getName() != null)
        {
          tabName = table.getName().trim();
        }
        
        if(data.containsKey(tabName))
        {
          paramColName = tabName;
        }
        else if(data.containsKey(tabName.toUpperCase()))
        {
          paramColName = tabName.toUpperCase();
        }
        // standard params or deliberate names, i.e., REST params        
        else if(data.containsKey(YADA_COLUMN + (paramColIndex + 1)))
          paramColName = YADA_COLUMN + (paramColIndex + 1);
        // named columns in json params
        else if (data.containsKey(paramColName.toUpperCase()))
          paramColName = paramColName.toUpperCase();
        // named columns with quotes
        else if(data.containsKey(paramColName.replaceAll("\"", "").toUpperCase()))        
          paramColName = paramColName.replaceAll("\"", "").toUpperCase();
        
        // obtain the values for the column
        String[] valsForColumn;
        valsForColumn = data.get(paramColName);
        
        
        
        if(!processedValuesAliases.contains(tabName))        
        {         
          // handle VALUE columns         
          if(yq.getValuesList() != null
              && yq.getValuesList().getAlias() != null
              && yq.getValuesList().getAlias().getName().trim().contentEquals(tabName))
          {
            String alias = yq.getValuesList().getAlias().getName();
            if(valsForColumn != null && valsForColumn[0].contains(","))
            {
              valsForColumn = String.join(",", Arrays.asList(valsForColumn)).split(",");
            }
              
            /* 
             * A query such as:
             *            
             *   select * from tab a join (values (?v, ?v)) vals(v,w) on a.col1 = vals.v and a.col2 = ?i
             *   
             * with standard paramters would contain the following:
             * 
             *   YADA_1 = [(A,1),(B,2),(Z,3)]           *   
             *   YADA_3 = 1
             *   
             * with JSONParams, data would be
             * 
             *   {"vals":[(A,1),(B,2),(Z,3)],"cols2":1}
             * 
             * valsInPosition must result in [A,1,B,2,C,3,1]
             * 
             */             
            // if we haven't processed the values alias yet, do them all
            for(int valIndex = 0; valIndex < valsForColumn.length; valIndex++)
            {
              String val = valsForColumn[valIndex];
              String cleanVal = "";              
              Matcher m_paren  = RX_VALUES_PARAM_SINGLE.matcher(val);
              Matcher m_left   = RX_VALUES_PARAM_LEFT.matcher(val);
              Matcher m_right  = RX_VALUES_PARAM_RIGHT.matcher(val);
              if(m_paren.matches())
              {
                cleanVal = m_paren.group(1);
              }
              else if(m_left.matches())
              {
                cleanVal = m_left.group(1);
              }
              else if(m_right.matches())
              {
                cleanVal = m_right.group(1);
              }
              else
              {
                // middle of value set 
                cleanVal = val;
              }
              valsInPosition.add(cleanVal);
            }
            processedValuesAliases.add(alias);
                  
          }
          else
          {
            // add the values in the correct order
            for (String val : valsForColumn)
            {
              l.debug("Column [" + String.valueOf(paramColIndex + 1) + ": " + paramColumns.get(paramColIndex) + "] has value [" + val + "]");
              valsInPosition.add(val);
            }
          }
          paramColIndex++;
        }
      }
    }
    yq.addVals(row, valsInPosition);
	}

	/**
	 * This method creates a list for positional-indexed storage of data values,
	 * then iterates over the list of jdbc-parameterized columns, extracting the
	 * corresponding values from the data map. It then stores the value in the
	 * list at it's proper positional index. Finally, the indexed value list is
	 * added to the list of value lists in the query {@code yq}, at position
	 * {@code row}.
	 * 
	 * @param yq
	 *          the query containing the data to process
	 * @param row
	 *          the index of the data list
	 */
	public void setValsInPosition(YADAQuery yq, int row)
	{
		List<String> valsInPosition = new ArrayList<>();
		if (yq.getData().size() > 0)
		{
			String[] columns = yq.getParameterizedColumns();
			Map<String,String[]> data = yq.getDataRow(row);
			for (int j = 0; j < columns.length; j++)
			{
				String colName = columns[j]; 
				if(data.containsKey(YADA_COLUMN + (j + 1)))
          colName = YADA_COLUMN + (j + 1);
        else if (data.containsKey(colName.toUpperCase()))
          colName = colName.toUpperCase();   
				    
				String[] valsForColumn;

				valsForColumn = data.get(colName);

				for (String val : valsForColumn)
				{
					l.debug("Column [" + String.valueOf(j + 1) + ": " + columns[j] + "] has value [" + val + "]");
					valsInPosition.add(val);
				}
			}
		}
		yq.addVals(row, valsInPosition);
	}

	/**
	 * While processing JDBC statements, this method handles mapping of data
	 * values to JDBC positional parameters.
	 * 
	 * @param yq
	 *          the query containing the statement
	 * @param row
	 *          the index of the value list stored in the query
	 */
	public void setQueryParameters(YADAQuery yq, int row)
	{
		int jdbcParamCount = yq.getParamCount(row);
		char[] dataTypes = yq.getDataTypes(row);
		List<String> valsInPosition = yq.getVals(row);
		PreparedStatement pstmt = yq.getPstmt(row);

		for (int j = 0; j < jdbcParamCount; j++)
		{
			int position = j + 1;
			char dt = dataTypes[j];
			String val = valsInPosition.get(j);
			setQueryParameter(pstmt, position, dt, val);
		}
	}

	/**
	 * Calls the appropriate setter method for {@code type} in the {@code pstmt},
	 * performing the appropriate type conversion or syntax change as needed
	 * (e.g., for {@link java.sql.Date}s)
	 * 
	 * @param pstmt
	 *          the statement to which to assign the parameter values
	 * @param index
	 *          the position of the parameter
	 * @param type
	 *          the data type of the parameter
	 * @param val
	 *          the value to assign
	 */
	
	private void setQueryParameter(PreparedStatement pstmt, int index, char type,
																	String val)
	{
		String idx = (index < 10)
															? " " + String.valueOf(index)
															: String.valueOf(index);
		l.debug("Setting param [" + idx + "] of type [" + String.valueOf(type) + "] to: " + val);
		try
		{
			switch (type)
			{
				case DATE :

					try
					{
						if ("".equals(val) || val == null)
						{
							pstmt.setNull(index, java.sql.Types.DATE);
						} 
						else
						{
							SimpleDateFormat sdf = new SimpleDateFormat(STANDARD_DATE_FMT);
							ParsePosition pp = new ParsePosition(0);
							Date dateVal = sdf.parse(val, pp);
							if (dateVal == null)
							{
								sdf = new SimpleDateFormat(ORACLE_DATE_FMT);
								dateVal = sdf.parse(val, pp);
							}
							if (dateVal != null)
							{
								long t = dateVal.getTime();
								java.sql.Date sqlDateVal = new java.sql.Date(t);
								pstmt.setDate(index, sqlDateVal);
							}
						}
					} 
					catch (Exception e)
					{
						l.error("Error: " + e.getMessage());
					}
					break;
				case INTEGER :
					try
					{
						int ival = Integer.parseInt(val);
						pstmt.setInt(index, ival);
					} 
					catch (NumberFormatException nfe)
					{
						l.error("Error: " + nfe.getMessage());
						l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
						pstmt.setNull(index, java.sql.Types.INTEGER);
					} 
					catch (NullPointerException npe)
					{
						l.error("Error: " + npe.getMessage());
						l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
						pstmt.setNull(index, java.sql.Types.INTEGER);
					} 
					catch (Exception sqle)
					{
						l.error("Error: " + sqle.getMessage());
						l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: 0");
						pstmt.setNull(index, java.sql.Types.INTEGER);
					}
					break;
				case NUMBER :
					try
					{
						float fval = Float.parseFloat(val);
						pstmt.setFloat(index, fval);
					} 
					catch (NumberFormatException nfe)
					{
						l.error("Error: " + nfe.getMessage());
						l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
						pstmt.setNull(index, java.sql.Types.INTEGER);
					} 
					catch (NullPointerException npe)
					{
						l.error("Error: " + npe.getMessage());
						l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
						pstmt.setNull(index, java.sql.Types.INTEGER);
					}
					catch (Exception sqle)
					{
						l.error("Error: " + sqle.getMessage());
						l.debug("Setting param [" + String.valueOf(index) + "] of type [" + String.valueOf(type) + "] to: null");
						pstmt.setNull(index, java.sql.Types.INTEGER);
					}
					break;
				case OUTPARAM_DATE :
					((CallableStatement)pstmt).registerOutParameter(index,
																													java.sql.Types.DATE);
					break;
				case OUTPARAM_INTEGER :
					((CallableStatement)pstmt).registerOutParameter(index,
																													java.sql.Types.INTEGER);
					break;
				case OUTPARAM_NUMBER :
					((CallableStatement)pstmt).registerOutParameter(index,
																													java.sql.Types.FLOAT);
					break;
				case OUTPARAM_VARCHAR :
					((CallableStatement)pstmt).registerOutParameter(index,
																													java.sql.Types.VARCHAR);
					break;
				default : // VARCHAR2
					pstmt.setString(index, val);
					break;
			}
		} 
		catch (SQLException e)
		{
			e.printStackTrace();
			l.error(e.getMessage());
		}
	}
	
	/**
	 * Uses the metadata collected during {@link Parser#parseDeparse(String)} to
	 * modify the {@link Statement} by appending positional parameters to IN clause
	 * expression lists, dynamically, based on the data passed in the request. 
	 * 
	 * Also stores the transformed data types, param counts, and SQL corresponding to the row
	 * so the return value is for illustrative or logging purposes only, in all likelihood.
	 * @param yq the query to process
	 * @param row the index of the data array passed for processing
	 * @return the modified YADA SQL
	 * @throws YADAParserException if parsing or deparsing the query encounters a non-conforming state 
	 * @since 7.1.0
	 */
	public String processInList(YADAQuery yq, int row) throws YADAParserException
	{
	  Parser parser = new Parser();
	  // Are there "in" columns
	  if(yq.getInList().size() > 0)
	  {
	    //TODO there should be a better way than reparsing to get access to the parser.
	    //     like aren't the incols and expression map already in the query at this point?
	    //     Probably just need to store the statement object in the yq
	    try 
	    {
	      parser.parseDeparse(yq.getYADACode());
	    } 
	    catch (YADAParserException e) 
	    {
	      String msg = "Unable to reparse statement for IN clause processing.";
	      throw new YADAParserException(msg, e);
	    }
	    List<Column>             inColumns  = parser.getInColumnList();
	    Map<Column,InExpression> inExprs    = parser.getInExpressionMap();
	    Map<String,String[]>     dataForRow = yq.getDataRow(row);
	    
	    // iterate inColumns list
	    for(int colIndex=0; colIndex<inColumns.size(); colIndex++)
	    {
	      Column inColumn = inColumns.get(colIndex);
	      if(inColumn != null)
	      {
	        String colName = inColumn.getColumnName(); // json params
	        if(dataForRow.containsKey(YADA_COLUMN + (colIndex+1)))
	        { // standard params
	          colName = YADA_COLUMN + (colIndex + 1);
	        }
	        else if(dataForRow.containsKey(colName.toUpperCase()))
	        { // json params upper case
	          colName = colName.toUpperCase();
	        }
	        else if(dataForRow.containsKey(colName.replaceAll("\"", "").toUpperCase()))
	        {
	        	colName = colName.replaceAll("\"", "").toUpperCase();
	        }
	        
	        // length of value array for inColumn
	        int dataLen = dataForRow.get(colName).length;
	        
	        // special case of comma-separated strings, e.g., ["A,B,C"] (instead of ["A","B","C"]) 
	        if(dataLen == 1)
	        {
	          dataForRow.put(colName, dataForRow.get(colName)[0].split(","));
	          dataLen = dataForRow.get(colName).length;
	        }
	        
	        // special case of standard params without brackets e.g., p=1,2,3,4
	        if(colName.startsWith(YADA_COLUMN)
	            && colIndex == (inColumns.size() - 1) // last index
	            && dataForRow.keySet().size() > inColumns.size()) // more values
	        {
	          StringBuilder inVals = new StringBuilder();
	          for(int i=colIndex+1;i<=dataForRow.keySet().size();i++)
	          {
	            if(i > colIndex+1)
	              inVals.append(",");
	            String name = YADA_COLUMN + i;
	            int j=0;
	            while(j<dataForRow.get(name).length)
	            { 
	              if(j > 0)
	                inVals.append(",");
	              inVals.append(dataForRow.get(name)[j++]);
	            }
	          }
	          
	          dataForRow.put(colName, inVals.toString().split(","));
	          dataLen  = dataForRow.get(colName).length;
	        }
	        
	        	        
	        // amend the in clause with the additional markup
	        InExpression     inExpr         = inExprs.get(inColumn);
	        ItemsList        rightItemsList = inExpr.getRightItemsList();
	        List<Expression> rightItemsExpressionList = ((ExpressionList)rightItemsList).getExpressions();
	        String           dataType = String.valueOf(((YADAMarkupParameter)rightItemsExpressionList.get(0)).getType());
	        for(int i=0;i<dataLen-1;i++)
	        {
	          YADAMarkupParameter ymp = new YADAMarkupParameter();
	          ymp.setType(dataType);
	          rightItemsExpressionList.add(ymp);
	        }
	        ((ExpressionList)rightItemsList).setExpressions(rightItemsExpressionList);
	        inExpr.setRightItemsList(rightItemsList);
	      }
	    }
	    yq.addDataTypes(row, getDataTypes(parser.getStatement().toString()));
	    yq.addParamCount(row, yq.getDataTypes(row).length);
	    yq.addCoreCode(row, parser.getStatement().toString());
	  }
	  return parser.getStatement().toString();
	}

	/**
	 * This method uses a variety of variables from {@code yq} to determine the
	 * number of jdbc positional parameters which must be included in the SQL
	 * {@code in} clause in the stored query. Once the parameter count is
	 * determined, the original SQL is amended with additional jdbc parameter
	 * placeholders (i.e., {@code ?}), and the amended SQL is stored in the
	 * {@code yq}.
	 * 
	 * @param yq
	 *          the query being processed
	 * @param row
	 *          the index of the list of value lists in the query containing the
	 *          data to evaluate
	 * @return modified SQL code
	 * @deprecated as of 7.1.0
	 */
	@Deprecated
	public String processInColumns(YADAQuery yq, int row)
	{
		String[] inColumns = yq.getIns();
		String   coreSql   = yq.getYADACode();
		LinkedHashMap<String,String[]> newData = new LinkedHashMap<>(); // to be i.e., YADA_1:[],YADA_2:[]
		if (inColumns.length > 0)
		{
			String[]             columns   = yq.getParameterizedColumns();
			Map<String,String[]> data      = yq.getDataRow(row);
			char[]               dataTypes = yq.getDataTypes(row);
			Matcher matcher;

			l.debug("Processing inColumns [" + StringUtils.join(inColumns, ",") + "]");
			for (String in : inColumns)
			{
				int colIndex = -1, j = 0;
				String inCol = in.toUpperCase(); 

				// get the index of the 'incolumn' in the 'JDBCcolumns' array
				l.debug("Looking for column [" + inCol + "] in columns array " + ArrayUtils.toString(columns));
				while (j < columns.length && colIndex != j)
				{
					if (inCol.contains(columns[j]))
					{
						colIndex = j;
						l.debug("Found column [" + inCol + "] at index [" + String.valueOf(colIndex) + "] of columns array.");
						break;
					}
					j++;
				}

				// get the value list associated to the column in the data hash
				String colName = "";
				String[] inData = null;
				int inLen = 0;
				if (data.containsKey(columns[colIndex])) // JSONParams
				{
					colName = columns[colIndex];
					if (data.get(colName).length == 1)
					{
						inData = data.get(colName)[0].split(",");
						for (int m = 0; m < columns.length; m++)
						{
							if (columns[m].equals(colName))
							{
								// add the new data for the column
								newData.put(colName, inData);
							}
							else
							{
								// add the existing data for the column
								newData.put(columns[m], data.get(columns[m]));
							}
							// add data row
							yq.getData().set(row, newData);
						}
						yq.getData().set(row, newData);
					}
					else
						inData = data.get(colName);
					l.debug("Splitting in args [" + data.get(colName) + "]");
				}
				else
				// Standard Params
				{

					// Get an array of keys to compare and potentially manipulate
					String[] colNames = new String[data.size()];
					int k = 0;
					for (String col : data.keySet())
					{
						colNames[k] = col;
						k++;
					}

					// if colNames and columns array are of equal size,
					// then there is no param value manipulation required
					if (colNames.length == columns.length)
					{
						colName = QueryUtils.YADA_COLUMN + (colIndex + 1);
						inData = data.get(colName);
					}
					else
					// there is a length discrepancy
					{
						for (int m = colIndex; m < colNames.length; m++)
						{
							if (m == colIndex) // it's the first index
								inData = data.get(colNames[m]);
							else
								// further indexes must build aggregate array
								inData = (String[])ArrayUtils.addAll(	inData,
																											data.get(colNames[m]));
						}

						for (int m = 0; m < columns.length; m++)
						{
							if (m == columns.length - 1)
							{
								// it's the last index, so add the aggregrate inData array
								newData.put(colNames[m], inData);
							}
							else
							{
								// not the last index, add the existing array
								newData.put(colNames[m], data.get(colNames[m]));
							}
							// add data row
							yq.getData().set(row, newData);
						}
					}
					l.debug("Setting IN args [" + ArrayUtils.toString(inData) + "]");
				}
				if (inData != null)
				{
					inLen = inData.length;
				}

				if (inLen > 1) // there's an aggregate of multiple values
				{
					l.debug("Length of value list [" + String.valueOf(inLen) + "]");
					l.debug("Getting data type of [" + columns[colIndex] + "]");
					char dt = dataTypes[colIndex];
					String dtStr = "?" + String.valueOf(dt);

					// generate the new parameter string with data type markers
					String[] pList = new String[inLen];
					for (int k = 0; k < inLen; k++)
					{
						pList[k] = dtStr;
					}
					String pListStr = StringUtils.join(pList, ",");
					l.debug("New parameter list [" + pListStr + "]");

					// add additional parameters to coreSql
					String rx = "(.+)(" + inCol + "\\s+in\\s+\\(\\" + dtStr + "\\))(.*)";
					String repl = inCol + " IN (" + pListStr + ")";
					String sql = coreSql.replaceAll(NEWLINE, " ");
					l.debug("Attempting to replace part of [" + sql + "] with [" + repl + "]");
					matcher = Pattern.compile(rx, Pattern.CASE_INSENSITIVE).matcher(sql);
					if (matcher.matches())
					{
						coreSql = matcher.group(1) + repl + matcher.group(3);
					}
					l.debug("Matched clause in coreSql [" + matcher.toString() + "]");
				} // end current incolumn processing
			} // end all incolumn processing
		}
		// reset datatype and param count with new coreSql
		yq.addDataTypes(row, this.getDataTypes(coreSql));
		yq.addParamCount(row, yq.getDataTypes(row).length);

		return coreSql;
	}

	/**
	 * Replaces YADA markup in {@code VALUES} clause in {@code SELECT} statement with 
	 * corresponding parameter values.  Called internally by {@link QueryManager} 
	 * during {@link QueryManager#prepQueryForExecution} processing.
	 * @param yq
   *          the query being processed
   * @param row
   *          the index of the list of value lists in the query containing the
   *          data to evaluate
 	 * @return the {@code SQL} string with transformed {@code VALUES} clause 
	 * @throws YADAParserException when statement parsing fails
	 */
  public String processValuesList(YADAQuery yq, int row) throws YADAParserException {
    
    // Reparsing happens for each iteration of the data.  This is 
    // necessary as it's a tradeoff between iterating over the data
    // then or now, and also parsing the query
    Parser parser = new Parser();
        
    /*
     * What are all the different scenarios:
     * 
     *  - join (values(?v)) vals(v) = 1 column
     *  - join (values(?v,?v) vals(v,w) = >1 column
     *  - join (values(?v,'x') vals(v,w) = >1 column with literal
     */
    
    if(yq.getValuesList() != null)
    {     
      try 
      {
        parser.parseDeparse(yq.getYADACode());
      } 
      catch (YADAParserException e) 
      {
        String msg = "Unable to reparse statement for VALUES JOIN clause processing.";
        throw new YADAParserException(msg, e);
      }
      ValuesList           valuesList = parser.getValuesList();
      List<String>         valColumns = valuesList.getColumnNames();
      Map<String,String[]> dataForRow = yq.getDataRow(row);
      int                  colIndex   = 0;
      String               colName    = valColumns.get(colIndex);
      String               tabName    = valuesList.getAlias().getName();
      if(colName != null)
      {
        
        if(dataForRow.containsKey(tabName))
        {
          colName = tabName;
        }
        else if(dataForRow.containsKey(tabName.toUpperCase()))
        {
          colName = tabName.toUpperCase();
        }
        else if(dataForRow.containsKey(YADA_COLUMN + (colIndex+1)))
        { 
          // standard params
          // TODO I think this means data at end of params list
          colName = YADA_COLUMN + (colIndex + 1);
        }
        else if(dataForRow.containsKey(colName.toUpperCase()))
        { 
          // json params upper case
          colName = colName.toUpperCase();
        }
        else if(dataForRow.containsKey(colName.replaceAll("\"", "").toUpperCase()))
        {
          // json params, quoted column nams, e.g., postgres names with spaces or mixed case
          colName = colName.replaceAll("\"", "").toUpperCase();
        }
        
        // length of value array for valColumn.
        // all we want here are the sets of parens
        Matcher m = RX_VALUES_PARAM_STRING.matcher(String.join(",",dataForRow.get(colName)));
        int dataLen = 0;
        while(m.find())
        {
          dataLen++;
        }
//        int dataLen = dataForRow.get(colName).length;
        
        // special case of comma-separated strings, e.g., ["A,B,C"] (instead of ["A","B","C"]) 
        if(dataLen == 1)
        {
          dataForRow.put(colName, dataForRow.get(colName)[0].split(","));
          dataLen = dataForRow.get(colName).length;
        }
        
        // special case of standard params without brackets e.g., p=1,2,3,4
        if(colName.startsWith(YADA_COLUMN)
            && colIndex == (valColumns.size() - 1) // last index
            && dataForRow.keySet().size() > valColumns.size()) // more values
        {
          StringBuilder valuesVals = new StringBuilder();
          for(int i=colIndex+1;i<=dataForRow.keySet().size();i++)
          {
            if(i > colIndex+1)
              valuesVals.append(",");
            String name = YADA_COLUMN + i;
            int j=0;
            while(j<dataForRow.get(name).length)
            { 
              if(j > 0)
                valuesVals.append(",");
              valuesVals.append(dataForRow.get(name)[j++]);
            }
          }            
          dataForRow.put(colName, valuesVals.toString().split(","));
          dataLen  = dataForRow.get(colName).length;
        }
        //TODO this will limit use cases to YADA queries that contain VALUES clauses containing only a single expression list
        //   i.e., (VALUES (?v,?v,?v...)) vals(x,y,z...) will work 
        //   but   (VALUES (?v,?v,?v...),(?v,?v,?v...)) vals(x,y,z...) wont work
        
        // column list:  [v, w, x]
        // multiExpressionList = (?v, ?v, 'xyz')
        // I assume the multiexpressionlist is just a container for the "expressionlist list" 
        // and entries would take the form (x1,y1,z1),(xn,yn,zn)
        // so what we're doing here is creating new expression lists and adding them to the 
        // multiExpressionList.exprList.expressions: [?v, ?v, 'xyz']
        
        MultiExpressionList mel      = valuesList.getMultiExpressionList();
        ExpressionList      el       = mel.getExprList().get(0);
        List<Expression>    exprList = el.getExpressions(); 
        // All we need to do here is create a new expression list for each entry in the data row.
        // TODO BUT, there is a complication with how to pass the data and handle the parameter value mapping 
        for(int i=0; i<dataLen-1; i++)
        {
          List<Expression> neo = new ArrayList<>(exprList);
          mel.addExpressionList(neo);
        }
      }
      yq.addDataTypes(row, getDataTypes(parser.getStatement().toString()));
      yq.addParamCount(row, yq.getDataTypes(row).length);
      yq.addCoreCode(row, parser.getStatement().toString());
    }
    return parser.getStatement().toString();
  }
}
