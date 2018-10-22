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
	private   final static String  PROTOCOL        = "file://";
	/**
   * Constant equal to: {@code ([\\/=:&lt;])(\\?[idvn])}
   */
	protected final static String  PARAM_SYMBOL_RX = "([\\/=:<])(\\?[idvn])";
	/**
   * Constant equal to: {@code &lt;&lcub;1,2&rcub;.*}
   */
	protected final static String  NON_READ_SFX_RX = "<{1,2}.*";
	/**
   * Constant equal to: {@code ".+"+PARAM_SYMBOL_RX+".*"}
   */
	protected final static Pattern PARAM_URL_RX    = Pattern.compile(".+"+PARAM_SYMBOL_RX+".*");
	/**
   * Constant equal to: {@code "^file:\\/\\/\\/(io\\/(in|out))(\\/.+)*$"}
   */
	protected final static Pattern SOURCE_RX       = Pattern.compile("^file:\\/\\/\\/(io\\/(in|out))(\\/.+)*$");	
	
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
	 * Currently supports directory READ, only that return single-level depth
	 * and WRITE and APPEND operations for single files. WRITEs can be performed
	 * on non-existent files, and currently there is no security attached to 
	 * ensure non-executables only can be written.
	 * @throws YADAAdaptorExecutionException when the execution fails as a result of an IO error or Resource error, e.g., a problem accessing or reading a file
	 */
	//TODO support parameters for filesystem depth
	@Override
	public void execute(YADAQuery yq) throws YADAAdaptorExecutionException
	{
		Object result = null;
		resetCountParameter(yq);
		for(int row=0;row<yq.getData().size();row++)
		{
			yq.setResult();
			YADAQueryResult yqr    = yq.getResult();
			yqr.setApp(yq.getApp());
			String          urlStr = yq.getUrl(row);
			
			for(int i=0;i<yq.getParamCount(row);i++)
			{
				String param = "";
				Matcher m = PARAM_URL_RX.matcher(urlStr);
				if(m.matches())
				{
					param = yq.getVals(row).get(i);
					if(i==yq.getParamCount(row)-1                                 // last param
							&& (yq.getType().equals(QueryUtils.WRITE) || yq.getType().equals(QueryUtils.APPEND))) // non-read
					{
						urlStr = urlStr.replaceFirst(NON_READ_SFX_RX, "");
						setData(param);
					}
					else
					{
						urlStr = urlStr.replaceFirst(PARAM_SYMBOL_RX,m.group(1)+param);
					}
				}
				
			}
			
			File f = new File(urlStr.replace(PROTOCOL, ""));
			
			//TODO Prevent writing of unix executables. does this require simply checking for hashbang?
			//TODO Ensure no written files have x privs
			//TODO Enable creation of directories, maybe.
			if(yq.getType().equals(QueryUtils.WRITE) || yq.getType().equals(QueryUtils.APPEND))
			{
				boolean append = yq.getType().equals(QueryUtils.APPEND);
				try(FileWriter out = new FileWriter(f,append)) 
				{
			    
			    out.write(getData());
			    out.flush();
			  } 
				catch (IOException e) 
				{
					String msg = "There was a problem writing the file.";
					throw new YADAAdaptorExecutionException(msg,e);
				}
			}
			else
			{
			  if(f.isDirectory())
			  {
			    result = FileUtils.getFileList(f,0);
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
		String conf   = ConnectionFactory.getConnectionFactory().getWsSourceMap().get(yq.getApp());
		String uriStr = yq.getYADACode();
		String env    = "";
		try
		{
			Matcher m   = SOURCE_RX.matcher(conf);
	
			if(m.matches())
			{
				try
				{
					env  = Finder.getEnv(m.group(1));
				} 
				catch (YADAResourceException e)
				{
				  String msg = "The JNDI Resource could not be found.";
				  throw new YADAAdaptorException(msg, e);
				}
			}
//			Matcher m1  = URI_RX.matcher(uriStr);
//			if(m1.matches()) 
//			{
//				if(m1.groupCount() > 1 && m1.group(3) != null)
//				{
//					setType(m1.group(3));
//				}
//			}
		}
		catch(Exception e)
		{
			String msg = "There was a problem with the source or uri syntax";
			throw new YADAAdaptorException(msg, e);
		}
		return env + uriStr;
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
