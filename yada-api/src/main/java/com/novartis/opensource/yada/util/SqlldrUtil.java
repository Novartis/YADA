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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.log4j.Logger;
import com.zaxxer.hikari.HikariDataSource;
//import org.apache.tomcat.dbcp.dbcp.BasicDataSource;

/**
 * A utility for executing Oracle sqlldr from java
 * @author David Varon
 * @since PROVISIONAL
 * //TODO tests for sqlldr utility
 */
public class SqlldrUtil {
	/**
	 * Local logging handle
	 */
	private static Logger l = Logger.getLogger(SqlldrUtil.class);

//  sqlldr procs
	/**
	 * Constant value equal to: {@code System.getProperty("line.separator")}
	 */
	public final static String NEWLINE             = System.getProperty("line.separator");
	/**
	 * Constant value equal to: {@code System.getProperty("java.io.tmpdir")}
	 */
	public final static String TMP                 = System.getProperty("java.io.tmpdir");
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String COMMA               = ",";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String APOS	               = "'";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String QUOTE               = "\"";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String EQUAL               = "=";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String PAREN_OPEN          = "(";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String PAREN_CLOSE         = ")";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String YADA_DATASOURCE	   = "java:comp/env/jdbc/yada";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String YADA                = "yada";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String CTL_FILE			       = ".ctl";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String DAT_FILE            = ".dat";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String LOG_FILE			       = ".log";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String BAD_FILE            = ".bad";
	/**
	 * Constant value equal to: {@code "LOAD DATA"+NEWLINE}
	 */
	public final static String LOAD_DATA           = "LOAD DATA"+NEWLINE;
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String INFILE              = "INFILE ";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String BADFILE             = "BADFILE ";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String INTO_TABLE          = "INTO TABLE ";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String TERM_STRING         = "FIELDS TERMINATED BY ";
	/**
	 * Constant value equal to: {@code System.getenv("ORACLE_HOME")}
	 */
	public final static String ORACLE_HOME         = System.getenv("ORACLE_HOME");
	/**
	 * Constant value equal to: {@code ORACLE_HOME+"/bin/sqlldr"}
	 */
	public final static String SQLLDR			         = ORACLE_HOME+"/bin/sqlldr";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String APPEND              = "APPEND";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String INSERT              = "INSERT";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String TRUNCATE            = "TRUNCATE";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String REPLACE             = "REPLACE";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String OPTIONS             = "OPTIONS";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String TRUE                = "true";
	/**
	 * Constant value equal to: {@value}
	 */
	public final static String FALSE               = "false";
  /**
   * Constant value equal to: {@code jdbc:oracle:thin:@ldap:\\/\\/.+\\/([A-Za-z]+),.+}
   */
	private final static Pattern JDBC_URL_RX       = Pattern.compile("jdbc:oracle:thin:@ldap:\\/\\/.+\\/([A-Za-z]+),.+");
	
	/**
	 * The name of the table upon which sqlldr will act
	 */
	private String   table;
	/**
	 * The path to the sqlldr control file
	 */
	private String   ctlFilePath;
	/**
	 * The column list for the load spec
	 */
	private String[] columns;
	/**
	 * A map of sqlldr options name/value pairs 
	 */
	private Map<String,String> options;
	/**
	 * A timestamp used as a unique id for file naming
	 */
	private String   loadId     = String.valueOf(new Date().getTime());
	/**
	 * The application identifier, defaults to {@link #YADA}
	 */
	private String   appId      = YADA;
	/**
	 * The directory to write the artifacts, defaults to {@link #TMP}
	 */
	private String   dir        = TMP;
	/**
	 * The column separator, defaults to {@link #COMMA}.
	 */
	private String   colSepChar = COMMA;
	/**
	 * The sqlldr action, defaults to {@link #APPEND}
	 */
	private String   method     = APPEND;
	/**
	 * The jndi string from which to obtain connection info, defaults to {@link #YADA_DATASOURCE}
	 */
	private String   dataSource = YADA_DATASOURCE;
	/**
	 * Flag for obtaining feedback, defaults to {@link #TRUE}
	 */
	private String   feedback   = TRUE;
	
	
	/**
	 * Default constructor
	 */
	public SqlldrUtil()
	{
		
	}
	
	/**
	 * Loaded constructor, sets all ivars.
	 * @param loadId a unique id for the load
	 * @param appId the id of the calling or related app
	 * @param table the table in which to load
	 * @param columns the column list
	 * @param options the sqlddr options names and values
	 * @param method the load method. see {@link #setMethod(String)}
	 */
	public SqlldrUtil(String loadId, String appId, String table, String[] columns, Map<String,String> options, String method)
	{
		setLoadId(loadId);
		setAppId(appId);
		setTable(table);
		setColumns(columns);
		setOptions(options);
		setMethod(method);
	}
	
	/**
	 * Standard mutator for variable
	 * @param dataSource the JNDI string for obtaining connection info
	 */
	public void setDataSource(String dataSource)
	{
		this.dataSource = dataSource;
	}
	
	/**
	 * Standard mutator for variable
	 * @param ctlFilePath the path to the control file
	 */
	public void setCtlFilePath(String ctlFilePath)
	{
		this.ctlFilePath = ctlFilePath;
	}
	/**
	 * The load method, one of {@link #INSERT}, {@link #APPEND}, {@link #REPLACE}, {@link #TRUNCATE}
	 * @param method the sqlldr action 
	 */
	public void setMethod(String method)
	{
		this.method = method;
	}
	/**
	 * Set the column separator character in the dat file
	 * @param c the column separator character in the dat file
	 */
	public void setColSepChar(char c)
	{
		this.colSepChar = String.valueOf(c);
	}
	/**
	 * Set the column separator character in the dat file
	 * @param c the column separator character in the dat file
	 */
	public void setColSepChar(String c)
	{
		this.colSepChar = c;
	}

	/**
	 * Set the directory where the sqlldr artifacts will be written
	 * @param dir the directory
	 */
	public void setDir(String dir)
	{
		this.dir = dir;
	}
	/**
	 * A unique id for the load, can be any value
	 * @param loadId the id
	 */
	public void setLoadId(String loadId)
	{
		this.loadId = loadId;
	}
	/**
	 * Set the id of the calling app.
	 * @param appId a string representing the calling app
	 */
	public void setAppId(String appId)
	{
		this.appId = appId;
	}
	/**
	 * Set the table into which the data will be loaded
	 * @param table the table name
	 */
	public void setTable(String table)
	{
		this.table = table;
	}
	/**
	 * Set the list of columns
	 * @param columns the list of calumns
	 */
	public void setColumns(String[] columns)
	{
		this.columns = columns;
	}
	/**
	 * Set the sqlldr options 
	 * @param options the option map
	 */
	public void setOptions(Map<String,String> options)
	{
		this.options = options;
	}
	
	/**
	 * Converts the options map into a string for writing into the ctl file
	 * @return the options
	 */
	public String getOptionsString()
	{
		StringBuffer o = new StringBuffer();
		o.append(OPTIONS+PAREN_OPEN);
		int size = this.options.keySet().size();
		int i    = 1;
		for (String option : this.options.keySet())
		{
			o.append(option);
			o.append(EQUAL);
			o.append(this.options.get(option));
			if (i < size - 1)
				o.append(COMMA);
		}
		o.append(PAREN_CLOSE);
		return o.toString();
	}
	
	/**
	 * Converts the column array into a string
	 * @return the column list as a string
	 */
	public String getColumnString()
	{
		StringBuffer colStr = new StringBuffer();
		colStr.append(PAREN_OPEN);
		for(int k=0;k<this.columns.length;k++)
		{
			colStr.append(this.columns[k]);
			if (k < this.columns.length - 1)
				colStr.append(COMMA);
		}
		colStr.append(PAREN_CLOSE);
		return colStr.toString();
	}
	
	/**
	 * Creates the sqlldr ctl file.
	 */
	public void createControlFile()
	{
		File ctlFile = new File(TMP+File.separator+this.loadId+this.appId+CTL_FILE);
		try(BufferedWriter out = new BufferedWriter(new FileWriter(ctlFile))) 
		{	
			out.write(getOptionsString());
			out.newLine();
			out.write(LOAD_DATA);
			out.write(INFILE+QUOTE+this.dir+File.separator+this.loadId+this.appId+DAT_FILE+QUOTE);
			out.newLine();
			out.write(BADFILE+QUOTE+this.dir+File.separator+this.loadId+this.appId+BAD_FILE+QUOTE);
			out.newLine();
			out.write(this.method);
			out.newLine();
			out.write(INTO_TABLE+this.table);
			out.newLine();
			out.write(TERM_STRING+APOS+this.colSepChar+APOS);
			out.newLine();
			out.write(getColumnString());
			out.newLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setCtlFilePath(ctlFile.getAbsolutePath());
	}
	
	/**
	 * A utility to write stdin and stderr to stdout
	 * @param in stdin or stderr
	 * @param out stdout
	 * @throws IOException if the {@code in} or {@code out} cannot be read or written
	 */
	
	private void copy(InputStream in, OutputStream out) throws IOException 
    {
		while (true) 
		{
			int c = in.read();
			if (c == -1) break;
			out.write((char)c);
	   }
	}
	
	/**
	 * //TODO implement printFeedback method 
	 * @param log the log line to write to the output
	 * @since PROVISIONAL
	 */
	public void printFeedback(String log)
	{
		// will implement
	}
	
	/**
	 * Launches the sqlldr executable using all the settings in this object
	 */
	public void invokeSqlldr()
	{
		try 
		{
			Context          ctx = new InitialContext();
			HikariDataSource ds  = (HikariDataSource)ctx.lookup(this.dataSource);
			
			String          usr = ds.getUsername();
			String          pwd = ds.getPassword();
			String          db  = "";
			String          url = ds.getJdbcUrl();
			Matcher         m   = JDBC_URL_RX.matcher(url);
			if (m.matches())
				db = m.group(1);
			String          logFile = TMP+File.separator+this.loadId+this.appId+LOG_FILE;
			String          uid = " "+usr+"/"+pwd+"@"+db;
			String          ctl = ", control="+this.ctlFilePath;
			String          log = ", log="+logFile;		
			String[]        cmd = {SQLLDR, uid, ctl, log};
			Runtime         r   = Runtime.getRuntime();
			l.info("Invoking sqlldr with cmd line ["+SQLLDR+uid+ctl+log+"]");
			Process         p   = r.exec(cmd);
			copy(p.getInputStream(), System.out);
			copy(p.getErrorStream(), System.out);
			int exit = p.waitFor();
			l.info("Finished loading ["+this.ctlFilePath+"] with exit code ["+String.valueOf(exit)+"]");
			if (this.feedback.equals(TRUE))
			{
				printFeedback(logFile);
			}
		}
		catch(Exception e)
		{
		    e.printStackTrace();
		}
	}
}
