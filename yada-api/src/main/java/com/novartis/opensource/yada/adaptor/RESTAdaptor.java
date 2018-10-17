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
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.novartis.opensource.yada.ConnectionFactory;
import com.novartis.opensource.yada.YADAQuery;
import com.novartis.opensource.yada.YADAQueryConfigurationException;
import com.novartis.opensource.yada.YADAQueryResult;
import com.novartis.opensource.yada.YADARequest;


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
	private final static String CHARSET_UTF8 = "UTF-8";
	
	private final static String OAUTH_VERIFIER = "oauth_verifier";
	private final static String OAUTH_TIMESTAMP = "oauth_timestamp";
	private final static String OAUTH_NONCE = "oauth_nonce";
	private final static String OAUTH_SIGNATURE = "oauth_signature";
	private final static String OAUTH_REALM = "OAuth realm";
	private final static String SPACE = " ";
	private final static String EQUAL = "=";
	private final static String QUOTE = "\"";
	private final static String COMMA = ",";
	private final static String AMP   = "&";
	private final static String CR    = "\r";
	private final static String LF    = "\n";
	private final static String CRLF  = CR+LF;
	private final static char URL_QUERY_MARKER = '?';
	private final static String BODY  = "body";
	
	/**
	 * Secure random number generator to sign requests.  
	 * @see <a href="https://github.com/googleapis/google-oauth-java-client/blob/master/google-oauth-client/src/main/java/com/google/api/client/auth/oauth/OAuthParameters.java">OAuthParameters.java</a>
	 * @since 8.7.0
	 */
	private final static SecureRandom RANDOM = new SecureRandom();
	private final static String ALGO_RSA = "RSA";
	private final static String ALGO_RSASHA1 = "SHA1withRSA";
	/**
	 * Constant equal to: {@value}
	 */
	protected final static String  PARAM_SYMBOL_RX = "([\\/=:~])(\\?[idvnt])";

	/**
	 * Constant equal to: {@value}
	 * {@link JSONParams} key for delivery of {@code HTTP POST, PUT, PATCH} body content
	 * @since 8.5.0
	 */
	private final static String YADA_PAYLOAD = "YADA_PAYLOAD";

	/**
	 * Constant equal to: {@value}
	 * Workaround for requests using {@code HTTP PATCH}
	 * @since 8.5.0
	 */
	private final static String X_HTTP_METHOD_OVERRIDE = "X-HTTP-Method-Override";
  private final static String X_AUTHORIZATION = "X-Authorization";
	private final static String CONTENT_LENGTH = "Content-length";
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
			this.oauth = yadaReq.getOAuth();			
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
	
	private void setAuthentication(YADAQuery yq, int row, HttpURLConnection hConn) throws YADAQueryConfigurationException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, IOException 
	{
		if(yq.hasParam(YADARequest.PS_OAUTH) || yq.hasParam(YADARequest.PL_OAUTH))
		{
			if(hConn.getURL().getUserInfo() != null) 
			{
				String msg  = "A query cannot contain both basic and oauth configurations. "
						        + "When using oauth, make sure the REST data source is not also configured with "
						   		  + "basic auth values.";
				throw new YADAQueryConfigurationException(msg);
			}
			String body = yq.getDataRow(row).get(YADA_PAYLOAD)[0];
			setAuthenticationOAuth(body,hConn);			
		}
		else if(hConn.getURL().getUserInfo() != null)
		{
			setAuthenticatonBasic(hConn.getURL(), hConn);
		}
	}
	
	/**
   * Generates a random nonce. This method originated at 
   * developer.pearson.com 
   * <a href="http://developer.pearson.com/learningstudio/oauth-1-sample-code">Oauth 1.0a Sample Code</a>
   * 
   * @return  A unique identifier for the request
   * @since 8.7.0
   */
  private static String getNonce()
  {
    return Long.toHexString(Math.abs(RANDOM.nextLong()));
  }
 
  /**
   * Generates an integer representing the number of seconds since the unix epoch using the
   * date/time the request is issued.  This method originated at
   * developer.pearson.com 
   * <a href="http://developer.pearson.com/learningstudio/oauth-1-sample-code">Oauth 1.0a Sample Code</a>
   * 
   * 
   * @return  A timestamp for the request
   * @since 8.7.0
   */
  private static String getTimestamp()
  {    
    return Long.toString((System.currentTimeMillis() / 1000));
  }
  
  /**
   * Generates an OAuth 1.0 signature. This method originated at
   * developer.pearson.com 
   * <a href="http://developer.pearson.com/learningstudio/oauth-1-sample-code">Oauth 1.0a Sample Code</a>
   * 
   * 
   * @param   httpMethod  The HTTP method of the request
   * @param   URL     The request URL
   * @param   oauthParams The associative set of signable oAuth parameters
   * @param   requestBody The serialized POST/PUT message body
   * @param   secret    Alphanumeric string used to validate the identity of the education partner (Private Key)
   * 
   * @return  A string containing the Base64-encoded signature digest
   * 
   * @throws  UnsupportedEncodingException
   * @throws SignatureException 
   * @throws InvalidKeySpecException 
   * @throws NoSuchAlgorithmException 
   * @throws InvalidKeyException 
   * @since 8.7.0
   */  
  private static String generateSignature(
      String httpMethod,
      URL url,
      Map<String, String> oauthParams,
      byte[] requestBody,
      String secret
  ) throws UnsupportedEncodingException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException
  {
    // Ensure the HTTP Method is upper-cased
    httpMethod = httpMethod.toUpperCase();
 
    // Construct the URL-encoded OAuth parameter portion of the signature base string
    String encodedParams = normalizeParams(httpMethod, url, oauthParams, requestBody);
 
    // URL-encode the relative URL
    String encodedUri = URLEncoder.encode(url.getPath(), CHARSET_UTF8);
 
    // Build the signature base string to be signed with the Consumer Secret
    String baseString = String.format("%s&%s&%s", httpMethod, encodedUri, encodedParams);
 
    return generateRSA(secret, baseString);
    
  }
	
  /**
   * Normalizes all OAuth signable parameters and url query parameters according to OAuth 1.0.  This method originated at
   * developer.pearson.com 
   * <a href="http://developer.pearson.com/learningstudio/oauth-1-sample-code">Oauth 1.0a Sample Code</a>
   * 
   * 
   * @param   httpMethod  The upper-cased HTTP method
   * @param   URL     The request URL
   * @param   oauthParams The associative set of signable oAuth parameters
   * @param   requstBody  The serialized POST/PUT message body
   * 
   * @return  A string containing normalized and encoded oAuth parameters
   * 
   * @throws  UnsupportedEncodingException
   * @since 8.7.0
   */
  private static String normalizeParams(
      String httpMethod,
      URL url,
      Map<String, String> oauthParams,
      byte[] requestBody
  ) throws UnsupportedEncodingException
  {
 
    // Sort the parameters in lexicographical order, 1st by Key then by Value
    Map<String, String> kvpParams = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
    kvpParams.putAll(oauthParams); 
 
    // Place any query string parameters into a key value pair using equals ("=") to mark
    // the key/value relationship and join each parameter with an ampersand ("&")
    if (url.getQuery() != null)
    {
      for(String keyValue : url.getQuery().split(AMP))
      {
        String[] p = keyValue.split(EQUAL);
        kvpParams.put(p[0],p[1]);
      }
 
    }
 
    // Include the body parameter if dealing with a POST or PUT request
    if (YADARequest.METHOD_POST.equals(httpMethod) || YADARequest.METHOD_PUT.equals(httpMethod))
    {
      String body = Base64.encodeBase64String(requestBody).replaceAll(CRLF, "");
      // url encode the body 2 times now before combining other params
      body = URLEncoder.encode(body, CHARSET_UTF8);
      body = URLEncoder.encode(body, CHARSET_UTF8);
      kvpParams.put(BODY, body);    
    }
 
    // separate the key and values with a "="
    // separate the kvp with a "&"
    StringBuilder combinedParams = new StringBuilder();
    String delimiter="";
    for(String key : kvpParams.keySet()) {
      combinedParams.append(delimiter);
      combinedParams.append(key);
      combinedParams.append(EQUAL);
      combinedParams.append(kvpParams.get(key));
      delimiter=AMP;
    }
 
    // url encode the entire string again before returning
    return URLEncoder.encode(combinedParams.toString(), CHARSET_UTF8);
  }
  
//TODO implement MAC support for Oauth 1.0a
//  Leave the commented out MAC stuff in here for now.  It's not pressing for the current requirement
//  but should be rectified soon.  It will require finding or building a provider in order to test, 
//  which could take some time.
//  
//  	 /**
//   * Generates a Base64-encoded CMAC-AES digest
//   * 
//   * @param   key The secret key used to sign the data
//   * @param   msg The data to be signed
//   * 
//   * @return  A CMAC-AES hash
//   * 
//   * @throws  UnsupportedEncodingException 
//   */
//  private static String generateCmac(String key, String msg) throws UnsupportedEncodingException
//  {
//    byte[] keyBytes = key.getBytes(CHARSET_UTF8);
//    byte[] data     = msg.getBytes(CHARSET_UTF8);
  
////  Person code: 
////    CMac macProvider = new CMac(new AESFastEngine());
////    macProvider.init(new KeyParameter(keyBytes));
////    macProvider.reset();
//// 
////    macProvider.update(data, 0, data.length);
////    byte[] output = new byte[macProvider.getMacSize()];
////    macProvider.doFinal(output, 0);

//    YADA stubby:
//    Mac mac = Mac.getInstance("HmacSHA1");
//    mac.init(secretKey);
//    
//    // Convert the CMAC to a Base64 string and remove the new line the Base64 library adds
//    String cmac = Base64.encodeBase64String(output).replaceAll("\r\n", "");
// 
//    return cmac;
//  	
//  }
  
  /**
   * Generates a Base64-encoded RSA-SHA1 digest
   * 
   * @param   key The secret key used to sign the data
   * @param   msg The data to be signed
   * 
   * @return  An RSA-SHA1 hash
   * 
   * @throws UnsupportedEncodingException 
   * @throws NoSuchAlgorithmException 
   * @throws InvalidKeySpecException 
   * @throws InvalidKeyException 
   * @throws SignatureException 
   */
  private static String generateRSA(String key, String msg) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException
  {
    byte[]    keyBytes = key.getBytes(CHARSET_UTF8);
    byte[]    data     = msg.getBytes(CHARSET_UTF8);
    Signature signer   = Signature.getInstance(ALGO_RSASHA1);
    KeyFactory kf = KeyFactory.getInstance(ALGO_RSA);
    PrivateKey privateKey = kf.generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
    signer.initSign(privateKey);
    signer.update(data);
    byte[]    signed = signer.sign();
    return Base64.encodeBase64String(signed);
  }
  
  /**
   * This method relies on passage of {@link YADAQuery#getOAuth()} which returns
   * a {@link JSONObject} containing:
   * 
   *   <ul>
   *    <li><code>oauth_consumer_key</code></li>
   *    <li><code>oauth_signature_method</code>. For now, should always equal <code>RSA</code></li>
   *    <li><code>oauth_token</code></li>
   *    <li><code>oauth_verifier (secret)</code></li>
   *    <li><code>oauth_version</code>. For now, should always equal <code>1.0a</code></li>
   *   </ul>
   *   
   * The logic of this method is derived from 
   * developer.pearson.com  
   * <a href="http://developer.pearson.com/learningstudio/oauth-1-sample-code">Oauth 1.0a Sample Code</a>
   *    
   * @param body the value associated to the <code>YADA_PAYLOAD</code> key in the current "row"
   * @param hConn the {@link HttpURLConnection} used to engage with the web service
   * @throws SignatureException 
   * @throws InvalidKeySpecException 
   * @throws NoSuchAlgorithmException 
   * @throws InvalidKeyException 
   * @throws IOException 
   * @since 8.7.0
   */
	private void setAuthenticationOAuth(String body, HttpURLConnection hConn) throws InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, IOException
	{
		
    String httpMethod  = hConn.getRequestMethod(); 
    URL    url         = hConn.getURL();
    byte[] requestBody = null;
    BufferedReader in  = null;
    
       // Set the request body if making a POST or PUT request
    //TODO handle body content--this is just YADA_PAYLOAD now which is probs totes wrong
    if (YADARequest.METHOD_POST.equals(httpMethod)  || YADARequest.METHOD_PUT.equals(httpMethod))
    {
      requestBody = body.getBytes(CHARSET_UTF8);
    }
 
    // Create the OAuth parameter map from the oauth yada param
    Map<String, String> oauthParams = new LinkedHashMap<String, String>();
    for(Object key : oauth.keySet())
    {
    	oauthParams.put((String)key, oauth.getString((String)key));
    }
    oauthParams.put(OAUTH_TIMESTAMP, getTimestamp());
    oauthParams.put(OAUTH_NONCE, getNonce());
 
    
    // Get the OAuth 1.0 Signature
    String secret    = oauthParams.get(OAUTH_VERIFIER);
    String signature = generateSignature(httpMethod, url, oauthParams, requestBody, secret);
    
    l.debug(String.format("OAuth 1.0 Signature = %s", signature));
 
    // Add the oauth_signature parameter to the set of OAuth Parameters
    oauthParams.put(OAUTH_SIGNATURE, signature);    
 
    // Generate a string of comma delimited: keyName="URL-encoded(value)" pairs
    StringBuilder paramStringBldr = new StringBuilder();
    String delimiter = "";
    for (String keyName : oauthParams.keySet()) {
    	paramStringBldr.append(delimiter);
      String value = oauthParams.get((String) keyName);
      paramStringBldr.append(keyName).append(EQUAL+QUOTE).append(URLEncoder.encode(value, CHARSET_UTF8)).append(QUOTE);
      delimiter=COMMA;
    }
 
    String urlString = url.toString();
    // omit the queryString from the url
    int startOfQueryString = urlString.indexOf(URL_QUERY_MARKER);
    if(startOfQueryString != -1) {
      urlString = urlString.substring(0,startOfQueryString);	
    }
 
    // Build the X-Authorization request header
    String xauth = String.format(OAUTH_REALM+EQUAL+QUOTE+"%s"+QUOTE+",%s", urlString, paramStringBldr.toString());
    l.debug(String.format("X-Authorization request header = %s", xauth));
 
    // Add the header
    hConn.addRequestProperty(X_AUTHORIZATION, xauth);  
	}
	
	/**
	 * Sets the <code>Authorization</code> header for the request with Base64-encoded key/value pair
	 * @param url the URL object
	 * @param conn the URLConnection object
	 * @since 8.7.0
	 */
	private void setAuthenticatonBasic(URL url, URLConnection conn) 
	{
		//TODO basic auth and other auth methods should be mutually exclusive
		if (url.getUserInfo() != null)
		{
			//TODO issue with '@' sign in pw, must decode first
		  String basicAuth = "Basic " + new String(new Base64().encode(url.getUserInfo().getBytes()));
		  conn.setRequestProperty("Authorization", basicAuth);
		}
	}
	
	/**
	 * Looks for cookie strings passed in the request or stored in the {@link YADAQuery} 
	 * and adds them to the {@link URLConnection}
	 * @param yq The YADAQuery object
	 * @param conn The URLConnection object
	 * @since 8.7.0
	 */
	private void setCookies(YADAQuery yq, URLConnection conn)
	{
		if(yq.getCookies() != null && yq.getCookies().size() > 0)
		{
		  String cookieStr = "";
		  for (HttpCookie cookie : yq.getCookies())
		  {
		    cookieStr += cookie.getName()+"="+cookie.getValue()+";";
		  }
		  conn.setRequestProperty("Cookie", cookieStr);
		}
	}
	
	/**
	 * Looks for http header strings passed in the request or stored in the {@link YADAQuery}
	 * and adds them to the {@link URLConnection}
	 * @param yq the YADAQuery object
	 * @param conn the URLConnection object
	 * @since 8.7.0
	 */
	private void setHeaders(YADAQuery yq, URLConnection conn)
	{
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
	}
	
	/**
	 * Injects the payload into the request's {@link OutputStream} if {@link YADARequest#getMethod()} returns
	 * {@link YADARequest#METHOD_POST}, {@link YADARequest#METHOD_PUT}, or {@link YADARequest#METHOD_PATCH} 
	 * @param yq The current YADAQuery object
	 * @param row The current data "row"
	 * @param hConn The HTTPUrlConnection into which the data will be put
	 * @throws YADAAdaptorExecutionException in the event of an issue writing to the output stream.
	 * @since 8.7.0
	 */
	private void handlePostPutPatch(YADAQuery yq, int row, HttpURLConnection hConn) throws YADAAdaptorExecutionException {
		boolean isPostPutPatch = this.method.equals(YADARequest.METHOD_POST)
				|| this.method.equals(YADARequest.METHOD_PUT)
				|| this.method.equals(YADARequest.METHOD_PATCH);
		
		if(!this.method.equals(YADARequest.METHOD_GET))
		{
			try
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
					writer = new OutputStreamWriter(hConn.getOutputStream());
					writer.write(payload.toString());
			    writer.flush();
				}
			}
			catch (IOException e)
			{
				String msg = "There was a problem injecting the payload into the request";
				throw new YADAAdaptorExecutionException(msg, e);
			}
		}
	}
	
	/**
	 * Reads the response {@link InputStream} and writes it to a {@link String}
	 * @param hConn the connection object
	 * @return a String containing the response data
	 * @throws YADAAdaptorExecutionException when reading the input stream fails
	 * @since 8.7.0
	 */
	private String processResponse(HttpURLConnection hConn) throws YADAAdaptorExecutionException 
	{
		String result = "";
		try(BufferedReader in = new BufferedReader(new InputStreamReader(hConn.getInputStream())))
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
	 * Writes the header fields to the console or log
	 * @param conn the current connection object
	 * @since 8.7.0
	 */
	private void logRequest(URLConnection conn) {
		Map<String, List<String>> map = conn.getHeaderFields();
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			l.debug("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
		}
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
		URL url; 
		URLConnection conn; 
		HttpURLConnection hConn = null;
		for(int row=0;row<rows;row++)
		{
			// creates result array and assigns it
			setYADAQueryResultForYADAQuery(yq);

			try
			{
				// set positional parameter values and create connection
				url   = new URL(setPositionalParameterValues(yq,row));
				conn  = null;

				// open connection or proxy connection
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

				hConn = (HttpURLConnection)conn;

				// cookies
				setCookies(yq, hConn);
				
				// headers
				setHeaders(yq, hConn);
					
				// handle auth if necessary
				setAuthentication(yq,row,hConn);
				
				// inject body content if necessary
				handlePostPutPatch(yq, row, hConn);
				
				// debug
				logRequest(hConn);

				// process the response
				String result = processResponse(hConn);
				
				// store the response in the YADAQueryResult
				yq.getResult().addResult(row, result);
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
			catch (YADAQueryConfigurationException e) 
			{
				String msg = "The YADAQuery was configured improperly. Check YADA parameters, especially 'oauth'.";
				throw new YADAAdaptorExecutionException(msg, e);
			} 
			catch (InvalidKeyException|NoSuchAlgorithmException|InvalidKeySpecException|SignatureException e) 
			{
				String msg = "There was a problem oauth signature encryption. It could be related to key validation, alogorithm spec, etc.";
				throw new YADAAdaptorExecutionException(msg, e);
			} 
			finally
			{
				if(hConn != null) hConn.disconnect();
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
