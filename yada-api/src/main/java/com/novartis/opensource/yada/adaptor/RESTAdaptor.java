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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.javanet.NetHttpTransport;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;
import com.novartis.opensource.yada.YADASecurityException;

import org.json.JSONException;
import org.json.JSONObject;


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
	 * Constant equal to: {@value}.  The character set name.
	 * @since 8.7.0
	 */
	private final static String PROP_KEYSTORE          = "javax.net.ssl.keyStore";
	
	/**
	 * Constant equal to: {@value}.  The keystore password passed to the jvm.
	 * @since 8.7.0
	 */
	private final static String PROP_KEYSTORE_PASS     = "javax.net.ssl.keyStorePassword";
	
	/**
	 * Constant equal to: {@value}.  The {@code oauth} request parameter property name.
	 * @since 8.7.0
	 */
	private final static String OAUTH_VERSION          = "oauth_version";
	
	/**
	 * Constant equal to: {@value}.  The {@code oauth} request parameter property name.
	 * @since 8.7.0
	 */
	private final static String OAUTH_SIGNATURE_METHOD = "oauth_signature_method";
	
	/**
	 * Constant equal to: {@value}.  The {@code oauth} request parameter property name.
	 * @since 8.7.0
	 */
	private final static String OAUTH_VERIFIER         = "oauth_verifier";
	
	/**
	 * Constant equal to: {@value}.  The {@code oauth} request parameter property name.
	 * @since 8.7.0
	 */
	private final static String OAUTH_PRIVATE_KEY      = "oauth_private_key";
	
	/**
	 * Constant equal to: {@value}.  The {@code oauth} request parameter property name.
	 * @since 8.7.0
	 */
	private final static String OAUTH_CONSUMER_KEY     = "oauth_consumer_key";
	
	/**
	 * Constant equal to: {@value}.  The {@code oauth} request parameter property name.
	 * @since 8.7.0
	 */
	private final static String OAUTH_TOKEN            = "oauth_token";
	
	/**
	 * Constant equal to: {@value}
	 */
	protected final static String  PARAM_SYMBOL_RX = "([\\/=:~]|%(?:2F|3D|3A|7E))(\\?[idvnt])";

	/**
	 * Constant equal to: {@value}
	 * {@link com.novartis.opensource.yada.JSONParams} key for delivery of {@code HTTP POST, PUT, PATCH} body content
	 * @since 8.5.0
	 */
	private final static String YADA_PAYLOAD = "YADA_PAYLOAD";

	/**
	 * Constant equal to: {@value}. 
	 * Workaround for requests using {@code HTTP PATCH}
	 * @since 8.5.0
	 */
	private final static String H_X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
	
	/**
	 * Constant equal to: {@value}. For retrieving the default keystore.
	 */
	private static final String KEYSTORE_TYPE_JKS = "jks";

	/**
	 * Constant equal to: {@value}. 
	 * @since 8.7.4
	 */
	private static final int HTTP_STATUS_401 = 401;
	
	/**
	 * Constant equal to: {@value}. 
	 * @since 8.7.4
	 */
	private static final int HTTP_STATUS_403 = 403;

	/**
	 * Variable to hold the proxy server string if necessary
	 */
	private String proxy = null;

	/** 
	 * Variable to hold the oauth parameters
	 */
	private JSONObject oauth = null;
	
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
	 * The yadaReq constructor called by {@link com.novartis.opensource.yada.QueryManager#endowQuery}
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
		
		if(yadaReq.getOAuth() != null)
		{
			setOAuth(yadaReq.getOAuth());
		}
	}

	/**
	 * Standard mutator for variable
	 * @param oauth the JSONObject store in the {@link YADARequest}
	 * @since 8.7.1
	 */
	private void setOAuth(JSONObject oauth) {
		this.oauth = oauth;
	}
	
	/**
	 * Standard mutator for variable which converts {@link String} to {@link JSONObject}
	 * @param yq The {@link YADAQuery} containing the {@code oauth} or {@code o} parameter
	 * @since 8.7.1
	 */
	private void setOAuth(YADAQuery yq) {
		this.oauth = new JSONObject(yq.getYADAQueryParamValue(YADARequest.PS_OAUTH)[0]);
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
	 * Creates the {@link YADAQueryResult} and adds it to the {@link YADAQuery}
	 * @param yq The {@link YADAQuery} to process
	 * @since 8.7.0
	 */
	private void setYADAQueryResultForYADAQuery(YADAQuery yq) {
		yq.setResult();
		YADAQueryResult yqr    = yq.getResult();
		yqr.setApp(yq.getApp());
	}

	/**
	 * Replaces YADA markup in the url query string with values from the request
	 * @param yq The {@link YADAQuery} to process
	 * @param row The current collection of query parameter values
	 * @return the fully fleshed-out query string
	 * @since 8.7.0
	 */
	private String setPositionalParameterValues(YADAQuery yq, int row) {
		String          urlStr = yq.getUrl(row);
		Matcher m = Pattern.compile(PARAM_SYMBOL_RX).matcher(urlStr);
		StringBuffer sb    = new StringBuffer();
		int i=0;
		while(m.find())
		{		
			String param = yq.getVals(row).get(i++);
			String repl  = m.group(1)+param;
			m.appendReplacement(sb, repl);
		}
		m.appendTail(sb);
		urlStr = sb.toString();
		l.debug("REST url w/params: ["+urlStr+"]");
		return urlStr;
	}
	
	/**
	 * Determines if OAuth or Basic Authentication should be applied given the {@link YADARequest} 
	 * configuration, and proceeds accordingly. 
	 * @param yq The query object defined in the {@link YADARequest}
	 * @param url The REST endpoint
	 * @return The {@link HttpRequestInitializer} to pass to the {@link HttpRequestFactory}
	 * @throws YADAQueryConfigurationException if both OAuth and Basic are defined in the request
	 * @throws YADASecurityException  if authentication fails
	 * @since 8.7.0
	 */
	private HttpRequestInitializer setAuthentication(YADAQuery yq, GenericUrl url) throws YADAQueryConfigurationException, YADASecurityException 
	{		
		if(yq.hasParam(YADARequest.PS_OAUTH) || yq.hasParam(YADARequest.PL_OAUTH))
		{
			if(url.getUserInfo() != null) 
			{
				String msg  = "A query cannot contain both basic and oauth configurations. "
						        + "When using oauth, make sure the REST data source is not also configured with "
						   		  + "basic auth values.";
				throw new YADAQueryConfigurationException(msg);
			}
			if(null == this.oauth) // not in the request
				setOAuth(yq);
			return setAuthenticationOAuth(url);			
		}
		else if(url.getUserInfo() != null)
		{
			return setAuthenticatonBasic(url);
		}
		return null;
	}
	
  /**
   * Generates a {@link PrivateKey} object from the encoded {@code privateKey}string obtained from the 
   * {@code JKS} keystore. The provided {@link KeyStore} is designated at application boot time by the JVM's
   * {@code javax.net.ssl.keyStore} and {@code javax.net.ssl.keyStorePassword} properties.s
   * under the provided {@code alias} 
   * @param alias the alias to the private key stored in the YADA keystore
   * @return the private key object
   * @throws NoSuchAlgorithmException when the JKS keystore can't be loaded
   * @throws KeyStoreException when there is no {@code JKS} keystore available
   * @throws IOException when the JKS keystore can't be loaded due to a filesystem issue
   * @throws CertificateException when the JKS keystore can't be loaded
   * @throws UnrecoverableEntryException when the desired entry can't be extracted
 	 * @since 8.7.0
   */
  private PrivateKey getPrivateKey(String alias) throws NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableEntryException
  {
  	String     ksPath = System.getProperty(PROP_KEYSTORE);
  	char[]     ksPass = System.getProperty(PROP_KEYSTORE_PASS).toCharArray();
  	KeyStore.ProtectionParameter ksProtParam = new KeyStore.PasswordProtection(ksPass);
  	File       ksFile = new File(ksPath);
  	URL        ksURL  = ksFile.toURI().toURL();
  	KeyStore   keyStore = KeyStore.getInstance(KEYSTORE_TYPE_JKS);  	
  	try(InputStream is = ksURL.openStream())
  	{
  		keyStore.load(is, null == ksPass ? null : ksPass);
  	}
  	KeyStore.PrivateKeyEntry ksEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, ksProtParam);
  	PrivateKey pk            = ksEntry.getPrivateKey();
  	
    return pk;
  }
  
  
  /**
   * Creates the {@link OAuthParameters} {@link HttpRequestInitializer}. 
   * This method relies on inclusion of {@link YADARequest#getOAuth()} which returns 
   * a {@link JSONObject} containing:
   * 
   *   <ul>
   *    <li><code>oauth_consumer_key</code></li>
   *    <li><code>oauth_signature_method</code>. For now, should always equal <code>RSA</code></li>
   *    <li><code>oauth_token</code></li>
   *    <li><code>oauth_verifier (secret)</code></li>
   *    <li><code>oauth_version</code>. For now, should always equal <code>1.0a</code></li>
   *    <li><code>oauth_private_key</code> which is the <span style="font-style:italic;">alias</span> 
   *    under which the desired <code>KeyStore.PrivateKeyEntry</code> is stored</li>
   *   </ul>
   *    
   * @param url the transformed (conformed) REST endpoint
   * @return the initializer to pass to the request factory
   * @throws YADASecurityException if there is an issue with the keystore or key entry
   * @throws YADAQueryConfigurationException if the 'oauth' request param is misconfigured
   * @since 8.7.0
   */
	private HttpRequestInitializer setAuthenticationOAuth(GenericUrl url) throws YADASecurityException, YADAQueryConfigurationException 
	{
    OAuthRsaSigner signer = new OAuthRsaSigner();
    
    try 
    {
			signer.privateKey = getPrivateKey(oauth.getString(OAUTH_PRIVATE_KEY));
		} 
    catch (NoSuchAlgorithmException|KeyStoreException|CertificateException|UnrecoverableEntryException|IOException e) 
    {
			String msg = "There was a problem loading the KeyStore or obtaining the designated key.";
			throw new YADASecurityException(msg,e);
		} 
    catch (JSONException e) 
    {
			String msg = "There was a problem obtaining the private key entry alias from the request. Check the 'oauth' request parameter for the 'oauth_private_key' property.";
			throw new YADAQueryConfigurationException(msg,e);
		}
    
    OAuthParameters oauthParams = new OAuthParameters();
    oauthParams.consumerKey = oauth.getString(OAUTH_CONSUMER_KEY);    
    oauthParams.signer = signer;
    oauthParams.signatureMethod = oauth.getString(OAUTH_SIGNATURE_METHOD); 
    oauthParams.token = oauth.getString(OAUTH_TOKEN);
    oauthParams.verifier = oauth.getString(OAUTH_VERIFIER);
    oauthParams.version = oauth.getString(OAUTH_VERSION);
    oauthParams.computeNonce();
    oauthParams.computeTimestamp();
    try 
    
    {
			oauthParams.computeSignature(this.method, url);
		} 
    catch (GeneralSecurityException e) 
    {
			String msg = "Unable to compute signature with the information provided. Check 'oauth' request parameter and key pair.";
			throw new YADASecurityException(msg, e);
		}
    
    return oauthParams;
	}
	
	/**
	 * Creates the {@link BasicAuthentication} {@link HttpRequestInitializer}
	 * @param url the conformed REST endpoint
	 * @return the initializer to pass to the request factory
	 * @since 8.7.0
	 */
	private HttpRequestInitializer setAuthenticatonBasic(GenericUrl url)  
	{
			//TODO issue with '@' sign in pw, must decode first (is this still an issue?)
			String[] userinfo = url.getUserInfo().split(":");
			String username = userinfo[0];
			String password = userinfo[1];
			BasicAuthentication ba = new BasicAuthentication(username, password);
			return ba;
	}
	
	/**
	 * Looks for cookie strings passed in the request or stored in the {@link YADAQuery} 
	 * and adds them to the {@link HttpRequest}
	 * @param yq The YADAQuery object
	 * @param request The HttpRequest object
	 * @since 8.7.0
	 */
	private void setCookies(YADAQuery yq, HttpRequest request)
	{
		if(yq.getCookies() != null && yq.getCookies().size() > 0)
		{
		  String cookieStr = "";
		  for (HttpCookie cookie : yq.getCookies())
		  {
		    cookieStr += cookie.getName()+"="+cookie.getValue()+";";
		  }
		  HttpHeaders headers = request.getHeaders();
		  headers.setCookie(cookieStr);
		  request.setHeaders(headers);
		}
	}
	
	/**
	 * Looks for http header strings passed in the request or stored in the {@link YADAQuery}
	 * and adds them to the {@link HttpRequest}
	 * @param yq the {@link YADAQuery} object
	 * @param request the HttpRequest object
	 * @throws YADAQueryConfigurationException 
	 * @since 8.7.0
	 */
	private void setHeaders(YADAQuery yq, HttpRequest request) throws YADAQueryConfigurationException
	{
		if(yq.getHttpHeaders() != null && yq.getHttpHeaders().length() > 0)
		{
			HttpHeaders headers = request.getHeaders();

			l.debug("Processing custom headers...");
			@SuppressWarnings("unchecked")
			Iterator<String> keys = yq.getHttpHeaders().keys();
			while(keys.hasNext())
			{
				String name = keys.next();
				String value = yq.getHttpHeaders().getString(name);
				l.debug("Custom header: "+name+" : "+value);
				
				String method = "set"+name.replace("-","");
				try 
				{
					Class<HttpHeaders> clazz = HttpHeaders.class;
					Method meth = clazz.getMethod(method, java.lang.String.class);
					meth.invoke(headers, value);
				} 
				catch (NoSuchMethodException|SecurityException e) 
				{
					String msg = "There is no method named ["+method+"]. It could be a case issue."
							       + "Method names are camel-cased, and header names should have initial " 
							       + " caps, and hyphens instead of spaces."; 
					l.warn(msg);
					l.warn("Trying to use generic setter");
					headers.set(name, value);
				} 
				catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) 
				{
					String msg = "The ["+name+"] header could not be set.";
					throw new YADAQueryConfigurationException(msg, e);
				} 
				request.setHeaders(headers);
			}
		}
	}
	
	/**
	 * Returns true if <code>method</code> matches <code>POST</code>, <code>PUT</code>, or <code>PATCH</code>
	 * @param method the http method of the request to check
	 * @return true if <code>method</code> matches <code>POST</code>, <code>PUT</code>, or <code>PATCH</code>
	 * @since 8.7.0
	 */
	protected static boolean isPostPutPatch(String method) 
	{
		return method.equals(YADARequest.METHOD_POST)
				|| method.equals(YADARequest.METHOD_PUT)
				|| method.equals(YADARequest.METHOD_PATCH);
	}
	
	/**
	 * Reads the response {@link InputStream} and writes it to a {@link String}
	 * @param request the request object
	 * @return a String containing the response data
	 * @throws YADAAdaptorExecutionException when reading the input stream fails
	 * @throws YADASecurityException when the service returns a 401 or 403
	 * @since 8.7.0
	 */ 
	private String processResponse(HttpRequest request) throws YADAAdaptorExecutionException, YADASecurityException 
	{
		String result = "";
		HttpResponse response = null;
		try 
		{
			response = request.execute();
		} 
		catch (HttpResponseException e)
		{
			int code = e.getStatusCode();
			if(code == HTTP_STATUS_401 || code == HTTP_STATUS_403)
			{
				String msg = "Unauthorized: "+e.getStatusMessage();
				throw new YADASecurityException(msg,e);
			}
				
		}
		catch (IOException e) 
		{
			String msg = "Unable to execute request.";
			throw new YADAAdaptorExecutionException(msg,e);
		}
		
		try(BufferedReader in = new BufferedReader(new InputStreamReader(response.getContent())))
		{
		  String 		   inputLine;
		  while ((inputLine = in.readLine()) != null)
      {
        result += String.format("%1s%n",inputLine);
      }
		} 
		catch (IOException e) 
		{
			String msg = "There was a problem reading from the response's input stream";
			throw new YADAAdaptorExecutionException(msg, e);
		}
		return result;
	}
	
	/**
	 * Writes the header fields to the console or log while in DEBUG mode
	 * @param request the current request object
	 * @since 8.7.0
	 */
	private void logRequest(HttpRequest request) {
		Map<String, Object> map = request.getHeaders();
		for (Entry<String, Object> entry : map.entrySet()) {
			l.debug("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
		}
	}
	
	/**
	 * Wrapper for {@link HttpRequestFactory#buildRequest(String, GenericUrl, HttpContent)}
	 * @param yq The {@link YADAQuery} defined by the {@link YADARequest}
	 * @param payload the request body or null 
	 * @param url the REST endpoint
	 * @return the initialized {@link HttpRequest}
	 * @throws YADAQueryConfigurationException when there is an issue with request construction
	 * @throws YADASecurityException when authentication fails
	 */
	private HttpRequest buildRequest(YADAQuery yq, String payload, GenericUrl url) throws YADAQueryConfigurationException, YADASecurityException
	{
		HttpContent            content        = null;
		HttpRequestInitializer initializer    = setAuthentication(yq, url);
		HttpRequestFactory     requestFactory = new NetHttpTransport().createRequestFactory(initializer);
		if(isPostPutPatch(this.method))
		{			
			content = ByteArrayContent.fromString(null, payload);
			if(this.method.equals(YADARequest.METHOD_PATCH))
				this.method = YADARequest.METHOD_PUT;
		}
		
		try
		{
			return requestFactory.buildRequest(this.method, url, content);
		} 
		catch (IOException e) 
		{
			String msg = "Unable to initialize the "+this.method+" request for ["+url+"]";
			throw new YADAQueryConfigurationException(msg,e);
		}
	}
	
	/**
	 * Gets the input stream from the {@link URLConnection} and stores it in
	 * the {@link YADAQueryResult} in {@code yq}
	 * @throws YADASecurityException if authentication fails
	 * @see com.novartis.opensource.yada.adaptor.Adaptor#execute(com.novartis.opensource.yada.YADAQuery)
	 */
	@Override
	public void execute(YADAQuery yq) throws YADAAdaptorExecutionException, YADASecurityException
	{
		//
		//   THINGS ORGANIZED IN HERE:
		//   
		//   Type of request
		//   		This will determine the 'build' method in the HttpRequestFactory (i.e., buildGet, buildPost, etc)
		//   
		//   Type of authentication
		// 			Basic, Oauth, None
		// 
		//   Post content
		//   Cookies
		//   Headers
		//   Proxy (config'd in JVM for now)
		//
		
		// 1) Override method type if necessary
		if(yq.getHttpHeaders().has(H_X_HTTP_METHOD_OVERRIDE))
		{			
			this.method = yq.getHttpHeaders().getString(H_X_HTTP_METHOD_OVERRIDE);
			l.debug("Resetting method to ["+this.method+"]");
		}
		
		// 2) set 'count' to false until the algo for counting rest response rows is impl.
		resetCountParameter(yq);
		
		// 3) get the data array from the YADAQuery
		//
	  // Remember:
	  // A row is a set of YADA URL parameter values, e.g.,
	  //
	  //  x,y,z in this:
	  //    ...yada/q/queryname/p/x,y,z
	  //  so 1 row
	  //
	  //  or each of {col1:x,col2:y,col3:z} and {col1:a,col2:b,col3:c} in this:
	  //    ...j=[{qname:queryname,DATA:[{col1:x,col2:y,col3:z},{col1:a,col2:b,col3:c}]}]
	  //  so 2 rows
	  //
		int rows = yq.getData().size() > 0 ? yq.getData().size() : 1;
			
		
		// 4) process the url 
		String      urlString; 
		HttpRequest request = null;
		
		for(int row=0;row<rows;row++)
		{
			
			// creates result array and assigns it
			setYADAQueryResultForYADAQuery(yq);

			try
			{

				// replace yada markup
				urlString = setPositionalParameterValues(yq,row);
				
				// get body content 
				String payload = null;
				if(yq.getData().size() > 0 && yq.getDataRow(row).containsKey(YADA_PAYLOAD))
						payload = yq.getDataRow(row).get(YADA_PAYLOAD)[0];
				
				// init url
				GenericUrl url     = new GenericUrl(urlString);
				
				// build request, handling auth if necessary
				request = buildRequest(yq,payload,url);

				// cookies
				setCookies(yq,request);
				
				// headers
				setHeaders(yq,request);
				
				// inject body content if necessary
//				handlePostPutPatch(request, yq, row);
				
				// debug
				logRequest(request);

				// process the response
				String result = processResponse(request);
				
				// store the response in the YADAQueryResult
				yq.getResult().addResult(row, result);
			}
			catch (YADAQueryConfigurationException e) 
			{
				String msg = "The YADAQuery was configured improperly. Check YADA parameters, especially 'oauth'.";
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
