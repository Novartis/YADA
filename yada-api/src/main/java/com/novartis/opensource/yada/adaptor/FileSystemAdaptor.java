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
package com.novartis.opensource.yada.adaptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.Finder;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADAResourceException;
import com.novartis.opensource.yada.io.YADAIOException;
import com.novartis.opensource.yada.util.FileUtils;
import com.novartis.opensource.yada.util.QueryUtils;

/**
 * An adaptor for reading from and writing to safe, designated directories on the YADA server.
 * @author David Varon
 * @since 4.0.0
 */
public class FileSystemAdaptor extends Adaptor
{
  /**
   * Local logger handle
   */
	private static Logger l = Logger.getLogger(FileSystemAdaptor.class);	
	/**
	 * Constant equal to: {@value}
	 */
	protected final static String  PROTOCOL        = "file://";
	/**
   * Constant equal to: {@code \\?[idvn]}
   */
  protected final static String  PARAM_MARKUP_RX = "\\?[idvn]";
	/**
   * Constant equal to: {@code ([\\/=:&lt;])(\\?[idvn])}
   */
  protected final static String  PARAM_SYMBOL_RX = "([\\/=:<])("+PARAM_MARKUP_RX+")";
	/**
   * Constant equal to: {@code &lt;&lcub;1,2&rcub;.*}
   */
	protected final static String  NON_READ_SFX_RX = "<{1,2}.*";
	/**
   * Constant equal to: {@code ".+([\\/=:<])(\\?[idvn]).*"}
   */
	protected final static Pattern PARAM_URL_RX    = Pattern.compile(".+"+PARAM_SYMBOL_RX+".*");
	/**
   * Constant equal to: {@code "^file:\\/\\/\\/(io\\/(?:in|out\\/))?(.+)?$"}
   */
	protected final static Pattern SOURCE_RX       = Pattern.compile("^file:\\/\\/\\/(io\\/(?:in|out\\/))?(.+)?$");	
	
//	/**
//	 * Variable for storing file system access mode, either {@link #READ} (default), {@link #WRITE}, or {@link #APPEND}
//	 */
//	private String type = READ;
	/**
	 * The request query string
	 */
	private String uri;
	/**
	 * The content to be written to the file when the query type is {@link QueryUtils#WRITE} or {@link QueryUtils#APPEND}
	 */
	private String data;
	
	/**
	 * Default constructor.
	 */
	public FileSystemAdaptor()
	{
		super();
		l.debug("Initializing FileSystemAdaptor");
	}

	/**
	 * Preferred "YADARequest" constructor.
	 * @param yadaReq YADA request configuration
	 */
	public FileSystemAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
	}
	
	/**
	 * Currently supports directory READ, WRITE, and APPEND operations 
	 * for single files or directories. WRITEs can be performed on non-existent 
	 * files.  All files written are stored as non-executable.
	 * @throws YADAAdaptorExecutionException when the execution 
	 *   fails as a result of an IO error or Resource error, 
	 *   e.g., a problem accessing or reading a file
	 */
	@Override
	public void execute(YADAQuery yq) throws YADAAdaptorExecutionException
	{
	  boolean isWrite = yq.getType().equals(QueryUtils.WRITE);
	  boolean isAppend = yq.getType().equals(QueryUtils.APPEND);
		Object result = null;
		resetCountParameter(yq);
		for(int row=0;row<yq.getData().size();row++)
		{
			yq.setResult();
			YADAQueryResult yqr    = yq.getResult();			
			String          urlStr = yq.getUrl(row);			
			String[]        parts  = urlStr.split(PARAM_MARKUP_RX);
			StringBuffer    urlOut = new StringBuffer();
			yqr.setApp(yq.getApp());
			for(int i=0;i<yq.getParamCount(row);i++)
			{
				String param = "";
				// 2020-12-05 DV new logic to support depth
				// if matches (urlStr contains either markup preceded by a symbol e.g., /=:<)
				//   if (markup mark), replace first
				//   else (append or write mark), replace first with empty string and setData(param)
				
				
				Matcher m = PARAM_URL_RX.matcher(urlStr);
				if(m.matches())
				{				  
					param = yq.getVals(row).get(i);
					// are we at the last param and in write or append mode?
					if(i==yq.getParamCount(row)-1 && (isWrite || isAppend)) // last param and non-read
					{
						setData(param);
						if(yq.getParamCount(row) == 1)
						  urlOut.append(parts[i].replaceAll("<",""));
					}
					else
					{
					  urlOut.append(parts[i]);					  
					  urlOut.append(param);
					}
				}
			}
			// file handle for writes
			File f = new File(urlOut.toString().replace(PROTOCOL, ""));
			

			if(isWrite || isAppend)
			{
			  if(isWrite)
	      {
			    // check parent dirs exist and create if necessary
	        File d = f.getParentFile();               
	        if(!d.exists())
	          d.mkdirs();
	      }
				try(FileWriter out = new FileWriter(f,isAppend)) 
				{
			    out.write(getData());
			    out.flush();
			  } 
				catch (IOException e) 
				{
					String msg = "There was a problem writing the file.";
					throw new YADAAdaptorExecutionException(msg,e);
				}
				Path fp = f.toPath();
				// Check if new file is executable and remove those privs if so
				if(Files.isExecutable(fp))
				{				  
				  try
				  {
            String perms = PosixFilePermissions.toString(Files.getPosixFilePermissions(fp, new LinkOption[] {LinkOption.NOFOLLOW_LINKS}));
            Files.setPosixFilePermissions(fp, PosixFilePermissions.fromString(perms.replace("x","-")));
          }
          catch (IOException e)
          {
             throw new YADAAdaptorExecutionException(e);
          }
				}
			}
			else // read stuff
			{
			  if(f.isDirectory())
			  {
			    result = FileUtils.getFileList(f,-1);
			  }
			  else
			  {
			    try 
			    {
            result = FileUtils.getText(f);
          } 
			    catch (YADAIOException e) 
			    {
			      throw new YADAAdaptorExecutionException(e);
          }
			    catch (YADAResourceException e) 
          {
            throw new YADAAdaptorExecutionException(e);
          }
			  }
			}
			yqr.addResult(row, result);
		}		
	}
	
	/**
	 * Returns the full path to be processed by the adaptor
	 * @param yq the query containing the code to build
	 * 
	 * @return the path to the desired file system location
	 * @throws YADAAdaptorException if there was an issue with accessing the JNDI source stored in {@code yq}
	 */
	@Override
	public String build(YADAQuery yq) throws YADAAdaptorException {
	  Properties props = ConnectionFactory.getConnectionFactory().getWsSourceMap().get(yq.getApp());	  
		String source = props.getProperty(ConnectionFactory.YADA_CONF_SOURCE); 
		String uriStr = yq.getYADACode();
		String path   = "";
		try
		{
			Matcher m   = SOURCE_RX.matcher(source); // ^file:\\/\\/\\/(io\\/(?:in|out\\/))?(.+)?$
	
			if(m.matches() && !(null == m.group(1) || "".contentEquals(m.group(1))) ) // has io/in or io/out subdir in source
			{
				try
				{
					path  = Finder.getEnv(m.group(1));
				} 
				catch (YADAResourceException e)
				{
				  String msg = "The JNDI Resource could not be found.";
				  throw new YADAAdaptorException(msg, e);
				}
			}			  
			else
			{  
			  path = source;
			}
		}
		catch(Exception e)
		{
			String msg = "There was a problem with the source or uri syntax";
			throw new YADAAdaptorException(msg, e);
		}
		return path + uriStr;
	}
	
//	/**
//	 * Standard mutator for variable.
//	 * @param type
//	 */
//	public void setType(String type) {
//		this.type = type;
//	}
//	
//	/**
//	 * Returns the type of the query, based on parsing of the {@code uri}.  Possible values are {@link #READ},
//	 * {@link #WRITE}, or {@link #APPEND}
//	 * @return the type of query
//	 */
//	public String getType() {
//		return this.type;
//	}
	
	/**
	 * Standard mutator for variable.
	 * @param uri the url path and parameter portion of the request string
	 */
	public void setUri(String uri) {
		this.uri = uri;
	}
	
	/**
	 * Standard accessor for variable.
	 * @return the url portion of the query
	 */
	public String getUri() {
		return this.uri;
	}

	/**
	 * Standard mutator for variable.  {@code data} is passed as a url parameter in the query string
	 * and ultimately written or appended to a file.
	 * @param data the content to write or append
	 */
	public void setData(String data) {
		this.data = data;
	}
	
	/**
	 * Standard accessor for variable.
	 * @return the data to be written to the filesystem
	 */
	public String getData() {
		return this.data;
	}

}
