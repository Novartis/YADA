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
import java.io.OutputStreamWriter;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
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
	protected final static String  PARAM_SYMBOL_RX = "([\\/=:~])(\\?[idvnt])";

	/**
	 * Constant equal to: {@code ".+"+PARAM_SYMBOL_RX+".*"}
	 */
	protected final static Pattern PARAM_URL_RX    = Pattern.compile(".+"+PARAM_SYMBOL_RX+".*");

	/**
	 * Constant equal to: {@value}
	 * {@link JSONParams} key for delivery of {@code HTTP POST, PUT, PATCH} body content
	 * @since 8.5.0
	 */
	private final static String YADA_PAYLOAD       = "YADA_PAYLOAD";

	/**
	 * Constant equal to: {@value}
	 * Workaround for requests using {@code HTTP PATCH}
	 * @since 8.5.0
	 */
	private final static String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";

	/**
	 * Variable to hold the proxy server string if necessary
	 */
	private String proxy = null;

	/**
	 * Variable denoting the HTTP method
	 * @since 8.5.0
	 */
	private String method = YADARequest.METHOD_GET;


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
		if(yadaReq.getMethod() != null && !yadaReq.getMethod().equals(YADARequest.METHOD_GET))
		{
			this.method = yadaReq.getMethod();
		}

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
		boolean isPostPutPatch = this.method.equals(YADARequest.METHOD_POST)
																|| this.method.equals(YADARequest.METHOD_PUT)
																|| this.method.equals(YADARequest.METHOD_PATCH);
		resetCountParameter(yq);
		int rows = yq.getData().size() > 0 ? yq.getData().size() : 1;
		/*
		 * Remember:
		 * A row is an set of YADA URL parameter values, e.g.,
		 *
		 *  x,y,z in this:
		 *    ...yada/q/queryname/p/x,y,z
		 *  so 1 row
		 *
		 *  or each of {col1:x,col2:y,col3:z} and {col1:a,col2:b,col3:c} in this:
		 *    ...j=[{qname:queryname,DATA:[{col1:x,col2:y,col3:z},{col1:a,col2:b,col3:c}]}]
		 *  so 2 rows
		 */
		for(int row=0;row<rows;row++)
		{
			String result = "";

			// creates result array and assigns it
			yq.setResult();
			YADAQueryResult yqr    = yq.getResult();


			String          urlStr = yq.getUrl(row);


			for (int i=0;i<yq.getParamCount(row);i++)
			{
				Matcher m = PARAM_URL_RX.matcher(urlStr);
				if(m.matches())
				{					
					String param = yq.getVals(row).get(i);
					//urlStr = urlStr.replaceFirst(PARAM_SYMBOL_RX,m.group(1)+param);
					StringBuffer sb = new StringBuffer();
					m.appendReplacement(sb, m.group(1)+param);
					m.appendTail(sb);
					urlStr = sb.toString();
				}
			}

			l.debug("REST url w/params: ["+urlStr+"]");
			try
			{
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

				// basic auth
				if (url.getUserInfo() != null)
				{
					//TODO issue with '@' sign in pw, must decode first
				  String basicAuth = "Basic " + new String(new Base64().encode(url.getUserInfo().getBytes()));
				  conn.setRequestProperty("Authorization", basicAuth);
				}

				// cookies
				if(yq.getCookies() != null && yq.getCookies().size() > 0)
				{
				  String cookieStr = "";
				  for (HttpCookie cookie : yq.getCookies())
				  {
				    cookieStr += cookie.getName()+"="+cookie.getValue()+";";
				  }
				  conn.setRequestProperty("Cookie", cookieStr);
				}

				if(yq.getHttpHeaders() != null && yq.getHttpHeaders().length() > 0)
				{
					l.debug("Processing custom headers...");
					@SuppressWarnings("unchecked")
					Iterator<String> keys = yq.getHttpHeaders().keys();
					while(keys.hasNext())
					{
						String name = keys.next();
						String value = yq.getHttpHeaders().getString(name);
						l.debug("Custom header: "+name+" : "+value);
						conn.setRequestProperty(name, value);
						if(name.equals(X_HTTP_METHOD_OVERRIDE) && value.equals(YADARequest.METHOD_PATCH))
						{
							l.debug("Resetting method to ["+YADARequest.METHOD_POST+"]");
							this.method = YADARequest.METHOD_POST;
						}
					}
				}


				HttpURLConnection hConn = (HttpURLConnection)conn;
				if(!this.method.equals(YADARequest.METHOD_GET))
				{
					hConn.setRequestMethod(this.method);
					if(isPostPutPatch)
					{
						//TODO make YADA_PAYLOAD case-insensitive and create an alias for it, e.g., ypl
						// NOTE: YADA_PAYLOAD is a COLUMN NAME found in a JSONParams DATA object.  It
						//       is not a YADA param
						String payload = yq.getDataRow(row).get(YADA_PAYLOAD)[0];
						hConn.setDoOutput(true);
						OutputStreamWriter writer;
						writer = new OutputStreamWriter(conn.getOutputStream());
						writer.write(payload.toString());
				    writer.flush();
					}
				}

				// debug
				Map<String, List<String>> map = conn.getHeaderFields();
				for (Map.Entry<String, List<String>> entry : map.entrySet()) {
					l.debug("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
				}

				try(BufferedReader in = new BufferedReader(new InputStreamReader(hConn.getInputStream())))
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
