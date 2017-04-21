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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;


/**
 * For connecting to REST endpoints with YADA.
 * @author David Varon
 *
 */
public class RESTAdaptor extends Adaptor {

	/**
	 * Local logger handle
	 */
	private static Logger l = Logger.getLogger(RESTAdaptor.class);
	/**
	 * Constant equal to: {@value}
	 */
	protected final static String  PARAM_SYMBOL_RX = "([\\/=:])(\\?[idvnt])";
	
	/**
	 * Constant equal to: {@code ".+"+PARAM_SYMBOL_RX+".*"}
	 */
	protected final static Pattern PARAM_URL_RX    = Pattern.compile(".+"+PARAM_SYMBOL_RX+".*");
	
	/**
	 * Variable to hold the proxy server string if necessary
	 */
	private String proxy = null;
	/**
	 * Default constructor
	 */
	public RESTAdaptor() {
		super();
		l.debug("Initializing REST JDBCAdaptor");
	}
	
	/**
	 * The yadaReq constructor
	 * @param yadaReq YADA request configuration
	 */
	public RESTAdaptor(YADARequest yadaReq)
	{
		super(yadaReq);
		if(yadaReq.getProxy() != null && !yadaReq.getProxy().equals(""))
		{
			this.proxy = yadaReq.getProxy();
		}
	}
	
	/**
	 * Tests if proxy has been set in {@link YADARequest#setProxy(String[])}
	 * @return {@code true} if {@link #proxy} is not null or an empty {@link String}
	 */
	protected boolean hasProxy()
	{
		if(this.proxy != null && !this.proxy.equals(""))
			return true;
		return false;
	}

	/**
	 * Gets the input stream from the {@link URLConnection} and stores it in 
	 * the {@link YADAQueryResult} in {@code yq}
	 * @see com.novartis.opensource.yada.adaptor.Adaptor#execute(com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public void execute(YADAQuery yq) throws YADAAdaptorExecutionException
	{
		resetCountParameter(yq);
		int rows = yq.getData().size() > 0 ? yq.getData().size() : 1;
		for(int row=0;row<rows;row++)
		{
			String result = "";
			yq.setResult();
			YADAQueryResult yqr    = yq.getResult();
			String          urlStr = yq.getUrl(row);
			for (int i=0;i<yq.getParamCount(row);i++)
			{
				Matcher m = PARAM_URL_RX.matcher(urlStr);
				if(m.matches())
				{
					String param = yq.getVals(row).get(i);
					urlStr = urlStr.replaceFirst(PARAM_SYMBOL_RX,m.group(1)+param);
				}
			}
			
			l.debug("REST url w/params: ["+urlStr+"]");
			try {
				URL           url  = new URL(urlStr);
				URLConnection conn = null;
				
				if(this.hasProxy())
				{
					String[] proxyStr = this.proxy.split(":");
					Proxy    proxySvr = new Proxy(Proxy.Type.HTTP, 
												  new InetSocketAddress(proxyStr[0], Integer.parseInt(proxyStr[1])));
					conn = url.openConnection(proxySvr);
				}
				else
				{
					conn = url.openConnection();
				}
				
				if (url.getUserInfo() != null) 
				{
					// TODO issue with '@' sign in pw, must decode first
				    String basicAuth = "Basic " + new String(new Base64().encode(url.getUserInfo().getBytes()));
				    conn.setRequestProperty("Authorization", basicAuth);
				}
				
				if(yq.getCookies() != null && yq.getCookies().size() > 0)
				{
				  String cookieStr = "";
				  for (HttpCookie cookie : yq.getCookies())
				  {
				    cookieStr += cookie.getName()+"="+cookie.getValue()+";";
				  }
				  conn.setRequestProperty("Cookie", cookieStr);
				}
				
				Map<String, List<String>> map = conn.getHeaderFields();
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					l.debug("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
				}
				
				try(BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream())))
				{
				  String 		   inputLine;
				  while ((inputLine = in.readLine()) != null)
	        {
	          result += String.format("%1s%n",inputLine);
	        }
				}
				yqr.addResult(row, result);
			} 
			catch (MalformedURLException e) 
			{	
				String msg = "Unable to access REST source due to a URL issue.";
				throw new YADAAdaptorExecutionException(msg, e);
			} 
			catch (IOException e) 
			{
				String msg = "Unable to read REST response.";
				throw new YADAAdaptorExecutionException(msg, e);
			}
		}
	}
	
	/**
	 * Constructs a single url string containing the REST endpoint and query string
	 * 
	 * @param yq the {@link YADAQuery} containing the source code and metadata required to construct an executable query
	 * @return a string containing the entire REST url
	 */
	@Override
	public String build(YADAQuery yq) {
	  String conf   = ConnectionFactory.getConnectionFactory().getWsSourceMap().get(yq.getApp());
		String uri    = yq.getYADACode();
		return conf + uri;
	}
}
